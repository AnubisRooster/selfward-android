package com.selfward.core.local

import com.selfward.core.model.Message
import kotlinx.coroutines.flow.Flow

interface LocalLLMService {
    suspend fun isModelLoaded(): Boolean
    suspend fun load(model: LocalModel, path: String)
    fun stream(messages: List<Message>): Flow<String>
    fun close()
}
