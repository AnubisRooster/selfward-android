package com.selfward.ui.newsession

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.selfward.config.CompanionGender
import com.selfward.config.CompanionPersonality
import com.selfward.config.PersonaKind
import com.selfward.config.SpiritualTradition
import com.selfward.config.TherapyConfig
import com.selfward.core.modality.ModalityRouter
import com.selfward.core.modality.TherapyModality
import com.selfward.ui.components.SelectionChips
import com.selfward.ui.components.prettifyEnumName

/**
 * Mirrors the iOS `NewSessionView`: choose who you're talking with, customise
 * that persona inline, optionally name the session, and pick a modality when the
 * persona is the therapist.
 */
@Composable
fun NewSessionScreen(
    onCreated: () -> Unit,
    onCancel: () -> Unit,
    viewModel: NewSessionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (state.created) {
        onCreated()
        return
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("New session", style = MaterialTheme.typography.headlineSmall)

            Field("Who do you want to talk with?") {
                SelectionChips(
                    options = PersonaKind.entries,
                    selected = state.kind,
                    label = { prettifyEnumName(it.name) },
                    onSelect = viewModel::setKind
                )
                Text(state.blurb, style = MaterialTheme.typography.bodySmall)
            }

            when (state.kind) {
                PersonaKind.COMPANION -> {
                    Field("Your companion") {
                        OutlinedTextField(
                            value = state.companionName,
                            onValueChange = viewModel::setCompanionName,
                            label = { Text("Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Gender", style = MaterialTheme.typography.labelLarge)
                        SelectionChips(
                            options = CompanionGender.entries,
                            selected = state.companionGender,
                            label = { it.label },
                            onSelect = viewModel::setCompanionGender
                        )
                        Text("Personality", style = MaterialTheme.typography.labelLarge)
                        SelectionChips(
                            options = CompanionPersonality.entries,
                            selected = state.companionPersonality,
                            label = { it.label },
                            onSelect = viewModel::setCompanionPersonality
                        )
                    }
                }

                PersonaKind.SPIRITUAL -> {
                    Field("Your spiritual advisor") {
                        OutlinedTextField(
                            value = state.spiritualName,
                            onValueChange = viewModel::setSpiritualName,
                            label = { Text("Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Tradition", style = MaterialTheme.typography.labelLarge)
                        SelectionChips(
                            options = SpiritualTradition.entries,
                            selected = state.spiritualTradition,
                            label = { it.label },
                            onSelect = viewModel::setSpiritualTradition
                        )
                        Text(
                            "The advisor draws on this tradition's wisdom. They will never " +
                                "proselytise or judge your beliefs.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                PersonaKind.THERAPIST -> Unit
            }

            Field("Session") {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = viewModel::setTitle,
                    label = { Text("Title (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("sessionTitle")
                )
            }

            if (state.showsModality) {
                Field("Therapy modality") {
                    SelectionChips(
                        options = TherapyModality.entries,
                        selected = state.modality,
                        label = { it.label },
                        onSelect = viewModel::setModality
                    )
                    val description = TherapyConfig.MODALITY_DESCRIPTIONS[
                        ModalityRouter.promptKey(state.modality)
                    ]
                    if (description != null) {
                        Text(description, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Text(
                "Starts with whichever model you've set up in Settings. You can switch " +
                    "between on-device and cloud at any time.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onCancel) { Text("Cancel") }
            Spacer(Modifier.weight(1f))
            Button(onClick = viewModel::create, modifier = Modifier.testTag("createSession")) {
                Text("Start")
            }
        }
    }
}

@Composable
private fun Field(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        content()
    }
}

