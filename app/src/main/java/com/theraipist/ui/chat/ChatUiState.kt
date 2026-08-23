package com.theraipist.ui.chat

import com.theraipist.core.graph.GraphNode
import com.theraipist.core.model.Message
import com.theraipist.core.safety.CrisisLevel

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isSending: Boolean = false,
    val crisisLevel: CrisisLevel? = null,
    val resourceMessage: String? = null,
    val graphNodes: List<GraphNode> = emptyList(),
    val errorMessage: String? = null,
    val needsApiKey: Boolean = false,
    val reEntryMessage: String? = null
)
