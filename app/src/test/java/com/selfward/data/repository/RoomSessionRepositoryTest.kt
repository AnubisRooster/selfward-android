package com.selfward.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.selfward.config.PersonaKind
import com.selfward.core.model.Message
import com.selfward.core.model.Persona
import com.selfward.core.model.Role
import com.selfward.data.local.SelfwardDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class RoomSessionRepositoryTest {

    private lateinit var db: SelfwardDatabase
    private lateinit var repo: RoomSessionRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, SelfwardDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = RoomSessionRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun createAppendGetList() = runTest {
        val session = repo.createSession(Persona(PersonaKind.THERAPIST), "S1")
        repo.appendMessage(session.id, Message("1", Role.USER, "hi"))
        repo.appendMessage(session.id, Message("2", Role.ASSISTANT, "hello"))

        val msgs = repo.getMessages(session.id)
        assertEquals(2, msgs.size)
        assertEquals("hi", msgs[0].content)
        assertEquals(Role.USER, msgs[0].role)

        val list = repo.listSessions()
        assertEquals(1, list.size)
        assertEquals("S1", list[0].title)
    }

    @Test
    fun deleteSessionRemovesItAndItsMessages() = runTest {
        val session = repo.createSession(Persona(PersonaKind.THERAPIST), "S1")
        repo.appendMessage(session.id, Message("1", Role.USER, "hi"))

        repo.deleteSession(session.id)

        assertEquals(0, repo.listSessions().size)
        assertEquals(0, repo.getMessages(session.id).size)
    }
}
