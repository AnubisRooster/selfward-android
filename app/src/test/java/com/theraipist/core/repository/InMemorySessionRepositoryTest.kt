package com.theraipist.core.repository

import com.theraipist.config.PersonaKind
import com.theraipist.core.model.Message
import com.theraipist.core.model.Persona
import com.theraipist.core.model.Role
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class InMemorySessionRepositoryTest {

    @Test
    fun createAndAppendAndList() = runTest {
        val repo = InMemorySessionRepository()
        val session = repo.createSession(Persona(PersonaKind.THERAPIST), "Session 1")
        repo.appendMessage(session.id, Message("1", Role.USER, "hi"))
        repo.appendMessage(session.id, Message("2", Role.ASSISTANT, "hello"))

        val msgs = repo.getMessages(session.id)
        assertEquals(2, msgs.size)
        assertEquals("hi", msgs[0].content)

        val list = repo.listSessions()
        assertEquals(1, list.size)
        assertEquals("Session 1", list[0].title)
    }

    @Test
    fun deleteSessionRemovesItAndItsMessages() = runTest {
        val repo = InMemorySessionRepository()
        val session = repo.createSession(Persona(PersonaKind.THERAPIST), "Session 1")
        repo.appendMessage(session.id, Message("1", Role.USER, "hi"))

        repo.deleteSession(session.id)

        assertTrue(repo.listSessions().isEmpty())
        assertTrue(repo.getMessages(session.id).isEmpty())
    }
}
