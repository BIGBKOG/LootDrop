package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OstCacheDao {
    @Query("SELECT * FROM ost_cache WHERE gameKey = :gameKey LIMIT 1")
    suspend fun getByKey(gameKey: String): OstCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cache: OstCacheEntity)

    @Query("SELECT COUNT(*) FROM ost_cache")
    suspend fun count(): Int

    @Query("DELETE FROM ost_cache WHERE cachedTimestamp < :threshold")
    suspend fun deleteExpired(threshold: Long)

    @Query("DELETE FROM ost_cache")
    suspend fun clearAll()
}
