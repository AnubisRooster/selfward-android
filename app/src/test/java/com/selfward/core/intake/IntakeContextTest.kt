package com.selfward.core.intake

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IntakeContextTest {

    @Test
    fun emptyIntakeProducesNoBlock() {
        assertNull(IntakeContext.block(Intake()))
    }

    @Test
    fun blankFieldsCountAsEmpty() {
        assertNull(IntakeContext.block(Intake(name = "   ", concerns = "\n")))
    }

    @Test
    fun identityFieldsAreCombinedIntoOneLine() {
        val block = IntakeContext.block(Intake(name = "Sam", pronouns = "they/them", age = "34"))

        assertEquals(
            "Client context (from intake):\nThe client's name is Sam. Pronouns: they/them. Age: 34.",
            block
        )
    }

    @Test
    fun onlyThePresentFieldsAppear() {
        val block = IntakeContext.block(Intake(concerns = "Sleep and work stress"))

        assertEquals("Client context (from intake):\nPresenting concerns: Sleep and work stress", block)
    }

    @Test
    fun allSectionsAreLabelled() {
        val block = IntakeContext.block(
            Intake(
                name = "Sam",
                concerns = "Anxiety",
                history = "CBT in 2019",
                goals = "Sleep better"
            )
        )!!

        assertTrue(block.contains("The client's name is Sam."))
        assertTrue(block.contains("Presenting concerns: Anxiety"))
        assertTrue(block.contains("Therapy background: CBT in 2019"))
        assertTrue(block.contains("Goals: Sleep better"))
    }
}
