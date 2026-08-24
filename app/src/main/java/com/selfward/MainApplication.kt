package com.selfward

import android.app.Application
import com.selfward.core.ModelSettings
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application() {

    @Inject lateinit var modelSettings: ModelSettings

    override fun onCreate() {
        super.onCreate()
        // AndroidKeyStore-backed SecureSettings isn't available under Robolectric,
        // which invokes Application.onCreate() for every test; don't let that crash
        // app startup in any test environment where the real Keystore is absent.
        runCatching { modelSettings.initFromSettings() }
    }
}
