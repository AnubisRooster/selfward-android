package com.selfward.core.modality

/**
 * @param label how the mode is named to the client. Held here rather than in
 *   each screen because it was duplicated in two `modalityLabel` functions that
 *   had already drifted apart once.
 *
 * The constant names are persisted on every message, so they are fixed; only
 * [label] is free to change.
 */
enum class TherapyModality(val label: String) {
    TALK("Talk"),

    /**
     * Long-form writing met with a humanistic reflection. Called "Writing"
     * rather than "Journal" because the Journal tab, a note type, and this mode
     * were all called Journal, and only one of the three is a place to write
     * things down.
     */
    JOURNAL("Writing"),

    ACTIVE_IMAGINATION("Active Imagination"),
    ROLEPLAY("Roleplay"),
    DREAM("Dream"),
    GROUNDING("Grounding"),
    IDENTITY("Identity"),
    PURPOSE("Purpose"),
    AUDIO("Audio")
}
