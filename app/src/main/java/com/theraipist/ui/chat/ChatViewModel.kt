package com.theraipist.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.media.MediaPlayer
import com.theraipist.core.ActiveSessionHolder
import com.theraipist.core.PersonaHolder
import com.theraipist.core.GraphHolder
import com.theraipist.core.ModelSettings
import com.theraipist.core.chat.ChatService
import com.theraipist.core.chat.ChatServiceException
import com.theraipist.core.chat.MissingApiKeyException
import com.theraipist.core.graph.InsightExtractor
import com.theraipist.core.local.DownloadStatus
import com.theraipist.core.local.GGUFModelCatalog
import com.theraipist.core.local.LocalLLMService
import com.theraipist.core.local.LocalModel
import com.theraipist.core.local.ModelDownloader
import com.theraipist.core.model.Message
import com.theraipist.core.model.Persona
import com.theraipist.core.model.Role
import com.theraipist.core.modality.ModalityRouter
import com.theraipist.core.modality.TherapyModality
import com.theraipist.core.prompt.TherapyPromptBuilder
import com.theraipist.core.repository.SessionRepository
import com.theraipist.core.safety.SafetyGuardrails
import com.theraipist.core.voice.LocalTtsService
import com.theraipist.core.voice.TtsRequest
import com.theraipist.core.voice.TtsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

private const val TTS_FAILED_MESSAGE =
    "Couldn't read that reply aloud. The reply itself is fine — only the audio failed."

private const val EMPTY_REPLY_MESSAGE =
    "The model didn't send anything back. Please try again."

