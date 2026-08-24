package com.selfward.core.voice

/** Available cloud TTS voices (OpenAI). Mirrors the portable voice catalog. */
object VoiceCatalog {
    val openAiVoices: List<String> = listOf("alloy", "echo", "fable", "onyx", "nova", "shimmer")
    fun isKnown(voice: String): Boolean = openAiVoices.contains(voice)
}
