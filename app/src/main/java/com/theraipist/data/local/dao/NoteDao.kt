package com.theraipist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.theraipist.data.local.entity.NoteEntity

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    @Query("SELECT * FROM notes WHERE sessionId = :sessionId ORDER BY createdAt DESC")
    suspend fun getBySession(sessionId: String): List<NoteEntity>

    @Query("SELECT * FROM notes ORDER BY createdAt ASC")
    suspend fun getAll(): List<NoteEntity>

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM notes WHERE sessionId = :sessionId")
    suspend fun deleteBySession(sessionId: String)
}
