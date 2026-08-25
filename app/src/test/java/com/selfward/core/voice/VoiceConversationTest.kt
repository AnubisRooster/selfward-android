package com.selfward.core.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The loop's sequencing, driven entirely through events.
 *
 * No microphone, no recogniser, no timer — which is the point. The things worth
 * being certain of here are all orderings: that the mic is shut before the app
 * speaks, that a rolled-over sentence survives, that a run of failures stops
 * instead of spinning. Those can only be pinned down exactly when they are not
 * tangled up with hardware that has to be waited on.
 */
class VoiceConversationTest {

    private fun listening(): VoiceConversation =
        VoiceConversation().apply { start() }

    /** Drives to the point where a turn has been sent. */
    private fun thinking(said: String = "I had a hard week"): VoiceConversation =
        listening().apply {
            handle(VoiceInput.Partial(said))
            handle(VoiceInput.SilenceElapsed)
        }

    // MARK: - Starting and stopping

    @Test
    fun startingOpensTheMicAndBeginsTheCountdown() {
        val conversation = VoiceConversation()

        val actions = conversation.start()

        assertEquals(
            listOf(VoiceAction.StartListening, VoiceAction.ArmSilence),
            actions
        )
        assertEquals(VoicePhase.LISTENING, conversation.phase)
    }

    @Test
    fun startingTwiceDoesNotOpenASecondRecogniser() {
        val conversation = listening()

        assertTrue(conversation.start().isEmpty())
    }

    @Test
    fun stoppingClosesTheMicAndCancelsTheCountdown() {
        val conversation = listening()

        val actions = conversation.handle(VoiceInput.Stop)

        assertEquals(
            listOf(VoiceAction.CancelSilence, VoiceAction.StopListening, VoiceAction.Ended),
            actions
        )
        assertEquals(VoicePhase.IDLE, conversation.phase)
    }

    /** Switching off mid-reply must still shut everything down. */
    @Test
    fun stoppingWhileSpeakingStillEndsTheLoop() {
        val conversation = thinking()
        conversation.handle(VoiceInput.ReplyReady("Tell me more."))

        conversation.handle(VoiceInput.Stop)

        assertEquals(VoicePhase.IDLE, conversation.phase)
    }

    @Test
    fun stoppingWhenAlreadyIdleDoesNothing() {
        assertTrue(VoiceConversation().handle(VoiceInput.Stop).isEmpty())
    }

    // MARK: - Hearing a turn

    @Test
    fun speechRestartsTheCountdownSoAPauseForBreathIsNotTheEndOfATurn() {
        val conversation = listening()

        val actions = conversation.handle(VoiceInput.Partial("I have been"))

        assertEquals(listOf(VoiceAction.ArmSilence), actions)
        assertEquals(VoicePhase.LISTENING, conversation.phase)
    }

    @Test
    fun theRunningTranscriptIsWhatHasBeenHeardSoFar() {
        val conversation = listening()

        conversation.handle(VoiceInput.Partial("I have been"))
        conversation.handle(VoiceInput.Partial("I have been thinking"))

        assertEquals("I have been thinking", conversation.heardSoFar)
    }

    /**
     * The recogniser rolling over to a new sentence mid-turn. Without keeping
     * the first one, the person says two sentences and the app hears the
     * second.
     */
    @Test
    fun aSentenceTheRecogniserRollsPastIsKept() {
        val conversation = listening()

        conversation.handle(VoiceInput.Partial("I saw my mother yesterday"))
        conversation.handle(VoiceInput.Partial("work has been difficult"))

        assertEquals("I saw my mother yesterday work has been difficult", conversation.heardSoFar)
    }

    @Test
    fun aRevisedSentenceReplacesRatherThanRepeatsItself() {
        val conversation = listening()

        conversation.handle(VoiceInput.Partial("I feel anx"))
        conversation.handle(VoiceInput.Partial("I feel anxious"))

        assertEquals("I feel anxious", conversation.heardSoFar)
    }

    /**
     * The recogniser settling a segment does not mean the person has finished —
     * it stops on its own after a stretch of speech. The turn ends on silence,
     * not on the recogniser's convenience.
     */
    @Test
    fun aSettledSegmentListensAgainRatherThanEndingTheTurn() {
        val conversation = listening()

        val actions = conversation.handle(VoiceInput.Final("I saw my mother yesterday"))

        assertEquals(listOf(VoiceAction.StartListening, VoiceAction.ArmSilence), actions)
        assertEquals(VoicePhase.LISTENING, conversation.phase)
    }

    @Test
    fun aLongTurnSpanningSeveralSegmentsIsStitchedTogether() {
        val conversation = listening()

        conversation.handle(VoiceInput.Final("I saw my mother yesterday"))
        conversation.handle(VoiceInput.Partial("and it was harder than I expected"))

        assertEquals(
            "I saw my mother yesterday and it was harder than I expected",
            conversation.heardSoFar
        )
    }

