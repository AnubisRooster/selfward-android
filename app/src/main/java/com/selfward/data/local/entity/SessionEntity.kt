package com.selfward.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val personaKind: String,
    val name: String?,
    val companionGender: String?,
    val companionPersonality: String?,
    val spiritualTradition: String?,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isArchived: Boolean = false
)
