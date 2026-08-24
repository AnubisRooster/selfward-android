package com.theraipist.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Adds the notes and dreams tables.
 *
 * Written out rather than falling back to a destructive migration: sessions and
 * their transcripts are the one thing in this app that cannot be regenerated,
 * and dropping them to add two empty tables would be a poor trade.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `notes` (
                `id` TEXT NOT NULL,
                `sessionId` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `content` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_sessionId` ON `notes` (`sessionId`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `dreams` (
                `id` TEXT NOT NULL,
                `sessionId` TEXT NOT NULL,
                `narrative` TEXT NOT NULL,
                `feelings` TEXT NOT NULL,
                `symbols` TEXT NOT NULL,
                `analysis` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_dreams_sessionId` ON `dreams` (`sessionId`)")
    }
}

/** Adds the single-row narrative document table. */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `narrative` (
                `id` TEXT NOT NULL,
                `content` TEXT NOT NULL,
                `sessionCount` INTEGER NOT NULL,
                `sourceWatermark` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
    }
}

/**
 * Adds archiving. Existing sessions default to not archived, so an upgrade
 * leaves every conversation exactly where its owner left it.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `sessions` ADD COLUMN `isArchived` INTEGER NOT NULL DEFAULT 0")
    }
}
