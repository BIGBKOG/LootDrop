package com.example.data.repository

import com.example.data.local.GiveawayDao
import com.example.data.local.GiveawayEntity
import com.example.data.model.GiveawayDto
import com.example.data.model.GiveawayItem
import com.example.data.remote.ApiClient
import com.example.notifications.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

data class SyncResult(
    val totalCount: Int,
    val newGamesCount: Int
)

class GiveawaysRepository(
    private val giveawayDao: GiveawayDao,
    private val notificationHelper: NotificationHelper
) {

    val allGiveawaysFlow: Flow<List<GiveawayItem>> =
        giveawayDao.getAllGiveawaysFlow().map { list ->
            list.map { it.toDomain() }
        }

    val bookmarkedGiveawaysFlow: Flow<List<GiveawayItem>> =
        giveawayDao.getBookmarkedGiveawaysFlow().map { list ->
            list.map { it.toDomain() }
        }

    val claimedGiveawaysFlow: Flow<List<GiveawayItem>> =
        giveawayDao.getClaimedGiveawaysFlow().map { list ->
            list.map { it.toDomain() }
        }

    suspend fun refreshGiveaways(): Result<SyncResult> = withContext(Dispatchers.IO) {
        try {
            val dtoList = ApiClient.apiService.getGiveaways(platform = "pc")
            val existingIds = giveawayDao.getAllExistingIds().toSet()
            val isInitialBootstrap = existingIds.isEmpty()

            val entities = dtoList.map { dto ->
                dto.toEntity()
            }

            val newlyDiscovered = giveawayDao.syncGiveawaysPreservingStatus(entities)

            // If we already had games cached and new ones appeared, notify user for each!
            if (!isInitialBootstrap) {
                for (fresh in newlyDiscovered) {
                    val domainItem = fresh.toDomain()
                    notificationHelper.sendNewGameNotification(domainItem)
                    giveawayDao.markNotified(fresh.id)
                }
            }

            val newCount = if (isInitialBootstrap) 0 else newlyDiscovered.size
            Result.success(SyncResult(totalCount = entities.size, newGamesCount = newCount))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleClaimed(id: Int, currentlyClaimed: Boolean) = withContext(Dispatchers.IO) {
        giveawayDao.updateClaimed(id, !currentlyClaimed)
    }

    suspend fun toggleBookmarked(id: Int, currentlyBookmarked: Boolean) = withContext(Dispatchers.IO) {
        giveawayDao.updateBookmarked(id, !currentlyBookmarked)
    }

    suspend fun getGiveawayById(id: Int): GiveawayItem? = withContext(Dispatchers.IO) {
        giveawayDao.getGiveawayById(id)?.toDomain()
    }

    suspend fun clearAllClaimed() = withContext(Dispatchers.IO) {
        giveawayDao.clearAllClaimed()
    }

    suspend fun clearAllBookmarks() = withContext(Dispatchers.IO) {
        giveawayDao.clearAllBookmarks()
    }

    suspend fun forceResync(): Result<SyncResult> = withContext(Dispatchers.IO) {
        try {
            giveawayDao.clearAll()
            refreshGiveaways()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun GiveawayDto.toEntity(): GiveawayEntity {
        val cleanWorth = worth?.trim() ?: "FREE"
        val numeric = cleanWorth.replace("$", "").replace("€", "").trim().toDoubleOrNull() ?: 0.0

        return GiveawayEntity(
            id = id,
            title = title,
            worth = cleanWorth,
            numericWorth = numeric,
            thumbnail = thumbnail ?: "",
            image = image ?: thumbnail ?: "",
            description = description ?: "Free PC giveaway available to claim.",
            instructions = instructions ?: "Click Claim to visit the official giveaway page.",
            openGiveawayUrl = openGiveawayUrl ?: gamerpowerUrl ?: "https://www.gamerpower.com",
            publishedDate = publishedDate ?: "",
            type = type ?: "Game",
            platforms = platforms ?: "PC",
            endDate = endDate ?: "N/A",
            users = users ?: 0,
            status = status ?: "Active",
            firstSeenTimestamp = System.currentTimeMillis()
        )
    }
}
