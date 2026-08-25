package com.selfward.core.voice

/** Where the loop has got to. */
enum class VoicePhase { IDLE, LISTENING, THINKING, SPEAKING }

/** Something that happened, from the recogniser, the model, or the person. */
sealed interface VoiceInput {
    /** The recogniser's running guess at the current segment. */
    data class Partial(val text: String) : VoiceInput

    /** The recogniser settling a segment. It may start another after this. */
    data class Final(val text: String) : VoiceInput

    /** The recogniser stopped on its own without settling anything. */
    data object RecognizerEnded : VoiceInput

    /** The recogniser gave up. [heardNothing] separates quiet from a real fault. */
    data class RecognizerFailed(val heardNothing: Boolean) : VoiceInput

    /** The quiet that ends a turn has elapsed. */
    data object SilenceElapsed : VoiceInput

    data class ReplyReady(val text: String) : VoiceInput
    data class ReplyFailed(val message: String) : VoiceInput
    data object SpeechFinished : VoiceInput

    /** The person switched the loop off. */
    data object Stop : VoiceInput
}

/** Something for the driver to do. Nothing here touches audio itself. */
sealed interface VoiceAction {
    data object StartListening : VoiceAction
    data object StopListening : VoiceAction

    /** Start, or restart, the countdown to the end of a turn. */
    data object ArmSilence : VoiceAction
    data object CancelSilence : VoiceAction

    /** The finished turn, to be sent to the model. */
    data class Send(val text: String) : VoiceAction
    data class Speak(val text: String) : VoiceAction
    data class Failed(val message: String) : VoiceAction
    data object Ended : VoiceAction
}

/**
 * The hands-free loop: listening → a pause → thinking → speaking → listening.
 *
 * This is the whole of the decision-making, as a state machine over events, and
 * it owns no microphone, no recogniser and no timer. That is deliberate. The
 * behaviour worth being sure about — that a rolled-over sentence is kept, that
 * the mic is closed before the app speaks, that a run of failures gives up
 * instead of spinning — is all sequencing, and sequencing can be tested exactly
 * only when it is not tangled up with hardware that has to be waited on.
 *
 * The counterpart of iOS `VoiceConversationController`.
 */
