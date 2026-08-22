package com.theraipist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.theraipist.R
import com.theraipist.config.TherapyConfig

@Composable
fun MainScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("therAIpist", style = MaterialTheme.typography.headlineMedium)
            Text("Android · Phase 0 scaffold")
            Text("Modalities available: ${TherapyConfig.ALL_MODALITIES.size}")
            Text(stringResource(R.string.disclaimer), style = MaterialTheme.typography.bodySmall)
        }
    }
}
