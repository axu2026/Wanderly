package com.example.wanderly.local

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            )
                .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()

            INSTANCE = instance
            instance
        }
    }

    // v3 → v4: add users table for local auth. Preserves existing places/saved_trips data.
    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    username TEXT NOT NULL,
                    displayName TEXT NOT NULL,
                    passwordHash TEXT NOT NULL,
                    passwordSalt TEXT NOT NULL,
                    createdAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_users_username ON users(username)")
        }
    }

    // v4 → v5: scope saved_trips per user. Pre-auth rows are claimed by the
    // first registered user so existing trips don't vanish. If no user has
    // signed up yet (rare — fresh install with no trips), rows fall back to
    // userId=0 and stay invisible until claimed.
    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE saved_trips ADD COLUMN userId INTEGER NOT NULL DEFAULT 0")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_saved_trips_userId ON saved_trips(userId)")
            db.execSQL(
                "UPDATE saved_trips SET userId = " +
                    "COALESCE((SELECT id FROM users ORDER BY id LIMIT 1), 0) " +
                    "WHERE userId = 0"
            )
        }
    }
}
