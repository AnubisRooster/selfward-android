package com.theraipist.ui.graph

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.theraipist.core.modality.TherapyModality

@Composable
fun GraphScreen(viewModel: GraphViewModel = hiltViewModel()) {
    val nodes by viewModel.nodes.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Memory & Insights", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Extracted insights accumulate here as you talk. Modalities supported: " +
                TherapyModality.values().joinToString { it.name.lowercase() },
            style = MaterialTheme.typography.bodyMedium
        )
        LazyColumn(
            Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(nodes) { node ->
                Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(node.label, style = MaterialTheme.typography.bodyLarge)
                        node.kind?.let {
                            Text(it, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
        if (nodes.isEmpty()) {
            Text("No insights yet — start a conversation.", style = MaterialTheme.typography.bodySmall)
        }
    }
}