    @Test
    fun theRecogniserStoppingOnItsOwnJustStartsItAgain() {
        val conversation = listening()

        assertEquals(
            listOf(VoiceAction.StartListening),
            conversation.handle(VoiceInput.RecognizerEnded)
        )
    }

    // MARK: - Ending a turn

    @Test
    fun silenceEndsTheTurnAndSendsWhatWasSaid() {
        val conversation = listening()
        conversation.handle(VoiceInput.Partial("I had a hard week"))

        val actions = conversation.handle(VoiceInput.SilenceElapsed)

        assertEquals(
            listOf(
                VoiceAction.CancelSilence,
                VoiceAction.StopListening,
                VoiceAction.Send("I had a hard week")
            ),
            actions
        )
        assertEquals(VoicePhase.THINKING, conversation.phase)
    }

    /**
     * The mic is closed as part of ending the turn, before anything is spoken
     * back. Left open, the app hears its own reply and answers itself.
     */
    @Test
    fun theMicIsClosedBeforeTheReplyIsEverSpoken() {
        val conversation = listening()
        conversation.handle(VoiceInput.Partial("hello"))

        val ending = conversation.handle(VoiceInput.SilenceElapsed)
        val speaking = conversation.handle(VoiceInput.ReplyReady("Hello. What is on your mind?"))

        // The mic is shut before the turn is even handed over, so there is no
        // window in which it is open while a reply could arrive and be spoken.
        val closed = ending.indexOf(VoiceAction.StopListening)
        val sent = ending.indexOfFirst { it is VoiceAction.Send }
        assertTrue("mic was never closed", closed >= 0)
        assertTrue("turn was sent before the mic closed", closed < sent)
        assertFalse(speaking.any { it is VoiceAction.StartListening })
    }

    /**
     * A cough in a quiet room. Sending it would put noise in front of the model
     * and an empty message in the transcript; dropping out of voice mode would
     * punish someone for shifting in their chair.
     */
    @Test
    fun aBlipTooShortToBeSpeechKeepsWaitingInsteadOfSending() {
        val conversation = listening()
        conversation.handle(VoiceInput.Partial("a"))

        val actions = conversation.handle(VoiceInput.SilenceElapsed)

        assertEquals(listOf(VoiceAction.ArmSilence), actions)
        assertEquals(VoicePhase.LISTENING, conversation.phase)
    }

    @Test
    fun silenceWithNothingSaidAtAllKeepsWaiting() {
        val conversation = listening()

        assertEquals(listOf(VoiceAction.ArmSilence), conversation.handle(VoiceInput.SilenceElapsed))
        assertEquals(VoicePhase.LISTENING, conversation.phase)
    }

    @Test
    fun sayingSendEndsTheTurnWithoutWaitingForTheSilence() {
        val conversation = listening()

        val actions = conversation.handle(VoiceInput.Partial("I had a hard week send it"))

        assertTrue(actions.contains(VoiceAction.Send("I had a hard week")))
        assertEquals(VoicePhase.THINKING, conversation.phase)
    }

    @Test
    fun sayingSendWithNothingBeforeItDoesNotSendAnEmptyTurn() {
        val conversation = listening()

        val actions = conversation.handle(VoiceInput.Partial("send"))

        assertFalse(actions.any { it is VoiceAction.Send })
        assertEquals(VoicePhase.LISTENING, conversation.phase)
    }

    @Test
    fun theTranscriptIsClearedOnceATurnHasBeenSent() {
        val conversation = thinking("I had a hard week")

        assertEquals("", conversation.heardSoFar)
    }

    // MARK: - Replying, and going round again

    @Test
    fun aReplyIsSpokenAloud() {
        val conversation = thinking()

        val actions = conversation.handle(VoiceInput.ReplyReady("That sounds heavy."))

        assertEquals(listOf(VoiceAction.Speak("That sounds heavy.")), actions)
        assertEquals(VoicePhase.SPEAKING, conversation.phase)
    }

    @Test
    fun theLoopListensAgainOnceTheReplyHasBeenSpoken() {
        val conversation = thinking()
        conversation.handle(VoiceInput.ReplyReady("That sounds heavy."))

        val actions = conversation.handle(VoiceInput.SpeechFinished)

        assertEquals(listOf(VoiceAction.StartListening, VoiceAction.ArmSilence), actions)
        assertEquals(VoicePhase.LISTENING, conversation.phase)
    }

    @Test
    fun aWholeExchangeReturnsToListeningWithACleanTranscript() {
        val conversation = thinking("I had a hard week")
        conversation.handle(VoiceInput.ReplyReady("Tell me more."))
        conversation.handle(VoiceInput.SpeechFinished)

        assertEquals("", conversation.heardSoFar)
        assertEquals(VoicePhase.LISTENING, conversation.phase)
    }

