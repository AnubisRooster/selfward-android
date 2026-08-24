package com.selfward.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Feelings and symbols are stored as newline-separated text rather than a
 * relation: they are short free-text lists that are only ever read back whole.
 */
@Entity(tableName = "dreams", indices = [Index("sessionId")])
data class DreamEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val narrative: String,
    val feelings: String,
    val symbols: String,
    val analysis: String,
    val createdAt: Long
)
