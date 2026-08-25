package com.selfward.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.selfward.data.local.entity.MessageEntity

/**
 * The counts below are read as aggregates rather than by loading rows. A
 * dashboard over a year of sessions would otherwise pull every message body in
 * the app into memory only to count them.
 */
data class MessageCountRow(val sessionId: String, val total: Int, val fromYou: Int)

data class ModalityCountRow(val modality: String, val total: Int)

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    suspend fun getBySession(sessionId: String): List<MessageEntity>

    @Query("DELETE FROM messages WHERE sessionId = :sessionId")
    suspend fun deleteBySession(sessionId: String)

    /**
     * System messages are excluded: the prompt is not something the person
     * said, and counting it would inflate every session by the same amount.
     */
    @Query(
        """
        SELECT sessionId,
               COUNT(*) AS total,
               SUM(CASE WHEN role = 'USER' THEN 1 ELSE 0 END) AS fromYou
        FROM messages
        WHERE role != 'SYSTEM'
        GROUP BY sessionId
        """
    )
    suspend fun countsBySession(): List<MessageCountRow>

    @Query(
        """
        SELECT modality, COUNT(*) AS total
        FROM messages
        WHERE modality IS NOT NULL AND role != 'SYSTEM'
        GROUP BY modality
        """
    )
    suspend fun countsByModality(): List<ModalityCountRow>
}
