package com.selfward.core.modality

import com.selfward.config.TherapyConfig

object ModalityRouter {

    fun select(input: String): TherapyModality {
        val t = input.lowercase()
        return when {
            t.contains("dream") -> TherapyModality.DREAM
            t.contains("imagine") || t.contains("image") || t.contains("visuali") ->
                TherapyModality.ACTIVE_IMAGINATION
            t.contains("ground") || t.contains("panic") || t.contains("anxiet") || t.contains("overwhelm") ->
                TherapyModality.GROUNDING
            t.contains("role") || t.contains("pretend") || t.contains("act as") ->
                TherapyModality.ROLEPLAY
            t.contains("journal") || t.contains("write") || input.length > 400 ->
                TherapyModality.JOURNAL
            t.contains("who am i") || t.contains("identity") || t.contains("true self") ->
                TherapyModality.IDENTITY
            // Adlerian territory: where the client is heading and where they fit,
            // as distinct from IDENTITY's question of who they are.
            t.contains("purpose") || t.contains("belong") || t.contains("goal") ||
                t.contains("fit in") || t.contains("contribut") ->
                TherapyModality.PURPOSE
            else -> TherapyModality.TALK
        }
    }

    /** Framework prompt key used to build the system prompt for a modality. */
    fun promptKey(modality: TherapyModality): String = when (modality) {
        TherapyModality.ACTIVE_IMAGINATION -> "active_imagination"
        TherapyModality.DREAM -> "jungian"
        TherapyModality.GROUNDING -> "dbt"
        TherapyModality.IDENTITY -> "existential"
        TherapyModality.PURPOSE -> "adlerian"
        TherapyModality.ROLEPLAY -> "gestalt"
        TherapyModality.JOURNAL -> "humanistic"
        TherapyModality.AUDIO -> "integrated"
        TherapyModality.TALK -> "integrated"
    }

    fun instruction(modality: TherapyModality): String? =
        TherapyConfig.MODALITY_PROMPTS[promptKey(modality)]
}
