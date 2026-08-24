package com.selfward.core.model

data class Message(
    val id: String,
    val role: Role,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val modality: String? = null
)
