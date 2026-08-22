package com.theraipist.core.safety

import com.theraipist.config.TherapyConfig

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

    fun resourceMessage(): String = TherapyConfig.RESOURCE_MESSAGE
}
