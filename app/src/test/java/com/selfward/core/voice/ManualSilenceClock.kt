package com.selfward.core.voice

/** A countdown the test fires by hand, instead of waiting out real seconds. */
class ManualSilenceClock : SilenceClock {

    var armedFor: Double? = null
        private set

    var arms = 0
        private set

    private var onElapsed: (() -> Unit)? = null

    override fun arm(seconds: Double, onElapsed: () -> Unit) {
        arms++
        armedFor = seconds
        this.onElapsed = onElapsed
    }

    override fun cancel() {
        armedFor = null
        onElapsed = null
    }

    val isArmed: Boolean get() = onElapsed != null

    /** Runs the countdown out, as a real pause would. */
    fun elapse() {
        onElapsed?.invoke()
    }
}
