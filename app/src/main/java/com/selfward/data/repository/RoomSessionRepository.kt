package com.selfward.data.repository

import com.selfward.core.model.Message
import com.selfward.core.model.Persona
import com.selfward.core.repository.Session
import com.selfward.core.repository.SessionRepository
import com.selfward.core.repository.SessionSummary
import com.selfward.data.local.toDomain
import com.selfward.data.local.toEntity
import com.selfward.data.local.toSessionEntity
import com.selfward.data.local.SelfwardDatabase
import java.util.UUID

class RoomSessionRepository(
    private val db: SelfwardDatabase
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

    override suspend fun getSession(sessionId: String): Session? =
        db.sessionDao().getById(sessionId)?.toDomain()

    override suspend fun listSessions(): List<SessionSummary> =
        db.sessionDao().getAll().map { SessionSummary(it.id, it.title, it.updatedAt) }

    override suspend fun listArchivedSessions(): List<SessionSummary> =
        db.sessionDao().getArchived().map { SessionSummary(it.id, it.title, it.updatedAt) }

    override suspend fun setArchived(sessionId: String, archived: Boolean) =
        db.sessionDao().setArchived(sessionId, archived, System.currentTimeMillis())

    override suspend fun deleteSession(sessionId: String) {
        db.messageDao().deleteBySession(sessionId)
        db.sessionDao().deleteById(sessionId)
    }
}
