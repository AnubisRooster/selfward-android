package com.selfward.core.chat

import com.selfward.core.model.Message
import com.selfward.core.model.Role
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Pure, engine-free Anthropic Messages API protocol helpers. Anthropic's wire
 * format differs from the OpenAI-compatible one in [ChatProtocol]: the system
 * prompt is a top-level field (not a "system"-role message), and a streaming
 * response is a sequence of typed events (message_start, content_block_delta,
 * message_stop, ...) rather than one message-shaped chunk per event.
 */
internal object AnthropicProtocol {

    private const val DEFAULT_MAX_TOKENS = 1024
    private const val CONTENT_BLOCK_DELTA = "content_block_delta"
    private const val ERROR = "error"

    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    data class ReqMessage(val role: String, val content: String)

    /**
     * [max_tokens] and [stream] carry no defaults on purpose.
     *
     * kotlinx.serialization omits any value equal to its default, so both were
     * silently dropped from every request. Anthropic requires max_tokens and
     * rejects a request without it, and the missing stream flag meant the reply
     * came back as one JSON body while the client parsed it as an event stream.
     * [system] keeps its null default, because there it is right for the field
     * to be absent rather than sent as null.
     */
    @Serializable
    data class ChatRequest(
        val model: String,
        val messages: List<ReqMessage>,
        val system: String? = null,
        val max_tokens: Int,
        val stream: Boolean
    )

    @Serializable
    data class StreamDelta(val type: String? = null, val text: String? = null)

    @Serializable
    data class ApiError(val message: String? = null, val type: String? = null)

    @Serializable
    data class StreamEvent(
        val type: String? = null,
        val delta: StreamDelta? = null,
        val error: ApiError? = null
    )

    fun buildRequest(messages: List<Message>, model: String): ChatRequest {
        val system = messages.firstOrNull { it.role == Role.SYSTEM }?.content
        val conversation = messages
            .filter { it.role != Role.SYSTEM }
            .map { ReqMessage(it.role.name.lowercase(), it.content) }
        return ChatRequest(
            model = model,
            messages = conversation,
            system = system,
            max_tokens = DEFAULT_MAX_TOKENS,
            stream = true
        )
    }

    /** The incremental text (if any) carried by one `data:` payload of a streaming response. */
    fun parseStreamDelta(payload: String): String? {
        val event = runCatching { json.decodeFromString<StreamEvent>(payload) }.getOrNull() ?: return null
        if (event.type != CONTENT_BLOCK_DELTA) return null
        return event.delta?.text
    }

    /**
     * The error described by one `data:` payload, if it is an `error` event rather
     * than a delta. Anthropic answers 200 and then reports overload, rate limits,
     * and content filtering in-band, so these have to be read off the stream rather
     * than inferred from the HTTP status.
     */
    fun parseStreamError(payload: String): String? {
        val event = runCatching { json.decodeFromString<StreamEvent>(payload) }.getOrNull() ?: return null
        if (event.type != ERROR) return null
        return event.error?.let { it.message ?: it.type } ?: ERROR
    }
}
