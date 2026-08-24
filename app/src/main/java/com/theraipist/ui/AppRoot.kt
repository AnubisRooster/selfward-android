package com.theraipist.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.theraipist.ui.lock.PinScreen

/**
 * Root routing, mirroring the iOS `AppRootView`: the PIN gate stands in front of
 * the tabbed app, and unlocking is per-launch rather than remembered.
 *
 * Onboarding will slot in ahead of the gate in a following change.
 */
@Composable
fun AppRoot() {
    var unlocked by remember { mutableStateOf(false) }

    if (unlocked) {
        MainScreen()
    } else {
        PinScreen(onUnlocked = { unlocked = true })
    }
}
