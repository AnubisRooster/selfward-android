package com.selfward.core.voice

/**
 * Where spoken input comes from.
 *
 * An interface so the conversation loop can be driven end to end in a test —
 * transcripts, silences, failures and all — without a microphone. The real
 * implementation is `ContinuousSpeechRecognizer`.
 */
interface SpeechSource {

    /** True when the device has no speech recognition to offer. */
    fun isUnavailable(): Boolean

    /** Begins a segment, replacing anything already running. */
    fun start(onInput: (VoiceInput) -> Unit)

    /** Stops and releases, and marks anything still in flight as stale. */
    fun stop()
}
