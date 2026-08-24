package com.selfward.data.repository

import com.selfward.core.journal.Dream
import com.selfward.core.journal.DreamRepository
import com.selfward.core.journal.Note
import com.selfward.core.journal.NoteRepository
import com.selfward.core.journal.NoteType
import com.selfward.data.local.SelfwardDatabase
import com.selfward.data.local.entity.DreamEntity
import com.selfward.data.local.entity.NoteEntity
import java.util.UUID

class RoomNoteRepository(private val db: SelfwardDatabase) : NoteRepository {

    override suspend fun create(sessionId: String, type: NoteType, title: String, content: String): Note {
        val now = System.currentTimeMillis()
        val entity = NoteEntity(
            id = "note_${now}_${UUID.randomUUID()}",
            sessionId = sessionId,
            type = type.name,
            title = title,
            content = content,
            createdAt = now,
            updatedAt = now
        )
        db.noteDao().insert(entity)
        return entity.toDomain()
    }

    override suspend fun listForSession(sessionId: String): List<Note> =
        db.noteDao().getBySession(sessionId).map { it.toDomain() }

    override suspend fun listAll(): List<Note> = db.noteDao().getAll().map { it.toDomain() }

    override suspend fun delete(noteId: String) = db.noteDao().deleteById(noteId)
}

class RoomDreamRepository(private val db: SelfwardDatabase) : DreamRepository {

    override suspend fun record(
        sessionId: String,
        narrative: String,
        feelings: List<String>,
        symbols: List<String>
    ): Dream {
        val now = System.currentTimeMillis()
        val entity = DreamEntity(
            id = "dream_${now}_${UUID.randomUUID()}",
            sessionId = sessionId,
            narrative = narrative,
            feelings = feelings.joinToList(),
            symbols = symbols.joinToList(),
            analysis = "",
            createdAt = now
        )
        db.dreamDao().insert(entity)
        return entity.toDomain()
    }

    override suspend fun setAnalysis(dreamId: String, analysis: String) =
        db.dreamDao().setAnalysis(dreamId, analysis)

    override suspend fun listForSession(sessionId: String): List<Dream> =
        db.dreamDao().getBySession(sessionId).map { it.toDomain() }

    override suspend fun listAll(): List<Dream> = db.dreamDao().getAll().map { it.toDomain() }

    override suspend fun delete(dreamId: String) = db.dreamDao().deleteById(dreamId)
}

private fun NoteEntity.toDomain() = Note(
    id = id,
    sessionId = sessionId,
    type = runCatching { NoteType.valueOf(type) }.getOrDefault(NoteType.REFLECTION),
    title = title,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun DreamEntity.toDomain() = Dream(
    id = id,
    sessionId = sessionId,
    narrative = narrative,
    feelings = feelings.splitList(),
    symbols = symbols.splitList(),
    analysis = analysis,
    createdAt = createdAt
)

/** Free-text lists round-trip as newline-separated text; blanks are dropped. */
private fun List<String>.joinToList(): String =
    filter { it.isNotBlank() }.joinToString("\n") { it.trim() }

private fun String.splitList(): List<String> =
    split("\n").map { it.trim() }.filter { it.isNotEmpty() }
