package com.selfward.core

import com.selfward.core.embedding.EmbeddingModelDownloader
import com.selfward.core.embedding.EmbeddingProvider
import com.selfward.core.embedding.EmbeddingProviderFactory
import com.selfward.core.embedding.MemoryVectorStore
import com.selfward.core.graph.GraphEdge
import com.selfward.core.graph.GraphNode
import com.selfward.core.graph.MessageAnalyzer
import com.selfward.core.graph.TherapyGraph
import com.selfward.core.local.DownloadStatus
import com.selfward.core.repository.GraphRepository
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
     * Reads what the client just wrote and folds the people, feelings and
     * beliefs it mentions into the graph, reinforcing anything already there.
     *
     * This runs on the client's own message rather than the reply, because the
     * graph is meant to be a picture of what they brought, not of what the model
     * said back. It is pure local text matching, so it costs nothing and works
     * with no key and no connection.
     */
    suspend fun analyzeMessage(sessionId: String, text: String) {
        ensureLoaded()
        val extraction = MessageAnalyzer.analyze(text)
        if (extraction.isEmpty) return

        val idsByLabel = mutableMapOf<String, String>()
        extraction.nodes.forEach { spec ->
            val isNew = graph.findExact(spec.label) == null
            val id = graph.upsertNode(spec.label, spec.kind)
            idsByLabel[spec.label.lowercase()] = id
            graph.nodeById(id)?.let { node ->
                runCatching { repository.saveNode(sessionId, node) }
                // Reinforcing does not change the label, so the vector would be
                // identical; embedding is model inference, so only pay it once.
                if (isNew) embed(id, node.label)
            }
        }
        extraction.edges.forEach { spec ->
            val sourceId = idsByLabel[spec.sourceLabel.lowercase()] ?: return@forEach
            val targetId = idsByLabel[spec.targetLabel.lowercase()] ?: return@forEach
            val edgeId = graph.upsertEdge(sourceId, targetId, spec.relation) ?: return@forEach
            graph.allEdges().firstOrNull { it.id == edgeId }?.let { edge ->
                runCatching { repository.saveEdge(sessionId, edge) }
            }
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
