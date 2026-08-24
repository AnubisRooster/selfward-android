package com.theraipist.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.theraipist.core.chat.Provider
import com.theraipist.core.local.DownloadProgress
import com.theraipist.core.local.DownloadStatus
import com.theraipist.core.local.GGUFModelCatalog
import com.theraipist.ui.components.SelectionChips

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
    val embeddingModel = viewModel.embeddingModel

    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp).testTag("settingsList"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Provider", style = MaterialTheme.typography.titleSmall)
            SelectionChips(
                options = Provider.entries,
                selected = provider,
                label = { providerLabel(it) },
                onSelect = { viewModel.setProvider(it) }
            )
        }
        item {
            OutlinedTextField(value = apiKey, onValueChange = viewModel::setApiKey,
                label = { Text("API Key") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(value = model, onValueChange = viewModel::setModel,
                label = { Text("Model") }, modifier = Modifier.fillMaxWidth())
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
                            val sizeGb = "%.1f".format(m.sizeBytes / 1_000_000_000.0)
                            Text(
                                "$sizeGb GB · min ${(m.minRamBytes / 1_000_000_000L)} GB RAM",
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
            Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
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
            Button(onClick = { viewModel.save() }) { Text("Save") }
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
