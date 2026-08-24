package com.theraipist.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The notes/dreams migration is hand-written rather than destructive, because
 * sessions and their transcripts are the one thing in this app that cannot be
 * regenerated. These tests exist to prove an upgrade keeps them.
 *
 * A v1 database is built here with raw SQL rather than through
 * `MigrationTestHelper`: schema export was only switched on with this change, so
 * there is no exported v1 schema to hand it, and inventing one would test a
 * fabrication rather than what is actually on people's phones.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MigrationTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dbName = "migration-test.db"

    @After
    fun tearDown() {
        context.deleteDatabase(dbName)
    }

    /** The schema exactly as version 1 shipped, before notes and dreams existed. */
    private fun createVersion1Database() {
        val db = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null)
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `sessions` (`id` TEXT NOT NULL, `personaKind` TEXT NOT NULL, " +
                "`name` TEXT, `companionGender` TEXT, `companionPersonality` TEXT, " +
                "`spiritualTradition` TEXT, `title` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, " +
                "`updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `messages` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, " +
                "`role` TEXT NOT NULL, `content` TEXT NOT NULL, `modality` TEXT, " +
                "`createdAt` INTEGER NOT NULL, `turn` INTEGER, PRIMARY KEY(`id`))"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_sessionId` ON `messages` (`sessionId`)")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `insights` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, " +
                "`text` TEXT NOT NULL, `source` TEXT, `kind` TEXT, `createdAt` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`))"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_insights_sessionId` ON `insights` (`sessionId`)")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `graph_nodes` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, " +
                "`label` TEXT NOT NULL, `kind` TEXT, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_graph_nodes_sessionId` ON `graph_nodes` (`sessionId`)")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `graph_edges` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, " +
                "`sourceId` TEXT NOT NULL, `targetId` TEXT NOT NULL, `label` TEXT, `weight` REAL, " +
                "PRIMARY KEY(`id`))"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_graph_edges_sessionId` ON `graph_edges` (`sessionId`)")
        db.execSQL(
            "INSERT INTO sessions (id, personaKind, name, companionGender, companionPersonality, " +
                "spiritualTradition, title, createdAt, updatedAt) " +
                "VALUES ('s1', 'THERAPIST', NULL, NULL, NULL, NULL, 'A hard week', 100, 200)"
        )
        db.execSQL(
            "INSERT INTO messages (id, sessionId, role, content, modality, createdAt, turn) " +
                "VALUES ('m1', 's1', 'USER', 'something I would hate to lose', NULL, 150, NULL)"
        )
        db.version = 1
        db.close()
    }

    private fun openMigrated() = Room.databaseBuilder(context, TherAIpistDatabase::class.java, dbName)
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
        .build()

    @Test
    fun upgradingFromV1KeepsSessionsAndTranscripts() {
        createVersion1Database()

        val db = openMigrated()
        // Opening triggers the migration and Room's own schema validation; a
        // mismatch between the hand-written DDL and the entities throws here.
        val session = kotlinx.coroutines.runBlocking { db.sessionDao().getById("s1") }
        val messages = kotlinx.coroutines.runBlocking { db.messageDao().getBySession("s1") }
        db.close()

        assertEquals("A hard week", session?.title)
        assertEquals(1, messages.size)
        assertEquals("something I would hate to lose", messages.first().content)
    }

    @Test
    fun upgradingFromV1LeavesUsableNotesAndDreamsTables() {
        createVersion1Database()

        val db = openMigrated()
        val notesBefore = kotlinx.coroutines.runBlocking { db.noteDao().getAll() }
        val dreamsBefore = kotlinx.coroutines.runBlocking { db.dreamDao().getAll() }
        db.close()

        assertTrue("notes table should exist and start empty", notesBefore.isEmpty())
        assertTrue("dreams table should exist and start empty", dreamsBefore.isEmpty())
    }

    /** A v1 install must reach v3 in one go, not just v2. */
    @Test
    fun upgradingAllTheWayFromV1ReachesTheNarrativeTable() {
        createVersion1Database()

        val db = openMigrated()
        val narrative = kotlinx.coroutines.runBlocking { db.narrativeDao().get() }
        val session = kotlinx.coroutines.runBlocking { db.sessionDao().getById("s1") }
        db.close()

        assertEquals("the session must survive both migrations", "A hard week", session?.title)
        assertTrue("narrative starts empty", narrative == null)
    }

    @Test
    fun aFreshInstallHasTheSameTablesAsAnUpgradedOne() {
        val db = Room.inMemoryDatabaseBuilder(context, TherAIpistDatabase::class.java).build()

        val tables = db.openHelper.writableDatabase
            .query("SELECT name FROM sqlite_master WHERE type='table'")
            .use { c -> buildList { while (c.moveToNext()) add(c.getString(0)) } }
        db.close()

        assertTrue("notes missing from a fresh database", tables.contains("notes"))
        assertTrue("dreams missing from a fresh database", tables.contains("dreams"))
        assertTrue("narrative missing from a fresh database", tables.contains("narrative"))
    }
}
