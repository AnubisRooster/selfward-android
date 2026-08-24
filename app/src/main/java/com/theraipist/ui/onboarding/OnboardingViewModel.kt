package com.theraipist.ui.onboarding

import androidx.lifecycle.ViewModel
import com.theraipist.core.chat.Provider
import com.theraipist.core.intake.Intake
import com.theraipist.core.intake.IntakeStore
import com.theraipist.core.settings.SecureSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/** Steps, in the same order as the iOS onboarding. */
enum class OnboardingStep {
    WELCOME, DISCLAIMER, API_KEY, LOCAL_MODEL, ABOUT_YOU, CONCERNS, HISTORY, GOALS;

    val isLast: Boolean get() = this == GOALS
}

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val disclaimerAcknowledged: Boolean = false,
    val provider: Provider = Provider.OPENAI,
    val apiKey: String = "",
    val intake: Intake = Intake(),
    val finished: Boolean = false
) {
    /** The disclaimer is the one step that cannot be walked past. */
    val canContinue: Boolean
        get() = step != OnboardingStep.DISCLAIMER || disclaimerAcknowledged

    val progress: Float
        get() = (step.ordinal + 1f) / OnboardingStep.entries.size
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val intakeStore: IntakeStore,
    private val secureSettings: SecureSettings
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState = _uiState.asStateFlow()

    fun setAcknowledged(value: Boolean) = _uiState.update { it.copy(disclaimerAcknowledged = value) }
    fun setProvider(value: Provider) = _uiState.update { it.copy(provider = value) }
    fun setApiKey(value: String) = _uiState.update { it.copy(apiKey = value) }

    fun setName(value: String) = updateIntake { it.copy(name = value) }
    fun setPronouns(value: String) = updateIntake { it.copy(pronouns = value) }
    fun setAge(value: String) = updateIntake { it.copy(age = value) }
    fun setConcerns(value: String) = updateIntake { it.copy(concerns = value) }
    fun setHistory(value: String) = updateIntake { it.copy(history = value) }
    fun setGoals(value: String) = updateIntake { it.copy(goals = value) }

    private fun updateIntake(transform: (Intake) -> Intake) =
        _uiState.update { it.copy(intake = transform(it.intake)) }

    fun back() {
        val current = _uiState.value.step
        if (current == OnboardingStep.WELCOME) return
        _uiState.update { it.copy(step = OnboardingStep.entries[current.ordinal - 1]) }
    }

    fun next() {
        val state = _uiState.value
        if (!state.canContinue) return
        if (state.step.isLast) {
            finish()
        } else {
            _uiState.update { it.copy(step = OnboardingStep.entries[state.step.ordinal + 1]) }
        }
    }

    /** Persists everything gathered and marks onboarding done. */
    fun finish() {
        val state = _uiState.value
        val key = state.apiKey.trim()
        if (key.isNotEmpty()) {
            secureSettings.save(state.provider, key, secureSettings.model)
        }
        intakeStore.save(state.intake.trimmed())
        intakeStore.onboardingComplete = true
        _uiState.update { it.copy(finished = true) }
    }

    private fun Intake.trimmed() = Intake(
        name = name.trim(),
        pronouns = pronouns.trim(),
        age = age.trim(),
        concerns = concerns.trim(),
        history = history.trim(),
        goals = goals.trim()
    )
}
