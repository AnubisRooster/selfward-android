package com.selfward.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.selfward.data.voice.AndroidSttService
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mikepenz.markdown.m3.Markdown
import com.selfward.core.modality.TherapyModality
import com.selfward.ui.components.SelectionChips
import com.selfward.core.model.Message
import com.selfward.core.model.Role

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    onOpenSessions: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
        val state by viewModel.uiState.collectAsState()
    val ttsEnabled by viewModel.ttsEnabled.collectAsState()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val context = LocalContext.current
    val stt = remember { AndroidSttService(context) }
    var micGranted by remember { mutableStateOf(false) }
    val micLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        micGranted = granted
        if (granted) stt.startListening(onFinal = { input = it })
    }

    // Keyed on the last message's length as well as the count: while a reply is
    // streaming the count never changes, so a size-only key would stop following
    // the text as it grows.
    LaunchedEffect(state.messages.size, state.messages.lastOrNull()?.content?.length) {
        if (state.messages.isNotEmpty()) {
            listState.scrollToItem(state.messages.lastIndex)
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onOpenSessions) { Text("History") }
        }
        ModalityPicker(
            selected = state.selectedModality,
            onSelect = viewModel::selectModality,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        state.reEntryMessage?.let { message ->
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(message, style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = { viewModel.clearReEntry() }) { Text("Okay") }
                }
            }
        }
        state.errorMessage?.let { message ->
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(message, style = MaterialTheme.typography.bodyMedium)
                    Row {
                        if (state.needsApiKey) {
                            TextButton(onClick = {
                                viewModel.clearError()
                                onOpenSettings()
                            }) { Text("Open Settings") }
                        }
                        TextButton(onClick = { viewModel.clearError() }) { Text("Dismiss") }
                    }
                }
            }
        }
        state.crisisLevel?.let { level ->
            Surface(
                color = if (level.name == "CRITICAL")
                    MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("Crisis support", style = MaterialTheme.typography.titleSmall)
                    Text(state.resourceMessage ?: "", style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = { viewModel.clearCrisis() }) { Text("Acknowledged") }
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
            // Anchored to the bottom, next to the composer. Top-anchored, a new
            // conversation left a screen-height gap between the first message and
            // the input, which reads as something having failed to load.
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom)
        ) {
            items(state.messages, key = { it.id }) { message -> MessageBubble(message) }
        }

            Row(
                Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Share what's on your mind…") }
                )
                Button(
                    onClick = {
                        viewModel.send(input)
                        input = ""
                    },
                    enabled = !state.isSending
                ) { Text(if (state.isSending) "…" else "Send") }
                TextButton(onClick = {
                    if (micGranted) stt.startListening(onFinal = { input = it })
                    else micLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                }) { Text("Speak") }
                TextButton(onClick = { viewModel.setTtsEnabled(!ttsEnabled) }) {
                    Text(if (ttsEnabled) "Read: On" else "Read: Off")
                }
            }
    }
}

@Composable
private fun MessageBubble(message: Message) {
    val isUser = message.role == Role.USER
    Box(Modifier.fillMaxWidth(), contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart) {
        Surface(
            color = if (isUser) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.clip(RoundedCornerShape(12.dp)).padding(4.dp)
        ) {
            Column(Modifier.padding(10.dp)) {
                message.modality?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall)
                }
                if (isUser) {
                    Text(message.content, style = MaterialTheme.typography.bodyLarge)
                } else {
                    Markdown(content = message.content)
                }
            }
        }
    }
}

@Composable
private fun ModalityPicker(
    selected: TherapyModality?,
    onSelect: (TherapyModality?) -> Unit,
    modifier: Modifier = Modifier
) {
    // A LazyRow scrolled horizontally with no affordance, so the last option
    // rendered as a clipped "R" at the screen edge and looked broken. These wrap
    // onto as many lines as they need, like every other chip group in the app.
    SelectionChips(
        options = listOf<TherapyModality?>(null) + TherapyModality.entries,
        selected = selected,
        label = { it?.let(::modalityLabel) ?: "Auto" },
        onSelect = onSelect,
        modifier = modifier
    )
}

private fun modalityLabel(modality: TherapyModality): String = when (modality) {
    TherapyModality.TALK -> "Talk"
    TherapyModality.JOURNAL -> "Journal"
    TherapyModality.ACTIVE_IMAGINATION -> "Active Imagination"
    TherapyModality.ROLEPLAY -> "Roleplay"
    TherapyModality.DREAM -> "Dream"
    TherapyModality.GROUNDING -> "Grounding"
    TherapyModality.IDENTITY -> "Identity"
    TherapyModality.PURPOSE -> "Purpose"
    TherapyModality.AUDIO -> "Audio"
}
