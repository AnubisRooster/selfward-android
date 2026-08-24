package com.selfward.ui.chat

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.selfward.core.ActiveSessionHolder
import com.selfward.core.GraphHolder
import com.selfward.core.ModelSettings
import com.selfward.core.PersonaHolder
import com.selfward.core.chat.ChatService
import com.selfward.core.graph.GraphEdge
import com.selfward.core.graph.GraphNode
import com.selfward.core.local.DownloadProgress
import com.selfward.core.local.DownloadStatus
import com.selfward.core.local.LocalLLMService
import com.selfward.core.local.LocalModel
import com.selfward.core.local.ModelDownloader
import com.selfward.core.modality.ModalityRouter
import com.selfward.core.modality.TherapyModality
import com.selfward.core.model.Message
import com.selfward.core.model.Persona
import com.selfward.core.model.Role
import com.selfward.core.prompt.TherapyPromptBuilder
import com.selfward.core.repository.GraphRepository
import com.selfward.core.repository.GraphSnapshot
import com.selfward.core.repository.Session
import com.selfward.core.repository.SessionRepository
import com.selfward.core.repository.SessionSummary
import com.selfward.core.safety.SafetyGuardrails
import com.selfward.core.voice.LocalTtsService
import com.selfward.core.voice.TtsRequest
import com.selfward.core.voice.TtsService
import com.selfward.data.settings.FakeSecureSettings
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

        override suspend fun getSession(sessionId: String): Session? =

            sessions.firstOrNull { it.id == sessionId }


        override suspend fun listSessions(): List<SessionSummary> =
            sessions.map { SessionSummary(it.id, it.title, it.updatedAt) }

        override suspend fun listArchivedSessions(): List<SessionSummary> = emptyList()


        override suspend fun setArchived(sessionId: String, archived: Boolean) {}


        override suspend fun deleteSession(sessionId: String) {
            sessions.removeAll { it.id == sessionId }
            messagesBySession.remove(sessionId)
        }
    }

    private class FakeChatService(private val reply: String = "reflection: hi there") : ChatService {
        override fun sendStreaming(messages: List<Message>) = kotlinx.coroutines.flow.flowOf(reply)
    }

    private class FakeIntakeStore(private var intake: com.selfward.core.intake.Intake = com.selfward.core.intake.Intake()) :
        com.selfward.core.intake.IntakeStore {
        override fun load() = intake
        override fun save(intake: com.selfward.core.intake.Intake) { this.intake = intake }
        override fun clear() { intake = com.selfward.core.intake.Intake() }
        override var onboardingComplete: Boolean = true
    }

    private class FakeGraphRepository : GraphRepository {
        override suspend fun saveNode(sessionId: String, node: GraphNode) {}
        override suspend fun saveEdge(sessionId: String, edge: GraphEdge) {}
        override suspend fun saveInsight(sessionId: String, text: String) {}
        override suspend fun loadAll(): GraphSnapshot = GraphSnapshot(emptyList(), emptyList())
    }

    private class FakeEmbeddingModelDownloader : com.selfward.core.embedding.EmbeddingModelDownloader {
        override fun status(model: com.selfward.core.embedding.EmbeddingModelSpec) = DownloadStatus.NOT_DOWNLOADED
        override fun progress(model: com.selfward.core.embedding.EmbeddingModelSpec): DownloadProgress? = null
        override fun onnxFile(model: com.selfward.core.embedding.EmbeddingModelSpec) = java.io.File("/fake/model.onnx")
        override fun vocabFile(model: com.selfward.core.embedding.EmbeddingModelSpec) = java.io.File("/fake/vocab.txt")
        override fun startDownload(model: com.selfward.core.embedding.EmbeddingModelSpec) {}
        override fun cancelDownload(model: com.selfward.core.embedding.EmbeddingModelSpec) {}
        override fun deleteDownload(model: com.selfward.core.embedding.EmbeddingModelSpec) {}
        override suspend fun awaitCompletion(model: com.selfward.core.embedding.EmbeddingModelSpec) = DownloadStatus.FAILED
    }

    private fun buildGraphHolder() = GraphHolder(
        FakeGraphRepository(),
        FakeEmbeddingModelDownloader(),
        com.selfward.core.embedding.EmbeddingProviderFactory { _, _ ->
            throw UnsupportedOperationException("embedding model never downloaded in these tests")
        },
        com.selfward.core.embedding.MemoryVectorStore()
    )

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
        buildGraphHolder(),
        FakeTtsService(),
        FakeLocalTtsService(),
        ModelSettings(FakeSecureSettings()),
        FakeLocalLLMService(),
        FakeModelDownloader(),
        ActiveSessionHolder(),
        FakeIntakeStore()
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
