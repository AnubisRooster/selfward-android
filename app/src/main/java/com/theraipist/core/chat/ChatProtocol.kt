package com.theraipist.core.chat

import com.theraipist.core.model.Message
import com.theraipist.core.model.Role
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Pure, engine-free OpenAI-compatible chat protocol helpers.
 * Separated from [CloudChatService] so the request/response mapping can be
 * unit-tested without a Ktor engine.
 */
internal object ChatProtocol {

    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    data class ReqMessage(val role: String, val content: String)

    @Serializable
    data class ChatRequest(val model: String, val messages: List<ReqMessage>, val stream: Boolean = true)

    @Serializable
    data class StreamDelta(val content: String? = null)

    @Serializable
    data class StreamChoice(val delta: StreamDelta = StreamDelta())

    @Serializable
    data class StreamChunk(val choices: List<StreamChoice> = emptyList())

    @Serializable
    data class ApiError(val message: String? = null, val type: String? = null)

    @Serializable
    data class ErrorEnvelope(val error: ApiError? = null)

    fun buildRequest(messages: List<Message>, model: String): ChatRequest =
        ChatRequest(
            model = model,
            messages = messages.map { ReqMessage(it.role.name.lowercase(), it.content) }
        )

    /** The incremental text (if any) carried by one `data:` payload of a streaming response. */
    fun parseStreamDelta(payload: String): String? =
        runCatching { json.decodeFromString<StreamChunk>(payload) }
            .getOrNull()
            ?.choices
            ?.firstOrNull()
            ?.delta
            ?.content

    /**
     * The error described by one `data:` payload, if it is an error report rather
     * than a delta. Streaming endpoints answer 200 and then report rate limits,
     * content filtering, and upstream outages in-band, so these have to be read
     * off the stream rather than inferred from the HTTP status.
     */
    fun parseStreamError(payload: String): String? =
        runCatching { json.decodeFromString<ErrorEnvelope>(payload) }
            .getOrNull()
            ?.error
            ?.let { it.message ?: it.type }
}
