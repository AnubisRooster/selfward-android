package com.theraipist.data.local

import com.theraipist.config.CompanionGender
import com.theraipist.config.CompanionPersonality
import com.theraipist.config.PersonaKind
import com.theraipist.config.SpiritualTradition
import com.theraipist.core.model.Message
import com.theraipist.core.model.Persona
import com.theraipist.core.model.Role
import com.theraipist.core.repository.Session
import com.theraipist.data.local.entity.MessageEntity
import com.theraipist.data.local.entity.SessionEntity

internal fun Persona.toSessionEntity(
    id: String,
    title: String,
    createdAt: Long,
    updatedAt: Long
): SessionEntity = SessionEntity(
    id = id,
    personaKind = kind.name,
    name = name,
    companionGender = companionGender.name,
    companionPersonality = companionPersonality.name,
    spiritualTradition = spiritualTradition.name,
    title = title,
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun SessionEntity.toDomain(): Session = Session(
    id = id,
    persona = Persona(
        kind = PersonaKind.valueOf(personaKind),
        name = name,
        companionGender = companionGender?.let { CompanionGender.valueOf(it) } ?: CompanionGender.UNSPECIFIED,
        companionPersonality = companionPersonality?.let { CompanionPersonality.valueOf(it) } ?: CompanionPersonality.WARM,
        spiritualTradition = spiritualTradition?.let { SpiritualTradition.valueOf(it) } ?: SpiritualTradition.INTERFAITH
    ),
    title = title,
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun Message.toEntity(sessionId: String, turn: Int? = null): MessageEntity = MessageEntity(
    id = id,
    sessionId = sessionId,
    role = role.name,
    content = content,
    modality = modality,
    createdAt = createdAt,
    turn = turn
)

internal fun MessageEntity.toDomain(): Message = Message(
    id = id,
    role = Role.valueOf(role),
    content = content,
    createdAt = createdAt,
    modality = modality
)
