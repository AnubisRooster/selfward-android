package com.selfward.core.modality

import org.junit.Assert.assertEquals
import org.junit.Test

class ModalityRouterTest {

    @Test
    fun defaultsToTalk() =
        assertEquals(TherapyModality.TALK, ModalityRouter.select("I'm having a normal day"))

    @Test
    fun routesDream() =
        assertEquals(TherapyModality.DREAM, ModalityRouter.select("I had a strange dream last night"))

    @Test
    fun routesActiveImagination() =
        assertEquals(TherapyModality.ACTIVE_IMAGINATION, ModalityRouter.select("Can we visualize an image?"))

    @Test
    fun routesGrounding() =
        assertEquals(TherapyModality.GROUNDING, ModalityRouter.select("I'm feeling a panic attack coming on"))

    @Test
    fun routesRoleplay() =
        assertEquals(TherapyModality.ROLEPLAY, ModalityRouter.select("Let's pretend you are my father"))

    @Test
    fun routesJournal() =
        assertEquals(TherapyModality.JOURNAL, ModalityRouter.select("I want to write about my week"))

    @Test
    fun routesLongInputToJournal() =
        assertEquals(TherapyModality.JOURNAL, ModalityRouter.select("a".repeat(500)))

    @Test
    fun routesIdentity() =
        assertEquals(TherapyModality.IDENTITY, ModalityRouter.select("who am I really"))

    @Test
    fun routesPurpose() {
        assertEquals(TherapyModality.PURPOSE, ModalityRouter.select("I'm searching for my purpose"))
        assertEquals(TherapyModality.PURPOSE, ModalityRouter.select("I never feel like I belong"))
        assertEquals(TherapyModality.PURPOSE, ModalityRouter.select("what are my goals really"))
    }

    /** "who am I" is the identity question; "where am I heading" is the Adlerian one. */
    @Test
    fun identityStillWinsOverPurposeForWhoAmI() =
        assertEquals(TherapyModality.IDENTITY, ModalityRouter.select("who am I really"))

    @Test
    fun promptKeyMapsJungianForDream() =
        assertEquals("jungian", ModalityRouter.promptKey(TherapyModality.DREAM))

    /** Active Imagination has its own phased prompt; it used to fall back to the Jungian one. */
    @Test
    fun activeImaginationUsesItsOwnPromptNotTheJungianOne() =
        assertEquals("active_imagination", ModalityRouter.promptKey(TherapyModality.ACTIVE_IMAGINATION))

    @Test
    fun purposeUsesTheAdlerianPrompt() =
        assertEquals("adlerian", ModalityRouter.promptKey(TherapyModality.PURPOSE))

    @Test
    fun instructionReturnsNonNullForKnownModality() =
        org.junit.Assert.assertNotNull(ModalityRouter.instruction(TherapyModality.GROUNDING))

    /**
     * Every mode must resolve to a prompt that actually exists. A typo or a
     * renamed key would otherwise silently drop the framework instruction and
     * leave the model running on the persona prompt alone.
     */
    @Test
    fun everyModalityResolvesToARealPrompt() {
        for (modality in TherapyModality.values()) {
            val key = ModalityRouter.promptKey(modality)
            org.junit.Assert.assertTrue(
                "$modality maps to '$key', which is not in ALL_MODALITIES",
                key in com.selfward.config.TherapyConfig.ALL_MODALITIES
            )
            org.junit.Assert.assertNotNull(
                "$modality resolves to no instruction text",
                ModalityRouter.instruction(modality)
            )
        }
    }

    /**
     * Documents which frameworks a user can actually reach. Docs and store copy
     * are written from this set, so a change here should be a deliberate one.
     */
    @Test
    fun reachableFrameworksAreTheDocumentedEight() {
        val reachable = TherapyModality.values().map { ModalityRouter.promptKey(it) }.toSortedSet()
        assertEquals(
            setOf(
                "active_imagination", "adlerian", "dbt", "existential",
                "gestalt", "humanistic", "integrated", "jungian"
            ).toSortedSet(),
            reachable
        )
    }
}
