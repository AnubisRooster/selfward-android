package com.theraipist.ui.settings

import androidx.lifecycle.ViewModel
import com.theraipist.core.chat.Provider
import com.theraipist.data.settings.SecureSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val secureSettings: SecureSettings
) : ViewModel() {

    private val _provider = MutableStateFlow(secureSettings.provider)
    val provider = _provider.asStateFlow()

    private val _apiKey = MutableStateFlow(secureSettings.apiKey ?: "")
    val apiKey = _apiKey.asStateFlow()

    private val _model = MutableStateFlow(secureSettings.model)
    val model = _model.asStateFlow()

    fun setProvider(provider: Provider) { _provider.value = provider }
    fun setApiKey(apiKey: String) { _apiKey.value = apiKey }
    fun setModel(model: String) { _model.value = model }

    fun save() {
        secureSettings.save(_provider.value, _apiKey.value, _model.value)
    }
}
