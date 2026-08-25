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

    const val OPENAI_DEFAULT = "gpt-4o-mini"
    const val ANTHROPIC_DEFAULT = "claude-3-5-haiku-latest"

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
