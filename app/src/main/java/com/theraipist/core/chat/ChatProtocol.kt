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
}
