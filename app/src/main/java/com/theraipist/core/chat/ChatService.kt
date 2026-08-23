package com.theraipist.core.chat

import com.theraipist.core.model.Message

interface ChatService {
    suspend fun send(messages: List<Message>): String
}

open class ChatServiceException(message: String) : Exception(message)

class MissingApiKeyException :
    ChatServiceException("No API key is set for this provider yet — add one in Settings.")
