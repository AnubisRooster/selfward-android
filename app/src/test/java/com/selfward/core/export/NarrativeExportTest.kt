package com.selfward.core.export

import com.selfward.core.narrative.NarrativeDocument
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class NarrativeExportTest {

    /** Fixed so the assertions do not move with the machine's zone or locale. */
    private val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.UK).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private fun document(
        content: String = "They had a hard week.",
        sessions: Int = 3,
        updatedAt: Long = 1_700_000_000_000L
    ) = NarrativeDocument(
        content = content,
        sessionCount = sessions,
        sourceWatermark = 0L,
        updatedAt = updatedAt
    )

    @Test
    fun theMarkdownCarriesTheTitleAndTheStory() {
        val markdown = NarrativeExport.markdown(document(), dateFormat)

        assertTrue(markdown.startsWith("# My Story"))
        assertTrue(markdown.contains("They had a hard week."))
    }

    @Test
    fun theSubtitleNamesTheDateAndTheSessionCount() {
        val subtitle = NarrativeExport.subtitle(document(sessions = 3), dateFormat)

        assertEquals("Last updated 14 November 2023 · 3 sessions woven in", subtitle)
    }

    @Test
    fun oneSessionIsNotCalledSessions() {
        assertTrue(
            NarrativeExport.subtitle(document(sessions = 1), dateFormat)
                .contains("1 session woven in")
        )
    }

    /**
     * A document that has never been built has no update time. Formatting the
     * zero would date the person's story to January 1970.
     */
    @Test
    fun aNarrativeThatWasNeverBuiltIsNotDatedToTheEpoch() {
        val subtitle = NarrativeExport.subtitle(document(updatedAt = 0L), dateFormat)

        assertFalse(subtitle.contains("1970"))
        assertFalse(subtitle.contains("Last updated"))
        assertTrue(subtitle.contains("3 sessions woven in"))
    }

    // MARK: - Pagination

    @Test
    fun theDocumentOpensWithATitleAndSubtitle() {
        val blocks = NarrativeExport.paginate(document(), dateFormat)

        assertEquals(NarrativeExport.Style.TITLE, blocks[0].style)
        assertEquals("My Story", blocks[0].text)
        assertEquals(NarrativeExport.Style.SUBTITLE, blocks[1].style)
    }

    @Test
    fun markdownHeadingsBecomeHeadingBlocksWithoutTheirHashes() {
        val blocks = NarrativeExport.paginate(
            document(content = "## The long winter\n\nIt was cold."),
            dateFormat
        )

        val heading = blocks.first { it.style == NarrativeExport.Style.HEADING }
        assertEquals("The long winter", heading.text)
        assertTrue(blocks.any { it.style == NarrativeExport.Style.BODY && it.text == "It was cold." })
    }

    @Test
    fun blankLinesDoNotBecomeEmptyBlocks() {
        val blocks = NarrativeExport.paginate(
            document(content = "One.\n\n\n\nTwo."),
            dateFormat
        )

        assertEquals(
            listOf("One.", "Two."),
            blocks.filter { it.style == NarrativeExport.Style.BODY }.map { it.text }
        )
    }

    /**
     * An unrecognised line is still the person's story. Dropping it silently
     * would lose part of what they came to keep.
     */
    @Test
    fun anUnrecognisedLineIsKeptAsBodyRatherThanDiscarded() {
        val blocks = NarrativeExport.paginate(
            document(content = "- a bulleted thought\n> a quoted one"),
            dateFormat
        )

        val body = blocks.filter { it.style == NarrativeExport.Style.BODY }.map { it.text }
        assertEquals(listOf("- a bulleted thought", "> a quoted one"), body)
    }

    @Test
    fun everyLineOfTheNarrativeSurvivesPagination() {
        val lines = (1..40).map { "Paragraph $it." }
        val blocks = NarrativeExport.paginate(
            document(content = lines.joinToString("\n\n")),
            dateFormat
        )

        assertEquals(lines, blocks.filter { it.style == NarrativeExport.Style.BODY }.map { it.text })
    }
}
