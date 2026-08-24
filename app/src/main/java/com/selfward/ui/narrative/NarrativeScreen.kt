package com.selfward.ui.narrative

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.mikepenz.markdown.m3.Markdown
import java.text.DateFormat
import java.util.Date

/**
 * The single evolving account of the person's inner life, the counterpart of the
 * iOS Narrative tab.
 */
@Composable
fun NarrativeScreen(viewModel: NarrativeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Narrative", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            if (state.building) {
                CircularProgressIndicator(modifier = Modifier.padding(end = 12.dp))
            }
            Button(
                onClick = viewModel::regenerate,
                enabled = !state.building,
                modifier = Modifier.testTag("regenerateNarrative")
            ) {
                Text(if (state.document.isEmpty) "Write it" else "Update")
            }
        }

        state.error?.let { message ->
            Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(message, style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = viewModel::dismissError) { Text("Dismiss") }
                }
            }
        }

        if (state.nothingNew) {
            Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        "Nothing new to add yet — write a note, record a dream, or have a " +
                            "conversation, then come back.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = viewModel::dismissError) { Text("Okay") }
                }
            }
        }

        // Regenerating hands a batch of past material to whichever model is set
        // up. That is a heavier disclosure than a single chat turn, so it is
        // stated before the button is pressed rather than buried in the policy.
        Text(
            if (state.onDevice) {
                "Your notes, dreams and conversations are re-read by the model on your phone. " +
                    "Nothing leaves the device."
            } else {
                "Writing this sends your notes, dreams and past conversations to your cloud " +
                    "provider in one batch — more than a single message does. Switch on an " +
                    "on-device model in Settings to keep it all on your phone."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (state.document.isEmpty) {
            Text(
                "No narrative yet. Once there's something to work from, Selfward can weave " +
                    "it into a single account of what you've been exploring.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Text(
                "Last updated ${formatDate(state.document.updatedAt)} · " +
                    "${state.document.sessionCount} session${if (state.document.sessionCount == 1) "" else "s"}",
                style = MaterialTheme.typography.labelSmall
            )
            Column(Modifier.verticalScroll(rememberScrollState()).testTag("narrativeBody")) {
                Markdown(content = state.document.content)
            }
        }
    }
}

private fun formatDate(epochMillis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(epochMillis))
