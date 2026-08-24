package com.selfward.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "notes", indices = [Index("sessionId")])
data class NoteEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val type: String,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long
)
