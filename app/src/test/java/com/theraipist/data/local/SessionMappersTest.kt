package com.theraipist.data.local

import com.theraipist.config.CompanionGender
import com.theraipist.config.CompanionPersonality
import com.theraipist.config.PersonaKind
import com.theraipist.config.SpiritualTradition
import com.theraipist.core.model.Message
import com.theraipist.core.model.Persona
import com.theraipist.core.model.Role
import org.junit.Assert.*
import org.junit.Test

class SessionMappersTest {

    @Test
    fun therapistPersonaRoundTripsThroughEntity() {
        val p = Persona(kind = PersonaKind.THERAPIST)
        val e = p.toSessionEntity("id1", "T", 100, 200)
        assertEquals("THERAPIST", e.personaKind)
        val back = e.toDomain()
        assertEquals(PersonaKind.THERAPIST, back.persona.kind)
        assertEquals("id1", back.id)
        assertEquals("T", back.title)
        assertEquals(100, back.createdAt)
        assertEquals(200, back.updatedAt)
    }

    @Test
    fun companionPersonaRoundTripsAllFields() {
        val p = Persona(
            kind = PersonaKind.COMPANION,
            name = "Luna",
            companionGender = CompanionGender.FEMININE,
            companionPersonality = CompanionPersonality.BOLD
        )
        val e = p.toSessionEntity("id2", "C", 1, 2)
        assertEquals("Luna", e.name)
        assertEquals("FEMININE", e.companionGender)
        assertEquals("BOLD", e.companionPersonality)
        val back = e.toDomain()
        assertEquals("Luna", back.persona.name)
        assertEquals(CompanionGender.FEMININE, back.persona.companionGender)
        assertEquals(CompanionPersonality.BOLD, back.persona.companionPersonality)
    }

    @Test
    fun spiritualPersonaRoundTripsTradition() {
        val p = Persona(
            kind = PersonaKind.SPIRITUAL,
            name = "Sage",
            spiritualTradition = SpiritualTradition.BUDDHIST
        )
        val e = p.toSessionEntity("id3", "S", 1, 2)
        assertEquals("BUDDHIST", e.spiritualTradition)
        val back = e.toDomain()
        assertEquals(SpiritualTradition.BUDDHIST, back.persona.spiritualTradition)
    }

    @Test
    fun messageRoundTripsWithModality() {
        val m = Message("m1", Role.ASSISTANT, "hello", 123, "jungian")
        val e = m.toEntity("sessX")
        assertEquals("m1", e.id)
        assertEquals("sessX", e.sessionId)
        assertEquals("ASSISTANT", e.role)
        assertEquals("jungian", e.modality)
        val back = e.toDomain()
        assertEquals(Role.ASSISTANT, back.role)
        assertEquals("hello", back.content)
        assertEquals("jungian", back.modality)
        assertEquals(123, back.createdAt)
    }
}
