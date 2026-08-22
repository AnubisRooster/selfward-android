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

    @Serializable
    data class ReqMessage(val role: String, val content: String)

    @Serializable
    data class ChatRequest(val model: String, val messages: List<ReqMessage>, val stream: Boolean = false)

    @Serializable
    data class RespMessage(val role: String, val content: String)

    @Serializable
    data class Choice(val message: RespMessage, val index: Int = 0, val finish_reason: String? = null)

    @Serializable
    data class ChatResponse(val id: String? = null, val choices: List<Choice> = emptyList())

    fun buildRequest(messages: List<Message>, model: String): ChatRequest =
        ChatRequest(
            model = model,
            messages = messages.map { ReqMessage(it.role.name.lowercase(), it.content) },
            stream = false
        )

    fun parseResponse(json: String): String {
        val parsed = Json { ignoreUnknownKeys = true }.decodeFromString<ChatResponse>(json)
        return parsed.choices.firstOrNull()?.message?.content ?: ""
    }
}
