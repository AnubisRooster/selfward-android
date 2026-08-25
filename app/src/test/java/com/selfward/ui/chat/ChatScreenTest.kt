package com.selfward.ui.chat

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
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

    private fun buildViewModel(
        chatService: ChatService = FakeChatService(),
        sessionRepository: SessionRepository = FakeSessionRepository(),
        activeSessionHolder: ActiveSessionHolder = ActiveSessionHolder()
    ): ChatViewModel = ChatViewModel(
        sessionRepository,
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
        activeSessionHolder,
        FakeIntakeStore(),
        FakeSecureSettings(),
        FakeCatalog(),
        FakeUnusable()
    )

    /**
     * Stages a conversation the way the Sessions screen does — write it to the
     * repository, then hand the id over through [ActiveSessionHolder], which is
     * what ChatViewModel consumes at construction.
     */
    private fun viewModelShowing(messages: List<Message>): ChatViewModel {
        val repo = FakeSessionRepository()
        val holder = ActiveSessionHolder()
        kotlinx.coroutines.runBlocking {
            val session = repo.createSession(Persona(com.selfward.config.PersonaKind.THERAPIST), "A session")
            messages.forEach { repo.appendMessage(session.id, it) }
            holder.open(session.id)
        }
        return buildViewModel(sessionRepository = repo, activeSessionHolder = holder)
    }

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

    /**
     * Every bubble used to carry its mode, so a whole conversation read TALK,
     * TALK, TALK — a label on everything tells you nothing. The chip row at the
     * top still offers "Talk" as a choice, so the assertion counts bubbles by
     * looking for the exchange that follows the stamp.
     */
    @Test
    fun anUnchangedModalityIsNotStampedOnEveryBubble() {
        val vm = viewModelShowing(
            listOf(
                message("m1", Role.USER, "first thing", TherapyModality.TALK),
                message("m2", Role.ASSISTANT, "a reply", TherapyModality.TALK),
                message("m3", Role.USER, "second thing", TherapyModality.TALK)
            )
        )
        composeRule.setContent { ChatScreen(viewModel = vm) }

        // One stamp for the run, not one per message.
        composeRule.onAllNodesWithText("Talk").assertCountEquals(2) // chip + single stamp
    }

    @Test
    fun theModalityIsStampedWhereItChanges() {
        val vm = viewModelShowing(
            listOf(
                message("m1", Role.USER, "first thing", TherapyModality.TALK),
                message("m2", Role.ASSISTANT, "a reply", TherapyModality.TALK),
                message("m3", Role.USER, "I had a dream", TherapyModality.DREAM)
            )
        )
        composeRule.setContent { ChatScreen(viewModel = vm) }

        // "Dream" appears as a chip and once more where the conversation turned.
        composeRule.onAllNodesWithText("Dream").assertCountEquals(2)
    }

    /** The raw enum name must never reach the screen. */
    @Test
    fun theStampIsWrittenForAReaderNotAsAnEnum() {
        val vm = viewModelShowing(
            listOf(message("m1", Role.USER, "something", TherapyModality.ACTIVE_IMAGINATION))
        )
        composeRule.setContent { ChatScreen(viewModel = vm) }

        composeRule.onNodeWithText("ACTIVE_IMAGINATION").assertDoesNotExist()
        composeRule.onAllNodesWithText("Active Imagination").assertCountEquals(2)
    }

    private fun message(id: String, role: Role, content: String, modality: TherapyModality) =
        Message(id = id, role = role, content = content, modality = modality.name)

    /** No catalogue and no exclusions unless a test supplies them. */
    private class FakeCatalog(
        private val models: List<com.selfward.core.catalog.OpenRouterModel> = emptyList()
    ) : com.selfward.core.catalog.OpenRouterCatalog {
        override suspend fun models(apiKey: String?, forceRefresh: Boolean) = models
        override fun cached() = models
    }

    private class FakeUnusable : com.selfward.core.catalog.UnusableModels {
        val remembered = mutableMapOf<String, String>()
        override fun all() = remembered.keys.toSet()
        override fun remember(modelId: String, reason: String) { remembered[modelId] = reason }
        override fun forget(modelId: String) { remembered.remove(modelId) }
        override fun clear() = remembered.clear()
        override fun reasonFor(modelId: String) = remembered[modelId]
        val workingIds = mutableSetOf<String>()
        override fun working() = workingIds.toSet()
        override fun rememberWorking(modelId: String) {
            workingIds += modelId
            remembered.remove(modelId)
        }
    }

}
