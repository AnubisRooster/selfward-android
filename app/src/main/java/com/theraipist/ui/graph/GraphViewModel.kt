package com.theraipist.ui.graph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theraipist.core.GraphHolder
import com.theraipist.core.graph.GraphEdge
import com.theraipist.core.graph.GraphNode
import com.theraipist.core.graph.TherapyInsights
import com.theraipist.core.modality.ModalityRouter
import com.theraipist.core.modality.TherapyModality
import com.theraipist.core.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InsightsUiState(
    val nodes: List<GraphNode> = emptyList(),
    val edges: List<GraphEdge> = emptyList(),
    val insights: TherapyInsights.Result? = null,
    val loading: Boolean = true
) {
    val isEmpty: Boolean get() = nodes.isEmpty()
}

@HiltViewModel
class GraphViewModel @Inject constructor(
    private val graphHolder: GraphHolder,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    val nodes: StateFlow<List<GraphNode>> = graphHolder.nodes
    val edges: StateFlow<List<GraphEdge>> = graphHolder.edges

    init {
        refresh()
    }

    /**
     * Recomputed on every visit rather than only in init, because the graph
     * grows while the user is away on the chat tab and this ViewModel outlives
     * the navigation.
     */
    fun refresh() {
        viewModelScope.launch {
            graphHolder.ensureLoaded()
            val nodes = graphHolder.nodes.value
            val edges = graphHolder.edges.value
            _uiState.value = InsightsUiState(
                nodes = nodes,
                edges = edges,
                insights = if (nodes.isEmpty()) null
                else TherapyInsights.generate(nodes, edges, currentFrameworkKey()),
                loading = false
            )
        }
    }

    /**
     * The framework the most recent conversation was running under, so the
     * commentary matches the work actually being done. Falls back to the
     * integrated prompt when nothing has been said yet.
     */
    private suspend fun currentFrameworkKey(): String {
        val latest = runCatching { sessionRepository.listSessions().firstOrNull() }.getOrNull()
            ?: return ModalityRouter.promptKey(TherapyModality.TALK)
        val modality = runCatching { sessionRepository.getMessages(latest.id) }.getOrNull()
            ?.lastOrNull { it.modality != null }
            ?.modality
            ?.let { name -> TherapyModality.entries.firstOrNull { it.name == name } }
            ?: TherapyModality.TALK
        return ModalityRouter.promptKey(modality)
    }
}
