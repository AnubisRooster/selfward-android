package com.theraipist.core.local

import java.io.File

enum class DownloadStatus { NOT_DOWNLOADED, DOWNLOADING, VERIFYING, DOWNLOADED, FAILED }

data class DownloadProgress(val bytesDownloaded: Long, val totalBytes: Long)

/**
 * Downloads and verifies on-device GGUF models from [GGUFModelCatalog]. A download
 * survives app-process death (it's queued with the OS download service); [status]
 * re-associates with any in-flight or completed download by [LocalModel.fileName].
 */
interface ModelDownloader {

    fun status(model: LocalModel): DownloadStatus

    /** Non-null only while [status] is [DownloadStatus.DOWNLOADING]. */
    fun progress(model: LocalModel): DownloadProgress?

    /** Where the model file lives once downloaded, regardless of current status. */
    fun localFile(model: LocalModel): File

    fun startDownload(model: LocalModel)

    fun cancelDownload(model: LocalModel)

    fun deleteDownload(model: LocalModel)

    /**
     * Suspends until the download reaches a terminal state, verifying the file's
     * SHA-256 against [LocalModel.sha256] once the transfer completes. Deletes the
     * file and returns [DownloadStatus.FAILED] on a checksum mismatch.
     */
    suspend fun awaitCompletion(model: LocalModel): DownloadStatus
}
