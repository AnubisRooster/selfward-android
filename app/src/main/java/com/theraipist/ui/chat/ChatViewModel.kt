package com.theraipist.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.media.MediaPlayer
import com.theraipist.core.PersonaHolder
import com.theraipist.core.GraphHolder
import com.theraipist.core.ModelSettings
import com.theraipist.core.chat.ChatService
import com.theraipist.core.graph.InsightExtractor
import com.theraipist.core.local.GGUFModelCatalog
import com.theraipist.core.local.LocalLLMService
import com.theraipist.core.local.LocalModel
import com.theraipist.core.model.Message
import com.theraipist.core.model.Persona
import com.theraipist.core.model.Role
import com.theraipist.core.modality.ModalityRouter
import com.theraipist.core.prompt.TherapyPromptBuilder
import com.theraipist.core.repository.SessionRepository
import com.theraipist.core.safety.SafetyGuardrails
import com.theraipist.core.voice.TtsRequest
import com.theraipist.core.voice.TtsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

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
    private val modelSettings: ModelSettings,
    private val localLLMService: LocalLLMService
) : ViewModel() {

    private val modelDir = "/sdcard/Android/data/com.theraipist.app/files/models"

    private var sessionId: String? = null
    private var loadedModelId: String? = null
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    private val _ttsEnabled = MutableStateFlow(false)
    val ttsEnabled = _ttsEnabled.asStateFlow()

    fun setTtsEnabled(enabled: Boolean) {
        _ttsEnabled.value = enabled
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
                        it.copy(errorMessage = e.message ?: "Something went wrong sending your message.")
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
        val modality = modalityRouter.select(trimmed)
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
        val rawReply = produceReply(conversation)
        val reply = if (safety.detectBoundaryViolation(rawReply)) BOUNDARY_FALLBACK_MESSAGE else rawReply
        val assistantMessage = Message(
            id = "a-${System.nanoTime()}",
            role = Role.ASSISTANT,
            content = reply,
            modality = modality.name
        )
        sessionRepository.appendMessage(sid, assistantMessage)
        val insights = InsightExtractor.extract(reply)
        graphHolder.addInsights(insights)
        _uiState.update {
            it.copy(
                messages = it.messages + assistantMessage,
                graphNodes = graphHolder.nodes.value
            )
        }
        if (_ttsEnabled.value) {
            speak(reply)
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

    private suspend fun produceReply(conversation: List<Message>): String {
        if (!modelSettings.useLocalModel.value) return chatService.send(conversation)
        val id = modelSettings.localModelId.value ?: return chatService.send(conversation)
        val model = GGUFModelCatalog.byId(id) ?: return chatService.send(conversation)
        if (!ensureLocalModel(model)) return chatService.send(conversation)
        return runCatching { localLLMService.generate(conversation) }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: chatService.send(conversation)
    }

    private suspend fun ensureLocalModel(model: LocalModel): Boolean {
        if (localLLMService.isModelLoaded() && loadedModelId == model.id) return true
        return runCatching {
            localLLMService.load(model, "$modelDir/${model.fileName}")
            loadedModelId = model.id
            localLLMService.isModelLoaded()
        }.getOrDefault(false)
    }

    private fun speak(text: String) {
        viewModelScope.launch {
            runCatching {
                val audio = ttsService.synthesize(TtsRequest(input = text))
                playMp3(audio)
            }
        }
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
        } catch (_: Exception) {
            player.release()
            file.delete()
        }
    }

    fun clearCrisis() {
        _uiState.update { it.copy(crisisLevel = null, resourceMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearReEntry() {
        _uiState.update { it.copy(reEntryMessage = null) }
    }
}
