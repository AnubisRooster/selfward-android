package com.theraipist.ui.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theraipist.core.ActiveSessionHolder
import com.theraipist.core.repository.SessionRepository
import com.theraipist.core.repository.SessionSummary
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

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _sessions.value = sessionRepository.listSessions()
        }
    }

    /** Records which session Chat should resume; the caller is responsible for navigating there. */
    fun openSession(sessionId: String) {
        activeSessionHolder.open(sessionId)
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            sessionRepository.deleteSession(sessionId)
            refresh()
        }
    }
}
