package com.selfward.ui.settings

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
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import com.selfward.core.voice.VoiceTranscript
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfward.config.TherapyConfig
import com.selfward.core.safety.SafetyGuardrails
import com.selfward.core.catalog.OpenRouterModel
import com.selfward.core.chat.Provider
import com.selfward.core.local.DownloadProgress
import com.selfward.core.local.DownloadStatus
import com.selfward.core.local.GGUFModelCatalog
import com.selfward.ui.components.SelectionChips

/** Provider names as they are written by the vendors, rather than SCREAMING_CASE. */
private fun providerLabel(provider: Provider): String = when (provider) {
    Provider.OPENROUTER -> "OpenRouter"
    Provider.OPENAI -> "OpenAI"
    Provider.ANTHROPIC -> "Anthropic"
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val provider by viewModel.provider.collectAsState()
    val apiKey by viewModel.apiKey.collectAsState()
    val model by viewModel.model.collectAsState()
    val useLocalModel by viewModel.useLocalModel.collectAsState()
    val localModelId by viewModel.localModelId.collectAsState()
    val downloadStatus by viewModel.downloadStatus.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val useLocalTts by viewModel.useLocalTts.collectAsState()
    val ttsVoice by viewModel.ttsVoice.collectAsState()
    val localTtsVoiceName by viewModel.localTtsVoiceName.collectAsState()
    val deviceVoices by viewModel.deviceVoices.collectAsState()
    val voiceSilence by viewModel.voiceSilenceSeconds.collectAsState()
    val openRouterModels by viewModel.openRouterModels.collectAsState()
    val catalogLoading by viewModel.catalogLoading.collectAsState()
    val embeddingModel = viewModel.embeddingModel

    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp).testTag("settingsList"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Four of the six screens carried a headline and this did not, so it
            // opened cold on a section label.
            Text("Settings", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))
            Text("Provider", style = MaterialTheme.typography.titleSmall)
            SelectionChips(
                options = Provider.entries,
                selected = provider,
                label = { providerLabel(it) },
                onSelect = { viewModel.setProvider(it) }
            )
        }
        item {
            // The key is a secret and this screen is reachable at any time, so it
            // is masked by default. Onboarding already masked it; leaving Settings
            // in the clear meant the same secret was hidden on one screen and
            // legible on the other. Revealing is opt-in, per visit, so a mistyped
            // key can still be checked.
            var keyVisible by rememberSaveable { mutableStateOf(false) }
            OutlinedTextField(
                value = apiKey,
                onValueChange = viewModel::setApiKey,
                label = { Text("API Key") },
                singleLine = true,
                visualTransformation =
                    if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { keyVisible = !keyVisible }) {
                        Text(if (keyVisible) "Hide" else "Show")
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("apiKeyField")
            )
        }
        item {
            OutlinedTextField(value = model, onValueChange = viewModel::setModel,
                label = { Text("Model") }, modifier = Modifier.fillMaxWidth())
        }

        if (provider == Provider.OPENROUTER) {
            item {
                OpenRouterModelSection(
                    models = openRouterModels,
                    selected = model,
                    loading = catalogLoading,
                    onSelect = viewModel::setModel,
                    onRefresh = { viewModel.refreshOpenRouterModels(force = true) }
                )
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Use on-device model", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(
                    checked = useLocalModel,
                    onCheckedChange = viewModel::setUseLocalModel,
                    modifier = Modifier.testTag("useLocalModelSwitch")
                )
            }
        }
        items(GGUFModelCatalog.allModels) { m ->
            val status = downloadStatus[m.id] ?: DownloadStatus.NOT_DOWNLOADED
            val canSelect = status == DownloadStatus.DOWNLOADED
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().clickable(enabled = canSelect) { viewModel.setLocalModelId(m.id) }
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(
                            selected = localModelId == m.id,
                            enabled = canSelect,
                            onClick = { viewModel.setLocalModelId(m.id) }
                        )
                        Column(Modifier.weight(1f)) {
                            Text(m.name, style = MaterialTheme.typography.bodyLarge)
                            // RAM first: the catalogue is ordered by it, and it is
                            // what decides whether the model will run at all.
                            // Leading with download size made the list look unsorted.
                            val sizeGb = "%.1f".format(m.sizeBytes / 1_000_000_000.0)
                            Text(
                                "Needs ${(m.minRamBytes / 1_000_000_000L)} GB RAM · $sizeGb GB download",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        DownloadActionButton(
                            status = status,
                            onDownload = { viewModel.downloadModel(m) },
                            onCancel = { viewModel.cancelDownload(m) },
                            onDelete = { viewModel.deleteModel(m) }
                        )
                    }
                    DownloadProgressAndError(status, downloadProgress[m.id])
                }
            }
        }
        item {
            Text("Semantic memory", style = MaterialTheme.typography.titleSmall)
            val embeddingStatus = downloadStatus[embeddingModel.id] ?: DownloadStatus.NOT_DOWNLOADED
            Surface(tonalElevation = 2.dp, shadowElevation = 2.dp, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(embeddingModel.name, style = MaterialTheme.typography.bodyLarge)
                            val sizeMb = (embeddingModel.onnxSizeBytes + embeddingModel.vocabSizeBytes) / 1_000_000L
                            Text("~$sizeMb MB · finds related past insights", style = MaterialTheme.typography.bodySmall)
                        }
                        DownloadActionButton(
                            status = embeddingStatus,
                            onDownload = viewModel::downloadEmbeddingModel,
                            onCancel = viewModel::cancelEmbeddingDownload,
                            onDelete = viewModel::deleteEmbeddingModel
                        )
                    }
                    DownloadProgressAndError(embeddingStatus, downloadProgress[embeddingModel.id])
                }
            }
        }
        item {
            Text("Voice", style = MaterialTheme.typography.titleSmall)
            Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(
                    "Read replies aloud on-device",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = useLocalTts,
                    onCheckedChange = viewModel::setUseLocalTts,
                    modifier = Modifier.testTag("useLocalTtsSwitch")
                )
            }
            Text(
                if (useLocalTts) {
                    "Uses your device's built-in text-to-speech. Works offline, no audio leaves your phone."
                } else {
                    "Uses the cloud provider's voice (requires an API key and network)."
                },
                style = MaterialTheme.typography.bodySmall
            )
        }
        item {
            if (useLocalTts) {
                DeviceVoicePicker(
                    grouped = deviceVoices,
                    selected = localTtsVoiceName,
                    onSelect = viewModel::setLocalTtsVoiceName,
                    onRefresh = viewModel::refreshDeviceVoices
                )
            } else {
                Text("Voice", style = MaterialTheme.typography.bodyLarge)
                SelectionChips(
                    options = viewModel.cloudVoices,
                    selected = ttsVoice,
                    label = { it.replaceFirstChar { c -> c.uppercase() } },
                    onSelect = viewModel::setTtsVoice,
                    modifier = Modifier.testTag("cloudVoicePicker")
                )
            }
        }
        item {
            Text(
                "Hands-free pause: ${"%.0f".format(voiceSilence)}s",
                style = MaterialTheme.typography.bodyLarge
            )
            Slider(
                value = voiceSilence.toFloat(),
                onValueChange = { viewModel.setVoiceSilenceSeconds(it.toDouble()) },
                valueRange = VoiceTranscript.MIN_SILENCE_SECONDS.toFloat()..
                    VoiceTranscript.MAX_SILENCE_SECONDS.toFloat(),
                steps = 9,
                modifier = Modifier.testTag("voiceSilenceSlider")
            )
            Text(
                "How long a pause has to run before a hands-free turn is treated as " +
                    "finished. Longer gives you more room to think mid-sentence.",
                style = MaterialTheme.typography.bodySmall
            )
        }
        item {
            Button(onClick = { viewModel.save() }) { Text("Save") }
        }
        item { AboutSection() }
    }
}

