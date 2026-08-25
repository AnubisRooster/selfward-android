package com.selfward.core.chat

import com.selfward.core.model.Message
import com.selfward.core.model.Role
import org.junit.Assert.*
import kotlinx.serialization.json.Json
import org.junit.Assert.assertTrue
import org.junit.Test

class AnthropicProtocolTest {

    @Test
    fun buildRequestSeparatesSystemPromptAndMapsRoles() {
        val messages = listOf(
            Message("1", Role.SYSTEM, "sys"),
            Message("2", Role.USER, "hi"),
            Message("3", Role.ASSISTANT, "hello")
        )
        val req = AnthropicProtocol.buildRequest(messages, "claude-3-5-sonnet")
        assertEquals("claude-3-5-sonnet", req.model)
        assertEquals("sys", req.system)
        assertEquals(2, req.messages.size)
        assertEquals("user", req.messages[0].role)
        assertEquals("hi", req.messages[0].content)
        assertEquals("assistant", req.messages[1].role)
    }

    @Test
    fun buildRequestHandlesNoSystemMessage() {
        val messages = listOf(Message("1", Role.USER, "hi"))
        val req = AnthropicProtocol.buildRequest(messages, "claude-3-5-sonnet")
        assertNull(req.system)
        assertEquals(1, req.messages.size)
    }

    @Test
    fun parseStreamDeltaExtractsTextFromContentBlockDelta() {
        val json = """{"type":"content_block_delta","delta":{"type":"text_delta","text":"Hi there"}}"""
        assertEquals("Hi there", AnthropicProtocol.parseStreamDelta(json))
    }

    @Test
    fun parseStreamDeltaReturnsNullForOtherEventTypes() {
        val json = """{"type":"message_start","message":{"id":"msg_1"}}"""
        assertNull(AnthropicProtocol.parseStreamDelta(json))
    }

    @Test
    fun parseStreamDeltaReturnsNullWhenDeltaMissing() {
        val json = """{"type":"content_block_delta"}"""
        assertNull(AnthropicProtocol.parseStreamDelta(json))
    }

    /**
     * Anthropic requires max_tokens and rejects a request without it. Both it
     * and the stream flag had default values, so kotlinx.serialization dropped
     * them from every request the app sent.
     */
    @Test
    fun theRequestCarriesTheFieldsAnthropicRequires() {
        val body = Json.encodeToString(
            AnthropicProtocol.ChatRequest.serializer(),
            AnthropicProtocol.buildRequest(listOf(Message("1", Role.USER, "hi")), "claude-3-5-haiku-latest")
        )

        assertTrue("max_tokens missing from $body", body.contains("\"max_tokens\""))
        assertTrue("stream flag missing from $body", body.contains("\"stream\":true"))
    }

    /** An absent system prompt should be absent, not sent as null. */
    @Test
    fun noSystemPromptMeansNoSystemField() {
        val body = Json.encodeToString(
            AnthropicProtocol.ChatRequest.serializer(),
            AnthropicProtocol.buildRequest(listOf(Message("1", Role.USER, "hi")), "claude-3-5-haiku-latest")
        )

        assertTrue("system should be omitted, got $body", !body.contains("\"system\""))
    }

}
