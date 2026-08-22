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
        assertEquals("I avoid vulnerability out of fear.", out[0])
        assertEquals("My anger protects a softer part of me.", out[1])
        assertEquals("Small steps build safety.", out[2])
    }

    @Test
    fun ignoresUnmarkedText() {
        assertTrue(InsightExtractor.extract("just a normal reply with no markers").isEmpty())
    }
}
