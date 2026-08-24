package com.theraipist.ui.narrative

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theraipist.core.ModelSettings
import com.theraipist.core.chat.ChatService
import com.theraipist.core.chat.MissingApiKeyException
import com.theraipist.core.journal.DreamRepository
import com.theraipist.core.journal.NoteRepository
import com.theraipist.core.local.GGUFModelCatalog
import com.theraipist.core.local.LocalLLMService
import com.theraipist.core.model.Message
import com.theraipist.core.model.Role
import com.theraipist.core.narrative.NarrativeDocument
import com.theraipist.core.narrative.NarrativePrompt
import com.theraipist.core.narrative.NarrativeSource
import com.theraipist.core.narrative.NarrativeSources
import com.theraipist.core.narrative.NarrativeStore
import com.theraipist.core.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NarrativeUiState(
    val document: NarrativeDocument = NarrativeDocument(),
    val building: Boolean = false,
    val error: String? = null,
    val nothingNew: Boolean = false,
    /** True when replies come from the phone, which changes what regenerating sends. */
    val onDevice: Boolean = false
)

@HiltViewModel
class NarrativeViewModel @Inject constructor(
    private val narrativeStore: NarrativeStore,
    private val noteRepository: NoteRepository,
    private val dreamRepository: DreamRepository,
    private val sessionRepository: SessionRepository,
    private val chatService: ChatService,
    private val localLLMService: LocalLLMService,
    private val modelSettings: ModelSettings
) : ViewModel() {

    private val _uiState = MutableStateFlow(NarrativeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val stored = narrativeStore.load() ?: NarrativeDocument()
            _uiState.update { it.copy(document = stored, onDevice = usingOnDeviceModel()) }
        }
    }

    fun regenerate() {
        if (_uiState.value.building) return
        _uiState.update { it.copy(building = true, error = null, nothingNew = false) }

        viewModelScope.launch {
            try {
                build()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = when (e) {
                            is MissingApiKeyException ->
                                "Add an API key in Settings, or switch on an on-device model, to write your narrative."
                            else -> e.message ?: "Couldn't write the narrative just now."
                        }
                    )
                }
            } finally {
                _uiState.update { it.copy(building = false, onDevice = usingOnDeviceModel()) }
            }
        }
    }

    fun dismissError() = _uiState.update { it.copy(error = null, nothingNew = false) }

    private suspend fun build() {
        val existing = narrativeStore.load() ?: NarrativeDocument()
        val sources = gatherSources(existing.sourceWatermark)
        if (sources.isEmpty()) {
            _uiState.update { it.copy(nothingNew = true) }
            return
        }

        val prose = generate(
            system = NarrativePrompt.system(personaName()),
            user = NarrativePrompt.user(existing.content, sources)
        ).trim()

        if (prose.isBlank()) {
            _uiState.update { it.copy(error = "The model returned nothing. Please try again.") }
            return
        }

        val updated = NarrativeDocument(
            content = prose,
            sessionCount = sessionRepository.listSessions().size,
            // Advance only to the newest source actually woven in, so a later run
            // picks up exactly what came after and nothing is read twice.
            sourceWatermark = sources.maxOf { it.createdAt },
            updatedAt = System.currentTimeMillis()
        )
        narrativeStore.save(updated)
        _uiState.update { it.copy(document = updated) }
    }

    /**
     * Notes, dreams and extracted insights first; raw conversation only when
     * there is none of that, matching how iOS chooses its material.
     */
    private suspend fun gatherSources(watermark: Long): List<NarrativeSource> {
        val artifacts = buildList {
            noteRepository.listAll().forEach {
                val text = it.content.ifBlank { it.title }
                add(NarrativeSource(it.createdAt, "Note", text))
            }
            dreamRepository.listAll().forEach {
                val text = it.analysis.ifBlank { it.narrative }
                add(NarrativeSource(it.createdAt, "Dream", text))
            }
        }

        val turns = sessionRepository.listSessions().flatMap { summary ->
            sessionRepository.getMessages(summary.id)
                .filter { it.role != Role.SYSTEM }
                .map {
                    NarrativeSource(
                        createdAt = summary.updatedAt,
                        kind = if (it.role == Role.USER) "You said" else "Reflection",
                        text = it.content
                    )
                }
        }

        return NarrativeSources.select(artifacts, turns, watermark)
    }

    private suspend fun generate(system: String, user: String): String {
        val messages = listOf(
            Message(id = "narrative-system", role = Role.SYSTEM, content = system),
            Message(id = "narrative-user", role = Role.USER, content = user)
        )
        val flow = if (usingOnDeviceModel()) {
            localLLMService.stream(messages)
        } else {
            chatService.sendStreaming(messages)
        }
        return buildString { flow.collect { append(it) } }
    }

    private fun usingOnDeviceModel(): Boolean =
        modelSettings.useLocalModel.value &&
            modelSettings.localModelId.value?.let { GGUFModelCatalog.byId(it) } != null

    private suspend fun personaName(): String =
        sessionRepository.listSessions()
            .maxByOrNull { it.updatedAt }
            ?.let { sessionRepository.getSession(it.id)?.persona?.name }
            ?: "a compassionate therapist"
}
