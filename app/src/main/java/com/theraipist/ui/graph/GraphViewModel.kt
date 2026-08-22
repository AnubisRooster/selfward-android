package com.theraipist.ui.graph

import androidx.lifecycle.ViewModel
import com.theraipist.core.GraphHolder
import com.theraipist.core.graph.GraphEdge
import com.theraipist.core.graph.GraphNode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class GraphViewModel @Inject constructor(
    private val graphHolder: GraphHolder
) : ViewModel() {
    val nodes: StateFlow<List<GraphNode>> = graphHolder.nodes
    val edges: StateFlow<List<GraphEdge>> = graphHolder.edges
}
