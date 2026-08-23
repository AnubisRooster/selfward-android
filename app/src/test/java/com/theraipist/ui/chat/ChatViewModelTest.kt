package com.theraipist.ui.chat

import com.theraipist.core.ActiveSessionHolder
import com.theraipist.core.PersonaHolder
import com.theraipist.core.GraphHolder
import com.theraipist.core.ModelSettings
import com.theraipist.core.chat.ChatService
import com.theraipist.core.local.DownloadProgress
import com.theraipist.core.local.DownloadStatus
import com.theraipist.core.local.LocalLLMService
import com.theraipist.core.local.LocalModel
import com.theraipist.core.local.ModelDownloader
import com.theraipist.core.voice.TtsRequest
import com.theraipist.data.settings.SecureSettings
import com.theraipist.core.voice.TtsService
import com.theraipist.core.model.Message
import com.theraipist.core.model.Persona
import com.theraipist.core.model.Role
import com.theraipist.core.graph.GraphEdge
import com.theraipist.core.graph.GraphNode
import com.theraipist.core.modality.ModalityRouter
import com.theraipist.core.prompt.TherapyPromptBuilder
import com.theraipist.core.repository.GraphRepository
import com.theraipist.core.repository.GraphSnapshot
import com.theraipist.core.repository.Session
import com.theraipist.core.repository.SessionRepository
import com.theraipist.core.repository.SessionSummary
import com.theraipist.core.safety.SafetyGuardrails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private class FakeSessionRepository : SessionRepository {
        val stored = mutableListOf<Message>()
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
            stored.add(message)
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

    private class FakeChatService(private val replyPrefix: String = "reflection: ") : ChatService {
        var lastMessages: List<Message>? = null
        override suspend fun send(messages: List<Message>): String {
            lastMessages = messages
            return "$replyPrefix${messages.last().content}"
        }
    }

    private class FailingChatService : ChatService {
        override suspend fun send(messages: List<Message>): String =
            throw RuntimeException("network unreachable")
    }

    private class MissingKeyChatService : ChatService {
        override suspend fun send(messages: List<Message>): String =
            throw com.theraipist.core.chat.MissingApiKeyException()
    }

    private class FakeTtsService : TtsService {
        override suspend fun synthesize(request: TtsRequest): ByteArray = byteArrayOf()
        override fun close() {}
    }

    private class FakeLocalTtsService : com.theraipist.core.voice.LocalTtsService {
        var spokenText: String? = null
        override fun speak(text: String, onDone: () -> Unit) {
            spokenText = text
            onDone()
        }
    }

    private class FakeLocalLLMService : LocalLLMService {
        override suspend fun isModelLoaded(): Boolean = false
        override suspend fun load(model: com.theraipist.core.local.LocalModel, path: String) {}
        override suspend fun generate(messages: List<com.theraipist.core.model.Message>): String = "local"
        override fun stream(messages: List<com.theraipist.core.model.Message>) =
            kotlinx.coroutines.flow.emptyFlow<String>()
        override fun close() {}
    }

    private class FakeGraphRepository : GraphRepository {
        override suspend fun saveNode(sessionId: String, node: GraphNode) {}
        override suspend fun saveEdge(sessionId: String, edge: GraphEdge) {}
        override suspend fun saveInsight(sessionId: String, text: String) {}
        override suspend fun loadAll(): GraphSnapshot = GraphSnapshot(emptyList(), emptyList())
    }

    private class FakeModelDownloader : ModelDownloader {
        override fun status(model: LocalModel): DownloadStatus = DownloadStatus.NOT_DOWNLOADED
        override fun progress(model: LocalModel): DownloadProgress? = null
        override fun localFile(model: LocalModel) = java.io.File("/fake/${model.fileName}")
        override fun startDownload(model: LocalModel) {}
        override fun cancelDownload(model: LocalModel) {}
        override fun deleteDownload(model: LocalModel) {}
        override suspend fun awaitCompletion(model: LocalModel): DownloadStatus = DownloadStatus.NOT_DOWNLOADED
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val mainDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(mainDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildVm(
        chatService: ChatService = FakeChatService(),
        repo: FakeSessionRepository = FakeSessionRepository(),
        activeSessionHolder: ActiveSessionHolder = ActiveSessionHolder()
    ): Pair<ChatViewModel, FakeSessionRepository> {
        val vm = ChatViewModel(
            repo,
            chatService,
            ModalityRouter,
            TherapyPromptBuilder,
            SafetyGuardrails,
            PersonaHolder(),
            GraphHolder(FakeGraphRepository()),
            FakeTtsService(),
            FakeLocalTtsService(),
            ModelSettings(SecureSettings(android.app.Application())),
            FakeLocalLLMService(),
            FakeModelDownloader(),
            activeSessionHolder
        )
        return vm to repo
    }

    @Test
    fun send_appendsUserThenAssistant() = runTest {
        val (vm, repo) = buildVm()
        vm.send("I had a dream about flying")
        val state = vm.uiState.value
        assertTrue(state.messages.any { it.role == Role.USER && it.content == "I had a dream about flying" })
        assertTrue(state.messages.any { it.role == Role.ASSISTANT })
        assertEquals(false, state.isSending)
        assertTrue(repo.stored.any { it.role == Role.USER })
    }

    @Test
    fun blankInput_isIgnored() = runTest {
        val (vm, repo) = buildVm()
        vm.send("   ")
        assertTrue(repo.stored.isEmpty())
        assertTrue(vm.uiState.value.messages.isEmpty())
    }

    @Test
    fun crisisInput_setsResourceMessage() = runTest {
        val (vm, _) = buildVm()
        vm.send("I want to kill myself")
        val state = vm.uiState.value
        assertTrue(state.crisisLevel != null)
        assertTrue(!state.resourceMessage.isNullOrBlank())
    }

    @Test
    fun assistantReply_extractsInsightsIntoGraph() = runTest {
        val (vm, _) = buildVm()
        vm.send("I had a dream about flying")
        val state = vm.uiState.value
        assertTrue(state.graphNodes.isNotEmpty())
    }

    @Test
    fun send_appliesModalitySpecificSystemPrompt() = runTest {
        val chatService = FakeChatService()
        val (vm, _) = buildVm(chatService)
        vm.send("I had a dream about flying last night")
        val systemMessage = chatService.lastMessages?.firstOrNull { it.role == Role.SYSTEM }
        assertNotNull(systemMessage)
        assertTrue(systemMessage!!.content.contains("Jungian analyst"))
    }

    @Test
    fun selectModality_overridesAutoDetection() = runTest {
        val chatService = FakeChatService()
        val (vm, _) = buildVm(chatService)
        vm.selectModality(com.theraipist.core.modality.TherapyModality.GROUNDING)

        vm.send("just chatting about my day, nothing dream-related")

        val systemMessage = chatService.lastMessages?.firstOrNull { it.role == Role.SYSTEM }
        assertNotNull(systemMessage)
        assertTrue(systemMessage!!.content.contains("DBT therapist"))
        val userMessage = vm.uiState.value.messages.first { it.role == Role.USER }
        assertEquals("GROUNDING", userMessage.modality)
    }

    @Test
    fun selectModality_clearedByPassingNull() = runTest {
        val chatService = FakeChatService()
        val (vm, _) = buildVm(chatService)
        vm.selectModality(com.theraipist.core.modality.TherapyModality.GROUNDING)
        vm.selectModality(null)

        vm.send("I had a dream about flying")

        val systemMessage = chatService.lastMessages?.firstOrNull { it.role == Role.SYSTEM }
        assertTrue(systemMessage!!.content.contains("Jungian analyst"))
    }

    @Test
    fun assistantReply_boundaryViolationIsIntercepted() = runTest {
        val chatService = FakeChatService(replyPrefix = "you need medication for ")
        val (vm, _) = buildVm(chatService)
        vm.send("I'm struggling today")
        val assistantMessage = vm.uiState.value.messages.first { it.role == Role.ASSISTANT }
        assertFalse(assistantMessage.content.contains("you need medication"))
    }

    @Test
    fun send_surfacesErrorAndResetsSendingOnFailure() = runTest {
        val (vm, _) = buildVm(FailingChatService())
        vm.send("hello")
        val state = vm.uiState.value
        assertEquals(false, state.isSending)
        assertTrue(!state.errorMessage.isNullOrBlank())
        assertFalse(state.needsApiKey)
    }

    @Test
    fun send_flagsNeedsApiKeyOnMissingApiKeyFailure() = runTest {
        val (vm, _) = buildVm(MissingKeyChatService())
        vm.send("hello")
        val state = vm.uiState.value
        assertTrue(state.needsApiKey)
        assertTrue(!state.errorMessage.isNullOrBlank())
    }

    @Test
    fun reEntry_showsCheckInAfterPriorCrisisSession() = runTest {
        val repo = FakeSessionRepository()
        val (vm1, _) = buildVm(repo = repo)
        vm1.send("I want to kill myself")

        val (vm2, _) = buildVm(repo = repo)
        vm2.send("hi again")
        assertTrue(!vm2.uiState.value.reEntryMessage.isNullOrBlank())
    }

    @Test
    fun openSession_resumesPriorMessagesOnAFreshViewModel() = runTest {
        val repo = FakeSessionRepository()
        val (vm1, _) = buildVm(repo = repo)
        vm1.send("first session message")
        val sessionId = repo.listSessions().first().id

        val holder = ActiveSessionHolder()
        holder.open(sessionId)
        val (vm2, _) = buildVm(repo = repo, activeSessionHolder = holder)

        assertTrue(vm2.uiState.value.messages.any { it.content == "first session message" })
    }

    @Test
    fun reEntry_staysNullWithoutPriorCrisis() = runTest {
        val repo = FakeSessionRepository()
        val (vm1, _) = buildVm(repo = repo)
        vm1.send("just checking in today")

        val (vm2, _) = buildVm(repo = repo)
        vm2.send("hi again")
        assertTrue(vm2.uiState.value.reEntryMessage == null)
    }
}
