package com.selfward.core.voice

/**
 * Stitching a spoken turn together out of what the recogniser hands back.
 *
 * Android's `SpeechRecognizer` does not give one clean transcript per turn. It
 * ends a segment on its own after a stretch of speech, and while a segment is
 * running it revises what it has heard — usually extending the same sentence,
 * but sometimes replacing it outright with a new one when it decides the person
 * has moved on. Neither of those is announced.
 *
 * Everything here is pure, and separate from the audio, because the awkward
 * cases are all about text: which of two transcripts continues the other, and
 * where a spoken command ends and the message begins. Those can be checked
 * exactly, without a microphone.
 */
object VoiceTranscript {

    /** Below this, a turn is a cough or a door closing rather than something said. */
    const val MIN_CHARACTERS = 2

    /** Seconds of trailing quiet that end a turn, and the range that is sane. */
    const val DEFAULT_SILENCE_SECONDS = 5.0
    const val MIN_SILENCE_SECONDS = 2.0
    const val MAX_SILENCE_SECONDS = 12.0

    /**
     * The silence a turn ends on, held inside a range someone can live with.
     *
     * Too short and the turn is cut off while the person is thinking mid-
     * sentence, which in this app means being interrupted while saying
     * something hard. Too long and the conversation stops feeling like one.
     * Anything unset or nonsensical falls back to the default rather than
     * being obeyed.
     */
    fun silenceSeconds(stored: Double?): Double {
        if (stored == null || stored <= 0.0) return DEFAULT_SILENCE_SECONDS
        return stored.coerceIn(MIN_SILENCE_SECONDS, MAX_SILENCE_SECONDS)
    }

    /** Joins text kept from earlier segments of this turn with the live one. */
    fun combined(committed: String, segment: String): String = when {
        committed.isBlank() -> segment.trim()
        segment.isBlank() -> committed.trim()
        else -> "${committed.trim()} ${segment.trim()}"
    }

    /**
     * Whether [current] is the recogniser still working on the same sentence as
     * [previous], rather than having rolled over to a new one.
     *
     * A revision keeps the opening and changes the end, so the two share a long
     * prefix. A rollover replaces the text with something unrelated, so they
     * share almost nothing. The comparison is against the *shorter* string's
     * length rather than a fixed number of characters, because a rollover can be
     * either longer or shorter than what it replaced.
     *
     * Getting this wrong is not subtle. Treat a rollover as a continuation and
     * the earlier sentence is thrown away — the person said two things and the
     * app heard the second one.
     */
    fun isContinuation(previous: String, current: String): Boolean {
        val before = previous.trim().lowercase()
        val after = current.trim().lowercase()
        if (before.isEmpty() || after.isEmpty()) return true

        val shared = before.zip(after).takeWhile { (a, b) -> a == b }.count()
        val shorter = minOf(before.length, after.length)
        // At least half the shorter string, and never fewer than a few
        // characters, so two sentences that happen to start "I " are not
        // mistaken for one.
        return shared >= maxOf(3, shorter / 2)
    }

    /** Spoken ways of saying "that's my turn, go ahead". */
    private val SEND_COMMANDS = listOf(
        "send message", "send the message", "send it now", "send it",
        "send now", "send"
    )

    /**
     * A trailing spoken send command, if there is one.
     *
     * @return null when nothing was said that ends the turn; an empty string
     *   when the command was the only thing said, so there is nothing to send;
     *   otherwise the message with the command taken off the end.
     */
    fun detectSendCommand(text: String): String? {
        val trimmed = text.trim().trimEnd { it.isPunctuation() }
        val lower = trimmed.lowercase()

        // Longest first, so "send it now" is not matched as "send" with "it
        // now" left dangling on the end of the message.
        SEND_COMMANDS.sortedByDescending { it.length }.forEach { command ->
            if (lower == command) return ""
            val suffix = " $command"
            if (lower.endsWith(suffix)) {
                return trimmed.dropLast(suffix.length)
                    .trimEnd { it.isWhitespace() || it.isPunctuation() }
            }
        }
        return null
    }

    private fun Char.isPunctuation(): Boolean = !isLetterOrDigit() && !isWhitespace()
}
