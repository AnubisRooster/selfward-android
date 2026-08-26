package com.selfward.core

import com.selfward.core.voice.DeviceVoice
import com.selfward.core.voice.LocalTtsService
import com.selfward.core.voice.VoiceCatalog
import com.selfward.data.settings.FakeSecureSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ModelSettingsTest {

    /** Records every voice it was asked to switch to, so tests can check the engine was actually told. */
    private class SpyingLocalTtsService : LocalTtsService {
        val requestedVoices = mutableListOf<String?>()
        override fun speak(text: String, onDone: () -> Unit) {}
        override fun availableVoices() = emptyList<DeviceVoice>()
        override fun setVoice(name: String?) {
            requestedVoices += name
        }
    }

    // MARK: - Cloud voice

    @Test
    fun theCloudVoiceDefaultsToTheFirstInTheCatalogue() {
        val settings = ModelSettings(FakeSecureSettings(), SpyingLocalTtsService())

        assertEquals(VoiceCatalog.openAiVoices.first(), settings.ttsVoice.value)
    }

    @Test
    fun settingTheCloudVoicePersistsIt() {
        val secureSettings = FakeSecureSettings()
        val settings = ModelSettings(secureSettings, SpyingLocalTtsService())

        settings.setTtsVoice("nova")

        assertEquals("nova", settings.ttsVoice.value)
        assertEquals("nova", secureSettings.ttsVoice)
    }

    @Test
    fun aStoredCloudVoiceIsRestoredOnInit() {
        val secureSettings = FakeSecureSettings().apply { ttsVoice = "shimmer" }
        val settings = ModelSettings(secureSettings, SpyingLocalTtsService())

        settings.initFromSettings()

        assertEquals("shimmer", settings.ttsVoice.value)
    }

    /**
     * A voice name from a vendor that no longer exists, or corrupted prefs,
     * must not be handed to the TTS request as-is - it would fail every reply
     * instead of falling back to something that works.
     */
    @Test
    fun anUnrecognisedStoredCloudVoiceFallsBackToTheDefault() {
        val secureSettings = FakeSecureSettings().apply { ttsVoice = "not-a-real-voice" }
        val settings = ModelSettings(secureSettings, SpyingLocalTtsService())

        settings.initFromSettings()

        assertEquals(VoiceCatalog.openAiVoices.first(), settings.ttsVoice.value)
    }

    // MARK: - On-device voice

    @Test
    fun settingTheLocalVoicePersistsItAndTellsTheEngine() {
        val secureSettings = FakeSecureSettings()
        val tts = SpyingLocalTtsService()
        val settings = ModelSettings(secureSettings, tts)

        settings.setLocalTtsVoiceName("en-us-x-tpc-local")

        assertEquals("en-us-x-tpc-local", settings.localTtsVoiceName.value)
        assertEquals("en-us-x-tpc-local", secureSettings.localTtsVoiceName)
        assertEquals(listOf("en-us-x-tpc-local"), tts.requestedVoices)
    }

    /**
     * The engine remembers nothing across a process restart - the whole reason
     * this exists - so the persisted choice has to reach the engine again on
     * every cold start, not only when the person changes it in this session.
     */
    @Test
    fun aStoredLocalVoiceIsReAppliedToTheEngineOnInit() {
        val secureSettings = FakeSecureSettings().apply { localTtsVoiceName = "en-us-x-sfg-local" }
        val tts = SpyingLocalTtsService()
        val settings = ModelSettings(secureSettings, tts)

        settings.initFromSettings()

        assertEquals("en-us-x-sfg-local", settings.localTtsVoiceName.value)
        assertEquals(listOf("en-us-x-sfg-local"), tts.requestedVoices)
    }

    /**
     * Constructing [ModelSettings] alone must not call into the engine - only
     * [ModelSettings.initFromSettings] does, since that is the one point in
     * the lifecycle where "apply what was saved" makes sense.
     */
    @Test
    fun constructingModelSettingsAloneDoesNotTouchTheEngine() {
        val tts = SpyingLocalTtsService()

        ModelSettings(FakeSecureSettings(), tts)

        assertEquals(emptyList<String?>(), tts.requestedVoices)
    }

    @Test
    fun noStoredLocalVoiceLeavesTheSelectionNull() {
        val settings = ModelSettings(FakeSecureSettings(), SpyingLocalTtsService())

        settings.initFromSettings()

        assertNull(settings.localTtsVoiceName.value)
    }
}
