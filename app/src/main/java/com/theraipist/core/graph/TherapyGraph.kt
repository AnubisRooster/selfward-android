package com.theraipist.core.graph

import java.util.concurrent.atomic.AtomicLong

data class GraphNode(
    val id: String,
    val label: String,
    val kind: String?,
    val createdAt: Long = System.currentTimeMillis(),
    /** How often this has come up, from [BASE_STRENGTH] to [MAX_STRENGTH]. */
    val strength: Float = BASE_STRENGTH
) {
    companion object {
        const val BASE_STRENGTH = 1.0f
        const val MAX_STRENGTH = 2.0f
    }
}
data class GraphEdge(
    val id: String,
    val sourceId: String,
    val targetId: String,
    val label: String?,
    val weight: Float?
)

/**
 * In-memory knowledge graph of the client's inner world: people, emotions,
 * beliefs, memories and the relationships between them. Persisted separately
 * via Room graph tables.
 */
class TherapyGraph {

    private companion object {
        const val STRENGTH_STEP = 0.5f
        const val BASE_WEIGHT = 1.0f
        const val MAX_WEIGHT = 2.0f
    }

    private val nodes = LinkedHashMap<String, GraphNode>()
    private val edges = LinkedHashMap<String, GraphEdge>()
    private val counter = AtomicLong(0)

    fun addNode(label: String, kind: String? = null): String {
        val id = "n_${counter.incrementAndGet()}"
        nodes[id] = GraphNode(id, label, kind)
        return id
    }

    /**
     * Adds a node for [label], or reinforces the existing one if the client has
     * raised it before. Returns the node's id either way.
     *
     * Mentioning Mother in ten messages should leave one Mother that has grown
     * heavier, not ten identical nodes, so labels are matched case-insensitively.
     */
    fun upsertNode(label: String, kind: String?): String {
        val existing = findExact(label)
        if (existing != null) {
            nodes[existing.id] = existing.copy(
                strength = minOf(existing.strength + STRENGTH_STEP, GraphNode.MAX_STRENGTH)
            )
            return existing.id
        }
        return addNode(label, kind)
    }

    /**
     * Adds a [relation] edge between two nodes, or reinforces the existing one.
     * Returns the edge's id, or null if either endpoint is unknown.
     *
     * Unlike [addEdge] this does not throw on an unknown endpoint: extraction
     * proposes edges by label, and a label that produced no node simply has no
     * edge to draw.
     */
    fun upsertEdge(sourceId: String, targetId: String, relation: String): String? {
        if (!nodes.containsKey(sourceId) || !nodes.containsKey(targetId)) return null
        if (sourceId == targetId) return null
        val existing = edges.values.firstOrNull {
            it.sourceId == sourceId && it.targetId == targetId && it.label == relation
        }
        if (existing != null) {
            val weight = (existing.weight ?: BASE_WEIGHT) + STRENGTH_STEP
            edges[existing.id] = existing.copy(weight = minOf(weight, MAX_WEIGHT))
            return existing.id
        }
        return addEdge(sourceId, targetId, relation, BASE_WEIGHT)
    }

    fun nodeById(id: String): GraphNode? = nodes[id]

    /** The node carrying exactly this label, ignoring case. */
    fun findExact(label: String): GraphNode? =
        nodes.values.firstOrNull { it.label.equals(label, ignoreCase = true) }

    fun outgoingEdges(nodeId: String): List<GraphEdge> =
        edges.values.filter { it.sourceId == nodeId }

    fun nodesOfKind(kind: String): List<GraphNode> = nodes.values.filter { it.kind == kind }

    fun addEdge(
        sourceId: String,
        targetId: String,
        label: String? = null,
        weight: Float? = null
    ): String {
        require(nodes.containsKey(sourceId) && nodes.containsKey(targetId)) {
            "Cannot add edge between unknown nodes: $sourceId -> $targetId"
        }
        val id = "e_${counter.incrementAndGet()}"
        edges[id] = GraphEdge(id, sourceId, targetId, label, weight)
        return id
    }

    fun neighbors(nodeId: String): List<GraphNode> {
        val otherIds = edges.values
            .filter { it.sourceId == nodeId || it.targetId == nodeId }
            .map { if (it.sourceId == nodeId) it.targetId else it.sourceId }
        return otherIds.mapNotNull { nodes[it] }
    }

    fun edgesOf(nodeId: String): List<GraphEdge> =
        edges.values.filter { it.sourceId == nodeId || it.targetId == nodeId }

    fun findByLabel(substring: String): List<GraphNode> {
        val s = substring.lowercase()
        return nodes.values.filter { it.label.lowercase().contains(s) }
    }

    fun allNodes(): List<GraphNode> = nodes.values.toList()
    fun allEdges(): List<GraphEdge> = edges.values.toList()

    /**
     * Repopulates this graph from persisted [nodes]/[edges], preserving their original
     * ids, and advances the id counter past the highest restored suffix so newly added
     * nodes/edges never collide with a restored id.
     */
    fun restore(nodes: List<GraphNode>, edges: List<GraphEdge>) {
        nodes.forEach { this.nodes[it.id] = it }
        edges.forEach { this.edges[it.id] = it }
        val maxSuffix = (nodes.map { it.id } + edges.map { it.id })
            .mapNotNull { it.substringAfterLast('_').toLongOrNull() }
            .maxOrNull() ?: 0L
        counter.updateAndGet { maxOf(it, maxSuffix) }
    }
}
