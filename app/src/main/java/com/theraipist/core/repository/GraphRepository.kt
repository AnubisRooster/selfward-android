package com.theraipist.core.repository

import com.theraipist.core.graph.GraphEdge
import com.theraipist.core.graph.GraphNode

data class GraphSnapshot(val nodes: List<GraphNode>, val edges: List<GraphEdge>)

/** Durable storage for the client's knowledge graph - one continuous memory across sessions. */
interface GraphRepository {
    suspend fun saveNode(sessionId: String, node: GraphNode)
    suspend fun saveEdge(sessionId: String, edge: GraphEdge)
    suspend fun saveInsight(sessionId: String, text: String)

    /** All nodes/edges ever persisted, across every session, oldest first. */
    suspend fun loadAll(): GraphSnapshot
}
