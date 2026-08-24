package com.selfward.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfward.core.ModelSettings
import com.selfward.core.chat.Provider
import com.selfward.core.embedding.EmbeddingModelCatalog
import com.selfward.core.embedding.EmbeddingModelDownloader
import com.selfward.core.local.DownloadProgress
import com.selfward.core.local.DownloadStatus
import com.selfward.core.local.GGUFModelCatalog
import com.selfward.core.local.LocalModel
import com.selfward.core.local.ModelDownloader
import com.selfward.core.settings.SecureSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val DOWNLOAD_POLL_INTERVAL_MS = 1000L

/** [GGUFModelCatalog] model ids and [EmbeddingModelCatalog] ids never collide, so download state for both lives in one map. */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val secureSettings: SecureSettings,
    private val modelSettings: ModelSettings,
    private val modelDownloader: ModelDownloader,
    private val embeddingModelDownloader: EmbeddingModelDownloader
) : ViewModel() {

    val provider = MutableStateFlow(secureSettings.provider)
    val apiKey = MutableStateFlow(secureSettings.apiKey ?: "")
    val model = MutableStateFlow(secureSettings.model)

    val useLocalModel = modelSettings.useLocalModel
    val localModelId = modelSettings.localModelId
    val useLocalTts = modelSettings.useLocalTts

    val embeddingModel = EmbeddingModelCatalog.default

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
        awaitGgufVerification(model)
    }

    fun cancelDownload(model: LocalModel) {
        modelDownloader.cancelDownload(model)
        refreshDownloadState()
    }

    fun deleteModel(model: LocalModel) {
        modelDownloader.deleteDownload(model)
        refreshDownloadState()
    }

    fun downloadEmbeddingModel() {
        embeddingModelDownloader.startDownload(embeddingModel)
        awaitEmbeddingVerification()
    }

    fun cancelEmbeddingDownload() {
        embeddingModelDownloader.cancelDownload(embeddingModel)
        refreshDownloadState()
    }

    fun deleteEmbeddingModel() {
        embeddingModelDownloader.deleteDownload(embeddingModel)
        refreshDownloadState()
    }

    private fun refreshDownloadState() {
        val ggufStatuses = GGUFModelCatalog.allModels.associate { it.id to modelDownloader.status(it) }
        val embeddingStatus = embeddingModelDownloader.status(embeddingModel)
        _downloadStatus.value = ggufStatuses + (embeddingModel.id to embeddingStatus)

        val ggufProgress = GGUFModelCatalog.allModels
            .filter { ggufStatuses[it.id] == DownloadStatus.DOWNLOADING }
            .mapNotNull { m -> modelDownloader.progress(m)?.let { m.id to it } }
            .toMap()
        val embeddingProgress = if (embeddingStatus == DownloadStatus.DOWNLOADING) {
            embeddingModelDownloader.progress(embeddingModel)?.let { mapOf(embeddingModel.id to it) }.orEmpty()
        } else {
            emptyMap()
        }
        _downloadProgress.value = ggufProgress + embeddingProgress

        GGUFModelCatalog.allModels
            .filter { ggufStatuses[it.id] == DownloadStatus.VERIFYING && it.id !in awaitedDownloads }
            .forEach { awaitGgufVerification(it) }
        if (embeddingStatus == DownloadStatus.VERIFYING && embeddingModel.id !in awaitedDownloads) {
            awaitEmbeddingVerification()
        }
    }

    private fun awaitGgufVerification(model: LocalModel) {
        awaitedDownloads += model.id
        viewModelScope.launch {
            val result = modelDownloader.awaitCompletion(model)
            _downloadStatus.update { it + (model.id to result) }
            awaitedDownloads -= model.id
        }
    }

    private fun awaitEmbeddingVerification() {
        awaitedDownloads += embeddingModel.id
        viewModelScope.launch {
            val result = embeddingModelDownloader.awaitCompletion(embeddingModel)
            _downloadStatus.update { it + (embeddingModel.id to result) }
            awaitedDownloads -= embeddingModel.id
        }
    }
}
