package com.theraipist.data.voice

import com.theraipist.core.chat.ApiConfig
import com.theraipist.core.voice.SttService
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.MultipartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

/**
 * Cloud speech-to-text via an OpenAI-compatible Whisper `/audio/transcriptions`
 * endpoint (multipart form). Compile-verified by CI; exercises Ktor form APIs.
 */
class CloudSttService(
    private val client: HttpClient,
    private val apiConfig: ApiConfig
) : SttService {

    override suspend fun transcribe(audio: ByteArray): String {
        val url = "${apiConfig.baseUrl.trimEnd('/')}/audio/transcriptions"
        val response = client.post(url) {
            bearerAuth(apiConfig.apiKey)
            setBody(
                MultipartFormDataContent(
                    formData {
                        append("model", "whisper-1")
                        append(
                            "file",
                            audio,
                            Headers.build {
                                append(HttpHeaders.ContentDisposition, "filename=\"audio.webm\"")
                            }
                        )
                    }
                )
            )
        }
        return response.bodyAsText()
    }

    override fun close() {
        // HttpClient lifecycle is owned by the caller.
    }
}
