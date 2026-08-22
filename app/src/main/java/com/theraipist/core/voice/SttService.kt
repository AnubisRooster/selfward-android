package com.theraipist.core.voice

interface SttService {
    suspend fun transcribe(audio: ByteArray): String
    fun close()
}
