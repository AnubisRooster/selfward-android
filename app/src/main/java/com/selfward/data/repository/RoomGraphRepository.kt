package com.selfward.data.repository

import com.selfward.core.graph.GraphEdge
import com.selfward.core.graph.GraphNode
import com.selfward.core.repository.GraphRepository
import com.selfward.core.repository.GraphSnapshot
import com.selfward.data.local.SelfwardDatabase
import com.selfward.data.local.entity.InsightEntity
import com.selfward.data.local.toDomain
import com.selfward.data.local.toEntity

class RoomGraphRepository(
    private val db: SelfwardDatabase
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
