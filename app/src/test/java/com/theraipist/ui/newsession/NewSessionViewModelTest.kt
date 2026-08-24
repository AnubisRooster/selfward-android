package com.theraipist.ui.newsession

import com.theraipist.config.CompanionPersonality
import com.theraipist.config.PersonaKind
import com.theraipist.config.SpiritualTradition
import com.theraipist.core.ActiveSessionHolder
import com.theraipist.core.model.Persona
import com.theraipist.core.modality.TherapyModality
import com.theraipist.core.repository.InMemorySessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewSessionViewModelTest {

    private val repo = InMemorySessionRepository()
    private val holder = ActiveSessionHolder()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = NewSessionViewModel(repo, holder)

    private suspend fun createdPersona(): Persona? =
        repo.listSessions().firstOrNull()?.let { repo.getSession(it.id)?.persona }

    @Test
    fun defaultsToTheTherapist() {
        assertEquals(PersonaKind.THERAPIST, viewModel().uiState.value.kind)
    }

    /** Modality is a therapist-only concept, as on iOS. */
    @Test
    fun onlyTheTherapistShowsAModalityPicker() {
        val vm = viewModel()
        assertTrue(vm.uiState.value.showsModality)

        vm.setKind(PersonaKind.COMPANION)
        assertFalse(vm.uiState.value.showsModality)

        vm.setKind(PersonaKind.SPIRITUAL)
        assertFalse(vm.uiState.value.showsModality)
    }

    @Test
    fun creatingStoresTheChosenPersona() = runTest {
        val vm = viewModel()
        vm.setKind(PersonaKind.SPIRITUAL)
        vm.setSpiritualName("Sage")
        vm.setSpiritualTradition(SpiritualTradition.BUDDHIST)

        vm.create()

        val persona = createdPersona()
        assertEquals(PersonaKind.SPIRITUAL, persona?.kind)
        assertEquals("Sage", persona?.name)
        assertEquals(SpiritualTradition.BUDDHIST, persona?.spiritualTradition)
    }

    @Test
    fun companionCustomisationIsCarriedOntoTheSession() = runTest {
        val vm = viewModel()
        vm.setKind(PersonaKind.COMPANION)
        vm.setCompanionName("Robin")
        vm.setCompanionPersonality(CompanionPersonality.CALM)

        vm.create()

        val persona = createdPersona()
        assertEquals("Robin", persona?.name)
        assertEquals(CompanionPersonality.CALM, persona?.companionPersonality)
    }

    @Test
    fun aTypedTitleIsUsedVerbatim() = runTest {
        val vm = viewModel()
        vm.setTitle("  The thing about Tuesdays  ")

        vm.create()

        assertEquals("The thing about Tuesdays", repo.listSessions().single().title)
    }

    @Test
    fun anEmptyTitleFallsBackToADatedDefault() = runTest {
        val vm = viewModel()

        vm.create()

        val title = repo.listSessions().single().title
        assertTrue("expected a dated therapist default, got '$title'", title.startsWith("Session "))
    }

    @Test
    fun aCompanionSessionIsNamedAfterTheCompanion() = runTest {
        val vm = viewModel()
        vm.setKind(PersonaKind.COMPANION)
        vm.setCompanionName("Robin")

        vm.create()

        assertTrue(repo.listSessions().single().title.startsWith("Robin · "))
    }

    /** A blank name must not produce a session titled " · 01/01/2026". */
    @Test
    fun aBlankCompanionNameFallsBackToTheDefaultName() = runTest {
        val vm = viewModel()
        vm.setKind(PersonaKind.COMPANION)
        vm.setCompanionName("   ")

        vm.create()

        assertEquals("Kai", createdPersona()?.name)
        assertTrue(repo.listSessions().single().title.startsWith("Kai · "))
    }

    /**
     * The chat screen picks the session up through the holder, so creating one
     * without handing it over would drop the user into an empty conversation.
     */
    @Test
    fun theNewSessionIsHandedToTheChatScreen() = runTest {
        val vm = viewModel()

        vm.create()

        val handed = holder.consumePendingOpen()
        assertNotNull("the new session was never offered to the chat screen", handed)
        assertEquals(repo.listSessions().single().id, handed)
    }

    @Test
    fun creatingReportsCompletionSoTheScreenCanNavigate() = runTest {
        val vm = viewModel()
        assertFalse(vm.uiState.value.created)

        vm.create()

        assertTrue(vm.uiState.value.created)
    }

    @Test
    fun theSelectedModalityIsRetained() {
        val vm = viewModel()
        vm.setModality(TherapyModality.DREAM)
        assertEquals(TherapyModality.DREAM, vm.uiState.value.modality)
    }
}
