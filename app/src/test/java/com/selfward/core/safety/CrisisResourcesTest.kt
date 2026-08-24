package com.selfward.core.safety

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CrisisResourcesTest {

    private fun message(country: String?) = CrisisResources.forCountry(country).asMessage()

    @Test
    fun theUnitedStatesGetsItsOwnLines() {
        val us = message("US")
        assertTrue(us, us.contains("988"))
        assertTrue(us, us.contains("741741"))
        assertTrue(us, us.contains("911"))
    }

    @Test
    fun theUnitedKingdomGetsSamaritansAnd999() {
        val gb = message("GB")
        assertTrue(gb, gb.contains("116 123"))
        assertTrue(gb, gb.contains("999"))
    }

    @Test
    fun australiaGetsLifelineAnd000() {
        val au = message("AU")
        assertTrue(au, au.contains("13 11 14"))
        assertTrue(au, au.contains("000"))
    }

    /**
     * The whole point of this change. A US number shown to someone in another
     * country costs them the attempt at the worst possible moment.
     */
    @Test
    fun aNonUsRegionIsNeverGivenUsNumbers() {
        listOf("GB", "AU", "NZ", "IE").forEach { country ->
            val text = message(country)
            assertFalse("$country was given 988: $text", text.contains("988"))
            assertFalse("$country was given 911: $text", text.contains("911"))
            assertFalse("$country was given 741741: $text", text.contains("741741"))
        }
    }

    /** Canada also runs 988, so it must not be swept up by the rule above. */
    @Test
    fun canadaKeepsIts988() {
        assertTrue(message("CA").contains("988"))
    }

    @Test
    fun anUnknownCountryNamesNoNumberItCannotStandBehind() {
        val text = message("JP")

        assertFalse(text, text.contains("988"))
        assertFalse(text, text.contains("911"))
        assertFalse(text, text.contains("116 123"))
        assertTrue("must still offer a route to help", text.contains("findahelpline.com"))
        assertTrue(text, text.contains("local emergency number"))
    }

    @Test
    fun aMissingCountryFallsBackRatherThanGuessing() {
        listOf(null, "", "   ").forEach { country ->
            val text = message(country)
            assertTrue("$country: $text", text.contains("findahelpline.com"))
            assertFalse("$country: $text", text.contains("988"))
        }
    }

    @Test
    fun countryCodesAreMatchedRegardlessOfCase() {
        assertEquals(message("US"), message("us"))
        assertEquals(message("GB"), message(" gb "))
    }

    @Test
    fun theFallbackHasNoEmergencyNumberOfItsOwn() {
        assertNull(CrisisResources.forCountry("JP").emergency)
    }

    @Test
    fun knownRegionsAreReportedAsKnown() {
        assertTrue(CrisisResources.isKnown("GB"))
        assertTrue(CrisisResources.isKnown("us"))
        assertFalse(CrisisResources.isKnown("JP"))
        assertFalse(CrisisResources.isKnown(null))
    }

    /** Every listed region must actually offer something dialable. */
    @Test
    fun everyKnownRegionListsAtLeastOneLineAndAnEmergencyNumber() {
        listOf("US", "CA", "GB", "IE", "AU", "NZ").forEach { country ->
            val resource = CrisisResources.forCountry(country)
            assertTrue("$country has no lines", resource.lines.isNotEmpty())
            assertTrue("$country has no emergency number", !resource.emergency.isNullOrBlank())
        }
    }

    @Test
    fun theGuardrailMessageCarriesTheRegionsLinesAndTheClosing() {
        val text = SafetyGuardrails.resourceMessage("GB")

        assertTrue(text, text.contains("116 123"))
        assertTrue(text, text.contains("free, confidential"))
    }
}
