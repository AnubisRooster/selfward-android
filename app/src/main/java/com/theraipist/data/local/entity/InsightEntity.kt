package com.theraipist.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "insights", indices = [Index("sessionId")])
data class InsightEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val text: String,
    val source: String?,
    val kind: String?,
    val createdAt: Long
)
