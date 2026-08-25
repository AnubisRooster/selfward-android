package com.selfward.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

/** One format on offer, and what to do when it is chosen. */
data class ExportOption(val label: String, val onSelect: () -> Unit)

/**
 * An "Export" button that opens the list of formats.
 *
 * A menu rather than a row of buttons because the formats are alternatives, and
 * because a screen header has no room for three of them beside a title.
 */
@Composable
fun ExportMenu(
    options: List<ExportOption>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var open by remember { mutableStateOf(false) }

    Box(modifier) {
        TextButton(
            onClick = { open = true },
            enabled = enabled,
            modifier = Modifier.testTag("exportButton")
        ) {
            Text("Export")
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    modifier = Modifier.testTag("export:${option.label}"),
                    onClick = {
                        open = false
                        option.onSelect()
                    }
                )
            }
        }
    }
}
