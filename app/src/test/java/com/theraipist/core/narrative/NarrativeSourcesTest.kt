package com.theraipist.core.narrative

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NarrativeSourcesTest {

    private fun note(at: Long, text: String = "a note") = NarrativeSource(at, "Note", text)
    private fun turn(at: Long, text: String = "a turn") = NarrativeSource(at, "You said", text)

    @Test
    fun artifactsAreUsedWhenPresent() {
        val selected = NarrativeSources.select(
            artifacts = listOf(note(20)),
            conversationTurns = listOf(turn(30)),
            watermark = 0
        )

        assertEquals(listOf("Note"), selected.map { it.kind })
    }

    /** Chat-only history still gets a narrative, via the fallback. */
    @Test
    fun conversationIsUsedWhenThereAreNoArtifacts() {
        val selected = NarrativeSources.select(
            artifacts = emptyList(),
            conversationTurns = listOf(turn(10), turn(20)),
            watermark = 0
        )

        assertEquals(2, selected.size)
        assertTrue(selected.all { it.kind == "You said" })
    }

    @Test
    fun nothingNewerThanTheWatermarkIsSelected() {
        val selected = NarrativeSources.select(
            artifacts = listOf(note(10), note(20), note(30)),
            conversationTurns = emptyList(),
            watermark = 20
        )

        assertEquals(listOf(30L), selected.map { it.createdAt })
    }

    /**
     * The watermark is exclusive: a source created at exactly the watermark was
     * already woven in, so re-reading it would duplicate material.
     */
    @Test
    fun theWatermarkItselfIsExcluded() {
        val selected = NarrativeSources.select(
            artifacts = listOf(note(20)),
            conversationTurns = emptyList(),
            watermark = 20
        )

        assertTrue(selected.isEmpty())
    }

    @Test
    fun nothingNewYieldsNothingRatherThanRebuildingFromScratch() {
        val selected = NarrativeSources.select(
            artifacts = listOf(note(5)),
            conversationTurns = listOf(turn(5)),
            watermark = 100
        )

        assertTrue(selected.isEmpty())
    }

    @Test
    fun sourcesComeBackOldestFirst() {
        val selected = NarrativeSources.select(
            artifacts = listOf(note(30), note(10), note(20)),
            conversationTurns = emptyList(),
            watermark = 0
        )

        assertEquals(listOf(10L, 20L, 30L), selected.map { it.createdAt })
    }

    @Test
    fun theFallbackKeepsOnlyTheMostRecentTurns() {
        val many = (1..100L).map { turn(it) }

        val selected = NarrativeSources.select(emptyList(), many, watermark = 0)

        assertEquals(NarrativeSources.MAX_FALLBACK_TURNS, selected.size)
        assertEquals(41L, selected.first().createdAt)
        assertEquals(100L, selected.last().createdAt)
    }

    @Test
    fun longTurnsAreTruncatedSoOnePersonCannotFloodThePrompt() {
        val selected = NarrativeSources.select(
            artifacts = emptyList(),
            conversationTurns = listOf(turn(1, "x".repeat(1000))),
            watermark = 0
        )

        assertEquals(NarrativeSources.MAX_FALLBACK_CHARS + 1, selected.single().text.length)
        assertTrue(selected.single().text.endsWith("…"))
    }

    @Test
    fun blankTurnsAreDropped() {
        val selected = NarrativeSources.select(
            artifacts = emptyList(),
            conversationTurns = listOf(turn(1, "   "), turn(2, "real")),
            watermark = 0
        )

        assertEquals(listOf("real"), selected.map { it.text })
    }
}

class NarrativePromptTest {

    private val sources = listOf(NarrativeSource(1, "Note", "felt steadier this week"))

    @Test
    fun theFirstRunAsksForAStoryBuiltFromScratch() {
        val prompt = NarrativePrompt.user(existing = "", sources = sources)

        assertTrue(prompt.contains("Weave them into a single cohesive narrative"))
        assertTrue(prompt.contains("felt steadier this week"))
    }

    @Test
    fun alaterRunAsksForTheExistingStoryToBeRevisedRatherThanAppended() {
        val prompt = NarrativePrompt.user(existing = "Once upon a time", sources = sources)

        assertTrue(prompt.contains("ONE cohesive, comprehensive story"))
        assertTrue(prompt.contains("not a concatenation"))
        assertTrue("the existing narrative must be included to be revised", prompt.contains("Once upon a time"))
    }

    @Test
    fun sourcesAreLabelledByKind() {
        assertTrue(NarrativePrompt.user("", sources).contains("[Note] felt steadier this week"))
    }

    /** The narrative is about the person, so it must not address them by name. */
    @Test
    fun theSystemPromptForbidsUsingTheirName() {
        val system = NarrativePrompt.system("Kai")

        assertTrue(system.contains("never by name"))
        assertTrue(system.contains("Kai"))
    }
}