/**
 * About and crisis resources, folded into Settings rather than occupying a tab
 * of their own — iOS keeps its four tabs for Chats, Narrative, Insights and
 * Settings, and this is reference material rather than a destination.
 */
@Composable
private fun AboutSection() {
    Column(
        Modifier.fillMaxWidth().padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("About", style = MaterialTheme.typography.titleSmall)
        Text(
            "Selfward is a private companion for self-reflection. It is not a licensed " +
                "therapist and is not a substitute for professional mental-health care.",
            style = MaterialTheme.typography.bodyMedium
        )
        Text("Crisis resources", style = MaterialTheme.typography.titleSmall)
        Surface(tonalElevation = 2.dp, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
            Text(
                SafetyGuardrails.resourceMessage(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun DownloadActionButton(
    status: DownloadStatus,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    when (status) {
        DownloadStatus.NOT_DOWNLOADED -> TextButton(onClick = onDownload) { Text("Download") }
        DownloadStatus.FAILED -> TextButton(onClick = onDownload) { Text("Retry") }
        DownloadStatus.DOWNLOADING -> TextButton(onClick = onCancel) { Text("Cancel") }
        DownloadStatus.VERIFYING -> Text("Verifying…", style = MaterialTheme.typography.bodySmall)
        DownloadStatus.DOWNLOADED -> TextButton(onClick = onDelete) { Text("Delete") }
    }
}

@Composable
private fun DownloadProgressAndError(status: DownloadStatus, progress: DownloadProgress?) {
    if (status == DownloadStatus.DOWNLOADING) {
        if (progress != null && progress.totalBytes > 0) {
            LinearProgressIndicator(
                progress = { (progress.bytesDownloaded.toFloat() / progress.totalBytes).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            )
        } else {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
        }
    }
    if (status == DownloadStatus.FAILED) {
        Text(
            "Download failed or the file didn't match — tap Retry.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}

/**
 * OpenRouter's catalogue, free models first.
 *
 * The model field above is still a free-text box, because OpenRouter carries
 * hundreds of models and someone who knows the slug they want should be able to
 * type it. This list exists so that nobody has to: picking a provider whose
 * models are addressed by namespaced slug and being handed an empty box is how
 * the previous version sent people to the API with an id it would reject.
 */
@Composable
private fun OpenRouterModelSection(
    models: List<OpenRouterModel>,
    selected: String,
    loading: Boolean,
    onSelect: (String) -> Unit,
    onRefresh: () -> Unit
) {
    val free = models.filter { it.isFree }
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text(
                if (free.isEmpty()) "Free models" else "Free models (${free.size})",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onRefresh, enabled = !loading) {
                Text(if (loading) "Loading…" else "Refresh")
            }
        }

        if (free.isEmpty()) {
            Text(
                if (loading) "Fetching the catalogue from OpenRouter…"
                else "No catalogue yet. Tap Refresh — the list loads without a key, " +
                    "and adding yours shows what your account can reach.",
                style = MaterialTheme.typography.bodySmall
            )
            return@Column
        }

        Text(
            "Free models are rate-limited, and their providers may train on what is " +
                "sent. Switch on an on-device model if nothing should leave the phone.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            free.forEach { candidate ->
                OpenRouterModelRow(
                    model = candidate,
                    isSelected = candidate.id == selected,
                    onSelect = { onSelect(candidate.id) }
                )
            }
        }
    }
}

@Composable
private fun OpenRouterModelRow(
    model: OpenRouterModel,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        tonalElevation = if (isSelected) 4.dp else 1.dp,
        shadowElevation = if (isSelected) 3.dp else 1.dp,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect)
    ) {
        Row(
            Modifier.padding(10.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            RadioButton(selected = isSelected, onClick = onSelect)
            Column(Modifier.weight(1f)) {
                Text(model.shortName, style = MaterialTheme.typography.bodyMedium)
                Text(
                    listOfNotNull(
                        model.vendor.takeIf { it.isNotEmpty() },
                        model.contextLength.takeIf { it > 0 }?.let { "${it / 1000}k context" }
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}


/**
 * Grouped by [com.selfward.core.voice.VoiceTier], best tier first, matching
 * the Premium/Enhanced/Standard grouping iOS's own voice picker uses.
 */
@Composable
private fun DeviceVoicePicker(
    grouped: Map<com.selfward.core.voice.VoiceTier, List<com.selfward.core.voice.DeviceVoice>>,
    selected: String?,
    onSelect: (String) -> Unit,
    onRefresh: () -> Unit
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Text("Voice", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        TextButton(onClick = onRefresh) { Text("Refresh") }
    }
    if (grouped.isEmpty()) {
        Text(
            "No voices found yet. The system speech engine can take a moment to " +
                "start - tap Refresh once it has.",
            style = MaterialTheme.typography.bodySmall
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        com.selfward.core.voice.VoiceTier.entries.forEach { tier ->
            val voices = grouped[tier].orEmpty()
            if (voices.isEmpty()) return@forEach
            Text(
                tier.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelLarge
            )
            voices.forEach { voice ->
                Surface(
                    tonalElevation = if (voice.name == selected) 4.dp else 1.dp,
                    shadowElevation = if (voice.name == selected) 3.dp else 1.dp,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth().clickable { onSelect(voice.name) }
                ) {
                    Row(
                        Modifier.padding(10.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        RadioButton(selected = voice.name == selected, onClick = { onSelect(voice.name) })
                        Text(voice.locale, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
