package com.selfward.core.embedding

object CosineSimilarity {

    fun of(a: FloatArray, b: FloatArray): Float {
        require(a.isNotEmpty() && b.isNotEmpty()) { "vectors must be non-empty" }
        require(a.size == b.size) { "vectors must share a dimension (${a.size} vs ${b.size})" }
        var dot = 0f
        var na = 0f
        var nb = 0f
        for (i in a.indices) {
            dot += a[i] * b[i]
            na += a[i] * a[i]
            nb += b[i] * b[i]
        }
        val denom = kotlin.math.sqrt(na) * kotlin.math.sqrt(nb)
        if (denom == 0f) return 0f
        return dot / denom
    }
}
