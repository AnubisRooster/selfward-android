package com.selfward.data.export

import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ExportFilesTest {

    private val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    private val exportFiles = ExportFiles(context)

    private fun exportDir() = File(context.cacheDir, ExportFiles.DIRECTORY)

    @Test
    fun theTextIsWrittenAndReadsBackUnchanged() {
        val written = exportFiles.textFile("note.md", "# Hello\n\nBody.")

        assertTrue(written.exists())
        assertEquals(exportDir(), written.parentFile)
        assertEquals("# Hello\n\nBody.", written.readText())
    }

    @Test
    fun nonAsciiTextSurvivesTheRoundTrip() {
        val written = exportFiles.textFile("note.md", "café — 深呼吸 — naïve")

        assertEquals("café — 深呼吸 — naïve", written.readText())
    }

    @Test
    fun binaryContentIsWrittenThroughTheStream() {
        val written = exportFiles.binaryFile("thing.bin") { it.write(byteArrayOf(1, 2, 3)) }

        org.junit.Assert.assertArrayEquals(byteArrayOf(1, 2, 3), written.readBytes())
    }

    /**
     * The directory holds the person's narrative and graph in the clear. A
     * previous export must not sit in the cache indefinitely just because the
     * next one had a different name.
     */
    @Test
    fun anEarlierExportIsClearedBeforeTheNextOneIsWritten() {
        val first = exportFiles.textFile("first.md", "old")
        exportFiles.textFile("second.md", "new")

        assertFalse(first.exists())
        assertTrue(File(exportDir(), "second.md").exists())
    }

    /** Nothing may be written outside the one directory the provider exposes. */
    @Test
    fun everythingIsWrittenInsideTheSharedDirectory() {
        val text = exportFiles.textFile("a.md", "x")
        val binary = exportFiles.binaryFile("b.bin") { it.write(1) }

        assertEquals(exportDir(), text.parentFile)
        assertEquals(exportDir(), binary.parentFile)
    }

    /**
     * The authority the manifest declares and the one the code asks
     * `FileProvider` for have to be the same string, and nothing else in the
     * build checks that they are: a mismatch throws only when someone taps
     * Export on a real phone.
     */
    @Test
    fun theManifestDeclaresTheAuthorityTheCodeAsksFor() {
        val providers = context.packageManager
            .getPackageInfo(context.packageName, PackageManager.GET_PROVIDERS)
            .providers
            .orEmpty()
            .map { it.authority }

        assertTrue(
            "declared: $providers",
            providers.contains(ExportFiles.authority(context.packageName))
        )
    }
}
