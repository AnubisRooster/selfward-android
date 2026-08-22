package com.theraipist.core.modality

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
    fun promptKeyMapsJungianForDream() =
        assertEquals("jungian", ModalityRouter.promptKey(TherapyModality.DREAM))

    @Test
    fun instructionReturnsNonNullForKnownModality() =
        org.junit.Assert.assertNotNull(ModalityRouter.instruction(TherapyModality.GROUNDING))
}
