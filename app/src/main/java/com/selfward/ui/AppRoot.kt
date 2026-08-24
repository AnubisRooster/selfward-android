package com.selfward.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfward.ui.lock.PinScreen
import com.selfward.ui.onboarding.OnboardingScreen

/**
 * Root routing, mirroring the iOS `AppRootView`: first launch runs onboarding,
 * then the PIN gate stands in front of the tabbed app. Unlocking is per-launch
 * rather than remembered.
 */
@Composable
fun AppRoot(viewModel: AppRootViewModel = hiltViewModel()) {
    var onboarded by remember { mutableStateOf(viewModel.onboardingComplete) }
    var unlocked by remember { mutableStateOf(false) }

    when {
        !onboarded -> OnboardingScreen(onFinished = { onboarded = true })
        !unlocked -> PinScreen(onUnlocked = { unlocked = true })
        else -> MainScreen()
    }
}
