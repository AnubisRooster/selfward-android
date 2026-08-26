package com.selfward.data.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import com.selfward.core.voice.DeviceVoice
import com.selfward.core.voice.DeviceVoiceRanking
import com.selfward.core.voice.LocalTtsService
import java.util.Locale

/**
 * On-device TTS using the Android `TextToSpeech` engine. Speaks directly to the
 * audio stream (no byte buffer), so it implements the fire-and-forget
 * [LocalTtsService] rather than the byte-oriented [com.selfward.core.voice.TtsService].
 * Compile-verified by CI; requires a device at runtime.
 *
 * `TextToSpeech`'s own construction is asynchronous - the engine is not ready,
 * and `tts.voices` is not populated, until [TextToSpeech.OnInitListener] fires.
 * A `setVoice` call made before then (as happens whenever the app opens with a
 * voice already chosen from a previous run - the engine remembers nothing
 * across a process restart) is remembered in [pendingVoiceName] and applied the
 * moment init completes, rather than silently doing nothing.
 */
class AndroidTtsService(context: Context) : LocalTtsService {

    private var ready = false
    private var pendingVoiceName: String? = null

    private val tts = TextToSpeech(context) { status ->
        if (status == TextToSpeech.SUCCESS) {
            ready = true
            applyVoice(pendingVoiceName)
        }
    }

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

    override fun availableVoices(): List<DeviceVoice> {
        if (!ready) return emptyList()
        val voices = tts.voices.orEmpty().map {
            DeviceVoice(it.name, it.locale.toString(), DeviceVoiceRanking.tierFor(it.quality))
        }
        return DeviceVoiceRanking.englishVoices(voices)
    }

    override fun setVoice(name: String?) {
        pendingVoiceName = name
        if (ready) applyVoice(name)
    }

    private fun applyVoice(name: String?) {
        val match: Voice? = name?.let { wanted -> tts.voices?.firstOrNull { it.name == wanted } }
        tts.voice = match ?: tts.defaultVoice
    }

    fun shutdown() {
        tts.shutdown()
    }
}
