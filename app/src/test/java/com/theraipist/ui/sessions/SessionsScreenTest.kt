package com.theraipist.ui.sessions

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
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

        private var archivedIds = setOf<String>()

        override suspend fun listSessions(): List<SessionSummary> =
            sessions.filterNot { it.id in archivedIds }

        override suspend fun listArchivedSessions(): List<SessionSummary> =
            sessions.filter { it.id in archivedIds }

        override suspend fun setArchived(sessionId: String, archived: Boolean) {
            archivedIds = if (archived) archivedIds + sessionId else archivedIds - sessionId
        }

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
        composeRule.onNodeWithText("No sessions yet — start one and it'll show up here.")
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

    /** Archiving is the everyday action and must not destroy anything. */
    @Test
    fun archivingHidesTheSessionWithoutDeletingIt() {
        val repo = FakeSessionRepository()
        val vm = SessionsViewModel(repo, ActiveSessionHolder())
        composeRule.setContent {
            SessionsScreen(viewModel = vm, onOpenSession = {}, onNewSession = {})
        }

        composeRule.onNodeWithText("Archive").performClick()

        composeRule.onNodeWithText("No sessions yet — start one and it'll show up here.")
            .assertIsDisplayed()
        assertTrue("archiving must not delete the session", repo.sessions.any { it.id == "s1" })
    }

    @Test
    fun anArchivedSessionCanBeRestored() {
        val repo = FakeSessionRepository()
        val vm = SessionsViewModel(repo, ActiveSessionHolder())
        composeRule.setContent {
            SessionsScreen(viewModel = vm, onOpenSession = {}, onNewSession = {})
        }

        composeRule.onNodeWithText("Archive").performClick()
        composeRule.onNodeWithText("Archive (1)").performClick()
        composeRule.onNodeWithText("Restore").performClick()
        composeRule.onNodeWithText("Done").performClick()

        composeRule.onNodeWithText("First session").assertIsDisplayed()
    }

    /** Permanent deletion is only reachable from the archive, never the main list. */
    @Test
    fun deleteIsNotOfferedOnTheActiveList() {
        val vm = SessionsViewModel(FakeSessionRepository(), ActiveSessionHolder())
        composeRule.setContent {
            SessionsScreen(viewModel = vm, onOpenSession = {}, onNewSession = {})
        }

        composeRule.onAllNodesWithText("Delete").assertCountEquals(0)
    }

    @Test
    fun deletingFromTheArchiveRemovesItForGood() {
        val repo = FakeSessionRepository()
        val vm = SessionsViewModel(repo, ActiveSessionHolder())
        composeRule.setContent {
            SessionsScreen(viewModel = vm, onOpenSession = {}, onNewSession = {})
        }

        composeRule.onNodeWithText("Archive").performClick()
        composeRule.onNodeWithText("Archive (1)").performClick()
        composeRule.onNodeWithText("Delete").performClick()

        assertTrue(repo.sessions.isEmpty())
    }

    /**
     * The ViewModel outlives navigating away to start a session, so a list that
     * only loaded in init would show "no sessions" straight after creating one.
     * Found on a device; this pins it.
     */
    @Test
    fun theListReloadsWhenTheScreenIsShown() {
        val repo = FakeSessionRepository(emptyList())
        val vm = SessionsViewModel(repo, ActiveSessionHolder())

        // A session appears after the ViewModel was constructed, as happens when
        // one is created on another screen.
        repo.sessions += SessionSummary("s9", "Created elsewhere", 500)

        composeRule.setContent {
            SessionsScreen(viewModel = vm, onOpenSession = {}, onNewSession = {})
        }

        composeRule.onNodeWithText("Created elsewhere").assertIsDisplayed()
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
