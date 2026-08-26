package com.selfward.core.voice

/**
 * Available cloud TTS voices (OpenAI). Mirrors the portable voice catalog.
 *
 * A fixed list rather than a fetched one: OpenAI's `/audio/speech` endpoint has
 * no "list voices" call, so this is the only source there is.
 */
object VoiceCatalog {
    val openAiVoices: List<String> = listOf("alloy", "echo", "fable", "onyx", "nova", "shimmer")
    fun isKnown(voice: String): Boolean = openAiVoices.contains(voice)
}
