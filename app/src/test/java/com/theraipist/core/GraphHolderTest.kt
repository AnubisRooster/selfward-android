package com.theraipist.core

import com.theraipist.core.embedding.EmbeddingModelDownloader
import com.theraipist.core.embedding.EmbeddingModelSpec
import com.theraipist.core.embedding.EmbeddingProvider
import com.theraipist.core.embedding.EmbeddingProviderFactory
import com.theraipist.core.embedding.MemoryVectorStore
import com.theraipist.core.graph.GraphEdge
import com.theraipist.core.graph.GraphNode
import com.theraipist.core.local.DownloadProgress
import com.theraipist.core.local.DownloadStatus
import com.theraipist.core.repository.GraphRepository
import com.theraipist.core.repository.GraphSnapshot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GraphHolderTest {

    private class RecordingGraphRepository(
        private val initial: GraphSnapshot = GraphSnapshot(emptyList(), emptyList())
    ) : GraphRepository {
        val savedNodes = mutableListOf<Pair<String, GraphNode>>()
        val savedEdges = mutableListOf<Pair<String, GraphEdge>>()
        val savedInsights = mutableListOf<Pair<String, String>>()
        var loadAllCallCount = 0

        override suspend fun saveNode(sessionId: String, node: GraphNode) { savedNodes += sessionId to node }
        override suspend fun saveEdge(sessionId: String, edge: GraphEdge) { savedEdges += sessionId to edge }
        override suspend fun saveInsight(sessionId: String, text: String) { savedInsights += sessionId to text }
        override suspend fun loadAll(): GraphSnapshot {
            loadAllCallCount++
            return initial
        }
    }

    private class FakeEmbeddingModelDownloader(private val downloaded: Boolean) : EmbeddingModelDownloader {
        override fun status(model: EmbeddingModelSpec) =
            if (downloaded) DownloadStatus.DOWNLOADED else DownloadStatus.NOT_DOWNLOADED
        override fun progress(model: EmbeddingModelSpec): DownloadProgress? = null
        override fun onnxFile(model: EmbeddingModelSpec) = java.io.File("/fake/model.onnx")
        override fun vocabFile(model: EmbeddingModelSpec) = java.io.File("/fake/vocab.txt")
        override fun startDownload(model: EmbeddingModelSpec) {}
        override fun cancelDownload(model: EmbeddingModelSpec) {}
        override fun deleteDownload(model: EmbeddingModelSpec) {}
        override suspend fun awaitCompletion(model: EmbeddingModelSpec) =
            if (downloaded) DownloadStatus.DOWNLOADED else DownloadStatus.FAILED
    }

    /** Deterministic "embedding": one-hot on a fixed vocabulary index for the text, so exact matches score highest. */
    private class FakeEmbeddingProvider(private val vocabulary: List<String>) : EmbeddingProvider {
        override suspend fun embed(text: String): FloatArray {
            val vector = FloatArray(vocabulary.size)
            val index = vocabulary.indexOf(text)
            if (index >= 0) vector[index] = 1f
            return vector
        }
    }

    private fun buildHolder(
        repository: GraphRepository = RecordingGraphRepository(),
        downloaded: Boolean = false,
        vocabulary: List<String> = emptyList()
    ): GraphHolder = GraphHolder(
        repository,
        FakeEmbeddingModelDownloader(downloaded),
        EmbeddingProviderFactory { _, _ -> FakeEmbeddingProvider(vocabulary) },
        MemoryVectorStore()
    )

    @Test
    fun addInsightsPersistsNodesEdgesAndInsights() = runTest {
        val repo = RecordingGraphRepository()
        val holder = buildHolder(repository = repo)

        holder.addInsights("s1", listOf("felt calmer", "named the fear"))

        assertEquals(2, holder.nodes.value.size)
        assertEquals(1, holder.edges.value.size)
        assertEquals(2, repo.savedNodes.size)
        assertEquals(1, repo.savedEdges.size)
        assertEquals(listOf("s1" to "felt calmer", "s1" to "named the fear"), repo.savedInsights)
    }

    @Test
    fun ensureLoadedRestoresPriorSessionAndContinuesTheChain() = runTest {
        val restoredNode = GraphNode(id = "n_1", label = "Mother", kind = "insight", createdAt = 100)
        val repo = RecordingGraphRepository(initial = GraphSnapshot(listOf(restoredNode), emptyList()))
        val holder = buildHolder(repository = repo)

        holder.addInsights("s2", listOf("new insight"))

        assertEquals(2, holder.nodes.value.size)
        val newEdge = holder.edges.value.single()
        assertEquals("n_1", newEdge.sourceId)
    }

    @Test
    fun ensureLoadedOnlyQueriesTheRepositoryOnce() = runTest {
        val repo = RecordingGraphRepository()
        val holder = buildHolder(repository = repo)

        holder.addInsights("s1", listOf("a"))
        holder.addInsights("s1", listOf("b"))
        holder.ensureLoaded()

        assertEquals(1, repo.loadAllCallCount)
        assertTrue(holder.nodes.value.map { it.label }.containsAll(listOf("a", "b")))
    }

    @Test
    fun findSimilarReturnsEmptyWhenEmbeddingModelNotDownloaded() = runTest {
        val holder = buildHolder(downloaded = false)
        holder.addInsights("s1", listOf("felt anxious about work"))

        val result = holder.findSimilar("anxious")

        assertTrue(result.isEmpty())
    }

    @Test
    fun findSimilarReturnsTheMatchingNodeOnceEmbeddingModelIsDownloaded() = runTest {
        val vocabulary = listOf("felt anxious about work", "had a great day", "anxious")
        val holder = buildHolder(downloaded = true, vocabulary = vocabulary)
        holder.addInsights("s1", listOf("felt anxious about work", "had a great day"))

        val result = holder.findSimilar("felt anxious about work", k = 1)

        assertEquals(1, result.size)
        assertEquals("felt anxious about work", result.first().label)
    }
}
