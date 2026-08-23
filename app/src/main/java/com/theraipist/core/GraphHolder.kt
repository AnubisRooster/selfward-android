package com.theraipist.core

import com.theraipist.core.embedding.EmbeddingModelDownloader
import com.theraipist.core.embedding.EmbeddingProvider
import com.theraipist.core.embedding.EmbeddingProviderFactory
import com.theraipist.core.embedding.MemoryVectorStore
import com.theraipist.core.graph.GraphEdge
import com.theraipist.core.graph.GraphNode
import com.theraipist.core.graph.TherapyGraph
import com.theraipist.core.local.DownloadStatus
import com.theraipist.core.repository.GraphRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GraphHolder @Inject constructor(
    private val repository: GraphRepository,
    private val embeddingModelDownloader: EmbeddingModelDownloader,
    private val embeddingProviderFactory: EmbeddingProviderFactory,
    private val memoryVectorStore: MemoryVectorStore
) {
    private val graph = TherapyGraph()
    private val _nodes = MutableStateFlow<List<GraphNode>>(emptyList())
    private val _edges = MutableStateFlow<List<GraphEdge>>(emptyList())
    val nodes = _nodes.asStateFlow()
    val edges = _edges.asStateFlow()

    private var lastNodeId: String? = null
    private var loaded = false
    private val loadMutex = Mutex()
    private var embeddingProvider: EmbeddingProvider? = null

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
            embed(id, text)
            lastNodeId?.let { previous ->
                val edgeId = graph.addEdge(previous, id, "next")
                val edge = graph.allEdges().last { it.id == edgeId }
                runCatching { repository.saveEdge(sessionId, edge) }
            }
            lastNodeId = id
        }
        publish()
    }

    /**
     * Nodes whose text is most semantically similar to [queryText], via the
     * on-device embedding model. Empty if that model hasn't been downloaded, or
     * if nothing has been embedded yet (only nodes added since the model became
     * available are indexed - embeddings aren't persisted, matching
     * MemoryVectorStore's current in-memory-only scope).
     */
    suspend fun findSimilar(queryText: String, k: Int = 5): List<GraphNode> {
        val provider = readyEmbeddingProvider() ?: return emptyList()
        val queryVector = runCatching { provider.embed(queryText) }.getOrNull() ?: return emptyList()
        val nodesById = graph.allNodes().associateBy { it.id }
        return memoryVectorStore.query(queryVector, k).mapNotNull { nodesById[it.id] }
    }

    private suspend fun embed(nodeId: String, text: String) {
        val provider = readyEmbeddingProvider() ?: return
        runCatching { memoryVectorStore.add(nodeId, provider.embed(text)) }
    }

    private fun readyEmbeddingProvider(): EmbeddingProvider? {
        if (embeddingModelDownloader.status() != DownloadStatus.DOWNLOADED) return null
        embeddingProvider?.let { return it }
        return runCatching {
            embeddingProviderFactory.create(
                embeddingModelDownloader.onnxFile(),
                embeddingModelDownloader.vocabFile()
            )
        }.getOrNull()?.also { embeddingProvider = it }
    }

    private fun publish() {
        _nodes.value = graph.allNodes()
        _edges.value = graph.allEdges()
    }
}
