package com.selfward.core.embedding

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MeanPoolingTest {

    private val eps = 1e-6f

    @Test
    fun averagesAcrossAllRealTokens() {
        val embeddings = arrayOf(
            floatArrayOf(1f, 2f),
            floatArrayOf(3f, 4f)
        )

        val pooled = MeanPooling.pool(embeddings, longArrayOf(1, 1))

        assertArrayEquals(floatArrayOf(2f, 3f), pooled, eps)
    }

    /**
     * The failure this is here to catch: dividing by the sequence length instead
     * of the real-token count. With one padded position that would give (0.5, 1.0)
     * instead of (1, 2) — a shorter vector that still looks entirely reasonable.
     */
    @Test
    fun dividesByRealTokenCountNotSequenceLength() {
        val embeddings = arrayOf(
            floatArrayOf(1f, 2f),
            floatArrayOf(99f, 99f)
        )

        val pooled = MeanPooling.pool(embeddings, longArrayOf(1, 0))

        assertArrayEquals(floatArrayOf(1f, 2f), pooled, eps)
    }

    @Test
    fun ignoresPaddingVectorsEntirely() {
        val embeddings = arrayOf(
            floatArrayOf(2f, 0f),
            floatArrayOf(4f, 0f),
            floatArrayOf(1000f, 1000f)
        )

        val pooled = MeanPooling.pool(embeddings, longArrayOf(1, 1, 0))

        assertArrayEquals(floatArrayOf(3f, 0f), pooled, eps)
    }

    @Test
    fun allPaddingYieldsZeroVectorRatherThanDividingByZero() {
        val embeddings = arrayOf(
            floatArrayOf(5f, 5f),
            floatArrayOf(7f, 7f)
        )

        val pooled = MeanPooling.pool(embeddings, longArrayOf(0, 0))

        assertArrayEquals(floatArrayOf(0f, 0f), pooled, eps)
    }

    @Test
    fun emptyInputYieldsEmptyVector() {
        assertEquals(0, MeanPooling.pool(emptyArray(), longArrayOf()).size)
    }

    @Test
    fun preservesHiddenSize() {
        val embeddings = arrayOf(FloatArray(384) { 1f }, FloatArray(384) { 3f })

        val pooled = MeanPooling.pool(embeddings, longArrayOf(1, 1))

        assertEquals(384, pooled.size)
        assertEquals(2f, pooled[0], eps)
        assertEquals(2f, pooled[383], eps)
    }

    /** A shape disagreement means any pooled result would be wrong; fail loudly. */
    @Test
    fun rejectsMaskThatDoesNotLineUpWithTokens() {
        val embeddings = arrayOf(floatArrayOf(1f), floatArrayOf(2f))

        assertThrows(IllegalArgumentException::class.java) {
            MeanPooling.pool(embeddings, longArrayOf(1))
        }
    }
}
