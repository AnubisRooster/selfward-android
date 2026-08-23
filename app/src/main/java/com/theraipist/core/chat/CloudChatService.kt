package com.theraipist.core.chat

import com.theraipist.core.model.Message
import com.theraipist.data.settings.SecureSettings
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

private const val ANTHROPIC_VERSION = "2023-06-01"

class CloudChatService(
    private val client: HttpClient,
    private val secureSettings: SecureSettings
) : ChatService {

    override suspend fun send(messages: List<Message>): String {
        val config = secureSettings.apiConfig()
        if (config.apiKey.isBlank()) throw MissingApiKeyException()
        val response = if (config.provider == Provider.ANTHROPIC) {
            client.post(config.baseUrl.trimEnd('/') + "/messages") {
                header("x-api-key", config.apiKey)
                header("anthropic-version", ANTHROPIC_VERSION)
                contentType(ContentType.Application.Json)
                setBody(AnthropicProtocol.buildRequest(messages, config.model))
            }
        } else {
            client.post(config.baseUrl.trimEnd('/') + "/chat/completions") {
                bearerAuth(config.apiKey)
                setBody(ChatProtocol.buildRequest(messages, config.model))
            }
        }
        checkSuccess(response)
        val body = response.bodyAsText()
        return if (config.provider == Provider.ANTHROPIC) {
            AnthropicProtocol.parseResponse(body)
        } else {
            ChatProtocol.parseResponse(body)
        }
    }

    private suspend fun checkSuccess(response: HttpResponse) {
        if (!response.status.isSuccess()) {
            throw ChatServiceException(
                "Chat request failed (${response.status.value}): ${response.bodyAsText().take(500)}"
            )
        }
    }
}
