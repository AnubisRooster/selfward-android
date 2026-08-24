package com.selfward.core.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SseParserTest {

    @Test
    fun parsesSingleEvent() {
        val raw = "data: {\"choices\":[{\"delta\":{\"content\":\"hi\"}}]}\n\n"
        val out = SseParser.parseStream(raw)
        assertEquals(1, out.size)
        assertTrue(out[0].contains("\"content\":\"hi\""))
    }

    @Test
    fun skipsDoneEvent() {
        val raw = "data: {\"x\":1}\n\ndata: [DONE]\n\n"
        val out = SseParser.parseStream(raw)
        assertEquals(1, out.size)
        assertEquals("{\"x\":1}", out[0])
    }

    @Test
    fun parsesMultipleEvents() {
        val raw = "data: a\n\ndata: b\n\ndata: c\n\n"
        assertEquals(listOf("a", "b", "c"), SseParser.parseStream(raw))
    }

    @Test
    fun emptyEventYieldsNull() {
        assertNull(SseParser.parseEvent("\n\n"))
    }

    @Test
    fun handlesMultilineData() {
        val raw = "data: line1\ndata: line2\n\n"
        assertEquals("line1\nline2", SseParser.parseEvent(raw))
    }
}
