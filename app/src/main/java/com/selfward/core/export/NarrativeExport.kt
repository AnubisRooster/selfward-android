package com.selfward.core.export

import com.selfward.core.narrative.NarrativeDocument
import java.text.DateFormat
import java.util.Date

/**
 * Turns the narrative into something the person can keep.
 *
 * Markdown is produced here, as text; the PDF is rendered from the same
 * [paginate] structure by `NarrativePdfWriter`, which needs Android's text
 * layout and so cannot live in this layer.
 *
 * The header matches iOS `NarrativeExportService` so a file exported from
 * either phone reads the same way.
 */
object NarrativeExport {

    const val MARKDOWN_FILENAME = "life-narrative.md"
    const val PDF_FILENAME = "life-narrative.pdf"

    const val TITLE = "My Story"

    fun markdown(
        document: NarrativeDocument,
        dateFormat: DateFormat = DateFormat.getDateInstance(DateFormat.LONG)
    ): String = buildString {
        append("# ").append(TITLE).append("\n\n")
        append("*").append(subtitle(document, dateFormat)).append("*\n\n")
        append("---\n\n")
        append(document.content.trim()).append("\n")
    }

    /**
     * The one line under the title: when it was last written and how much of
     * the person's material is in it.
     *
     * A narrative that has never been built has no update time to report, and
     * printing the epoch — "1 January 1970" — would be worse than saying
     * nothing, so that case is left off.
     */
    fun subtitle(document: NarrativeDocument, dateFormat: DateFormat): String {
        val sessions = "${document.sessionCount} " +
            if (document.sessionCount == 1) "session woven in" else "sessions woven in"
        if (document.updatedAt <= 0L) return sessions
        return "Last updated ${dateFormat.format(Date(document.updatedAt))} · $sessions"
    }

    /** One block of the document, as the PDF renderer needs to lay it out. */
    data class Block(val text: String, val style: Style)

    enum class Style { TITLE, SUBTITLE, HEADING, BODY }

    /**
     * Splits the narrative into styled blocks for typesetting.
     *
     * The model is asked for `## headings` and paragraphs, so that is what is
     * recognised. Anything else is carried through as body text rather than
     * dropped — an unexpected line is still the person's story, and losing it
     * silently would be the worse failure.
     */
    fun paginate(
        document: NarrativeDocument,
        dateFormat: DateFormat = DateFormat.getDateInstance(DateFormat.LONG)
    ): List<Block> = buildList {
        add(Block(TITLE, Style.TITLE))
        add(Block(subtitle(document, dateFormat), Style.SUBTITLE))
        document.content.trim().lines().forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.isEmpty() -> Unit
                trimmed.startsWith("#") ->
                    add(Block(trimmed.trimStart('#').trim(), Style.HEADING))
                else -> add(Block(trimmed, Style.BODY))
            }
        }
    }
}
