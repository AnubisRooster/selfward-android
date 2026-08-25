package com.selfward.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.media.MediaPlayer
import com.selfward.core.ActiveSessionHolder
import com.selfward.core.PersonaHolder
import com.selfward.core.GraphHolder
import com.selfward.core.ModelSettings
import com.selfward.core.chat.ChatService
import com.selfward.core.catalog.ModelRanking
import com.selfward.core.catalog.ModelRefusal
import com.selfward.core.catalog.OpenRouterCatalog
import com.selfward.core.catalog.OpenRouterModel
import com.selfward.core.catalog.UnusableModels
import com.selfward.core.chat.Provider
import com.selfward.core.settings.SecureSettings
import com.selfward.core.chat.ChatServiceException
import com.selfward.core.chat.MissingApiKeyException
import com.selfward.core.graph.InsightExtractor
import com.selfward.core.intake.IntakeContext
import com.selfward.core.intake.IntakeStore
import com.selfward.core.local.DownloadStatus
import com.selfward.core.local.GGUFModelCatalog
import com.selfward.core.local.LocalLLMService
import com.selfward.core.local.LocalModel
import com.selfward.core.local.ModelDownloader
import com.selfward.core.model.Message
import com.selfward.core.model.Persona
import com.selfward.core.model.Role
import com.selfward.core.modality.ModalityRouter
import com.selfward.core.modality.TherapyModality
import com.selfward.core.prompt.TherapyPromptBuilder
import com.selfward.core.repository.SessionRepository
import com.selfward.core.safety.SafetyGuardrails
import com.selfward.core.voice.LocalTtsService
import com.selfward.core.voice.TtsRequest
import com.selfward.core.voice.TtsService
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
    private val activeSessionHolder: ActiveSessionHolder,
    private val intakeStore: IntakeStore,
    private val secureSettings: SecureSettings,
    private val openRouterCatalog: OpenRouterCatalog,
    private val unusableModels: UnusableModels
) : ViewModel() {

    private var sessionId: String? = null

    /** Free models to choose between, best first. Refreshed each time the app opens. */
    private val _freeModels = MutableStateFlow<List<OpenRouterModel>>(emptyList())
    val freeModels = _freeModels.asStateFlow()

    /**
     * The persona the open session was started with. A session carries its own
     * persona, as on iOS, so reopening an old conversation keeps the companion
     * it was held with rather than adopting whatever was last selected.
     */
    private var sessionPersona: Persona? = null
    private var loadedModelId: String? = null
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    private val _ttsEnabled = MutableStateFlow(false)
    val ttsEnabled = _ttsEnabled.asStateFlow()

    init {
        activeSessionHolder.consumePendingOpen()?.let { id -> openSession(id) }
        publishModelLabel()
        refreshFreeModels()
    }

    fun setTtsEnabled(enabled: Boolean) {
        _ttsEnabled.value = enabled
    }

    private fun openSession(id: String) {
        sessionId = id
        viewModelScope.launch {
            val session = sessionRepository.getSession(id)
            sessionPersona = session?.persona
            val messages = sessionRepository.getMessages(id)
            _uiState.update { it.copy(messages = messages, sessionTitle = session?.title) }
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
                    // A model that refuses to serve is the app's problem, not
                    // the client's: it chose the model. Set it aside, move to
                    // the next best free one, and send again before surfacing
                    // anything.
                    if (retryOnAnotherFreeModel(e)) {
                        runCatching { doSend(trimmed) }
                            .onFailure { second -> showError(second) }
                    } else {
                        showError(e)
                    }
                }
            _uiState.update { it.copy(isSending = false) }
        }
    }

    /**
     * Refetches the catalogue so the list is current every time the app opens.
     *
     * Forced rather than served from the day-old cache: free models appear and
     * are withdrawn constantly, and a stale list is how someone ends up on a
     * model that was retired last week. The cache still covers being offline.
     */
    fun refreshFreeModels() {
        viewModelScope.launch {
            val catalogue = runCatching {
                openRouterCatalog.models(secureSettings.apiKey, forceRefresh = true)
            }.getOrDefault(openRouterCatalog.cached())
            _freeModels.value = ModelRanking.freeModels(catalogue, unusableModels.all())
        }
    }

    /** Switches the model mid-conversation, from the chat screen. */
    fun selectModel(modelId: String) {
        secureSettings.save(Provider.OPENROUTER, secureSettings.apiKey.orEmpty(), modelId)
        publishModelLabel()
        _uiState.update { it.copy(modelNotice = null) }
    }

    fun dismissModelNotice() = _uiState.update { it.copy(modelNotice = null) }

    private fun publishModelLabel() {
        val label = if (modelSettings.useLocalModel.value) {
            modelSettings.localModelId.value?.let { "On device · $it" } ?: "On device"
        } else {
            secureSettings.model.substringAfterLast('/')
        }
        _uiState.update { it.copy(modelLabel = label) }
    }

    private fun showError(e: Throwable) {
        _uiState.update {
            it.copy(
                errorMessage = e.message ?: "Something went wrong sending your message.",
                needsApiKey = e is MissingApiKeyException
            )
        }
    }

    /**
     * @return true when the failure was a refusal by the selected OpenRouter
     *   model and a different free one has been put in its place.
     */
    private suspend fun retryOnAnotherFreeModel(e: Throwable): Boolean {
        if (secureSettings.provider != Provider.OPENROUTER) return false
        if (!ModelRefusal.isPermanent(e.message)) return false

        val failed = secureSettings.model
        unusableModels.remember(failed, e.message.orEmpty())

        val catalogue = runCatching { openRouterCatalog.models(secureSettings.apiKey) }
            .getOrDefault(emptyList())
        val next = ModelRanking.nextFreeAfter(catalogue, failed, unusableModels.all())
            ?: return false

        secureSettings.save(Provider.OPENROUTER, secureSettings.apiKey.orEmpty(), next.id)
        publishModelLabel()
        _uiState.update { it.copy(modelNotice = "${failed.substringAfterLast('/')} wouldn't take the request, so this is ${next.shortName} instead.") }
        return true
    }

    private suspend fun doSend(trimmed: String) {
        if (sessionId == null) {
            checkReEntry()
            val created = sessionRepository.createSession(personaHolder.persona.value)
            sessionId = created.id
            sessionPersona = created.persona
            _uiState.update { it.copy(sessionTitle = created.title) }
        }
        val sid = sessionId!!
        val persona: Persona = sessionPersona ?: personaHolder.persona.value
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

        // Read what the client wrote into the graph before the reply is
        // requested, so Insights reflects the message even if the request fails.
        runCatching { graphHolder.analyzeMessage(sid, trimmed) }

        val promptKey = modalityRouter.promptKey(modality)
        // Resolved up front because it decides whether the intake block may be
        // included at all: it is only ever shown to a model running on-device.
        val localModel = resolveUsableLocalModel()
        val cloudConversation = promptBuilder.buildConversation(persona, promptKey, history, trimmed)
        val conversation = if (localModel == null) {
            cloudConversation
        } else {
            promptBuilder.buildConversation(
                persona, promptKey, history, trimmed,
                intakeContext = IntakeContext.block(intakeStore.load())
            )
        }

        val reply = streamReply(sid, conversation, cloudConversation, localModel, modality)
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
        cloudConversation: List<Message>,
        localModel: LocalModel?,
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
            produceReply(conversation, cloudConversation, localModel)
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

    /** The on-device model to use for this turn, or null when the cloud will answer. */
    private suspend fun resolveUsableLocalModel(): LocalModel? {
        if (!modelSettings.useLocalModel.value) return null
        val id = modelSettings.localModelId.value ?: return null
        val model = GGUFModelCatalog.byId(id) ?: return null
        return if (ensureLocalModel(model)) model else null
    }

    private fun produceReply(
        conversation: List<Message>,
        cloudConversation: List<Message>,
        localModel: LocalModel?
    ): Flow<String> {
        if (localModel == null) return chatService.sendStreaming(cloudConversation)
        return localWithCloudFallback(conversation, cloudConversation)
    }

    /**
     * On-device generation, falling back to the cloud when it fails outright or
     * produces nothing — native inference can OOM or hit a malformed chat template,
     * and a silent empty reply is indistinguishable from a broken model. Once any
     * text has been emitted the fallback is off the table: switching sources
     * mid-sentence would splice two different replies together.
     *
     * The fallback deliberately sends [cloudConversation], not the local one: the
     * local prompt carries the intake block, and that must never reach a provider.
     */
    private fun localWithCloudFallback(
        conversation: List<Message>,
        cloudConversation: List<Message>
    ): Flow<String> = flow {
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
        if (!emitted) emitAll(chatService.sendStreaming(cloudConversation))
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
        val file = File.createTempFile("selfward_tts_", ".mp3")
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