private const val BOUNDARY_FALLBACK_MESSAGE =
    "I want to be careful here — I can't diagnose or prescribe anything. " +
        "That's something a licensed professional needs to weigh in on. What I can do is keep exploring this with you — what's coming up for you right now?"

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val chatService: ChatService,
    private val modalityRouter: ModalityRouter,
    private val promptBuilder: TherapyPromptBuilder,
    private val safety: SafetyGuardrails,
    private val personaHolder: PersonaHolder,
    private val graphHolder: GraphHolder,
    private val ttsService: TtsService,
    private val localTtsService: LocalTtsService,
    private val modelSettings: ModelSettings,
    private val localLLMService: LocalLLMService,
    private val modelDownloader: ModelDownloader,
    private val activeSessionHolder: ActiveSessionHolder
) : ViewModel() {

    private var sessionId: String? = null
    private var loadedModelId: String? = null
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    private val _ttsEnabled = MutableStateFlow(false)
    val ttsEnabled = _ttsEnabled.asStateFlow()

    init {
        activeSessionHolder.consumePendingOpen()?.let { id -> openSession(id) }
    }

    fun setTtsEnabled(enabled: Boolean) {
        _ttsEnabled.value = enabled
    }

    private fun openSession(id: String) {
        sessionId = id
        viewModelScope.launch {
            val messages = sessionRepository.getMessages(id)
            _uiState.update { it.copy(messages = messages) }
        }
    }

    fun send(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return
        if (_uiState.value.isSending) return
        _uiState.update { it.copy(isSending = true) }
        viewModelScope.launch {
            runCatching { doSend(trimmed) }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            errorMessage = e.message ?: "Something went wrong sending your message.",
                            needsApiKey = e is MissingApiKeyException
                        )
                    }
                }
            _uiState.update { it.copy(isSending = false) }
        }
    }

    private suspend fun doSend(trimmed: String) {
        if (sessionId == null) {
            checkReEntry()
            sessionId = sessionRepository.createSession(personaHolder.persona.value).id
        }
        val sid = sessionId!!
        val persona: Persona = personaHolder.persona.value
        val modality = _uiState.value.selectedModality ?: modalityRouter.select(trimmed)
        val crisis = safety.detectCrisis(trimmed)
        if (crisis != null) {
            _uiState.update {
                it.copy(crisisLevel = crisis, resourceMessage = safety.resourceMessage())
            }
        }
        val history = sessionRepository.getMessages(sid)
        val userMessage = Message(
            id = "u-${System.nanoTime()}",
            role = Role.USER,
            content = trimmed,
            modality = modality.name
        )
        sessionRepository.appendMessage(sid, userMessage)
        _uiState.update { it.copy(messages = it.messages + userMessage) }

        val promptKey = modalityRouter.promptKey(modality)
        val conversation = promptBuilder.buildConversation(persona, promptKey, history, trimmed)
        val reply = streamReply(sid, conversation, modality)
        val insights = InsightExtractor.extract(reply)
        runCatching { graphHolder.addInsights(sid, insights) }
        _uiState.update { it.copy(graphNodes = graphHolder.nodes.value) }
        if (reply.isNotBlank() && _ttsEnabled.value) {
            speak(reply)
        }
    }

    /**
     * Streams the reply into a live-updating assistant bubble (added empty, then
     * grown as text arrives). The boundary check runs on each partial rather than
     * only on the finished reply, so a violating sentence is never rendered even
     * briefly, and whatever arrived is settled on the way out of both the success
     * and the failure path — otherwise a mid-stream error would leave text on
     * screen that was never written to the session the model is shown next turn.
     */
    private suspend fun streamReply(
        sessionId: String,
        conversation: List<Message>,
        modality: TherapyModality
    ): String {
        val assistantId = "a-${System.nanoTime()}"
        _uiState.update {
            it.copy(
                messages = it.messages +
                    Message(id = assistantId, role = Role.ASSISTANT, content = "", modality = modality.name)
            )
        }

        val accumulated = StringBuilder()
        try {
            produceReply(conversation)
                .takeWhile { delta ->
                    accumulated.append(delta)
                    !safety.detectBoundaryViolation(accumulated.toString())
                }
                .collect { showPartial(assistantId, accumulated.toString()) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            settleReply(sessionId, assistantId, accumulated.toString(), modality)
            throw e
        }

        val reply = settleReply(sessionId, assistantId, accumulated.toString(), modality)
        if (reply.isBlank()) throw ChatServiceException(EMPTY_REPLY_MESSAGE)
        return reply
    }

    /**
     * Applies the boundary check to whatever was accumulated, writes it through to
     * storage, and returns the text left on screen. A blank result means nothing
     * usable arrived, so the placeholder is dropped rather than leaving an empty
     * bubble in the UI and an empty assistant turn in the session history.
     */
    private suspend fun settleReply(
        sessionId: String,
        assistantId: String,
        raw: String,
        modality: TherapyModality
    ): String {
        val finalReply = if (safety.detectBoundaryViolation(raw)) BOUNDARY_FALLBACK_MESSAGE else raw
        if (finalReply.isBlank()) {
            _uiState.update { state ->
                state.copy(messages = state.messages.filterNot { it.id == assistantId })
            }
            return ""
        }
        showPartial(assistantId, finalReply)
        sessionRepository.appendMessage(
            sessionId,
            Message(id = assistantId, role = Role.ASSISTANT, content = finalReply, modality = modality.name)
        )
        return finalReply
    }

    private fun showPartial(assistantId: String, content: String) {
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map {
                    if (it.id == assistantId) it.copy(content = content) else it
                }
            )
        }
    }

    /** Shows a gentle check-in if the most recent prior session ended on a crisis message. */
    private suspend fun checkReEntry() {
        val previousSession = sessionRepository.listSessions().maxByOrNull { it.updatedAt } ?: return
        val previousMessages = sessionRepository.getMessages(previousSession.id)
        val hadCrisis = previousMessages.any {
            it.role == Role.USER && safety.detectCrisis(it.content) != null
        }
        safety.reEntryCheck(hadCrisis)?.let { message ->
            _uiState.update { it.copy(reEntryMessage = message) }
        }
    }

    private suspend fun produceReply(conversation: List<Message>): Flow<String> {
        if (!modelSettings.useLocalModel.value) return chatService.sendStreaming(conversation)
        val id = modelSettings.localModelId.value ?: return chatService.sendStreaming(conversation)
        val model = GGUFModelCatalog.byId(id) ?: return chatService.sendStreaming(conversation)
        if (!ensureLocalModel(model)) return chatService.sendStreaming(conversation)
        return localWithCloudFallback(conversation)
    }

    /**
     * On-device generation, falling back to the cloud when it fails outright or
     * produces nothing — native inference can OOM or hit a malformed chat template,
     * and a silent empty reply is indistinguishable from a broken model. Once any
     * text has been emitted the fallback is off the table: switching sources
     * mid-sentence would splice two different replies together.
     */
    private fun localWithCloudFallback(conversation: List<Message>): Flow<String> = flow {
        var emitted = false
        try {
            localLLMService.stream(conversation).collect { delta ->
                emitted = true
                emit(delta)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (emitted) throw e
        }
        if (!emitted) emitAll(chatService.sendStreaming(conversation))
    }

    private suspend fun ensureLocalModel(model: LocalModel): Boolean {
        if (localLLMService.isModelLoaded() && loadedModelId == model.id) return true
        if (modelDownloader.status(model) != DownloadStatus.DOWNLOADED) return false
        return runCatching {
            localLLMService.load(model, modelDownloader.localFile(model).absolutePath)
            loadedModelId = model.id
            localLLMService.isModelLoaded()
        }.getOrDefault(false)
    }

    private fun speak(text: String) {
        if (modelSettings.useLocalTts.value) {
            runCatching { localTtsService.speak(text) }
                .onFailure { reportPlaybackFailure() }
            return
        }
        viewModelScope.launch {
            runCatching {
                val audio = ttsService.synthesize(TtsRequest(input = text))
                playMp3(audio)
            }.onFailure { reportPlaybackFailure() }
        }
    }

    /**
     * Read-aloud is a side channel — the reply itself is already on screen, so a
     * failure here is reported without disturbing the conversation. Staying silent
     * would be indistinguishable from the feature simply not working.
     */
    private fun reportPlaybackFailure() {
        _uiState.update { it.copy(errorMessage = TTS_FAILED_MESSAGE) }
    }

    private suspend fun playMp3(bytes: ByteArray) = withContext(Dispatchers.IO) {
        val file = File.createTempFile("theraipist_tts_", ".mp3")
        val player = MediaPlayer()
        try {
            file.writeBytes(bytes)
            player.setDataSource(file.absolutePath)
            player.prepare()
            player.start()
            player.setOnCompletionListener {
                it.release()
                file.delete()
            }
        } catch (e: Exception) {
            player.release()
            file.delete()
            throw e
        }
    }

    fun clearCrisis() {
        _uiState.update { it.copy(crisisLevel = null, resourceMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null, needsApiKey = false) }
    }

    fun clearReEntry() {
        _uiState.update { it.copy(reEntryMessage = null) }
    }

    /** Overrides auto-detection for the next message; pass null to resume auto-detecting. */
    fun selectModality(modality: TherapyModality?) {
        _uiState.update { it.copy(selectedModality = modality) }
    }
}
