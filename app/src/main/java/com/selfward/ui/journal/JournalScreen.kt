package com.selfward.ui.journal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfward.core.journal.Dream
import com.selfward.core.journal.Note
import com.selfward.core.journal.NoteType
import com.selfward.ui.components.SelectionChips
import java.text.DateFormat
import java.util.Date

/**
 * Notes and dreams, the high-signal material the narrative is later woven from —
 * the Android counterpart of the capture parts of the iOS `InsightsView`.
 */
@Composable
fun JournalScreen(viewModel: JournalViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp).testTag("journalList"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        state.message?.let { message ->
            item {
                Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(message, style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = viewModel::dismissMessage) { Text("Dismiss") }
                    }
                }
            }
        }

        item {
            // The tab is called Journal and the screen used to open on a heading
            // called Notes, so the two never agreed. Journal is the screen;
            // Notes and Dreams are what is on it.
            Text("Journal", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))
            Text("Notes", style = MaterialTheme.typography.titleMedium)
            SelectionChips(
                options = NoteType.entries,
                selected = state.noteType,
                label = { it.label },
                onSelect = viewModel::setNoteType
            )
        }
        item {
            OutlinedTextField(
                value = state.noteTitle,
                onValueChange = viewModel::setNoteTitle,
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("noteTitle")
            )
        }
        item {
            OutlinedTextField(
                value = state.noteContent,
                onValueChange = viewModel::setNoteContent,
                label = { Text("What's worth remembering?") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth().testTag("noteContent")
            )
            Button(
                onClick = viewModel::saveNote,
                enabled = state.canSaveNote,
                modifier = Modifier.padding(top = 8.dp).testTag("saveNote")
            ) { Text("Save note") }
        }

        items(state.notes, key = { it.id }) { note ->
            NoteRow(note, onDelete = { viewModel.deleteNote(note.id) })
        }

        item {
            Text("Dreams", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            Text(
                "Write the dream as you remember it. Recurring images are picked out on the " +
                    "device as you type — no model needed.",
                style = MaterialTheme.typography.bodySmall
            )
        }
        item {
            OutlinedTextField(
                value = state.dreamNarrative,
                onValueChange = viewModel::setDreamNarrative,
                label = { Text("The dream") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth().testTag("dreamNarrative")
            )
        }
        item {
            OutlinedTextField(
                value = state.dreamFeelings,
                onValueChange = viewModel::setDreamFeelings,
                label = { Text("Feelings (comma separated)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (state.previewSymbols.isNotEmpty()) {
                Text(
                    "Symbols: ${state.previewSymbols.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            Button(
                onClick = viewModel::saveDream,
                enabled = state.canSaveDream,
                modifier = Modifier.padding(top = 8.dp).testTag("saveDream")
            ) { Text("Record dream") }
        }

        items(state.dreams, key = { it.id }) { dream ->
            DreamRow(dream, onDelete = { viewModel.deleteDream(dream.id) })
        }
    }
}

@Composable
private fun NoteRow(note: Note, onDelete: () -> Unit) {
    Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(note.title, style = MaterialTheme.typography.bodyLarge)
                Text(note.content, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${note.type.label} · ${formatDate(note.createdAt)}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            TextButton(onClick = onDelete) { Text("Delete") }
        }
    }
}

@Composable
private fun DreamRow(dream: Dream, onDelete: () -> Unit) {
    Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(dream.narrative, style = MaterialTheme.typography.bodyMedium)
                if (dream.feelings.isNotEmpty()) {
                    Text("Felt: ${dream.feelings.joinToString(", ")}", style = MaterialTheme.typography.labelSmall)
                }
                if (dream.symbols.isNotEmpty()) {
                    Text("Symbols: ${dream.symbols.joinToString(", ")}", style = MaterialTheme.typography.labelSmall)
                }
                Text(formatDate(dream.createdAt), style = MaterialTheme.typography.labelSmall)
            }
            TextButton(onClick = onDelete) { Text("Delete") }
        }
    }
}

private fun formatDate(epochMillis: Long): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(epochMillis))
