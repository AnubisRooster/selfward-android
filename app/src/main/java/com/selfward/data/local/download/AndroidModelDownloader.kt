package com.selfward.data.local.download

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import com.selfward.core.local.DownloadProgress
import com.selfward.core.local.DownloadStatus
import com.selfward.core.local.LocalModel
import com.selfward.core.local.ModelDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

private const val POLL_INTERVAL_MS = 500L

/**
 * Downloads GGUF models via the OS [DownloadManager] so multi-gigabyte transfers survive
 * app-process death, backgrounding, and network changes. Downloads are re-associated
 * across process restarts by a stable title (`selfward:<modelId>`) rather than a
 * persisted download id, since DownloadManager itself is the source of truth for
 * in-flight/completed downloads. Compile-verified by CI; exercises real OS services so
 * it isn't unit-tested (no device in CI), matching [com.selfward.data.local.llm.LlamaCppLocalService].
 */
class AndroidModelDownloader(private val context: Context) : ModelDownloader {

    private val downloadManager: DownloadManager
        get() = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    override fun status(model: LocalModel): DownloadStatus {
        val file = localFile(model)
        if (file.exists() && file.length() == model.sizeBytes && verifiedMarker(model).exists()) {
            return DownloadStatus.DOWNLOADED
        }
        val downloadId = findDownloadId(model)
            ?: return if (file.exists() && file.length() == model.sizeBytes) {
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

    override fun progress(model: LocalModel): DownloadProgress? {
        val downloadId = findDownloadId(model) ?: return null
        val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId)) ?: return null
        cursor.use {
            if (!it.moveToFirst()) return null
            val status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
            val inProgress = status == DownloadManager.STATUS_RUNNING ||
                status == DownloadManager.STATUS_PENDING ||
                status == DownloadManager.STATUS_PAUSED
            if (!inProgress) return null
            val downloaded = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
            val total = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
            return DownloadProgress(downloaded, if (total > 0) total else model.sizeBytes)
        }
    }

    override fun localFile(model: LocalModel): File = File(modelsDir(), model.fileName)

    override fun startDownload(model: LocalModel) {
        cancelDownload(model)
        verifiedMarker(model).delete()
        localFile(model).delete()
        val request = DownloadManager.Request(Uri.parse(model.downloadUrl))
            .setTitle(titleFor(model))
            .setDescription("Downloading ${model.name} for on-device use")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, null, "models/${model.fileName}")
        downloadManager.enqueue(request)
    }

    override fun cancelDownload(model: LocalModel) {
        findDownloadId(model)?.let { downloadManager.remove(it) }
    }

    override fun deleteDownload(model: LocalModel) {
        cancelDownload(model)
        verifiedMarker(model).delete()
        localFile(model).delete()
    }

    override suspend fun awaitCompletion(model: LocalModel): DownloadStatus {
        while (true) {
            val downloadId = findDownloadId(model)
            if (downloadId == null) {
                return if (localFile(model).exists() && verifiedMarker(model).exists()) {
                    DownloadStatus.DOWNLOADED
                } else {
                    DownloadStatus.FAILED
                }
            }
            when (queryRawStatus(downloadId)) {
                DownloadManager.STATUS_SUCCESSFUL -> return verifyAndFinish(model)
                DownloadManager.STATUS_FAILED -> return DownloadStatus.FAILED
                else -> delay(POLL_INTERVAL_MS)
            }
        }
    }

    private suspend fun verifyAndFinish(model: LocalModel): DownloadStatus = withContext(Dispatchers.IO) {
        val file = localFile(model)
        val actualHash = runCatching { sha256Of(file) }.getOrNull()
        if (actualHash != null && actualHash.equals(model.sha256, ignoreCase = true)) {
            verifiedMarker(model).createNewFile()
            DownloadStatus.DOWNLOADED
        } else {
            file.delete()
            DownloadStatus.FAILED
        }
    }

    private fun sha256Of(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(1 shl 16)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun findDownloadId(model: LocalModel): Long? {
        val title = titleFor(model)
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

    private fun titleFor(model: LocalModel) = "selfward:${model.id}"

    private fun verifiedMarker(model: LocalModel): File = File(modelsDir(), "${model.fileName}.verified")

    private fun modelsDir(): File {
        val base = context.getExternalFilesDir(null) ?: context.filesDir
        return File(base, "models").apply { mkdirs() }
    }
}
