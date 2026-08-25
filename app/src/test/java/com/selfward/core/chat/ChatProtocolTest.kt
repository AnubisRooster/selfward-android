package com.selfward.core.chat

import com.selfward.core.model.Message
import com.selfward.core.model.Role
import org.junit.Assert.*
import kotlinx.serialization.json.Json
import org.junit.Assert.assertTrue
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

    /**
     * The bug that made cloud chat impossible on Android. `stream` had a default
     * value, kotlinx.serialization omits anything equal to its default, and so
     * the flag was never written into the body. Every request asked for an
     * ordinary completion while the client parsed the reply as an event stream,
     * found no `data:` lines, and reported that the model sent nothing back.
     *
     * Every test here fed the parser a stream without ever checking that one had
     * been asked for, which is how it survived.
     */
    @Test
    fun theRequestActuallyAsksForAStream() {
        val body = Json.encodeToString(
            ChatProtocol.ChatRequest.serializer(),
            ChatProtocol.buildRequest(listOf(Message("1", Role.USER, "hi")), "v/m:free")
        )

        assertTrue("stream flag missing from $body", body.contains("\"stream\":true"))
    }

    @Test
    fun aNonStreamingRequestSaysSoExplicitly() {
        val body = Json.encodeToString(
            ChatProtocol.ChatRequest.serializer(),
            ChatProtocol.buildRequest(listOf(Message("1", Role.USER, "hi")), "v/m:free", stream = false)
        )

        assertTrue("stream flag missing from $body", body.contains("\"stream\":false"))
    }

    @Test
    fun theWholeReplyIsReadFromANonStreamingResponse() {
        val reply = ChatProtocol.parseWholeReply(
            """{"choices":[{"message":{"role":"assistant","content":"here it is"}}]}"""
        )

        assertEquals("here it is", reply)
    }

    @Test
    fun anEmptyNonStreamingReplyIsNothingRatherThanBlank() {
        assertEquals(null, ChatProtocol.parseWholeReply("""{"choices":[]}"""))
        assertEquals(null, ChatProtocol.parseWholeReply("""{"choices":[{"message":{"content":""}}]}"""))
        assertEquals(null, ChatProtocol.parseWholeReply("not json"))
    }

}
