package com.theraipist.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.media.MediaPlayer
import com.theraipist.core.PersonaHolder
import com.theraipist.core.GraphHolder
import com.theraipist.core.chat.ChatService
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

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val chatService: ChatService,
    private val modalityRouter: ModalityRouter,
    private val promptBuilder: TherapyPromptBuilder,
    private val safety: SafetyGuardrails,
    private val personaHolder: PersonaHolder,
    private val graphHolder: GraphHolder,
    private val ttsService: TtsService
) : ViewModel() {

    private var sessionId: String? = null
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
        viewModelScope.launch {
            if (sessionId == null) {
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
            _uiState.update { it.copy(messages = it.messages + userMessage, isSending = true) }

            val conversation = promptBuilder.buildConversation(persona, modality.name, history, trimmed)
            val reply = chatService.send(conversation)
            val assistantMessage = Message(
                id = "a-${System.nanoTime()}",
                role = Role.ASSISTANT,
                content = reply,
                modality = modality.name
            )
            sessionRepository.appendMessage(sid, assistantMessage)
            val insights = com.theraipist.core.graph.InsightExtractor.extract(reply)
            graphHolder.addInsights(insights)
            _uiState.update {
                it.copy(
                    messages = it.messages + assistantMessage,
                    isSending = false,
                    graphNodes = graphHolder.nodes.value
                )
            }
            if (_ttsEnabled.value) {
                speak(reply)
            }
        }
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
        try {
            file.writeBytes(bytes)
            val player = MediaPlayer()
            player.setDataSource(file.absolutePath)
            player.prepare()
            player.start()
            player.setOnCompletionListener {
                it.release()
                file.delete()
            }
        } catch (_: Exception) {
            file.delete()
        }
    }

    fun clearCrisis() {
        _uiState.update { it.copy(crisisLevel = null, resourceMessage = null) }
    }
}
