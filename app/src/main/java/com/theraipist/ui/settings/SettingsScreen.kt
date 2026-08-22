package com.theraipist.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.theraipist.core.chat.Provider

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val provider by viewModel.provider.collectAsState()
    val apiKey by viewModel.apiKey.collectAsState()
    val model by viewModel.model.collectAsState()

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Provider", style = MaterialTheme.typography.titleSmall)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Provider.values().forEach { p ->
                Button(
                    onClick = { viewModel.setProvider(p) },
                    modifier = Modifier.weight(1f)
                ) { Text(p.name) }
            }
        }

        OutlinedTextField(
            value = apiKey,
            onValueChange = viewModel::setApiKey,
            label = { Text("API Key") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = model,
            onValueChange = viewModel::setModel,
            label = { Text("Model") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = { viewModel.save() }) { Text("Save") }
    }
}
