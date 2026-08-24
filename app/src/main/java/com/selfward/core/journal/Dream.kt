package com.selfward.core.journal

data class Dream(
    val id: String,
    val sessionId: String,
    val narrative: String,
    val feelings: List<String>,
    val symbols: List<String>,
    /** Jungian reading of the dream, empty until one has been generated. */
    val analysis: String,
    val createdAt: Long
)

interface DreamRepository {
    suspend fun record(
        sessionId: String,
        narrative: String,
        feelings: List<String>,
        symbols: List<String>
    ): Dream

    suspend fun setAnalysis(dreamId: String, analysis: String)
    suspend fun listForSession(sessionId: String): List<Dream>
    suspend fun listAll(): List<Dream>
    suspend fun delete(dreamId: String)
}

/**
 * Pulls recurring dream images out of a narrative by matching a fixed vocabulary,
 * mirroring the iOS `DreamService.extractSymbols`.
 *
 * This is deliberately a keyword match rather than anything cleverer: it runs
 * with no model and no network, so symbols are available even offline and before
 * any analysis has been generated.
 */
object DreamSymbols {

    /** The same vocabulary the iOS app matches against. */
    val VOCABULARY = listOf(
        "water", "house", "forest", "animal", "flight", "falling", "chase",
        "death", "birth", "marriage", "child", "snake", "bird", "fire",
        "mountain", "ocean", "door", "window", "bridge", "shadow", "light"
    )

    fun extract(narrative: String): List<String> {
        val lower = narrative.lowercase()
        return VOCABULARY.filter { lower.contains(it) }
    }
}
