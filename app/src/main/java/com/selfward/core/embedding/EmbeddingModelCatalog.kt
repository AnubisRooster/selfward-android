package com.selfward.core.embedding

/**
 * A downloadable on-device sentence-embedding model: an ONNX BERT-family model plus
 * its WordPiece vocabulary. Unlike [com.selfward.core.local.GGUFModelCatalog] there's
 * a single well-tested default rather than a size/quality tradeoff to pick from.
 */
data class EmbeddingModelSpec(
    val id: String,
    val name: String,
    val onnxUrl: String,
    val onnxFileName: String,
    val onnxSizeBytes: Long,
    val onnxSha256: String,
    val vocabUrl: String,
    val vocabFileName: String,
    val vocabSizeBytes: Long,
    val vocabSha256: String,
    val hiddenSize: Int,
    val maxSequenceLength: Int = 256
)

object EmbeddingModelCatalog {

    /**
     * Xenova/all-MiniLM-L6-v2, INT8-quantized ONNX export of sentence-transformers/
     * all-MiniLM-L6-v2 (Apache-2.0). Sizes and SHA-256 checksums verified against
     * Hugging Face's own X-Linked-ETag / recomputed locally for non-LFS files.
     */
    val default = EmbeddingModelSpec(
        id = "minilm-l6-v2",
        name = "Sentence embeddings (MiniLM L6 v2)",
        onnxUrl = "https://huggingface.co/Xenova/all-MiniLM-L6-v2/resolve/main/onnx/model_quantized.onnx",
        onnxFileName = "minilm-l6-v2.onnx",
        onnxSizeBytes = 22_972_370L,
        onnxSha256 = "afdb6f1a0e45b715d0bb9b11772f032c399babd23bfc31fed1c170afc848bdb1",
        vocabUrl = "https://huggingface.co/Xenova/all-MiniLM-L6-v2/resolve/main/vocab.txt",
        vocabFileName = "minilm-l6-v2-vocab.txt",
        vocabSizeBytes = 231_508L,
        vocabSha256 = "07eced375cec144d27c900241f3e339478dec958f92fddbc551f295c992038a3",
        hiddenSize = 384
    )
}
