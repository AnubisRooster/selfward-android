package com.theraipist.core.modality

import com.theraipist.config.TherapyConfig

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
            else -> TherapyModality.TALK
        }
    }

    /** Framework prompt key used to build the system prompt for a modality. */
    fun promptKey(modality: TherapyModality): String = when (modality) {
        TherapyModality.ACTIVE_IMAGINATION -> "jungian"
        TherapyModality.DREAM -> "jungian"
        TherapyModality.GROUNDING -> "dbt"
        TherapyModality.IDENTITY -> "existential"
        TherapyModality.ROLEPLAY -> "gestalt"
        TherapyModality.JOURNAL -> "humanistic"
        TherapyModality.AUDIO -> "integrated"
        TherapyModality.TALK -> "integrated"
    }

    fun instruction(modality: TherapyModality): String? =
        TherapyConfig.MODALITY_PROMPTS[promptKey(modality)]
}
