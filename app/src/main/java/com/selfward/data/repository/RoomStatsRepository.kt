package com.selfward.data.repository

import com.selfward.core.dashboard.MessageTally
import com.selfward.core.dashboard.StatsRepository
import com.selfward.core.dashboard.Tally
import com.selfward.data.local.SelfwardDatabase
import com.selfward.data.local.dao.SessionCountRow

/**
 * Counts read as aggregates, one query per kind, rather than by loading rows
 * and counting them here.
 */
class RoomStatsRepository(private val db: SelfwardDatabase) : StatsRepository {

    override suspend fun messageTallies(): List<MessageTally> =
        db.messageDao().countsBySession().map { MessageTally(it.sessionId, it.total, it.fromYou) }

    override suspend fun modalityTallies(): Map<String, Int> =
        db.messageDao().countsByModality().associate { it.modality to it.total }

    override suspend fun nodeTallies(): List<Tally> = db.graphDao().countsBySession().toTallies()

    override suspend fun noteTallies(): List<Tally> = db.noteDao().countsBySession().toTallies()

    override suspend fun dreamTallies(): List<Tally> = db.dreamDao().countsBySession().toTallies()

    private fun List<SessionCountRow>.toTallies() = map { Tally(it.sessionId, it.total) }
}
