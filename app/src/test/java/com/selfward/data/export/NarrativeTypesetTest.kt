package com.selfward.data.export

import android.graphics.Bitmap
import android.graphics.Canvas
import com.selfward.core.export.NarrativeExport
import com.selfward.core.export.NarrativeExport.Block
import com.selfward.core.export.NarrativeExport.Style
import com.selfward.core.narrative.NarrativeDocument
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Covers where the page breaks fall.
 *
 * The PDF container itself is not exercised: [android.graphics.pdf.PdfDocument]
 * is native with no Robolectric shadow, so it can only be checked on a device.
 * Everything that decides how many pages there are, and what lands on each,
 * runs against a bitmap canvas here.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NarrativeTypesetTest {

    /** Records how many pages were started and how much was drawn on each. */
    private class RecordingTarget : PageTarget {
        val pageNumbers = mutableListOf<Int>()
        var openPages = 0
        var maxOpenPages = 0
        private val drawn = mutableListOf<Int>()

        override fun startPage(number: Int): Canvas {
            pageNumbers += number
            openPages++
            maxOpenPages = maxOf(maxOpenPages, openPages)
            drawn += 0
            val bitmap = Bitmap.createBitmap(PAGE_WIDTH, PAGE_HEIGHT, Bitmap.Config.ARGB_8888)
            return Canvas(bitmap)
        }

        override fun finishPage() {
            openPages--
        }

        val pageCount: Int get() = pageNumbers.size
    }

    private fun typesetting(
        blocks: List<Block>,
        pageHeight: Int = PAGE_HEIGHT
    ): RecordingTarget =
        RecordingTarget().also { typeset(blocks, it, PAGE_WIDTH, pageHeight) }

    @Test
    fun aShortNarrativeIsOnePage() {
        val target = typesetting(
            listOf(Block("My Story", Style.TITLE), Block("It was a quiet week.", Style.BODY))
        )

        assertEquals(1, target.pageCount)
    }

    /**
     * A narrative long enough to overflow A4 has to continue onto another page.
     * Without line-level pagination this rendered one page and dropped
     * everything past the bottom margin.
     */
    @Test
    fun aLongNarrativeRunsOntoFurtherPages() {
        val paragraphs = (1..120).map {
            Block(
                "Paragraph $it. " + "There was a great deal to say about it. ".repeat(4),
                Style.BODY
            )
        }

        assertTrue(typesetting(paragraphs).pageCount > 1)
    }

    /**
     * One block taller than a whole page must flow across the break rather than
     * being pushed onto the next page and clipped there.
     *
     * The block is given hard line breaks rather than left to wrap, because
     * Robolectric's text layout does not wrap at all — a fifteen-thousand
     * character string comes back as `lineCount == 1`. The loop being exercised
     * is the same one either way: it walks the laid-out lines and decides where
     * the page ends. That the wrapping itself produces those lines is checked on
     * a device instead.
     */
    @Test
    fun aBlockTallerThanAPageIsSplitAcrossPages() {
        val manyLines = Block((1..80).joinToString("\n") { "Line $it." }, Style.BODY)

        assertTrue(typesetting(listOf(manyLines)).pageCount > 1)
    }

    /**
     * The guard against an endless run of pages when a single line cannot fit
     * on any of them.
     *
     * A4 has no such case, so the page is shrunk until one does not fit. A
     * regression here does not fail the test — it hangs it — so it is bounded.
     */
    @Test(timeout = 30_000)
    fun aLineTallerThanThePageStillTerminates() {
        val target = typesetting(
            listOf(Block((1..5).joinToString("\n") { "Line $it." }, Style.BODY)),
            pageHeight = 100
        )

        assertTrue("paginated ${target.pageCount} pages", target.pageCount in 1..10)
        assertEquals(0, target.openPages)
    }

    @Test
    fun pagesAreNumberedFromOneWithoutGaps() {
        val paragraphs = (1..120).map {
            Block("Paragraph $it. " + "Words and words and words. ".repeat(5), Style.BODY)
        }

        val target = typesetting(paragraphs)

        assertEquals((1..target.pageCount).toList(), target.pageNumbers)
    }

    /**
     * A page left open leaks into the PDF as an unfinished page, and
     * `PdfDocument` refuses to start the next one while one is still open.
     */
    @Test
    fun everyPageIsFinishedAndOnlyOneIsOpenAtATime() {
        val target = typesetting(
            (1..80).map { Block("Paragraph $it. " + "Words. ".repeat(20), Style.BODY) }
        )

        assertEquals(0, target.openPages)
        assertEquals(1, target.maxOpenPages)
    }

    @Test
    fun anEmptyBlockDoesNotStartAPage() {
        val target = typesetting(listOf(Block("", Style.BODY), Block("Real text.", Style.BODY)))

        assertEquals(1, target.pageCount)
    }

    @Test
    fun aNarrativeWithNoContentStillProducesTheTitlePage() {
        val target = typesetting(listOf(Block("My Story", Style.TITLE)))

        assertEquals(1, target.pageCount)
    }

    @Test
    fun aWholeNarrativeDocumentTypesetsEndToEnd() {
        val document = NarrativeDocument(
            content = "## Beginnings\n\nThey started somewhere.\n\n## Now\n\nThey are here.",
            sessionCount = 4,
            sourceWatermark = 0L,
            updatedAt = 1_700_000_000_000L
        )

        assertEquals(1, typesetting(NarrativeExport.paginate(document)).pageCount)
    }
}
