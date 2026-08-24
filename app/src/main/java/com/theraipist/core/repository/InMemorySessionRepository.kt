package com.theraipist.core.repository

import com.theraipist.core.model.Message
import com.theraipist.core.model.Persona

class InMemorySessionRepository : SessionRepository {

    private val sessions = LinkedHashMap<String, Session>()
    private val messages = LinkedHashMap<String, MutableList<Message>>()

    override suspend fun createSession(persona: Persona, title: String): Session {
        val now = System.currentTimeMillis()
        val id = "sess_${now}_${sessions.size}"
        val session = Session(id, persona, title, now, now)
        sessions[id] = session
        messages[id] = mutableListOf()
        return session
    }

    override suspend fun appendMessage(sessionId: String, message: Message) {
        messages[sessionId]?.add(message)
        sessions[sessionId]?.let {
            sessions[sessionId] = it.copy(updatedAt = System.currentTimeMillis())
        }
    }

    override suspend fun getMessages(sessionId: String): List<Message> {
        return messages[sessionId]?.toList() ?: emptyList()
    }

    override suspend fun getSession(sessionId: String): Session? = sessions[sessionId]

    override suspend fun listSessions(): List<SessionSummary> {
        return sessions.values.map { SessionSummary(it.id, it.title, it.updatedAt) }
    }

    override suspend fun deleteSession(sessionId: String) {
        sessions.remove(sessionId)
        messages.remove(sessionId)
    }
}
