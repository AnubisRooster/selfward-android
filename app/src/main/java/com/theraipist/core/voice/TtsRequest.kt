package com.theraipist.core.voice

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val requestJson = Json { encodeDefaults = true }

/**
 * Request for an OpenAI-compatible text-to-speech endpoint (`/audio/speech`).
 * `toRequestBody()` is engine-free so it can be unit-tested without a Ktor client.
 */
@Serializable
data class TtsRequest(
    val model: String = "tts-1",
    val input: String,
    val voice: String = "alloy",
    @SerialName("response_format")
    val responseFormat: String = "mp3",
    val speed: Float = 1.0f
) {
    fun toRequestBody(): String = requestJson.encodeToString(TtsRequest.serializer(), this)
}
