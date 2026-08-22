package com.theraipist.core.chat

import com.theraipist.core.model.Message
import com.theraipist.core.model.Role
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable

class CloudChatService(
    private val client: HttpClient,
    private val config: ApiConfig
) : ChatService {

    @Serializable
    private data class ReqMessage(val role: String, val content: String)

    @Serializable
    private data class ChatRequest(
        val model: String,
        val messages: List<ReqMessage>,
        val stream: Boolean = false
    )

    @Serializable
    private data class RespMessage(val role: String, val content: String)

    @Serializable
    private data class Choice(val message: RespMessage, val index: Int = 0, val finish_reason: String? = null)

    @Serializable
    private data class ChatResponse(val id: String? = null, val choices: List<Choice> = emptyList())

    override suspend fun send(messages: List<Message>): String {
        val requestBody = ChatRequest(
            model = config.model,
            messages = messages.map { ReqMessage(it.role.name.lowercase(), it.content) },
            stream = false
        )
        val response = client.post(config.baseUrl.trimEnd('/') + "/chat/completions") {
            bearerAuth(config.apiKey)
            setBody(requestBody)
        }
        val parsed: ChatResponse = response.body()
        return parsed.choices.firstOrNull()?.message?.content ?: ""
    }
}
