package com.theraipist.core

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
    val nodes = _nodes.asStateFlow()

    fun addInsights(insights: List<String>) {
        insights.forEach { graph.addNode(it, "insight") }
        _nodes.value = graph.allNodes()
    }
}
