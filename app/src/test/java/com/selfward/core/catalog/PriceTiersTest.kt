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

    private fun choice(id: String) = ModelChoice(id, id, "tier", false)

    /**
     * A vendor lists a moving name and every pinned snapshot behind it, so
     * sixty-five entries are really twenty said three times.
     */
    @Test
    fun snapshotsCollapseOntoTheirMovingName() {
        val collapsed = PriceTiers.collapseSnapshots(
            listOf(
                choice("gpt-4.1-nano"),
                choice("gpt-4.1-nano-2025-04-14"),
                choice("gpt-5-nano"),
                choice("gpt-5-nano-2025-08-07")
            )
        )

        assertEquals(listOf("gpt-4.1-nano", "gpt-5-nano"), collapsed.map { it.id })
    }

    /** Anthropic's moving name is "-latest" rather than the bare id. */
    @Test
    fun anthropicsLatestAliasIsTheOneKept() {
        val collapsed = PriceTiers.collapseSnapshots(
            listOf(
                choice("claude-3-5-haiku-20241022"),
                choice("claude-3-5-haiku-latest")
            )
        )

        assertEquals(listOf("claude-3-5-haiku-latest"), collapsed.map { it.id })
    }

    /**
     * The moving name is preferred even when it is listed after the snapshot,
     * because a model chosen today should keep working when the snapshot behind
     * it is retired.
     */
    @Test
    fun theMovingNameWinsWhicheverOrderTheyArrive() {
        val snapshotFirst = PriceTiers.collapseSnapshots(
            listOf(choice("gpt-4o-2024-08-06"), choice("gpt-4o"))
        )

        assertEquals(listOf("gpt-4o"), snapshotFirst.map { it.id })
    }

    /**
     * Some models are only ever published dated. Dropping the family would hide
     * a model the client can actually use.
     */
    @Test
    fun aFamilyWithNoMovingNameKeepsItsNewestSnapshot() {
        val collapsed = PriceTiers.collapseSnapshots(
            listOf(
                choice("vendor/pinned-2024-01-01"),
                choice("vendor/pinned-2025-06-01")
            )
        )

        assertEquals(listOf("vendor/pinned-2025-06-01"), collapsed.map { it.id })
    }

    /** Collapsing must not reshuffle the cheapest-tier-first ranking. */
    @Test
    fun theRankingSurvivesCollapsing() {
        val collapsed = PriceTiers.collapseSnapshots(
            listOf(
                choice("gpt-4.1-nano"),
                choice("gpt-4.1-nano-2025-04-14"),
                choice("gpt-4o-mini"),
                choice("gpt-4o")
            )
        )

        assertEquals(listOf("gpt-4.1-nano", "gpt-4o-mini", "gpt-4o"), collapsed.map { it.id })
    }

    /** Unrelated models must not be merged just because names look similar. */
    @Test
    fun differentModelsAreNotCollapsedTogether() {
        val collapsed = PriceTiers.collapseSnapshots(
            listOf(choice("gpt-4o-mini"), choice("gpt-4o"), choice("gpt-4.1-nano"))
        )

        assertEquals(3, collapsed.size)
    }

    @Test
    fun aFamilyIsTheIdWithoutItsDateOrLatest() {
        assertEquals("gpt-4.1-nano", PriceTiers.familyOf("gpt-4.1-nano-2025-04-14"))
        assertEquals("claude-3-5-haiku", PriceTiers.familyOf("claude-3-5-haiku-latest"))
        assertEquals("claude-3-5-haiku", PriceTiers.familyOf("claude-3-5-haiku-20241022"))
        assertEquals("gpt-4o", PriceTiers.familyOf("gpt-4o"))
    }

}
