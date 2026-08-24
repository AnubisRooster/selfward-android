package com.selfward.ui.chat

import com.selfward.core.graph.GraphNode
import com.selfward.core.modality.TherapyModality
import com.selfward.core.model.Message
import com.selfward.core.safety.CrisisLevel

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isSending: Boolean = false,
    val crisisLevel: CrisisLevel? = null,
    val resourceMessage: String? = null,
    val graphNodes: List<GraphNode> = emptyList(),
    val errorMessage: String? = null,
    val needsApiKey: Boolean = false,
    val reEntryMessage: String? = null,
    /** Title of the open session, shown as the screen's heading. */
    val sessionTitle: String? = null,
    /** Manual modality override; null means auto-detect via ModalityRouter. */
    val selectedModality: TherapyModality? = null
)
