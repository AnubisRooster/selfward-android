package com.selfward.core

import com.selfward.config.PersonaKind
import com.selfward.core.model.Persona
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonaHolder @Inject constructor() {
    private val _persona = MutableStateFlow(Persona(PersonaKind.THERAPIST))
    val persona = _persona.asStateFlow()

    fun update(block: (Persona) -> Persona) {
        _persona.value = block(_persona.value)
    }
}
