package com.selfward.ui.onboarding

import com.selfward.core.chat.Provider
import com.selfward.core.intake.Intake
import com.selfward.core.intake.IntakeStore
import com.selfward.data.settings.FakeSecureSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingViewModelTest {

    private class FakeIntakeStore : IntakeStore {
        var saved: Intake? = null
        override fun load(): Intake = saved ?: Intake()
        override fun save(intake: Intake) { saved = intake }
        override fun clear() { saved = null }
        override var onboardingComplete: Boolean = false
    }

    private fun viewModel(
        store: FakeIntakeStore = FakeIntakeStore(),
        settings: FakeSecureSettings = FakeSecureSettings()
    ) = OnboardingViewModel(store, settings) to store

    private fun OnboardingViewModel.advanceTo(step: OnboardingStep) {
        while (uiState.value.step != step) {
            if (uiState.value.step == OnboardingStep.DISCLAIMER) setAcknowledged(true)
            next()
        }
    }

    @Test
    fun startsAtWelcome() {
        val (vm, _) = viewModel()
        assertEquals(OnboardingStep.WELCOME, vm.uiState.value.step)
    }

    /** The disclaimer is the one step that cannot be walked past. */
    @Test
    fun disclaimerBlocksContinueUntilAcknowledged() {
        val (vm, _) = viewModel()
        vm.next()

        assertEquals(OnboardingStep.DISCLAIMER, vm.uiState.value.step)
        assertFalse(vm.uiState.value.canContinue)

        vm.next()
        assertEquals("must not advance past an unacknowledged disclaimer", OnboardingStep.DISCLAIMER, vm.uiState.value.step)

        vm.setAcknowledged(true)
        vm.next()
        assertEquals(OnboardingStep.API_KEY, vm.uiState.value.step)
    }

    @Test
    fun unacknowledgingTheDisclaimerBlocksAgain() {
        val (vm, _) = viewModel()
        vm.next()
        vm.setAcknowledged(true)
        vm.setAcknowledged(false)

        vm.next()

        assertEquals(OnboardingStep.DISCLAIMER, vm.uiState.value.step)
    }

    @Test
    fun everyOtherStepCanBeWalkedPast() {
        val (vm, _) = viewModel()
        vm.advanceTo(OnboardingStep.GOALS)

        assertEquals(OnboardingStep.GOALS, vm.uiState.value.step)
        assertTrue(vm.uiState.value.step.isLast)
    }

    @Test
    fun backStepsWithoutFallingOffTheStart() {
        val (vm, _) = viewModel()

        vm.back()
        assertEquals("back from the first step should be harmless", OnboardingStep.WELCOME, vm.uiState.value.step)

        vm.next()
        vm.back()
        assertEquals(OnboardingStep.WELCOME, vm.uiState.value.step)
    }

    @Test
    fun finishingPersistsIntakeAndMarksOnboardingDone() {
        val (vm, store) = viewModel()
        vm.setName("Sam")
        vm.setConcerns("Sleep")
        vm.advanceTo(OnboardingStep.GOALS)

        vm.next()

        assertTrue(store.onboardingComplete)
        assertEquals("Sam", store.saved?.name)
        assertEquals("Sleep", store.saved?.concerns)
        assertTrue(vm.uiState.value.finished)
    }

    @Test
    fun intakeIsTrimmedBeforeBeingStored() {
        val (vm, store) = viewModel()
        vm.setName("  Sam  ")
        vm.setGoals("\n sleep better \n")
        vm.advanceTo(OnboardingStep.GOALS)

        vm.next()

        assertEquals("Sam", store.saved?.name)
        assertEquals("sleep better", store.saved?.goals)
    }

    @Test
    fun anApiKeyEnteredDuringOnboardingIsSaved() {
        val settings = FakeSecureSettings()
        val (vm, _) = viewModel(settings = settings)
        vm.setProvider(Provider.ANTHROPIC)
        vm.setApiKey("sk-test-123")
        vm.advanceTo(OnboardingStep.GOALS)

        vm.next()

        assertEquals(Provider.ANTHROPIC, settings.provider)
        assertEquals("sk-test-123", settings.apiKey)
    }

    /** Skipping the key step must not clobber the stored provider settings. */
    @Test
    fun aBlankApiKeyIsNotSaved() {
        val settings = FakeSecureSettings(initialApiKey = "existing-key")
        val (vm, _) = viewModel(settings = settings)
        vm.setApiKey("   ")
        vm.advanceTo(OnboardingStep.GOALS)

        vm.next()

        assertEquals("existing-key", settings.apiKey)
    }

    @Test
    fun anEmptyIntakeIsStoredAsEmptyRatherThanFabricated() {
        val (vm, store) = viewModel()
        vm.advanceTo(OnboardingStep.GOALS)

        vm.next()

        assertTrue(store.saved!!.isEmpty)
        assertTrue("onboarding should still complete with nothing filled in", store.onboardingComplete)
    }

    @Test
    fun progressAdvancesAcrossTheSteps() {
        val (vm, _) = viewModel()
        val atStart = vm.uiState.value.progress

        vm.advanceTo(OnboardingStep.GOALS)

        assertTrue(vm.uiState.value.progress > atStart)
        assertEquals(1f, vm.uiState.value.progress, 0.001f)
    }
}
