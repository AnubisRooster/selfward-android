package com.theraipist.core.intake

/**
 * What the person told us about themselves during onboarding.
 *
 * This is the most sensitive text the app holds — presenting concerns, therapy
 * history, goals. It stays on the device and is only ever put in front of an
 * on-device model. See [IntakeContext] for why that boundary exists.
 */
data class Intake(
    val name: String = "",
    val pronouns: String = "",
    val age: String = "",
    val concerns: String = "",
    val history: String = "",
    val goals: String = ""
) {
    val isEmpty: Boolean
        get() = listOf(name, pronouns, age, concerns, history, goals).all { it.isBlank() }
}

interface IntakeStore {
    fun load(): Intake
    fun save(intake: Intake)
    fun clear()

    /** Whether the first-launch flow has been completed. */
    var onboardingComplete: Boolean
}

/**
 * Renders [Intake] into the block that precedes the system prompt, matching the
 * shape iOS uses in `TherapyService`.
 *
 * **This block is only ever given to an on-device model.** The iOS app sends it
 * to whichever provider is configured; on Android it must not leave the device,
 * so callers pass it only on the local path — including making sure a fallback
 * from a failed local model to the cloud rebuilds the prompt without it.
 */
object IntakeContext {

    fun block(intake: Intake): String? {
        if (intake.isEmpty) return null

        val lines = buildList {
            val who = buildString {
                if (intake.name.isNotBlank()) append("The client's name is ${intake.name}.")
                if (intake.pronouns.isNotBlank()) {
                    if (isNotEmpty()) append(" ")
                    append("Pronouns: ${intake.pronouns}.")
                }
                if (intake.age.isNotBlank()) {
                    if (isNotEmpty()) append(" ")
                    append("Age: ${intake.age}.")
                }
            }
            if (who.isNotBlank()) add(who)
            if (intake.concerns.isNotBlank()) add("Presenting concerns: ${intake.concerns}")
            if (intake.history.isNotBlank()) add("Therapy background: ${intake.history}")
            if (intake.goals.isNotBlank()) add("Goals: ${intake.goals}")
        }

        if (lines.isEmpty()) return null
        return "Client context (from intake):\n" + lines.joinToString("\n")
    }
}
