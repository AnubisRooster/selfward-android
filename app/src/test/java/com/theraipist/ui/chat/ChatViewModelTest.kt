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
import com.theraipist.data.settings.FakeSecureSettings
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
import kotlinx.coroutines.launch
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
        override fun sendStreaming(messages: List<Message>) = kotlinx.coroutines.flow.flow {
            lastMessages = messages
            emit("$replyPrefix${messages.last().content}")
        }
    }

    private class FailingChatService : ChatService {
        override fun sendStreaming(messages: List<Message>) = kotlinx.coroutines.flow.flow<String> {
            throw RuntimeException("network unreachable")
        }
    }

    private class MissingKeyChatService : ChatService {
        override fun sendStreaming(messages: List<Message>) = kotlinx.coroutines.flow.flow<String> {
            throw com.theraipist.core.chat.MissingApiKeyException()
        }
    }

    private class FakeTtsService : TtsService {
        override suspend fun synthesize(request: TtsRequest): ByteArray = byteArrayOf()
        override fun close() {}
    }

    private class FakeLocalTtsService(private val fail: Boolean = false) :
        com.theraipist.core.voice.LocalTtsService {
        var spokenText: String? = null
        override fun speak(text: String, onDone: () -> Unit) {
            if (fail) throw RuntimeException("tts engine unavailable")
            spokenText = text
            onDone()
        }
    }

    private class FakeLocalLLMService : LocalLLMService {
        override suspend fun isModelLoaded(): Boolean = false
        override suspend fun load(model: com.theraipist.core.local.LocalModel, path: String) {}
        override fun stream(messages: List<com.theraipist.core.model.Message>) =
            kotlinx.coroutines.flow.emptyFlow<String>()
        override fun close() {}
    }

    /** Emits [deltas] one at a time, then optionally fails — the mid-stream error case. */
    private class ChunkedChatService(
        private val deltas: List<String>,
        private val failAfter: Boolean = false
    ) : ChatService {
        override fun sendStreaming(messages: List<Message>) = kotlinx.coroutines.flow.flow {
            deltas.forEach { emit(it) }
            if (failAfter) throw RuntimeException("connection reset")
        }
    }

    /** Emits [deltas], running [afterEach] once the collector has processed each one. */
    private class ObservingChatService(
        private val deltas: List<String>,
        private val afterEach: () -> Unit
    ) : ChatService {
        override fun sendStreaming(messages: List<Message>) = kotlinx.coroutines.flow.flow {
            deltas.forEach {
                emit(it)
                afterEach()
            }
        }
    }

    /** A loadable on-device model whose generation behaviour the test controls. */
    private class ProgrammableLocalLLMService(
        private val deltas: List<String> = emptyList(),
        private val throwOnStream: Boolean = false
    ) : LocalLLMService {
        private var loaded = false
        override suspend fun isModelLoaded(): Boolean = loaded
        override suspend fun load(model: LocalModel, path: String) { loaded = true }
        override fun stream(messages: List<Message>) = kotlinx.coroutines.flow.flow {
            if (throwOnStream) throw RuntimeException("native inference failed")
            deltas.forEach { emit(it) }
        }
        override fun close() { loaded = false }
    }

    private class DownloadedModelDownloader : ModelDownloader {
        override fun status(model: LocalModel): DownloadStatus = DownloadStatus.DOWNLOADED
        override fun progress(model: LocalModel): DownloadProgress? = null
        override fun localFile(model: LocalModel) = java.io.File("/fake/${model.fileName}")
        override fun startDownload(model: LocalModel) {}
        override fun cancelDownload(model: LocalModel) {}
        override fun deleteDownload(model: LocalModel) {}
        override suspend fun awaitCompletion(model: LocalModel): DownloadStatus = DownloadStatus.DOWNLOADED
    }

    private class FakeGraphRepository : GraphRepository {
        override suspend fun saveNode(sessionId: String, node: GraphNode) {}
        override suspend fun saveEdge(sessionId: String, edge: GraphEdge) {}
        override suspend fun saveInsight(sessionId: String, text: String) {}
        override suspend fun loadAll(): GraphSnapshot = GraphSnapshot(emptyList(), emptyList())
    }

    private class FakeEmbeddingModelDownloader : com.theraipist.core.embedding.EmbeddingModelDownloader {
        override fun status(model: com.theraipist.core.embedding.EmbeddingModelSpec) = DownloadStatus.NOT_DOWNLOADED
        override fun progress(model: com.theraipist.core.embedding.EmbeddingModelSpec): DownloadProgress? = null
        override fun onnxFile(model: com.theraipist.core.embedding.EmbeddingModelSpec) = java.io.File("/fake/model.onnx")
        override fun vocabFile(model: com.theraipist.core.embedding.EmbeddingModelSpec) = java.io.File("/fake/vocab.txt")
        override fun startDownload(model: com.theraipist.core.embedding.EmbeddingModelSpec) {}
        override fun cancelDownload(model: com.theraipist.core.embedding.EmbeddingModelSpec) {}
        override fun deleteDownload(model: com.theraipist.core.embedding.EmbeddingModelSpec) {}
        override suspend fun awaitCompletion(model: com.theraipist.core.embedding.EmbeddingModelSpec) = DownloadStatus.FAILED
    }

    private fun buildGraphHolder() = GraphHolder(
        FakeGraphRepository(),
        FakeEmbeddingModelDownloader(),
        com.theraipist.core.embedding.EmbeddingProviderFactory { _, _ ->
            throw UnsupportedOperationException("embedding model never downloaded in these tests")
        },
        com.theraipist.core.embedding.MemoryVectorStore()
    )

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
        activeSessionHolder: ActiveSessionHolder = ActiveSessionHolder(),
        localLLMService: LocalLLMService = FakeLocalLLMService(),
        modelDownloader: ModelDownloader = FakeModelDownloader(),
        modelSettings: ModelSettings = ModelSettings(FakeSecureSettings()),
        localTts: FakeLocalTtsService = FakeLocalTtsService()
    ): Pair<ChatViewModel, FakeSessionRepository> {
        val vm = ChatViewModel(
            repo,
            chatService,
            ModalityRouter,
            TherapyPromptBuilder,
            SafetyGuardrails,
            PersonaHolder(),
            buildGraphHolder(),
            FakeTtsService(),
            localTts,
            modelSettings,
            localLLMService,
            modelDownloader,
            activeSessionHolder
        )
        return vm to repo
    }

    /** ModelSettings wired to the on-device path with [modelId] selected. */
    private fun localModelSettings(modelId: String = "tinyllama-1.1b") =
        ModelSettings(FakeSecureSettings()).also {
            it.setUseLocalModel(true)
            it.setLocalModelId(modelId)
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
    fun missingApiKey_leavesNoEmptyAssistantBubbleBehind() = runTest {
        val (vm, repo) = buildVm(MissingKeyChatService())
        vm.send("hello")
        assertTrue(vm.uiState.value.needsApiKey)
        assertTrue(
            "placeholder bubble was left on screen",
            vm.uiState.value.messages.none { it.role == Role.ASSISTANT }
        )
        assertTrue(repo.stored.none { it.role == Role.ASSISTANT })
    }

    @Test
    fun midStreamFailure_keepsPartialTextAndPersistsIt() = runTest {
        val chatService = ChunkedChatService(listOf("Hello", " there"), failAfter = true)
        val (vm, repo) = buildVm(chatService)
        vm.send("hi")

        val shown = vm.uiState.value.messages.single { it.role == Role.ASSISTANT }
        assertEquals("Hello there", shown.content)
        // The visible reply must also be in storage, or the next turn's prompt
        // would tell the model it never replied.
        val persisted = repo.stored.single { it.role == Role.ASSISTANT }
        assertEquals("Hello there", persisted.content)
        assertTrue(!vm.uiState.value.errorMessage.isNullOrBlank())
    }

    @Test
    fun boundaryViolation_neverAppearsInAnyEmittedState() = runTest {
        val chatService = ChunkedChatService(listOf("You should ", "start taking ", "lithium daily."))
        val (vm, _) = buildVm(chatService)

        val rendered = mutableListOf<String>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.uiState.collect { state -> state.messages.forEach { rendered += it.content } }
        }
        vm.send("what should I do")
        job.cancel()

        assertTrue(
            "violating text was rendered before being swapped out",
            rendered.none { it.contains("start taking") }
        )
        val reply = vm.uiState.value.messages.single { it.role == Role.ASSISTANT }
        assertTrue(reply.content.contains("can't diagnose"))
    }

    @Test
    fun emptyReply_surfacesAnErrorAndPersistsNothing() = runTest {
        val (vm, repo) = buildVm(ChunkedChatService(emptyList()))
        vm.send("hi")

        assertTrue(!vm.uiState.value.errorMessage.isNullOrBlank())
        assertTrue(vm.uiState.value.messages.none { it.role == Role.ASSISTANT })
        assertTrue(
            "an empty assistant turn was written to the session",
            repo.stored.none { it.role == Role.ASSISTANT }
        )
    }

    @Test
    fun ttsFailure_isSurfacedButLeavesTheReplyIntact() = runTest {
        val settings = ModelSettings(FakeSecureSettings()).also { it.setUseLocalTts(true) }
        val (vm, repo) = buildVm(modelSettings = settings, localTts = FakeLocalTtsService(fail = true))
        vm.setTtsEnabled(true)

        vm.send("hi")

        assertTrue("a silent read-aloud failure told the user nothing", !vm.uiState.value.errorMessage.isNullOrBlank())
        // Read-aloud is a side channel: the reply must survive its failure.
        assertEquals("reflection: hi", vm.uiState.value.messages.single { it.role == Role.ASSISTANT }.content)
        assertTrue(repo.stored.any { it.role == Role.ASSISTANT })
    }

    @Test
    fun emptyReply_doesNotInvokeTts() = runTest {
        val settings = ModelSettings(FakeSecureSettings()).also { it.setUseLocalTts(true) }
        val tts = FakeLocalTtsService()
        val (vm, _) = buildVm(ChunkedChatService(emptyList()), modelSettings = settings, localTts = tts)
        vm.setTtsEnabled(true)

        vm.send("hi")

        assertEquals(null, tts.spokenText)
    }

    @Test
    fun localModelThrowing_fallsBackToCloud() = runTest {
        val cloud = FakeChatService()
        val (vm, _) = buildVm(
            chatService = cloud,
            localLLMService = ProgrammableLocalLLMService(throwOnStream = true),
            modelDownloader = DownloadedModelDownloader(),
            modelSettings = localModelSettings()
        )
        vm.send("I had a dream about flying")

        val reply = vm.uiState.value.messages.single { it.role == Role.ASSISTANT }
        assertTrue("expected the cloud reply, got '${reply.content}'", reply.content.startsWith("reflection: "))
    }

    @Test
    fun localModelProducingNothing_fallsBackToCloud() = runTest {
        val cloud = FakeChatService()
        val (vm, _) = buildVm(
            chatService = cloud,
            localLLMService = ProgrammableLocalLLMService(deltas = emptyList()),
            modelDownloader = DownloadedModelDownloader(),
            modelSettings = localModelSettings()
        )
        vm.send("I had a dream about flying")

        val reply = vm.uiState.value.messages.single { it.role == Role.ASSISTANT }
        assertTrue("expected the cloud reply, got '${reply.content}'", reply.content.startsWith("reflection: "))
    }

    @Test
    fun localModelProducingText_isUsedWithoutCallingCloud() = runTest {
        val cloud = FakeChatService()
        val (vm, _) = buildVm(
            chatService = cloud,
            localLLMService = ProgrammableLocalLLMService(deltas = listOf("local ", "reply")),
            modelDownloader = DownloadedModelDownloader(),
            modelSettings = localModelSettings()
        )
        vm.send("I had a dream about flying")

        val reply = vm.uiState.value.messages.single { it.role == Role.ASSISTANT }
        assertEquals("local reply", reply.content)
        assertEquals(null, cloud.lastMessages)
    }

    /**
     * Snapshots the visible reply after each delta is emitted. Collecting uiState
     * from outside would not work: it is a StateFlow, so a test that drives the
     * whole stream synchronously only ever observes the conflated final value.
     */
    @Test
    fun streamingReply_growsIncrementallyInTheUi() = runTest {
        val snapshots = mutableListOf<String>()
        lateinit var underTest: ChatViewModel
        val service = ObservingChatService(listOf("one ", "two ", "three")) {
            underTest.uiState.value.messages
                .firstOrNull { it.role == Role.ASSISTANT }
                ?.let { snapshots += it.content }
        }
        val (vm, _) = buildVm(service)
        underTest = vm

        vm.send("hi")

        assertEquals(listOf("one ", "one two ", "one two three"), snapshots)
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
