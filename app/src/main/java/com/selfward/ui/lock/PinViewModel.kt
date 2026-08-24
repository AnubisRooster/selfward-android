package com.selfward.ui.lock

import androidx.lifecycle.ViewModel
import com.selfward.core.lock.PIN_LENGTH
import com.selfward.core.lock.PinAttempt
import com.selfward.core.lock.PinService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/** Which question the screen is currently asking. */
enum class PinMode { SETUP, CONFIRM, UNLOCK }

data class PinUiState(
    val mode: PinMode = PinMode.UNLOCK,
    val entered: String = "",
    val message: String = "",
    val shaking: Boolean = false,
    val lockedOut: Boolean = false,
    val unlocked: Boolean = false
) {
    val heading: String
        get() = when (mode) {
            PinMode.SETUP -> "Create a PIN"
            PinMode.CONFIRM -> "Confirm your PIN"
            PinMode.UNLOCK -> "Enter your PIN"
        }

    /** The honest note about what a PIN does only belongs on first setup. */
    val showsPrivacyNote: Boolean get() = mode == PinMode.SETUP
}

@HiltViewModel
class PinViewModel @Inject constructor(
    private val pinService: PinService
) : ViewModel() {

    private var firstEntry: String = ""

    private val _uiState = MutableStateFlow(
        PinUiState(
            mode = if (pinService.isPinSet) PinMode.UNLOCK else PinMode.SETUP,
            lockedOut = pinService.isLockedOut,
            message = lockoutMessage(pinService.lockoutRemainingSeconds)
        )
    )
    val uiState = _uiState.asStateFlow()

    fun append(digit: Char) {
        val state = _uiState.value
        if (state.lockedOut || state.entered.length >= PIN_LENGTH) return

        val entered = state.entered + digit
        _uiState.update { it.copy(entered = entered, message = "", shaking = false) }
        if (entered.length == PIN_LENGTH) commit(entered)
    }

    fun deleteLast() {
        _uiState.update {
            if (it.entered.isEmpty()) it else it.copy(entered = it.entered.dropLast(1), message = "")
        }
    }

    private fun commit(entered: String) {
        when (_uiState.value.mode) {
            PinMode.SETUP -> {
                firstEntry = entered
                _uiState.update { it.copy(mode = PinMode.CONFIRM, entered = "", message = "") }
            }

            PinMode.CONFIRM -> {
                if (entered == firstEntry) {
                    pinService.save(entered)
                    _uiState.update { it.copy(entered = "", unlocked = true) }
                } else {
                    firstEntry = ""
                    _uiState.update {
                        it.copy(
                            mode = PinMode.SETUP,
                            entered = "",
                            message = "Those didn't match — let's try again",
                            shaking = true
                        )
                    }
                }
            }

            PinMode.UNLOCK -> when (val result = pinService.attempt(entered)) {
                is PinAttempt.Success ->
                    _uiState.update { it.copy(entered = "", unlocked = true) }

                is PinAttempt.Incorrect -> _uiState.update {
                    it.copy(
                        entered = "",
                        shaking = true,
                        message = if (result.attemptsRemaining <= 2) {
                            val plural = if (result.attemptsRemaining == 1) "try" else "tries"
                            "Incorrect PIN — ${result.attemptsRemaining} $plural left"
                        } else {
                            "Incorrect PIN"
                        }
                    )
                }

                is PinAttempt.LockedOut -> _uiState.update {
                    it.copy(
                        entered = "",
                        shaking = true,
                        lockedOut = true,
                        message = lockoutMessage(result.secondsRemaining)
                    )
                }
            }
        }
    }

    /** Re-checks the clock so a lockout clears when the screen is returned to. */
    fun refreshLockout() {
        val remaining = pinService.lockoutRemainingSeconds
        _uiState.update {
            it.copy(lockedOut = remaining > 0, message = lockoutMessage(remaining))
        }
    }

    private fun lockoutMessage(seconds: Int): String =
        if (seconds <= 0) "" else "Too many attempts. Try again in ${seconds}s."
}
