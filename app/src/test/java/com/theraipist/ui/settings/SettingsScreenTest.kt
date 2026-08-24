package com.theraipist.ui.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import com.theraipist.core.ModelSettings
import com.theraipist.core.chat.Provider
import com.theraipist.core.embedding.EmbeddingModelDownloader
import com.theraipist.core.embedding.EmbeddingModelSpec
import com.theraipist.core.local.DownloadProgress
import com.theraipist.core.local.DownloadStatus
import com.theraipist.core.local.LocalModel
import com.theraipist.core.local.ModelDownloader
import com.theraipist.data.settings.FakeSecureSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

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
        override fun status(model: EmbeddingModelSpec) = DownloadStatus.NOT_DOWNLOADED
        override fun progress(model: EmbeddingModelSpec): DownloadProgress? = null
        override fun onnxFile(model: EmbeddingModelSpec) = java.io.File("/fake/model.onnx")
        override fun vocabFile(model: EmbeddingModelSpec) = java.io.File("/fake/vocab.txt")
        override fun startDownload(model: EmbeddingModelSpec) {}
        override fun cancelDownload(model: EmbeddingModelSpec) {}
        override fun deleteDownload(model: EmbeddingModelSpec) {}
        override suspend fun awaitCompletion(model: EmbeddingModelSpec) = DownloadStatus.DOWNLOADED
    }

    private fun buildViewModel(
        secureSettings: FakeSecureSettings = FakeSecureSettings(),
        modelDownloader: FakeModelDownloader = FakeModelDownloader()
    ) = SettingsViewModel(secureSettings, ModelSettings(secureSettings), modelDownloader, FakeEmbeddingModelDownloader())

    @Test
    fun showsStoredProviderAndModel() {
        val secureSettings = FakeSecureSettings(
            initialProvider = Provider.OPENAI,
            initialApiKey = "sk-existing",
            initialModel = "gpt-4o-mini"
        )
        composeRule.setContent { SettingsScreen(viewModel = buildViewModel(secureSettings)) }

        composeRule.onNodeWithText("gpt-4o-mini").assertIsDisplayed()
    }

    /**
     * Settings is reachable at any moment, so the key must not sit on screen in
     * the clear. Onboarding already masked it; this screen did not, which left
     * the same secret hidden in one place and legible in the other.
     */
    @Test
    fun theStoredApiKeyIsNotLegibleByDefault() {
        val secureSettings = FakeSecureSettings(initialApiKey = "sk-existing")
        composeRule.setContent { SettingsScreen(viewModel = buildViewModel(secureSettings)) }

        composeRule.onNodeWithText("sk-existing").assertDoesNotExist()
    }

    @Test
    fun theApiKeyCanBeRevealedDeliberately() {
        val secureSettings = FakeSecureSettings(initialApiKey = "sk-existing")
        composeRule.setContent { SettingsScreen(viewModel = buildViewModel(secureSettings)) }

        composeRule.onNodeWithText("Show").performClick()

        composeRule.onNodeWithText("sk-existing").assertIsDisplayed()
    }

    @Test
    fun editingApiKeyAndSavingWritesThroughToSecureSettings() {
        val secureSettings = FakeSecureSettings()
        val vm = buildViewModel(secureSettings)
        composeRule.setContent { SettingsScreen(viewModel = vm) }

        composeRule.onNodeWithText("API Key").performTextInput("sk-new-key")
        composeRule.onNodeWithTag("settingsList").performScrollToNode(hasText("Save"))
        composeRule.onNodeWithText("Save").performClick()

        assertEquals("sk-new-key", secureSettings.apiKey)
    }

    @Test
    fun togglingUseOnDeviceModelUpdatesState() {
        val vm = buildViewModel()
        composeRule.setContent { SettingsScreen(viewModel = vm) }

        composeRule.onNodeWithTag("useLocalModelSwitch").performClick()

        assertTrue(vm.useLocalModel.value)
    }

    @Test
    fun tappingDownloadStartsAModelDownload() {
        val downloader = FakeModelDownloader()
        composeRule.setContent { SettingsScreen(viewModel = buildViewModel(modelDownloader = downloader)) }

        composeRule.onAllNodesWithText("Download").onFirst().performClick()

        assertTrue(downloader.started.isNotEmpty())
    }

    @Test
    fun togglingReadRepliesAloudUpdatesState() {
        val vm = buildViewModel()
        composeRule.setContent { SettingsScreen(viewModel = vm) }

        composeRule.onNodeWithTag("settingsList").performScrollToNode(hasText("Read replies aloud on-device"))
        composeRule.onNodeWithTag("useLocalTtsSwitch").performClick()

        assertTrue(vm.useLocalTts.value)
    }
}
