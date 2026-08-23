package com.theraipist.core.embedding

import com.theraipist.core.local.DownloadProgress
import com.theraipist.core.local.DownloadStatus
import java.io.File

/**
 * Downloads and verifies the on-device embedding model's two assets (the ONNX
 * weights and the WordPiece vocabulary) together - status/progress reflect
 * whichever of the pair is furthest from done, and status is only DOWNLOADED
 * once both are present and verified.
 */
interface EmbeddingModelDownloader {

    fun status(model: EmbeddingModelSpec = EmbeddingModelCatalog.default): DownloadStatus

    fun progress(model: EmbeddingModelSpec = EmbeddingModelCatalog.default): DownloadProgress?

    fun onnxFile(model: EmbeddingModelSpec = EmbeddingModelCatalog.default): File

    fun vocabFile(model: EmbeddingModelSpec = EmbeddingModelCatalog.default): File

    fun startDownload(model: EmbeddingModelSpec = EmbeddingModelCatalog.default)

    fun cancelDownload(model: EmbeddingModelSpec = EmbeddingModelCatalog.default)

    fun deleteDownload(model: EmbeddingModelSpec = EmbeddingModelCatalog.default)

    suspend fun awaitCompletion(model: EmbeddingModelSpec = EmbeddingModelCatalog.default): DownloadStatus
}
