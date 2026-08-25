package com.selfward.core

import com.selfward.core.settings.SecureSettings
import com.selfward.core.voice.VoiceTranscript
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

    /** How long a pause has to run before voice mode treats a turn as finished. */
    private val _voiceSilenceSeconds =
        MutableStateFlow(VoiceTranscript.DEFAULT_SILENCE_SECONDS)
    val voiceSilenceSeconds = _voiceSilenceSeconds.asStateFlow()

    fun initFromSettings() {
        _useLocalModel.value = secureSettings.useLocalModel
        _localModelId.value = secureSettings.localModelId
        _useLocalTts.value = secureSettings.useLocalTts
        _voiceSilenceSeconds.value =
            VoiceTranscript.silenceSeconds(secureSettings.voiceSilenceSeconds)
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
}
