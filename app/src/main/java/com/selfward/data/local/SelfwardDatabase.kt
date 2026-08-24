package com.selfward.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.selfward.data.local.dao.DreamDao
import com.selfward.data.local.dao.GraphDao
import com.selfward.data.local.dao.InsightDao
import com.selfward.data.local.dao.MessageDao
import com.selfward.data.local.dao.NarrativeDao
import com.selfward.data.local.dao.NoteDao
import com.selfward.data.local.dao.SessionDao
import com.selfward.data.local.entity.DreamEntity
import com.selfward.data.local.entity.GraphEdgeEntity
import com.selfward.data.local.entity.GraphNodeEntity
import com.selfward.data.local.entity.InsightEntity
import com.selfward.data.local.entity.MessageEntity
import com.selfward.data.local.entity.NarrativeEntity
import com.selfward.data.local.entity.NoteEntity
import com.selfward.data.local.entity.SessionEntity

@Database(
    entities = [
        SessionEntity::class,
        MessageEntity::class,
        InsightEntity::class,
        GraphNodeEntity::class,
        GraphEdgeEntity::class,
        NoteEntity::class,
        DreamEntity::class,
        NarrativeEntity::class
    ],
    version = 5,
    exportSchema = true
)
abstract class SelfwardDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun messageDao(): MessageDao
    abstract fun insightDao(): InsightDao
    abstract fun graphDao(): GraphDao
    abstract fun noteDao(): NoteDao
    abstract fun dreamDao(): DreamDao
    abstract fun narrativeDao(): NarrativeDao
}
