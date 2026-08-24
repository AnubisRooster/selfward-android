package com.selfward.core

import javax.inject.Inject
import javax.inject.Singleton

/**
 * One-shot mailbox for "open this specific session" requests from the Sessions screen.
 * ChatViewModel consumes it exactly once at construction time, after the caller forces a
 * fresh ChatViewModel instance by popping and re-pushing the "chat" destination - this
 * avoids racing a live StateFlow collector against ChatViewModel's own in-flight send().
 */
@Singleton
class ActiveSessionHolder @Inject constructor() {

    @Volatile
    private var pendingOpenId: String? = null

    fun open(sessionId: String) {
        pendingOpenId = sessionId
    }

    fun consumePendingOpen(): String? {
        val id = pendingOpenId
        pendingOpenId = null
        return id
    }
}
