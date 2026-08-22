package com.theraipist.core.chat

import com.theraipist.core.model.Message
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText

class CloudChatService(
    private val client: HttpClient,
    private val config: ApiConfig
) : ChatService {

    override suspend fun send(messages: List<Message>): String {
        val requestBody = ChatProtocol.buildRequest(messages, config.model)
        val response = client.post(config.baseUrl.trimEnd('/') + "/chat/completions") {
            bearerAuth(config.apiKey)
            setBody(requestBody)
        }
        return ChatProtocol.parseResponse(response.bodyAsText())
    }
}
