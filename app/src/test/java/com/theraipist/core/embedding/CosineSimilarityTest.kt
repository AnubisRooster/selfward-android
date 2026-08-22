package com.theraipist.core.embedding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CosineSimilarityTest {

    @Test
    fun identicalVectorsScoreOne() {
        val v = floatArrayOf(1f, 2f, 3f)
        assertEquals(1f, CosineSimilarity.of(v, v), 1e-6f)
    }

    @Test
    fun orthogonalVectorsScoreZero() {
        assertEquals(0f, CosineSimilarity.of(floatArrayOf(1f, 0f), floatArrayOf(0f, 1f)), 1e-6f)
    }

    @Test
    fun rejectsMismatchedDimensions() {
        var threw = false
        try {
            CosineSimilarity.of(floatArrayOf(1f), floatArrayOf(1f, 2f))
        } catch (e: IllegalArgumentException) {
            threw = true
        }
        assertTrue(threw)
    }
}
