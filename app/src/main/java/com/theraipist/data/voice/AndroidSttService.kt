package com.theraipist.data.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale
import javax.inject.Inject

/**
 * On-device speech-to-text via Android `SpeechRecognizer`. Streams partial/final
 * results through callbacks rather than the byte-oriented [SttService]. Wired into
 * the Chat screen. Compile-verified by CI; requires a device at runtime and the
 * RECORD_AUDIO permission.
 */
class AndroidSttService @Inject constructor(
    private val context: Context
) {

    private var recognizer: SpeechRecognizer? = null

    fun startListening(
        onPartial: (String) -> Unit = {},
        onFinal: (String) -> Unit = {},
        onError: (Int) -> Unit = {}
    ) {
        val r = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer = r
        r.setRecognitionListener(object : RecognitionListener {
            override fun onPartialResults(partialResults: Bundle?) {
                partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()?.let(onPartial)
            }

            override fun onResults(results: Bundle?) {
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()?.let(onFinal)
            }

            override fun onError(error: Int) = onError(error)
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
        }
        r.startListening(intent)
    }

    fun stopListening() {
        recognizer?.stopListening()
    }

    fun destroy() {
        recognizer?.destroy()
        recognizer = null
    }
}
