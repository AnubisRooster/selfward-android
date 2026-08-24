package com.selfward.core.chat

import com.selfward.core.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatService {
    /** Emits incremental text deltas as the reply is generated, not the full reply at once. */
    fun sendStreaming(messages: List<Message>): Flow<String>
}

open class ChatServiceException(message: String) : Exception(message)

class MissingApiKeyException :
    ChatServiceException("No API key is set for this provider yet — add one in Settings.")
