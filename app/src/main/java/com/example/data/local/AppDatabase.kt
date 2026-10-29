package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [GiveawayEntity::class, NotificationHistoryEntity::class, SteamGameEntity::class], version = 6, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun giveawayDao(): GiveawayDao
    abstract fun notificationHistoryDao(): NotificationHistoryDao
    abstract fun steamGameDao(): SteamGameDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lootdrop_games.db"
                ).fallbackToDestructiveMigration()
                    .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            // Speed up transactions with synchronous NORMAL, in-memory temp tables, and 2MB cache
                            db.execSQL("PRAGMA synchronous = NORMAL")
                            db.execSQL("PRAGMA temp_store = MEMORY")
                            db.execSQL("PRAGMA cache_size = -2000")
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }

        fun vacuumAndCompact(context: Context) {
            runCatching {
                val db = getInstance(context)
                val writableDb = db.openHelper.writableDatabase
                // Truncate write-ahead log to reclaim space immediately
                writableDb.execSQL("PRAGMA wal_checkpoint(TRUNCATE)")
                // Reclaim deleted table pages and defragment database file on disk
                writableDb.execSQL("VACUUM")
            }
        }
    }
}
