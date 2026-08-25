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


    /**
     * The shape a session takes after a run of failed sends: the app writes the
     * client's message down before asking for a reply, so each failure leaves a
     * user turn with nothing after it. OpenAI-compatible endpoints accept that;
     * Anthropic requires the roles to alternate.
     */
    @Test
    fun consecutiveTurnsFromTheSameSpeakerAreJoined() {
        val req = AnthropicProtocol.buildRequest(
            listOf(
                Message("1", Role.USER, "hello"),
                Message("2", Role.USER, "are you there"),
                Message("3", Role.USER, "I had a hard day"),
                Message("4", Role.ASSISTANT, "I'm here.")
            ),
            "claude-3-5-haiku-latest"
        )

        assertEquals(2, req.messages.size)
        assertEquals("user", req.messages[0].role)
        assertEquals("assistant", req.messages[1].role)
        assertTrue(req.messages[0].content.contains("hello"))
        assertTrue(req.messages[0].content.contains("I had a hard day"))
    }

    @Test
    fun rolesAlwaysAlternateInTheRequest() {
        val req = AnthropicProtocol.buildRequest(
            listOf(
                Message("1", Role.USER, "a"),
                Message("2", Role.ASSISTANT, "b"),
                Message("3", Role.ASSISTANT, "c"),
                Message("4", Role.USER, "d"),
                Message("5", Role.USER, "e")
            ),
            "claude-3-5-haiku-latest"
        )

        val roles = req.messages.map { it.role }
        assertEquals(listOf("user", "assistant", "user"), roles)
    }

    /** Anthropic requires the conversation to open with the user. */
    @Test
    fun anAssistantTurnBeforeTheFirstUserTurnIsDropped() {
        val req = AnthropicProtocol.buildRequest(
            listOf(
                Message("1", Role.ASSISTANT, "welcome back"),
                Message("2", Role.USER, "hi")
            ),
            "claude-3-5-haiku-latest"
        )

        assertEquals(1, req.messages.size)
        assertEquals("user", req.messages[0].role)
        assertEquals("hi", req.messages[0].content)
    }

    /** A conversation that already alternates must pass through untouched. */
    @Test
    fun anAlreadyAlternatingConversationIsUnchanged() {
        val req = AnthropicProtocol.buildRequest(
            listOf(
                Message("1", Role.SYSTEM, "be kind"),
                Message("2", Role.USER, "a"),
                Message("3", Role.ASSISTANT, "b"),
                Message("4", Role.USER, "c")
            ),
            "claude-3-5-haiku-latest"
        )

        assertEquals(listOf("a", "b", "c"), req.messages.map { it.content })
        assertEquals("be kind", req.system)
    }

    /**
     * Anthropic streams typed events, most of which carry no text. The whole
     * documented sequence is exercised here rather than only the one event the
     * parser cares about, since ignoring the others is the behaviour.
     */
    @Test
    fun aFullAnthropicEventSequenceYieldsOnlyTheText() {
        val events = listOf(
            """{"type":"message_start","message":{"id":"msg_1","role":"assistant","content":[]}}""",
            """{"type":"content_block_start","index":0,"content_block":{"type":"text","text":""}}""",
            """{"type":"ping"}""",
            """{"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":"I'm "}}""",
            """{"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":"here."}}""",
            """{"type":"content_block_stop","index":0}""",
            """{"type":"message_delta","delta":{"stop_reason":"end_turn"}}""",
            """{"type":"message_stop"}"""
        )

        val text = events.mapNotNull { AnthropicProtocol.parseStreamDelta(it) }

        assertEquals(listOf("I'm ", "here."), text)
    }

    @Test
    fun anOverloadedErrorEventIsReported() {
        val error = AnthropicProtocol.parseStreamError(
            """{"type":"error","error":{"type":"overloaded_error","message":"Overloaded"}}"""
        )

        assertEquals("Overloaded", error)
    }

}
