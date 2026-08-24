package com.theraipist.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A single-select group of chips that wraps onto as many lines as it needs.
 *
 * The previous implementation laid every option out in one [androidx.compose.foundation.layout.Row]
 * with `weight(1f)`, which divided the screen width by the number of options: nine
 * options left roughly 40dp each, so labels wrapped one letter per line and the
 * widest group rendered no text at all. Chips here size to their own content and
 * flow onto new lines instead, and [FilterChip] shows which option is selected -
 * the old buttons computed the selected state but never displayed it.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> SelectionChips(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(label(option)) }
            )
        }
    }
}

/** Turns an enum constant name into something readable: NONBINARY -> "Nonbinary". */
fun prettifyEnumName(name: String): String =
    name.split('_').joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercase() }
    }
