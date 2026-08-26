package com.selfward.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfward.core.dashboard.GlobalStats

/**
 * The headline numbers, shown above the patterns on the Insights tab.
 *
 * iOS gives this its own tab. There is no room for a sixth one here — a
 * Material navigation bar takes five before it starts crowding — and this is
 * the tab the numbers belong to anyway: Insights is already the answer to
 * "what has this added up to".
 */
@Composable
fun DashboardSection(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    if (state.loading || state.stats.isEmpty) return

    Surface(
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth().testTag("dashboard")
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Where you've got to", style = MaterialTheme.typography.titleMedium)
            Counts(state.stats)
            Modalities(state.stats)
            Themes(state.stats)
        }
    }
}

@Composable
private fun Counts(stats: GlobalStats) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Count(stats.sessionsWithMessages, plural(stats.sessionsWithMessages, "conversation"))
        Count(stats.messages, plural(stats.messages, "message"))
        Count(stats.patterns, plural(stats.patterns, "pattern"))
    }
    val extras = buildList {
        if (stats.notes > 0) add("${stats.notes} ${plural(stats.notes, "note")}")
        if (stats.dreams > 0) add("${stats.dreams} ${plural(stats.dreams, "dream")}")
        if (stats.people > 0) add("${stats.people} ${plural(stats.people, "person", "people")}")
    }
    if (extras.isNotEmpty()) {
        Text(
            extras.joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun Count(value: Int, label: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            value.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun Modalities(stats: GlobalStats) {
    if (stats.modalities.isEmpty()) return
    Text(
        "Mostly " + stats.modalities.take(3).joinToString(", ") { (label, count) ->
            "$label ($count)"
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Themes(stats: GlobalStats) {
    val shown = stats.themes.ifEmpty { stats.topFeelings }
    if (shown.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            if (stats.themes.isNotEmpty()) "Themes running through it" else "What comes up most",
            style = MaterialTheme.typography.labelLarge
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            shown.forEach { theme ->
                Surface(
                    tonalElevation = 4.dp,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        theme,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

private fun plural(count: Int, singular: String, plural: String = "${singular}s") =
    if (count == 1) singular else plural
