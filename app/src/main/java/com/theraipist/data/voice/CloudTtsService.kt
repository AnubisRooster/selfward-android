package com.theraipist.data.voice

import com.theraipist.core.voice.TtsRequest
import com.theraipist.core.voice.TtsService
import com.theraipist.core.voice.TtsServiceException
import com.theraipist.core.settings.SecureSettings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

/**
 * Cloud text-to-speech via an OpenAI-compatible `/audio/speech` endpoint.
 * The request body is built by the engine-free [TtsRequest.toRequestBody].
 */
class CloudTtsService(
    private val client: HttpClient,
    private val secureSettings: SecureSettings
) : TtsService {

    override suspend fun synthesize(request: TtsRequest): ByteArray {
        val apiConfig = secureSettings.apiConfig()
        val url = "${apiConfig.baseUrl.trimEnd('/')}/audio/speech"
        val response = client.post(url) {
            bearerAuth(apiConfig.apiKey)
            contentType(ContentType.Application.Json)
            setBody(request.toRequestBody())
        }
        // Without a status check the JSON error body comes back as if it were
        // audio, and the player just fails to decode it with nothing to explain why.
        if (!response.status.isSuccess()) {
            throw TtsServiceException(
                "Speech request failed (${response.status.value}): ${response.bodyAsText().take(500)}"
            )
        }
        return response.body<ByteArray>()
    }

    override fun close() {
        // HttpClient lifecycle is owned by the caller.
    }
}
