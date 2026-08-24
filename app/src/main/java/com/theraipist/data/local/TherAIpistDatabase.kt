package com.theraipist.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.theraipist.data.local.dao.DreamDao
import com.theraipist.data.local.dao.GraphDao
import com.theraipist.data.local.dao.InsightDao
import com.theraipist.data.local.dao.MessageDao
import com.theraipist.data.local.dao.NarrativeDao
import com.theraipist.data.local.dao.NoteDao
import com.theraipist.data.local.dao.SessionDao
import com.theraipist.data.local.entity.DreamEntity
import com.theraipist.data.local.entity.GraphEdgeEntity
import com.theraipist.data.local.entity.GraphNodeEntity
import com.theraipist.data.local.entity.InsightEntity
import com.theraipist.data.local.entity.MessageEntity
import com.theraipist.data.local.entity.NarrativeEntity
import com.theraipist.data.local.entity.NoteEntity
import com.theraipist.data.local.entity.SessionEntity

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
    version = 4,
    exportSchema = true
)
abstract class TherAIpistDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun messageDao(): MessageDao
    abstract fun insightDao(): InsightDao
    abstract fun graphDao(): GraphDao
    abstract fun noteDao(): NoteDao
    abstract fun dreamDao(): DreamDao
    abstract fun narrativeDao(): NarrativeDao
}
