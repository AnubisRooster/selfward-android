package com.theraipist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.theraipist.data.local.entity.DreamEntity

@Dao
interface DreamDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dream: DreamEntity)

    @Query("UPDATE dreams SET analysis = :analysis WHERE id = :id")
    suspend fun setAnalysis(id: String, analysis: String)

    @Query("SELECT * FROM dreams WHERE sessionId = :sessionId ORDER BY createdAt DESC")
    suspend fun getBySession(sessionId: String): List<DreamEntity>

    @Query("SELECT * FROM dreams ORDER BY createdAt ASC")
    suspend fun getAll(): List<DreamEntity>

    @Query("DELETE FROM dreams WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM dreams WHERE sessionId = :sessionId")
    suspend fun deleteBySession(sessionId: String)
}
