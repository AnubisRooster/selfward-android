package com.theraipist.data.narrative

import com.theraipist.core.narrative.NarrativeDocument
import com.theraipist.core.narrative.NarrativeStore
import com.theraipist.data.local.TherAIpistDatabase
import com.theraipist.data.local.entity.NarrativeEntity

class RoomNarrativeStore(private val db: TherAIpistDatabase) : NarrativeStore {

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
