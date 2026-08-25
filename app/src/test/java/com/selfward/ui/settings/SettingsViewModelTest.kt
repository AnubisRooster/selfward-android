package com.selfward.ui.settings

import com.selfward.core.ModelSettings
import com.selfward.core.chat.Provider
import com.selfward.core.embedding.EmbeddingModelDownloader
import com.selfward.core.embedding.EmbeddingModelSpec
import com.selfward.core.local.DownloadProgress
import com.selfward.core.local.DownloadStatus
import com.selfward.core.local.GGUFModelCatalog
import com.selfward.core.local.LocalModel
import com.selfward.core.local.ModelDownloader
import com.selfward.data.settings.FakeSecureSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * SettingsViewModel.init launches an unbounded `while (isActive) { ...; delay(...) }`
 * poll loop with no natural completion (nothing calls onCleared() outside the real
 * Android lifecycle), so these tests deliberately do NOT wrap bodies in runTest {} -
 * runTest's end-of-test advanceUntilIdle() operates on the whole shared scheduler,
 * not just its own structured children, and would hang trying to advance through an
 * infinite delay loop. Every public method under test here is non-suspend, so plain
 * @Test methods are sufficient; UnconfinedTestDispatcher still runs the eager,
 * non-delayed parts of the poll loop's first iteration inline, then parks harmlessly
 * at its first delay() with nothing driving that dispatcher's clock forward.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private class FakeModelDownloader : ModelDownloader {
        val started = mutableListOf<String>()
        override fun status(model: LocalModel) = DownloadStatus.NOT_DOWNLOADED
        override fun progress(model: LocalModel): DownloadProgress? = null
        override fun localFile(model: LocalModel) = java.io.File("/fake/${model.fileName}")
        override fun startDownload(model: LocalModel) { started += model.id }
        override fun cancelDownload(model: LocalModel) {}
        override fun deleteDownload(model: LocalModel) {}
        override suspend fun awaitCompletion(model: LocalModel) = DownloadStatus.DOWNLOADED
    }

    private class FakeEmbeddingModelDownloader : EmbeddingModelDownloader {
        var startedCount = 0
        override fun status(model: EmbeddingModelSpec) = DownloadStatus.NOT_DOWNLOADED
        override fun progress(model: EmbeddingModelSpec): DownloadProgress? = null
        override fun onnxFile(model: EmbeddingModelSpec) = java.io.File("/fake/model.onnx")
        override fun vocabFile(model: EmbeddingModelSpec) = java.io.File("/fake/vocab.txt")
        override fun startDownload(model: EmbeddingModelSpec) { startedCount++ }
        override fun cancelDownload(model: EmbeddingModelSpec) {}
        override fun deleteDownload(model: EmbeddingModelSpec) {}
        override suspend fun awaitCompletion(model: EmbeddingModelSpec) = DownloadStatus.DOWNLOADED
    }

    private val mainDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(mainDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildVm(
        secureSettings: FakeSecureSettings = FakeSecureSettings(),
        modelDownloader: FakeModelDownloader = FakeModelDownloader(),
        embeddingDownloader: FakeEmbeddingModelDownloader = FakeEmbeddingModelDownloader()
    ) = SettingsViewModel(secureSettings, ModelSettings(secureSettings), modelDownloader, embeddingDownloader, FakeOpenRouterCatalog())

    @Test
    fun initialStateReflectsStoredSettings() {
        val secureSettings = FakeSecureSettings(
            initialProvider = Provider.ANTHROPIC,
            initialApiKey = "key123",
            initialModel = "claude-3"
        )

        val vm = buildVm(secureSettings = secureSettings)

        assertEquals(Provider.ANTHROPIC, vm.provider.value)
        assertEquals("key123", vm.apiKey.value)
        assertEquals("claude-3", vm.model.value)
    }

    @Test
    fun saveWritesThroughToSecureSettings() {
        val secureSettings = FakeSecureSettings()
        val vm = buildVm(secureSettings = secureSettings)

        vm.setProvider(Provider.OPENROUTER)
        vm.setApiKey("new-key")
        vm.setModel("some-model")
        vm.save()

        assertEquals(Provider.OPENROUTER, secureSettings.provider)
        assertEquals("new-key", secureSettings.apiKey)
        assertEquals("some-model", secureSettings.model)
    }

    @Test
    fun setUseLocalModelUpdatesStateAndPersists() {
        val secureSettings = FakeSecureSettings()
        val vm = buildVm(secureSettings = secureSettings)

        vm.setUseLocalModel(true)

        assertTrue(vm.useLocalModel.value)
        assertTrue(secureSettings.useLocalModel)
    }

    @Test
    fun downloadModelStartsDownloadForThatModel() {
        val downloader = FakeModelDownloader()
        val vm = buildVm(modelDownloader = downloader)
        val model = GGUFModelCatalog.allModels.first()

        vm.downloadModel(model)

        assertTrue(downloader.started.contains(model.id))
    }

    @Test
    fun downloadEmbeddingModelStartsDownload() {
        val downloader = FakeEmbeddingModelDownloader()
        val vm = buildVm(embeddingDownloader = downloader)

        vm.downloadEmbeddingModel()

        assertEquals(1, downloader.startedCount)
    }

    /** No network in these tests; the catalogue is empty unless a test says otherwise. */
    private class FakeOpenRouterCatalog(
        private val models: List<com.selfward.core.catalog.OpenRouterModel> = emptyList()
    ) : com.selfward.core.catalog.OpenRouterCatalog {
        override suspend fun models(apiKey: String?, forceRefresh: Boolean) = models
        override fun cached() = models
    }

}
