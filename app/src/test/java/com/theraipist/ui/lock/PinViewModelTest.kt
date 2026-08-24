package com.theraipist.ui.lock

import com.theraipist.core.lock.LockoutStore
import com.theraipist.core.lock.PinLockout
import com.theraipist.core.lock.PinService
import com.theraipist.core.lock.PinStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinViewModelTest {

    private class FakePinStore(private var pin: String? = null) : PinStore {
        override fun load(): String? = pin
        override fun save(pin: String) { this.pin = pin }
        override fun clear() { pin = null }
    }

    private class FakeLockoutStore : LockoutStore {
        override var failCount: Int = 0
        override var lockLevel: Int = 0
        override var lockedUntilMillis: Long = 0L
        override fun clear() { failCount = 0; lockLevel = 0; lockedUntilMillis = 0L }
    }

    private fun viewModel(existingPin: String? = null, nowMillis: Long = 1_000_000L): PinViewModel {
        val service = PinService(
            FakePinStore(existingPin),
            PinLockout(FakeLockoutStore(), now = { nowMillis })
        )
        return PinViewModel(service)
    }

    private fun PinViewModel.type(digits: String) = digits.forEach { append(it) }

    @Test
    fun withNoPinSetItAsksToCreateOne() {
        assertEquals(PinMode.SETUP, viewModel().uiState.value.mode)
    }

    @Test
    fun withAPinSetItAsksToEnterIt() {
        assertEquals(PinMode.UNLOCK, viewModel(existingPin = "123456").uiState.value.mode)
    }

    @Test
    fun setupAsksForConfirmationBeforeSaving() {
        val vm = viewModel()

        vm.type("123456")

        assertEquals(PinMode.CONFIRM, vm.uiState.value.mode)
        assertEquals("", vm.uiState.value.entered)
        assertFalse("must not unlock until confirmed", vm.uiState.value.unlocked)
    }

    @Test
    fun matchingConfirmationSavesAndUnlocks() {
        val vm = viewModel()

        vm.type("123456")
        vm.type("123456")

        assertTrue(vm.uiState.value.unlocked)
    }

    @Test
    fun mismatchedConfirmationStartsOverWithoutSaving() {
        val vm = viewModel()

        vm.type("123456")
        vm.type("654321")

        val state = vm.uiState.value
        assertEquals(PinMode.SETUP, state.mode)
        assertFalse(state.unlocked)
        assertTrue(state.message.contains("didn't match"))
    }

    /**
     * The second attempt must be compared against the second entry, not against
     * a stale first entry left over from the abandoned round.
     */
    @Test
    fun retryingSetupAfterAMismatchUsesTheNewEntry() {
        val vm = viewModel()
        vm.type("111111")
        vm.type("222222") // mismatch, back to SETUP

        vm.type("333333")
        vm.type("333333")

        assertTrue(vm.uiState.value.unlocked)
    }

    @Test
    fun correctPinUnlocks() {
        val vm = viewModel(existingPin = "123456")

        vm.type("123456")

        assertTrue(vm.uiState.value.unlocked)
    }

    @Test
    fun wrongPinReportsRemainingTriesOnlyWhenFew() {
        val vm = viewModel(existingPin = "123456")

        vm.type("000000")
        assertEquals("Incorrect PIN", vm.uiState.value.message)

        vm.type("000000")
        vm.type("000000")
        assertTrue(vm.uiState.value.message.contains("2 tries left"))

        vm.type("000000")
        assertTrue(vm.uiState.value.message.contains("1 try left"))
    }

    @Test
    fun fifthWrongPinLocksTheKeypad() {
        val vm = viewModel(existingPin = "123456")

        repeat(5) { vm.type("000000") }

        val state = vm.uiState.value
        assertTrue(state.lockedOut)
        assertTrue(state.message.contains("30s"))
        assertFalse(state.unlocked)
    }

    @Test
    fun digitsAreIgnoredWhileLockedOut() {
        val vm = viewModel(existingPin = "123456")
        repeat(5) { vm.type("000000") }

        vm.type("123456") // the correct PIN, but the keypad is locked

        assertFalse("a lockout must not be bypassable by entering the right PIN", vm.uiState.value.unlocked)
        assertEquals("", vm.uiState.value.entered)
    }

    @Test
    fun deleteRemovesTheLastDigitOnly() {
        val vm = viewModel(existingPin = "123456")

        vm.type("12")
        vm.deleteLast()

        assertEquals("1", vm.uiState.value.entered)
    }

    @Test
    fun deleteOnAnEmptyEntryIsHarmless() {
        val vm = viewModel(existingPin = "123456")

        vm.deleteLast()

        assertEquals("", vm.uiState.value.entered)
    }

    @Test
    fun extraDigitsBeyondTheLengthAreIgnored() {
        val vm = viewModel()

        vm.type("1234567")

        // The sixth digit commits, moving to CONFIRM with an empty entry; the
        // seventh starts the confirmation rather than overflowing the first.
        assertEquals(PinMode.CONFIRM, vm.uiState.value.mode)
        assertEquals("7", vm.uiState.value.entered)
    }

    @Test
    fun theSetupScreenIsHonestAboutWhatAPinProtects() {
        assertTrue(viewModel().uiState.value.showsPrivacyNote)
        assertFalse(viewModel(existingPin = "123456").uiState.value.showsPrivacyNote)
    }
}
