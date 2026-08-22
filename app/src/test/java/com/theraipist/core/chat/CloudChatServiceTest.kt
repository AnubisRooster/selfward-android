package com.theraipist.core.chat

import com.theraipist.core.model.Message
import com.theraipist.core.model.Role
import org.junit.Assert.*
import org.junit.Test

class CloudChatServiceTest {

    @Test
    fun buildRequestMapsRolesAndModel() {
        val config = ApiConfig(Provider.OPENROUTER, "https://openrouter.ai/api/v1", "k", "openai/gpt-4o")
        val service = CloudChatService(HttpClientStub, config)
        val messages = listOf(
            Message("1", Role.SYSTEM, "sys"),
            Message("2", Role.USER, "hi")
        )
        val req = service.buildRequest(messages)
        assertEquals("openai/gpt-4o", req.model)
        assertFalse(req.stream)
        assertEquals(2, req.messages.size)
        assertEquals("system", req.messages[0].role)
        assertEquals("sys", req.messages[0].content)
        assertEquals("user", req.messages[1].role)
    }

    @Test
    fun parseResponseExtractsContent() {
        val service = CloudChatService(HttpClientStub, ApiConfig(Provider.OPENAI, "x", "k", "gpt-4o"))
        val json = """{"choices":[{"message":{"role":"assistant","content":"Hi there"}}]}"""
        assertEquals("Hi there", service.parseResponse(json))
    }

    @Test
    fun parseResponseReturnsEmptyWhenNoChoices() {
        val service = CloudChatService(HttpClientStub, ApiConfig(Provider.OPENAI, "x", "k", "gpt-4o"))
        assertEquals("", service.parseResponse("""{"choices":[]}"""))
    }

    @Test
    fun parseResponseIgnoresUnknownFields() {
        val service = CloudChatService(HttpClientStub, ApiConfig(Provider.OPENAI, "x", "k", "gpt-4o"))
        val json = """{"id":"chatcmpl-1","object":"chat.completion","choices":[{"index":0,"message":{"role":"assistant","content":"ok"},"finish_reason":"stop"}]}"""
        assertEquals("ok", service.parseResponse(json))
    }

    private companion object {
        // ChatService requires an HttpClient; tests only exercise pure helpers,
        // so a no-op engine is sufficient.
        val HttpClientStub = HttpClient(io.ktor.client.engine.mock.MockEngine) { }
    }
}
