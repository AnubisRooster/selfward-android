package com.selfward.core.embedding

import java.io.File

/** Builds an [EmbeddingProvider] from a downloaded model's files, keeping GraphHolder decoupled from the concrete ONNX implementation. */
fun interface EmbeddingProviderFactory {
    fun create(onnxFile: File, vocabFile: File): EmbeddingProvider
}
