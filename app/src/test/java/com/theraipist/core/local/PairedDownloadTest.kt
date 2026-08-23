package com.theraipist.core.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PairedDownloadTest {

    private val onnxSize = 90_000_000L
    private val vocabSize = 232_000L

    private fun half(
        status: DownloadStatus,
        bytes: Long? = null,
        total: Long = onnxSize
    ) = PairedDownload.Half(
        status = status,
        progress = bytes?.let { DownloadProgress(it, total) },
        totalBytes = total
    )

    // ---- status ----

    @Test
    fun bothDownloadedIsDownloaded() {
        assertEquals(
            DownloadStatus.DOWNLOADED,
            PairedDownload.combineStatus(DownloadStatus.DOWNLOADED, DownloadStatus.DOWNLOADED)
        )
    }

    @Test
    fun eitherFailedIsFailed() {
        assertEquals(
            DownloadStatus.FAILED,
            PairedDownload.combineStatus(DownloadStatus.DOWNLOADED, DownloadStatus.FAILED)
        )
        assertEquals(
            DownloadStatus.FAILED,
            PairedDownload.combineStatus(DownloadStatus.FAILED, DownloadStatus.DOWNLOADING)
        )
    }

    @Test
    fun failedOutranksDownloading() {
        assertEquals(
            DownloadStatus.FAILED,
            PairedDownload.combineStatus(DownloadStatus.DOWNLOADING, DownloadStatus.FAILED)
        )
    }

    @Test
    fun oneStillTransferringIsDownloading() {
        assertEquals(
            DownloadStatus.DOWNLOADING,
            PairedDownload.combineStatus(DownloadStatus.DOWNLOADED, DownloadStatus.DOWNLOADING)
        )
    }

    @Test
    fun oneAwaitingVerificationIsVerifying() {
        assertEquals(
            DownloadStatus.VERIFYING,
            PairedDownload.combineStatus(DownloadStatus.DOWNLOADED, DownloadStatus.VERIFYING)
        )
    }

    /** A verified half plus a missing half must not read as ready to use. */
    @Test
    fun oneMissingIsNotDownloaded() {
        assertEquals(
            DownloadStatus.NOT_DOWNLOADED,
            PairedDownload.combineStatus(DownloadStatus.DOWNLOADED, DownloadStatus.NOT_DOWNLOADED)
        )
    }

    // ---- progress ----

    @Test
    fun progressIsNullWhenNeitherHalfIsTransferring() {
        assertNull(
            PairedDownload.combineProgress(
                half(DownloadStatus.NOT_DOWNLOADED),
                half(DownloadStatus.NOT_DOWNLOADED, total = vocabSize)
            )
        )
    }

    @Test
    fun progressSumsBothHalves() {
        val combined = PairedDownload.combineProgress(
            half(DownloadStatus.DOWNLOADING, bytes = 45_000_000L),
            half(DownloadStatus.DOWNLOADING, bytes = 100_000L, total = vocabSize)
        )

        assertEquals(45_100_000L, combined!!.bytesDownloaded)
        assertEquals(onnxSize + vocabSize, combined.totalBytes)
    }

    /** The small vocab finishing first must count fully toward the total. */
    @Test
    fun aFinishedHalfCountsAsComplete() {
        val combined = PairedDownload.combineProgress(
            half(DownloadStatus.DOWNLOADING, bytes = 10_000_000L),
            half(DownloadStatus.DOWNLOADED, total = vocabSize)
        )

        assertEquals(10_000_000L + vocabSize, combined!!.bytesDownloaded)
    }

    /**
     * The bug this class was extracted to fix: a half that has not started has no
     * progress reading, and counting it as complete showed a bar at ~99% before
     * the 90MB file had moved a byte — then snapped back to zero once it began.
     */
    @Test
    fun aNotStartedHalfCountsAsZeroRatherThanComplete() {
        val combined = PairedDownload.combineProgress(
            half(DownloadStatus.NOT_DOWNLOADED),
            half(DownloadStatus.DOWNLOADING, bytes = 50_000L, total = vocabSize)
        )

        assertEquals(50_000L, combined!!.bytesDownloaded)
        assertEquals(onnxSize + vocabSize, combined.totalBytes)
    }

    @Test
    fun aFailedHalfCountsAsZero() {
        val combined = PairedDownload.combineProgress(
            half(DownloadStatus.FAILED),
            half(DownloadStatus.DOWNLOADING, bytes = 50_000L, total = vocabSize)
        )

        assertEquals(50_000L, combined!!.bytesDownloaded)
    }
}
