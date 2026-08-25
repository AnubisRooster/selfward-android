package com.selfward.core.catalog

/**
 * One entry from OpenRouter's model catalogue.
 *
 * Prices are the per-token strings the API returns ("0", "0.0000006"). They are
 * kept as strings because that is what arrives, and compared numerically rather
 * than by text: iOS tests `prompt == "0"`, which would call a model priced
 * "0.0" paid, and would call one priced "0" free even if the field were
 * malformed. Money is the wrong place to be approximate in either direction.
 */
data class OpenRouterModel(
    val id: String,
    val name: String,
    val promptPrice: String,
    val completionPrice: String,
    val contextLength: Int,
    /** From architecture.input_modalities, e.g. ["text", "image"]. */
    val inputModalities: List<String> = listOf("text"),
    /** From architecture.output_modalities. */
    val outputModalities: List<String> = listOf("text"),
    /**
     * Artificial Analysis' intelligence index, when the catalogue carries one.
     *
     * The closest thing to a quality signal OpenRouter publishes, and far better
     * than context length for choosing a model to hold a conversation with:
     * ranking on context put a 1M-token content-safety classifier above a
     * capable 256k chat model.
     */
    val intelligenceIndex: Double? = null
) {
    /**
     * True only when both prices parse cleanly to zero.
     *
     * Anything unparseable counts as paid. Showing a paid model under a "free"
     * heading spends the client's money without asking; hiding a free one costs
     * them nothing but a scroll.
     */
    val isFree: Boolean
        get() = promptPrice.parsesToZero() && completionPrice.parsesToZero()

    /** The part after the slash, for compact display: "llama-3.2-3b-instruct:free". */
    val shortName: String get() = id.substringAfterLast('/')

    /** The vendor prefix, for grouping: "meta-llama". */
    val vendor: String get() = id.substringBefore('/', missingDelimiterValue = "")

    /**
     * Whether this model can hold a text conversation.
     *
     * Free and roomy is not the same as usable. OpenRouter's catalogue prices
     * Google's Lyria music models at zero with a million-token context, so
     * ranking free models on context alone put a music generator at the top of
     * the list and would have made it the default for a therapy app. Their
     * output modality is "text+audio"; a model that can actually reply in a
     * conversation emits text and nothing else.
     */
    val isTextChat: Boolean
        get() = outputModalities == listOf("text") && inputModalities.contains("text")

    /**
     * OpenRouter's cloaked models, published under "stealth" while a provider
     * evaluates them.
     *
     * They are free because prompts are logged and shared with that provider.
     * That is an ordinary trade for a coding tool and the wrong one here, so
     * they are kept out of the offered list — this app's whole proposition is
     * that what gets written stays where the client put it.
     */
    val isCloaked: Boolean get() = vendor.equals("stealth", ignoreCase = true)

    /**
     * Models published for a job that is not conversation.
     *
     * The free list carries a content-safety classifier, code-specialised
     * models, and a routing pseudo-model. They will all answer a request, and
     * none of them should be sitting across from someone describing a hard
     * week. Matched on the name because that is where the purpose is stated.
     */
    val isUnsuitedToConversation: Boolean
        get() {
            val haystack = "$id $name".lowercase()
            return NON_CONVERSATIONAL.any { haystack.contains(it) }
        }

    private companion object {
        val NON_CONVERSATIONAL = listOf(
            "content-safety", "guard", "moderation", "classifier",
            "embed", "rerank", "-code", "coder"
        )
    }

    private fun String.parsesToZero(): Boolean {
        val value = trim().toDoubleOrNull() ?: return false
        return value == 0.0
    }
}

/**
 * Ranking and selection over a fetched catalogue. Pure, so the choice of "best"
 * can be tested without a network.
 */
object ModelRanking {

    /**
     * The model to use when nothing has been chosen and the catalogue cannot be
     * reached. Free, small, and widely available — a starting point rather than
     * a recommendation, and the reason the catalogue is fetched at all is so
     * this is rarely what gets used. Free models are deprecated regularly, so
     * expect this to rot and treat a failure against it as "refresh the list",
     * not "the app is broken".
     */
    const val PINNED_FREE_FALLBACK = "meta-llama/llama-3.2-3b-instruct:free"

    /**
     * Free models, best first.
     *
     * Only models that can hold a text conversation, and never a cloaked one.
     *
     * Among those, "best" is context length: the only quality signal the
     * catalogue carries beyond the name. It is a crude proxy — a large context
     * does not make a model good — but it is the honest one available.
     */
    /**
     * Best first, by measured intelligence.
     *
     * Models the catalogue has never benchmarked sort last rather than being
     * dropped: unmeasured is not the same as bad, and on the current free list
     * it is mostly what the previews and the oddities have in common. Context
     * length breaks ties, because between two equally capable models the roomier
     * one holds more of the conversation.
     */
    private val BEST_FIRST = compareByDescending<OpenRouterModel> { it.intelligenceIndex ?: -1.0 }
        .thenByDescending { it.contextLength }
        .thenBy { it.id }

    private fun usable(model: OpenRouterModel, excluded: Set<String>) =
        model.isTextChat &&
            !model.isCloaked &&
            !model.isUnsuitedToConversation &&
            model.id !in excluded

    @JvmOverloads
    fun freeModels(
        all: List<OpenRouterModel>,
        excluded: Set<String> = emptySet()
    ): List<OpenRouterModel> =
        all.filter { it.isFree && usable(it, excluded) }.sortedWith(BEST_FIRST)

    @JvmOverloads
    fun paidModels(
        all: List<OpenRouterModel>,
        excluded: Set<String> = emptySet()
    ): List<OpenRouterModel> =
        all.filter { !it.isFree && usable(it, excluded) }.sortedWith(BEST_FIRST)

    /** Free first, then by context length, matching how the picker lists them. */
    fun ranked(all: List<OpenRouterModel>): List<OpenRouterModel> =
        freeModels(all) + paidModels(all)

    /**
     * The best free model in [all], or null when the catalogue holds none.
     * Callers fall back to [PINNED_FREE_FALLBACK] rather than silently
     * selecting something paid.
     */
    @JvmOverloads
    fun bestFree(all: List<OpenRouterModel>, excluded: Set<String> = emptySet()): OpenRouterModel? =
        freeModels(all, excluded).firstOrNull()

    /** The id to preselect for OpenRouter: the best free model, else the pin. */
    @JvmOverloads
    fun defaultFreeId(all: List<OpenRouterModel>, excluded: Set<String> = emptySet()): String =
        bestFree(all, excluded)?.id ?: PINNED_FREE_FALLBACK

    /**
     * The next free model to try after [afterId] failed.
     *
     * Nothing in the catalogue says a model is gated to particular apps — no
     * field, and the per-model endpoints API does not say either. It is only
     * discoverable by asking and being refused, so the app has to learn it and
     * move on rather than leaving someone staring at an error.
     */
    fun nextFreeAfter(
        all: List<OpenRouterModel>,
        afterId: String,
        excluded: Set<String> = emptySet()
    ): OpenRouterModel? = freeModels(all, excluded + afterId).firstOrNull()
}
