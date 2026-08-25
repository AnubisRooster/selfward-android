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

    fun isPermanent(message: String?): Boolean {
        val text = message?.lowercase() ?: return false
        if (TRANSIENT_MARKERS.any { text.contains(it) }) return false
        return PERMANENT_MARKERS.any { text.contains(it) }
    }
}
