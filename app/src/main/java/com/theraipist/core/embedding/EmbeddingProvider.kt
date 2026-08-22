package com.theraipist.core.embedding

interface EmbeddingProvider {
    suspend fun embed(text: String): FloatArray
}
