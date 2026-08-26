package com.selfward.core

import com.selfward.core.settings.SecureSettings
import com.selfward.core.voice.LocalTtsService
import com.selfward.core.voice.VoiceCatalog
import com.selfward.core.voice.VoiceTranscript
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelSettings @Inject constructor(
    private val secureSettings: SecureSettings,
    private val localTtsService: LocalTtsService
) {
    private val _useLocalModel = MutableStateFlow(false)
    val useLocalModel = _useLocalModel.asStateFlow()

    private val _localModelId = MutableStateFlow<String?>(null)
    val localModelId = _localModelId.asStateFlow()

    private val _useLocalTts = MutableStateFlow(false)
    val useLocalTts = _useLocalTts.asStateFlow()

    /** How long a pause has to run before voice mode treats a turn as finished. */
    private val _voiceSilenceSeconds =
        MutableStateFlow(VoiceTranscript.DEFAULT_SILENCE_SECONDS)
    val voiceSilenceSeconds = _voiceSilenceSeconds.asStateFlow()

    /** The OpenAI-compatible cloud voice replies are spoken in. */
    private val _ttsVoice = MutableStateFlow(VoiceCatalog.openAiVoices.first())
    val ttsVoice = _ttsVoice.asStateFlow()

    /** The chosen on-device voice's name, or null for the engine's own default. */
    private val _localTtsVoiceName = MutableStateFlow<String?>(null)
    val localTtsVoiceName = _localTtsVoiceName.asStateFlow()

    fun initFromSettings() {
        _useLocalModel.value = secureSettings.useLocalModel
        _localModelId.value = secureSettings.localModelId
        _useLocalTts.value = secureSettings.useLocalTts
        _voiceSilenceSeconds.value =
            VoiceTranscript.silenceSeconds(secureSettings.voiceSilenceSeconds)
        _ttsVoice.value = secureSettings.ttsVoice.takeIf { VoiceCatalog.isKnown(it) }
            ?: VoiceCatalog.openAiVoices.first()
        _localTtsVoiceName.value = secureSettings.localTtsVoiceName
        // The engine remembers nothing across process restarts, so the saved
        // choice has to be re-applied every time the app starts, not only when
        // the person picks a voice in this session.
        localTtsService.setVoice(_localTtsVoiceName.value)
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

    /** Clamped on the way in, so a stored nonsense value cannot cut turns short. */
    fun setVoiceSilenceSeconds(seconds: Double) {
        val clamped = VoiceTranscript.silenceSeconds(seconds)
        _voiceSilenceSeconds.value = clamped
        secureSettings.voiceSilenceSeconds = clamped
    }

    fun setTtsVoice(voice: String) {
        _ttsVoice.value = voice
        secureSettings.ttsVoice = voice
    }

    /**
     * Persists [name] and applies it to the engine in the same call, so a
     * ViewModel cannot do one and forget the other.
     */
    fun setLocalTtsVoiceName(name: String?) {
        _localTtsVoiceName.value = name
        secureSettings.localTtsVoiceName = name
        localTtsService.setVoice(name)
    }
}
