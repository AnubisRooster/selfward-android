package com.selfward.data.narrative

import com.selfward.core.narrative.NarrativeDocument
import com.selfward.core.narrative.NarrativeStore
import com.selfward.data.local.SelfwardDatabase
import com.selfward.data.local.entity.NarrativeEntity

class RoomNarrativeStore(private val db: SelfwardDatabase) : NarrativeStore {

    override suspend fun load(): NarrativeDocument? =
        db.narrativeDao().get()?.let {
            NarrativeDocument(
                content = it.content,
                sessionCount = it.sessionCount,
                sourceWatermark = it.sourceWatermark,
                updatedAt = it.updatedAt
            )
        }

    override suspend fun save(document: NarrativeDocument) {
        db.narrativeDao().upsert(
            NarrativeEntity(
                content = document.content,
                sessionCount = document.sessionCount,
                sourceWatermark = document.sourceWatermark,
                updatedAt = document.updatedAt
            )
        )
    }

    override suspend fun clear() = db.narrativeDao().clear()
}
