package com.theraipist.core.chat

import com.theraipist.core.model.Message
import com.theraipist.core.model.Role
import com.theraipist.data.settings.FakeSecureSettings
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Exercises [CloudChatService] through a real Ktor client (MockEngine) rather than
 * a fake [ChatService], because the parts most likely to break — whether the body
 * is actually streamed, and how SSE framing is handled — live in the Ktor call
 * itself and are invisible to a fake that just returns a Flow.
 */
class CloudChatServiceTest {

    private val userMessage = listOf(Message("1", Role.USER, "hi"))

    private fun clientReturning(engine: MockEngine) = HttpClient(engine) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    private fun sseEngine(body: String) = MockEngine {
        respond(
            content = ByteReadChannel(body),
            headers = headersOf(HttpHeaders.ContentType, "text/event-stream")
        )
    }

    private fun service(engine: MockEngine, provider: Provider = Provider.OPENAI) =
        CloudChatService(
            clientReturning(engine),
            FakeSecureSettings(initialProvider = provider, initialApiKey = "test-key")
        )

    /**
     * The regression guard for the whole feature: deltas must reach the collector
     * while the response body is still open. The writer refuses to send its second
     * event until the first delta has been observed, so an implementation that
     * buffers the body before parsing (e.g. `client.post()` instead of
     * `preparePost().execute { }`) deadlocks and fails on the timeout.
     */
    @Test
    fun emitsEachDeltaBeforeTheResponseBodyIsComplete() = runBlocking {
        val body = ByteChannel(autoFlush = true)
        val engine = MockEngine {
            respond(content = body, headers = headersOf(HttpHeaders.ContentType, "text/event-stream"))
        }
        val firstDeltaSeen = CompletableDeferred<Unit>()

        val writer = launch {
            body.writeStringUtf8("""data: {"choices":[{"delta":{"content":"Hello"}}]}""" + "\n\n")
            firstDeltaSeen.await()
            body.writeStringUtf8("""data: {"choices":[{"delta":{"content":" there"}}]}""" + "\n\n")
            body.writeStringUtf8("data: [DONE]\n\n")
            body.close(null)
        }

        val deltas = mutableListOf<String>()
        withTimeout(10_000) {
            service(engine).sendStreaming(userMessage).collect {
                deltas += it
                firstDeltaSeen.complete(Unit)
            }
        }
        writer.join()

        assertEquals(listOf("Hello", " there"), deltas)
    }

    @Test
    fun parsesAnthropicEventAndDataLinePairs() = runBlocking {
        val body = buildString {
            append("event: message_start\n")
            append("""data: {"type":"message_start","message":{"id":"m1"}}""").append("\n\n")
            append("event: content_block_delta\n")
            append("""data: {"type":"content_block_delta","delta":{"type":"text_delta","text":"Hi"}}""").append("\n\n")
            append("event: message_stop\n")
            append("""data: {"type":"message_stop"}""").append("\n\n")
        }

        val deltas = service(sseEngine(body), Provider.ANTHROPIC).sendStreaming(userMessage).toList()

        assertEquals(listOf("Hi"), deltas)
    }

    /** A `: ping` comment and a bare `id:` line must not be mistaken for payload. */
    @Test
    fun ignoresKeepaliveAndNonDataLines() = runBlocking {
        val body = buildString {
            append(": ping\n\n")
            append("id: 42\n")
            append("""data: {"choices":[{"delta":{"content":"ok"}}]}""").append("\n\n")
            append("data: [DONE]\n\n")
        }

        val deltas = service(sseEngine(body)).sendStreaming(userMessage).toList()

        assertEquals(listOf("ok"), deltas)
    }

    /** Some servers close without a terminating blank line; the last delta still counts. */
    @Test
    fun doesNotDropAFinalEventWithoutATrailingBlankLine() = runBlocking {
        val body = """data: {"choices":[{"delta":{"content":"first"}}]}""" + "\n\n" +
            """data: {"choices":[{"delta":{"content":" last"}}]}""" + "\n"

        val deltas = service(sseEngine(body)).sendStreaming(userMessage).toList()

        assertEquals(listOf("first", " last"), deltas)
    }

    /**
     * Streaming endpoints answer 200 and then report failures in-band. Swallowing
     * these as "no delta" would show the user a blank reply with no error at all.
     */
    @Test
    fun surfacesAnthropicInBandErrorEvent() {
        val body = buildString {
            append("event: error\n")
            append("""data: {"type":"error","error":{"type":"overloaded_error","message":"Overloaded"}}""").append("\n\n")
        }

        val error = runCatching {
            runBlocking { service(sseEngine(body), Provider.ANTHROPIC).sendStreaming(userMessage).toList() }
        }.exceptionOrNull()

        assertTrue("expected ChatServiceException, got $error", error is ChatServiceException)
        assertTrue(error!!.message!!.contains("Overloaded"))
    }

    @Test
    fun surfacesOpenAiInBandErrorEvent() {
        val body = """data: {"error":{"message":"Rate limit reached","type":"rate_limit_error"}}""" + "\n\n"

        val error = runCatching {
            runBlocking { service(sseEngine(body)).sendStreaming(userMessage).toList() }
        }.exceptionOrNull()

        assertTrue("expected ChatServiceException, got $error", error is ChatServiceException)
        assertTrue(error!!.message!!.contains("Rate limit reached"))
    }

    @Test
    fun surfacesHttpFailureStatus() {
        val engine = MockEngine {
            respond(
                content = ByteReadChannel("""{"error":"nope"}"""),
                status = HttpStatusCode.TooManyRequests,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val error = runCatching {
            runBlocking { service(engine).sendStreaming(userMessage).toList() }
        }.exceptionOrNull()

        assertTrue(error is ChatServiceException)
        assertTrue(error!!.message!!.contains("429"))
    }

    @Test
    fun throwsMissingApiKeyWhenNoKeyIsConfigured() {
        val service = CloudChatService(
            clientReturning(sseEngine("")),
            FakeSecureSettings(initialApiKey = null)
        )

        val error = runCatching {
            runBlocking { service.sendStreaming(userMessage).toList() }
        }.exceptionOrNull()

        assertTrue(error is MissingApiKeyException)
    }

    /** ContentNegotiation silently skips serialization when no Content-Type is set. */
    @Test
    fun sendsJsonContentTypeOnBothProviderBranches() = runBlocking {
        for (provider in listOf(Provider.OPENAI, Provider.ANTHROPIC)) {
            val engine = sseEngine("data: [DONE]\n\n")
            service(engine, provider).sendStreaming(userMessage).toList()

            val request = engine.requestHistory.single()
            assertEquals(
                "missing JSON content type for $provider",
                "application/json",
                request.body.contentType?.let { "${it.contentType}/${it.contentSubtype}" }
            )
        }
    }
}
