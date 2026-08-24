package com.selfward.core.voice

interface TtsService {
    suspend fun synthesize(request: TtsRequest): ByteArray
    fun close()
}

class TtsServiceException(message: String) : Exception(message)
