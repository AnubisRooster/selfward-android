package com.theraipist.core

import com.theraipist.core.graph.GraphEdge
import com.theraipist.core.graph.GraphNode
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

    @Test
    fun addInsightsPersistsNodesEdgesAndInsights() = runTest {
        val repo = RecordingGraphRepository()
        val holder = GraphHolder(repo)

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
        val holder = GraphHolder(repo)

        holder.addInsights("s2", listOf("new insight"))

        assertEquals(2, holder.nodes.value.size)
        val newEdge = holder.edges.value.single()
        assertEquals("n_1", newEdge.sourceId)
    }

    @Test
    fun ensureLoadedOnlyQueriesTheRepositoryOnce() = runTest {
        val repo = RecordingGraphRepository()
        val holder = GraphHolder(repo)

        holder.addInsights("s1", listOf("a"))
        holder.addInsights("s1", listOf("b"))
        holder.ensureLoaded()

        assertEquals(1, repo.loadAllCallCount)
        assertTrue(holder.nodes.value.map { it.label }.containsAll(listOf("a", "b")))
    }
}
