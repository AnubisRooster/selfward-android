package com.selfward.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfward.core.GraphHolder
import com.selfward.core.dashboard.Dashboard
import com.selfward.core.dashboard.GlobalStats
import com.selfward.core.dashboard.StatsRepository
import com.selfward.core.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val stats: GlobalStats = GlobalStats(),
    val loading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val sessionRepository: SessionRepository,
    private val graphHolder: GraphHolder
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    /**
     * Recomputed on every visit, because the numbers move while the person is
     * on another tab and this ViewModel outlives the navigation.
     *
     * Archived sessions are counted. Setting a session aside is not the same as
     * saying it never happened, and a total that dropped when someone tidied up
     * would be telling them they had done less than they had.
     */
    fun refresh() {
        viewModelScope.launch {
            graphHolder.ensureLoaded()
            val sessions = runCatching {
                sessionRepository.listSessions() + sessionRepository.listArchivedSessions()
            }.getOrDefault(emptyList())

            val stats = runCatching {
                Dashboard.global(
                    sessions = sessions,
                    messages = statsRepository.messageTallies(),
                    modalities = statsRepository.modalityTallies(),
                    nodes = graphHolder.nodes.value,
                    notes = statsRepository.noteTallies(),
                    dreams = statsRepository.dreamTallies()
                )
            }.getOrDefault(GlobalStats())

            _uiState.value = DashboardUiState(stats = stats, loading = false)
        }
    }
}
