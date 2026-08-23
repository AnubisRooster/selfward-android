package com.theraipist.core.chat

import com.theraipist.core.model.Message
import com.theraipist.core.model.Role
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Pure, engine-free Anthropic Messages API protocol helpers. Anthropic's wire
 * format differs from the OpenAI-compatible one in [ChatProtocol]: the system
 * prompt is a top-level field (not a "system"-role message), and the response
 * content is a list of typed blocks rather than `choices[].message`.
 */
internal object AnthropicProtocol {

    private const val DEFAULT_MAX_TOKENS = 1024

    @Serializable
    data class ReqMessage(val role: String, val content: String)

    @Serializable
    data class ChatRequest(
        val model: String,
        val messages: List<ReqMessage>,
        val system: String? = null,
        val max_tokens: Int = DEFAULT_MAX_TOKENS
    )

    @Serializable
    data class ContentBlock(val type: String, val text: String? = null)

    @Serializable
    data class ChatResponse(val content: List<ContentBlock> = emptyList())

    fun buildRequest(messages: List<Message>, model: String): ChatRequest {
        val system = messages.firstOrNull { it.role == Role.SYSTEM }?.content
        val conversation = messages
            .filter { it.role != Role.SYSTEM }
            .map { ReqMessage(it.role.name.lowercase(), it.content) }
        return ChatRequest(model = model, messages = conversation, system = system)
    }

    fun parseResponse(json: String): String {
        val parsed = Json { ignoreUnknownKeys = true }.decodeFromString<ChatResponse>(json)
        return parsed.content.firstOrNull { it.type == "text" }?.text ?: ""
    }
}
