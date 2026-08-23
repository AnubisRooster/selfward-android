package com.theraipist.data.voice

import com.theraipist.core.voice.SttService
import com.theraipist.core.voice.SttServiceException
import com.theraipist.core.settings.SecureSettings
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Cloud speech-to-text via an OpenAI-compatible Whisper `/audio/transcriptions`
 * endpoint (multipart form). Compile-verified by CI; exercises Ktor form APIs.
 */
class CloudSttService(
    private val client: HttpClient,
    private val secureSettings: SecureSettings
) : SttService {

    @Serializable
    private data class TranscriptionResponse(val text: String = "")

    override suspend fun transcribe(audio: ByteArray): String {
        val apiConfig = secureSettings.apiConfig()
        val url = "${apiConfig.baseUrl.trimEnd('/')}/audio/transcriptions"
        val response = client.submitFormWithBinaryData(
            url = url,
            formData = formData {
                append("model", "whisper-1")
                append("response_format", "json")
                append(
                    "file",
                    audio,
                    Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"audio.webm\"")
                    }
                )
            }
        ) {
            bearerAuth(apiConfig.apiKey)
        }
        val body = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw SttServiceException("Transcription failed (${response.status.value}): ${body.take(500)}")
        }
        // Falling back to the raw body here would hand the caller an error
        // payload as though the user had spoken it.
        return runCatching {
            Json { ignoreUnknownKeys = true }.decodeFromString<TranscriptionResponse>(body).text
        }.getOrElse { throw SttServiceException("Could not read the transcription response.") }
    }

    override fun close() {
        // HttpClient lifecycle is owned by the caller.
    }
}
