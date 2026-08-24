package com.theraipist.core.lock

/**
 * Stores and verifies the app's numeric PIN.
 *
 * The PIN is a privacy curtain, not data protection: it keeps a session list off
 * a shoulder-surfer's screen. The conversation database itself is not encrypted
 * by the app, so anyone with the unlocked device and developer access can still
 * read it. Nothing in the UI or the privacy policy should imply otherwise.
 */
interface PinStore {
    /** The stored PIN, or null when none has been set. */
    fun load(): String?
    fun save(pin: String)
    fun clear()
}

/** Length of the PIN, matching the iOS app. */
const val PIN_LENGTH = 6

/**
 * PIN setup and verification with brute-force lockout.
 *
 * Mirrors the iOS `PINService`: the PIN is held as entered in Keystore-backed
 * storage (the counterpart of the iOS Keychain) and compared directly, so that
 * both apps behave identically.
 */
class PinService(
    private val store: PinStore,
    private val lockout: PinLockout
) {

    val isPinSet: Boolean get() = store.load() != null

    val isLockedOut: Boolean get() = lockout.isLockedOut

    val lockoutRemainingSeconds: Int get() = lockout.lockoutRemainingSeconds()

    fun save(pin: String) {
        store.save(pin)
        lockout.registerSuccess()
    }

    fun clear() {
        store.clear()
        lockout.registerSuccess()
    }

    /** Verifies [pin], enforcing lockout. Always prefer this over a raw comparison. */
    fun attempt(pin: String): PinAttempt {
        val remaining = lockout.lockoutRemainingSeconds()
        if (remaining > 0) return PinAttempt.LockedOut(remaining)

        return if (store.load() == pin) {
            lockout.registerSuccess()
            PinAttempt.Success
        } else {
            lockout.registerFailure()
        }
    }
}
