package com.selfward.core.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ModelSelectorTest {

    @Test
    fun picksLargestModelThatFits() {
        val sel = ModelSelector.select(freeRamBytes = 5_000_000_000L)
        assertEquals("phi-3.5-mini", sel?.id)
    }

    @Test
    fun dropsModelsExceedingRam() {
        val sel = ModelSelector.select(freeRamBytes = 2_500_000_000L)
        assertEquals("tinyllama-1.1b", sel?.id)
    }

    @Test
    fun returnsNullWhenNothingFits() {
        assertNull(ModelSelector.select(freeRamBytes = 1_000_000_000L))
    }
}
