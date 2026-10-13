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

    @Query("UPDATE giveaways SET notified = 1 WHERE id = :id")
    suspend fun markNotified(id: Int)

    @Query("SELECT id FROM giveaways")
    suspend fun getAllExistingIds(): List<Int>

    @Query("UPDATE giveaways SET isClaimed = 0")
    suspend fun clearAllClaimed()

    @Query("UPDATE giveaways SET isBookmarked = 0")
    suspend fun clearAllBookmarks()

    @Query("DELETE FROM giveaways")
    suspend fun clearAll()

    @Transaction
    suspend fun syncGiveawaysPreservingStatus(newItems: List<GiveawayEntity>): List<GiveawayEntity> {
        val freshlyDiscovered = mutableListOf<GiveawayEntity>()
        for (item in newItems) {
            val existing = getGiveawayById(item.id)
            if (existing == null) {
                // Completely new giveaway!
                freshlyDiscovered.add(item)
                insertAll(listOf(item))
            } else {
                // Preserve user bookmarks, claimed status, and original discovery timestamp
                val updated = item.copy(
                    isClaimed = existing.isClaimed,
                    isBookmarked = existing.isBookmarked,
                    firstSeenTimestamp = existing.firstSeenTimestamp,
                    notified = existing.notified
                )
                insertAll(listOf(updated))
            }
        }
        return freshlyDiscovered
    }
}
