package com.theraipist.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.theraipist.config.TherapyConfig

@Composable
fun AboutScreen() {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("therAIpist", style = MaterialTheme.typography.headlineSmall)
        Text(
            "A private AI therapy companion grounded in Jungian, Adlerian, and DBT traditions. " +
                "It is not a substitute for professional care.",
            style = MaterialTheme.typography.bodyMedium
        )
        Text("Crisis resources", style = MaterialTheme.typography.titleSmall)
        Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
            Text(
                TherapyConfig.RESOURCE_MESSAGE,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(12.dp)
            )
        }
        Text("Version 0.1.0 (Phase 9)", style = MaterialTheme.typography.labelSmall)
    }
}
