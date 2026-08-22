package com.theraipist.core.chat

import com.theraipist.core.model.Message

interface ChatService {
    suspend fun send(messages: List<Message>): String
}
