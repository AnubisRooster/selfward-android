package com.selfward.core.dashboard

import com.selfward.core.graph.GraphNode
import com.selfward.core.modality.TherapyModality
import com.selfward.core.repository.SessionSummary

/** What one session amounts to, as shown on its row in the session list. */
data class SessionStats(
    val messages: Int = 0,
    val notes: Int = 0,
    val dreams: Int = 0,
    val patterns: Int = 0
) {
    /** True when there is nothing yet worth putting on the row. */
    val isEmpty: Boolean get() = messages == 0 && notes == 0 && dreams == 0 && patterns == 0
}

/** The whole picture, across every session. */
data class GlobalStats(
    val sessions: Int = 0,
    /** Sessions that were actually talked in, as opposed to started and left. */
    val sessionsWithMessages: Int = 0,
    val messages: Int = 0,
    val messagesFromYou: Int = 0,
    val patterns: Int = 0,
    val feelings: Int = 0,
    val people: Int = 0,
    val notes: Int = 0,
    val dreams: Int = 0,
    /** Message counts per modality label, largest first. */
    val modalities: List<Pair<String, Int>> = emptyList(),
    val themes: List<String> = emptyList(),
    val topFeelings: List<String> = emptyList()
) {
    val isEmpty: Boolean get() = messages == 0 && patterns == 0 && notes == 0 && dreams == 0
}

/**
 * Turns the stored counts into the numbers the Insights tab and the session
 * list show, mirroring what iOS `DashboardService` computes.
 *
 * Pure, and separate from where the counts came from, so the arithmetic can be
 * tested without a database.
 */
object Dashboard {

    /** The graph kinds that are feelings, and the ones that are people. */
    private const val KIND_EMOTION = "emotion"
    private const val KIND_PERSON = "person"
    private const val KIND_THEME = "theme"

    private const val MAX_THEMES = 8
    private const val MAX_FEELINGS = 5

    fun global(
        sessions: List<SessionSummary>,
        messages: List<MessageTally>,
        modalities: Map<String, Int>,
        nodes: List<GraphNode>,
        notes: List<Tally>,
        dreams: List<Tally>
    ): GlobalStats {
        val byKind = nodes.groupBy { it.kind }
        return GlobalStats(
            sessions = sessions.size,
            sessionsWithMessages = messages.count { it.total > 0 },
            messages = messages.sumOf { it.total },
            messagesFromYou = messages.sumOf { it.fromYou },
            patterns = nodes.size,
            feelings = byKind[KIND_EMOTION].orEmpty().size,
            people = byKind[KIND_PERSON].orEmpty().size,
            notes = notes.sumOf { it.count },
            dreams = dreams.sumOf { it.count },
            modalities = rankModalities(modalities),
            // Themes are distinct labels: the same theme raised in six sessions
            // is one theme in this person's life, not six.
            themes = byKind[KIND_THEME].orEmpty()
                .distinctLabels()
                .take(MAX_THEMES),
            topFeelings = byKind[KIND_EMOTION].orEmpty()
                .sortedByDescending { it.strength }
                .distinctLabels()
                .take(MAX_FEELINGS)
        )
    }

    /**
     * Per-session counts, keyed by session id.
     *
     * Every session in [sessions] gets an entry, including ones with nothing in
     * them — a row with no counts should show no badges, which is not the same
     * as a row whose counts have not loaded.
     */
    fun perSession(
        sessions: List<SessionSummary>,
        messages: List<MessageTally>,
        nodes: List<Tally>,
        notes: List<Tally>,
        dreams: List<Tally>
    ): Map<String, SessionStats> {
        val messagesById = messages.associateBy { it.sessionId }
        val nodesById = nodes.associate { it.sessionId to it.count }
        val notesById = notes.associate { it.sessionId to it.count }
        val dreamsById = dreams.associate { it.sessionId to it.count }

        return sessions.associate { session ->
            session.id to SessionStats(
                messages = messagesById[session.id]?.total ?: 0,
                notes = notesById[session.id] ?: 0,
                dreams = dreamsById[session.id] ?: 0,
                patterns = nodesById[session.id] ?: 0
            )
        }
    }

    /**
     * Modality counts as readable labels, largest first.
     *
     * Messages are stored with the modality's enum name, which is not what to
     * put on screen. An unrecognised name is carried through rather than
     * dropped, so a modality added later still appears instead of silently
     * going missing from the totals.
     */
    private fun rankModalities(counts: Map<String, Int>): List<Pair<String, Int>> =
        counts.entries
            .filter { it.value > 0 }
            .map { (name, count) -> labelFor(name) to count }
            .sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first })

    private fun labelFor(storedName: String): String =
        TherapyModality.entries.firstOrNull { it.name == storedName }?.label
            ?: storedName.lowercase().replaceFirstChar { it.uppercase() }

    /** Distinct by label, ignoring case, keeping the first spelling seen. */
    private fun List<GraphNode>.distinctLabels(): List<String> =
        distinctBy { it.label.lowercase() }.map { it.label }
}
