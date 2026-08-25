package com.selfward.core.catalog

import com.selfward.core.chat.Provider

/**
 * The models a given provider will serve, cheapest first.
 *
 * One list per provider rather than one list for the app: the three do not
 * share a model namespace, a price list, or even a way of publishing what they
 * offer. OpenRouter returns prices, so its ranking is a fact; the other two
 * return names, so theirs is a reading of vendor tiers — see [PriceTiers].
 */
interface ProviderCatalog {

    /**
     * @param force refetch even if a recent answer is held. The list is
     *   refreshed on every app open, because models are withdrawn and added
     *   constantly and a stale list offers something that no longer exists.
     * @return an empty list when the provider cannot be reached, so a screen
     *   still opens on a train.
     */
    suspend fun ranked(
        provider: Provider,
        apiKey: String?,
        force: Boolean = false
    ): List<ModelChoice>
}
