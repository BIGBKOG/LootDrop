package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface GiveawayDao {

    @Query("SELECT * FROM giveaways ORDER BY id DESC")
    fun getAllGiveawaysFlow(): Flow<List<GiveawayEntity>>

    @Query("SELECT * FROM giveaways WHERE id = :id LIMIT 1")
    suspend fun getGiveawayById(id: Int): GiveawayEntity?

    @Query("SELECT * FROM giveaways WHERE isBookmarked = 1 ORDER BY id DESC")
    fun getBookmarkedGiveawaysFlow(): Flow<List<GiveawayEntity>>

    @Query("SELECT * FROM giveaways WHERE isClaimed = 1 ORDER BY id DESC")
    fun getClaimedGiveawaysFlow(): Flow<List<GiveawayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(giveaways: List<GiveawayEntity>)

    @Query("UPDATE giveaways SET isClaimed = :claimed WHERE id = :id")
    suspend fun updateClaimed(id: Int, claimed: Boolean)

    @Query("UPDATE giveaways SET isBookmarked = :bookmarked WHERE id = :id")
    suspend fun updateBookmarked(id: Int, bookmarked: Boolean)

    suspend fun setBookmarked(id: Int, bookmarked: Boolean) = updateBookmarked(id, bookmarked)

    @Query("SELECT * FROM giveaways WHERE isBookmarked = 1")
    suspend fun getBookmarkedGiveawaysSync(): List<GiveawayEntity>

    @Query("UPDATE giveaways SET notified = 1 WHERE id = :id")
    suspend fun markNotified(id: Int)

    @Query("SELECT id FROM giveaways")
    suspend fun getAllExistingIds(): List<Int>

    suspend fun getAllGiveawayIds(): List<Int> = getAllExistingIds()

    @Query("UPDATE giveaways SET isClaimed = 0")
    suspend fun clearAllClaimed()

    @Query("UPDATE giveaways SET isBookmarked = 0")
    suspend fun clearAllBookmarks()

    @Query("DELETE FROM giveaways")
    suspend fun clearAll()

    @Query("SELECT * FROM giveaways")
    suspend fun getAllGiveawaysList(): List<GiveawayEntity>

    @Query("DELETE FROM giveaways WHERE isClaimed = 0 AND isBookmarked = 0 AND id NOT IN (:activeIds)")
    suspend fun pruneInactiveGiveaways(activeIds: List<Int>): Int

    @Query("DELETE FROM giveaways WHERE isClaimed = 0 AND isBookmarked = 0 AND (status = 'Ended' OR status = 'expired' OR (endDate != 'N/A' AND endDate != '' AND endDate < :currentDateIso))")
    suspend fun pruneExpiredGiveaways(currentDateIso: String): Int

    @Query("SELECT COUNT(*) FROM giveaways")
    suspend fun getGiveawaysCount(): Int

    @Query("UPDATE giveaways SET playStatus = :status WHERE id = :id")
    suspend fun updatePlayStatus(id: Int, status: com.example.data.model.PlayStatus)

    @Query("UPDATE giveaways SET playStatus = :status, steamAppId = :steamAppId, steamPlaytimeMinutes = :playtimeMinutes, steamAchievementsUnlocked = :achievementsUnlocked, steamAchievementsTotal = :achievementsTotal, steamLastPlayedTimestamp = :lastPlayed WHERE id = :id")
    suspend fun updateSteamTelemetry(id: Int, status: com.example.data.model.PlayStatus, steamAppId: Int?, playtimeMinutes: Int, achievementsUnlocked: Int, achievementsTotal: Int, lastPlayed: Long)

    @Query("UPDATE giveaways SET isWishlisted = :wishlisted WHERE id = :id")
    suspend fun updateWishlisted(id: Int, wishlisted: Boolean)

    @Query("UPDATE giveaways SET isWishlisted = 1 WHERE steamAppId IN (:appIds)")
    suspend fun markWishlistedBySteamAppIds(appIds: List<Int>)

    @Query("UPDATE giveaways SET isWishlisted = 0")
    suspend fun clearAllWishlisted()

    @Query("UPDATE giveaways SET manualInstallSizeGb = :sizeGb WHERE id = :id")
    suspend fun updateManualInstallSize(id: Int, sizeGb: Float?)

    @Transaction
    suspend fun syncGiveawaysPreservingStatus(newItems: List<GiveawayEntity>): List<GiveawayEntity> {
        val freshlyDiscovered = mutableListOf<GiveawayEntity>()
        val existingItems = getAllGiveawaysList()
        val existingMap = existingItems.associateBy { it.id }
        val toInsert = ArrayList<GiveawayEntity>(newItems.size)

        for (item in newItems) {
            val existing = existingMap[item.id]
            if (existing == null) {
                // Completely new giveaway!
                freshlyDiscovered.add(item)
                toInsert.add(item)
            } else {
                // Preserve user bookmarks, claimed status, playStatus, Steam telemetry, Wishlist, and manual storage
                val updated = item.copy(
                    isClaimed = existing.isClaimed,
                    isBookmarked = existing.isBookmarked,
                    firstSeenTimestamp = existing.firstSeenTimestamp,
                    notified = existing.notified,
                    playStatus = existing.playStatus,
                    steamAppId = existing.steamAppId,
                    steamPlaytimeMinutes = existing.steamPlaytimeMinutes,
                    steamAchievementsUnlocked = existing.steamAchievementsUnlocked,
                    steamAchievementsTotal = existing.steamAchievementsTotal,
                    steamLastPlayedTimestamp = existing.steamLastPlayedTimestamp,
                    isWishlisted = existing.isWishlisted,
                    manualInstallSizeGb = existing.manualInstallSizeGb
                )
                toInsert.add(updated)
            }
        }
        if (toInsert.isNotEmpty()) {
            insertAll(toInsert)
        }

        // Prune obsolete giveaways that ended and are not saved by the user
        if (newItems.isNotEmpty()) {
            val activeIds = newItems.map { it.id }
            pruneInactiveGiveaways(activeIds)
        }

        return freshlyDiscovered
    }
}
