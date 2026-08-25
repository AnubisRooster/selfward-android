package com.selfward.data.export

import android.content.Context
import android.content.Intent
import android.net.Uri

/** A file that has been written and is ready to hand to another app. */
data class ExportedFile(
    val uri: Uri,
    val mimeType: String,
    /** What the share sheet calls it. */
    val title: String
)

/**
 * Offers [file] to the system share sheet.
 *
 * The chooser is the whole point: the app never picks a destination, so nothing
 * leaves the phone until the person taps an app in a list they can also
 * dismiss. The read permission travels with the intent and applies to that one
 * uri.
 */
fun Context.share(file: ExportedFile) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = file.mimeType
        putExtra(Intent.EXTRA_STREAM, file.uri)
        putExtra(Intent.EXTRA_TITLE, file.title)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startActivity(Intent.createChooser(send, file.title))
}
