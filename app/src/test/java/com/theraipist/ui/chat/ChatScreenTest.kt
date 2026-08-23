package com.theraipist.ui.chat

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.theraipist.core.ActiveSessionHolder
import com.theraipist.core.GraphHolder
import com.theraipist.core.ModelSettings
import com.theraipist.core.PersonaHolder
import com.theraipist.core.chat.ChatService
import com.theraipist.core.graph.GraphEdge
import com.theraipist.core.graph.GraphNode
import com.theraipist.core.local.DownloadProgress
import com.theraipist.core.local.DownloadStatus
import com.theraipist.core.local.LocalLLMService
import com.theraipist.core.local.LocalModel
import com.theraipist.core.local.ModelDownloader
import com.theraipist.core.modality.ModalityRouter
import com.theraipist.core.modality.TherapyModality
import com.theraipist.core.model.Message
import com.theraipist.core.model.Persona
import com.theraipist.core.model.Role
import com.theraipist.core.prompt.TherapyPromptBuilder
import com.theraipist.core.repository.GraphRepository
import com.theraipist.core.repository.GraphSnapshot
import com.theraipist.core.repository.Session
import com.theraipist.core.repository.SessionRepository
import com.theraipist.core.repository.SessionSummary
import com.theraipist.core.safety.SafetyGuardrails
import com.theraipist.core.voice.LocalTtsService
import com.theraipist.core.voice.TtsRequest
import com.theraipist.core.voice.TtsService
import com.theraipist.data.settings.SecureSettings
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ChatScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private class FakeSessionRepository : SessionRepository {
        private val sessions = mutableListOf<Session>()
        private val messagesBySession = mutableMapOf<String, MutableList<Message>>()
        private var counter = 0

        override suspend fun createSession(persona: Persona, title: String): Session {
            counter += 1
            val session = Session("s$counter", persona, title, counter.toLong(), counter.toLong())
            sessions.add(session)
            messagesBySession[session.id] = mutableListOf()
            return session
        }

        override suspend fun appendMessage(sessionId: String, message: Message) {
            messagesBySession.getOrPut(sessionId) { mutableListOf() }.add(message)
        }

        override suspend fun getMessages(sessionId: String): List<Message> =
            messagesBySession[sessionId]?.filter { it.role != Role.SYSTEM } ?: emptyList()

        override suspend fun listSessions(): List<SessionSummary> =
            sessions.map { SessionSummary(it.id, it.title, it.updatedAt) }

        override suspend fun deleteSession(sessionId: String) {
            sessions.removeAll { it.id == sessionId }
            messagesBySession.remove(sessionId)
        }
    }

    private class FakeChatService(private val reply: String = "reflection: hi there") : ChatService {
        override suspend fun send(messages: List<Message>): String = reply
    }

    private class FakeGraphRepository : GraphRepository {
        override suspend fun saveNode(sessionId: String, node: GraphNode) {}
        override suspend fun saveEdge(sessionId: String, edge: GraphEdge) {}
        override suspend fun saveInsight(sessionId: String, text: String) {}
        override suspend fun loadAll(): GraphSnapshot = GraphSnapshot(emptyList(), emptyList())
    }

    private class FakeModelDownloader : ModelDownloader {
        override fun status(model: LocalModel) = DownloadStatus.NOT_DOWNLOADED
        override fun progress(model: LocalModel): DownloadProgress? = null
        override fun localFile(model: LocalModel) = java.io.File("/fake/${model.fileName}")
        override fun startDownload(model: LocalModel) {}
        override fun cancelDownload(model: LocalModel) {}
        override fun deleteDownload(model: LocalModel) {}
        override suspend fun awaitCompletion(model: LocalModel) = DownloadStatus.NOT_DOWNLOADED
    }

    private class FakeLocalLLMService : LocalLLMService {
        override suspend fun isModelLoaded() = false
        override suspend fun load(model: LocalModel, path: String) {}
        override suspend fun generate(messages: List<Message>) = "local"
        override fun stream(messages: List<Message>) = emptyFlow<String>()
        override fun close() {}
    }

    private class FakeTtsService : TtsService {
        override suspend fun synthesize(request: TtsRequest): ByteArray = byteArrayOf()
        override fun close() {}
    }

    private class FakeLocalTtsService : LocalTtsService {
        override fun speak(text: String, onDone: () -> Unit) {
            onDone()
        }
    }

    private fun buildViewModel(chatService: ChatService = FakeChatService()): ChatViewModel = ChatViewModel(
        FakeSessionRepository(),
        chatService,
        ModalityRouter,
        TherapyPromptBuilder,
        SafetyGuardrails,
        PersonaHolder(),
        GraphHolder(FakeGraphRepository()),
        FakeTtsService(),
        FakeLocalTtsService(),
        ModelSettings(SecureSettings(ApplicationProvider.getApplicationContext())),
        FakeLocalLLMService(),
        FakeModelDownloader(),
        ActiveSessionHolder()
    )

    @Test
    fun sendingAMessageShowsUserBubble() {
        val vm = buildViewModel()
        composeRule.setContent { ChatScreen(viewModel = vm) }

        composeRule.onNodeWithText("Share what's on your mind…").performTextInput("Hello there")
        composeRule.onNodeWithText("Send").performClick()

        composeRule.onNodeWithText("Hello there").assertIsDisplayed()
    }

    @Test
    fun historyButtonInvokesCallback() {
        val vm = buildViewModel()
        var opened = false
        composeRule.setContent { ChatScreen(viewModel = vm, onOpenSessions = { opened = true }) }

        composeRule.onNodeWithText("History").performClick()

        assertTrue(opened)
    }

    @Test
    fun modalityChipSelectsOverride() {
        val vm = buildViewModel()
        composeRule.setContent { ChatScreen(viewModel = vm) }

        composeRule.onNodeWithText("Talk").performClick()

        assertTrue(vm.uiState.value.selectedModality == TherapyModality.TALK)
    }
}
