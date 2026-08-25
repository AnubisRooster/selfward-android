package com.selfward.core.voice

/**
 * A microphone that is driven by the test rather than by a room.
 *
 * Records how many times it was started and stopped, so the ordering that
 * matters — the mic being shut before a reply is spoken — can be asserted from
 * the outside.
 */
class FakeSpeechSource(private val unavailable: Boolean = false) : SpeechSource {

    var starts = 0
        private set
    var stops = 0
        private set

    /** True between a start and the stop that follows it. */
    var isListening = false
        private set

    private var listener: ((VoiceInput) -> Unit)? = null

    override fun isUnavailable(): Boolean = unavailable

    override fun start(onInput: (VoiceInput) -> Unit) {
        starts++
        isListening = true
        listener = onInput
    }

    override fun stop() {
        stops++
        isListening = false
    }

    /** Delivers an event as the recogniser would. */
    fun emit(input: VoiceInput) {
        listener?.invoke(input)
    }
}
