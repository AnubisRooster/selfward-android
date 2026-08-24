package com.selfward.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfward.core.journal.Dream
import com.selfward.core.journal.DreamRepository
import com.selfward.core.journal.DreamSymbols
import com.selfward.core.journal.Note
import com.selfward.core.journal.NoteRepository
import com.selfward.core.journal.NoteType
import com.selfward.core.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JournalUiState(
    val notes: List<Note> = emptyList(),
    val dreams: List<Dream> = emptyList(),
    val noteType: NoteType = NoteType.REFLECTION,
    val noteTitle: String = "",
    val noteContent: String = "",
    val dreamNarrative: String = "",
    val dreamFeelings: String = "",
    val message: String? = null
) {
    val canSaveNote: Boolean get() = noteTitle.isNotBlank() && noteContent.isNotBlank()
    val canSaveDream: Boolean get() = dreamNarrative.isNotBlank()

    /** Symbols found so far, shown live so the match is visible before saving. */
    val previewSymbols: List<String> get() = DreamSymbols.extract(dreamNarrative)
}

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val dreamRepository: DreamRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    notes = noteRepository.listAll().sortedByDescending { n -> n.createdAt },
                    dreams = dreamRepository.listAll().sortedByDescending { d -> d.createdAt }
                )
            }
        }
    }

    fun setNoteType(value: NoteType) = _uiState.update { it.copy(noteType = value) }
    fun setNoteTitle(value: String) = _uiState.update { it.copy(noteTitle = value) }
    fun setNoteContent(value: String) = _uiState.update { it.copy(noteContent = value) }
    fun setDreamNarrative(value: String) = _uiState.update { it.copy(dreamNarrative = value) }
    fun setDreamFeelings(value: String) = _uiState.update { it.copy(dreamFeelings = value) }
    fun dismissMessage() = _uiState.update { it.copy(message = null) }

    fun saveNote() {
        val state = _uiState.value
        if (!state.canSaveNote) return
        viewModelScope.launch {
            val sessionId = resolveSessionId() ?: return@launch fail("Start a session first.")
            noteRepository.create(
                sessionId = sessionId,
                type = state.noteType,
                title = state.noteTitle.trim(),
                content = state.noteContent.trim()
            )
            _uiState.update { it.copy(noteTitle = "", noteContent = "") }
            refresh()
        }
    }

    fun saveDream() {
        val state = _uiState.value
        if (!state.canSaveDream) return
        viewModelScope.launch {
            val sessionId = resolveSessionId() ?: return@launch fail("Start a session first.")
            val narrative = state.dreamNarrative.trim()
            dreamRepository.record(
                sessionId = sessionId,
                narrative = narrative,
                feelings = state.dreamFeelings.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                symbols = DreamSymbols.extract(narrative)
            )
            _uiState.update { it.copy(dreamNarrative = "", dreamFeelings = "") }
            refresh()
        }
    }

    fun deleteNote(id: String) = viewModelScope.launch {
        noteRepository.delete(id)
        refresh()
    }

    fun deleteDream(id: String) = viewModelScope.launch {
        dreamRepository.delete(id)
        refresh()
    }

    /**
     * Notes and dreams belong to a session, as on iOS, and attach to the most
     * recently used one. Deliberately not read from ActiveSessionHolder: that is
     * a one-shot mailbox consumed by ChatViewModel, and reading it here would
     * swallow a pending "open this session" request.
     */
    private suspend fun resolveSessionId(): String? =
        sessionRepository.listSessions().maxByOrNull { it.updatedAt }?.id

    private fun fail(message: String) {
        _uiState.update { it.copy(message = message) }
    }
}
