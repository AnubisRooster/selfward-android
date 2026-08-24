package com.selfward.core.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GGUFModelCatalogTest {

    @Test
    fun catalogIsNonEmpty() {
        assertTrue(GGUFModelCatalog.allModels.isNotEmpty())
    }

    @Test
    fun idsAreUnique() {
        val ids = GGUFModelCatalog.allModels.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun byIdFindsKnownAndUnknown() {
        assertNotNull(GGUFModelCatalog.byId("qwen2.5-1.5b"))
        assertNull(GGUFModelCatalog.byId("does-not-exist"))
    }
}
