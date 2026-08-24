package com.selfward.core.graph

/**
 * Extracts compact reflective insights from a therapist response so they can be
 * stored in the client's memory graph. Lines beginning with a known marker
 * (Insight:, Reflection:, Takeaway:, Learning:) are captured.
 */
object InsightExtractor {

    private val MARKERS = listOf("insight:", "reflection:", "takeaway:", "learning:")

    fun extract(text: String): List<String> {
        return text.lines().map { it.trim() }.mapNotNull { line ->
            val marker = MARKERS.firstOrNull { line.startsWith(it, ignoreCase = true) }
                ?: return@mapNotNull null
            val content = line.substring(marker.length).trim().removePrefix(":").trim()
            content.takeIf { it.isNotEmpty() }
        }
    }
}
