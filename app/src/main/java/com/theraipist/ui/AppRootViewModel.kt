package com.theraipist.ui

import androidx.lifecycle.ViewModel
import com.theraipist.core.intake.IntakeStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** Supplies the one persisted fact the root routing needs. */
@HiltViewModel
class AppRootViewModel @Inject constructor(
    private val intakeStore: IntakeStore
) : ViewModel() {
    val onboardingComplete: Boolean get() = intakeStore.onboardingComplete
}
