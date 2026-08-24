package com.selfward.core.embedding

interface EmbeddingProvider {
    suspend fun embed(text: String): FloatArray
}