    /** An empty reply is nothing to say out loud, so just listen again. */
    @Test
    fun anEmptyReplyGoesStraightBackToListening() {
        val conversation = thinking()

        val actions = conversation.handle(VoiceInput.ReplyReady("   "))

        assertFalse(actions.any { it is VoiceAction.Speak })
        assertEquals(VoicePhase.LISTENING, conversation.phase)
    }

    /**
     * Someone talking to a phone that has stopped answering should be told, not
     * left waiting in silence for a reply that is not coming.
     */
    @Test
    fun aFailedReplyEndsTheLoopAndSaysSo() {
        val conversation = thinking()

        val actions = conversation.handle(VoiceInput.ReplyFailed("No API key."))

        assertEquals(
            listOf(VoiceAction.Failed("No API key."), VoiceAction.Ended),
            actions
        )
        assertEquals(VoicePhase.IDLE, conversation.phase)
    }

    // MARK: - Failures

    /** A quiet room is not a fault, and must never count towards giving up. */
    @Test
    fun hearingNothingJustListensAgainForever() {
        val conversation = listening()

        repeat(20) {
            assertEquals(
                listOf(VoiceAction.StartListening),
                conversation.handle(VoiceInput.RecognizerFailed(heardNothing = true))
            )
        }
        assertEquals(VoicePhase.LISTENING, conversation.phase)
    }

    @Test
    fun aSingleRealFailureIsRetriedRatherThanGivingUp() {
        val conversation = listening()

        assertEquals(
            listOf(VoiceAction.StartListening),
            conversation.handle(VoiceInput.RecognizerFailed(heardNothing = false))
        )
        assertEquals(VoicePhase.LISTENING, conversation.phase)
    }

    /**
     * Without this the loop restarts a recogniser that cannot start, forever,
     * with the mic held open and the person told nothing.
     */
    @Test
    fun aRunOfRealFailuresStopsTheLoopAndExplainsWhy() {
        val conversation = listening()

        conversation.handle(VoiceInput.RecognizerFailed(heardNothing = false))
        conversation.handle(VoiceInput.RecognizerFailed(heardNothing = false))
        val actions = conversation.handle(VoiceInput.RecognizerFailed(heardNothing = false))

        assertTrue(actions.contains(VoiceAction.StopListening))
        assertTrue(actions.any { it is VoiceAction.Failed })
        assertTrue(actions.contains(VoiceAction.Ended))
        assertEquals(VoicePhase.IDLE, conversation.phase)
    }

    /** Hearing something proves the recogniser works, so the count starts over. */
    @Test
    fun successfulSpeechClearsEarlierFailures() {
        val conversation = listening()

        conversation.handle(VoiceInput.RecognizerFailed(heardNothing = false))
        conversation.handle(VoiceInput.RecognizerFailed(heardNothing = false))
        conversation.handle(VoiceInput.Partial("I am still here"))
        conversation.handle(VoiceInput.RecognizerFailed(heardNothing = false))

        assertEquals(VoicePhase.LISTENING, conversation.phase)
    }

    // MARK: - Events arriving in the wrong phase
    //
    // The recogniser and the speech engine both deliver late callbacks after
    // being told to stop, so every one of these can really happen.

    @Test
    fun aLateTranscriptArrivingWhileThinkingIsIgnored() {
        val conversation = thinking()

        assertTrue(conversation.handle(VoiceInput.Partial("stray words")).isEmpty())
        assertEquals(VoicePhase.THINKING, conversation.phase)
    }

    @Test
    fun aLateTranscriptArrivingWhileSpeakingIsIgnored() {
        val conversation = thinking()
        conversation.handle(VoiceInput.ReplyReady("Tell me more."))

        assertTrue(conversation.handle(VoiceInput.Partial("its own voice")).isEmpty())
        assertEquals(VoicePhase.SPEAKING, conversation.phase)
    }

    @Test
    fun aSilenceTimerFiringAfterTheTurnEndedIsIgnored() {
        val conversation = thinking()

        assertTrue(conversation.handle(VoiceInput.SilenceElapsed).isEmpty())
        assertEquals(VoicePhase.THINKING, conversation.phase)
    }

    @Test
    fun aReplyArrivingAfterTheLoopWasStoppedIsIgnored() {
        val conversation = thinking()
        conversation.handle(VoiceInput.Stop)

        assertTrue(conversation.handle(VoiceInput.ReplyReady("too late")).isEmpty())
        assertEquals(VoicePhase.IDLE, conversation.phase)
    }

    @Test
    fun speechFinishingWhenNothingWasBeingSpokenIsIgnored() {
        val conversation = listening()

        assertTrue(conversation.handle(VoiceInput.SpeechFinished).isEmpty())
        assertEquals(VoicePhase.LISTENING, conversation.phase)
    }

    @Test
    fun aSecondReplyForTheSameTurnIsIgnored() {
        val conversation = thinking()
        conversation.handle(VoiceInput.ReplyReady("first"))

        assertTrue(conversation.handle(VoiceInput.ReplyReady("second")).isEmpty())
    }
}
