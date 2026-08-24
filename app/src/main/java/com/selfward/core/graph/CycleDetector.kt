package com.selfward.core.graph

/**
 * Finds loops in the client's graph — a run of feelings and beliefs that leads
 * back to where it started. These are what the app surfaces as "repeating
 * patterns", so a loop counted twice would overstate how much is repeating.
 */
object CycleDetector {

    /**
     * Cap on loop length. Emotion-to-emotion edges make the graph dense, and a
     * twelve-step loop is not a pattern anyone can recognise in themselves
     * anyway — the legible ones are short.
     */
    private const val MAX_LENGTH = 6

    /** How many loops are worth showing. The screen lists them; it is not a report. */
    private const val MAX_CYCLES = 12

    /**
     * Hard ceiling on edges followed. Enumerating every loop is exponential in a
     * densely connected graph — forty mutually-linked feelings admit more paths
     * than could ever be walked — so the search stops rather than hanging the
     * Insights tab on someone with a long history.
     */
    private const val STEP_BUDGET = 200_000

    /**
     * @param cycles distinct loops, each as node ids in order, without the
     *   repeated closing node.
     * @param truncated true when the search hit a limit, so more loops exist
     *   than are listed. Callers that state a count must say "or more".
     */
    data class Cycles(val cycles: List<List<String>>, val truncated: Boolean) {
        val size: Int get() = cycles.size
        val isEmpty: Boolean get() = cycles.isEmpty()
    }

    fun detect(nodes: List<GraphNode>, edges: List<GraphEdge>): Cycles {
        val order = nodes.map { it.id }
        val rank = order.withIndex().associate { (i, id) -> id to i }
        val outgoing = edges
            .filter { rank.containsKey(it.sourceId) && rank.containsKey(it.targetId) }
            .groupBy { it.sourceId }

        val found = mutableListOf<List<String>>()
        var steps = 0
        var truncated = false

        /**
         * Walks forward from [current], only through nodes ranked at or after
         * [startRank]. Restricting to nodes that come after the start is what
         * makes each loop turn up exactly once: A→B→C, B→C→A and C→A→B are one
         * pattern, and only the rotation beginning at the lowest-ranked member
         * survives the restriction.
         */
        fun walk(start: String, startRank: Int, current: String, path: MutableList<String>) {
            if (found.size >= MAX_CYCLES || steps >= STEP_BUDGET) return
            for (edge in outgoing[current].orEmpty()) {
                if (steps >= STEP_BUDGET) {
                    truncated = true
                    return
                }
                steps++
                val next = edge.targetId
                if (next == start) {
                    found.add(path.toList())
                    if (found.size >= MAX_CYCLES) {
                        truncated = true
                        return
                    }
                    continue
                }
                val nextRank = rank[next] ?: continue
                if (nextRank < startRank || next in path) continue
                if (path.size >= MAX_LENGTH) continue
                path.add(next)
                walk(start, startRank, next, path)
                path.removeAt(path.size - 1)
                if (found.size >= MAX_CYCLES || steps >= STEP_BUDGET) return
            }
        }

        for ((index, id) in order.withIndex()) {
            if (found.size >= MAX_CYCLES || steps >= STEP_BUDGET) {
                truncated = true
                break
            }
            walk(id, index, id, mutableListOf(id))
        }

        return Cycles(found, truncated)
    }

    /** Renders a loop as "Mother → Anxious → Worthless → Mother" for display. */
    fun format(cycle: List<String>, nodes: List<GraphNode>): String {
        val labels = nodes.associate { it.id to it.label }
        val named = cycle.mapNotNull { labels[it] }
        if (named.isEmpty()) return ""
        return (named + named.first()).joinToString(" → ")
    }
}
