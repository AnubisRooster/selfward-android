package com.selfward.core.chat

import com.selfward.core.model.Message
import com.selfward.core.model.Role
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

    /**
     * [stream] carries no default on purpose.
     *
     * It had one, and kotlinx.serialization omits any value equal to its
     * default, so "stream" was never written into the body at all. Every
     * request the app has ever sent asked for an ordinary completion while the
     * client parsed the reply as an event stream — no `data:` lines, no deltas,
     * and the client was told the model sent nothing back. Without a default
     * the field cannot go missing again.
     */
    @Serializable
    data class ChatRequest(val model: String, val messages: List<ReqMessage>, val stream: Boolean)

    // Non-streaming reply shape, for the fallback path.
    @Serializable
    data class WholeMessage(val content: String? = null)

    @Serializable
    data class WholeChoice(val message: WholeMessage = WholeMessage())

    @Serializable
    data class WholeResponse(val choices: List<WholeChoice> = emptyList())

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

    fun buildRequest(messages: List<Message>, model: String, stream: Boolean = true): ChatRequest =
        ChatRequest(
            model = model,
            messages = messages.map { ReqMessage(it.role.name.lowercase(), it.content) },
            stream = stream
        )

    /** The whole reply from a non-streaming response. */
    fun parseWholeReply(body: String): String? =
        runCatching { json.decodeFromString<WholeResponse>(body) }
            .getOrNull()
            ?.choices
            ?.firstOrNull()
            ?.message
            ?.content
            ?.takeIf { it.isNotBlank() }

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
