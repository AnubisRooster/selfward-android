package com.theraipist.core.chat

import com.theraipist.core.model.Message
import com.theraipist.core.settings.SecureSettings
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val ANTHROPIC_VERSION = "2023-06-01"

class CloudChatService(
    private val client: HttpClient,
    private val secureSettings: SecureSettings
) : ChatService {

    override fun sendStreaming(messages: List<Message>): Flow<String> = flow {
        val config = secureSettings.apiConfig()
        if (config.apiKey.isBlank()) throw MissingApiKeyException()
        val isAnthropic = config.provider == Provider.ANTHROPIC

        val response = if (isAnthropic) {
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

        val channel = response.bodyAsChannel()
        val eventLines = mutableListOf<String>()
        while (!channel.isClosedForRead) {
            val line = channel.readUTF8Line() ?: break
            if (line.isNotEmpty()) {
                eventLines += line
                continue
            }
            if (eventLines.isEmpty()) continue
            val payload = SseParser.parseEvent(eventLines.joinToString("\n"))
            eventLines.clear()
            val delta = payload?.let { if (isAnthropic) AnthropicProtocol.parseStreamDelta(it) else ChatProtocol.parseStreamDelta(it) }
            if (!delta.isNullOrEmpty()) emit(delta)
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
