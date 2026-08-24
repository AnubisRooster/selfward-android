package com.theraipist.core.narrative

/**
 * The single, evolving account of the person's inner life.
 *
 * There is at most one of these. Each build rewrites [content] in place rather
 * than appending a chapter, so the result reads as one story rather than a pile
 * of per-session fragments — the same "revise in place" approach iOS uses.
 */
data class NarrativeDocument(
    val content: String = "",
    val sessionCount: Int = 0,
    /** Creation time of the newest source already woven in; 0 when nothing is. */
    val sourceWatermark: Long = 0L,
    val updatedAt: Long = 0L
) {
    val isEmpty: Boolean get() = content.isBlank()
}

interface NarrativeStore {
    suspend fun load(): NarrativeDocument?
    suspend fun save(document: NarrativeDocument)
    suspend fun clear()
}

/** One piece of material the narrative can be built from. */
data class NarrativeSource(
    val createdAt: Long,
    val kind: String,
    val text: String
)

/**
 * Chooses what material a build should read, mirroring iOS `NarrativeService`.
 *
 * High-signal artifacts — notes, dreams, insights — are preferred. Only when
 * there are none does it fall back to raw conversation turns, so someone who
 * only ever chats still gets a narrative.
 */
object NarrativeSources {

    /** Caps on the fallback, matching iOS: the last 60 turns, 600 chars each. */
    const val MAX_FALLBACK_TURNS = 60
    const val MAX_FALLBACK_CHARS = 600

    fun select(
        artifacts: List<NarrativeSource>,
        conversationTurns: List<NarrativeSource>,
        watermark: Long
    ): List<NarrativeSource> {
        val freshArtifacts = artifacts.filter { it.createdAt > watermark }.sortedBy { it.createdAt }
        if (freshArtifacts.isNotEmpty()) return freshArtifacts

        return conversationTurns
            .filter { it.createdAt > watermark && it.text.isNotBlank() }
            .sortedBy { it.createdAt }
            .takeLast(MAX_FALLBACK_TURNS)
            .map { it.copy(text = it.text.truncateForPrompt()) }
    }

    private fun String.truncateForPrompt(): String {
        val trimmed = trim()
        return if (trimmed.length > MAX_FALLBACK_CHARS) {
            trimmed.take(MAX_FALLBACK_CHARS) + "…"
        } else {
            trimmed
        }
    }
}

/** Builds the prompt pair for a narrative run. */
object NarrativePrompt {

    fun system(personaName: String): String =
        "You are narrating the story of a person's inner life as understood by $personaName. " +
            "Your tone is warm, thoughtful, and literary — like a compassionate biographer who sees " +
            "the patterns and growth in this person's journey. Write about real feelings and real " +
            "growth. Never interpret beyond the evidence, but find the threads that connect events " +
            "into a meaningful arc. Use Markdown section headings (## heading) to organise the " +
            "narrative. Keep the language clear and accessible, not clinical. " +
            "Refer to the person as \"they\" — never by name."

    fun user(existing: String, sources: List<NarrativeSource>): String {
        val sourceText = sources.joinToString("\n\n") { "[${it.kind}] ${it.text}" }
        return if (existing.isBlank()) {
            """
            Here is a collection of insights, memories, and conversations from this person's
            sessions. Weave them into a single cohesive narrative that reads like a compassionate
            biographer's account of their inner life. Use the third person and past tense.
            Organise with light Markdown section headings (## heading) where natural.
            Be warm, specific, and true to the material — avoid generalities.

            Sources:
            $sourceText
            """.trimIndent()
        } else {
            """
            Below is the person's existing life narrative, followed by new material from
            recent sessions that has not yet been incorporated.

            Rewrite the narrative as ONE cohesive, comprehensive story that seamlessly
            integrates the new material with everything that came before. Preserve all
            existing detail and voice. Use the third person, past tense, and light Markdown
            section headings (## heading) where natural. The result should read as a single,
            unified account — not a concatenation.

            ## Existing narrative

            $existing

            ## New material to integrate

            $sourceText
            """.trimIndent()
        }
    }
}
