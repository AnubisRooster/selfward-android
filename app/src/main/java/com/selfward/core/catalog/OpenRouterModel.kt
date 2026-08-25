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
    val outputModalities: List<String> = listOf("text")
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
    fun freeModels(all: List<OpenRouterModel>): List<OpenRouterModel> =
        all.filter { it.isFree && it.isTextChat && !it.isCloaked }.sortedWith(
            compareByDescending<OpenRouterModel> { it.contextLength }.thenBy { it.id }
        )

    fun paidModels(all: List<OpenRouterModel>): List<OpenRouterModel> =
        all.filter { !it.isFree && it.isTextChat && !it.isCloaked }.sortedWith(
            compareByDescending<OpenRouterModel> { it.contextLength }.thenBy { it.id }
        )

    /** Free first, then by context length, matching how the picker lists them. */
    fun ranked(all: List<OpenRouterModel>): List<OpenRouterModel> =
        freeModels(all) + paidModels(all)

    /**
     * The best free model in [all], or null when the catalogue holds none.
     * Callers fall back to [PINNED_FREE_FALLBACK] rather than silently
     * selecting something paid.
     */
    fun bestFree(all: List<OpenRouterModel>): OpenRouterModel? = freeModels(all).firstOrNull()

    /** The id to preselect for OpenRouter: the best free model, else the pin. */
    fun defaultFreeId(all: List<OpenRouterModel>): String =
        bestFree(all)?.id ?: PINNED_FREE_FALLBACK
}
