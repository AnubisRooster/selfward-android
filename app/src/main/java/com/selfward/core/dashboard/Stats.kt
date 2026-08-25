package com.selfward.core.dashboard

/** How many messages a session holds, and how many of them the person wrote. */
data class MessageTally(val sessionId: String, val total: Int, val fromYou: Int)

/** A count of something belonging to one session. */
data class Tally(val sessionId: String, val count: Int)

/**
 * Counts read straight from the database.
 *
 * These are counts rather than rows on purpose. A dashboard over a year of
 * sessions would otherwise load every message body in the app to find out how
 * many there were, and then throw all of them away.
 */
interface StatsRepository {
    suspend fun messageTallies(): List<MessageTally>

    /** How many messages were sent under each therapy modality. */
    suspend fun modalityTallies(): Map<String, Int>

    suspend fun nodeTallies(): List<Tally>
    suspend fun noteTallies(): List<Tally>
    suspend fun dreamTallies(): List<Tally>
}
