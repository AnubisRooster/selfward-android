package com.selfward.data.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.OutputStream

/**
 * Writes an export to a file another app can be handed.
 *
 * Everything goes into one directory under `cacheDir`, declared to
 * [FileProvider] in the manifest as `exports`. Nothing else in the app's
 * storage is exposed: the provider's path list names that directory alone, so a
 * granted read cannot walk sideways into the database or the keys.
 *
 * The directory is emptied before each export. What it holds is the person's
 * narrative and their graph in plain text — the same content the app otherwise
 * keeps behind the PIN — so it exists for as long as it takes the share sheet
 * to hand it over, and no longer than the next export. Android also clears
 * `cacheDir` under storage pressure, which is the behaviour wanted here.
 */
class ExportFiles(private val context: Context) {

    companion object {
        const val DIRECTORY = "exports"

        /** Must match the authority in the manifest's provider entry. */
        fun authority(packageName: String) = "$packageName.exports"
    }

    private fun directory(): File =
        File(context.cacheDir, DIRECTORY).apply {
            deleteRecursively()
            mkdirs()
        }

    /** Writes [content] as UTF-8 and returns a shareable uri for it. */
    fun writeText(filename: String, content: String): Uri = uriFor(textFile(filename, content))

    /**
     * Creates [filename] and hands its stream to [write], returning a shareable
     * uri for the result.
     */
    fun writeBinary(filename: String, write: (OutputStream) -> Unit): Uri =
        uriFor(binaryFile(filename, write))

    /**
     * The two below return the [File] rather than a uri so that what was
     * written can be asserted on directly. Off-device, `FileProvider` resolves
     * its roots through `getCanonicalPath`, and the temporary directory a test
     * runs in is reached through a symlink on macOS — so asking it for a uri
     * fails there for reasons that have nothing to do with this code.
     */
    internal fun textFile(filename: String, content: String): File =
        File(directory(), filename).apply { writeText(content) }

    /**
     * The stream is closed on the way out whether or not [write] threw, so a
     * renderer that fails partway does not leave a half-written file open.
     */
    internal fun binaryFile(filename: String, write: (OutputStream) -> Unit): File =
        File(directory(), filename).apply { outputStream().use(write) }

    private fun uriFor(file: File): Uri =
        FileProvider.getUriForFile(context, authority(context.packageName), file)
}
