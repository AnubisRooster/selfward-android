package com.selfward.data.voice

import com.selfward.core.voice.SilenceClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The real countdown.
 *
 * It holds its own scope rather than borrowing the ViewModel's, so it can be
 * injected like anything else. Only one countdown is ever pending — arming
 * replaces whatever was running — and the loop cancels it whenever a turn ends,
 * so nothing is left ticking after voice mode is switched off.
 */
class CoroutineSilenceClock(
    private val scope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) : SilenceClock {

    private var job: Job? = null

    override fun arm(seconds: Double, onElapsed: () -> Unit) {
        job?.cancel()
        job = scope.launch {
            delay((seconds * 1000).toLong())
            onElapsed()
        }
    }

    override fun cancel() {
        job?.cancel()
        job = null
    }
}
