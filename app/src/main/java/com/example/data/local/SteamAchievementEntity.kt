package com.example.data.local

import androidx.compose.runtime.Immutable
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.TrophyRarityTier
import kotlinx.coroutines.flow.Flow
import java.util.Locale

@Immutable
@Entity(
    tableName = "steam_achievements",
    primaryKeys = ["appId", "apiName"]
)
data class SteamAchievementEntity(
    val appId: Int,
    val apiName: String,
    val displayName: String,
    val description: String,
    val isAchieved: Boolean,
    val unlockTimestamp: Long = 0L,
    val globalPercentage: Float = 0f,
    val iconUrl: String = "",
    val gameName: String = ""
) {
    val rarityTier: TrophyRarityTier
        get() = when {
            globalPercentage <= 0f -> TrophyRarityTier.COMMON
            globalPercentage < 5.0f -> TrophyRarityTier.MYTHIC
            globalPercentage < 15.0f -> TrophyRarityTier.GOLD
            globalPercentage < 35.0f -> TrophyRarityTier.SILVER
            else -> TrophyRarityTier.BRONZE
        }

    val formattedRarity: String
        get() = if (globalPercentage > 0f) String.format(Locale.US, "%.1f%%", globalPercentage) else "N/A"
}

@Dao
interface SteamAchievementDao {
    @Query("SELECT * FROM steam_achievements WHERE isAchieved = 1 ORDER BY globalPercentage ASC, unlockTimestamp DESC")
    fun getUnlockedRarestFlow(): Flow<List<SteamAchievementEntity>>

    @Query("SELECT * FROM steam_achievements WHERE isAchieved = 1 ORDER BY globalPercentage ASC, unlockTimestamp DESC LIMIT :limit")
    suspend fun getRarestTrophies(limit: Int = 50): List<SteamAchievementEntity>

    @Query("SELECT * FROM steam_achievements WHERE appId = :appId ORDER BY isAchieved DESC, globalPercentage ASC")
    fun getAchievementsForAppFlow(appId: Int): Flow<List<SteamAchievementEntity>>

    @Query("SELECT * FROM steam_achievements WHERE appId = :appId ORDER BY isAchieved DESC, globalPercentage ASC")
    suspend fun getAchievementsForApp(appId: Int): List<SteamAchievementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(achievements: List<SteamAchievementEntity>)

    @Query("SELECT COUNT(*) FROM steam_achievements WHERE isAchieved = 1")
    fun getUnlockedCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM steam_achievements WHERE isAchieved = 1 AND globalPercentage > 0 AND globalPercentage < 5.0")
    fun getMythicCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM steam_achievements")
    suspend fun getTotalCount(): Int

    @Query("DELETE FROM steam_achievements")
    suspend fun clearAll()

    @Query("DELETE FROM steam_achievements WHERE appId IN (:appIds)")
    suspend fun deleteByAppIds(appIds: List<Int>)
}
