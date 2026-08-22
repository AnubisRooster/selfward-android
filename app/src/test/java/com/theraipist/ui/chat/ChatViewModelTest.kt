package com.theraipist.ui.chat

import app.cash.turbine.test
import com.theraipist.core.PersonaHolder
import com.theraipist.core.chat.ChatService
import com.theraipist.core.model.Message
import com.theraipist.core.model.Persona
import com.theraipist.core.model.Role
import com.theraipist.core.modality.ModalityRouter
import com.theraipist.core.prompt.TherapyPromptBuilder
import com.theraipist.core.repository.SessionRepository
import com.theraipist.core.repository.SessionSummary
import com.theraipist.core.safety.SafetyGuardrails
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    private fun buildVm(): Pair<ChatViewModel, FakeSessionRepository> {
        val repo = FakeSessionRepository()
        val vm = ChatViewModel(
            repo,
            FakeChatService(),
            ModalityRouter,
            TherapyPromptBuilder,
            SafetyGuardrails,
            PersonaHolder()
        )
        return vm to repo
    }

    @Test
    fun send_appendsUserThenAssistant() = runTest(UnconfinedTestDispatcher()) {
        val (vm, repo) = buildVm()
        vm.uiState.test {
            expectMostRecentItem() // initial empty state
            vm.send("I had a dream about flying")
            val final = awaitItem()
            assertTrue(final.messages.any { it.role == Role.USER && it.content == "I had a dream about flying" })
            assertTrue(final.messages.any { it.role == Role.ASSISTANT })
            assertEquals(false, final.isSending)
            assertTrue(repo.stored.any { it.role == Role.USER })
        }
    }

    @Test
    fun blankInput_isIgnored() = runTest(UnconfinedTestDispatcher()) {
        val (vm, repo) = buildVm()
        vm.uiState.test {
            vm.send("   ")
            expectMostRecentItem()
            assertTrue(repo.stored.isEmpty())
        }
    }

    @Test
    fun crisisInput_setsResourceMessage() = runTest(UnconfinedTestDispatcher()) {
        val (vm, _) = buildVm()
        vm.uiState.test {
            vm.send("I want to kill myself")
            val final = awaitItem()
            assertTrue(final.crisisLevel != null)
            assertTrue(!final.resourceMessage.isNullOrBlank())
        }
    }
}
