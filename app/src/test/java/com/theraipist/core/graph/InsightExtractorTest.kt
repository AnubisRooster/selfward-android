package com.theraipist.core.graph

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InsightExtractorTest {

    @Test
    fun extractsMarkedLines() {
        val text = """
            Some reflective text.
            Insight: I avoid vulnerability out of fear.
            Reflection: My anger protects a softer part of me.
            Normal line.
            Takeaway: Small steps build safety.
        """.trimIndent()
        val out = InsightExtractor.extract(text)
        assertEquals(3, out.size)
        assertTrue(out[0].startsWith("I avoid"))
        assertTrue(out.any { it.startsWith("Small steps") })
    }

    @Test
    fun ignoresUnmarkedText() {
        assertEquals(emptyList(), InsightExtractor.extract("just a normal reply with no markers"))
    }
}
