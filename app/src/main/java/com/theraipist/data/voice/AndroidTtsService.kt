package com.theraipist.data.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * On-device TTS using the Android `TextToSpeech` engine. Speaks directly to the
 * audio stream (no byte buffer), so it does not implement the byte-oriented
 * [com.theraipist.core.voice.TtsService]. Wired into the Phase 7 UI.
 * Compile-verified by CI; requires a device at runtime.
 */
class AndroidTtsService(context: Context) {

    private val tts = TextToSpeech(context) {}

    fun setLanguage(locale: Locale = Locale.US) {
        tts.language = locale
    }

    fun speak(text: String, onDone: () -> Unit = {}) {
        val id = "tts_${System.currentTimeMillis()}"
        tts.setOnUtteranceProgressListener(object :
            android.speech.tts.UtteranceProgressListener() {
            override fun onDone(utteranceId: String?) = onDone()
            override fun onError(utteranceId: String?) = onDone()
            override fun onStart(utteranceId: String?) {}
        })
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, id)
    }

    fun shutdown() {
        tts.shutdown()
    }
}
