package com.selfward.ui.journal

import com.selfward.config.PersonaKind
import com.selfward.core.journal.Dream
import com.selfward.core.journal.DreamRepository
import com.selfward.core.journal.Note
import com.selfward.core.journal.NoteRepository
import com.selfward.core.journal.NoteType
import com.selfward.core.model.Persona
import com.selfward.core.repository.InMemorySessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JournalViewModelTest {

    private class FakeNoteRepository : NoteRepository {
        val notes = mutableListOf<Note>()
        override suspend fun create(sessionId: String, type: NoteType, title: String, content: String): Note {
            val note = Note("n${notes.size}", sessionId, type, title, content, notes.size.toLong(), notes.size.toLong())
            notes += note
            return note
        }
        override suspend fun listForSession(sessionId: String) = notes.filter { it.sessionId == sessionId }
        override suspend fun listAll() = notes.toList()
        override suspend fun delete(noteId: String) { notes.removeAll { it.id == noteId } }
    }

    private class FakeDreamRepository : DreamRepository {
        val dreams = mutableListOf<Dream>()
        override suspend fun record(
            sessionId: String,
            narrative: String,
            feelings: List<String>,
            symbols: List<String>
        ): Dream {
            val dream = Dream("d${dreams.size}", sessionId, narrative, feelings, symbols, "", dreams.size.toLong())
            dreams += dream
            return dream
        }
        override suspend fun setAnalysis(dreamId: String, analysis: String) {}
        override suspend fun listForSession(sessionId: String) = dreams.filter { it.sessionId == sessionId }
        override suspend fun listAll() = dreams.toList()
        override suspend fun delete(dreamId: String) { dreams.removeAll { it.id == dreamId } }
    }

    private val notes = FakeNoteRepository()
    private val dreams = FakeDreamRepository()
    private val sessions = InMemorySessionRepository()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = JournalViewModel(notes, dreams, sessions)

    private suspend fun givenASession() =
        sessions.createSession(Persona(PersonaKind.THERAPIST), "A session").id

    @Test
    fun aNoteNeedsBothATitleAndSomeContent() {
        val vm = viewModel()
        assertFalse(vm.uiState.value.canSaveNote)

        vm.setNoteTitle("Title")
        assertFalse("a title alone is not enough", vm.uiState.value.canSaveNote)

        vm.setNoteContent("Content")
        assertTrue(vm.uiState.value.canSaveNote)
    }

    @Test
    fun savingANoteStoresItAgainstTheLatestSession() = runTest {
        val sessionId = givenASession()
        val vm = viewModel()
        vm.setNoteType(NoteType.JOURNAL)
        vm.setNoteTitle("  Tuesday  ")
        vm.setNoteContent("  went better  ")

        vm.saveNote()

        val saved = notes.notes.single()
        assertEquals(sessionId, saved.sessionId)
        assertEquals(NoteType.JOURNAL, saved.type)
        assertEquals("Tuesday", saved.title)
        assertEquals("went better", saved.content)
    }

    @Test
    fun theNoteFormIsClearedAfterSaving() = runTest {
        givenASession()
        val vm = viewModel()
        vm.setNoteTitle("Title")
        vm.setNoteContent("Content")

        vm.saveNote()

        assertEquals("", vm.uiState.value.noteTitle)
        assertEquals("", vm.uiState.value.noteContent)
    }

    /** Without a session there is nothing to attach to; say so rather than losing it. */
    @Test
    fun savingWithNoSessionReportsRatherThanSilentlyDropping() = runTest {
        val vm = viewModel()
        vm.setNoteTitle("Title")
        vm.setNoteContent("Content")

        vm.saveNote()

        assertTrue(notes.notes.isEmpty())
        assertTrue(vm.uiState.value.message!!.contains("Start a session"))
    }

    @Test
    fun recordingADreamExtractsItsSymbols() = runTest {
        givenASession()
        val vm = viewModel()
        vm.setDreamNarrative("a house beside dark water")
        vm.setDreamFeelings("uneasy, curious")

        vm.saveDream()

        val dream = dreams.dreams.single()
        assertEquals(listOf("water", "house"), dream.symbols)
        assertEquals(listOf("uneasy", "curious"), dream.feelings)
    }

    @Test
    fun blankFeelingsAreDroppedRatherThanStoredAsEmptyStrings() = runTest {
        givenASession()
        val vm = viewModel()
        vm.setDreamNarrative("a dream")
        vm.setDreamFeelings("calm, , ,  ")

        vm.saveDream()

        assertEquals(listOf("calm"), dreams.dreams.single().feelings)
    }

    @Test
    fun symbolsArePreviewedBeforeSaving() {
        val vm = viewModel()
        vm.setDreamNarrative("a snake near fire")

        assertEquals(listOf("snake", "fire"), vm.uiState.value.previewSymbols)
    }

    @Test
    fun aDreamNeedsANarrative() {
        val vm = viewModel()
        assertFalse(vm.uiState.value.canSaveDream)

        vm.setDreamNarrative("something")
        assertTrue(vm.uiState.value.canSaveDream)
    }

    @Test
    fun savedItemsAppearNewestFirst() = runTest {
        givenASession()
        val vm = viewModel()
        vm.setNoteTitle("first"); vm.setNoteContent("x"); vm.saveNote()
        vm.setNoteTitle("second"); vm.setNoteContent("y"); vm.saveNote()

        assertEquals(listOf("second", "first"), vm.uiState.value.notes.map { it.title })
    }

    @Test
    fun deletingRemovesTheItem() = runTest {
        givenASession()
        val vm = viewModel()
        vm.setNoteTitle("Title"); vm.setNoteContent("Content"); vm.saveNote()
        val id = notes.notes.single().id

        vm.deleteNote(id)

        assertTrue(notes.notes.isEmpty())
        assertTrue(vm.uiState.value.notes.isEmpty())
    }
}
