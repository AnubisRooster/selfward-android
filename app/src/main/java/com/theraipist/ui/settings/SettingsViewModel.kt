package com.theraipist.ui.settings

import androidx.lifecycle.ViewModel
import com.theraipist.core.ModelSettings
import com.theraipist.core.chat.Provider
import com.theraipist.data.settings.SecureSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val secureSettings: SecureSettings,
    private val modelSettings: ModelSettings
) : ViewModel() {

    val provider = MutableStateFlow(secureSettings.provider)
    val apiKey = MutableStateFlow(secureSettings.apiKey ?: "")
    val model = MutableStateFlow(secureSettings.model)

    val useLocalModel = modelSettings.useLocalModel
    val localModelId = modelSettings.localModelId

    init {
        modelSettings.initFromSettings()
    }

    fun setProvider(provider: Provider) { this.provider.value = provider }
    fun setApiKey(apiKey: String) { this.apiKey.value = apiKey }
    fun setModel(model: String) { this.model.value = model }
    fun setUseLocalModel(use: Boolean) = modelSettings.setUseLocalModel(use)
    fun setLocalModelId(id: String) = modelSettings.setLocalModelId(id)

    fun save() {
        secureSettings.save(provider.value, apiKey.value, model.value)
    }
}
