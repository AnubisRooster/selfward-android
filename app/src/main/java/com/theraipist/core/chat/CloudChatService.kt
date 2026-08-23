package com.theraipist.core.chat

import com.theraipist.core.model.Message
import com.theraipist.core.settings.SecureSettings
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
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

        val statement = if (isAnthropic) {
            client.preparePost(config.baseUrl.trimEnd('/') + "/messages") {
                header("x-api-key", config.apiKey)
                header("anthropic-version", ANTHROPIC_VERSION)
                contentType(ContentType.Application.Json)
                setBody(AnthropicProtocol.buildRequest(messages, config.model))
            }
        } else {
            client.preparePost(config.baseUrl.trimEnd('/') + "/chat/completions") {
                bearerAuth(config.apiKey)
                contentType(ContentType.Application.Json)
                setBody(ChatProtocol.buildRequest(messages, config.model))
            }
        }

        // execute { } holds the connection open and hands back a live body channel.
        // The plain post() overload saves the whole body to a ByteArray before it
        // returns, which would collapse the stream into one delayed burst.
        statement.execute { response ->
            checkSuccess(response)
            emitDeltas(response, isAnthropic)
        }
    }

    private suspend fun FlowCollector<String>.emitDeltas(
        response: HttpResponse,
        isAnthropic: Boolean
    ) {
        val channel = response.bodyAsChannel()
        val eventLines = mutableListOf<String>()
        while (true) {
            val line = channel.readUTF8Line()
            if (line == null) {
                // A last event the server didn't terminate with a blank line.
                flushEvent(eventLines, isAnthropic)
                break
            }
            if (line.isEmpty()) flushEvent(eventLines, isAnthropic) else eventLines += line
        }
    }

    private suspend fun FlowCollector<String>.flushEvent(
        eventLines: MutableList<String>,
        isAnthropic: Boolean
    ) {
        if (eventLines.isEmpty()) return
        val raw = eventLines.joinToString("\n")
        eventLines.clear()
        val payload = SseParser.parseEvent(raw) ?: return

        val error = if (isAnthropic) {
            AnthropicProtocol.parseStreamError(payload)
        } else {
            ChatProtocol.parseStreamError(payload)
        }
        if (error != null) throw ChatServiceException("Chat request failed: $error")

        val delta = if (isAnthropic) {
            AnthropicProtocol.parseStreamDelta(payload)
        } else {
            ChatProtocol.parseStreamDelta(payload)
        }
        if (!delta.isNullOrEmpty()) emit(delta)
    }

    private suspend fun checkSuccess(response: HttpResponse) {
        if (!response.status.isSuccess()) {
            throw ChatServiceException(
                "Chat request failed (${response.status.value}): ${response.bodyAsText().take(500)}"
            )
        }
    }
}
