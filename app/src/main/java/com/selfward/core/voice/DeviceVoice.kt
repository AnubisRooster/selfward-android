package com.selfward.core.voice

/** How natural a voice on this device sounds, mirroring iOS's Premium/Enhanced/Standard tiers. */
enum class VoiceTier { PREMIUM, ENHANCED, STANDARD }

/** One voice `android.speech.tts.TextToSpeech` can speak with. */
data class DeviceVoice(val name: String, val locale: String, val tier: VoiceTier)

/**
 * Sorting and grouping for the on-device voice list, kept separate from
 * `android.speech.tts.Voice` so it can be tested without a `TextToSpeech`
 * engine, which only exists on a device and only after its async init callback
 * has fired.
 */
object DeviceVoiceRanking {

    /**
     * Android's `Voice.getQuality()` values, from the SDK: `QUALITY_VERY_LOW`
     * 100 through `QUALITY_VERY_HIGH` 500. Grouped into three tiers rather than
     * kept as five, to read the same way as iOS's Premium/Enhanced/Standard
     * rather than as a number nobody asked for.
     */
    fun tierFor(qualityScore: Int): VoiceTier = when {
        qualityScore >= 400 -> VoiceTier.PREMIUM
        qualityScore >= 300 -> VoiceTier.ENHANCED
        else -> VoiceTier.STANDARD
    }

    /** Every English voice, best tier first and alphabetical within a tier. */
    fun englishVoices(voices: List<DeviceVoice>): List<DeviceVoice> =
        voices
            .filter { it.locale.startsWith("en") }
            .sortedWith(compareBy({ it.tier.ordinal }, { it.name }))

    /** [voices], already ranked, split into the three tiers that have anything in them. */
    fun grouped(voices: List<DeviceVoice>): Map<VoiceTier, List<DeviceVoice>> =
        englishVoices(voices).groupBy { it.tier }
}
