package com.example.screentimemanager.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val NoopMigration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Placeholder to illustrate where future schema changes will live.
    }
}
