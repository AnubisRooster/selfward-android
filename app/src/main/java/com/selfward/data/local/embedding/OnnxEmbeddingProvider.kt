package com.selfward.data.local.embedding

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.selfward.core.embedding.EmbeddingProvider
import com.selfward.core.embedding.MeanPooling
import com.selfward.core.embedding.WordPieceTokenizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.LongBuffer

private const val OUTPUT_NAME = "last_hidden_state"

/**
 * On-device sentence embeddings via ONNX Runtime Mobile (MiniLM L6 v2): WordPiece
 * tokenize, run the BERT graph, mean-pool [last_hidden_state] over real (non-padding)
 * tokens. The tokenizer is pure Kotlin and unit-tested against the real vocabulary
 * (see WordPieceTokenizerTest); the ORT session/inference calls are native and, like
 * the other on-device integrations in this codebase (LlamaCppLocalService,
 * AndroidModelDownloader), are compile-verified by CI but require a real device to
 * exercise end to end.
 */
class OnnxEmbeddingProvider(
    private val modelFile: File,
    private val vocabFile: File
) : EmbeddingProvider {

    private val env by lazy { OrtEnvironment.getEnvironment() }
    private val session by lazy { env.createSession(modelFile.absolutePath, OrtSession.SessionOptions()) }
    private val tokenizer by lazy {
        WordPieceTokenizer(WordPieceTokenizer.loadVocab(vocabFile.readText()))
    }

    override suspend fun embed(text: String): FloatArray = withContext(Dispatchers.Default) {
        val tokens = tokenizer.tokenize(text)
        val seqLen = tokens.inputIds.size.toLong()
        val shape = longArrayOf(1, seqLen)

        OnnxTensor.createTensor(env, LongBuffer.wrap(tokens.inputIds), shape).use { inputIds ->
            OnnxTensor.createTensor(env, LongBuffer.wrap(tokens.attentionMask), shape).use { attentionMask ->
                OnnxTensor.createTensor(env, LongBuffer.wrap(tokens.tokenTypeIds), shape).use { tokenTypeIds ->
                    session.run(
                        mapOf(
                            "input_ids" to inputIds,
                            "attention_mask" to attentionMask,
                            "token_type_ids" to tokenTypeIds
                        )
                    ).use { result ->
                        val hiddenStates = result.get(OUTPUT_NAME).get().value as Array<Array<FloatArray>>
                        MeanPooling.pool(hiddenStates[0], tokens.attentionMask)
                    }
                }
            }
        }
    }

    /**
     * Releases the inference session. The [OrtEnvironment] is deliberately left
     * alone: `getEnvironment()` hands back a process-wide singleton shared with
     * any other session, so it is not this object's to close.
     */
    fun close() {
        session.close()
    }
}
