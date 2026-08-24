package com.selfward.core.model

import com.selfward.config.CompanionGender
import com.selfward.config.CompanionPersonality
import com.selfward.config.PersonaKind
import com.selfward.config.SpiritualTradition

data class Persona(
    val kind: PersonaKind,
    val name: String? = null,
    val companionGender: CompanionGender = CompanionGender.UNSPECIFIED,
    val companionPersonality: CompanionPersonality = CompanionPersonality.WARM,
    val spiritualTradition: SpiritualTradition = SpiritualTradition.INTERFAITH
)
