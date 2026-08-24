package com.selfward.core.safety

import org.junit.Assert.*
import org.junit.Test

class SafetyGuardrailsTest {

    @Test
    fun detectsCriticalCrisis() {
        assertEquals(CrisisLevel.CRITICAL, SafetyGuardrails.detectCrisis("I want to kill myself tonight"))
    }

    @Test
    fun detectsWarningCrisis() {
        assertEquals(CrisisLevel.WARNING, SafetyGuardrails.detectCrisis("I feel worthless and hopeless"))
    }

    @Test
    fun noCrisisForNormalText() {
        assertNull(SafetyGuardrails.detectCrisis("I had a really good day at work"))
    }

    @Test
    fun detectsBoundaryViolation() {
        assertTrue(SafetyGuardrails.detectBoundaryViolation("I diagnose you with anxiety"))
        assertTrue(SafetyGuardrails.detectBoundaryViolation("you need medication for this"))
    }

    @Test
    fun noBoundaryViolationForNormalText() {
        assertFalse(SafetyGuardrails.detectBoundaryViolation("that makes sense, thank you"))
    }

    @Test
    fun resourceMessageIsNonEmpty() {
        assertTrue(SafetyGuardrails.resourceMessage().isNotBlank())
    }

    @Test
    fun reEntryCheckReturnsMessageAfterCrisis() {
        val msg = SafetyGuardrails.reEntryCheck(true)
        assertNotNull(msg)
        assertTrue(msg!!.isNotBlank())
    }

    @Test
    fun reEntryCheckReturnsNullWithoutPriorCrisis() {
        assertNull(SafetyGuardrails.reEntryCheck(false))
    }
}
