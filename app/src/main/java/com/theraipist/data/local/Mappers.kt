package com.theraipist.data.local

import com.theraipist.config.CompanionGender
import com.theraipist.config.CompanionPersonality
import com.theraipist.config.PersonaKind
import com.theraipist.config.SpiritualTradition
import com.theraipist.core.graph.GraphEdge
import com.theraipist.core.graph.GraphNode
import com.theraipist.core.model.Message
import com.theraipist.core.model.Persona
import com.theraipist.core.model.Role
import com.theraipist.core.repository.Session
import com.theraipist.data.local.entity.GraphEdgeEntity
import com.theraipist.data.local.entity.GraphNodeEntity
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

internal fun GraphNode.toEntity(sessionId: String): GraphNodeEntity = GraphNodeEntity(
    id = id,
    sessionId = sessionId,
    label = label,
    kind = kind,
    createdAt = createdAt
)

internal fun GraphNodeEntity.toDomain(): GraphNode = GraphNode(
    id = id,
    label = label,
    kind = kind,
    createdAt = createdAt
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
