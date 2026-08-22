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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.theraipist.core.chat.Provider
import com.theraipist.core.local.GGUFModelCatalog

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val provider by viewModel.provider.collectAsState()
    val apiKey by viewModel.apiKey.collectAsState()
    val model by viewModel.model.collectAsState()
    val useLocalModel by viewModel.useLocalModel.collectAsState()
    val localModelId by viewModel.localModelId.collectAsState()

    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Provider", style = MaterialTheme.typography.titleSmall)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Provider.values().forEach { p ->
                    Button(onClick = { viewModel.setProvider(p) }, modifier = Modifier.weight(1f)) {
                        Text(p.name)
                    }
                }
            }
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
                Switch(checked = useLocalModel, onCheckedChange = viewModel::setUseLocalModel)
            }
        }
        items(GGUFModelCatalog.allModels) { m ->
            Surface(
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth().clickable { viewModel.setLocalModelId(m.id) }
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = localModelId == m.id, onClick = { viewModel.setLocalModelId(m.id) })
                    Column(Modifier.weight(1f)) {
                        Text(m.name, style = MaterialTheme.typography.bodyLarge)
                        Text("${m.sizeBytes / 1_000_000_000L} GB · min ${(m.minRamBytes / 1_000_000_000L)} GB RAM",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        item {
            Button(onClick = { viewModel.save() }) { Text("Save") }
        }
    }
}
