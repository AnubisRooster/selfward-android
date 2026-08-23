package com.theraipist.ui.sessions

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.theraipist.core.repository.SessionSummary
import java.text.DateFormat
import java.util.Date

@Composable
fun SessionsScreen(
    viewModel: SessionsViewModel = hiltViewModel(),
    onOpenSession: () -> Unit,
    onNewSession: () -> Unit
) {
    val sessions by viewModel.sessions.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Sessions", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            TextButton(onClick = onNewSession) { Text("New session") }
        }
        Spacer(Modifier.height(12.dp))
        if (sessions.isEmpty()) {
            Text(
                "No past sessions yet — start a conversation and it'll show up here.",
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sessions, key = { it.id }) { session ->
                    SessionRow(
                        session = session,
                        onOpen = {
                            viewModel.openSession(session.id)
                            onOpenSession()
                        },
                        onDelete = { viewModel.deleteSession(session.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionRow(session: SessionSummary, onOpen: () -> Unit, onDelete: () -> Unit) {
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(session.title, style = MaterialTheme.typography.bodyLarge)
                Text(formatTimestamp(session.updatedAt), style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onDelete) { Text("Delete") }
        }
    }
}

private fun formatTimestamp(epochMillis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(epochMillis))
