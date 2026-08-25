package com.selfward.core.dashboard

import com.selfward.core.graph.GraphNode
import com.selfward.core.modality.TherapyModality
import com.selfward.core.repository.SessionSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardTest {

    private fun session(id: String) = SessionSummary(id, "Session $id", 1_700_000_000_000L)

    private fun node(label: String, kind: String?, strength: Float = 1.0f) =
        GraphNode("n_$label", label, kind, 1_700_000_000_000L, strength)

    private fun global(
        sessions: List<SessionSummary> = listOf(session("a")),
        messages: List<MessageTally> = emptyList(),
        modalities: Map<String, Int> = emptyMap(),
        nodes: List<GraphNode> = emptyList(),
        notes: List<Tally> = emptyList(),
        dreams: List<Tally> = emptyList()
    ) = Dashboard.global(sessions, messages, modalities, nodes, notes, dreams)

    // MARK: - Totals

    @Test
    fun messagesAreTotalledAcrossEverySession() {
        val stats = global(
            sessions = listOf(session("a"), session("b")),
            messages = listOf(MessageTally("a", 10, 5), MessageTally("b", 4, 2))
        )

        assertEquals(14, stats.messages)
        assertEquals(7, stats.messagesFromYou)
    }

    @Test
    fun notesAndDreamsAreTotalled() {
        val stats = global(
            notes = listOf(Tally("a", 3), Tally("b", 2)),
            dreams = listOf(Tally("a", 1))
        )

        assertEquals(5, stats.notes)
        assertEquals(1, stats.dreams)
    }

    /**
     * A session that was started and never spoken in is not a conversation. It
     * still counts as a session, so both numbers exist separately.
     */
    @Test
    fun aSessionWithNoMessagesIsCountedButIsNotAConversation() {
        val stats = global(
            sessions = listOf(session("a"), session("b")),
            messages = listOf(MessageTally("a", 6, 3))
        )

        assertEquals(2, stats.sessions)
        assertEquals(1, stats.sessionsWithMessages)
    }

    @Test
    fun nodesAreSplitByWhatKindOfThingTheyAre() {
        val stats = global(
            nodes = listOf(
                node("Anxious", "emotion"),
                node("Guilty", "emotion"),
                node("Mother", "person"),
                node("Work", "theme")
            )
        )

        assertEquals(4, stats.patterns)
        assertEquals(2, stats.feelings)
        assertEquals(1, stats.people)
    }

    // MARK: - Themes and feelings

    /**
     * The same theme raised in six sessions is one theme in this person's life.
     * Listing it six times would be a chip wall that says nothing.
     */
    @Test
    fun aThemeRaisedRepeatedlyIsListedOnce() {
        val stats = global(
            nodes = listOf(
                node("Work", "theme"),
                node("work", "theme"),
                node("WORK", "theme"),
                node("Family", "theme")
            )
        )

        assertEquals(listOf("Work", "Family"), stats.themes)
    }

    @Test
    fun theStrongestFeelingsComeFirst() {
        val stats = global(
            nodes = listOf(
                node("Tired", "emotion", strength = 1.0f),
                node("Anxious", "emotion", strength = 2.0f),
                node("Hopeful", "emotion", strength = 1.5f)
            )
        )

        assertEquals(listOf("Anxious", "Hopeful", "Tired"), stats.topFeelings)
    }

    @Test
    fun onlyAHandfulOfThemesAndFeelingsAreOffered() {
        val stats = global(
            nodes = (1..30).map { node("Theme $it", "theme") } +
                (1..30).map { node("Feeling $it", "emotion", strength = it.toFloat()) }
        )

        assertTrue(stats.themes.size <= 8)
        assertTrue(stats.topFeelings.size <= 5)
    }

    // MARK: - Modalities

    @Test
    fun modalitiesAreNamedAsTheyAreToTheClientNotAsStored() {
        val stats = global(modalities = mapOf(TherapyModality.TALK.name to 5))

        assertEquals(listOf(TherapyModality.TALK.label to 5), stats.modalities)
        assertFalse(stats.modalities.any { it.first == "TALK" })
    }

    @Test
    fun theMostUsedModalityComesFirst() {
        val stats = global(
            modalities = mapOf(
                TherapyModality.TALK.name to 2,
                TherapyModality.DREAM.name to 9
            )
        )

        assertEquals(TherapyModality.DREAM.label, stats.modalities.first().first)
    }

    /**
     * A modality added to the app later must still show up in the totals rather
     * than vanishing because this code has not heard of it.
     */
    @Test
    fun anUnrecognisedModalityIsStillCounted() {
        val stats = global(modalities = mapOf("SOMETHING_NEW" to 3))

        assertEquals(1, stats.modalities.size)
        assertEquals(3, stats.modalities.first().second)
    }

    @Test
    fun aModalityNobodyUsedIsNotListed() {
        val stats = global(modalities = mapOf(TherapyModality.TALK.name to 0))

        assertTrue(stats.modalities.isEmpty())
    }

    /** Equal counts must not reshuffle between visits to the tab. */
    @Test
    fun equalModalityCountsAreOrderedStably() {
        val counts = mapOf(
            TherapyModality.TALK.name to 4,
            TherapyModality.DREAM.name to 4,
            TherapyModality.JOURNAL.name to 4
        )

        val reversed: Map<String, Int> =
            counts.entries.reversed().associate { entry -> entry.key to entry.value }

        assertEquals(global(modalities = counts).modalities, global(modalities = reversed).modalities)
    }

    // MARK: - Emptiness

    @Test
    fun aFreshInstallHasNothingToShow() {
        assertTrue(global(sessions = emptyList()).isEmpty)
    }

    /**
     * Someone can write notes before any pattern has been drawn out of them.
     * Treating that as empty would hide what they had actually done.
     */
    @Test
    fun notesAloneAreEnoughToHaveSomethingToShow() {
        assertFalse(global(notes = listOf(Tally("a", 1))).isEmpty)
    }

    @Test
    fun messagesAloneAreEnoughToHaveSomethingToShow() {
        assertFalse(global(messages = listOf(MessageTally("a", 2, 1))).isEmpty)
    }

    // MARK: - Per session

    @Test
    fun eachSessionGetsItsOwnCounts() {
        val perSession = Dashboard.perSession(
            sessions = listOf(session("a"), session("b")),
            messages = listOf(MessageTally("a", 10, 5), MessageTally("b", 2, 1)),
            nodes = listOf(Tally("a", 4)),
            notes = listOf(Tally("b", 1)),
            dreams = emptyList()
        )

        assertEquals(SessionStats(messages = 10, patterns = 4), perSession["a"])
        assertEquals(SessionStats(messages = 2, notes = 1), perSession["b"])
    }

    /**
     * A row with no counts must be distinguishable from a row whose counts have
     * not arrived, so every session gets an entry either way.
     */
    @Test
    fun aSessionWithNothingInItStillHasAnEntry() {
        val perSession = Dashboard.perSession(
            sessions = listOf(session("empty")),
            messages = emptyList(),
            nodes = emptyList(),
            notes = emptyList(),
            dreams = emptyList()
        )

        assertEquals(SessionStats(), perSession["empty"])
        assertTrue(perSession.getValue("empty").isEmpty)
    }

    /**
     * Counts belonging to a session that is no longer listed — deleted while
     * the tallies were being read — must not invent a row.
     */
    @Test
    fun countsForAnUnknownSessionAreIgnored() {
        val perSession = Dashboard.perSession(
            sessions = listOf(session("a")),
            messages = listOf(MessageTally("a", 1, 1), MessageTally("gone", 99, 99)),
            nodes = emptyList(),
            notes = emptyList(),
            dreams = emptyList()
        )

        assertEquals(setOf("a"), perSession.keys)
    }
}
