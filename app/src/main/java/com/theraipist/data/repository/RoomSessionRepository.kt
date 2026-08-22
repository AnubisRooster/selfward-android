package com.theraipist.data.repository

import com.theraipist.core.model.Message
import com.theraipist.core.model.Persona
import com.theraipist.core.repository.Session
import com.theraipist.core.repository.SessionRepository
import com.theraipist.core.repository.SessionSummary
import com.theraipist.data.local.Mappers.toDomain
import com.theraipist.data.local.Mappers.toEntity
import com.theraipist.data.local.Mappers.toSessionEntity
import com.theraipist.data.local.TherAIpistDatabase
import java.util.UUID

class RoomSessionRepository(
    private val db: TherAIpistDatabase
) : SessionRepository {

    override suspend fun createSession(persona: Persona, title: String): Session {
        val now = System.currentTimeMillis()
        val id = "sess_${now}_${UUID.randomUUID()}"
        val entity = persona.toSessionEntity(id, title, now, now)
        db.sessionDao().insert(entity)
        return entity.toDomain()
    }

    override suspend fun appendMessage(sessionId: String, message: Message) {
        db.messageDao().insert(message.toEntity(sessionId))
        db.sessionDao().touch(sessionId, System.currentTimeMillis())
    }

    override suspend fun getMessages(sessionId: String): List<Message> =
        db.messageDao().getBySession(sessionId).map { it.toDomain() }

    override suspend fun listSessions(): List<SessionSummary> =
        db.sessionDao().getAll().map { SessionSummary(it.id, it.title, it.updatedAt) }
}
