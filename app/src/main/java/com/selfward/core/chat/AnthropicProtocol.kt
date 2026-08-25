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
        val conversation = alternating(messages.filter { it.role != Role.SYSTEM })
            .map { ReqMessage(it.role.name.lowercase(), it.content) }
        return ChatRequest(
            model = model,
            messages = conversation,
            system = system,
            max_tokens = DEFAULT_MAX_TOKENS,
            stream = true
        )
    }

    /**
     * Anthropic's messages must alternate between user and assistant, and must
     * begin with the user. OpenAI-compatible endpoints accept whatever they are
     * given, so a history that is fine for one provider is not automatically
     * fine for the other.
     *
     * Runs of the same role do arise in ordinary use: the app writes the
     * client's message down before it asks for a reply, so every failed send
     * leaves a user turn with no answer after it. A few of those in a row is
     * exactly what a session looks like after a spell of the provider being
     * unreachable, and it would then be the switch to Anthropic that appeared
     * to break.
     *
     * Consecutive turns from the same speaker are joined into one, which is
     * also what they are: two things said in a row without an answer between
     * them. Any assistant turn before the first user turn is dropped, since
     * there is no request it could be answering.
     */
    internal fun alternating(messages: List<Message>): List<Message> {
        val fromFirstUser = messages.dropWhile { it.role != Role.USER }
        return fromFirstUser.fold(mutableListOf()) { acc, message ->
            val previous = acc.lastOrNull()
            if (previous != null && previous.role == message.role) {
                acc[acc.lastIndex] = previous.copy(
                    content = previous.content + "\n\n" + message.content
                )
            } else {
                acc += message
            }
            acc
        }
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
