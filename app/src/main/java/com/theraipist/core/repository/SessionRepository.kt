package com.theraipist.core.repository

import com.theraipist.core.model.Message
import com.theraipist.core.model.Persona

data class Session(
    val id: String,
    val persona: Persona,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long
)

data class SessionSummary(
    val id: String,
    val title: String,
    val updatedAt: Long
)

interface SessionRepository {
    suspend fun createSession(persona: Persona, title: String = "New Session"): Session
    suspend fun appendMessage(sessionId: String, message: Message)
    suspend fun getMessages(sessionId: String): List<Message>
    /** Active sessions, newest first. Archived ones are not included. */
    suspend fun listSessions(): List<SessionSummary>

    /** Sessions the user has set aside. Nothing is deleted by archiving. */
    suspend fun listArchivedSessions(): List<SessionSummary>

    suspend fun setArchived(sessionId: String, archived: Boolean)

    /** The session with [sessionId], including the persona it was started with, or null. */
    suspend fun getSession(sessionId: String): Session?

    /** Permanently deletes the session and its messages. Graph nodes/insights derived from it are kept - the knowledge graph is one continuous memory, not scoped to any single session's transcript. */
    suspend fun deleteSession(sessionId: String)
}