class VoiceConversation(
    /** How many failures in a row before the loop stops and says so. */
    private val maxConsecutiveFailures: Int = 3
) {

    var phase: VoicePhase = VoicePhase.IDLE
        private set

    /** What has been heard so far this turn, for showing as it is said. */
    val heardSoFar: String
        get() = VoiceTranscript.combined(committed, segment)

    private var committed = ""
    private var segment = ""
    private var failures = 0

    /** Engages the loop and starts the first turn. */
    fun start(): List<VoiceAction> {
        if (phase != VoicePhase.IDLE) return emptyList()
        reset()
        phase = VoicePhase.LISTENING
        return listOf(VoiceAction.StartListening, VoiceAction.ArmSilence)
    }

    fun handle(input: VoiceInput): List<VoiceAction> = when (input) {
        is VoiceInput.Stop -> stop()
        is VoiceInput.Partial -> onPartial(input.text)
        is VoiceInput.Final -> onFinal(input.text)
        is VoiceInput.RecognizerEnded -> onRecognizerEnded()
        is VoiceInput.RecognizerFailed -> onRecognizerFailed(input.heardNothing)
        is VoiceInput.SilenceElapsed -> onSilenceElapsed()
        is VoiceInput.ReplyReady -> onReplyReady(input.text)
        is VoiceInput.ReplyFailed -> onReplyFailed(input.message)
        is VoiceInput.SpeechFinished -> onSpeechFinished()
    }

    private fun stop(): List<VoiceAction> {
        if (phase == VoicePhase.IDLE) return emptyList()
        reset()
        phase = VoicePhase.IDLE
        return listOf(VoiceAction.CancelSilence, VoiceAction.StopListening, VoiceAction.Ended)
    }

    /**
     * A revised guess at the current segment.
     *
     * When the recogniser has rolled over to a new sentence the old one is
     * committed first, because it is something the person actually said and
     * nothing else will hand it back.
     */
    private fun onPartial(text: String): List<VoiceAction> {
        if (phase != VoicePhase.LISTENING) return emptyList()
        if (text.isBlank()) return emptyList()

        if (!VoiceTranscript.isContinuation(segment, text)) {
            committed = VoiceTranscript.combined(committed, segment)
        }
        segment = text
        failures = 0

        // Any speech at all restarts the countdown: only real trailing quiet
        // should end a turn, not a pause between two words.
        return listOf(VoiceAction.ArmSilence) + sendCommandActions()
    }

    /** The recogniser settling a segment; more of the turn may still follow. */
    private fun onFinal(text: String): List<VoiceAction> {
        if (phase != VoicePhase.LISTENING) return emptyList()
        if (text.isNotBlank()) {
            if (!VoiceTranscript.isContinuation(segment, text)) {
                committed = VoiceTranscript.combined(committed, segment)
            }
            segment = text
        }
        committed = VoiceTranscript.combined(committed, segment)
        segment = ""
        failures = 0

        val commanded = sendCommandActions()
        if (commanded.isNotEmpty()) return commanded

        // The turn is not over just because the recogniser stopped: someone
        // pausing for breath mid-thought has not finished. Listen again and let
        // the silence decide.
        return listOf(VoiceAction.StartListening, VoiceAction.ArmSilence)
    }

    private fun onRecognizerEnded(): List<VoiceAction> {
        if (phase != VoicePhase.LISTENING) return emptyList()
        return listOf(VoiceAction.StartListening)
    }

    /**
     * Hearing nothing is not a fault — it is what a quiet room sounds like — so
     * it does not count towards giving up. Anything else does, and a run of them
     * means the recogniser is not going to work this time.
     */
    private fun onRecognizerFailed(heardNothing: Boolean): List<VoiceAction> {
        if (phase != VoicePhase.LISTENING) return emptyList()
        if (heardNothing) return listOf(VoiceAction.StartListening)

        failures++
        if (failures >= maxConsecutiveFailures) {
            reset()
            phase = VoicePhase.IDLE
            return listOf(
                VoiceAction.CancelSilence,
                VoiceAction.StopListening,
                VoiceAction.Failed("Speech recognition keeps failing. Voice mode is off."),
                VoiceAction.Ended
            )
        }
        return listOf(VoiceAction.StartListening)
    }

    /** The quiet ran out. If anything was said, that is the turn. */
    private fun onSilenceElapsed(): List<VoiceAction> {
        if (phase != VoicePhase.LISTENING) return emptyList()

        val spoken = heardSoFar.trim()
        if (spoken.length < VoiceTranscript.MIN_CHARACTERS) {
            // Nothing worth sending. Keep waiting rather than sending a cough
            // to the model, or dropping out of voice mode on the person.
            return listOf(VoiceAction.ArmSilence)
        }
        return endTurn(spoken)
    }

    private fun sendCommandActions(): List<VoiceAction> {
        val stripped = VoiceTranscript.detectSendCommand(heardSoFar) ?: return emptyList()
        // "Send" with nothing before it: obey the intent to stop talking, but
        // there is no message, so wait rather than send an empty turn.
        if (stripped.length < VoiceTranscript.MIN_CHARACTERS) return emptyList()
        return endTurn(stripped)
    }

    /**
     * Closes the turn and hands it over.
     *
     * The mic is stopped here rather than when speech starts, so it is already
     * shut before a single word is spoken back. Leaving it open would let the
     * app hear its own voice and answer itself.
     */
    private fun endTurn(text: String): List<VoiceAction> {
        committed = ""
        segment = ""
        phase = VoicePhase.THINKING
        return listOf(
            VoiceAction.CancelSilence,
            VoiceAction.StopListening,
            VoiceAction.Send(text)
        )
    }

    private fun onReplyReady(text: String): List<VoiceAction> {
        if (phase != VoicePhase.THINKING) return emptyList()
        if (text.isBlank()) return resumeListening()
        phase = VoicePhase.SPEAKING
        return listOf(VoiceAction.Speak(text))
    }

    /**
     * A failed reply ends the loop rather than quietly listening again. Someone
     * talking to a phone that has stopped answering deserves to be told, not
     * left waiting for a reply that is not coming.
     */
    private fun onReplyFailed(message: String): List<VoiceAction> {
        if (phase != VoicePhase.THINKING) return emptyList()
        reset()
        phase = VoicePhase.IDLE
        return listOf(VoiceAction.Failed(message), VoiceAction.Ended)
    }

    private fun onSpeechFinished(): List<VoiceAction> {
        if (phase != VoicePhase.SPEAKING) return emptyList()
        return resumeListening()
    }

    private fun resumeListening(): List<VoiceAction> {
        reset()
        phase = VoicePhase.LISTENING
        return listOf(VoiceAction.StartListening, VoiceAction.ArmSilence)
    }

    private fun reset() {
        committed = ""
        segment = ""
        failures = 0
    }
}
