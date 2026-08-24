package com.theraipist.core.journal

/** The kinds of note iOS offers, in the same order its picker lists them. */
enum class NoteType(val label: String) {
    REFLECTION("Reflection"),
    SESSION_NOTE("Session note"),
    JOURNAL("Journal")
}

data class Note(
    val id: String,
    val sessionId: String,
    val type: NoteType,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long
)

interface NoteRepository {
    suspend fun create(sessionId: String, type: NoteType, title: String, content: String): Note
    suspend fun listForSession(sessionId: String): List<Note>
    suspend fun listAll(): List<Note>
    suspend fun delete(noteId: String)
}
