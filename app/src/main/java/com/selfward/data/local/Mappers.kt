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
import com.selfward.core.repository.Session
import com.selfward.data.local.entity.GraphEdgeEntity
import com.selfward.data.local.entity.GraphNodeEntity
import com.selfward.data.local.entity.MessageEntity
import com.selfward.data.local.entity.SessionEntity

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

internal fun GraphNode.toEntity(sessionId: String): GraphNodeEntity = GraphNodeEntity(
    id = id,
    sessionId = sessionId,
    label = label,
    kind = kind,
    createdAt = createdAt,
    strength = strength
)

internal fun GraphNodeEntity.toDomain(): GraphNode = GraphNode(
    id = id,
    label = label,
    kind = kind,
    createdAt = createdAt,
    strength = strength
)

internal fun GraphEdge.toEntity(sessionId: String): GraphEdgeEntity = GraphEdgeEntity(
    id = id,
    sessionId = sessionId,
    sourceId = sourceId,
    targetId = targetId,
    label = label,
    weight = weight
)

internal fun GraphEdgeEntity.toDomain(): GraphEdge = GraphEdge(
    id = id,
    sourceId = sourceId,
    targetId = targetId,
    label = label,
    weight = weight
)
