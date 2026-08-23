package com.theraipist.core.voice

/** On-device, fire-and-forget text-to-speech - speaks directly to the audio stream. */
interface LocalTtsService {
    fun speak(text: String, onDone: () -> Unit = {})
}
