package com.theraipist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.theraipist.data.local.entity.NarrativeEntity

@Dao
interface NarrativeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(narrative: NarrativeEntity)

    @Query("SELECT * FROM narrative WHERE id = :id")
    suspend fun get(id: String = NarrativeEntity.SINGLETON_ID): NarrativeEntity?

    @Query("DELETE FROM narrative")
    suspend fun clear()
}
