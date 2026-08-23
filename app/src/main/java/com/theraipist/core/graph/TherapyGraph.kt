package com.theraipist.core.graph

import java.util.concurrent.atomic.AtomicLong

data class GraphNode(
    val id: String,
    val label: String,
    val kind: String?,
    val createdAt: Long = System.currentTimeMillis()
)
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

    private val nodes = LinkedHashMap<String, GraphNode>()
    private val edges = LinkedHashMap<String, GraphEdge>()
    private val counter = AtomicLong(0)

    fun addNode(label: String, kind: String? = null): String {
        val id = "n_${counter.incrementAndGet()}"
        nodes[id] = GraphNode(id, label, kind)
        return id
    }

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
