package com.theraipist.core.prompt

import com.theraipist.config.CompanionGender
import com.theraipist.config.CompanionPersonality
import com.theraipist.config.PersonaKind
import com.theraipist.config.SpiritualTradition
import com.theraipist.config.TherapyConfig
import com.theraipist.core.model.Message
import com.theraipist.core.model.Persona
import com.theraipist.core.model.Role

private const val THERAPIST_BASE =
    "You are therAIpist, a private AI therapy companion grounded in Jungian, Adlerian, and DBT traditions. " +
        "You listen deeply, reflect feelings, and help the client explore their experience. " +
        "You never diagnose or prescribe, and you are not a substitute for professional care."

object TherapyPromptBuilder {

    fun systemPrompt(persona: Persona, modality: String? = null): String {
        val base = when (persona.kind) {
            PersonaKind.THERAPIST -> THERAPIST_BASE
            PersonaKind.COMPANION -> buildCompanionPrompt(persona)
            PersonaKind.SPIRITUAL -> buildSpiritualPrompt(persona)
        }
        return if (persona.kind == PersonaKind.THERAPIST && modality != null) {
            val m = TherapyConfig.MODALITY_PROMPTS[modality]
            if (m != null) "$base\n\n$m" else base
        } else {
            base
        }
    }

    fun modalityInstruction(modality: String): String? = TherapyConfig.MODALITY_PROMPTS[modality]

    fun buildConversation(
        persona: Persona,
        modality: String? = null,
        history: List<Message> = emptyList(),
        userText: String
    ): List<Message> {
        val system = Message(
            id = "system",
            role = Role.SYSTEM,
            content = systemPrompt(persona, modality)
        )
        return buildList {
            add(system)
            addAll(history)
            add(
                Message(
                    id = "user-${System.currentTimeMillis()}",
                    role = Role.USER,
                    content = userText,
                    modality = modality
                )
            )
        }
    }

    private fun buildCompanionPrompt(persona: Persona): String {
        val name = persona.name ?: "your companion"
        val traits = listOf(persona.companionGender.promptLine, persona.companionPersonality.promptLine)
            .filter { it.isNotBlank() }
            .joinToString("\n\n")
        return TherapyConfig.COMPANION_PROMPT_TEMPLATE
            .replace("%NAME%", name)
            .replace("%TRAITS%", traits)
    }

    private fun buildSpiritualPrompt(persona: Persona): String {
        val name = persona.name ?: "your spiritual companion"
        return TherapyConfig.SPIRITUAL_PROMPT_TEMPLATE
            .replace("%NAME%", name)
            .replace("%TRADITION%", persona.spiritualTradition.promptLine)
    }
}
