package com.selfward.data.voice

import com.selfward.core.voice.TtsRequest
import com.selfward.core.voice.TtsServiceException
import com.selfward.data.settings.FakeSecureSettings
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Drives the cloud TTS service through a real Ktor client. It had no coverage,
 * and previously handed the caller an HTTP error body as though it were audio.
 */
class CloudTtsServiceTest {

    private fun client(engine: MockEngine) = HttpClient(engine) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    private fun engineReturning(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK,
        contentType: String = "application/json"
    ) = MockEngine {
        respond(
            content = ByteReadChannel(body),
            status = status,
            headers = headersOf(HttpHeaders.ContentType, contentType)
        )
    }

    private fun settings() = FakeSecureSettings(initialApiKey = "test-key")

    @Test
    fun ttsReturnsAudioBytesOnSuccess() = runBlocking {
        val engine = MockEngine {
            respond(
                content = ByteReadChannel(byteArrayOf(1, 2, 3)),
                headers = headersOf(HttpHeaders.ContentType, "audio/mpeg")
            )
        }
        val audio = CloudTtsService(client(engine), settings()).synthesize(TtsRequest(input = "hello"))

        assertArrayEquals(byteArrayOf(1, 2, 3), audio)
    }

    /**
     * The regression that mattered: without a status check the error JSON was
     * returned as though it were audio, so playback failed on undecodable bytes
     * and the user got silence with nothing explaining it.
     */
    @Test
    fun ttsRaisesInsteadOfReturningAnErrorBodyAsAudio() {
        val engine = engineReturning(
            """{"error":{"message":"Incorrect API key provided"}}""",
            status = HttpStatusCode.Unauthorized
        )
        val service = CloudTtsService(client(engine), settings())

        val error = runCatching { runBlocking { service.synthesize(TtsRequest(input = "hi")) } }
            .exceptionOrNull()

        assertTrue("expected TtsServiceException, got $error", error is TtsServiceException)
        assertTrue(error!!.message!!.contains("401"))
        assertTrue(error.message!!.contains("Incorrect API key"))
    }

    @Test
    fun ttsRaisesOnServerError() {
        val engine = engineReturning("upstream exploded", status = HttpStatusCode.BadGateway)
        val service = CloudTtsService(client(engine), settings())

        val error = runCatching { runBlocking { service.synthesize(TtsRequest(input = "hi")) } }
            .exceptionOrNull()

        assertTrue(error is TtsServiceException)
        assertTrue(error!!.message!!.contains("502"))
    }
}
