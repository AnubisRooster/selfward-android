package com.theraipist.core.graph

/**
 * Reads the client's graph and says what it shows.
 *
 * All of this is derived locally from nodes and edges the client's own messages
 * produced — no model call, so the Insights tab works with no key and no
 * connection, and nothing here is sent anywhere.
 */
object TherapyInsights {

    data class Result(
        /** Client-facing sentences, the first thing shown. */
        val highlights: List<String>,
        val lifestyleObservation: String,
        val skillSuggestion: String,
        val shadowObservation: String,
        val repeatingLoops: List<String>,
        /** Commentary specific to the framework the session is running under. */
        val frameworkAnalysis: String
    ) {
        val isEmpty: Boolean get() = highlights.isEmpty() && repeatingLoops.isEmpty()
    }

    private const val TOP_N = 3
    private const val MAX_HIGHLIGHTS = 4

    fun generate(
        nodes: List<GraphNode>,
        edges: List<GraphEdge>,
        frameworkKey: String
    ): Result {
        val cycles = CycleDetector.detect(nodes, recurringEdges(edges))
        val emotions = strongestLabels(nodes, MessageAnalyzer.Kind.EMOTION, limit = Int.MAX_VALUE)
        val beliefs = strongestLabels(nodes, MessageAnalyzer.Kind.BELIEF, limit = Int.MAX_VALUE)
        val themes = strongestLabels(nodes, MessageAnalyzer.Kind.THEME, limit = Int.MAX_VALUE)

        return Result(
            highlights = highlights(nodes, edges, emotions),
            lifestyleObservation = lifestyle(beliefs),
            skillSuggestion = skill(emotions),
            shadowObservation = shadow(cycles),
            repeatingLoops = cycles.cycles
                .map { CycleDetector.format(it, nodes) }
                .filter { it.isNotEmpty() },
            frameworkAnalysis = framework(frameworkKey, emotions, beliefs, themes, cycles)
        )
    }

    /**
     * Only relationships seen in more than one message.
     *
     * A single sentence naming a feeling and a belief produces a loop on its
     * own, because the extraction rules link emotion→belief and belief→emotion
     * both ways. Counting those would announce entrenched patterns to someone
     * who has written one line — so a pattern only counts as repeating once it
     * has actually repeated, which is exactly what a reinforced weight records.
     */
    private fun recurringEdges(edges: List<GraphEdge>): List<GraphEdge> =
        edges.filter { (it.weight ?: 0f) > BASE_EDGE_WEIGHT }

    private const val BASE_EDGE_WEIGHT = 1.0f

    /**
     * "3" when the search was exhaustive, "12 or more" when it stopped early.
     * The count is a statement about the client's own patterns, so it must not
     * present a floor as a total.
     */
    private fun countPhrase(cycles: CycleDetector.Cycles): String =
        if (cycles.truncated) "${cycles.size} or more" else "${cycles.size}"

    /**
     * Reads the heaviest edges back as plain sentences ("You often feel anxious
     * when Mother comes up"), falling back to the feelings themselves before any
     * relationship has formed.
     */
    fun highlights(
        nodes: List<GraphNode>,
        edges: List<GraphEdge>,
        emotions: List<String> = strongestLabels(nodes, MessageAnalyzer.Kind.EMOTION, Int.MAX_VALUE)
    ): List<String> {
        val byId = nodes.associateBy { it.id }
        val sentences = edges
            .sortedByDescending { it.weight ?: 0f }
            .mapNotNull { edge ->
                val source = byId[edge.sourceId] ?: return@mapNotNull null
                val target = byId[edge.targetId] ?: return@mapNotNull null
                val recurring = (edge.weight ?: 0f) > BASE_EDGE_WEIGHT
                sentenceFor(source, edge.label.orEmpty(), target, recurring)
            }
            .distinct()
            .take(MAX_HIGHLIGHTS)

        if (sentences.isNotEmpty()) return sentences
        val top = emotions.take(TOP_N)
        return if (top.isEmpty()) emptyList()
        else listOf("Feelings that have come up: ${top.joinToString(", ")}.")
    }

    /**
     * @param recurring whether this relationship has turned up more than once.
     *   Only then may the sentence claim a habit ("often", "tend to"); on a
     *   single mention it reports what was said and nothing more.
     */
    private fun sentenceFor(
        source: GraphNode,
        relation: String,
        target: GraphNode,
        recurring: Boolean
    ): String {
        val s = source.label
        val t = target.label
        return when {
            source.kind == MessageAnalyzer.Kind.PERSON &&
                relation == MessageAnalyzer.Relation.TRIGGERS &&
                target.kind == MessageAnalyzer.Kind.EMOTION ->
                if (recurring) "You often feel ${t.lowercase()} when $s comes up."
                else "$s came up alongside feeling ${t.lowercase()}."

            source.kind == MessageAnalyzer.Kind.EMOTION &&
                relation == MessageAnalyzer.Relation.CAUSES &&
                target.kind == MessageAnalyzer.Kind.BELIEF ->
                if (recurring) "Feeling ${s.lowercase()} seems to lead to the thought “$t”."
                else "Feeling ${s.lowercase()} came up with the thought “$t”."

            source.kind == MessageAnalyzer.Kind.EMOTION &&
                relation == MessageAnalyzer.Relation.ASSOCIATED_WITH &&
                target.kind == MessageAnalyzer.Kind.EMOTION ->
                if (recurring) "$s and ${t.lowercase()} tend to show up together."
                else "$s and ${t.lowercase()} came up together."

            source.kind == MessageAnalyzer.Kind.BELIEF &&
                relation == MessageAnalyzer.Relation.ASSOCIATED_WITH &&
                target.kind == MessageAnalyzer.Kind.EMOTION ->
                if (recurring) "The belief “$s” goes with feeling ${t.lowercase()}."
                else "The thought “$s” came up with feeling ${t.lowercase()}."

            else -> "$s ${MessageAnalyzer.relationLabel(relation)} $t."
        }
    }

