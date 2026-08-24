package com.theraipist.core.graph

import com.theraipist.core.graph.MessageAnalyzer.Kind
import com.theraipist.core.graph.MessageAnalyzer.Relation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageAnalyzerTest {

    private fun labels(text: String, kind: String) =
        MessageAnalyzer.analyze(text).nodes.filter { it.kind == kind }.map { it.label }

    @Test
    fun findsEmotionsAndCapitalisesThem() {
        assertEquals(listOf("Anxious"), labels("I feel anxious", Kind.EMOTION))
    }

    @Test
    fun findsPeopleByRelationship() {
        assertEquals(listOf("Mother"), labels("my mom called again", Kind.PERSON))
        assertEquals(listOf("Ex-partner"), labels("my ex texted", Kind.PERSON))
    }

    /** Mom and mother are the same person, so one node, not two. */
    @Test
    fun twoWordsForTheSamePersonProduceOneNode() {
        assertEquals(listOf("Mother"), labels("my mom and my mother", Kind.PERSON))
    }

    @Test
    fun aFeelingNamedTwiceIsRecordedOnce() {
        assertEquals(listOf("Sad"), labels("sad, so sad", Kind.EMOTION))
    }

    @Test
    fun capturesWhatFollowsABeliefCue() {
        val belief = labels("I always ruin things", Kind.BELIEF).single()
        assertTrue(belief, belief.contains("always"))
        assertTrue(belief, belief.contains("ruin things"))
    }

    /** The label is quoted back mid-sentence, so a bare "i always" reads wrong. */
    @Test
    fun aBeliefLabelStartsWithACapital() {
        assertEquals("I always fail", labels("I always fail.", Kind.BELIEF).single())
    }

    /** A belief label is a chip in the UI, so it cannot run on unbounded. */
    @Test
    fun aBeliefLabelIsBounded() {
        val long = "I believe " + "everything ".repeat(80)
        val belief = labels(long, Kind.BELIEF).single()
        assertTrue("label was ${belief.length} chars", belief.length <= 70)
    }

    @Test
    fun aBeliefStopsAtTheEndOfItsSentence() {
        val belief = labels("I always fail. My dad disagrees.", Kind.BELIEF).single()
        assertFalse("the next sentence leaked in: $belief", belief.contains("dad"))
    }

    /**
     * Substring matching would fire "empty" on "emptying" and "hurt" on
     * "hurtles", putting feelings in the graph that were never expressed.
     */
    @Test
    fun anEmotionWordInsideAnotherWordIsNotAFeeling() {
        assertTrue(labels("emptying the dishwasher", Kind.EMOTION).isEmpty())
        assertTrue(labels("the car hurtles downhill", Kind.EMOTION).isEmpty())
    }

    @Test
    fun aWordAtTheStartStillCounts() {
        assertEquals(listOf("Angry"), labels("angry at everything", Kind.EMOTION))
    }

    @Test
    fun anOrdinaryMessageProducesNothing() {
        assertTrue(MessageAnalyzer.analyze("The weather turned today.").isEmpty)
    }

    @Test
    fun aPersonMentionedAlongsideAFeelingTriggersIt() {
        val edges = MessageAnalyzer.analyze("my mother makes me anxious").edges
        assertTrue(
            edges.toString(),
            edges.any {
                it.sourceLabel == "Mother" &&
                    it.targetLabel == "Anxious" &&
                    it.relation == Relation.TRIGGERS
            }
        )
    }

    @Test
    fun feelingsInTheSameMessageAreLinkedToEachOther() {
        val edges = MessageAnalyzer.analyze("angry and ashamed").edges
        assertTrue(
            edges.toString(),
            edges.any {
                it.relation == Relation.ASSOCIATED_WITH &&
                    setOf(it.sourceLabel, it.targetLabel) == setOf("Angry", "Ashamed")
            }
        )
    }

    @Test
    fun aFeelingLeadingToABeliefIsRecordedAsCausing() {
        val edges = MessageAnalyzer.analyze("I feel worthless, I always mess up").edges
        assertTrue(edges.toString(), edges.any { it.relation == Relation.CAUSES })
    }

    /** Every edge must name nodes the same extraction produced. */
    @Test
    fun edgesOnlyReferenceExtractedNodes() {
        val extraction = MessageAnalyzer.analyze(
            "my mother and my boss leave me anxious and ashamed, I always fail"
        )
        val known = extraction.nodes.map { it.label }.toSet()
        extraction.edges.forEach {
            assertTrue("dangling source ${it.sourceLabel}", it.sourceLabel in known)
            assertTrue("dangling target ${it.targetLabel}", it.targetLabel in known)
        }
    }

    @Test
    fun relationsReadAsPlainEnglish() {
        assertEquals("brings up", MessageAnalyzer.relationLabel(Relation.TRIGGERS))
        assertEquals("leads to", MessageAnalyzer.relationLabel(Relation.CAUSES))
        assertEquals("goes with", MessageAnalyzer.relationLabel(Relation.ASSOCIATED_WITH))
        assertEquals("some verb", MessageAnalyzer.relationLabel("SOME_VERB"))
    }
}
