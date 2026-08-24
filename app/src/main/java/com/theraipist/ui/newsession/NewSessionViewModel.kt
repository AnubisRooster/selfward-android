package com.theraipist.ui.newsession

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theraipist.config.CompanionGender
import com.theraipist.config.CompanionPersonality
import com.theraipist.config.PersonaKind
import com.theraipist.config.SpiritualTradition
import com.theraipist.core.ActiveSessionHolder
import com.theraipist.core.model.Persona
import com.theraipist.core.modality.TherapyModality
import com.theraipist.core.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject

data class NewSessionUiState(
    val kind: PersonaKind = PersonaKind.THERAPIST,
    val title: String = "",
    val modality: TherapyModality = TherapyModality.TALK,
    val companionName: String = "Kai",
    val companionGender: CompanionGender = CompanionGender.UNSPECIFIED,
    val companionPersonality: CompanionPersonality = CompanionPersonality.WARM,
    val spiritualName: String = "Sage",
    val spiritualTradition: SpiritualTradition = SpiritualTradition.INTERFAITH,
    val created: Boolean = false
) {
    /** Modality only applies to the therapist persona, as on iOS. */
    val showsModality: Boolean get() = kind == PersonaKind.THERAPIST

    val personaName: String
        get() = when (kind) {
            PersonaKind.THERAPIST -> "your therapist"
            PersonaKind.COMPANION -> companionName.trim().ifEmpty { "Kai" }
            PersonaKind.SPIRITUAL -> spiritualName.trim().ifEmpty { "Sage" }
        }

    val blurb: String
        get() = when (kind) {
            PersonaKind.THERAPIST -> "A reflective space using a chosen therapeutic frame."
            PersonaKind.COMPANION -> "A warmer, more casual presence to think out loud with."
            PersonaKind.SPIRITUAL -> "Draws on a tradition's wisdom, without proselytising or judging."
        }
}

@HiltViewModel
class NewSessionViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val activeSessionHolder: ActiveSessionHolder
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewSessionUiState())
    val uiState = _uiState.asStateFlow()

    fun setKind(value: PersonaKind) = _uiState.update { it.copy(kind = value) }
    fun setTitle(value: String) = _uiState.update { it.copy(title = value) }
    fun setModality(value: TherapyModality) = _uiState.update { it.copy(modality = value) }
    fun setCompanionName(value: String) = _uiState.update { it.copy(companionName = value) }
    fun setCompanionGender(value: CompanionGender) = _uiState.update { it.copy(companionGender = value) }
    fun setCompanionPersonality(value: CompanionPersonality) =
        _uiState.update { it.copy(companionPersonality = value) }
    fun setSpiritualName(value: String) = _uiState.update { it.copy(spiritualName = value) }
    fun setSpiritualTradition(value: SpiritualTradition) =
        _uiState.update { it.copy(spiritualTradition = value) }

    fun create() {
        val state = _uiState.value
        viewModelScope.launch {
            val session = sessionRepository.createSession(state.toPersona(), state.resolvedTitle())
            // The chat screen picks the session up through the same holder the
            // session list uses, so opening a new session and opening an old one
            // take exactly the same path.
            activeSessionHolder.open(session.id)
            _uiState.update { it.copy(created = true) }
        }
    }

    private fun NewSessionUiState.toPersona() = Persona(
        kind = kind,
        name = when (kind) {
            PersonaKind.THERAPIST -> null
            PersonaKind.COMPANION -> companionName.trim().ifEmpty { "Kai" }
            PersonaKind.SPIRITUAL -> spiritualName.trim().ifEmpty { "Sage" }
        },
        companionGender = companionGender,
        companionPersonality = companionPersonality,
        spiritualTradition = spiritualTradition
    )

    private fun NewSessionUiState.resolvedTitle(): String {
        val typed = title.trim()
        if (typed.isNotEmpty()) return typed
        val date = DateFormat.getDateInstance(DateFormat.SHORT).format(Date())
        return when (kind) {
            PersonaKind.THERAPIST -> "Session $date"
            else -> "$personaName · $date"
        }
    }
}
