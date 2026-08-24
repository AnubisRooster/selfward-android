package com.selfward.core.graph

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TherapyGraphUpsertTest {

    @Test
    fun raisingTheSameSubjectTwiceReinforcesOneNode() {
        val graph = TherapyGraph()

        val first = graph.upsertNode("Mother", "person")
        val second = graph.upsertNode("Mother", "person")

        assertEquals("expected one Mother, not two", first, second)
        assertEquals(1, graph.allNodes().size)
        assertEquals(1.5f, graph.nodeById(first)!!.strength, 0.001f)
    }

    @Test
    fun labelsMatchRegardlessOfCase() {
        val graph = TherapyGraph()

        val first = graph.upsertNode("Mother", "person")
        val second = graph.upsertNode("mother", "person")

        assertEquals(first, second)
    }

    /** Strength is a display weight, not a counter; it must not grow forever. */
    @Test
    fun strengthStopsAtItsCeiling() {
        val graph = TherapyGraph()
        val id = graph.upsertNode("Anxious", "emotion")
        repeat(20) { graph.upsertNode("Anxious", "emotion") }

        assertEquals(GraphNode.MAX_STRENGTH, graph.nodeById(id)!!.strength, 0.001f)
    }

    @Test
    fun aNewNodeStartsAtTheBaseStrength() {
        val graph = TherapyGraph()
        val id = graph.upsertNode("Sad", "emotion")

        assertEquals(GraphNode.BASE_STRENGTH, graph.nodeById(id)!!.strength, 0.001f)
    }

    @Test
    fun theSameRelationshipTwiceReinforcesOneEdge() {
        val graph = TherapyGraph()
        val a = graph.upsertNode("Mother", "person")
        val b = graph.upsertNode("Anxious", "emotion")

        val first = graph.upsertEdge(a, b, "TRIGGERS")
        val second = graph.upsertEdge(a, b, "TRIGGERS")

        assertEquals(first, second)
        assertEquals(1, graph.allEdges().size)
        assertEquals(1.5f, graph.allEdges().single().weight!!, 0.001f)
    }

    @Test
    fun differentRelationshipsBetweenTheSamePairAreSeparateEdges() {
        val graph = TherapyGraph()
        val a = graph.upsertNode("Anxious", "emotion")
        val b = graph.upsertNode("Sad", "emotion")

        graph.upsertEdge(a, b, "CAUSES")
        graph.upsertEdge(a, b, "ASSOCIATED_WITH")

        assertEquals(2, graph.allEdges().size)
    }

    /**
     * Extraction proposes edges by label, so an endpoint that produced no node
     * must be skipped rather than throwing the way [TherapyGraph.addEdge] does.
     */
    @Test
    fun anEdgeToAnUnknownNodeIsRefusedQuietly() {
        val graph = TherapyGraph()
        val a = graph.upsertNode("Mother", "person")

        assertNull(graph.upsertEdge(a, "n_nope", "TRIGGERS"))
        assertNull(graph.upsertEdge("n_nope", a, "TRIGGERS"))
        assertTrue(graph.allEdges().isEmpty())
    }

    @Test
    fun aNodeIsNotLinkedToItself() {
        val graph = TherapyGraph()
        val a = graph.upsertNode("Anxious", "emotion")

        assertNull(graph.upsertEdge(a, a, "ASSOCIATED_WITH"))
    }

    @Test
    fun nodesCanBeFoundByKind() {
        val graph = TherapyGraph()
        graph.upsertNode("Mother", "person")
        graph.upsertNode("Anxious", "emotion")

        assertEquals(listOf("Mother"), graph.nodesOfKind("person").map { it.label })
    }

    @Test
    fun outgoingEdgesExcludeIncomingOnes() {
        val graph = TherapyGraph()
        val a = graph.upsertNode("A", "emotion")
        val b = graph.upsertNode("B", "emotion")
        graph.upsertEdge(a, b, "CAUSES")

        assertEquals(1, graph.outgoingEdges(a).size)
        assertTrue(graph.outgoingEdges(b).isEmpty())
    }

    /** Reinforcement must survive a restart, so restored strength is preserved. */
    @Test
    fun restoredNodesKeepTheirStrengthAndCanBeReinforcedFurther() {
        val graph = TherapyGraph()
        graph.restore(listOf(GraphNode("n_7", "Mother", "person", 0L, 1.5f)), emptyList())

        assertEquals(1.5f, graph.findExact("Mother")!!.strength, 0.001f)

        val id = graph.upsertNode("Mother", "person")

        assertEquals("n_7", id)
        assertEquals(2.0f, graph.nodeById("n_7")!!.strength, 0.001f)
    }

    @Test
    fun findExactDoesNotMatchOnSubstrings() {
        val graph = TherapyGraph()
        graph.upsertNode("Mother", "person")

        assertNotNull(graph.findExact("mother"))
        assertNull(graph.findExact("moth"))
    }
}
