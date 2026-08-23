package com.theraipist.ui.sessions

import com.theraipist.core.ActiveSessionHolder
import com.theraipist.core.model.Message
import com.theraipist.core.model.Persona
import com.theraipist.core.repository.Session
import com.theraipist.core.repository.SessionRepository
import com.theraipist.core.repository.SessionSummary
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
        override suspend fun listSessions(): List<SessionSummary> = sessions.toList()

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
        val vm = SessionsViewModel(FakeSessionRepository(), ActiveSessionHolder())
        assertEquals(2, vm.sessions.value.size)
    }

    @Test
    fun deleteSessionRemovesItAndRefreshes() = runTest {
        val repo = FakeSessionRepository()
        val vm = SessionsViewModel(repo, ActiveSessionHolder())

        vm.deleteSession("s1")

        assertEquals(listOf("s1"), repo.deletedIds)
        assertTrue(vm.sessions.value.none { it.id == "s1" })
    }

    @Test
    fun openSessionSetsThePendingSessionOnTheHolder() = runTest {
        val holder = ActiveSessionHolder()
        val vm = SessionsViewModel(FakeSessionRepository(), holder)

        vm.openSession("s2")

        assertEquals("s2", holder.consumePendingOpen())
    }
}
