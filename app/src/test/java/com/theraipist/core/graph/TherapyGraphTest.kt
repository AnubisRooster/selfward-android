package com.theraipist.core.graph

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TherapyGraphTest {

    @Test
    fun addAndConnectNodes() {
        val g = TherapyGraph()
        val a = g.addNode("Anxiety", "emotion")
        val b = g.addNode("Work", "context")
        g.addEdge(a, b, "triggered by")
        assertEquals(2, g.allNodes().size)
        assertEquals(1, g.edgesOf(a).size)
        assertEquals(listOf(b), g.neighbors(a).map { it.id })
    }

    @Test
    fun findByLabelCaseInsensitive() {
        val g = TherapyGraph()
        g.addNode("Mother", "person")
        g.addNode("Father", "person")
        g.addNode("Job", "context")
        assertEquals(2, g.findByLabel("ther").size)
    }

    @Test
    fun rejectsEdgeBetweenUnknownNodes() {
        val g = TherapyGraph()
        var threw = false
        try {
            g.addEdge("n_1", "n_2", "x")
        } catch (e: IllegalArgumentException) {
            threw = true
        }
        assertTrue("expected IllegalArgumentException for unknown nodes", threw)
    }

    @Test
    fun restoreRepopulatesWithOriginalIds() {
        val g = TherapyGraph()
        val restoredNode = GraphNode(id = "n_5", label = "Mother", kind = "person")
        val otherNode = GraphNode(id = "n_6", label = "Job", kind = "context")
        val restoredEdge = GraphEdge(id = "e_7", sourceId = "n_5", targetId = "n_6", label = "next", weight = null)

        g.restore(listOf(restoredNode, otherNode), listOf(restoredEdge))

        assertEquals(listOf(restoredNode, otherNode), g.allNodes())
        assertEquals(listOf(restoredEdge), g.allEdges())
        assertEquals(listOf("n_6"), g.neighbors("n_5").map { it.id })
    }

    @Test
    fun restoreAdvancesCounterPastRestoredIds() {
        val g = TherapyGraph()
        g.restore(listOf(GraphNode(id = "n_10", label = "Old", kind = null)), emptyList())

        val newId = g.addNode("New", null)

        assertEquals("n_11", newId)
    }
}
