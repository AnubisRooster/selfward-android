package com.theraipist.data.repository

import com.theraipist.core.graph.GraphEdge
import com.theraipist.core.graph.GraphNode
import com.theraipist.core.repository.GraphRepository
import com.theraipist.core.repository.GraphSnapshot
import com.theraipist.data.local.TherAIpistDatabase
import com.theraipist.data.local.entity.InsightEntity
import com.theraipist.data.local.toDomain
import com.theraipist.data.local.toEntity

class RoomGraphRepository(
    private val db: TherAIpistDatabase
) : GraphRepository {

    override suspend fun saveNode(sessionId: String, node: GraphNode) {
        db.graphDao().insertNode(node.toEntity(sessionId))
    }

    override suspend fun saveEdge(sessionId: String, edge: GraphEdge) {
        db.graphDao().insertEdge(edge.toEntity(sessionId))
    }

    override suspend fun saveInsight(sessionId: String, text: String) {
        db.insightDao().insert(
            InsightEntity(
                id = "insight_${System.nanoTime()}",
                sessionId = sessionId,
                text = text,
                source = "assistant",
                kind = "insight",
                createdAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun loadAll(): GraphSnapshot {
        val nodes = db.graphDao().getAllNodes().map { it.toDomain() }
        val edges = db.graphDao().getAllEdges().map { it.toDomain() }
        return GraphSnapshot(nodes, edges)
    }
}
