package com.theraipist.core.embedding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MemoryVectorStoreTest {

    @Test
    fun queryReturnsNearestByCosine() {
        val store = MemoryVectorStore()
        store.add("a", floatArrayOf(1f, 0f))
        store.add("b", floatArrayOf(0f, 1f))
        val top = store.query(floatArrayOf(0.9f, 0.1f), k = 1)
        assertEquals(1, top.size)
        assertEquals("a", top[0].id)
        assertTrue(top[0].score > 0.9f)
    }

    @Test
    fun respectsK() {
        val store = MemoryVectorStore()
        store.add("a", floatArrayOf(1f, 0f))
        store.add("b", floatArrayOf(0f, 1f))
        store.add("c", floatArrayOf(1f, 1f))
        assertEquals(2, store.query(floatArrayOf(1f, 0f), k = 2).size)
    }

    @Test
    fun emptyStoreReturnsEmpty() {
        assertEquals(0, MemoryVectorStore().query(floatArrayOf(1f)).size)
    }
}
