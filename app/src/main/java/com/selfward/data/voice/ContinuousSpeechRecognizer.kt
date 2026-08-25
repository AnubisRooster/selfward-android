package com.selfward.data.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.selfward.core.voice.SpeechSource
import com.selfward.core.voice.VoiceInput
import java.util.Locale

/**
 * Turns Android's `SpeechRecognizer` into a stream of [VoiceInput] events.
 *
 * The recogniser is built for one utterance at a time: it stops on its own, and
 * it has to be restarted for every segment. The conversation loop restarts it
 * constantly, so two things matter here that do not matter for a single
 * dictation.
 *
 * **One recogniser at a time.** Each instance holds a live microphone
 * connection. Creating a new one without destroying the old leaves the previous
 * one holding the mic — over a long conversation that is a growing pile of
 * open recognisers, and eventually the new one cannot get the microphone at
 * all.
 *
 * **Late callbacks are dropped.** A recogniser that has been told to stop can
 * still deliver one more result afterwards. Each start is tagged, and anything
 * arriving from a superseded generation is discarded, so a stale transcript
 * cannot land in the middle of the next turn.
 */
class ContinuousSpeechRecognizer(private val context: Context) : SpeechSource {

    private var recognizer: SpeechRecognizer? = null
    private var generation = 0

    /** True when this device has no recogniser to offer at all. */
    override fun isUnavailable(): Boolean = !SpeechRecognizer.isRecognitionAvailable(context)

    /**
     * Starts a fresh segment, replacing any recogniser already running.
     *
     * @param onInput receives every event, on the main thread.
     */
    override fun start(onInput: (VoiceInput) -> Unit) {
        release()
        generation++
        val thisGeneration = generation

        // Guards every callback: a superseded recogniser's last word must not
        // be mistaken for part of the current turn.
        fun current() = thisGeneration == generation

        val created = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer = created
        created.setRecognitionListener(object : RecognitionListener {
            override fun onPartialResults(partialResults: Bundle?) {
                if (!current()) return
                firstResult(partialResults)?.let { onInput(VoiceInput.Partial(it)) }
            }

            override fun onResults(results: Bundle?) {
                if (!current()) return
                val text = firstResult(results)
                onInput(if (text != null) VoiceInput.Final(text) else VoiceInput.RecognizerEnded)
            }

            override fun onError(error: Int) {
                if (!current()) return
                onInput(VoiceInput.RecognizerFailed(heardNothing = isSilence(error)))
            }

            override fun onReadyForSpeech(params: Bundle?) = Unit
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })

        created.startListening(recognizerIntent())
    }

    /**
     * Stops and releases the recogniser, and marks anything still in flight as
     * stale.
     *
     * Bumping the generation here is what makes this safe to call while the app
     * is about to speak: a result already on its way cannot arrive afterwards
     * and be treated as the person talking over the reply.
     */
    override fun stop() {
        generation++
        release()
    }

    private fun release() {
        recognizer?.let {
            runCatching { it.cancel() }
            runCatching { it.destroy() }
        }
        recognizer = null
    }

    private fun recognizerIntent(): Intent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            // Prefer a recogniser that runs on the phone. The loop sends whole
            // turns to whichever model the person chose; it should not also be
            // shipping their voice to a speech service they did not.
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }

    private fun firstResult(bundle: Bundle?): String? =
        bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()
            ?.takeIf { it.isNotBlank() }

    private companion object {
        /**
         * Errors that mean "nobody said anything", as opposed to a real fault.
         * A quiet room must not count towards giving up on the recogniser.
         */
        fun isSilence(error: Int): Boolean = error == SpeechRecognizer.ERROR_NO_MATCH ||
            error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT
    }
}
