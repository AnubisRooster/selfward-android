package com.selfward.core.voice

/** On-device, fire-and-forget text-to-speech - speaks directly to the audio stream. */
interface LocalTtsService {
    fun speak(text: String, onDone: () -> Unit = {})

    /**
     * Every English voice this engine can currently offer.
     *
     * Empty before the engine has finished its own async startup - a real
     * `TextToSpeech` only knows its voice list once its init callback has
     * fired, which can be after this is first called.
     */
    fun availableVoices(): List<DeviceVoice>

    /**
     * Switches to the voice named [name], or the engine's own default when
     * null or when no voice by that name is offered.
     *
     * Safe to call before the engine is ready: the request is remembered and
     * applied once it is.
     */
    fun setVoice(name: String?)
}
