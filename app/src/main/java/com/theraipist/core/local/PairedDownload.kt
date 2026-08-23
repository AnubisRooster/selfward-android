package com.theraipist.core.local

/**
 * Combines the status and progress of two files downloaded as a unit (the
 * embedding model's weights and its vocabulary). Kept engine-free and separate
 * from the DownloadManager-backed implementation so the arithmetic can be
 * unit-tested — it is easy to get subtly wrong and invisible on a device until
 * a progress bar behaves strangely.
 */
object PairedDownload {

    /** One half of the pair, as observed by the platform downloader. */
    data class Half(
        val status: DownloadStatus,
        val progress: DownloadProgress?,
        val totalBytes: Long
    )

    /** The pair is only as far along as its least-finished half. */
    fun combineStatus(a: DownloadStatus, b: DownloadStatus): DownloadStatus = when {
        a == DownloadStatus.FAILED || b == DownloadStatus.FAILED -> DownloadStatus.FAILED
        a == DownloadStatus.DOWNLOADING || b == DownloadStatus.DOWNLOADING -> DownloadStatus.DOWNLOADING
        a == DownloadStatus.VERIFYING || b == DownloadStatus.VERIFYING -> DownloadStatus.VERIFYING
        a == DownloadStatus.NOT_DOWNLOADED || b == DownloadStatus.NOT_DOWNLOADED -> DownloadStatus.NOT_DOWNLOADED
        else -> DownloadStatus.DOWNLOADED
    }

    /**
     * Null when neither half is actively transferring. A half with no live
     * progress reading counts as complete only if its transfer actually finished;
     * treating "not started yet" as complete would show a bar near 100% before
     * the larger file has moved a single byte, then snap it back to zero.
     */
    fun combineProgress(a: Half, b: Half): DownloadProgress? {
        if (a.progress == null && b.progress == null) return null
        return DownloadProgress(
            bytesDownloaded = bytesOf(a) + bytesOf(b),
            totalBytes = a.totalBytes + b.totalBytes
        )
    }

    private fun bytesOf(half: Half): Long = when {
        half.progress != null -> half.progress.bytesDownloaded
        half.status == DownloadStatus.DOWNLOADED || half.status == DownloadStatus.VERIFYING -> half.totalBytes
        else -> 0L
    }
}
