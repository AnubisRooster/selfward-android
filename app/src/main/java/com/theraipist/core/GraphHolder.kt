package com.theraipist.core

import com.theraipist.core.graph.GraphEdge
import com.theraipist.core.graph.GraphNode
import com.theraipist.core.graph.TherapyGraph
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GraphHolder @Inject constructor() {
    private val graph = TherapyGraph()
    private val _nodes = MutableStateFlow<List<GraphNode>>(emptyList())
    private val _edges = MutableStateFlow<List<GraphEdge>>(emptyList())
    val nodes = _nodes.asStateFlow()
    val edges = _edges.asStateFlow()

    private var lastNodeId: String? = null

    fun addInsights(insights: List<String>) {
        insights.forEach { text ->
            val id = graph.addNode(text, "insight")
            lastNodeId?.let { graph.addEdge(it, id, "next") }
            lastNodeId = id
        }
        _nodes.value = graph.allNodes()
        _edges.value = graph.allEdges()
    }
}
