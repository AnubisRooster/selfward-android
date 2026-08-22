package com.theraipist.core.chat

/**
 * Parses Server-Sent Events (SSE) as emitted by OpenAI-compatible streaming
 * chat completions. Each event is separated by a blank line; the payload is on
 * one or more `data:` lines. A `data: [DONE]` event terminates the stream.
 */
object SseParser {

    private const val DATA_PREFIX = "data:"
    private const val DONE = "[DONE]"

    /** Returns the payload of a single SSE event, or null if empty/[DONE]. */
    fun parseEvent(eventText: String): String? {
        val payload = eventText.lines()
            .filter { it.startsWith(DATA_PREFIX) }
            .joinToString("\n") { it.removePrefix(DATA_PREFIX).trim() }
            .trim()
        if (payload == DONE || payload.isEmpty()) return null
        return payload
    }

    /** Parses a full SSE stream (one or more events joined by blank lines). */
    fun parseStream(raw: String): List<String> =
        raw.split("\n\n").mapNotNull { parseEvent(it) }
}
