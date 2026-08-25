package com.selfward.ui.sessions

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.selfward.core.ActiveSessionHolder
import com.selfward.core.dashboard.MessageTally
import com.selfward.core.dashboard.StatsRepository
import com.selfward.core.dashboard.Tally
import com.selfward.core.model.Message
import com.selfward.core.model.Persona
import com.selfward.core.repository.Session
import com.selfward.core.repository.SessionRepository
import com.selfward.core.repository.SessionSummary
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

    /** Counts for the badges under each session title. */
    private class FakeStatsRepository(
        private val messages: List<MessageTally> = emptyList(),
        private val nodes: List<Tally> = emptyList(),
        private val notes: List<Tally> = emptyList(),
        private val dreams: List<Tally> = emptyList()
    ) : StatsRepository {
        override suspend fun messageTallies() = messages
        override suspend fun modalityTallies(): Map<String, Int> = emptyMap()
        override suspend fun nodeTallies() = nodes
        override suspend fun noteTallies() = notes
        override suspend fun dreamTallies() = dreams
    }

    /** Stands in for a database that cannot be read right now. */
    private class FailingStatsRepository : StatsRepository {
        override suspend fun messageTallies(): List<MessageTally> = error("no database")
        override suspend fun modalityTallies(): Map<String, Int> = error("no database")
        override suspend fun nodeTallies(): List<Tally> = error("no database")
        override suspend fun noteTallies(): List<Tally> = error("no database")
        override suspend fun dreamTallies(): List<Tally> = error("no database")
    }

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
        val vm = SessionsViewModel(FakeSessionRepository(emptyList()), FakeStatsRepository(), ActiveSessionHolder())
        composeRule.setContent {
            SessionsScreen(viewModel = vm, onOpenSession = {}, onNewSession = {})
        }
        composeRule.onNodeWithText("No sessions yet — start one and it'll show up here.")
            .assertIsDisplayed()
    }

    @Test
    fun listsSessionsAndOpensOnTap() {
        val repo = FakeSessionRepository()
        val vm = SessionsViewModel(repo, FakeStatsRepository(), ActiveSessionHolder())
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
        val vm = SessionsViewModel(repo, FakeStatsRepository(), ActiveSessionHolder())
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
        val vm = SessionsViewModel(repo, FakeStatsRepository(), ActiveSessionHolder())
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
        val vm = SessionsViewModel(FakeSessionRepository(), FakeStatsRepository(), ActiveSessionHolder())
        composeRule.setContent {
            SessionsScreen(viewModel = vm, onOpenSession = {}, onNewSession = {})
        }

        composeRule.onAllNodesWithText("Delete").assertCountEquals(0)
    }

    @Test
    fun deletingFromTheArchiveRemovesItForGood() {
        val repo = FakeSessionRepository()
        val vm = SessionsViewModel(repo, FakeStatsRepository(), ActiveSessionHolder())
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
        val vm = SessionsViewModel(repo, FakeStatsRepository(), ActiveSessionHolder())

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
        val vm = SessionsViewModel(FakeSessionRepository(), FakeStatsRepository(), ActiveSessionHolder())
        var newSessionRequested = false
        composeRule.setContent {
            SessionsScreen(viewModel = vm, onOpenSession = {}, onNewSession = { newSessionRequested = true })
        }
        composeRule.onNodeWithText("New session").performClick()
        assertTrue(newSessionRequested)
    }

    // MARK: - Row counts

    @Test
    fun aSessionRowShowsWhatItHoldsInIt() {
        val repo = FakeSessionRepository()
        val vm = SessionsViewModel(
            repo,
            FakeStatsRepository(
                messages = listOf(MessageTally("s1", 12, 6)),
                nodes = listOf(Tally("s1", 3)),
                notes = listOf(Tally("s1", 1))
            ),
            ActiveSessionHolder()
        )
        composeRule.setContent {
            SessionsScreen(viewModel = vm, onOpenSession = {}, onNewSession = {})
        }

        composeRule.onNodeWithText("12 messages · 3 patterns · 1 note").assertIsDisplayed()
    }

    /**
     * A row reading "0 messages · 0 notes" is a row telling someone what they
     * have not done, on the screen they open the app to.
     */
    @Test
    fun anEmptySessionRowShowsNoCountsAtAll() {
        val repo = FakeSessionRepository()
        val vm = SessionsViewModel(repo, FakeStatsRepository(), ActiveSessionHolder())
        composeRule.setContent {
            SessionsScreen(viewModel = vm, onOpenSession = {}, onNewSession = {})
        }

        composeRule.onAllNodesWithTag("sessionBadges").assertCountEquals(0)
    }

    @Test
    fun oneOfSomethingIsNotWrittenAsAPlural() {
        val repo = FakeSessionRepository()
        val vm = SessionsViewModel(
            repo,
            FakeStatsRepository(messages = listOf(MessageTally("s1", 1, 1))),
            ActiveSessionHolder()
        )
        composeRule.setContent {
            SessionsScreen(viewModel = vm, onOpenSession = {}, onNewSession = {})
        }

        composeRule.onNodeWithText("1 message").assertIsDisplayed()
    }

    /** Counts must not stop the list itself from appearing. */
    @Test
    fun theListStillRendersWhenTheCountsCannotBeRead() {
        val repo = FakeSessionRepository()
        val vm = SessionsViewModel(repo, FailingStatsRepository(), ActiveSessionHolder())
        composeRule.setContent {
            SessionsScreen(viewModel = vm, onOpenSession = {}, onNewSession = {})
        }

        composeRule.onNodeWithText("First session").assertIsDisplayed()
    }
}
