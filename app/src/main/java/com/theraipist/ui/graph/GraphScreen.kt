package com.theraipist.ui.graph

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.theraipist.core.modality.TherapyModality

@Composable
fun GraphScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Memory & Insights", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Persistent therapy graph and extracted insights will be visualized here. " +
                "Supported modalities:",
            style = MaterialTheme.typography.bodyMedium
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(TherapyModality.values().toList()) { modality ->
                Surface(
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(modality.name, modifier = Modifier.padding(12.dp))
                }
            }
        }
    }
}
