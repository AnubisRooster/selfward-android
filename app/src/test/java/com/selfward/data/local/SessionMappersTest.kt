package com.selfward.data.local

import com.selfward.config.CompanionGender
import com.selfward.config.CompanionPersonality
import com.selfward.config.PersonaKind
import com.selfward.config.SpiritualTradition
import com.selfward.core.graph.GraphEdge
import com.selfward.core.graph.GraphNode
import com.selfward.core.model.Message
import com.selfward.core.model.Persona
import com.selfward.core.model.Role
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

    @Test
    fun graphNodeRoundTripsWithSession() {
        val n = GraphNode(id = "n_1", label = "Anxiety", kind = "emotion", createdAt = 555)
        val e = n.toEntity("sessX")
        assertEquals("n_1", e.id)
        assertEquals("sessX", e.sessionId)
        assertEquals("Anxiety", e.label)
        assertEquals("emotion", e.kind)
        assertEquals(555, e.createdAt)
        assertEquals(n, e.toDomain())
    }

    @Test
    fun graphEdgeRoundTripsWithSession() {
        val edge = GraphEdge(id = "e_1", sourceId = "n_1", targetId = "n_2", label = "next", weight = 0.5f)
        val e = edge.toEntity("sessX")
        assertEquals("sessX", e.sessionId)
        assertEquals("n_1", e.sourceId)
        assertEquals("n_2", e.targetId)
        assertEquals(0.5f, e.weight)
        assertEquals(edge, e.toDomain())
    }
}
