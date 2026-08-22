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
        assertFalse(req.stream)
        assertEquals(2, req.messages.size)
        assertEquals("system", req.messages[0].role)
        assertEquals("sys", req.messages[0].content)
        assertEquals("user", req.messages[1].role)
    }

    @Test
    fun parseResponseExtractsContent() {
        val json = """{"choices":[{"message":{"role":"assistant","content":"Hi there"}}]}"""
        assertEquals("Hi there", ChatProtocol.parseResponse(json))
    }

    @Test
    fun parseResponseReturnsEmptyWhenNoChoices() {
        assertEquals("", ChatProtocol.parseResponse("""{"choices":[]}"""))
    }

    @Test
    fun parseResponseIgnoresUnknownFields() {
        val json = """{"id":"chatcmpl-1","object":"chat.completion","choices":[{"index":0,"message":{"role":"assistant","content":"ok"},"finish_reason":"stop"}]}"""
        assertEquals("ok", ChatProtocol.parseResponse(json))
    }
}
