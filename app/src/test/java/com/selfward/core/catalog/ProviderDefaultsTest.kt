package com.selfward.core.catalog

import com.selfward.core.chat.Provider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProviderDefaultsTest {

    /**
     * The bug this exists to stop: one default, "gpt-4o-mini", was handed to
     * whichever provider was selected. OpenRouter addresses models by
     * namespaced slug and Anthropic by its own names, so both rejected it.
     */
    @Test
    fun eachProviderDefaultsToSomethingItCouldActuallyServe() {
        assertEquals("gpt-4o-mini", ProviderDefaults.modelFor(Provider.OPENAI))
        assertTrue(ProviderDefaults.modelFor(Provider.ANTHROPIC).startsWith("claude"))
        assertTrue(ProviderDefaults.modelFor(Provider.OPENROUTER).contains('/'))
    }

    @Test
    fun openRouterDefaultsToAFreeModel() {
        assertEquals(
            ModelRanking.PINNED_FREE_FALLBACK,
            ProviderDefaults.modelFor(Provider.OPENROUTER)
        )
    }

    @Test
    fun aKnownFreeModelIsPreferredOverThePin() {
        assertEquals(
            "vendor/roomy:free",
            ProviderDefaults.modelFor(Provider.OPENROUTER, openRouterFreeId = "vendor/roomy:free")
        )
    }

    /** Only OpenRouter takes the catalogue hint; the others have fixed defaults. */
    @Test
    fun theCatalogueHintDoesNotLeakIntoOtherProviders() {
        assertEquals(
            "gpt-4o-mini",
            ProviderDefaults.modelFor(Provider.OPENAI, openRouterFreeId = "vendor/roomy:free")
        )
    }

    @Test
    fun anOpenAiIdIsWrongForOpenRouter() {
        assertTrue(ProviderDefaults.looksWrongFor(Provider.OPENROUTER, "gpt-4o-mini"))
        assertFalse(ProviderDefaults.looksWrongFor(Provider.OPENROUTER, "openai/gpt-4o-mini"))
    }

    @Test
    fun aNamespacedSlugIsWrongForTheVendorsOwnApi() {
        assertTrue(ProviderDefaults.looksWrongFor(Provider.OPENAI, "openai/gpt-4o-mini"))
        assertTrue(ProviderDefaults.looksWrongFor(Provider.ANTHROPIC, "anthropic/claude-3-5-haiku"))
    }

    @Test
    fun aClaudeIdIsWrongForOpenAiAndRightForAnthropic() {
        assertTrue(ProviderDefaults.looksWrongFor(Provider.OPENAI, "claude-3-5-haiku-latest"))
        assertFalse(ProviderDefaults.looksWrongFor(Provider.ANTHROPIC, "claude-3-5-haiku-latest"))
    }

    @Test
    fun anEmptyModelIsWrongForEveryProvider() {
        Provider.entries.forEach {
            assertTrue(it.name, ProviderDefaults.looksWrongFor(it, ""))
            assertTrue(it.name, ProviderDefaults.looksWrongFor(it, "   "))
        }
    }

    /**
     * Every default must survive its own check, or switching provider twice
     * would rewrite a model the app had just chosen.
     */
    @Test
    fun noProvidersOwnDefaultLooksWrongToIt() {
        Provider.entries.forEach {
            val default = ProviderDefaults.modelFor(it)
            assertFalse("$it rejected its own default $default",
                ProviderDefaults.looksWrongFor(it, default))
        }
    }
}
