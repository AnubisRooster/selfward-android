package com.selfward.core.chat

import com.selfward.core.model.Message
import com.selfward.core.settings.SecureSettings
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
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

/**
 * How this app identifies itself to OpenRouter.
 *
 * OpenRouter attributes requests to an app by these headers, and its own
 * refusal for a gated model says to plug it into "an app listed on
 * openrouter.ai/apps" - which is this identity. The iOS app has always sent a
 * referer and Android never did, which is one of the two differences between a
 * request that works there and the same request failing here.
 */
private const val APP_REFERER = "https://sites.google.com/view/selfward"
private const val APP_TITLE = "Selfward"

private const val HTTP_TOO_MANY_REQUESTS = 429

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
                identifyApp(config.provider)
                contentType(ContentType.Application.Json)
                setBody(ChatProtocol.buildRequest(messages, config.model))
            }
        }

        // execute { } holds the connection open and hands back a live body channel.
        // The plain post() overload saves the whole body to a ByteArray before it
        // returns, which would collapse the stream into one delayed burst.
        val streamed = runCatching {
            statement.execute { response ->
                checkSuccess(response)
                emitDeltas(response, isAnthropic)
            }
        }

        val failure = streamed.exceptionOrNull() ?: return@flow
        if (isAnthropic || !worthRetryingWithoutStreaming(failure)) throw failure

        // Not every model behind OpenRouter serves a streaming request, and the
        // catalogue does not say which. The iOS app has never streamed at all -
        // it asks for the whole reply and gets one - so a model that refuses to
        // stream here is one that answers perfectly well there. Ask again the
        // way iOS does before giving up.
        emit(wholeReply(config, messages) ?: throw failure)
    }

    /**
     * A rate limit means the account is over its allowance right now, and asking
     * again immediately spends another request against it for nothing. Anything
     * else is worth one attempt the way iOS asks.
     */
    private fun worthRetryingWithoutStreaming(failure: Throwable): Boolean =
        failure is ChatServiceException && failure.status != HTTP_TOO_MANY_REQUESTS

    private suspend fun wholeReply(config: ApiConfig, messages: List<Message>): String? {
        val response = client.post(config.baseUrl.trimEnd('/') + "/chat/completions") {
            bearerAuth(config.apiKey)
            identifyApp(config.provider)
            contentType(ContentType.Application.Json)
            setBody(ChatProtocol.buildRequest(messages, config.model, stream = false))
        }
        val body = response.bodyAsText()
        if (!response.status.isSuccess()) return null
        return ChatProtocol.parseWholeReply(body)
    }

    private fun HttpRequestBuilder.identifyApp(provider: Provider) {
        if (provider != Provider.OPENROUTER) return
        header("HTTP-Referer", APP_REFERER)
        header("X-Title", APP_TITLE)
    }

    private suspend fun FlowCollector<String>.emitDeltas(
        response: HttpResponse,
        isAnthropic: Boolean
    ) {
        val channel = response.bodyAsChannel()
        val eventLines = mutableListOf<String>()
        // Kept so a body that turned out not to be a stream can be read as the
        // error it usually is.
        val everything = StringBuilder()
        var sawEvent = false

        while (true) {
            val line = channel.readUTF8Line()
            if (line == null) {
                // A last event the server didn't terminate with a blank line.
                if (flushEvent(eventLines, isAnthropic)) sawEvent = true
                break
            }
            everything.append(line).append('\n')
            if (line.isEmpty()) {
                if (flushEvent(eventLines, isAnthropic)) sawEvent = true
            } else {
                eventLines += line
            }
        }

        if (!sawEvent) reportNonStreamBody(everything.toString(), isAnthropic)
    }

    /**
     * Reads a body that arrived with no SSE events in it.
     *
     * Providers answer a streaming request with 200 and a plain JSON error when
     * the request was accepted but the model will not serve it — OpenRouter does
     * this for models gated to particular apps, and for rate limits. The status
     * check passes and there is not a single `data:` line to parse, so without
     * this the client is told the model sent nothing back, and the provider's
     * own explanation is discarded on the way past.
     */
    private fun reportNonStreamBody(body: String, isAnthropic: Boolean) {
        val trimmed = body.trim()
        if (trimmed.isEmpty()) return
        val error = if (isAnthropic) {
            AnthropicProtocol.parseStreamError(trimmed)
        } else {
            ChatProtocol.parseStreamError(trimmed)
        }
        if (error != null) throw ChatServiceException("Chat request failed: $error")
    }

    /** @return true when the lines held a real SSE event, parsed or terminating. */
    private suspend fun FlowCollector<String>.flushEvent(
        eventLines: MutableList<String>,
        isAnthropic: Boolean
    ): Boolean {
        if (eventLines.isEmpty()) return false
        val raw = eventLines.joinToString("\n")
        val wasSse = eventLines.any { it.startsWith("data:") }
        eventLines.clear()
        val payload = SseParser.parseEvent(raw) ?: return wasSse

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
        return true
    }

    private suspend fun checkSuccess(response: HttpResponse) {
        if (response.status.isSuccess()) return
        throw ChatServiceException(
            "Chat request failed (${response.status.value}): ${response.bodyAsText().take(500)}",
            status = response.status.value
        )
    }
}