    fun strongestLabels(nodes: List<GraphNode>, kind: String, limit: Int): List<String> =
        nodes.filter { it.kind == kind }
            .sortedByDescending { it.strength }
            .take(limit)
            .map { it.label }

    private fun lifestyle(beliefs: List<String>): String =
        if (beliefs.isEmpty()) {
            "Nothing has settled into a firm conviction yet. Keep going and the beliefs you hold about yourself will start to show."
        } else {
            "Beliefs that keep surfacing: ${beliefs.take(TOP_N).joinToString(", ")}. " +
                "These are the rules you seem to be living by. It can be worth asking where each one was learned, and whether it still serves you."
        }

    private fun skill(emotions: List<String>): String {
        if (emotions.isEmpty()) {
            return "No clear emotional pattern yet — there is nothing to practise against until more has come up."
        }
        val suggestion = when (emotions.first().lowercase()) {
            "angry", "anger", "frustrated", "frustration" ->
                "Opposite action and interpersonal effectiveness are the skills that tend to help here."
            "anxious", "anxiety", "fearful", "fear", "overwhelmed", "panic" ->
                "Mindfulness and distress tolerance are the skills that tend to help here."
            "sad", "sadness", "lonely", "loneliness", "hopeless" ->
                "Emotion regulation and opposite action, to slowly rebuild a sense of mastery."
            else ->
                "Mindfulness first — noticing the feeling without acting on it is the skill underneath the others."
        }
        return "Most present: ${emotions.take(TOP_N).joinToString(", ")}. $suggestion"
    }

    private fun shadow(cycles: CycleDetector.Cycles): String =
        if (cycles.isEmpty) {
            "No repeating loop has shown up yet."
        } else {
            "${countPhrase(cycles)} repeating ${plural(cycles.size, "pattern")} found. Patterns that circle back on themselves often hold something being kept out of view — worth asking what each one is protecting."
        }

    private fun framework(
        key: String,
        emotions: List<String>,
        beliefs: List<String>,
        themes: List<String>,
        cycles: CycleDetector.Cycles
    ): String = when (key) {
        "cbt" ->
            if (beliefs.isEmpty()) "Keep tracking the thoughts that arrive automatically."
            else "Identified beliefs: ${beliefs.take(TOP_N).joinToString(", ")}. These may be core beliefs driving automatic thoughts. Consider the evidence for and against each, and what a more balanced version would sound like."

        "humanistic" ->
            if (beliefs.none { it.contains("should") || it.contains("must") })
                "Nothing yet reads as a borrowed rule. Notice which of your standards are actually your own."
            else "Some of these read as should/must statements — conditions of worth taken on from elsewhere. Notice which standards are yours and which were handed to you."

        "existential" ->
            if (themes.isEmpty() && beliefs.isEmpty()) "Existential themes will show as they arise; nothing has surfaced yet."
            else "Emerging ground: ${(themes + beliefs).take(TOP_N).joinToString(", ")}. Worth asking how you meet these givens, and what that says about how you are choosing to live."

        "gestalt" ->
            "Notice what you are avoiding right now, in this moment rather than in the story. Where does contact break off?"

        "somatic" ->
            "Track where this sits in the body. Is the system revved up or shut down? Resourcing and grounding come before insight."

        "narrative" ->
            "Listen for the dominant problem story, and for the moments the problem could have taken over and didn't. Those exceptions are where a different story starts."

        "act" ->
            if (emotions.isEmpty()) "Worth clarifying what you actually value, and where you are steering around discomfort."
            else "Dominant feeling: ${emotions.first().lowercase()}. Notice how you relate to it — fused with it, avoiding it, or willing to have it there while you do what matters."

        "psychodynamic" ->
            if (cycles.isEmpty) "Listen for the themes underneath what is being said directly."
            else "${countPhrase(cycles)} recurring relational ${plural(cycles.size, "pattern")}. Old attachment patterns tend to re-stage themselves. Notice where this one shows up in the present."

        "ifs" ->
            if (beliefs.isEmpty()) "Start mapping the system — which parts manage the day, and which react when something tender is touched?"
            else "${beliefs.size} identified protector ${plural(beliefs.size, "part")}. Work with protectors first: understand the job each is doing before asking for access to what it guards."

        "adlerian" ->
            if (beliefs.isEmpty()) "Keep exploring to surface the private logic underneath."
            else "Convictions in view: ${beliefs.take(TOP_N).joinToString(", ")}. These shape how you move toward belonging and contribution. Early memories often reinforce them."

        "jungian" ->
            if (cycles.isEmpty) "No shadow pattern yet. Dreams and repeated images are where it usually first appears."
            else "${countPhrase(cycles)} repeating ${plural(cycles.size, "pattern")} may carry shadow content asking to be integrated rather than removed."

        "dbt" ->
            if (emotions.isEmpty()) "Nothing to regulate against yet."
            else "Practise against what is actually present: ${emotions.take(TOP_N).joinToString(", ")}."

        "active_imagination" ->
            "Let the images speak for themselves before interpreting them. What arrived uninvited is usually the important part."

        else -> ""
    }

    private fun plural(count: Int, word: String) = if (count == 1) word else "${word}s"
}
