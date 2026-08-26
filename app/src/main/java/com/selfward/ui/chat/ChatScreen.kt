package com.selfward.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.selfward.core.voice.VoicePhase
import com.selfward.data.voice.AndroidSttService
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mikepenz.markdown.m3.Markdown
import com.selfward.core.catalog.ModelChoice
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
    val voicePhase by viewModel.voicePhase.collectAsState()
    val voiceHeard by viewModel.voiceHeard.collectAsState()
    val models by viewModel.models.collectAsState()
    val modelsHeading by viewModel.modelsHeading.collectAsState()
    val probeResults by viewModel.probeResults.collectAsState()
    val probing by viewModel.probing.collectAsState()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val context = LocalContext.current
    val stt = remember { AndroidSttService(context) }
    var micGranted by remember { mutableStateOf(false) }
    // Set when the mic was requested in order to start the hands-free loop
    // rather than a single dictation, so the grant resumes the right one.
    var micWantedForVoice by remember { mutableStateOf(false) }
    val micLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        micGranted = granted
        if (!granted) {
            micWantedForVoice = false
        } else if (micWantedForVoice) {
            micWantedForVoice = false
            viewModel.startVoice()
        } else {
            stt.startListening(onFinal = { input = it })
        }
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
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // This screen had no title at all, just a floating link, so there was
            // nothing naming where you were.
            Text(
                state.sessionTitle ?: "Session",
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onOpenSessions) { Text("History") }
        }

        // The model belongs next to the conversation it is holding, not three
        // taps away in Settings: it is the thing most worth changing when a
        // reply lands badly.
        ModelBar(
            label = state.modelLabel,
            models = models,
            heading = modelsHeading,
            probeResults = probeResults,
            probing = probing,
            onSelect = viewModel::selectModel,
            onRefresh = viewModel::refreshModels,
            onCheck = viewModel::checkWhichModelsWork
        )

        state.modelNotice?.let { notice ->
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        notice,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = viewModel::dismissModelNotice) { Text("OK") }
                }
            }
        }
        ModalityPicker(
            selected = state.selectedModality,
            onSelect = viewModel::selectModality,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        state.reEntryMessage?.let { message ->
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.medium,
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
                shape = MaterialTheme.shapes.medium,
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
                shape = MaterialTheme.shapes.medium,
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
            itemsIndexed(state.messages, key = { _, m -> m.id }) { index, message ->
                // The mode is announced only where it changes. Stamping every
                // bubble with TALK said nothing on any of them; a stamp that
                // appears when the conversation moves into Dream or Grounding
                // is the only time it carries information.
                val previous = state.messages.getOrNull(index - 1)?.modality
                MessageBubble(message, showsModality = message.modality != previous)
            }
        }

            // Actions first, then the field across the full width beneath them.
            // Sharing one row left the box about half the screen and wrapped the
            // placeholder onto two lines, which is a cramped place to write down
            // something difficult.
            Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            viewModel.send(input)
                            input = ""
                        },
                        enabled = !state.isSending && input.isNotBlank()
                    ) { Text(if (state.isSending) "…" else "Send") }
                    TextButton(onClick = {
                        if (micGranted) stt.startListening(onFinal = { input = it })
                        else micLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                    }) { Text("Speak") }
                    TextButton(onClick = { viewModel.setTtsEnabled(!ttsEnabled) }) {
                        Text(if (ttsEnabled) "Read: On" else "Read: Off")
                    }
                    TextButton(
                        onClick = {
                            if (voicePhase != VoicePhase.IDLE) {
                                viewModel.stopVoice()
                            } else if (micGranted) {
                                viewModel.startVoice()
                            } else {
                                micWantedForVoice = true
                                micLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier.testTag("handsFree")
                    ) {
                        Text(if (voicePhase != VoicePhase.IDLE) "Stop" else "Hands-free")
                    }
                }

                VoiceStatus(voicePhase, voiceHeard)
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .testTag("composer"),
                    // Single line to start, growing as it is written into. A
                    // fixed two-line box took height from the conversation, and
                    // on a short screen the messages lost the argument entirely.
                    maxLines = 6,
                    placeholder = { Text("Share what's on your mind…") }
                )
            }
    }
}

