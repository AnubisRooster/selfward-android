package com.theraipist.data.local.embedding

import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.theraipist.core.embedding.EmbeddingProvider

/**
 * On-device text embeddings via ONNX Runtime Mobile (MiniLM). The native
 * `onnxruntime-android` AAR is compile-verified by CI; the WordPiece tokenizer
 * and full inference graph are wired in Phase 7 (UI/integration). Until then
 * this implements the [EmbeddingProvider] contract and constructs the ORT
 * session so the dependency and API surface are exercised at build time.
 */
class OnnxEmbeddingProvider(
    private val modelPath: String
) : EmbeddingProvider {

    private val env by lazy { OrtEnvironment.getEnvironment() }
    private val session by lazy { env.createSession(modelPath, OrtSession.SessionOptions()) }

    override suspend fun embed(text: String): FloatArray {
        throw NotImplementedError(
            "MiniLM tokenizer + ORT inference is wired in Phase 7; " +
                "EmbeddingProvider contract is in place. Model path: $modelPath"
        )
    }

    fun close() {
        session.close()
        env.close()
    }
}
