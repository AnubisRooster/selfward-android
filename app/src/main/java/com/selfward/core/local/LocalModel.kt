package com.selfward.core.local

/**
 * Describes an on-device GGUF model available from the catalog. Mirrors the
 * portable model catalog used by the iOS app so both platforms ship the same
 * models.
 */
data class LocalModel(
    val id: String,
    val name: String,
    val fileName: String,
    val sizeBytes: Long,
    val minRamBytes: Long,
    val downloadUrl: String,
    /** SHA-256 of the GGUF file, verified against Hugging Face's X-Linked-ETag after download. */
    val sha256: String,
    val contextSize: Int = 4096,
    val quant: String = "Q4_K_M"
)
