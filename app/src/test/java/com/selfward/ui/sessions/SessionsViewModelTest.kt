package com.selfward.ui.sessions

import com.selfward.core.ActiveSessionHolder
import com.selfward.core.dashboard.MessageTally
import com.selfward.core.dashboard.StatsRepository
import com.selfward.core.dashboard.Tally
import com.selfward.core.model.Message
import com.selfward.core.model.Persona
import com.selfward.core.repository.Session
import com.selfward.core.repository.SessionRepository
import com.selfward.core.repository.SessionSummary
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
class SessionsViewModelTest {

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

    private class FakeSessionRepository : SessionRepository {
        val sessions = mutableListOf(
            SessionSummary("s1", "First", 100),
            SessionSummary("s2", "Second", 200)
        )
        val deletedIds = mutableListOf<String>()

        override suspend fun createSession(persona: Persona, title: String): Session =
            throw UnsupportedOperationException("not used by SessionsViewModel")

        override suspend fun appendMessage(sessionId: String, message: Message) {}
        override suspend fun getMessages(sessionId: String): List<Message> = emptyList()
        override suspend fun getSession(sessionId: String): Session? = null

        override suspend fun listSessions(): List<SessionSummary> = sessions.toList()

        private var archivedIds = setOf<String>()


        override suspend fun listArchivedSessions(): List<SessionSummary> =

            sessions.filter { it.id in archivedIds }


        override suspend fun setArchived(sessionId: String, archived: Boolean) {

            archivedIds = if (archived) archivedIds + sessionId else archivedIds - sessionId

        }


        override suspend fun deleteSession(sessionId: String) {
            deletedIds += sessionId
            sessions.removeAll { it.id == sessionId }
        }
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

    @Test
    fun loadsSessionsOnInit() = runTest {
        val vm = SessionsViewModel(FakeSessionRepository(), FakeStatsRepository(), ActiveSessionHolder())
        assertEquals(2, vm.sessions.value.size)
    }

    @Test
    fun deleteSessionRemovesItAndRefreshes() = runTest {
        val repo = FakeSessionRepository()
        val vm = SessionsViewModel(repo, FakeStatsRepository(), ActiveSessionHolder())

        vm.deleteSession("s1")

        assertEquals(listOf("s1"), repo.deletedIds)
        assertTrue(vm.sessions.value.none { it.id == "s1" })
    }

    @Test
    fun openSessionSetsThePendingSessionOnTheHolder() = runTest {
        val holder = ActiveSessionHolder()
        val vm = SessionsViewModel(FakeSessionRepository(), FakeStatsRepository(), holder)

        vm.openSession("s2")

        assertEquals("s2", holder.consumePendingOpen())
    }
}
