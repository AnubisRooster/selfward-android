package com.theraipist.core.chat

import com.theraipist.core.model.Message
import com.theraipist.core.model.Role
import org.junit.Assert.*
import org.junit.Test

class ChatProtocolTest {

    @Test
    fun buildRequestMapsRolesAndModel() {
        val messages = listOf(
            Message("1", Role.SYSTEM, "sys"),
            Message("2", Role.USER, "hi")
        )
        val req = ChatProtocol.buildRequest(messages, "openai/gpt-4o")
        assertEquals("openai/gpt-4o", req.model)
        assertTrue(req.stream)
        assertEquals(2, req.messages.size)
        assertEquals("system", req.messages[0].role)
        assertEquals("sys", req.messages[0].content)
        assertEquals("user", req.messages[1].role)
    }

    @Test
    fun parseStreamDeltaExtractsContent() {
        val json = """{"choices":[{"delta":{"content":"Hi"}}]}"""
        assertEquals("Hi", ChatProtocol.parseStreamDelta(json))
    }

    @Test
    fun parseStreamDeltaReturnsNullWhenNoChoices() {
        assertNull(ChatProtocol.parseStreamDelta("""{"choices":[]}"""))
    }

    @Test
    fun parseStreamDeltaReturnsNullWhenDeltaHasNoContent() {
        val json = """{"choices":[{"delta":{}}]}"""
        assertNull(ChatProtocol.parseStreamDelta(json))
    }

    @Test
    fun parseStreamDeltaIgnoresUnknownFields() {
        val json = """{"id":"chatcmpl-1","object":"chat.completion.chunk","choices":[{"index":0,"delta":{"content":"ok"},"finish_reason":null}]}"""
        assertEquals("ok", ChatProtocol.parseStreamDelta(json))
    }
}
