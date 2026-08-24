package com.selfward.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The single narrative document. The primary key is fixed so there can only ever
 * be one row: the narrative is revised in place, never accumulated.
 */
@Entity(tableName = "narrative")
data class NarrativeEntity(
    @PrimaryKey val id: String = SINGLETON_ID,
    val content: String,
    val sessionCount: Int,
    val sourceWatermark: Long,
    val updatedAt: Long
) {
    companion object {
        const val SINGLETON_ID = "narrative"
    }
}
