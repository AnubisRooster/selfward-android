package com.theraipist.core.repository

import com.theraipist.core.model.Message
import com.theraipist.core.model.Persona

class InMemorySessionRepository : SessionRepository {

    private val sessions = LinkedHashMap<String, Session>()
    private val messages = LinkedHashMap<String, MutableList<Message>>()
    private var archived = setOf<String>()

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

    override suspend fun listSessions(): List<SessionSummary> =
        sessions.values.filterNot { it.id in archived }
            .map { SessionSummary(it.id, it.title, it.updatedAt) }

    override suspend fun listArchivedSessions(): List<SessionSummary> =
        sessions.values.filter { it.id in archived }
            .map { SessionSummary(it.id, it.title, it.updatedAt) }

    override suspend fun setArchived(sessionId: String, archived: Boolean) {
        if (archived) this.archived += sessionId else this.archived -= sessionId
    }

    override suspend fun deleteSession(sessionId: String) {
        sessions.remove(sessionId)
        messages.remove(sessionId)
        archived -= sessionId
    }
}
