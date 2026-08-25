package com.selfward.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

    // Insets are held once, here, rather than in each screen. Onboarding and the
    // PIN keypad had none at all, and from Android 15 the app is drawn behind
    // the status bar whether it asked to be or not — so their first line would
    // have sat under the clock. Padding here consumes the insets, so the
    // Scaffold inside MainScreen does not add them a second time.
    Box(Modifier.fillMaxSize().safeDrawingPadding()) {
        when {
            !onboarded -> OnboardingScreen(onFinished = { onboarded = true })
            !unlocked -> PinScreen(onUnlocked = { unlocked = true })
            else -> MainScreen()
        }
    }
}
