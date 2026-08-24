package com.selfward.data.local.embedding

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import com.selfward.core.embedding.EmbeddingModelDownloader
import com.selfward.core.embedding.EmbeddingModelSpec
import com.selfward.core.local.DownloadProgress
import com.selfward.core.local.DownloadStatus
import com.selfward.core.local.PairedDownload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

private const val POLL_INTERVAL_MS = 500L

/**
 * Downloads the embedding model's ONNX weights and WordPiece vocabulary as a
 * pair via the OS [DownloadManager], mirroring [com.selfward.data.local.download.AndroidModelDownloader]'s
 * approach (survive process death, re-associate by a stable title, verify
 * SHA-256 after transfer). Combined status/progress reflect whichever of the
 * two assets is furthest from done.
 */
class AndroidEmbeddingModelDownloader(private val context: Context) : EmbeddingModelDownloader {

    private val downloadManager: DownloadManager
        get() = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    override fun status(model: EmbeddingModelSpec): DownloadStatus =
        PairedDownload.combineStatus(onnxAsset(model).status(), vocabAsset(model).status())

    override fun progress(model: EmbeddingModelSpec): DownloadProgress? =
        PairedDownload.combineProgress(onnxAsset(model).half(), vocabAsset(model).half())

    override fun onnxFile(model: EmbeddingModelSpec): File = onnxAsset(model).file
    override fun vocabFile(model: EmbeddingModelSpec): File = vocabAsset(model).file

    override fun startDownload(model: EmbeddingModelSpec) {
        onnxAsset(model).start()
        vocabAsset(model).start()
    }

    override fun cancelDownload(model: EmbeddingModelSpec) {
        onnxAsset(model).cancel()
        vocabAsset(model).cancel()
    }

    override fun deleteDownload(model: EmbeddingModelSpec) {
        onnxAsset(model).delete()
        vocabAsset(model).delete()
    }

    override suspend fun awaitCompletion(model: EmbeddingModelSpec): DownloadStatus {
        val onnxResult = onnxAsset(model).awaitCompletion()
        val vocabResult = vocabAsset(model).awaitCompletion()
        return PairedDownload.combineStatus(onnxResult, vocabResult)
    }

    private fun onnxAsset(model: EmbeddingModelSpec) = Asset(
        title = "selfward:${model.id}:onnx",
        url = model.onnxUrl,
        fileName = model.onnxFileName,
        sizeBytes = model.onnxSizeBytes,
        sha256 = model.onnxSha256
    )

    private fun vocabAsset(model: EmbeddingModelSpec) = Asset(
        title = "selfward:${model.id}:vocab",
        url = model.vocabUrl,
        fileName = model.vocabFileName,
        sizeBytes = model.vocabSizeBytes,
        sha256 = model.vocabSha256
    )

    private fun embeddingsDir(): File {
        val base = context.getExternalFilesDir(null) ?: context.filesDir
        return File(base, "embeddings").apply { mkdirs() }
    }

    private inner class Asset(
        private val title: String,
        private val url: String,
        private val fileName: String,
        private val sizeBytes: Long,
        private val sha256: String
    ) {
        val file: File get() = File(embeddingsDir(), fileName)
        private val verifiedMarker: File get() = File(embeddingsDir(), "$fileName.verified")

        fun status(): DownloadStatus {
            if (file.exists() && file.length() == sizeBytes && verifiedMarker.exists()) {
                return DownloadStatus.DOWNLOADED
            }
            val downloadId = findDownloadId()
                ?: return if (file.exists() && file.length() == sizeBytes) {
                    DownloadStatus.VERIFYING
                } else {
                    DownloadStatus.NOT_DOWNLOADED
                }
            return when (queryRawStatus(downloadId)) {
                DownloadManager.STATUS_SUCCESSFUL -> DownloadStatus.VERIFYING
                DownloadManager.STATUS_FAILED -> DownloadStatus.FAILED
                null -> DownloadStatus.NOT_DOWNLOADED
                else -> DownloadStatus.DOWNLOADING
            }
        }

        fun half() = PairedDownload.Half(status(), progress(), sizeBytes)

        fun progress(): DownloadProgress? {
            val downloadId = findDownloadId() ?: return null
            val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId)) ?: return null
            cursor.use {
                if (!it.moveToFirst()) return null
                val statusCode = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                val inProgress = statusCode == DownloadManager.STATUS_RUNNING ||
                    statusCode == DownloadManager.STATUS_PENDING ||
                    statusCode == DownloadManager.STATUS_PAUSED
                if (!inProgress) return null
                val downloaded = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                return DownloadProgress(downloaded, sizeBytes)
            }
        }

        fun start() {
            cancel()
            verifiedMarker.delete()
            file.delete()
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle(title)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalFilesDir(context, null, "embeddings/$fileName")
            downloadManager.enqueue(request)
        }

        fun cancel() {
            findDownloadId()?.let { downloadManager.remove(it) }
        }

        fun delete() {
            cancel()
            verifiedMarker.delete()
            file.delete()
        }

        suspend fun awaitCompletion(): DownloadStatus {
            while (true) {
                val downloadId = findDownloadId()
                if (downloadId == null) {
                    return if (file.exists() && verifiedMarker.exists()) DownloadStatus.DOWNLOADED else DownloadStatus.FAILED
                }
                when (queryRawStatus(downloadId)) {
                    DownloadManager.STATUS_SUCCESSFUL -> return verifyAndFinish()
                    DownloadManager.STATUS_FAILED -> return DownloadStatus.FAILED
                    else -> delay(POLL_INTERVAL_MS)
                }
            }
        }

        private suspend fun verifyAndFinish(): DownloadStatus = withContext(Dispatchers.IO) {
            val actualHash = runCatching { sha256Of(file) }.getOrNull()
            if (actualHash != null && actualHash.equals(sha256, ignoreCase = true)) {
                verifiedMarker.createNewFile()
                DownloadStatus.DOWNLOADED
            } else {
                file.delete()
                DownloadStatus.FAILED
            }
        }

        private fun sha256Of(f: File): String {
            val digest = MessageDigest.getInstance("SHA-256")
            f.inputStream().use { input ->
                val buffer = ByteArray(1 shl 16)
                while (true) {
                    val read = input.read(buffer)
                    if (read <= 0) break
                    digest.update(buffer, 0, read)
                }
            }
            return digest.digest().joinToString("") { "%02x".format(it) }
        }

        private fun findDownloadId(): Long? {
            val cursor = downloadManager.query(DownloadManager.Query()) ?: return null
            cursor.use {
                val idIdx = it.getColumnIndexOrThrow(DownloadManager.COLUMN_ID)
                val titleIdx = it.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE)
                while (it.moveToNext()) {
                    if (it.getString(titleIdx) == title) return it.getLong(idIdx)
                }
            }
            return null
        }

        private fun queryRawStatus(downloadId: Long): Int? {
            val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId)) ?: return null
            cursor.use {
                if (!it.moveToFirst()) return null
                return it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
            }
        }
    }
}
