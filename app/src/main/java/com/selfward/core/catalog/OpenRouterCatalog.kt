package com.selfward.core.catalog

/**
 * The OpenRouter model catalogue, so the app can offer the free models rather
 * than making the client type a slug from memory.
 */
interface OpenRouterCatalog {

    /**
     * The catalogue, from cache when it is fresh enough.
     *
     * Returns an empty list rather than throwing when OpenRouter cannot be
     * reached: not knowing the catalogue is a reason to fall back to the pinned
     * free model, not a reason to fail the screen the client is standing on.
     *
     * @param apiKey sent when present. The endpoint is public, but a key makes
     *   the response reflect what that account can actually reach.
     */
    suspend fun models(apiKey: String? = null, forceRefresh: Boolean = false): List<OpenRouterModel>

    /** Whatever was last fetched, without touching the network. */
    fun cached(): List<OpenRouterModel>
}
