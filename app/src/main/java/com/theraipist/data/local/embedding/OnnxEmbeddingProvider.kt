package com.theraipist.data.local.embedding

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.theraipist.core.embedding.EmbeddingProvider
import com.theraipist.core.embedding.WordPieceTokenizer
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
                        meanPool(hiddenStates[0], tokens.attentionMask)
                    }
                }
            }
        }
    }

    /** Mean pooling over real (non-padding) tokens, per sentence-transformers' pooling for this model. */
    private fun meanPool(tokenEmbeddings: Array<FloatArray>, attentionMask: LongArray): FloatArray {
        val hiddenSize = tokenEmbeddings.firstOrNull()?.size ?: return FloatArray(0)
        val sum = FloatArray(hiddenSize)
        var validTokens = 0f
        for (i in tokenEmbeddings.indices) {
            if (attentionMask[i] == 0L) continue
            validTokens += 1f
            val embedding = tokenEmbeddings[i]
            for (j in 0 until hiddenSize) sum[j] += embedding[j]
        }
        if (validTokens == 0f) return sum
        for (j in 0 until hiddenSize) sum[j] /= validTokens
        return sum
    }

    fun close() {
        session.close()
        env.close()
    }
}
