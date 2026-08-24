package com.selfward.data.local.llm

import com.selfward.core.local.LocalLLMService
import com.selfward.core.local.LocalModel
import com.selfward.core.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.codeshipping.llamakotlin.LlamaConfig
import org.codeshipping.llamakotlin.LlamaModel

/**
 * On-device LLM powered by llama.cpp (via the `llama-kotlin-android` Maven
 * wrapper). The model is loaded from a GGUF file on disk; chat history is
 * rendered with the model's own chat template before generation.
 *
 * Native inference only runs on a device with the bundled `libllama-android.so`;
 * this class is compile-verified by CI but not unit-tested (no device in CI).
 */
class LlamaCppLocalService : LocalLLMService {

    private var model: LlamaModel? = null

    override suspend fun isModelLoaded(): Boolean = model?.isLoaded ?: false

    override suspend fun load(modelSpec: LocalModel, path: String) {
        model?.close()
        val config = LlamaConfig().apply {
            contextSize = modelSpec.contextSize
            threads = Runtime.getRuntime().availableProcessors().coerceAtLeast(2)
            temperature = 0.7f
            topP = 0.9f
            maxTokens = 512
        }
        model = LlamaModel.load(path, config)
    }

    override fun stream(messages: List<Message>): Flow<String> {
        val m = model ?: return emptyFlow()
        val prompt = m.applyChatTemplate(messagesToJson(messages), true)
        return m.generateStream(prompt)
    }

    override fun close() {
        model?.close()
        model = null
    }

    private fun messagesToJson(messages: List<Message>): String {
        val sb = StringBuilder()
        sb.append("[")
        messages.forEachIndexed { i, m ->
            if (i > 0) sb.append(",")
            sb.append("{\"role\":")
                .append(escapeJson(m.role.name.lowercase()))
                .append(",\"content\":")
                .append(escapeJson(m.content))
                .append("}")
        }
        sb.append("]")
        return sb.toString()
    }

    private fun escapeJson(s: String): String {
        val out = StringBuilder()
        out.append("\"")
        for (c in s) {
            when (c) {
                '"' -> out.append("\\\"")
                '\\' -> out.append("\\\\")
                '\n' -> out.append("\\n")
                '\r' -> out.append("\\r")
                '\t' -> out.append("\\t")
                else -> out.append(c)
            }
        }
        out.append("\"")
        return out.toString()
    }
}
