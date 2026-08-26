package com.selfward.core.catalog

import com.selfward.core.chat.Provider

/**
 * The model each provider starts on.
 *
 * There used to be one default — "gpt-4o-mini" — handed to whichever provider
 * was selected. That is an OpenAI identifier: OpenRouter addresses models by
 * namespaced slug ("openai/gpt-4o-mini") and Anthropic uses its own names, so
 * choosing either provider and saving produced a model-not-found error from the
 * API unless the client happened to know to retype the field by hand.
 */
object ProviderDefaults {

    /**
     * The model to start on before the live catalogue has been fetched.
     *
     * These are cold-start fallbacks, not choices. A hardcoded model id has a
     * shelf life: Anthropic retired "claude-3-5-haiku-latest", and because it
     * was the starting default, choosing Anthropic and sending a first message
     * returned a bare "model: claude-3-5-haiku-latest" from the API — which
     * reads, to the person who just typed something difficult, as the app being
     * broken. [adoptedFrom] is what stops that happening again; keeping these
     * current only shortens the window before the catalogue loads.
     */
    const val OPENAI_DEFAULT = "gpt-4o-mini"
    const val ANTHROPIC_DEFAULT = "claude-haiku-4-5-20251001"

    /**
     * Model ids this app has shipped as a starting default before now.
     *
     * Kept because updating the constant alone helps nobody who already has the
     * old one saved: their stored model would stop matching the default and so
     * would never be corrected, leaving exactly the people affected by the
     * retirement stuck with it. Anything listed here is still a default nobody
     * chose, and may be replaced by what the provider actually serves.
     *
     * Add to this list when changing a default; never remove from it.
     */
    private val RETIRED_DEFAULTS = mapOf(
        Provider.ANTHROPIC to setOf("claude-3-5-haiku-latest", "claude-3-5-haiku-20241022"),
        Provider.OPENAI to emptySet(),
        Provider.OPENROUTER to emptySet()
    )

    /**
     * @param openRouterFreeId the best free model from the fetched catalogue,
     *   when one is known. Falls back to the pinned free slug, so OpenRouter
     *   defaults to something free even before the catalogue has loaded.
     */
    fun modelFor(provider: Provider, openRouterFreeId: String? = null): String = when (provider) {
        Provider.OPENAI -> OPENAI_DEFAULT
        Provider.ANTHROPIC -> ANTHROPIC_DEFAULT
        Provider.OPENROUTER -> openRouterFreeId ?: ModelRanking.PINNED_FREE_FALLBACK
    }

    /**
     * Whether [model] is a starting default rather than something chosen.
     *
     * Only a default is replaced when the catalogue disagrees with it. A model
     * the client picked themselves is left alone even if this code has never
     * heard of it — being second-guessed about your own choice is worse than
     * an error you can read.
     */
    fun isUnchosenDefault(provider: Provider, model: String): Boolean {
        val trimmed = model.trim()
        if (trimmed.isEmpty()) return true
        if (trimmed in RETIRED_DEFAULTS[provider].orEmpty()) return true
        return when (provider) {
            Provider.OPENAI -> trimmed == OPENAI_DEFAULT
            Provider.ANTHROPIC -> trimmed == ANTHROPIC_DEFAULT
            Provider.OPENROUTER -> trimmed == ModelRanking.PINNED_FREE_FALLBACK
        }
    }

    /**
     * The model to move to when the catalogue says the current one is not on
     * offer, or null to stay put.
     *
     * Returns the head of the fetched list, which is already ranked cheapest
     * first. Nothing is changed unless all three are true: the catalogue was
     * actually reached, the current model is a default nobody chose, and that
     * default is not among what is served.
     */
    fun adoptedFrom(
        provider: Provider,
        current: String,
        catalogue: List<ModelChoice>
    ): String? {
        if (catalogue.isEmpty()) return null
        if (!isUnchosenDefault(provider, current)) return null
        if (catalogue.any { it.id == current.trim() }) return null
        return catalogue.first().id
    }

    /**
     * Whether [model] plausibly belongs to [provider].
     *
     * Only used to decide whether to replace the field when the provider
     * changes, so it is deliberately loose: it answers "was this obviously
     * meant for a different provider", not "is this a real model".
     */
    fun looksWrongFor(provider: Provider, model: String): Boolean {
        val trimmed = model.trim()
        if (trimmed.isEmpty()) return true
        return when (provider) {
            // OpenRouter slugs are always vendor-namespaced.
            Provider.OPENROUTER -> !trimmed.contains('/')
            // A namespaced slug was meant for OpenRouter, not the vendor's own API.
            Provider.OPENAI -> trimmed.contains('/') || trimmed.startsWith("claude")
            Provider.ANTHROPIC -> trimmed.contains('/') || !trimmed.startsWith("claude")
        }
    }
}
