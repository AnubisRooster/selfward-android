package com.selfward.core.catalog

import com.selfward.core.chat.Provider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Moving off a starting default the provider has retired.
 *
 * Found in the field, not in a test: Anthropic withdrew
 * "claude-3-5-haiku-latest" while it was still this app's starting model, so
 * choosing Anthropic and sending a first message came back as a bare
 * "model: claude-3-5-haiku-latest". To the person who has just typed something
 * difficult, that is the app being broken.
 */
class ProviderDefaultsAdoptionTest {

    private fun choice(id: String) = ModelChoice(id, id, "tier", false)

    private val live = listOf(
        choice("claude-haiku-4-5-20251001"),
        choice("claude-sonnet-4-5-20250929")
    )

    // MARK: - Adoption

    @Test
    fun aRetiredStartingDefaultIsReplacedByWhatIsActuallyServed() {
        val adopted = ProviderDefaults.adoptedFrom(
            Provider.ANTHROPIC,
            current = "claude-3-5-haiku-latest",
            catalogue = live
        )

        assertEquals("claude-haiku-4-5-20251001", adopted)
    }

    /**
     * The head of the list is taken, whatever it happens to be: the catalogue
     * arrives already ranked cheapest tier first, and re-deciding that here
     * would be a second, quietly different ranking.
     */
    @Test
    fun theHeadOfTheRankedListIsWhatGetsAdopted() {
        val reordered = listOf(choice("claude-sonnet-4-5-20250929"), choice("claude-haiku-4-5-20251001"))

        val adopted = ProviderDefaults.adoptedFrom(
            Provider.ANTHROPIC,
            current = "claude-3-5-haiku-latest",
            catalogue = reordered
        )

        assertEquals("claude-sonnet-4-5-20250929", adopted)
    }

    /**
     * Updating the constant alone would help nobody who already had the old one
     * saved: it would stop matching "the default" and so never be corrected,
     * stranding exactly the people the retirement affected.
     */
    @Test
    fun aDefaultShippedByAnEarlierVersionIsStillReplaced() {
        assertTrue(
            ProviderDefaults.isUnchosenDefault(Provider.ANTHROPIC, "claude-3-5-haiku-latest")
        )
        assertEquals(
            "claude-haiku-4-5-20251001",
            ProviderDefaults.adoptedFrom(
                Provider.ANTHROPIC,
                current = "claude-3-5-haiku-latest",
                catalogue = live
            )
        )
    }

    /**
     * The client picked this one. Being second-guessed about your own choice is
     * worse than an error you can read and act on.
     */
    @Test
    fun aModelTheClientChoseIsNeverReplaced() {
        assertNull(
            ProviderDefaults.adoptedFrom(
                Provider.ANTHROPIC,
                current = "claude-opus-4-6-something",
                catalogue = live
            )
        )
    }

    @Test
    fun aDefaultThatIsStillServedIsLeftAlone() {
        val stillListed = listOf(choice(ProviderDefaults.ANTHROPIC_DEFAULT)) + live

        assertNull(
            ProviderDefaults.adoptedFrom(
                Provider.ANTHROPIC,
                current = ProviderDefaults.ANTHROPIC_DEFAULT,
                catalogue = stillListed
            )
        )
    }

    /**
     * An empty catalogue means the provider could not be reached — on a train,
     * or with no key yet. Treating that as "your model does not exist" would
     * change the model for someone who is merely offline.
     */
    @Test
    fun anUnreachableCatalogueChangesNothing() {
        assertNull(
            ProviderDefaults.adoptedFrom(
                Provider.ANTHROPIC,
                current = "claude-3-5-haiku-latest",
                catalogue = emptyList()
            )
        )
    }

    @Test
    fun theSameHoldsForOpenAiAndOpenRouter() {
        assertEquals(
            "gpt-5-nano",
            ProviderDefaults.adoptedFrom(
                Provider.OPENAI,
                current = ProviderDefaults.OPENAI_DEFAULT,
                catalogue = listOf(choice("gpt-5-nano"))
            )
        )
        assertEquals(
            "vendor/new:free",
            ProviderDefaults.adoptedFrom(
                Provider.OPENROUTER,
                current = ModelRanking.PINNED_FREE_FALLBACK,
                catalogue = listOf(choice("vendor/new:free"))
            )
        )
    }

    // MARK: - What counts as unchosen

    @Test
    fun onlyTheStartingDefaultsCountAsUnchosen() {
        assertTrue(
            ProviderDefaults.isUnchosenDefault(Provider.ANTHROPIC, ProviderDefaults.ANTHROPIC_DEFAULT)
        )
        assertTrue(
            ProviderDefaults.isUnchosenDefault(Provider.OPENAI, ProviderDefaults.OPENAI_DEFAULT)
        )
        assertTrue(
            ProviderDefaults.isUnchosenDefault(
                Provider.OPENROUTER, ModelRanking.PINNED_FREE_FALLBACK
            )
        )
        assertFalse(ProviderDefaults.isUnchosenDefault(Provider.ANTHROPIC, "claude-opus-4-6"))
    }

    /** Nothing chosen at all is as unchosen as it gets. */
    @Test
    fun anEmptyModelCountsAsUnchosen() {
        assertTrue(ProviderDefaults.isUnchosenDefault(Provider.ANTHROPIC, "   "))
    }

    /**
     * One provider's default must not be treated as another's — that is how a
     * model would be swapped out from under someone who switched provider.
     */
    @Test
    fun aDefaultBelongingToAnotherProviderIsNotTreatedAsUnchosen() {
        assertFalse(
            ProviderDefaults.isUnchosenDefault(Provider.OPENAI, ProviderDefaults.ANTHROPIC_DEFAULT)
        )
    }

    /**
     * The shipped fallback should at least be shaped like one of the vendor's
     * own names, or it fails the app's own sanity check the moment it is used.
     */
    @Test
    fun theShippedAnthropicFallbackLooksLikeAnAnthropicModel() {
        assertFalse(
            ProviderDefaults.looksWrongFor(Provider.ANTHROPIC, ProviderDefaults.ANTHROPIC_DEFAULT)
        )
    }
}
