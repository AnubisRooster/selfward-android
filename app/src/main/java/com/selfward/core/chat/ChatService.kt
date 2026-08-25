package com.selfward.core.chat

import com.selfward.core.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatService {
    /** Emits incremental text deltas as the reply is generated, not the full reply at once. */
    fun sendStreaming(messages: List<Message>): Flow<String>
}

/**
 * @param status the HTTP status the provider answered with, when there was one.
 *   Carried rather than parsed back out of the message: whether to retry a
 *   request differently depends on 429 versus 403, and matching on words in a
 *   sentence is a poor way to tell those apart.
 */
open class ChatServiceException(
    message: String,
    val status: Int? = null
) : Exception(message)

class MissingApiKeyException :
    ChatServiceException("No API key is set for this provider yet — add one in Settings.")