@Composable
private fun MessageBubble(message: Message, showsModality: Boolean) {
    val isUser = message.role == Role.USER
    // The corner nearest the sender is flattened - bottom-end for the user's
    // own bubbles, bottom-start for the reply - the one cue that reads as
    // "chat" rather than "a stack of identical rounded rectangles".
    val bubbleShape = if (isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }
    Box(Modifier.fillMaxWidth(), contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart) {
        Surface(
            color = if (isUser) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
            shape = bubbleShape,
            modifier = Modifier.padding(4.dp)
        ) {
            Column(Modifier.padding(10.dp)) {
                if (showsModality) {
                    message.modality
                        ?.let { name -> TherapyModality.entries.firstOrNull { it.name == name } }
                        ?.let { Text(it.label, style = MaterialTheme.typography.labelSmall) }
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
        label = { it?.label ?: "Auto" },
        onSelect = onSelect,
        modifier = modifier
    )
}

/**
 * The model in use, and a way to change it without leaving the conversation.
 *
 * Collapsed to a single line until tapped, because most of the time the answer
 * to "which model is this" is all anyone wants; the list only matters in the
 * moment a reply disappoints.
 */
@Composable
private fun ModelBar(
    label: String?,
    models: List<ModelChoice>,
    heading: String,
    probeResults: Map<String, String?>,
    probing: String?,
    onSelect: (String) -> Unit,
    onRefresh: () -> Unit,
    onCheck: () -> Unit
) {
    if (label == null) return
    var expanded by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (models.isNotEmpty()) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Close" else "Change")
                }
            }
        }

        if (!expanded) return@Column

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (probing != null) "Checking $probing…" else heading,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onRefresh, enabled = probing == null) { Text("Refresh") }
            TextButton(onClick = onCheck, enabled = probing == null) { Text("Check all") }
        }

        // Every viable free model, not a shortlist: which ones a given account
        // can reach is not knowable in advance, so hiding most of them hides the
        // one that would have worked. Capped in height and scrolled so a long
        // catalogue cannot push the conversation off the screen.
        Column(
            Modifier
                .heightIn(max = MODEL_LIST_MAX_HEIGHT)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            models.forEach { model ->
                ModelRow(
                    model = model,
                    result = probeResults[model.id],
                    checked = probeResults.containsKey(model.id),
                    onSelect = {
                        onSelect(model.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ModelRow(
    model: ModelChoice,
    result: String?,
    checked: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect)
    ) {
        Column(Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    model.name,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                if (checked) {
                    Text(
                        if (result == null) "answers" else "refused",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (result == null) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    )
                }
            }
            Text(model.detail, style = MaterialTheme.typography.labelSmall)
            result?.let { reason ->
                Text(
                    reason.take(140),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/** Enough to browse without the conversation disappearing behind it. */
private val MODEL_LIST_MAX_HEIGHT = 320.dp

/**
 * What the loop is doing, and what it has heard so far.
 *
 * Shown because a hands-free conversation gives no other sign of itself: with
 * nothing on screen, a person cannot tell being listened to from the app having
 * quietly stopped. The live transcript is also the only way to notice the
 * recogniser hearing something other than what was said.
 */
@Composable
private fun VoiceStatus(phase: VoicePhase, heard: String) {
    if (phase == VoicePhase.IDLE) return

    Surface(
        tonalElevation = 3.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .testTag("voiceStatus")
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                when (phase) {
                    VoicePhase.LISTENING -> "Listening — just pause when you're done"
                    VoicePhase.THINKING -> "Thinking…"
                    VoicePhase.SPEAKING -> "Speaking…"
                    VoicePhase.IDLE -> ""
                },
                style = MaterialTheme.typography.labelLarge
            )
            if (heard.isNotBlank()) {
                Text(
                    heard,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp).testTag("voiceHeard")
                )
            }
        }
    }
}
