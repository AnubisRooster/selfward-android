package com.selfward.core.chat

enum class Provider { OPENROUTER, OPENAI, ANTHROPIC }

data class ApiConfig(
    val provider: Provider,
    val baseUrl: String,
    val apiKey: String,
    val model: String
)
