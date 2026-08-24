package com.selfward.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "graph_edges", indices = [Index("sessionId")])
data class GraphEdgeEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val sourceId: String,
    val targetId: String,
    val label: String?,
    val weight: Float?
)
