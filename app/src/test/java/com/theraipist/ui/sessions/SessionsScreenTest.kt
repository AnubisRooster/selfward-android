package com.theraipist.ui.sessions

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.theraipist.core.ActiveSessionHolder
import com.theraipist.core.model.Message
import com.theraipist.core.model.Persona
import com.theraipist.core.repository.Session
import com.theraipist.core.repository.SessionRepository
import com.theraipist.core.repository.SessionSummary
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SessionsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private class FakeSessionRepository(
        initial: List<SessionSummary> = listOf(SessionSummary("s1", "First session", 100))
    ) : SessionRepository {
        val sessions = initial.toMutableList()

        override suspend fun createSession(persona: Persona, title: String): Session =
            throw UnsupportedOperationException("not used by SessionsScreen")

        override suspend fun appendMessage(sessionId: String, message: Message) {}
        override suspend fun getMessages(sessionId: String): List<Message> = emptyList()
        override suspend fun getSession(sessionId: String): Session? = null

        override suspend fun listSessions(): List<SessionSummary> = sessions.toList()
        override suspend fun deleteSession(sessionId: String) {
            sessions.removeAll { it.id == sessionId }
        }
    }

    @Test
    fun showsEmptyStateWhenNoSessions() {
        val vm = SessionsViewModel(FakeSessionRepository(emptyList()), ActiveSessionHolder())
        composeRule.setContent {
            SessionsScreen(viewModel = vm, onOpenSession = {}, onNewSession = {})
        }
        composeRule.onNodeWithText("No past sessions yet — start a conversation and it'll show up here.")
            .assertIsDisplayed()
    }

    @Test
    fun listsSessionsAndOpensOnTap() {
        val repo = FakeSessionRepository()
        val vm = SessionsViewModel(repo, ActiveSessionHolder())
        var opened = false
        composeRule.setContent {
            SessionsScreen(viewModel = vm, onOpenSession = { opened = true }, onNewSession = {})
        }
        composeRule.onNodeWithText("First session").assertIsDisplayed()
        composeRule.onNodeWithText("First session").performClick()
        assertTrue(opened)
    }

    @Test
    fun deleteRemovesSessionFromList() {
        val repo = FakeSessionRepository()
        val vm = SessionsViewModel(repo, ActiveSessionHolder())
        composeRule.setContent {
            SessionsScreen(viewModel = vm, onOpenSession = {}, onNewSession = {})
        }
        composeRule.onNodeWithText("Delete").performClick()
        composeRule.onNodeWithText("No past sessions yet — start a conversation and it'll show up here.")
            .assertIsDisplayed()
    }

    @Test
    fun newSessionButtonInvokesCallback() {
        val vm = SessionsViewModel(FakeSessionRepository(), ActiveSessionHolder())
        var newSessionRequested = false
        composeRule.setContent {
            SessionsScreen(viewModel = vm, onOpenSession = {}, onNewSession = { newSessionRequested = true })
        }
        composeRule.onNodeWithText("New session").performClick()
        assertTrue(newSessionRequested)
    }
}
