package com.selfward.core.catalog

import com.selfward.core.chat.Provider

/**
 * One model a provider will actually serve, as offered to the client.
 *
 * @param detail the line under the name: what is known about cost and size.
 * @param isFree true only when the provider says the model costs nothing.
 */
data class ModelChoice(
    val id: String,
    val name: String,
    val detail: String,
    val isFree: Boolean
)

/**
 * Cheapest-first ordering for the providers that do not publish prices.
 *
 * OpenRouter returns a price per model, so its list is ranked on fact. OpenAI
 * and Anthropic do not: their model endpoints return an id and a name and
 * nothing else, so the only thing to rank on is the naming each vendor uses for
 * its own tiers — "mini" and "nano" below the full-size models, "haiku" below
 * "sonnet" below "opus".
 *
 * That is a reading of vendor naming, not a price lookup, and it is labelled as
 * such wherever the list is shown. It will need revisiting when either vendor
 * names a tier differently.
 */
object PriceTiers {

    /** Lower sorts first. */
    private val OPENAI_TIERS = listOf("nano", "mini", "turbo")
    private val ANTHROPIC_TIERS = listOf("haiku", "sonnet", "opus")

    /**
     * Models that are not conversations: transcription, speech, images,
     * embeddings, moderation. OpenAI's list carries all of them beside the chat
     * models, and any of them chosen here would fail on the first message.
     */
    private val NOT_A_CHAT_MODEL = listOf(
        "whisper", "tts", "dall-e", "embedding", "moderation",
        "realtime", "audio", "image", "transcribe", "search", "codex"
    )

    fun isChatModel(provider: Provider, id: String): Boolean {
        val lower = id.lowercase()
        if (NOT_A_CHAT_MODEL.any { lower.contains(it) }) return false
        return when (provider) {
            Provider.OPENAI -> lower.startsWith("gpt") || lower.startsWith("o1") ||
                lower.startsWith("o3") || lower.startsWith("o4")
            Provider.ANTHROPIC -> lower.startsWith("claude")
            Provider.OPENROUTER -> true
        }
    }

    /**
     * Rank within the vendor's own naming. Unrecognised names sort after the
     * known tiers rather than being hidden — a tier this does not know about is
     * still a model the client may want.
     */
    fun rank(provider: Provider, id: String): Int {
        val lower = id.lowercase()
        val tiers = when (provider) {
            Provider.OPENAI -> OPENAI_TIERS
            Provider.ANTHROPIC -> ANTHROPIC_TIERS
            Provider.OPENROUTER -> return 0
        }
        val index = tiers.indexOfFirst { lower.contains(it) }
        return if (index >= 0) index else tiers.size
    }

    /** What to say under the name when no price is published. */
    fun detailFor(provider: Provider, id: String): String = when (rank(provider, id)) {
        0 -> "smallest tier"
        1 -> "mid tier"
        else -> "larger tier"
    }

    /** The heading above the list, which must not claim more than is known. */
    fun headingFor(provider: Provider, count: Int): String = when (provider) {
        Provider.OPENROUTER -> "$count free models, best first"
        else -> "$count models, cheapest tier first"
    }
}
