package com.theraipist.data.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import com.theraipist.core.voice.LocalTtsService
import java.util.Locale

/**
 * On-device TTS using the Android `TextToSpeech` engine. Speaks directly to the
 * audio stream (no byte buffer), so it implements the fire-and-forget
 * [LocalTtsService] rather than the byte-oriented [com.theraipist.core.voice.TtsService].
 * Compile-verified by CI; requires a device at runtime.
 */
class AndroidTtsService(context: Context) : LocalTtsService {

    private val tts = TextToSpeech(context) {}

    fun setLanguage(locale: Locale = Locale.US) {
        tts.language = locale
    }

    override fun speak(text: String, onDone: () -> Unit) {
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
