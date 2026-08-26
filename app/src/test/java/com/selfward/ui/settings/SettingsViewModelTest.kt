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
    private class FakeLocalTtsService(
        private val voices: List<com.selfward.core.voice.DeviceVoice> = emptyList()
    ) : com.selfward.core.voice.LocalTtsService {
        val requestedVoices = mutableListOf<String?>()
        override fun speak(text: String, onDone: () -> Unit) { onDone() }
        override fun availableVoices() = voices
        override fun setVoice(name: String?) { requestedVoices += name }
    }


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
        embeddingDownloader: FakeEmbeddingModelDownloader = FakeEmbeddingModelDownloader(),
        // One instance shared between ModelSettings and the ViewModel, as Hilt's
        // @Singleton binding shares one real engine between them in the app -
        // a test wiring two separate fakes here could not tell a voice picked
        // through one from a voice the other reports as available.
        localTtsService: FakeLocalTtsService = FakeLocalTtsService()
    ) = SettingsViewModel(
        secureSettings,
        ModelSettings(secureSettings, localTtsService),
        modelDownloader,
        embeddingDownloader,
        FakeOpenRouterCatalog(),
        localTtsService
    )

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


    /**
     * The reason keys are per provider rather than per app: one shared key was
     * kept when the selection changed, so an OpenAI key was handed to whichever
     * provider was chosen next and sent to them on the first message.
     */
    @Test
    fun switchingProviderDoesNotCarryTheOtherProvidersKey() {
        val settings = FakeSecureSettings(
            initialProvider = Provider.OPENAI,
            initialApiKey = "sk-openai-secret",
            initialModel = "gpt-4o-mini"
        )
        val vm = buildVm(settings)

        vm.setProvider(Provider.ANTHROPIC)

        assertEquals("", vm.apiKey.value)
    }

    @Test
    fun eachProviderKeepsItsOwnKey() {
        val settings = FakeSecureSettings(initialProvider = Provider.OPENAI)
        val vm = buildVm(settings)

        vm.setApiKey("sk-openai")
        vm.setModel("gpt-4o-mini")
        vm.save()

        vm.setProvider(Provider.ANTHROPIC)
        vm.setApiKey("sk-ant")
        vm.setModel("claude-3-5-haiku-latest")
        vm.save()

        assertEquals("sk-openai", settings.apiKeyFor(Provider.OPENAI))
        assertEquals("sk-ant", settings.apiKeyFor(Provider.ANTHROPIC))
    }

    /** Coming back to a provider should find it as it was left. */
    @Test
    fun returningToAProviderRestoresItsKeyAndModel() {
        val settings = FakeSecureSettings(initialProvider = Provider.OPENAI)
        val vm = buildVm(settings)
        vm.setApiKey("sk-openai")
        vm.setModel("gpt-4o-mini")
        vm.save()
        vm.setProvider(Provider.ANTHROPIC)
        vm.setApiKey("sk-ant")
        vm.save()

        vm.setProvider(Provider.OPENAI)

        assertEquals("sk-openai", vm.apiKey.value)
        assertEquals("gpt-4o-mini", vm.model.value)
    }

    /** A provider never set up starts empty rather than borrowing. */
    @Test
    fun anUnconfiguredProviderStartsWithNoKeyAndItsOwnDefaultModel() {
        val settings = FakeSecureSettings(
            initialProvider = Provider.OPENAI,
            initialApiKey = "sk-openai"
        )
        val vm = buildVm(settings)

        vm.setProvider(Provider.ANTHROPIC)

        assertEquals("", vm.apiKey.value)
        assertTrue(vm.model.value.startsWith("claude"))
    }


    // ---- Voice ----

    @Test
    fun setTtsVoiceUpdatesStateAndPersists() {
        val secureSettings = FakeSecureSettings()
        val vm = buildVm(secureSettings = secureSettings)

        vm.setTtsVoice("nova")

        assertEquals("nova", vm.ttsVoice.value)
        assertEquals("nova", secureSettings.ttsVoice)
    }

    @Test
    fun setLocalTtsVoiceNameUpdatesStateAndTellsTheEngine() {
        val tts = FakeLocalTtsService()
        val vm = buildVm(localTtsService = tts)

        vm.setLocalTtsVoiceName("en-us-x-tpc-local")

        assertEquals("en-us-x-tpc-local", vm.localTtsVoiceName.value)
        assertTrue(tts.requestedVoices.contains("en-us-x-tpc-local"))
    }

    @Test
    fun deviceVoicesIsPopulatedFromTheEngineOnInit() {
        val voice = com.selfward.core.voice.DeviceVoice(
            "Ava", "en-US", com.selfward.core.voice.VoiceTier.PREMIUM
        )
        val vm = buildVm(localTtsService = FakeLocalTtsService(voices = listOf(voice)))

        assertEquals(
            listOf("Ava"),
            vm.deviceVoices.value[com.selfward.core.voice.VoiceTier.PREMIUM]?.map { it.name }
        )
    }

    /**
     * The real engine only knows its voices after an async callback that can
     * land after this screen has already opened, so the list has to be
     * re-readable on demand rather than trusted from init alone.
     */
    @Test
    fun refreshDeviceVoicesPicksUpVoicesThatArrivedAfterInit() {
        val voice = com.selfward.core.voice.DeviceVoice(
            "Ava", "en-US", com.selfward.core.voice.VoiceTier.PREMIUM
        )
        var voicesNowReady = false
        val tts = object : com.selfward.core.voice.LocalTtsService {
            override fun speak(text: String, onDone: () -> Unit) {}
            override fun availableVoices() = if (voicesNowReady) listOf(voice) else emptyList()
            override fun setVoice(name: String?) {}
        }
        // Built directly rather than through buildVm: that helper takes the
        // test's own FakeLocalTtsService, and this test needs a stateful fake
        // whose answer changes between the two refreshDeviceVoices() calls.
        val secureSettings = FakeSecureSettings()
        val vm2 = SettingsViewModel(
            secureSettings,
            ModelSettings(secureSettings, tts),
            FakeModelDownloader(),
            FakeEmbeddingModelDownloader(),
            FakeOpenRouterCatalog(),
            tts
        )
        assertTrue(vm2.deviceVoices.value.isEmpty())

        voicesNowReady = true
        vm2.refreshDeviceVoices()

        assertEquals(listOf("Ava"), vm2.deviceVoices.value[com.selfward.core.voice.VoiceTier.PREMIUM]?.map { it.name })
    }
}
