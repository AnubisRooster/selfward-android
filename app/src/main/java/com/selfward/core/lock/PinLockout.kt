package com.selfward.core.lock

/** Outcome of a single PIN entry. */
sealed interface PinAttempt {
    data object Success : PinAttempt
    data class Incorrect(val attemptsRemaining: Int) : PinAttempt
    data class LockedOut(val secondsRemaining: Int) : PinAttempt
}

/** The small amount of state the lockout needs to survive process death. */
interface LockoutStore {
    var failCount: Int
    var lockLevel: Int
    /** Epoch millis until which entry is locked out; 0 when not locked out. */
    var lockedUntilMillis: Long
    fun clear()
}

/**
 * Brute-force lockout for the PIN gate, mirroring the iOS `PINLockout`.
 *
 * Kept free of Android and of the PIN itself so the escalation can be tested
 * against an injected clock: five wrong entries trigger a lockout, and each
 * subsequent lockout lasts longer — 30s, 60s, 5m, then 15m for every one after.
 */
class PinLockout(
    private val store: LockoutStore,
    private val maxAttempts: Int = MAX_ATTEMPTS,
    private val now: () -> Long = System::currentTimeMillis
) {

    fun lockoutDurationSeconds(level: Int): Int = when {
        level < 1 -> 0
        level == 1 -> 30
        level == 2 -> 60
        level == 3 -> 300
        else -> 900
    }

    /** Seconds left in the current lockout, or 0 when entry is allowed. */
    fun lockoutRemainingSeconds(): Int {
        val remainingMillis = store.lockedUntilMillis - now()
        if (remainingMillis <= 0) return 0
        // Round up so a partially-elapsed second still reads as time remaining.
        return ((remainingMillis + MILLIS_PER_SECOND - 1) / MILLIS_PER_SECOND).toInt()
    }

    val isLockedOut: Boolean get() = lockoutRemainingSeconds() > 0

    /** Records a wrong PIN and reports whether that tipped into a lockout. */
    fun registerFailure(): PinAttempt {
        val alreadyLocked = lockoutRemainingSeconds()
        if (alreadyLocked > 0) return PinAttempt.LockedOut(alreadyLocked)

        val fails = store.failCount + 1
        if (fails < maxAttempts) {
            store.failCount = fails
            return PinAttempt.Incorrect(attemptsRemaining = maxAttempts - fails)
        }

        val level = store.lockLevel + 1
        val duration = lockoutDurationSeconds(level)
        store.lockLevel = level
        store.lockedUntilMillis = now() + duration * MILLIS_PER_SECOND
        store.failCount = 0
        return PinAttempt.LockedOut(secondsRemaining = duration)
    }

    /** Clears failure and escalation state after a correct PIN. */
    fun registerSuccess() = store.clear()

    private companion object {
        const val MAX_ATTEMPTS = 5
        const val MILLIS_PER_SECOND = 1000L
    }
}
