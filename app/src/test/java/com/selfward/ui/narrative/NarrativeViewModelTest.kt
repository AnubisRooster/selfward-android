package com.selfward.ui.narrative

import com.selfward.config.PersonaKind
import com.selfward.core.ModelSettings
import com.selfward.core.chat.ChatService
import com.selfward.core.chat.MissingApiKeyException
import com.selfward.core.journal.Dream
import com.selfward.core.journal.DreamRepository
import com.selfward.core.journal.Note
import com.selfward.core.journal.NoteRepository
import com.selfward.core.journal.NoteType
import com.selfward.core.local.LocalLLMService
import com.selfward.core.local.LocalModel
import com.selfward.core.model.Message
import com.selfward.core.model.Persona
import com.selfward.core.model.Role
import com.selfward.core.narrative.NarrativeDocument
import com.selfward.core.narrative.NarrativeStore
import com.selfward.core.repository.InMemorySessionRepository
import com.selfward.data.export.ExportFiles
import com.selfward.data.export.NarrativePdfWriter
import com.selfward.data.settings.FakeSecureSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Robolectric because exporting writes a real file and hands back a content
// uri, and neither exists on a bare JVM.
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NarrativeViewModelTest {

    private class FakeNarrativeStore(private var doc: NarrativeDocument? = null) : NarrativeStore {
        override suspend fun load() = doc
        override suspend fun save(document: NarrativeDocument) { doc = document }
        override suspend fun clear() { doc = null }
        val saved: NarrativeDocument? get() = doc
    }

    private class FakeNoteRepository(private val items: List<Note> = emptyList()) : NoteRepository {
        override suspend fun create(sessionId: String, type: NoteType, title: String, content: String) =
            throw UnsupportedOperationException()
        override suspend fun listForSession(sessionId: String) = items
        override suspend fun listAll() = items
        override suspend fun delete(noteId: String) {}
    }

    private class FakeDreamRepository(private val items: List<Dream> = emptyList()) : DreamRepository {
        override suspend fun record(sessionId: String, narrative: String, feelings: List<String>, symbols: List<String>) =
            throw UnsupportedOperationException()
        override suspend fun setAnalysis(dreamId: String, analysis: String) {}
        override suspend fun listForSession(sessionId: String) = items
        override suspend fun listAll() = items
        override suspend fun delete(dreamId: String) {}
    }

    private class RecordingChatService(private val reply: String = "Once upon a time") : ChatService {
        var lastMessages: List<Message>? = null
        override fun sendStreaming(messages: List<Message>): Flow<String> = flow {
            lastMessages = messages
            emit(reply)
        }
    }

    private class FailingChatService(private val error: Exception) : ChatService {
        override fun sendStreaming(messages: List<Message>): Flow<String> = flow { throw error }
    }

    private class RecordingLocalLLM(private val reply: String = "local story") : LocalLLMService {
        var lastMessages: List<Message>? = null
        override suspend fun isModelLoaded() = true
        override suspend fun load(model: LocalModel, path: String) {}
        override fun stream(messages: List<Message>): Flow<String> = flow {
            lastMessages = messages
            emit(reply)
        }
        override fun close() {}
    }

    private val sessions = InMemorySessionRepository()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun note(at: Long, text: String) =
        Note("n$at", "s1", NoteType.REFLECTION, "t", text, at, at)

    private fun viewModel(
        store: NarrativeStore = FakeNarrativeStore(),
        notes: NoteRepository = FakeNoteRepository(),
        dreams: DreamRepository = FakeDreamRepository(),
        chat: ChatService = RecordingChatService(),
        local: LocalLLMService = RecordingLocalLLM(),
        settings: ModelSettings = ModelSettings(FakeSecureSettings())
    ) = NarrativeViewModel(
        store, notes, dreams, sessions, chat, local, settings,
        ExportFiles(androidx.test.core.app.ApplicationProvider.getApplicationContext()),
        NarrativePdfWriter()
    )

    private suspend fun givenASession() =
        sessions.createSession(Persona(PersonaKind.THERAPIST), "A session").id

    @Test
    fun startsEmpty() {
        assertTrue(viewModel().uiState.value.document.isEmpty)
    }

    @Test
    fun writingStoresTheGeneratedProse() = runTest {
        givenASession()
        val store = FakeNarrativeStore()
        val vm = viewModel(store = store, notes = FakeNoteRepository(listOf(note(10, "a note"))))

        vm.regenerate()

        assertEquals("Once upon a time", store.saved?.content)
        assertEquals("Once upon a time", vm.uiState.value.document.content)
    }

    /**
     * The watermark must land on the newest source actually used. Too low and
     * material is read twice; too high and later material is skipped entirely.
     */
    @Test
    fun theWatermarkAdvancesToTheNewestSourceUsed() = runTest {
        givenASession()
        val store = FakeNarrativeStore()
        val vm = viewModel(
            store = store,
            notes = FakeNoteRepository(listOf(note(10, "old"), note(40, "new")))
        )

        vm.regenerate()

        assertEquals(40L, store.saved?.sourceWatermark)
    }

    @Test
    fun nothingNewerThanTheWatermarkReportsRatherThanRewriting() = runTest {
        givenASession()
        val existing = NarrativeDocument(content = "Existing story", sourceWatermark = 100)
        val store = FakeNarrativeStore(existing)
        val vm = viewModel(store = store, notes = FakeNoteRepository(listOf(note(10, "old"))))

        vm.regenerate()

        assertTrue(vm.uiState.value.nothingNew)
        assertEquals("Existing story", store.saved?.content)
        assertEquals("the watermark must not move when nothing was used", 100L, store.saved?.sourceWatermark)
    }

    @Test
    fun anExistingNarrativeIsHandedBackForRevisionRatherThanReplaced() = runTest {
        givenASession()
        val chat = RecordingChatService()
        val vm = viewModel(
            store = FakeNarrativeStore(NarrativeDocument(content = "Chapter one", sourceWatermark = 5)),
            notes = FakeNoteRepository(listOf(note(10, "something new"))),
            chat = chat
        )

        vm.regenerate()

        val user = chat.lastMessages!!.single { it.role == Role.USER }.content
        assertTrue("the existing story must be sent so it can be revised", user.contains("Chapter one"))
        assertTrue(user.contains("something new"))
    }

    @Test
    fun aMissingApiKeyIsExplainedRatherThanShownRaw() = runTest {
        givenASession()
        val vm = viewModel(
            notes = FakeNoteRepository(listOf(note(10, "a note"))),
            chat = FailingChatService(MissingApiKeyException())
        )

        vm.regenerate()

        assertTrue(vm.uiState.value.error!!.contains("Settings"))
        assertFalse(vm.uiState.value.building)
    }

    @Test
    fun aFailureLeavesTheStoredNarrativeUntouched() = runTest {
        givenASession()
        val store = FakeNarrativeStore(NarrativeDocument(content = "Chapter one", sourceWatermark = 5))
        val vm = viewModel(
            store = store,
            notes = FakeNoteRepository(listOf(note(10, "new"))),
            chat = FailingChatService(RuntimeException("network unreachable"))
        )

        vm.regenerate()

        assertEquals("Chapter one", store.saved?.content)
        assertEquals(5L, store.saved?.sourceWatermark)
    }

    /** A blank reply must not overwrite a real narrative with nothing. */
    @Test
    fun anEmptyReplyDoesNotWipeTheNarrative() = runTest {
        givenASession()
        val store = FakeNarrativeStore(NarrativeDocument(content = "Chapter one", sourceWatermark = 5))
        val vm = viewModel(
            store = store,
            notes = FakeNoteRepository(listOf(note(10, "new"))),
            chat = RecordingChatService(reply = "   ")
        )

        vm.regenerate()

        assertEquals("Chapter one", store.saved?.content)
        assertTrue(vm.uiState.value.error!!.isNotBlank())
    }

    @Test
    fun theOnDeviceModelIsUsedWhenSelected() = runTest {
        givenASession()
        val local = RecordingLocalLLM()
        val chat = RecordingChatService()
        val settings = ModelSettings(FakeSecureSettings()).also {
            it.setUseLocalModel(true)
            it.setLocalModelId("tinyllama-1.1b")
        }

        viewModel(
            notes = FakeNoteRepository(listOf(note(10, "a note"))),
            chat = chat,
            local = local,
            settings = settings
        ).regenerate()

        assertTrue("the on-device model should have written it", local.lastMessages != null)
        assertTrue("nothing should have gone to the cloud", chat.lastMessages == null)
    }

    @Test
    fun theScreenKnowsWhetherRegeneratingWouldLeaveTheDevice() = runTest {
        val settings = ModelSettings(FakeSecureSettings()).also {
            it.setUseLocalModel(true)
            it.setLocalModelId("tinyllama-1.1b")
        }
        assertTrue(viewModel(settings = settings).uiState.value.onDevice)
        assertFalse(viewModel().uiState.value.onDevice)
    }

    @Test
    fun dreamsAndNotesAreBothOfferedAsMaterial() = runTest {
        givenASession()
        val chat = RecordingChatService()
        viewModel(
            notes = FakeNoteRepository(listOf(note(10, "a note"))),
            dreams = FakeDreamRepository(
                listOf(Dream("d1", "s1", "a dream about water", emptyList(), listOf("water"), "", 20))
            ),
            chat = chat
        ).regenerate()

        val user = chat.lastMessages!!.single { it.role == Role.USER }.content
        assertTrue(user.contains("a note"))
        assertTrue(user.contains("a dream about water"))
    }
}
