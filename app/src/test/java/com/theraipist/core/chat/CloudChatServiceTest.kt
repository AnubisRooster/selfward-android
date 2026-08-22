package com.theraipist.core.chat

import com.theraipist.core.model.Message
import com.theraipist.core.model.Role
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class CloudChatServiceTest {

    @Test
    fun parsesAssistantContent() = runTest {
        var auth: String? = null
        var path: String? = null
        val engine = MockEngine { request ->
            auth = request.headers[HttpHeaders.Authorization]
            path = request.url.encodedPath
            respond(
                content = """{"choices":[{"message":{"role":"assistant","content":"Hi there"}}]}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = HttpClient(engine) { install(ContentNegotiation) { json() } }
        val config = ApiConfig(
            provider = Provider.OPENROUTER,
            baseUrl = "https://openrouter.ai/api/v1",
            apiKey = "test-key",
            model = "openai/gpt-4o"
        )
        val service = CloudChatService(client, config)
        val messages = listOf(Message("1", Role.USER, "hi"))
        val result = service.send(messages)
        assertEquals("Hi there", result)
        assertEquals("Bearer test-key", auth)
        assertTrue(path!!.endsWith("/chat/completions"))
    }

    @Test
    fun returnsEmptyWhenNoChoices() = runTest {
        val engine = MockEngine {
            respond(
                content = """{"choices":[]}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = HttpClient(engine) { install(ContentNegotiation) { json() } }
        val service = CloudChatService(client, ApiConfig(Provider.OPENAI, "https://api.openai.com/v1", "k", "gpt-4o"))
        val out = service.send(listOf(Message("1", Role.USER, "hi")))
        assertEquals("", out)
    }
}
