package com.theraipist.core

import com.theraipist.core.settings.SecureSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelSettings @Inject constructor(
    private val secureSettings: SecureSettings
) {
    private val _useLocalModel = MutableStateFlow(false)
    val useLocalModel = _useLocalModel.asStateFlow()

    private val _localModelId = MutableStateFlow<String?>(null)
    val localModelId = _localModelId.asStateFlow()

    private val _useLocalTts = MutableStateFlow(false)
    val useLocalTts = _useLocalTts.asStateFlow()

    fun initFromSettings() {
        _useLocalModel.value = secureSettings.useLocalModel
        _localModelId.value = secureSettings.localModelId
        _useLocalTts.value = secureSettings.useLocalTts
    }

    fun setUseLocalModel(use: Boolean) {
        _useLocalModel.value = use
        secureSettings.useLocalModel = use
    }

    fun setLocalModelId(id: String?) {
        _localModelId.value = id
        secureSettings.localModelId = id
    }

    fun setUseLocalTts(use: Boolean) {
        _useLocalTts.value = use
        secureSettings.useLocalTts = use
    }
}
