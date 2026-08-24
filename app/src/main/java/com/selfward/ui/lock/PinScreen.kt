package com.selfward.ui.lock

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfward.core.lock.PIN_LENGTH

@Composable
fun PinScreen(
    onUnlocked: () -> Unit,
    viewModel: PinViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (state.unlocked) {
        onUnlocked()
        return
    }

    val shake by animateFloatAsState(
        targetValue = if (state.shaking) 1f else 0f,
        label = "pin-shake"
    )

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            Modifier.fillMaxWidth().padding(top = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(state.heading, style = MaterialTheme.typography.headlineSmall)

            Row(
                Modifier.graphicsLayer { translationX = if (shake > 0f) -18f * shake else 0f },
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                repeat(PIN_LENGTH) { index ->
                    val filled = index < state.entered.length
                    Box(
                        Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(
                                if (filled) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                            )
                    )
                }
            }

            if (state.message.isNotEmpty()) {
                Text(
                    state.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("pinMessage")
                )
            }

            // A PIN keeps the session list off a passer-by's screen. It is not
            // device encryption, and the UI should not suggest that it is.
            if (state.showsPrivacyNote) {
                Text(
                    "This keeps your sessions off the screen if someone else picks up your " +
                        "phone. It does not encrypt what is stored on the device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        NumberPad(
            enabled = !state.lockedOut,
            onDigit = viewModel::append,
            onDelete = viewModel::deleteLast
        )
    }
}

@Composable
private fun NumberPad(
    enabled: Boolean,
    onDigit: (Char) -> Unit,
    onDelete: () -> Unit
) {
    val alpha = if (enabled) 1f else 0.4f
    Column(
        Modifier.fillMaxWidth().padding(bottom = 32.dp).graphicsLayer { this.alpha = alpha },
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        listOf("123", "456", "789").forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                row.forEach { digit -> PinKey(digit.toString(), enabled) { onDigit(digit) } }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Spacer(Modifier.size(76.dp))
            PinKey("0", enabled) { onDigit('0') }
            Box(
                Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .clickable(enabled = enabled, onClick = onDelete)
                    .testTag("pinDelete"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun PinKey(label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(enabled = enabled, onClick = onClick)
            .testTag("pinKey$label"),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.titleLarge)
    }
}
