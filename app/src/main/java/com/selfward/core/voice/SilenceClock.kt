package com.selfward.core.voice

/**
 * The countdown that decides a spoken turn has ended.
 *
 * A seam rather than a bare `delay`, for two reasons. It lets a test fire the
 * countdown exactly when it means to, instead of waiting out real seconds. And
 * it keeps the loop's "nobody has said anything yet, wait again" behaviour from
 * becoming an infinite spin under a test scheduler that advances virtual time
 * for itself — which is precisely what happened when this was a plain `delay`.
 */
interface SilenceClock {

    /** Schedules [onElapsed], replacing any countdown already running. */
    fun arm(seconds: Double, onElapsed: () -> Unit)

    fun cancel()
}
