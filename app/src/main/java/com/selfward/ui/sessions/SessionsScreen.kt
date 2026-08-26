package com.selfward.ui.sessions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfward.core.dashboard.SessionStats
import com.selfward.core.repository.SessionSummary
import java.text.DateFormat
import java.util.Date

@Composable
fun SessionsScreen(
    viewModel: SessionsViewModel = hiltViewModel(),
    onOpenSession: () -> Unit,
    onNewSession: () -> Unit
) {
    val sessions by viewModel.sessions.collectAsState()
    val archived by viewModel.archived.collectAsState()
    val showingArchive by viewModel.showingArchive.collectAsState()
    val stats by viewModel.stats.collectAsState()

    // The ViewModel survives navigating away to start a session, so loading only
    // in init would leave a newly created session missing from this list until
    // the process restarted. Reload whenever the screen is shown again.
    LaunchedEffect(Unit) { viewModel.refresh() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (showingArchive) "Archived" else "Sessions",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            if (showingArchive) {
                TextButton(onClick = { viewModel.showArchive(false) }) { Text("Done") }
            } else {
                if (archived.isNotEmpty()) {
                    TextButton(
                        onClick = { viewModel.showArchive(true) },
                        modifier = Modifier.testTag("openArchive")
                    ) { Text("Archive (${archived.size})") }
                }
                TextButton(onClick = onNewSession) { Text("New session") }
            }
        }
        Spacer(Modifier.height(12.dp))

        if (showingArchive) {
            ArchiveList(
                archived = archived,
                stats = stats,
                onRestore = viewModel::restoreSession,
                onDelete = viewModel::deleteSession
            )
        } else {
            ActiveList(
                sessions = sessions,
                stats = stats,
                onOpen = { id ->
                    viewModel.openSession(id)
                    onOpenSession()
                },
                onArchive = viewModel::archiveSession
            )
        }
    }
}

@Composable
private fun ActiveList(
    sessions: List<SessionSummary>,
    stats: Map<String, SessionStats>,
    onOpen: (String) -> Unit,
    onArchive: (String) -> Unit
) {
    if (sessions.isEmpty()) {
        Text(
            "No sessions yet — start one and it'll show up here.",
            style = MaterialTheme.typography.bodyMedium
        )
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(sessions, key = { it.id }) { session ->
            SessionRow(session, stats[session.id], onClick = { onOpen(session.id) }) {
                TextButton(onClick = { onArchive(session.id) }) { Text("Archive") }
            }
        }
    }
}

@Composable
private fun ArchiveList(
    archived: List<SessionSummary>,
    stats: Map<String, SessionStats>,
    onRestore: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    if (archived.isEmpty()) {
        Text(
            "Nothing archived. Archiving a session hides it here without deleting anything.",
            style = MaterialTheme.typography.bodyMedium
        )
        return
    }
    Text(
        "Archived sessions keep their messages, insights and graph. Deleting is permanent.",
        style = MaterialTheme.typography.bodySmall
    )
    Spacer(Modifier.height(8.dp))
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(archived, key = { it.id }) { session ->
            SessionRow(session, stats[session.id], onClick = null) {
                TextButton(onClick = { onRestore(session.id) }) { Text("Restore") }
                TextButton(onClick = { onDelete(session.id) }) { Text("Delete") }
            }
        }
    }
}

@Composable
private fun SessionRow(
    session: SessionSummary,
    stats: SessionStats?,
    onClick: (() -> Unit)?,
    actions: @Composable () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(session.title, style = MaterialTheme.typography.bodyLarge)
                Text(formatTimestamp(session.updatedAt), style = MaterialTheme.typography.bodySmall)
                SessionBadges(stats)
            }
            actions()
        }
    }
}

/**
 * What the session holds, under its title.
 *
 * Only non-zero counts are named. A row reading "0 messages · 0 notes · 0
 * dreams" is a row telling someone what they have not done, which is the last
 * thing this app should be doing on its opening screen.
 */
@Composable
private fun SessionBadges(stats: SessionStats?) {
    if (stats == null || stats.isEmpty) return

    val parts = buildList {
        if (stats.messages > 0) add("${stats.messages} ${plural(stats.messages, "message")}")
        if (stats.patterns > 0) add("${stats.patterns} ${plural(stats.patterns, "pattern")}")
        if (stats.notes > 0) add("${stats.notes} ${plural(stats.notes, "note")}")
        if (stats.dreams > 0) add("${stats.dreams} ${plural(stats.dreams, "dream")}")
    }

    Text(
        parts.joinToString(" · "),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.testTag("sessionBadges")
    )
}

private fun plural(count: Int, singular: String) = if (count == 1) singular else "${singular}s"

private fun formatTimestamp(epochMillis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(epochMillis))
