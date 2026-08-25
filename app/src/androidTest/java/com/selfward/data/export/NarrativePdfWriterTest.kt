package com.selfward.data.export

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.selfward.core.export.NarrativeExport
import com.selfward.core.export.NarrativeExport.Block
import com.selfward.core.export.NarrativeExport.Style
import com.selfward.core.narrative.NarrativeDocument
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * The two things the unit tests cannot reach.
 *
 * `PdfDocument` is native with no Robolectric shadow, and Robolectric's text
 * layout does not wrap — a fifteen-thousand character string comes back as one
 * line there. Both are exercised here, on a real framework, so the parts of the
 * export that only exist on a device are not shipped unverified.
 */
@RunWith(AndroidJUnit4::class)
class NarrativePdfWriterTest {

    private val writer = NarrativePdfWriter()

    private fun render(blocks: List<Block>): ByteArray =
        ByteArrayOutputStream().also { writer.render(blocks, it) }.toByteArray()

    private fun pageCount(pdf: ByteArray): Int =
        Regex("/Type\\s*/Page[^s]").findAll(String(pdf, Charsets.ISO_8859_1)).count()

    @Test
    fun theOutputIsARealPdf() {
        val pdf = render(listOf(Block("My Story", Style.TITLE), Block("A quiet week.", Style.BODY)))

        assertEquals("%PDF", String(pdf.copyOfRange(0, 4), Charsets.ISO_8859_1))
        assertTrue("only ${pdf.size} bytes", pdf.size > 500)
        assertEquals(1, pageCount(pdf))
    }

    /**
     * Real line wrapping: one paragraph, no hard breaks, long enough that it
     * cannot fit on a single A4 page unless the text silently vanished.
     */
    @Test
    fun aWrappedParagraphLongerThanAPageRunsOntoTheNext() {
        val paragraph = Block("Then they said it again, and meant it. ".repeat(400), Style.BODY)

        assertTrue(pageCount(render(listOf(paragraph))) > 1)
    }

    @Test
    fun aWholeNarrativeRendersAndIsReadableAsAPdf() {
        val document = NarrativeDocument(
            content = buildString {
                append("## Beginnings\n\n")
                append("They started somewhere small. ".repeat(40))
                append("\n\n## Now\n\n")
                append("They are here, and it is different. ".repeat(40))
            },
            sessionCount = 4,
            sourceWatermark = 0L,
            updatedAt = 1_700_000_000_000L
        )

        val pdf = render(NarrativeExport.paginate(document))

        assertEquals("%PDF", String(pdf.copyOfRange(0, 4), Charsets.ISO_8859_1))
        assertTrue(pageCount(pdf) >= 1)
    }

    /**
     * The share path end to end: a file written where the provider can reach it,
     * and a content uri that resolves back through the app's own provider.
     */
    @Test
    fun anExportedFileIsReachableThroughTheProvider() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val exportFiles = ExportFiles(context)

        val uri = exportFiles.writeBinary(NarrativeExport.PDF_FILENAME) { out ->
            writer.render(listOf(Block("My Story", Style.TITLE)), out)
        }

        assertEquals("content", uri.scheme)
        assertEquals(ExportFiles.authority(context.packageName), uri.authority)

        val bytes = context.contentResolver.openInputStream(uri)!!.use { it.readBytes() }
        assertEquals("%PDF", String(bytes.copyOfRange(0, 4), Charsets.ISO_8859_1))
    }

    @Test
    fun theGraphExportIsAlsoReachableThroughTheProvider() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val exportFiles = ExportFiles(context)

        val uri = exportFiles.writeText("knowledge-graph.json", """{"elements":{}}""")

        val text = context.contentResolver.openInputStream(uri)!!
            .use { String(it.readBytes(), Charsets.UTF_8) }
        assertEquals("""{"elements":{}}""", text)
    }

    /** Nothing outside the exports directory may be handed out. */
    @Test
    fun aFileOutsideTheExportDirectoryIsRefusedByTheProvider() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val outside = File(context.filesDir, "secret.txt").apply { writeText("private") }

        val refused = runCatching {
            androidx.core.content.FileProvider.getUriForFile(
                context,
                ExportFiles.authority(context.packageName),
                outside
            )
        }.isFailure

        assertTrue("the provider handed out a file outside its configured root", refused)
    }
}
