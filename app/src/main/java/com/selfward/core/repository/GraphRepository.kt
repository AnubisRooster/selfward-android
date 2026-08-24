package com.selfward.core.repository

import com.selfward.core.graph.GraphEdge
import com.selfward.core.graph.GraphNode

data class GraphSnapshot(val nodes: List<GraphNode>, val edges: List<GraphEdge>)

/** Durable storage for the client's knowledge graph - one continuous memory across sessions. */
interface GraphRepository {
    suspend fun saveNode(sessionId: String, node: GraphNode)
    suspend fun saveEdge(sessionId: String, edge: GraphEdge)
    suspend fun saveInsight(sessionId: String, text: String)

    /** All nodes/edges ever persisted, across every session, oldest first. */
    suspend fun loadAll(): GraphSnapshot
}
