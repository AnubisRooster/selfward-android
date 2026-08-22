package com.theraipist.core.embedding

import java.util.concurrent.ConcurrentHashMap

data class ScoredId(val id: String, val score: Float)

/**
 * In-memory vector index over the client's memory. Used to retrieve the most
 * salient past reflections / graph nodes for a given query embedding. The
 * backing store is swapped for Room + a real ANN index in a later phase; the
 * retrieval logic here is provider-agnostic.
 */
class MemoryVectorStore {

    private val vectors = ConcurrentHashMap<String, FloatArray>()

    fun add(id: String, vector: FloatArray) {
        vectors[id] = vector
    }

    fun remove(id: String) {
        vectors.remove(id)
    }

    fun size(): Int = vectors.size

    fun query(vector: FloatArray, k: Int = 5): List<ScoredId> {
        if (vectors.isEmpty()) return emptyList()
        return vectors.entries
            .map { ScoredId(it.key, CosineSimilarity.of(it.value, vector)) }
            .sortedByDescending { it.score }
            .take(k)
    }
}
