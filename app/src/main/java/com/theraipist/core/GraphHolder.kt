package com.theraipist.core

import com.theraipist.core.graph.GraphEdge
import com.theraipist.core.graph.GraphNode
import com.theraipist.core.graph.TherapyGraph
import com.theraipist.core.repository.GraphRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GraphHolder @Inject constructor(
    private val repository: GraphRepository
) {
    private val graph = TherapyGraph()
    private val _nodes = MutableStateFlow<List<GraphNode>>(emptyList())
    private val _edges = MutableStateFlow<List<GraphEdge>>(emptyList())
    val nodes = _nodes.asStateFlow()
    val edges = _edges.asStateFlow()

    private var lastNodeId: String? = null
    private var loaded = false
    private val loadMutex = Mutex()

    /** Loads the persisted graph into memory once; safe to call repeatedly/concurrently. */
    suspend fun ensureLoaded() {
        if (loaded) return
        loadMutex.withLock {
            if (loaded) return
            val snapshot = runCatching { repository.loadAll() }.getOrNull()
            if (snapshot != null && snapshot.nodes.isNotEmpty()) {
                graph.restore(snapshot.nodes, snapshot.edges)
                lastNodeId = snapshot.nodes.maxByOrNull { it.createdAt }?.id
                publish()
            }
            loaded = true
        }
    }

    suspend fun addInsights(sessionId: String, insights: List<String>) {
        ensureLoaded()
        insights.forEach { text ->
            val id = graph.addNode(text, "insight")
            val node = graph.allNodes().last { it.id == id }
            runCatching { repository.saveNode(sessionId, node) }
            runCatching { repository.saveInsight(sessionId, text) }
            lastNodeId?.let { previous ->
                val edgeId = graph.addEdge(previous, id, "next")
                val edge = graph.allEdges().last { it.id == edgeId }
                runCatching { repository.saveEdge(sessionId, edge) }
            }
            lastNodeId = id
        }
        publish()
    }

    private fun publish() {
        _nodes.value = graph.allNodes()
        _edges.value = graph.allEdges()
    }
}
