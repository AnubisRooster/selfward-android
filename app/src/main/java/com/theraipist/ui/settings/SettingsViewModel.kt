package com.theraipist.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theraipist.core.ModelSettings
import com.theraipist.core.chat.Provider
import com.theraipist.core.local.DownloadProgress
import com.theraipist.core.local.DownloadStatus
import com.theraipist.core.local.GGUFModelCatalog
import com.theraipist.core.local.LocalModel
import com.theraipist.core.local.ModelDownloader
import com.theraipist.data.settings.SecureSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val DOWNLOAD_POLL_INTERVAL_MS = 1000L

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val secureSettings: SecureSettings,
    private val modelSettings: ModelSettings,
    private val modelDownloader: ModelDownloader
) : ViewModel() {

    val provider = MutableStateFlow(secureSettings.provider)
    val apiKey = MutableStateFlow(secureSettings.apiKey ?: "")
    val model = MutableStateFlow(secureSettings.model)

    val useLocalModel = modelSettings.useLocalModel
    val localModelId = modelSettings.localModelId
    val useLocalTts = modelSettings.useLocalTts

    private val _downloadStatus = MutableStateFlow<Map<String, DownloadStatus>>(emptyMap())
    val downloadStatus = _downloadStatus.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Map<String, DownloadProgress>>(emptyMap())
    val downloadProgress = _downloadProgress.asStateFlow()

    private val awaitedDownloads = mutableSetOf<String>()

    init {
        modelSettings.initFromSettings()
        viewModelScope.launch {
            while (isActive) {
                refreshDownloadState()
                delay(DOWNLOAD_POLL_INTERVAL_MS)
            }
        }
    }

    fun setProvider(provider: Provider) { this.provider.value = provider }
    fun setApiKey(apiKey: String) { this.apiKey.value = apiKey }
    fun setModel(model: String) { this.model.value = model }
    fun setUseLocalModel(use: Boolean) = modelSettings.setUseLocalModel(use)
    fun setLocalModelId(id: String) = modelSettings.setLocalModelId(id)
    fun setUseLocalTts(use: Boolean) = modelSettings.setUseLocalTts(use)

    fun save() {
        secureSettings.save(provider.value, apiKey.value, model.value)
    }

    fun downloadModel(model: LocalModel) {
        modelDownloader.startDownload(model)
        awaitVerification(model)
    }

    fun cancelDownload(model: LocalModel) {
        modelDownloader.cancelDownload(model)
        refreshDownloadState()
    }

    fun deleteModel(model: LocalModel) {
        modelDownloader.deleteDownload(model)
        refreshDownloadState()
    }

    private fun refreshDownloadState() {
        val statuses = GGUFModelCatalog.allModels.associate { it.id to modelDownloader.status(it) }
        _downloadStatus.value = statuses
        _downloadProgress.value = GGUFModelCatalog.allModels
            .filter { statuses[it.id] == DownloadStatus.DOWNLOADING }
            .mapNotNull { m -> modelDownloader.progress(m)?.let { m.id to it } }
            .toMap()
        GGUFModelCatalog.allModels
            .filter { statuses[it.id] == DownloadStatus.VERIFYING && it.id !in awaitedDownloads }
            .forEach { awaitVerification(it) }
    }

    private fun awaitVerification(model: LocalModel) {
        awaitedDownloads += model.id
        viewModelScope.launch {
            val result = modelDownloader.awaitCompletion(model)
            _downloadStatus.update { it + (model.id to result) }
            awaitedDownloads -= model.id
        }
    }
}
