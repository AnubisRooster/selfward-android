package com.selfward.core.catalog

import com.selfward.core.chat.Provider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PriceTiersTest {

    /**
     * OpenAI's list carries transcription, speech, image and embedding models
     * beside the chat ones. Any of them chosen here would fail on the first
     * message, having looked like a perfectly ordinary option.
     */
    @Test
    fun openAisNonChatModelsAreNotOffered() {
        listOf(
            "whisper-1", "tts-1-hd", "dall-e-3",
            "text-embedding-3-small", "omni-moderation-latest"
        ).forEach {
            assertFalse(it, PriceTiers.isChatModel(Provider.OPENAI, it))
        }
    }

    @Test
    fun openAisChatModelsAreOffered() {
        listOf("gpt-4o-mini", "gpt-4o", "o1-mini").forEach {
            assertTrue(it, PriceTiers.isChatModel(Provider.OPENAI, it))
        }
    }

    @Test
    fun anthropicOffersOnlyItsClaudeModels() {
        assertTrue(PriceTiers.isChatModel(Provider.ANTHROPIC, "claude-3-5-haiku-latest"))
        assertFalse(PriceTiers.isChatModel(Provider.ANTHROPIC, "something-else"))
    }

    /** OpenRouter's list is filtered on published prices, not on names. */
    @Test
    fun openRouterIsNotFilteredByName() {
        assertTrue(PriceTiers.isChatModel(Provider.OPENROUTER, "meta-llama/llama:free"))
    }

    @Test
    fun theSmallerOpenAiTiersSortFirst() {
        val sorted = listOf("gpt-4o", "gpt-4o-mini", "gpt-4.1-nano")
            .sortedBy { PriceTiers.rank(Provider.OPENAI, it) }

        assertEquals(listOf("gpt-4.1-nano", "gpt-4o-mini", "gpt-4o"), sorted)
    }

    @Test
    fun anthropicSortsHaikuBeforeSonnetBeforeOpus() {
        val sorted = listOf("claude-3-opus", "claude-3-5-haiku", "claude-3-5-sonnet")
            .sortedBy { PriceTiers.rank(Provider.ANTHROPIC, it) }

        assertEquals(
            listOf("claude-3-5-haiku", "claude-3-5-sonnet", "claude-3-opus"),
            sorted
        )
    }

    /**
     * A tier this does not recognise is still a model the client may want, so
     * it sorts after the known ones rather than disappearing.
     */
    @Test
    fun anUnrecognisedTierSortsLastRatherThanVanishing() {
        val unknown = PriceTiers.rank(Provider.ANTHROPIC, "claude-9-something")
        val known = PriceTiers.rank(Provider.ANTHROPIC, "claude-3-5-haiku")

        assertTrue(unknown > known)
        assertTrue(PriceTiers.isChatModel(Provider.ANTHROPIC, "claude-9-something"))
    }

    /**
     * The heading must not claim a price the app was never told. Only
     * OpenRouter publishes one, so only OpenRouter's list may say "free".
     */
    @Test
    fun onlyOpenRouterIsDescribedAsFree() {
        assertTrue(PriceTiers.headingFor(Provider.OPENROUTER, 3).contains("free"))
        assertFalse(PriceTiers.headingFor(Provider.OPENAI, 3).contains("free"))
        assertFalse(PriceTiers.headingFor(Provider.ANTHROPIC, 3).contains("free"))
    }

    @Test
    fun theHeadingCountsWhatIsOnOffer() {
        assertTrue(PriceTiers.headingFor(Provider.OPENAI, 7).startsWith("7 "))
    }
}
