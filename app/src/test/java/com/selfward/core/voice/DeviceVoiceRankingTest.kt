package com.selfward.core.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceVoiceRankingTest {

    private fun voice(name: String, tier: VoiceTier, locale: String = "en-US") =
        DeviceVoice(name, locale, tier)

    // MARK: - Quality score -> tier

    @Test
    fun theTopTwoAndroidQualityLevelsAreBothPremium() {
        assertEquals(VoiceTier.PREMIUM, DeviceVoiceRanking.tierFor(500))
        assertEquals(VoiceTier.PREMIUM, DeviceVoiceRanking.tierFor(400))
    }

    @Test
    fun theMiddleQualityLevelIsEnhanced() {
        assertEquals(VoiceTier.ENHANCED, DeviceVoiceRanking.tierFor(300))
    }

    @Test
    fun theBottomTwoQualityLevelsAreBothStandard() {
        assertEquals(VoiceTier.STANDARD, DeviceVoiceRanking.tierFor(200))
        assertEquals(VoiceTier.STANDARD, DeviceVoiceRanking.tierFor(100))
    }

    /** A quality score the SDK constants do not name should not crash the grouping. */
    @Test
    fun anUnrecognisedScoreFallsBackToStandardRatherThanThrowing() {
        assertEquals(VoiceTier.STANDARD, DeviceVoiceRanking.tierFor(0))
        assertEquals(VoiceTier.STANDARD, DeviceVoiceRanking.tierFor(-1))
    }

    // MARK: - Filtering and ordering

    @Test
    fun onlyEnglishVoicesAreOffered() {
        val voices = listOf(
            voice("Anna", VoiceTier.PREMIUM, locale = "de-DE"),
            voice("Amy", VoiceTier.PREMIUM, locale = "en-GB")
        )

        assertEquals(listOf("Amy"), DeviceVoiceRanking.englishVoices(voices).map { it.name })
    }

    @Test
    fun premiumSortsBeforeEnhancedBeforeStandard() {
        val voices = listOf(
            voice("Standard One", VoiceTier.STANDARD),
            voice("Premium One", VoiceTier.PREMIUM),
            voice("Enhanced One", VoiceTier.ENHANCED)
        )

        assertEquals(
            listOf("Premium One", "Enhanced One", "Standard One"),
            DeviceVoiceRanking.englishVoices(voices).map { it.name }
        )
    }

    @Test
    fun withinATierVoicesAreAlphabetical() {
        val voices = listOf(
            voice("Zoe", VoiceTier.PREMIUM),
            voice("Ava", VoiceTier.PREMIUM),
            voice("Nathan", VoiceTier.PREMIUM)
        )

        assertEquals(
            listOf("Ava", "Nathan", "Zoe"),
            DeviceVoiceRanking.englishVoices(voices).map { it.name }
        )
    }

    @Test
    fun anEmptyDeviceOffersAnEmptyList() {
        assertTrue(DeviceVoiceRanking.englishVoices(emptyList()).isEmpty())
    }

    // MARK: - Grouping

    @Test
    fun groupedSplitsByTier() {
        val voices = listOf(
            voice("Ava", VoiceTier.PREMIUM),
            voice("Sam", VoiceTier.ENHANCED),
            voice("Robo", VoiceTier.STANDARD)
        )

        val grouped = DeviceVoiceRanking.grouped(voices)

        assertEquals(listOf("Ava"), grouped[VoiceTier.PREMIUM]?.map { it.name })
        assertEquals(listOf("Sam"), grouped[VoiceTier.ENHANCED]?.map { it.name })
        assertEquals(listOf("Robo"), grouped[VoiceTier.STANDARD]?.map { it.name })
    }

    /**
     * A tier nobody has a voice in should not appear as an empty entry - the
     * settings screen skips a tier heading by checking whether the map has it,
     * and a present-but-empty list would draw a heading over nothing.
     */
    @Test
    fun aTierWithNoVoicesIsAbsentFromTheMapRatherThanEmpty() {
        val grouped = DeviceVoiceRanking.grouped(listOf(voice("Ava", VoiceTier.PREMIUM)))

        assertFalse(grouped.containsKey(VoiceTier.ENHANCED))
        assertFalse(grouped.containsKey(VoiceTier.STANDARD))
    }

    @Test
    fun groupedIgnoresNonEnglishVoicesTheSameAsTheFlatList() {
        val grouped = DeviceVoiceRanking.grouped(
            listOf(voice("Anna", VoiceTier.PREMIUM, locale = "de-DE"))
        )

        assertTrue(grouped.isEmpty())
    }
}
