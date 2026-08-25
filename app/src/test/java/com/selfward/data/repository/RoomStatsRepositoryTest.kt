package com.selfward.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.selfward.core.graph.GraphNode
import com.selfward.core.journal.NoteType
import com.selfward.core.model.Message
import com.selfward.core.model.Persona
import com.selfward.core.model.Role
import com.selfward.config.PersonaKind
import com.selfward.data.local.SelfwardDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The counting queries, against a real database.
 *
 * These matter more than they look. The SQL compares `role` to the literals
 * `'USER'` and `'SYSTEM'`, which are the [Role] enum's names written out as
 * strings — nothing in the compiler connects the two. Rename a constant and the
 * counts go quietly wrong rather than failing to build, which is exactly the
 * kind of bug that reaches a phone.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class RoomStatsRepositoryTest {

    private lateinit var db: SelfwardDatabase
    private lateinit var stats: RoomStatsRepository
    private lateinit var sessions: RoomSessionRepository
    private lateinit var graph: RoomGraphRepository
    private lateinit var notes: RoomNoteRepository
    private lateinit var dreams: RoomDreamRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, SelfwardDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        stats = RoomStatsRepository(db)
        sessions = RoomSessionRepository(db)
        graph = RoomGraphRepository(db)
        notes = RoomNoteRepository(db)
        dreams = RoomDreamRepository(db)
    }

    @After
    fun tearDown() = db.close()

    private suspend fun newSession(title: String) =
        sessions.createSession(Persona(PersonaKind.THERAPIST), title).id

    private suspend fun say(sessionId: String, role: Role, text: String, modality: String? = null) =
        sessions.appendMessage(
            sessionId,
            Message(
                id = "m_${sessionId}_${text.hashCode()}_${role.name}",
                role = role,
                content = text,
                modality = modality
            )
        )

    @Test
    fun messagesAreCountedPerSession() = runTest {
        val a = newSession("A")
        val b = newSession("B")
        say(a, Role.USER, "one")
        say(a, Role.ASSISTANT, "two")
        say(b, Role.USER, "three")

        val tallies = stats.messageTallies().associateBy { it.sessionId }

        assertEquals(2, tallies.getValue(a).total)
        assertEquals(1, tallies.getValue(b).total)
    }

    /**
     * The system prompt is not something the person said, and it is written
     * into every session. Counting it would inflate every total by the same
     * amount and make an untouched session look like it had been used.
     */
    @Test
    fun theSystemPromptIsNotCountedAsAMessage() = runTest {
        val id = newSession("A")
        say(id, Role.SYSTEM, "You are a therapist.")
        say(id, Role.USER, "hello")

        val tally = stats.messageTallies().single { it.sessionId == id }

        assertEquals(1, tally.total)
        assertEquals(1, tally.fromYou)
    }

    /**
     * The SQL matches the literal 'USER'. If [Role]'s constants are ever
     * renamed this is what notices.
     */
    @Test
    fun onlyTheClientsOwnMessagesCountAsFromYou() = runTest {
        val id = newSession("A")
        say(id, Role.USER, "one")
        say(id, Role.USER, "two")
        say(id, Role.ASSISTANT, "reply")

        val tally = stats.messageTallies().single { it.sessionId == id }

        assertEquals(3, tally.total)
        assertEquals(2, tally.fromYou)
        assertEquals(Role.USER.name, "USER")
        assertEquals(Role.SYSTEM.name, "SYSTEM")
    }

    @Test
    fun aSessionWithNoMessagesHasNoTallyRatherThanAZeroOne() = runTest {
        newSession("untouched")

        assertTrue(stats.messageTallies().isEmpty())
    }

    @Test
    fun modalitiesAreCountedAcrossSessions() = runTest {
        val a = newSession("A")
        val b = newSession("B")
        say(a, Role.USER, "one", modality = "TALK")
        say(b, Role.USER, "two", modality = "TALK")
        say(b, Role.USER, "three", modality = "DREAM")

        assertEquals(mapOf("TALK" to 2, "DREAM" to 1), stats.modalityTallies())
    }

    @Test
    fun messagesWithNoModalityAreLeftOutOfTheModalityCounts() = runTest {
        val id = newSession("A")
        say(id, Role.USER, "plain")

        assertTrue(stats.modalityTallies().isEmpty())
    }

    @Test
    fun graphNodesAreCountedPerSession() = runTest {
        val a = newSession("A")
        val b = newSession("B")
        graph.saveNode(a, GraphNode("n_1", "Mother", "person", 1L))
        graph.saveNode(a, GraphNode("n_2", "Guilt", "emotion", 2L))
        graph.saveNode(b, GraphNode("n_3", "Work", "theme", 3L))

        val tallies = stats.nodeTallies().associate { it.sessionId to it.count }

        assertEquals(2, tallies[a])
        assertEquals(1, tallies[b])
    }

    @Test
    fun notesAndDreamsAreCountedPerSession() = runTest {
        val id = newSession("A")
        notes.create(id, NoteType.REFLECTION, "A note", "body")
        notes.create(id, NoteType.JOURNAL, "Another", "body")
        dreams.record(id, "I was flying", listOf("free"), listOf("flight"))

        assertEquals(2, stats.noteTallies().single { it.sessionId == id }.count)
        assertEquals(1, stats.dreamTallies().single { it.sessionId == id }.count)
    }

    @Test
    fun deletingASessionRemovesItsMessagesFromTheCounts() = runTest {
        val id = newSession("A")
        say(id, Role.USER, "one")
        sessions.deleteSession(id)

        assertTrue(stats.messageTallies().none { it.sessionId == id })
    }

    @Test
    fun anEmptyDatabaseCountsNothingRatherThanFailing() = runTest {
        assertTrue(stats.messageTallies().isEmpty())
        assertTrue(stats.modalityTallies().isEmpty())
        assertTrue(stats.nodeTallies().isEmpty())
        assertTrue(stats.noteTallies().isEmpty())
        assertTrue(stats.dreamTallies().isEmpty())
    }
}
