package com.theraipist.core.lock

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinLockoutTest {

    private class FakeLockoutStore : LockoutStore {
        override var failCount: Int = 0
        override var lockLevel: Int = 0
        override var lockedUntilMillis: Long = 0L
        override fun clear() {
            failCount = 0
            lockLevel = 0
            lockedUntilMillis = 0L
        }
    }

    private class Clock(var nowMillis: Long = 1_000_000L) {
        fun advanceSeconds(seconds: Long) { nowMillis += seconds * 1000 }
    }

    private fun lockout(
        store: FakeLockoutStore = FakeLockoutStore(),
        clock: Clock = Clock()
    ) = Triple(PinLockout(store, now = { clock.nowMillis }), store, clock)

    @Test
    fun fourFailuresJustCountDown() {
        val (subject, _, _) = lockout()
        val results = (1..4).map { subject.registerFailure() }

        assertEquals(
            listOf(4, 3, 2, 1),
            results.map { (it as PinAttempt.Incorrect).attemptsRemaining }
        )
        assertFalse(subject.isLockedOut)
    }

    @Test
    fun fifthFailureLocksOutForThirtySeconds() {
        val (subject, _, _) = lockout()
        repeat(4) { subject.registerFailure() }

        val result = subject.registerFailure()

        assertEquals(30, (result as PinAttempt.LockedOut).secondsRemaining)
        assertTrue(subject.isLockedOut)
    }

    /** Each successive lockout is longer, then holds at fifteen minutes. */
    @Test
    fun lockoutsEscalateThenCap() {
        val (subject, _, _) = lockout()
        assertEquals(0, subject.lockoutDurationSeconds(0))
        assertEquals(30, subject.lockoutDurationSeconds(1))
        assertEquals(60, subject.lockoutDurationSeconds(2))
        assertEquals(300, subject.lockoutDurationSeconds(3))
        assertEquals(900, subject.lockoutDurationSeconds(4))
        assertEquals(900, subject.lockoutDurationSeconds(9))
    }

    @Test
    fun aSecondRoundOfFailuresLocksOutForLonger() {
        val (subject, _, clock) = lockout()
        repeat(5) { subject.registerFailure() }
        clock.advanceSeconds(31)

        repeat(4) { subject.registerFailure() }
        val second = subject.registerFailure()

        assertEquals(60, (second as PinAttempt.LockedOut).secondsRemaining)
    }

    @Test
    fun furtherAttemptsWhileLockedOutDoNotExtendTheLockout() {
        val (subject, _, clock) = lockout()
        repeat(5) { subject.registerFailure() }
        clock.advanceSeconds(10)

        val result = subject.registerFailure()

        assertEquals(20, (result as PinAttempt.LockedOut).secondsRemaining)
        assertEquals(20, subject.lockoutRemainingSeconds())
    }

    @Test
    fun lockoutClearsOnceTheTimeHasPassed() {
        val (subject, _, clock) = lockout()
        repeat(5) { subject.registerFailure() }

        clock.advanceSeconds(30)

        assertFalse(subject.isLockedOut)
        assertEquals(0, subject.lockoutRemainingSeconds())
    }

    @Test
    fun partialSecondsRoundUpSoTheCountdownNeverShowsZeroWhileLocked() {
        val store = FakeLockoutStore()
        val clock = Clock()
        val subject = PinLockout(store, now = { clock.nowMillis })
        repeat(5) { subject.registerFailure() }

        clock.nowMillis += 29_500 // half a second of the 30 remains

        assertEquals(1, subject.lockoutRemainingSeconds())
        assertTrue(subject.isLockedOut)
    }

    /** A correct PIN wipes the escalation, so the next lockout starts at 30s again. */
    @Test
    fun successResetsEscalation() {
        val (subject, store, clock) = lockout()
        repeat(5) { subject.registerFailure() }
        clock.advanceSeconds(31)

        subject.registerSuccess()

        assertEquals(0, store.failCount)
        assertEquals(0, store.lockLevel)
        assertFalse(subject.isLockedOut)

        repeat(4) { subject.registerFailure() }
        val again = subject.registerFailure()
        assertEquals(30, (again as PinAttempt.LockedOut).secondsRemaining)
    }

    @Test
    fun lockoutSurvivesRecreationFromTheSameStore() {
        val store = FakeLockoutStore()
        val clock = Clock()
        repeat(5) { PinLockout(store, now = { clock.nowMillis }).registerFailure() }

        // A new instance reading the same persisted state is still locked out,
        // so restarting the app cannot be used to skip the wait.
        val reborn = PinLockout(store, now = { clock.nowMillis })

        assertTrue(reborn.isLockedOut)
        assertEquals(30, reborn.lockoutRemainingSeconds())
    }
}
