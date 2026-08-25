package com.selfward.core.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceTranscriptTest {

    // MARK: - Joining segments

    @Test
    fun segmentsAreJoinedWithASingleSpace() {
        assertEquals(
            "I saw my mother today and it was hard",
            VoiceTranscript.combined("I saw my mother today", "and it was hard")
        )
    }

    @Test
    fun anEmptySideContributesNothingAndNoStraySpace() {
        assertEquals("only this", VoiceTranscript.combined("", "only this"))
        assertEquals("only this", VoiceTranscript.combined("only this", ""))
        assertEquals("", VoiceTranscript.combined("", ""))
    }

    @Test
    fun whitespaceAroundSegmentsIsNotCarriedIn() {
        assertEquals("one two", VoiceTranscript.combined("  one  ", "  two  "))
    }

    // MARK: - Continuation vs rollover
    //
    // This is the difference between keeping what someone said and losing it.

    @Test
    fun theSameSentenceGettingLongerIsAContinuation() {
        assertTrue(VoiceTranscript.isContinuation("I feel", "I feel anxious"))
        assertTrue(VoiceTranscript.isContinuation("I feel anxious", "I feel anxious about work"))
    }

    /** The recogniser revising its guess keeps the opening. */
    @Test
    fun aRevisedEndingIsStillTheSameSentence() {
        assertTrue(VoiceTranscript.isContinuation("I feel anxious about wor", "I feel anxious about work"))
        assertTrue(VoiceTranscript.isContinuation("my mother said too", "my mother said two"))
    }

    /**
     * A rollover replaces the text with an unrelated sentence. Miscalling this
     * a continuation throws the first sentence away: the person said two things
     * and the app heard the second.
     */
    @Test
    fun anUnrelatedSentenceIsARollover() {
        assertFalse(
            VoiceTranscript.isContinuation(
                "I saw my mother yesterday",
                "work has been difficult lately"
            )
        )
    }

    /** A rollover can be shorter than what it replaced, so length cannot decide. */
    @Test
    fun aShorterUnrelatedSentenceIsStillARollover() {
        assertFalse(
            VoiceTranscript.isContinuation("I saw my mother yesterday and it was hard", "work")
        )
    }

    @Test
    fun twoSentencesSharingOnlyAWordOpeningAreNotTheSameSentence() {
        assertFalse(VoiceTranscript.isContinuation("I went home early", "I cannot stop thinking about it"))
    }

    @Test
    fun caseAndSurroundingSpaceDoNotChangeTheAnswer() {
        assertTrue(VoiceTranscript.isContinuation("I Feel Anxious", "  i feel anxious about work  "))
    }

    /** Nothing to compare against yet, so the first segment always continues. */
    @Test
    fun anEmptyPreviousSegmentIsAlwaysContinued() {
        assertTrue(VoiceTranscript.isContinuation("", "anything at all"))
        assertTrue(VoiceTranscript.isContinuation("something", ""))
    }

    // MARK: - The spoken send command

    @Test
    fun aTrailingSendCommandIsStrippedFromTheMessage() {
        assertEquals(
            "I had a hard week",
            VoiceTranscript.detectSendCommand("I had a hard week send message")
        )
    }

    @Test
    fun theCommandIsFoundWhateverPunctuationTrailsIt() {
        assertEquals(
            "I had a hard week",
            VoiceTranscript.detectSendCommand("I had a hard week, send it.")
        )
    }

    /**
     * The longest matching command wins. Matching "send" first would leave "it
     * now" stuck on the end of the message.
     */
    @Test
    fun theLongestMatchingCommandIsTheOneStripped() {
        assertEquals("I had a hard week", VoiceTranscript.detectSendCommand("I had a hard week send it now"))
        assertEquals("I had a hard week", VoiceTranscript.detectSendCommand("I had a hard week send the message"))
    }

    @Test
    fun aCommandOnItsOwnLeavesNothingToSend() {
        assertEquals("", VoiceTranscript.detectSendCommand("send"))
        assertEquals("", VoiceTranscript.detectSendCommand("Send message."))
    }

    @Test
    fun ordinarySpeechIsNotACommand() {
        assertNull(VoiceTranscript.detectSendCommand("I had a hard week"))
        assertNull(VoiceTranscript.detectSendCommand("nothing to report"))
    }

    /**
     * "Send" has to be the last thing said. Someone describing sending an email
     * is talking, not commanding, and cutting their turn off mid-sentence would
     * be worse than making them wait for the pause.
     */
    @Test
    fun theWordSendInTheMiddleOfASentenceIsNotACommand() {
        assertNull(VoiceTranscript.detectSendCommand("I had to send an email to my boss"))
        assertNull(VoiceTranscript.detectSendCommand("send it back she said"))
    }

    @Test
    fun theCommandIsRecognisedWhateverTheCase() {
        assertEquals("I had a hard week", VoiceTranscript.detectSendCommand("I had a hard week SEND IT"))
    }

    // MARK: - Silence interval

    @Test
    fun anUnsetSilenceFallsBackToTheDefault() {
        assertEquals(VoiceTranscript.DEFAULT_SILENCE_SECONDS, VoiceTranscript.silenceSeconds(null), 0.001)
        assertEquals(VoiceTranscript.DEFAULT_SILENCE_SECONDS, VoiceTranscript.silenceSeconds(0.0), 0.001)
    }

    /**
     * A stored zero or negative would end every turn instantly, cutting the
     * person off before they had said anything.
     */
    @Test
    fun aNonsensicalStoredSilenceIsNotObeyed() {
        assertEquals(VoiceTranscript.DEFAULT_SILENCE_SECONDS, VoiceTranscript.silenceSeconds(-4.0), 0.001)
    }

    @Test
    fun aStoredSilenceIsHeldInsideTheSaneRange() {
        assertEquals(VoiceTranscript.MIN_SILENCE_SECONDS, VoiceTranscript.silenceSeconds(0.5), 0.001)
        assertEquals(VoiceTranscript.MAX_SILENCE_SECONDS, VoiceTranscript.silenceSeconds(90.0), 0.001)
        assertEquals(7.0, VoiceTranscript.silenceSeconds(7.0), 0.001)
    }
}
