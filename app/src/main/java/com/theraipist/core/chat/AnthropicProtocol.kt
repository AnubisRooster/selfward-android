package com.theraipist.core.chat

import com.theraipist.core.model.Message
import com.theraipist.core.model.Role
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

    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    data class ReqMessage(val role: String, val content: String)

    @Serializable
    data class ChatRequest(
        val model: String,
        val messages: List<ReqMessage>,
        val system: String? = null,
        val max_tokens: Int = DEFAULT_MAX_TOKENS,
        val stream: Boolean = true
    )

    @Serializable
    data class StreamDelta(val type: String? = null, val text: String? = null)

    @Serializable
    data class StreamEvent(val type: String? = null, val delta: StreamDelta? = null)

    fun buildRequest(messages: List<Message>, model: String): ChatRequest {
        val system = messages.firstOrNull { it.role == Role.SYSTEM }?.content
        val conversation = messages
            .filter { it.role != Role.SYSTEM }
            .map { ReqMessage(it.role.name.lowercase(), it.content) }
        return ChatRequest(model = model, messages = conversation, system = system)
    }

    /** The incremental text (if any) carried by one `data:` payload of a streaming response. */
    fun parseStreamDelta(payload: String): String? {
        val event = runCatching { json.decodeFromString<StreamEvent>(payload) }.getOrNull() ?: return null
        if (event.type != CONTENT_BLOCK_DELTA) return null
        return event.delta?.text
    }
}
