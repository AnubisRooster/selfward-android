package com.theraipist.data.voice

import com.theraipist.core.chat.ApiConfig
import com.theraipist.core.voice.TtsRequest
import com.theraipist.core.voice.TtsService
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Cloud text-to-speech via an OpenAI-compatible `/audio/speech` endpoint.
 * The request body is built by the engine-free [TtsRequest.toRequestBody].
 */
class CloudTtsService(
    private val client: HttpClient,
    private val apiConfig: ApiConfig
) : TtsService {

    override suspend fun synthesize(request: TtsRequest): ByteArray {
        val url = "${apiConfig.baseUrl.trimEnd('/')}/audio/speech"
        return client.post(url) {
            bearerAuth(apiConfig.apiKey)
            contentType(ContentType.Application.Json)
            setBody(request.toRequestBody())
        }.bodyAsBytes()
    }

    override fun close() {
        // HttpClient lifecycle is owned by the caller.
    }
}
