package com.theraipist.core.chat

import com.theraipist.core.model.Message
import com.theraipist.core.model.Role
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class CloudChatService(
    private val client: HttpClient,
    private val config: ApiConfig
) : ChatService {

    @Serializable
    private data class ReqMessage(val role: String, val content: String)

    @Serializable
    internal data class ChatRequest(val model: String, val messages: List<ReqMessage>, val stream: Boolean = false)

    @Serializable
    private data class RespMessage(val role: String, val content: String)

    @Serializable
    private data class Choice(val message: RespMessage, val index: Int = 0, val finish_reason: String? = null)

    @Serializable
    private data class ChatResponse(val id: String? = null, val choices: List<Choice> = emptyList())

    /** Build the OpenAI-compatible request body from domain messages. Visible for tests. */
    internal fun buildRequest(messages: List<Message>, model: String = config.model): ChatRequest =
        ChatRequest(
            model = model,
            messages = messages.map { ReqMessage(it.role.name.lowercase(), it.content) },
            stream = false
        )

    /** Parse a chat/completions JSON response into assistant text ("" if none). Visible for tests. */
    internal fun parseResponse(json: String): String {
        val parsed = Json { ignoreUnknownKeys = true }.decodeFromString<ChatResponse>(json)
        return parsed.choices.firstOrNull()?.message?.content ?: ""
    }

    override suspend fun send(messages: List<Message>): String {
        val requestBody = buildRequest(messages)
        val response = client.post(config.baseUrl.trimEnd('/') + "/chat/completions") {
            bearerAuth(config.apiKey)
            setBody(requestBody)
        }
        return parseResponse(response.bodyAsText())
    }
}
