package com.theraipist.ui.persona

import androidx.lifecycle.ViewModel
import com.theraipist.config.CompanionGender
import com.theraipist.config.CompanionPersonality
import com.theraipist.config.PersonaKind
import com.theraipist.config.SpiritualTradition
import com.theraipist.core.PersonaHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PersonaViewModel @Inject constructor(
    private val personaHolder: PersonaHolder
) : ViewModel() {

    val persona = personaHolder.persona

    fun setKind(kind: PersonaKind) = personaHolder.update { it.copy(kind = kind) }
    fun setGender(gender: CompanionGender) = personaHolder.update { it.copy(companionGender = gender) }
    fun setPersonality(personality: CompanionPersonality) =
        personaHolder.update { it.copy(companionPersonality = personality) }

    fun setSpiritualTradition(tradition: SpiritualTradition) =
        personaHolder.update { it.copy(spiritualTradition = tradition) }
}
