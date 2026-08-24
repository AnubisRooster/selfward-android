package com.theraipist.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import com.theraipist.config.TherapyConfig
import com.theraipist.core.chat.Provider
import com.theraipist.ui.components.SelectionChips

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (state.finished) {
        onFinished()
        return
    }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        if (state.step != OnboardingStep.WELCOME) {
            LinearProgressIndicator(
                progress = { state.progress },
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
            )
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (state.step) {
                OnboardingStep.WELCOME -> WelcomeStep()
                OnboardingStep.DISCLAIMER -> DisclaimerStep(
                    acknowledged = state.disclaimerAcknowledged,
                    onAcknowledge = viewModel::setAcknowledged
                )
                OnboardingStep.API_KEY -> ApiKeyStep(
                    provider = state.provider,
                    apiKey = state.apiKey,
                    onProvider = viewModel::setProvider,
                    onApiKey = viewModel::setApiKey
                )
                OnboardingStep.LOCAL_MODEL -> LocalModelStep()
                OnboardingStep.ABOUT_YOU -> AboutYouStep(state, viewModel)
                OnboardingStep.CONCERNS -> IntakeStep(
                    title = "What brings you here?",
                    blurb = "A sentence or two is plenty. You can skip this and add it later in Settings.",
                    value = state.intake.concerns,
                    onValueChange = viewModel::setConcerns,
                    tag = "concerns"
                )
                OnboardingStep.HISTORY -> IntakeStep(
                    title = "Any background worth knowing?",
                    blurb = "Past therapy, what helped, what didn't. Optional.",
                    value = state.intake.history,
                    onValueChange = viewModel::setHistory,
                    tag = "history"
                )
                OnboardingStep.GOALS -> GoalsStep(
                    value = state.intake.goals,
                    onValueChange = viewModel::setGoals
                )
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.step != OnboardingStep.WELCOME) {
                TextButton(onClick = viewModel::back) { Text("Back") }
            }
            Spacer(Modifier.weight(1f))
            if (state.step == OnboardingStep.LOCAL_MODEL || state.step.ordinal >= OnboardingStep.ABOUT_YOU.ordinal) {
                TextButton(onClick = viewModel::next) { Text("Skip") }
            }
            Button(
                onClick = viewModel::next,
                enabled = state.canContinue,
                modifier = Modifier.testTag("onboardingContinue")
            ) {
                Text(if (state.step.isLast) "Get started" else "Continue")
            }
        }
    }
}

@Composable
private fun WelcomeStep() {
    Text("Welcome to therAIpist", style = MaterialTheme.typography.headlineMedium)
    Text(
        "A private space to think out loud. Conversations stay on your device, and " +
            "you can run everything offline if you'd rather nothing left your phone at all.",
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
private fun DisclaimerStep(acknowledged: Boolean, onAcknowledge: (Boolean) -> Unit) {
    Text("Before we start", style = MaterialTheme.typography.headlineSmall)
    Text(
        "therAIpist is not a licensed therapist, psychologist, or medical provider. " +
            "It is a journaling and self-reflection tool. It cannot diagnose, treat, or " +
            "manage any mental health condition.",
        style = MaterialTheme.typography.bodyMedium
    )
    Text("If you are in crisis, please reach a real person:", style = MaterialTheme.typography.titleSmall)
    Text(TherapyConfig.RESOURCE_MESSAGE, style = MaterialTheme.typography.bodyMedium)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = acknowledged,
            onCheckedChange = onAcknowledge,
            modifier = Modifier.testTag("disclaimerCheckbox")
        )
        Text(
            "I understand that therAIpist is not a licensed therapist and is not a " +
                "substitute for professional mental health care.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ApiKeyStep(
    provider: Provider,
    apiKey: String,
    onProvider: (Provider) -> Unit,
    onApiKey: (String) -> Unit
) {
    Text("Connect a provider", style = MaterialTheme.typography.headlineSmall)
    Text(
        "Bring your own API key for higher-quality replies. Skip this if you'd rather " +
            "run a model on your phone — the next step covers that.",
        style = MaterialTheme.typography.bodyMedium
    )
    SelectionChips(
        options = Provider.entries,
        selected = provider,
        label = {
            when (it) {
                Provider.OPENROUTER -> "OpenRouter"
                Provider.OPENAI -> "OpenAI"
                Provider.ANTHROPIC -> "Anthropic"
            }
        },
        onSelect = onProvider
    )
    OutlinedTextField(
        value = apiKey,
        onValueChange = onApiKey,
        label = { Text("API key") },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth().testTag("onboardingApiKey")
    )
    Text(
        "Your key is stored in encrypted storage on this device and is only ever sent " +
            "to the provider you picked.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun LocalModelStep() {
    Text("Or run it on your phone", style = MaterialTheme.typography.headlineSmall)
    Text(
        "therAIpist can run a language model entirely on this device, with no API key " +
            "and no network connection. Replies are slower and simpler than a cloud model, " +
            "but nothing you write ever leaves your phone.",
        style = MaterialTheme.typography.bodyMedium
    )
    Text(
        "Models are 0.7–2.4 GB and download on demand. You can pick one any time from " +
            "Settings, so there's no need to decide now.",
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun AboutYouStep(state: OnboardingUiState, viewModel: OnboardingViewModel) {
    Text("A little about you", style = MaterialTheme.typography.headlineSmall)
    Text(
        "All optional, and kept on this device.",
        style = MaterialTheme.typography.bodyMedium
    )
    OutlinedTextField(
        value = state.intake.name,
        onValueChange = viewModel::setName,
        label = { Text("What should I call you?") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = state.intake.pronouns,
        onValueChange = viewModel::setPronouns,
        label = { Text("Pronouns") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = state.intake.age,
        onValueChange = viewModel::setAge,
        label = { Text("Age") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
    OnDeviceOnlyNote()
}

@Composable
private fun IntakeStep(
    title: String,
    blurb: String,
    value: String,
    onValueChange: (String) -> Unit,
    tag: String
) {
    Text(title, style = MaterialTheme.typography.headlineSmall)
    Text(blurb, style = MaterialTheme.typography.bodyMedium)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        minLines = 4,
        modifier = Modifier.fillMaxWidth().testTag(tag)
    )
    OnDeviceOnlyNote()
}

@Composable
private fun GoalsStep(value: String, onValueChange: (String) -> Unit) {
    IntakeStep(
        title = "What would you like to get out of this?",
        blurb = "However small. Optional.",
        value = value,
        onValueChange = onValueChange,
        tag = "goals"
    )
    Text(
        "therAIpist works best alongside real human support. If you ever feel " +
            "overwhelmed, please reach out to someone — in the US you can call or text 988.",
        style = MaterialTheme.typography.bodyMedium
    )
}

/**
 * The honest boundary on intake answers: unlike iOS, Android never sends these
 * to a cloud provider, so the screen that collects them says so.
 */
@Composable
private fun OnDeviceOnlyNote() {
    Text(
        "What you write here is only ever shown to a model running on your phone. " +
            "It is never sent to a cloud provider, even when you're using one for chat.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
