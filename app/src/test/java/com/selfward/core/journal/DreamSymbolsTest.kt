package com.selfward.core.journal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DreamSymbolsTest {

    @Test
    fun findsSymbolsRegardlessOfCase() {
        assertEquals(listOf("water", "house"), DreamSymbols.extract("A HOUSE beside still Water"))
    }

    @Test
    fun returnsNothingWhenNoSymbolIsPresent() {
        assertTrue(DreamSymbols.extract("I was late for an exam and could not find my pen").isEmpty())
    }

    @Test
    fun emptyNarrativeYieldsNothing() {
        assertTrue(DreamSymbols.extract("").isEmpty())
    }

    @Test
    fun eachSymbolIsReportedOnceEvenWhenRepeated() {
        assertEquals(listOf("fire"), DreamSymbols.extract("fire, more fire, and then fire again"))
    }

    @Test
    fun resultsFollowTheVocabularyOrderNotTheNarrativeOrder() {
        // Stable ordering keeps the displayed list from reshuffling as text is typed.
        assertEquals(listOf("water", "snake"), DreamSymbols.extract("a snake swimming in water"))
    }

    /**
     * The match is a plain substring test, as on iOS, so a symbol inside a longer
     * word still counts. Documented because it is a real limitation rather than
     * an accident — "brightness" contains no symbol, but "lighthouse" reports two.
     */
    @Test
    fun substringsInsideLongerWordsAlsoMatch() {
        assertEquals(listOf("house", "light"), DreamSymbols.extract("an old lighthouse"))
    }
}
