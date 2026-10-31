package com.example.data.local

import androidx.compose.runtime.Immutable
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.example.data.model.PlayStatus
import kotlinx.coroutines.flow.Flow

@Immutable
@Entity(tableName = "steam_games")
data class SteamGameEntity(
    @PrimaryKey val appId: Int,
    val name: String,
    val playtimeMinutes: Int,
    val playtime2WeeksMinutes: Int = 0,
    val lastPlayedTimestamp: Long = 0L,
    val playStatus: PlayStatus = PlayStatus.NOT_STARTED,
    val achievementsUnlocked: Int = 0,
    val achievementsTotal: Int = 0,
    val imgIconUrl: String = "",
    val headerImageUrl: String = "https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/$appId/header.jpg",
    val matchedGiveawayId: Int? = null,
    val isLootDropClaimed: Boolean = false,
    val manualInstallSizeGb: Float? = null
) {
    val hardwareProfile: com.example.data.model.GameHardwareProfile
        get() = com.example.util.HardwareReadinessCatalog.resolveProfile(this)

    val playtimeHours: String
        get() {
            val hours = playtimeMinutes / 60.0
            return if (hours >= 10.0) String.format(java.util.Locale.US, "%.0fh", hours)
            else String.format(java.util.Locale.US, "%.1fh", hours)
        }

    val achievementsPercentage: Int
        get() = if (achievementsTotal > 0) ((achievementsUnlocked.toDouble() / achievementsTotal.toDouble()) * 100).toInt() else 0

    val storeUrl: String
        get() = "https://store.steampowered.com/app/$appId"

    val launchUri: String
        get() = "steam://run/$appId"
}

@Dao
interface SteamGameDao {
    @Query("SELECT * FROM steam_games ORDER BY lastPlayedTimestamp DESC, playtimeMinutes DESC")
    fun getAllSteamGamesFlow(): Flow<List<SteamGameEntity>>

    @Query("SELECT * FROM steam_games WHERE appId = :appId LIMIT 1")
    suspend fun getSteamGameById(appId: Int): SteamGameEntity?

    @Query("SELECT * FROM steam_games WHERE playStatus = :status ORDER BY lastPlayedTimestamp DESC")
    fun getSteamGamesByStatusFlow(status: PlayStatus): Flow<List<SteamGameEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(games: List<SteamGameEntity>)

    @Query("UPDATE steam_games SET playStatus = :status WHERE appId = :appId")
    suspend fun updatePlayStatus(appId: Int, status: PlayStatus)

    @Query("UPDATE steam_games SET manualInstallSizeGb = :sizeGb WHERE appId = :appId")
    suspend fun updateManualInstallSize(appId: Int, sizeGb: Float?)

    @Query("UPDATE steam_games SET playStatus = :status, playtimeMinutes = :playtime, achievementsUnlocked = :unlocked, achievementsTotal = :total, lastPlayedTimestamp = :lastPlayed WHERE appId = :appId")
    suspend fun updateTelemetry(appId: Int, status: PlayStatus, playtime: Int, unlocked: Int, total: Int, lastPlayed: Long)

    @Query("DELETE FROM steam_games")
    suspend fun clearAll()

    @Query("DELETE FROM steam_games WHERE appId IN (:appIds)")
    suspend fun deleteByAppIds(appIds: List<Int>)

    @Query("SELECT COUNT(*) FROM steam_games")
    suspend fun getCount(): Int

    @Query("SELECT * FROM steam_games")
    suspend fun getAllSteamGamesList(): List<SteamGameEntity>
}
