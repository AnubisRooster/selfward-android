package com.selfward.core.safety

import java.util.Locale

/**
 * Crisis lines, chosen for where the user actually is.
 *
 * These were hardcoded to 988, 741741 and 911 — US-only numbers shown to
 * everyone. Someone in the UK or Australia at the worst moment of their week was
 * handed three numbers that do not work there, which is worse than showing
 * nothing, because it costs them the attempt.
 *
 * The region comes from the device locale. That is not the same as physical
 * location, and it can be wrong for travellers and for people who run their
 * phone in another language — but the alternative is asking for location
 * permission in an app whose whole proposition is that it collects nothing.
 * Every entry therefore also names the local emergency number, and the fallback
 * assumes nothing at all.
 *
 * These numbers change. They are worth re-checking before each release.
 */
object CrisisResources {

    data class Resource(
        /** Named lines, most specific to crisis first. */
        val lines: List<String>,
        /** What to dial if someone is in immediate danger. */
        val emergency: String?
    ) {
        fun asMessage(): String {
            val body = lines.joinToString("\n") { "- $it" }
            val tail = emergency?.let { "\n- Emergency services: $it" } ?: ""
            return body + tail
        }
    }

    private val UNITED_STATES = Resource(
        lines = listOf(
            "Suicide & Crisis Lifeline: call or text 988",
            "Crisis Text Line: text HOME to 741741"
        ),
        emergency = "911"
    )

    private val BY_COUNTRY: Map<String, Resource> = mapOf(
        "US" to UNITED_STATES,
        "CA" to Resource(
            lines = listOf("Suicide Crisis Helpline: call or text 988"),
            emergency = "911"
        ),
        "GB" to Resource(
            lines = listOf("Samaritans: call 116 123", "Shout: text SHOUT to 85258"),
            emergency = "999"
        ),
        "IE" to Resource(
            lines = listOf("Samaritans: call 116 123", "Text About It: text HELLO to 50808"),
            emergency = "112"
        ),
        "AU" to Resource(
            lines = listOf("Lifeline: call 13 11 14", "Beyond Blue: call 1300 22 4636"),
            emergency = "000"
        ),
        "NZ" to Resource(
            lines = listOf("Need to talk? call or text 1737"),
            emergency = "111"
        )
    )

    /**
     * The line to show when the country is unknown or unlisted. It names no
     * number it cannot stand behind, and points at a directory that covers the
     * rest of the world.
     */
    private val UNKNOWN = Resource(
        lines = listOf(
            "Find a helpline in your country: findahelpline.com",
            "If you are in immediate danger, call your local emergency number"
        ),
        emergency = null
    )

    /** @param country an ISO 3166-1 alpha-2 code, or null/blank if unknown. */
    fun forCountry(country: String?): Resource {
        val code = country?.trim()?.uppercase(Locale.ROOT)
        if (code.isNullOrEmpty()) return UNKNOWN
        return BY_COUNTRY[code] ?: UNKNOWN
    }

    /** The resource for the device's current locale. */
    fun forCurrentLocale(): Resource = forCountry(Locale.getDefault().country)

    /** True when a specific country's lines are known, rather than the fallback. */
    fun isKnown(country: String?): Boolean =
        BY_COUNTRY.containsKey(country?.trim()?.uppercase(Locale.ROOT).orEmpty())
}
