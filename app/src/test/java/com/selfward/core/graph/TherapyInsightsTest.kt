package com.selfward.core.graph

import com.selfward.core.graph.MessageAnalyzer.Kind
import com.selfward.core.graph.MessageAnalyzer.Relation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TherapyInsightsTest {

    private fun node(id: String, label: String, kind: String, strength: Float = 1f) =
        GraphNode(id, label, kind, 0L, strength)

    /**
     * Defaults to a reinforced weight, so most cases exercise a relationship the
     * client has raised more than once. Tests about a first mention pass 1f.
     */
    private fun edge(from: String, to: String, relation: String, weight: Float = 1.5f) =
        GraphEdge("$from-$to-$relation", from, to, relation, weight)

    private val mother = node("p1", "Mother", Kind.PERSON)
    private val anxious = node("e1", "Anxious", Kind.EMOTION, strength = 2f)
    private val sad = node("e2", "Sad", Kind.EMOTION, strength = 1.5f)
    private val belief = node("b1", "i always fail", Kind.BELIEF)

    @Test
    fun anEmptyGraphSaysSoRatherThanInventing() {
        val result = TherapyInsights.generate(emptyList(), emptyList(), "integrated")

        assertTrue(result.isEmpty)
        assertTrue(result.highlights.isEmpty())
        assertTrue(result.repeatingLoops.isEmpty())
    }

    @Test
    fun aPersonTriggeringAFeelingReadsAsASentence() {
        val result = TherapyInsights.generate(
            listOf(mother, anxious),
            listOf(edge("p1", "e1", Relation.TRIGGERS)),
            "integrated"
        )

        assertEquals(
            listOf("You often feel anxious when Mother comes up."),
            result.highlights
        )
    }

    @Test
    fun aFeelingLeadingToABeliefQuotesTheBelief() {
        val result = TherapyInsights.generate(
            listOf(anxious, belief),
            listOf(edge("e1", "b1", Relation.CAUSES)),
            "integrated"
        )

        assertTrue(
            result.highlights.single(),
            result.highlights.single().contains("Feeling anxious seems to lead to the thought")
        )
    }

    @Test
    fun coOccurringFeelingsReadAsShowingUpTogether() {
        val result = TherapyInsights.generate(
            listOf(anxious, sad),
            listOf(edge("e1", "e2", Relation.ASSOCIATED_WITH)),
            "integrated"
        )

        assertEquals(listOf("Anxious and sad tend to show up together."), result.highlights)
    }

    /** With no relationships yet, name the feelings rather than showing nothing. */
    @Test
    fun feelingsAloneStillProduceAHighlight() {
        val result = TherapyInsights.generate(listOf(anxious, sad), emptyList(), "integrated")

        assertEquals(listOf("Feelings that have come up: Anxious, Sad."), result.highlights)
    }

    @Test
    fun theStrongestRelationshipsComeFirst() {
        val result = TherapyInsights.generate(
            listOf(mother, anxious, sad),
            listOf(
                edge("p1", "e2", Relation.TRIGGERS, weight = 1f),
                edge("p1", "e1", Relation.TRIGGERS, weight = 2f)
            ),
            "integrated"
        )

        assertTrue(result.highlights.first(), result.highlights.first().contains("anxious"))
    }

    /** Four sentences is the cap; a busy graph must not flood the screen. */
    @Test
    fun highlightsAreCapped() {
        val emotions = (1..10).map { node("e$it", "Feeling$it", Kind.EMOTION) }
        val edges = (2..10).map { edge("e1", "e$it", Relation.ASSOCIATED_WITH) }

        val result = TherapyInsights.generate(emotions, edges, "integrated")

        assertEquals(4, result.highlights.size)
    }

    @Test
    fun anEdgeToAMissingNodeIsSkippedRatherThanCrashing() {
        val result = TherapyInsights.generate(
            listOf(mother),
            listOf(edge("p1", "gone", Relation.TRIGGERS)),
            "integrated"
        )

        assertFalse(result.highlights.any { it.contains("gone") })
    }

    @Test
    fun aLoopIsReportedAsARepeatingPattern() {
        val result = TherapyInsights.generate(
            listOf(anxious, sad),
            listOf(
                edge("e1", "e2", Relation.ASSOCIATED_WITH),
                edge("e2", "e1", Relation.ASSOCIATED_WITH)
            ),
            "integrated"
        )

        assertEquals(1, result.repeatingLoops.size)
        assertTrue(result.repeatingLoops.single(), result.repeatingLoops.single().contains("→"))
        assertTrue(result.shadowObservation.contains("1 repeating pattern"))
    }

    /**
     * The extraction rules link emotion→belief and belief→emotion both ways, so
     * one sentence naming a feeling and a belief closes a loop by itself.
     * Reporting that would tell someone who has written a single line that they
     * have an entrenched pattern.
     */
    @Test
    fun aSingleMessageDoesNotProduceARepeatingPattern() {
        val result = TherapyInsights.generate(
            listOf(anxious, belief),
            listOf(
                edge("e1", "b1", Relation.CAUSES, weight = 1f),
                edge("b1", "e1", Relation.ASSOCIATED_WITH, weight = 1f)
            ),
            "integrated"
        )

        assertTrue(result.repeatingLoops.toString(), result.repeatingLoops.isEmpty())
        assertTrue(result.shadowObservation.contains("No repeating loop"))
    }

    @Test
    fun aPatternCountsOnceTheSameRelationshipsRecur() {
        val result = TherapyInsights.generate(
            listOf(anxious, belief),
            listOf(
                edge("e1", "b1", Relation.CAUSES, weight = 1.5f),
                edge("b1", "e1", Relation.ASSOCIATED_WITH, weight = 1.5f)
            ),
            "integrated"
        )

        assertEquals(1, result.repeatingLoops.size)
    }

    /** "Often" is a claim about frequency and needs more than one mention. */
    @Test
    fun aSingleMentionIsNotDescribedAsAHabit() {
        val once = TherapyInsights.generate(
            listOf(mother, anxious),
            listOf(edge("p1", "e1", Relation.TRIGGERS, weight = 1f)),
            "integrated"
        ).highlights.single()

        assertFalse(once, once.contains("often"))
        assertTrue(once, once.contains("Mother") && once.contains("anxious"))
    }

    @Test
    fun aRepeatedRelationshipIsDescribedAsAHabit() {
        val repeated = TherapyInsights.generate(
            listOf(mother, anxious),
            listOf(edge("p1", "e1", Relation.TRIGGERS, weight = 2f)),
            "integrated"
        ).highlights.single()

        assertTrue(repeated, repeated.contains("often"))
    }

    @Test
    fun theSkillSuggestionMatchesTheStrongestFeeling() {
        val angry = node("e9", "Angry", Kind.EMOTION, strength = 2f)

        val result = TherapyInsights.generate(listOf(angry), emptyList(), "integrated")

        assertTrue(result.skillSuggestion, result.skillSuggestion.contains("Opposite action"))
    }

    @Test
    fun theFrameworkAnalysisFollowsTheSessionsFramework() {
        val nodes = listOf(anxious, belief)

        val cbt = TherapyInsights.generate(nodes, emptyList(), "cbt").frameworkAnalysis
        val ifs = TherapyInsights.generate(nodes, emptyList(), "ifs").frameworkAnalysis

        assertTrue(cbt, cbt.contains("core beliefs"))
        assertTrue(ifs, ifs.contains("protector"))
        assertFalse("frameworks must not share text", cbt == ifs)
    }

    @Test
    fun anUnknownFrameworkAddsNothingRatherThanGuessing() {
        val result = TherapyInsights.generate(listOf(anxious), emptyList(), "no_such_framework")

        assertEquals("", result.frameworkAnalysis)
    }

    /**
     * The person reading this is the one who wrote the messages, so it must
     * address them directly rather than describing them to a third party.
     */
    @Test
    fun insightsAddressTheReaderNotAClinicianAboutThem() {
        val result = TherapyInsights.generate(
            listOf(mother, anxious, belief),
            listOf(edge("p1", "e1", Relation.TRIGGERS)),
            "cbt"
        )

        val prose = listOf(
            result.highlights.joinToString(" "),
            result.lifestyleObservation,
            result.skillSuggestion,
            result.shadowObservation,
            result.frameworkAnalysis
        ).joinToString(" ").lowercase()

        assertFalse(prose, prose.contains("the client"))
    }

    @Test
    fun singleAndMultiplePatternsAreBothWordedCorrectly() {
        assertTrue(
            TherapyInsights.generate(
                listOf(anxious, sad),
                listOf(
                    edge("e1", "e2", Relation.ASSOCIATED_WITH),
                    edge("e2", "e1", Relation.ASSOCIATED_WITH)
                ),
                "integrated"
            ).shadowObservation.contains("1 repeating pattern found")
        )

        val threeWay = listOf(anxious, sad, node("e3", "Numb", Kind.EMOTION))
        val twoLoops = listOf(
            edge("e1", "e2", Relation.ASSOCIATED_WITH),
            edge("e2", "e1", Relation.ASSOCIATED_WITH),
            edge("e2", "e3", Relation.ASSOCIATED_WITH),
            edge("e3", "e2", Relation.ASSOCIATED_WITH)
        )
        assertTrue(
            TherapyInsights.generate(threeWay, twoLoops, "integrated")
                .shadowObservation.contains("patterns")
        )
    }
}
