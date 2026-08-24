package com.selfward.core.safety

import com.selfward.config.TherapyConfig

enum class CrisisLevel { CRITICAL, WARNING }

object SafetyGuardrails {

    fun detectCrisis(text: String): CrisisLevel? {
        val t = text.lowercase()
        for (pattern in TherapyConfig.CRISIS_PATTERNS) {
            val hit = pattern.patterns.any { t.contains(it.lowercase()) }
            if (hit) {
                return if (pattern.level == "critical") CrisisLevel.CRITICAL else CrisisLevel.WARNING
            }
        }
        return null
    }

    fun detectBoundaryViolation(text: String): Boolean {
        val t = text.lowercase()
        return TherapyConfig.BOUNDARY_PATTERNS.any { t.contains(it.lowercase()) }
    }

    /**
     * Crisis lines for [country] (defaulting to the device's locale), followed by
     * the standing reassurance about availability.
     */
    fun resourceMessage(country: String? = java.util.Locale.getDefault().country): String =
        CrisisResources.forCountry(country).asMessage() + "\n\n" + TherapyConfig.RESOURCE_CLOSING

    /**
     * On return after a prior crisis, invite a gentle check-in before resuming
     * work. Returns null when no previous crisis was recorded.
     */
    fun reEntryCheck(previousCrisis: Boolean): String? = if (previousCrisis) {
        "Welcome back. Before we continue, I want to gently check in — how are you feeling in this moment?"
    } else null
}
