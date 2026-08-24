package com.theraipist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.theraipist.data.local.entity.SessionEntity

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getById(id: String): SessionEntity?

    @Query("SELECT * FROM sessions WHERE isArchived = 0 ORDER BY updatedAt DESC")
    suspend fun getAll(): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE isArchived = 1 ORDER BY updatedAt DESC")
    suspend fun getArchived(): List<SessionEntity>

    @Query("UPDATE sessions SET isArchived = :archived, updatedAt = :ts WHERE id = :id")
    suspend fun setArchived(id: String, archived: Boolean, ts: Long)

    @Query("UPDATE sessions SET updatedAt = :ts WHERE id = :id")
    suspend fun touch(id: String, ts: Long)

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteById(id: String)
}
