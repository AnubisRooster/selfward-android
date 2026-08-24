package com.selfward.core.prompt

import com.selfward.config.CompanionGender
import com.selfward.config.CompanionPersonality
import com.selfward.config.PersonaKind
import com.selfward.config.SpiritualTradition
import com.selfward.core.model.Message
import com.selfward.core.model.Persona
import com.selfward.core.model.Role
import org.junit.Assert.*
import org.junit.Test

class TherapyPromptBuilderTest {

    @Test
    fun therapistSystemPromptMentionsBrand() {
        val p = Persona(kind = PersonaKind.THERAPIST)
        assertTrue(TherapyPromptBuilder.systemPrompt(p).contains("Selfward"))
    }

    @Test
    fun therapistPromptIncludesModality() {
        val p = Persona(kind = PersonaKind.THERAPIST)
        val prompt = TherapyPromptBuilder.systemPrompt(p, "jungian")
        assertTrue(prompt.contains("Jungian analyst"))
    }

    @Test
    fun companionPromptFillsNameAndTraits() {
        val p = Persona(
            kind = PersonaKind.COMPANION,
            name = "Luna",
            companionGender = CompanionGender.FEMININE,
            companionPersonality = CompanionPersonality.WARM
        )
        val prompt = TherapyPromptBuilder.systemPrompt(p)
        assertTrue(prompt.contains("Luna"))
        assertTrue(prompt.contains(CompanionPersonality.WARM.promptLine))
    }

    @Test
    fun spiritualPromptFillsTradition() {
        val p = Persona(
            kind = PersonaKind.SPIRITUAL,
            name = "Sage",
            spiritualTradition = SpiritualTradition.BUDDHIST
        )
        val prompt = TherapyPromptBuilder.systemPrompt(p)
        assertTrue(prompt.contains("Sage"))
        assertTrue(prompt.contains(SpiritualTradition.BUDDHIST.promptLine))
    }

    @Test
    fun buildConversationOrdersMessages() {
        val p = Persona(kind = PersonaKind.THERAPIST)
        val history = listOf(
            Message(id = "1", role = Role.USER, content = "hello"),
            Message(id = "2", role = Role.ASSISTANT, content = "hi there")
        )
        val convo = TherapyPromptBuilder.buildConversation(p, "dbt", history, "how are you")
        assertEquals(Role.SYSTEM, convo.first().role)
        assertEquals(Role.USER, convo.last().role)
        assertEquals("how are you", convo.last().content)
        assertEquals(4, convo.size)
        assertEquals("hello", convo[1].content)
        assertEquals("hi there", convo[2].content)
    }
}
