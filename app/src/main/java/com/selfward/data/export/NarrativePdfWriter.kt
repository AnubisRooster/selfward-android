package com.selfward.data.export

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.selfward.core.export.NarrativeExport.Block
import com.selfward.core.export.NarrativeExport.Style
import java.io.OutputStream

/** A4 at 72dpi, the unit PdfDocument works in. */
internal const val PAGE_WIDTH = 595
internal const val PAGE_HEIGHT = 842

/**
 * Somewhere to draw pages.
 *
 * This exists so the typesetting can be tested. [PdfDocument] is native with no
 * Robolectric shadow — constructing one off-device gives a handle that reports
 * itself closed — so a test that went straight to PDF could only ever assert
 * that nothing threw. Behind this interface the interesting part, deciding
 * where the page breaks fall, runs against an ordinary bitmap canvas and can be
 * checked properly.
 */
internal interface PageTarget {
    /** Begins page [number], 1-based, and returns the canvas to draw it on. */
    fun startPage(number: Int): Canvas

    fun finishPage()
}

/**
 * Typesets the narrative as an A4 PDF.
 *
 * The counterpart of iOS `NarrativeExportService.writePDF`, and the reason the
 * narrative can be printed or filed rather than only read on the phone.
 *
 * Text is laid out with [StaticLayout], which is what Android itself uses to
 * wrap text, so line breaking matches what the person sees in the app —
 * including for scripts that do not break on spaces.
 */
class NarrativePdfWriter {

    fun render(blocks: List<Block>, out: OutputStream) {
        val document = PdfDocument()
        try {
            typeset(blocks, PdfPageTarget(document))
            document.writeTo(out)
        } finally {
            document.close()
        }
    }

    private class PdfPageTarget(private val document: PdfDocument) : PageTarget {
        private var page: PdfDocument.Page? = null

        override fun startPage(number: Int): Canvas {
            val started = document.startPage(
                PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, number).create()
            )
            page = started
            return started.canvas
        }

        override fun finishPage() {
            page?.let { document.finishPage(it) }
            page = null
        }
    }
}

/**
 * Lays [blocks] out down successive pages of [target].
 *
 * Pagination is per line rather than per block, so a long paragraph flows
 * across a page break instead of being pushed whole onto the next page and
 * leaving half a page blank.
 *
 * The page size is a parameter only so tests can shrink it. A page too short
 * to hold even one line is the case the loop has to survive, and it cannot be
 * reached on A4 with the type sizes used here.
 */
internal fun typeset(
    blocks: List<Block>,
    target: PageTarget,
    pageWidth: Int = PAGE_WIDTH,
    pageHeight: Int = PAGE_HEIGHT
) {
    val cursor = Cursor(target, pageWidth, pageHeight)
    blocks.forEach { cursor.draw(it) }
    cursor.finish()
}

private const val MARGIN = 56f
private const val FOOTER_HEIGHT = 28f

private const val TITLE_SIZE = 26f
private const val SUBTITLE_SIZE = 10.5f
private const val HEADING_SIZE = 15f
private const val BODY_SIZE = 11f
private const val FOOTER_SIZE = 9f

private const val BODY_LINE_SPACING = 1.3f
private const val TIGHT_LINE_SPACING = 1.1f

private const val INK = 0xFF1A1A1AL
private const val MUTED = 0xFF6B6B6BL

private class Cursor(
    private val target: PageTarget,
    private val pageWidth: Int,
    private val pageHeight: Int
) {

    private val contentWidth = (pageWidth - 2 * MARGIN).toInt().coerceAtLeast(1)
    private val contentBottom = pageHeight - MARGIN - FOOTER_HEIGHT

    private var pageNumber = 1
    private var canvas: Canvas = target.startPage(pageNumber)
    private var y = MARGIN

    private fun breakPage() {
        drawFooter()
        target.finishPage()
        pageNumber++
        canvas = target.startPage(pageNumber)
        y = MARGIN
    }

    fun finish() {
        drawFooter()
        target.finishPage()
    }

    private fun drawFooter() {
        val paint = TextPaint().apply {
            isAntiAlias = true
            textSize = FOOTER_SIZE
            color = MUTED.toInt()
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            pageNumber.toString(),
            pageWidth / 2f,
            pageHeight - MARGIN + FOOTER_SIZE,
            paint
        )
    }

    fun draw(block: Block) {
        if (block.text.isBlank()) return
        y += spaceBefore(block.style)
        drawFlowing(layoutFor(block))
        y += spaceAfter(block.style)
    }

    private fun drawFlowing(layout: StaticLayout) {
        var line = 0
        while (line < layout.lineCount) {
            val top = layout.getLineTop(line)

            var end = line
            while (end < layout.lineCount &&
                y + (layout.getLineBottom(end) - top) <= contentBottom
            ) {
                end++
            }

            if (end == line) {
                // Nothing fits in what is left of the page. On a page already
                // written to, move to a fresh one and try again; on a page
                // that is still empty the line is taller than any page can
                // hold, so draw it anyway rather than starting pages forever.
                if (y > MARGIN) {
                    breakPage()
                    continue
                }
                end = line + 1
            }

            val height = (layout.getLineBottom(end - 1) - top).toFloat()
            canvas.save()
            canvas.clipRect(MARGIN, y, MARGIN + contentWidth, y + height)
            canvas.translate(MARGIN, y - top)
            layout.draw(canvas)
            canvas.restore()

            y += height
            line = end
            if (line < layout.lineCount) breakPage()
        }
    }

    private fun layoutFor(block: Block): StaticLayout {
        val paint = paintFor(block.style)
        val spacing = if (block.style == Style.BODY) BODY_LINE_SPACING else TIGHT_LINE_SPACING
        return StaticLayout.Builder
            .obtain(block.text, 0, block.text.length, paint, contentWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, spacing)
            .setIncludePad(false)
            .build()
    }

    private fun paintFor(style: Style): TextPaint = TextPaint().apply {
        isAntiAlias = true
        when (style) {
            Style.TITLE -> {
                textSize = TITLE_SIZE
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                color = INK.toInt()
            }
            Style.SUBTITLE -> {
                textSize = SUBTITLE_SIZE
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
                color = MUTED.toInt()
            }
            Style.HEADING -> {
                textSize = HEADING_SIZE
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                color = INK.toInt()
            }
            Style.BODY -> {
                textSize = BODY_SIZE
                typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
                color = Color.BLACK
            }
        }
    }

    private fun spaceBefore(style: Style): Float = when (style) {
        Style.TITLE -> 0f
        Style.SUBTITLE -> 6f
        Style.HEADING -> 20f
        Style.BODY -> 0f
    }

    private fun spaceAfter(style: Style): Float = when (style) {
        Style.TITLE -> 2f
        Style.SUBTITLE -> 22f
        Style.HEADING -> 8f
        Style.BODY -> 11f
    }
}
