package com.selfward.core.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelRankingTest {

    private fun model(
        id: String,
        prompt: String = "0",
        completion: String = "0",
        context: Int = 8_000,
        input: List<String> = listOf("text"),
        output: List<String> = listOf("text")
    ) = OpenRouterModel(id, id, prompt, completion, context, input, output)

    @Test
    fun bothPricesZeroMakesAModelFree() {
        assertTrue(model("a/b", "0", "0").isFree)
    }

    /** iOS compares the price strings to "0" exactly, so "0.0" reads as paid. */
    @Test
    fun aZeroWrittenAnyOtherWayIsStillFree() {
        assertTrue(model("a/b", "0.0", "0.00").isFree)
        assertTrue(model("a/b", "0", "0.000000").isFree)
    }

    @Test
    fun anyPriceAboveZeroMakesItPaid() {
        assertFalse(model("a/b", "0", "0.0000006").isFree)
        assertFalse(model("a/b", "0.0000006", "0").isFree)
    }

    /**
     * A model whose price cannot be read must never be offered as free —
     * getting that wrong spends the client's money without asking.
     */
    @Test
    fun anUnreadablePriceCountsAsPaid() {
        assertFalse(model("a/b", "", "").isFree)
        assertFalse(model("a/b", "unknown", "0").isFree)
        assertFalse(model("a/b", "0", "N/A").isFree)
    }

    @Test
    fun freeModelsAreRankedByContextLength() {
        val ranked = ModelRanking.freeModels(
            listOf(
                model("v/small", context = 4_000),
                model("v/large", context = 128_000),
                model("v/medium", context = 32_000)
            )
        )

        assertEquals(listOf("v/large", "v/medium", "v/small"), ranked.map { it.id })
    }

    @Test
    fun paidModelsAreExcludedFromTheFreeList() {
        val all = listOf(
            model("free/one", context = 8_000),
            model("paid/one", prompt = "0.001", context = 200_000)
        )

        assertEquals(listOf("free/one"), ModelRanking.freeModels(all).map { it.id })
        assertEquals(listOf("paid/one"), ModelRanking.paidModels(all).map { it.id })
    }

    /** Free first, whatever their context lengths, so the cheap option leads. */
    @Test
    fun rankingPutsEveryFreeModelAheadOfEveryPaidOne() {
        val ranked = ModelRanking.ranked(
            listOf(
                model("paid/huge", prompt = "0.01", context = 1_000_000),
                model("free/tiny", context = 2_000)
            )
        )

        assertEquals(listOf("free/tiny", "paid/huge"), ranked.map { it.id })
    }

    @Test
    fun theBestFreeModelIsTheRoomiestOne() {
        val best = ModelRanking.bestFree(
            listOf(model("v/small", context = 4_000), model("v/big", context = 64_000))
        )

        assertEquals("v/big", best?.id)
    }

    @Test
    fun aCatalogueWithNothingFreeHasNoBestFree() {
        assertNull(ModelRanking.bestFree(listOf(model("paid/one", prompt = "0.002"))))
    }

    /**
     * With no catalogue the default must still be free. Falling through to a
     * paid model would start charging someone who never chose to spend.
     */
    @Test
    fun theFallbackIsUsedWhenNothingFreeIsKnown() {
        assertEquals(ModelRanking.PINNED_FREE_FALLBACK, ModelRanking.defaultFreeId(emptyList()))
        assertEquals(
            ModelRanking.PINNED_FREE_FALLBACK,
            ModelRanking.defaultFreeId(listOf(model("paid/one", prompt = "0.002")))
        )
    }

    @Test
    fun theFallbackIsItselfAFreeSlug() {
        assertTrue(ModelRanking.PINNED_FREE_FALLBACK.endsWith(":free"))
    }

    @Test
    fun aKnownFreeModelBeatsTheFallback() {
        assertEquals("v/big", ModelRanking.defaultFreeId(listOf(model("v/big", context = 64_000))))
    }

    /**
     * Found against the live catalogue: OpenRouter prices Google's Lyria music
     * models at zero with a million-token context, so ranking free models on
     * context alone put a music generator first — and it would have become the
     * default model for a therapy app.
     */
    @Test
    fun aFreeModelThatCannotReplyInTextIsNotOffered() {
        val music = model(
            "google/lyria-3-pro-preview",
            context = 1_048_576,
            input = listOf("text", "image"),
            output = listOf("text", "audio")
        )
        val chat = model("nvidia/nemotron:free", context = 1_000_000)

        assertFalse(music.isTextChat)
        assertEquals(listOf(chat.id), ModelRanking.freeModels(listOf(music, chat)).map { it.id })
        assertEquals(chat.id, ModelRanking.defaultFreeId(listOf(music, chat)))
    }

    @Test
    fun aModelThatTakesImagesButRepliesInTextIsStillChat() {
        val multimodal = model("vendor/vision:free", input = listOf("text", "image"))

        assertTrue(multimodal.isTextChat)
    }

    /**
     * Cloaked models are free because the provider logs prompts to evaluate
     * them. That is the wrong trade for this app whatever their context length.
     */
    @Test
    fun cloakedModelsAreNeverOffered() {
        val cloaked = model("stealth/ox-alpha", context = 1_048_576)
        val ordinary = model("vendor/plain:free", context = 32_000)

        assertTrue(cloaked.isCloaked)
        assertEquals(
            listOf(ordinary.id),
            ModelRanking.freeModels(listOf(cloaked, ordinary)).map { it.id }
        )
    }

    @Test
    fun cloakedAndNonTextModelsAreKeptOutOfThePaidListToo() {
        val all = listOf(
            model("stealth/paid", prompt = "0.01"),
            model("vendor/music", prompt = "0.01", output = listOf("text", "audio")),
            model("vendor/ok", prompt = "0.01")
        )

        assertEquals(listOf("vendor/ok"), ModelRanking.paidModels(all).map { it.id })
    }

    @Test
    fun namesAreSplitForCompactDisplay() {
        val m = model("meta-llama/llama-3.2-3b-instruct:free")
        assertEquals("llama-3.2-3b-instruct:free", m.shortName)
        assertEquals("meta-llama", m.vendor)
    }

    @Test
    fun anUnnamespacedIdHasNoVendor() {
        assertEquals("", model("solo").vendor)
        assertEquals("solo", model("solo").shortName)
    }

    /** Equal context lengths must not shuffle between refreshes. */
    @Test
    fun tiesAreBrokenStably() {
        val a = listOf(model("v/b", context = 8_000), model("v/a", context = 8_000))

        assertEquals(
            ModelRanking.freeModels(a).map { it.id },
            ModelRanking.freeModels(a.reversed()).map { it.id }
        )
    }
}
