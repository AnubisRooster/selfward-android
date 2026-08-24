package com.selfward.ui.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfward.core.ActiveSessionHolder
import com.selfward.core.repository.SessionRepository
import com.selfward.core.repository.SessionSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val activeSessionHolder: ActiveSessionHolder
) : ViewModel() {

    private val _sessions = MutableStateFlow<List<SessionSummary>>(emptyList())
    val sessions = _sessions.asStateFlow()

    private val _archived = MutableStateFlow<List<SessionSummary>>(emptyList())
    val archived = _archived.asStateFlow()

    private val _showingArchive = MutableStateFlow(false)
    val showingArchive = _showingArchive.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _sessions.value = sessionRepository.listSessions()
            _archived.value = sessionRepository.listArchivedSessions()
        }
    }

    /** Records which session Chat should resume; the caller is responsible for navigating there. */
    fun openSession(sessionId: String) {
        activeSessionHolder.open(sessionId)
    }

    fun showArchive(showing: Boolean) {
        _showingArchive.value = showing
    }

    /**
     * Archiving is the everyday action; it hides the session without touching
     * its transcript, memories or graph, and can be undone from the archive.
     */
    fun archiveSession(sessionId: String) = setArchived(sessionId, true)

    fun restoreSession(sessionId: String) = setArchived(sessionId, false)

    private fun setArchived(sessionId: String, archived: Boolean) {
        viewModelScope.launch {
            sessionRepository.setArchived(sessionId, archived)
            refresh()
        }
    }

    /** Irreversible, and only offered from the archive. */
    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            sessionRepository.deleteSession(sessionId)
            refresh()
        }
    }
}
