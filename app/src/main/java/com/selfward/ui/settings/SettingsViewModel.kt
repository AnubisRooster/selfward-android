package com.selfward.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfward.core.ModelSettings
import com.selfward.core.catalog.ModelRanking
import com.selfward.core.catalog.OpenRouterCatalog
import com.selfward.core.catalog.OpenRouterModel
import com.selfward.core.catalog.ProviderDefaults
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
    private val embeddingModelDownloader: EmbeddingModelDownloader,
    private val openRouterCatalog: OpenRouterCatalog
) : ViewModel() {

    val provider = MutableStateFlow(secureSettings.provider)
    val apiKey = MutableStateFlow(secureSettings.apiKey ?: "")
    val model = MutableStateFlow(secureSettings.model)

    val useLocalModel = modelSettings.useLocalModel
    val localModelId = modelSettings.localModelId
    val useLocalTts = modelSettings.useLocalTts
    val voiceSilenceSeconds = modelSettings.voiceSilenceSeconds

    val embeddingModel = EmbeddingModelCatalog.default

    private val _downloadStatus = MutableStateFlow<Map<String, DownloadStatus>>(emptyMap())
    val downloadStatus = _downloadStatus.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Map<String, DownloadProgress>>(emptyMap())
    val downloadProgress = _downloadProgress.asStateFlow()

    private val awaitedDownloads = mutableSetOf<String>()

    /** OpenRouter's catalogue, free models first. Empty until it has been fetched. */
    private val _openRouterModels = MutableStateFlow<List<OpenRouterModel>>(emptyList())
    val openRouterModels = _openRouterModels.asStateFlow()

    private val _catalogLoading = MutableStateFlow(false)
    val catalogLoading = _catalogLoading.asStateFlow()

    init {
        modelSettings.initFromSettings()
        _openRouterModels.value = ModelRanking.ranked(openRouterCatalog.cached())
        if (secureSettings.provider == Provider.OPENROUTER) refreshOpenRouterModels()
        viewModelScope.launch {
            while (isActive) {
                refreshDownloadState()
                delay(DOWNLOAD_POLL_INTERVAL_MS)
            }
        }
    }

    /**
     * Switching provider also moves the model, when the one on screen plainly
     * belonged to the provider being left. A model id is not portable between
     * them - OpenRouter wants a namespaced slug, Anthropic wants its own names -
     * so carrying one across produced a model-not-found error from the API on
     * the client's first message.
     *
     * A model that could plausibly belong to the new provider is left alone: it
     * is more likely to be a deliberate choice than a leftover.
     */
    fun setProvider(provider: Provider) {
        val previous = this.provider.value
        this.provider.value = provider
        if (previous != provider) {
            // Each provider keeps its own key and model. Carrying the previous
            // one's key across was how an OpenAI key ended up being sent to
            // whichever provider was chosen next.
            apiKey.value = secureSettings.apiKeyFor(provider).orEmpty()
            val stored = secureSettings.modelFor(provider)
            model.value = if (provider == Provider.OPENROUTER && stored == ModelRanking.PINNED_FREE_FALLBACK) {
                ProviderDefaults.modelFor(
                    provider,
                    openRouterFreeId = ModelRanking.bestFree(_openRouterModels.value)?.id
                )
            } else {
                stored
            }
        }
        if (provider == Provider.OPENROUTER) refreshOpenRouterModels()
    }

    /**
     * Fetches the catalogue and, if the model on screen is still the pinned
     * fallback, moves it to the best free model actually on offer.
     */
    fun refreshOpenRouterModels(force: Boolean = false) {
        if (_catalogLoading.value) return
        viewModelScope.launch {
            _catalogLoading.value = true
            val fetched = runCatching {
                openRouterCatalog.models(apiKey.value.takeIf { it.isNotBlank() }, force)
            }.getOrDefault(emptyList())
            if (fetched.isNotEmpty()) {
                _openRouterModels.value = ModelRanking.ranked(fetched)
                if (model.value == ModelRanking.PINNED_FREE_FALLBACK) {
                    ModelRanking.bestFree(fetched)?.let { model.value = it.id }
                }
            }
            _catalogLoading.value = false
        }
    }
    fun setApiKey(apiKey: String) { this.apiKey.value = apiKey }
    fun setModel(model: String) { this.model.value = model }
    fun setUseLocalModel(use: Boolean) = modelSettings.setUseLocalModel(use)
    fun setLocalModelId(id: String) = modelSettings.setLocalModelId(id)
    fun setUseLocalTts(use: Boolean) = modelSettings.setUseLocalTts(use)

    fun setVoiceSilenceSeconds(seconds: Double) = modelSettings.setVoiceSilenceSeconds(seconds)

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
