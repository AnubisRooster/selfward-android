package com.theraipist.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "graph_nodes", indices = [Index("sessionId")])
data class GraphNodeEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val label: String,
    val kind: String?,
    val createdAt: Long
)
