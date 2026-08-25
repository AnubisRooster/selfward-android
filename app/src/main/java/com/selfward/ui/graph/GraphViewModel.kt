package com.selfward.ui.graph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfward.core.GraphHolder
import com.selfward.core.graph.GraphEdge
import com.selfward.core.graph.GraphNode
import com.selfward.core.graph.TherapyInsights
import com.selfward.core.modality.ModalityRouter
import com.selfward.core.modality.TherapyModality
import com.selfward.core.export.GraphExport
import com.selfward.core.repository.SessionRepository
import com.selfward.data.export.ExportFiles
import com.selfward.data.export.ExportedFile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InsightsUiState(
    val nodes: List<GraphNode> = emptyList(),
    val edges: List<GraphEdge> = emptyList(),
    val insights: TherapyInsights.Result? = null,
    val loading: Boolean = true,
    val exportError: String? = null
) {
    val isEmpty: Boolean get() = nodes.isEmpty()
}

@HiltViewModel
class GraphViewModel @Inject constructor(
    private val graphHolder: GraphHolder,
    private val sessionRepository: SessionRepository,
    private val exportFiles: ExportFiles
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    /** Set when a file is written and waiting to be offered to the share sheet. */
    private val _exported = MutableStateFlow<ExportedFile?>(null)
    val exported: StateFlow<ExportedFile?> = _exported.asStateFlow()

    val nodes: StateFlow<List<GraphNode>> = graphHolder.nodes
    val edges: StateFlow<List<GraphEdge>> = graphHolder.edges

    init {
        refresh()
    }

    enum class GraphFormat { JSON, GRAPHML }

    /**
     * Writes the graph in [format] and offers it for sharing.
     *
     * Writing is done off the main thread: the graph can hold thousands of
     * nodes, and serialising that is not work to do while the person is
     * watching the frame.
     */
    fun export(format: GraphFormat) {
        viewModelScope.launch {
            graphHolder.ensureLoaded()
            val nodes = graphHolder.nodes.value
            val edges = graphHolder.edges.value
            val file = runCatching {
                withContext(Dispatchers.IO) {
                    when (format) {
                        GraphFormat.JSON -> exportFiles.writeText(
                            GraphExport.JSON_FILENAME,
                            GraphExport.cytoscapeJson(nodes, edges)
                        ).let { ExportedFile(it, "application/json", "Knowledge graph") }

                        GraphFormat.GRAPHML -> exportFiles.writeText(
                            GraphExport.GRAPHML_FILENAME,
                            GraphExport.graphML(nodes, edges)
                        ).let { ExportedFile(it, "application/xml", "Knowledge graph") }
                    }
                }
            }.getOrNull()

            if (file == null) {
                _uiState.value = _uiState.value.copy(exportError = "Couldn't write the file.")
            } else {
                _exported.value = file
            }
        }
    }

    /** Called once the share sheet has been shown, so it is not shown again. */
    fun exportHandled() {
        _exported.value = null
    }

    fun dismissExportError() {
        _uiState.value = _uiState.value.copy(exportError = null)
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
