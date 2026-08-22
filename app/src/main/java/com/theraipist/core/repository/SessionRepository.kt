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
    suspend fun listSessions(): List<SessionSummary>
}
