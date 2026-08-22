package com.theraipist

import android.app.Application
import com.theraipist.core.ModelSettings
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application() {

    @Inject lateinit var modelSettings: ModelSettings

    override fun onCreate() {
        super.onCreate()
        modelSettings.initFromSettings()
    }
}
