package com.selfward.core.catalog

/**
 * Models that answered a real request by refusing to serve it.
 *
 * Nothing in OpenRouter's catalogue marks a model as gated. There is no field
 * for it, and the per-model endpoints API does not say either — a model
 * restricted to registered "agentic harnesses" looks identical to one anybody
 * can call, right down to a healthy list of providers. It is discoverable only
 * by asking and being refused.
 *
 * So the app remembers. A model that turns a request away is set aside, the next
 * best one is used instead, and the client is not left holding an error about a
 * choice the app made on their behalf.
 */
interface UnusableModels {
    fun all(): Set<String>
    fun remember(modelId: String, reason: String)
    fun forget(modelId: String)
    fun clear()

    /** Why [modelId] was set aside, for showing next to it in the list. */
    fun reasonFor(modelId: String): String?

    /** Models that answered a real request. */
    fun working(): Set<String>

    fun rememberWorking(modelId: String)
}

/**
 * Whether a provider's error means "this model will not serve you", as opposed
 * to something that might work on a second try.
 *
 * Deliberately narrow. Setting a model aside for a rate limit or a passing
 * upstream outage would burn through the free list in an afternoon and leave
 * someone on a worse model permanently, so only refusals that are about the
 * model itself count.
 */
object ModelRefusal {

    /**
     * OpenRouter will not serve any free model until the account opts in to
     * prompt logging, and reports that per model rather than per account — so
     * every model in the list refuses at once and it reads as "nothing works".
     * Recognised so the app can say what to actually do about it.
     */
    const val DATA_POLICY_HINT =
        "OpenRouter needs prompt logging switched on before free models will " +
            "answer. Turn it on at openrouter.ai/settings/privacy, then try again."

    fun isDataPolicy(message: String?): Boolean {
        val text = message?.lowercase() ?: return false
        return text.contains("data policy") || text.contains("no endpoints found")
    }

    private val PERMANENT_MARKERS = listOf(
        "only available on agentic harnesses",
        "is not available",
        "no endpoints found",
        "no allowed providers",
        "not a valid model",
        "invalid model",
        "unknown model",
        "model not found",
        "requires more credits",
        "data policy"
    )

    /** Errors that are about the moment, not the model. */
    private val TRANSIENT_MARKERS = listOf(
        "rate limit",
        "rate-limited",
        "temporarily",
        "timeout",
        "timed out",
        "overloaded",
        "try again",
        "capacity",
        "503",
        "502"
    )

    /** True when the failure was about the moment rather than the model. */
    fun isTransient(message: String?): Boolean {
        val text = message?.lowercase() ?: return false
        return TRANSIENT_MARKERS.any { text.contains(it) }
    }

    fun isPermanent(message: String?): Boolean {
        val text = message?.lowercase() ?: return false
        if (TRANSIENT_MARKERS.any { text.contains(it) }) return false
        return PERMANENT_MARKERS.any { text.contains(it) }
    }
}
