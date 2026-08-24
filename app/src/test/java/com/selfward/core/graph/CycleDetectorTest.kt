package com.selfward.core.graph

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CycleDetectorTest {

    private fun node(id: String) = GraphNode(id, id.uppercase(), "emotion", 0L)
    private fun edge(from: String, to: String) = GraphEdge("$from$to", from, to, "ASSOCIATED_WITH", 1f)

    @Test
    fun aChainHasNoLoop() {
        val nodes = listOf(node("a"), node("b"), node("c"))
        val edges = listOf(edge("a", "b"), edge("b", "c"))
        assertTrue(CycleDetector.detect(nodes, edges).isEmpty)
    }

    @Test
    fun findsASimpleLoop() {
        val nodes = listOf(node("a"), node("b"), node("c"))
        val edges = listOf(edge("a", "b"), edge("b", "c"), edge("c", "a"))

        val cycles = CycleDetector.detect(nodes, edges).cycles

        assertEquals(1, cycles.size)
        assertEquals(setOf("a", "b", "c"), cycles.single().toSet())
    }

    /**
     * The same loop is reachable from each of its members, and each start gives
     * a different rotation. Counting those separately would tell the client they
     * have three repeating patterns when they have one.
     */
    @Test
    fun oneLoopIsReportedOnceNotOncePerStartingPoint() {
        val nodes = listOf(node("a"), node("b"), node("c"))
        val edges = listOf(edge("a", "b"), edge("b", "c"), edge("c", "a"))

        assertEquals(1, CycleDetector.detect(nodes, edges).size)
    }

    @Test
    fun twoSeparateLoopsAreBothFound() {
        val nodes = listOf(node("a"), node("b"), node("x"), node("y"))
        val edges = listOf(edge("a", "b"), edge("b", "a"), edge("x", "y"), edge("y", "x"))

        assertEquals(2, CycleDetector.detect(nodes, edges).size)
    }

    @Test
    fun aPairPointingAtEachOtherIsALoop() {
        val nodes = listOf(node("a"), node("b"))
        val edges = listOf(edge("a", "b"), edge("b", "a"))

        assertEquals(setOf("a", "b"), CycleDetector.detect(nodes, edges).cycles.single().toSet())
    }

    /** Edges left behind by a deleted node must not crash the walk. */
    @Test
    fun edgesPointingAtMissingNodesAreIgnored() {
        val nodes = listOf(node("a"))
        val edges = listOf(edge("a", "gone"), edge("gone", "a"))

        assertTrue(CycleDetector.detect(nodes, edges).isEmpty)
    }

    /**
     * A dense graph must not be walked exhaustively — a fully connected set of
     * 40 nodes admits astronomically many paths, and enumerating them exhausted
     * the heap before the search was given a budget.
     */
    @Test
    fun aDenselyConnectedGraphTerminates() {
        val ids = (1..40).map { "n$it" }
        val nodes = ids.map { node(it) }
        val edges = ids.flatMap { a -> ids.filter { it != a }.map { b -> edge(a, b) } }

        val result = CycleDetector.detect(nodes, edges)

        assertTrue("expected to find loops, found none", !result.isEmpty)
        assertTrue("a capped search must admit it was capped", result.truncated)
    }

    /** An exhaustive search must not claim it was cut short. */
    @Test
    fun aSmallGraphIsNotReportedAsTruncated() {
        val nodes = listOf(node("a"), node("b"), node("c"))
        val edges = listOf(edge("a", "b"), edge("b", "c"), edge("c", "a"))

        assertFalse(CycleDetector.detect(nodes, edges).truncated)
    }

    @Test
    fun loopsAreCappedSoTheScreenIsNotFlooded() {
        val ids = (1..12).map { "n$it" }
        val nodes = ids.map { node(it) }
        val edges = ids.flatMap { a -> ids.filter { it != a }.map { b -> edge(a, b) } }

        val result = CycleDetector.detect(nodes, edges)

        assertTrue("got ${result.size}", result.size <= 12)
        assertTrue(result.truncated)
    }

    /** No loop may run longer than the display cap. */
    @Test
    fun reportedLoopsStayShort() {
        val ids = (1..20).map { "n$it" }
        val nodes = ids.map { node(it) }
        val ring = ids.indices.map { edge(ids[it], ids[(it + 1) % ids.size]) }

        val result = CycleDetector.detect(nodes, ring)

        assertTrue("a 20-node ring is longer than the cap", result.isEmpty)
    }

    @Test
    fun formattingClosesTheLoopBackToItsStart() {
        val nodes = listOf(node("a"), node("b"))
        assertEquals("A → B → A", CycleDetector.format(listOf("a", "b"), nodes))
    }

    @Test
    fun formattingAnUnknownCycleYieldsNothingRatherThanArrows() {
        assertEquals("", CycleDetector.format(listOf("gone"), listOf(node("a"))))
    }
}
