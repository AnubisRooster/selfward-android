package com.theraipist.core.local

import com.theraipist.core.model.Message
import kotlinx.coroutines.flow.Flow

interface LocalLLMService {
    suspend fun isModelLoaded(): Boolean
    suspend fun load(model: LocalModel, path: String)
    suspend fun generate(messages: List<Message>): String
    fun stream(messages: List<Message>): Flow<String>
    fun close()
}
