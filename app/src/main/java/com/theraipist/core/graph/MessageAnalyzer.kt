package com.theraipist.core.graph

/**
 * Reads a message the client wrote and returns the people, emotions and beliefs
 * it implies, plus the relationships between them.
 *
 * Entirely local: word and phrase matching, no model and no network. That is
 * deliberate — this runs on every message the client sends, including when the
 * app is talking to an on-device model with no connection at all.
 */
object MessageAnalyzer {

    /** Node kinds this analyzer produces. Stored in [GraphNode.kind]. */
    object Kind {
        const val EMOTION = "emotion"
        const val PERSON = "person"
        const val BELIEF = "belief"
        const val THEME = "theme"
    }

    /** Edge kinds this analyzer produces. Stored in [GraphEdge.label]. */
    object Relation {
        const val TRIGGERS = "TRIGGERS"
        const val CAUSES = "CAUSES"
        const val ASSOCIATED_WITH = "ASSOCIATED_WITH"
        const val SUPPRESSES = "SUPPRESSES"
        const val COMPENSATES_FOR = "COMPENSATES_FOR"
    }

    data class NodeSpec(val kind: String, val label: String)

    data class EdgeSpec(val sourceLabel: String, val targetLabel: String, val relation: String)

    data class Extraction(val nodes: List<NodeSpec>, val edges: List<EdgeSpec>) {
        val isEmpty: Boolean get() = nodes.isEmpty()
    }

    val EMOTION_WORDS = listOf(
        "angry", "anger", "sad", "sadness", "happy", "anxious", "anxiety",
        "fearful", "fear", "guilty", "guilt", "ashamed", "shame", "hopeful",
        "lonely", "loneliness", "frustrated", "frustration", "overwhelmed",
        "hopeless", "jealous", "jealousy", "grief", "hurt", "betrayed",
        "confused", "numb", "empty", "worthless", "helpless"
    )

    private val PERSON_PATTERNS = listOf(
        "my mother" to "Mother", "my mom" to "Mother",
        "my father" to "Father", "my dad" to "Father",
        "my sister" to "Sister", "my brother" to "Brother",
        "my partner" to "Partner", "my husband" to "Husband",
        "my wife" to "Wife", "my friend" to "Friend",
        "my boss" to "Boss", "my therapist" to "Previous therapist",
        "my child" to "Child", "my daughter" to "Daughter",
        "my son" to "Son", "my colleague" to "Colleague",
        "my ex" to "Ex-partner"
    )

    private val BELIEF_PATTERNS = listOf(
        "i believe", "i think that", "i feel that", "i always", "i never",
        "i should", "i must", "i can't", "i have to", "i am worthless",
        "i am not good enough", "i am a failure", "i don't deserve",
        "nobody cares", "i am broken", "i will never"
    )

    /** How much of the text after a belief cue is kept as the belief's label. */
    private const val BELIEF_TAIL_LIMIT = 50

    fun analyze(message: String): Extraction {
        val lower = message.lowercase()

        val emotions = EMOTION_WORDS
            .filter { lower.containsWord(it) }
            .map { NodeSpec(Kind.EMOTION, it.replaceFirstChar(Char::uppercase)) }
            .dedupeByLabel()

        val persons = PERSON_PATTERNS
            .filter { (pattern, _) -> lower.contains(pattern) }
            .map { (_, label) -> NodeSpec(Kind.PERSON, label) }
            .dedupeByLabel()

        val beliefs = BELIEF_PATTERNS
            .mapNotNull { pattern -> beliefFrom(lower, pattern) }
            .dedupeByLabel()

        return Extraction(emotions + persons + beliefs, edgesBetween(emotions, persons, beliefs))
    }

    private fun beliefFrom(lower: String, pattern: String): NodeSpec? {
        val at = lower.indexOf(pattern)
        if (at < 0) return null
        val tail = lower.substring(at + pattern.length)
            .trim()
            .trimStart { !it.isLetterOrDigit() }
            .takeWhile { it != '.' && it != '\n' }
            .trim()
            .take(BELIEF_TAIL_LIMIT)
            .trim()
        val label = if (tail.isEmpty()) pattern else "$pattern $tail"
        // Matching happens against lowercased text, but the label is quoted back
        // to the client mid-sentence, where a bare "i always..." reads wrong.
        return NodeSpec(Kind.BELIEF, label.replaceFirstChar(Char::uppercase))
    }

    private fun edgesBetween(
        emotions: List<NodeSpec>,
        persons: List<NodeSpec>,
        beliefs: List<NodeSpec>
    ): List<EdgeSpec> {
        val edges = mutableListOf<EdgeSpec>()
        for (person in persons) {
            for (emotion in emotions) {
                edges += EdgeSpec(person.label, emotion.label, Relation.TRIGGERS)
            }
        }
        for (emotion in emotions) {
            for (belief in beliefs) {
                edges += EdgeSpec(emotion.label, belief.label, Relation.CAUSES)
            }
        }
        for (belief in beliefs) {
            for (emotion in emotions) {
                edges += EdgeSpec(belief.label, emotion.label, Relation.ASSOCIATED_WITH)
            }
        }
        for (i in emotions.indices) {
            for (j in i + 1 until emotions.size) {
                edges += EdgeSpec(emotions[i].label, emotions[j].label, Relation.ASSOCIATED_WITH)
            }
        }
        return edges
    }

    /**
     * Substring matching would let "empty" fire on "emptying" and "hurt" on
     * "hurtles", so a cue must stand as a whole word.
     *
     * That costs the odd inflection ("angered" no longer matches "anger"), which
     * is the right trade here: [EMOTION_WORDS] already lists the common variants
     * separately, and recording a feeling the client never expressed is worse
     * than missing one they did.
     */
    private fun String.containsWord(word: String): Boolean {
        var from = 0
        while (true) {
            val at = indexOf(word, from)
            if (at < 0) return false
            val before = at - 1
            val after = at + word.length
            val freeLeft = before < 0 || !this[before].isLetterOrDigit()
            val freeRight = after >= length || !this[after].isLetterOrDigit()
            if (freeLeft && freeRight) return true
            from = at + 1
        }
    }

    private fun List<NodeSpec>.dedupeByLabel(): List<NodeSpec> =
        distinctBy { it.label.lowercase() }

    /**
     * Plain-language phrasing for a relation, so an edge reads as a sentence in
     * the UI ("Mother brings up Sadness") rather than exposing the raw verb.
     */
    fun relationLabel(relation: String): String = when (relation) {
        Relation.CAUSES -> "leads to"
        Relation.TRIGGERS -> "brings up"
        Relation.SUPPRESSES -> "pushes down"
        Relation.COMPENSATES_FOR -> "covers for"
        Relation.ASSOCIATED_WITH -> "goes with"
        else -> relation.replace('_', ' ').lowercase()
    }
}
