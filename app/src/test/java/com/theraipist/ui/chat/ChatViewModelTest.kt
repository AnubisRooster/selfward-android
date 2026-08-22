package com.theraipist.ui.chat

import com.theraipist.core.PersonaHolder
import com.theraipist.core.GraphHolder
import com.theraipist.core.chat.ChatService
import com.theraipist.core.voice.TtsRequest
import com.theraipist.core.voice.TtsService
import com.theraipist.core.model.Message
import com.theraipist.core.model.Persona
import com.theraipist.core.model.Role
import com.theraipist.core.modality.ModalityRouter
import com.theraipist.core.prompt.TherapyPromptBuilder
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private class FakeSessionRepository : SessionRepository {
        val stored = mutableListOf<Message>()
        override suspend fun createSession(persona: Persona, title: String): com.theraipist.core.repository.Session {
            return com.theraipist.core.repository.Session("s1", persona, title, 0, 0)
        }
        override suspend fun appendMessage(sessionId: String, message: Message) {
            stored.add(message)
        }
        override suspend fun getMessages(sessionId: String): List<Message> =
            stored.filter { it.role != Role.SYSTEM }
        override suspend fun listSessions(): List<SessionSummary> = emptyList()
    }

    private class FakeChatService : ChatService {
        var lastMessages: List<Message>? = null
        override suspend fun send(messages: List<Message>): String {
            lastMessages = messages
            return "reflection: ${messages.last().content}"
        }
    }

    private class FakeTtsService : TtsService {
        override suspend fun synthesize(request: TtsRequest): ByteArray = byteArrayOf()
        override fun close() {}
    }

    private val mainDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(mainDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildVm(): Pair<ChatViewModel, FakeSessionRepository> {
        val repo = FakeSessionRepository()
        val vm = ChatViewModel(
            repo,
            FakeChatService(),
            ModalityRouter,
            TherapyPromptBuilder,
            SafetyGuardrails,
            PersonaHolder(),
            GraphHolder(),
            FakeTtsService()
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
}
