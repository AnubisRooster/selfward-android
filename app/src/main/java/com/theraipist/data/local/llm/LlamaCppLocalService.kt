package com.theraipist.data.local.llm

import com.theraipist.core.local.LocalLLMService
import com.theraipist.core.local.LocalModel
import com.theraipist.core.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
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
        val config = LlamaConfig().apply {
            contextSize = modelSpec.contextSize
            threads = Runtime.getRuntime().availableProcessors().coerceAtLeast(2)
            temperature = 0.7f
            topP = 0.9f
            maxTokens = 512
        }
        model = LlamaModel.load(path, config)
    }

    override suspend fun generate(messages: List<Message>): String {
        val m = checkLoaded()
        val prompt = m.applyChatTemplate(messagesToJson(messages), true)
        return m.generate(prompt)
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

    private fun checkLoaded(): LlamaModel =
        model ?: throw IllegalStateException("Local model is not loaded")

    private fun messagesToJson(messages: List<Message>): String {
        val list = messages.map { ChatMsg(it.role.name.lowercase(), it.content) }
        return Json.encodeToString(list)
    }

    @Serializable
    private data class ChatMsg(val role: String, val content: String)
}
