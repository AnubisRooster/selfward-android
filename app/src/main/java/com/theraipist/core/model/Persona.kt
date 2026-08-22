package com.theraipist.core.model

import com.theraipist.config.CompanionGender
import com.theraipist.config.CompanionPersonality
import com.theraipist.config.PersonaKind
import com.theraipist.config.SpiritualTradition

data class Persona(
    val kind: PersonaKind,
    val name: String? = null,
    val companionGender: CompanionGender = CompanionGender.UNSPECIFIED,
    val companionPersonality: CompanionPersonality = CompanionPersonality.WARM,
    val spiritualTradition: SpiritualTradition = SpiritualTradition.INTERFAITH
)
