package com.theraipist.data.voice

import com.theraipist.core.voice.SttServiceException
import com.theraipist.core.voice.TtsRequest
import com.theraipist.core.voice.TtsServiceException
import com.theraipist.data.settings.FakeSecureSettings
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
 * Drives both cloud voice services through a real Ktor client. Neither had any
 * coverage, and both previously treated an HTTP error body as valid content.
 */
class CloudVoiceServicesTest {

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

    // ---- TTS ----

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

    // ---- STT ----

    @Test
    fun sttReturnsTranscribedText() = runBlocking {
        val engine = engineReturning("""{"text":"hello there"}""")

        val text = CloudSttService(client(engine), settings()).transcribe(byteArrayOf(1, 2))

        assertEquals("hello there", text)
    }

    /**
     * Previously the raw body was returned when decoding failed, so an auth error
     * was handed back as if the user had spoken the error JSON out loud.
     */
    @Test
    fun sttRaisesInsteadOfReturningAnErrorBodyAsTranscript() {
        val body = """{"error":{"message":"Incorrect API key provided"}}"""
        val engine = engineReturning(body, status = HttpStatusCode.Unauthorized)
        val service = CloudSttService(client(engine), settings())

        val error = runCatching { runBlocking { service.transcribe(byteArrayOf(1, 2)) } }
            .exceptionOrNull()

        assertTrue("expected SttServiceException, got $error", error is SttServiceException)
        assertTrue(error!!.message!!.contains("401"))
    }

    @Test
    fun sttRaisesOnUndecodableSuccessBody() {
        val engine = engineReturning("<html>gateway timeout</html>", contentType = "text/html")
        val service = CloudSttService(client(engine), settings())

        val error = runCatching { runBlocking { service.transcribe(byteArrayOf(1, 2)) } }
            .exceptionOrNull()

        assertTrue("expected SttServiceException, got $error", error is SttServiceException)
        assertTrue(
            "raw body leaked into the transcript",
            !error!!.message!!.contains("<html>")
        )
    }
}
