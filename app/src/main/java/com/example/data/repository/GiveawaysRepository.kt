package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.GiveawayDao
import com.example.data.local.GiveawayEntity
import com.example.data.local.SteamGameDao
import com.example.data.local.SteamGameEntity
import com.example.data.model.GiveawayDto
import com.example.data.model.GiveawayItem
import com.example.data.model.PlayStatus
import com.example.data.remote.ApiClient
import com.example.notifications.NotificationHelper
import com.example.util.MetacriticCatalog
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
    private val notificationHelper: NotificationHelper,
    steamGameDao: SteamGameDao? = null
) {
    private val steamDao: SteamGameDao by lazy {
        steamGameDao ?: AppDatabase.getInstance(notificationHelper.context).steamGameDao()
    }

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

    val allSteamGamesFlow: Flow<List<SteamGameEntity>>
        get() = steamDao.getAllSteamGamesFlow()


    suspend fun refreshGiveaways(): Result<SyncResult> = withContext(Dispatchers.IO) {
        var primaryEntities: List<GiveawayEntity>? = null
        var primaryException: Exception? = null

        // 1. Primary Source: GamerPower API
        try {
            val dtoList = ApiClient.apiService.getGiveaways(platform = null)
            if (dtoList.isNotEmpty()) {
                primaryEntities = dtoList.map { it.toEntity() }
            }
        } catch (e: Exception) {
            primaryException = e
        }

        // 2. Secondary Multi-Source & Fallback: CheapShark Free Deals Endpoint
        var cheapSharkEntities: List<GiveawayEntity> = emptyList()
        try {
            val csDeals = ApiClient.cheapSharkApiService.getFreeDeals(onSale = 1, upperPrice = 0)
            if (csDeals.isNotEmpty()) {
                // Register verified Metacritic ratings received from CheapShark
                for (deal in csDeals) {
                    val score = deal.metacriticScore?.toIntOrNull() ?: 0
                    if (score > 0) {
                        MetacriticCatalog.registerScore(deal.title, score)
                    }
                }
                cheapSharkEntities = csDeals.map { it.toEntity() }
            }
        } catch (_: Exception) {
            // Non-fatal if primary is healthy
        }

        // 3. Resolve Feed: Merge & Enrich or Trigger Emergency Fallback
        val finalEntities: List<GiveawayEntity> = when {
            // Case A: GamerPower succeeded - enrich with CheapShark metadata and merge unlisted free deals
            !primaryEntities.isNullOrEmpty() -> {
                val primaryCleanTitles = primaryEntities.map { MetacriticCatalog.cleanTitle(it.title).lowercase() }.toSet()

                // Enrich any GamerPower items that lacked precise details
                val enrichedPrimary = primaryEntities.map { gpItem ->
                    val cleanTitle = MetacriticCatalog.cleanTitle(gpItem.title).lowercase()
                    val csMatch = cheapSharkEntities.firstOrNull {
                        MetacriticCatalog.cleanTitle(it.title).lowercase() == cleanTitle
                    }
                    if (csMatch != null) {
                        var updated = gpItem
                        // If GamerPower lacks end date or has N/A, enrich with CheapShark's status
                        if ((updated.endDate == "N/A" || updated.endDate.isBlank()) && csMatch.endDate != "N/A") {
                            updated = updated.copy(endDate = csMatch.endDate)
                        }
                        updated
                    } else {
                        gpItem
                    }
                }

                // Add any 100% off deals on CheapShark not present in GamerPower feed
                val nonOverlappingCheapShark = cheapSharkEntities.filter { csItem ->
                    val clean = MetacriticCatalog.cleanTitle(csItem.title).lowercase()
                    !primaryCleanTitles.contains(clean)
                }

                enrichedPrimary + nonOverlappingCheapShark
            }
            // Case B: GamerPower failed/empty, but CheapShark free deals fallback is available!
            cheapSharkEntities.isNotEmpty() -> {
                cheapSharkEntities
            }
            // Case C: Both services failed
            else -> {
                return@withContext Result.failure(
                    primaryException ?: Exception("Both GamerPower and CheapShark multi-source endpoints failed.")
                )
            }
        }

        try {
            val existingIds = giveawayDao.getAllExistingIds().toSet()
            val isInitialBootstrap = existingIds.isEmpty()

            val newlyDiscovered = giveawayDao.syncGiveawaysPreservingStatus(finalEntities)

            // If we already had games cached and new ones appeared, notify user for each!
            if (!isInitialBootstrap) {
                for (fresh in newlyDiscovered) {
                    val domainItem = fresh.toDomain()
                    notificationHelper.sendNewGameNotification(domainItem)
                    giveawayDao.markNotified(fresh.id)
                }
            }

            val newCount = if (isInitialBootstrap) 0 else newlyDiscovered.size
            com.example.widget.WidgetUpdateHelper.updateAllWidgets(notificationHelper.context)
            Result.success(SyncResult(totalCount = finalEntities.size, newGamesCount = newCount))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleClaimed(id: Int, currentlyClaimed: Boolean) = withContext(Dispatchers.IO) {
        giveawayDao.updateClaimed(id, !currentlyClaimed)
        com.example.widget.WidgetUpdateHelper.updateAllWidgets(notificationHelper.context)
    }

    suspend fun updatePlayStatus(id: Int, status: com.example.data.model.PlayStatus) = withContext(Dispatchers.IO) {
        giveawayDao.updatePlayStatus(id, status)
    }

    suspend fun updateSteamTelemetry(
        id: Int,
        status: com.example.data.model.PlayStatus,
        steamAppId: Int?,
        playtimeMinutes: Int,
        achievementsUnlocked: Int,
        achievementsTotal: Int,
        lastPlayed: Long = System.currentTimeMillis()
    ) = withContext(Dispatchers.IO) {
        giveawayDao.updateSteamTelemetry(
            id = id,
            status = status,
            steamAppId = steamAppId,
            playtimeMinutes = playtimeMinutes,
            achievementsUnlocked = achievementsUnlocked,
            achievementsTotal = achievementsTotal,
            lastPlayed = lastPlayed
        )
    }

    suspend fun toggleBookmarked(id: Int, currentlyBookmarked: Boolean) = withContext(Dispatchers.IO) {
        giveawayDao.updateBookmarked(id, !currentlyBookmarked)
    }

    suspend fun toggleWishlisted(id: Int, currentlyWishlisted: Boolean) = withContext(Dispatchers.IO) {
        giveawayDao.updateWishlisted(id, !currentlyWishlisted)
    }

    suspend fun markWishlistedBySteamAppIds(steamAppIds: List<Int>) = withContext(Dispatchers.IO) {
        giveawayDao.markWishlistedBySteamAppIds(steamAppIds)
    }

    suspend fun clearAllWishlisted() = withContext(Dispatchers.IO) {
        giveawayDao.clearAllWishlisted()
    }

    suspend fun insertSteamGames(games: List<SteamGameEntity>) = withContext(Dispatchers.IO) {
        steamDao.insertAll(games)
    }

    suspend fun updateSteamGamePlayStatus(appId: Int, status: PlayStatus) = withContext(Dispatchers.IO) {
        steamDao.updatePlayStatus(appId, status)
    }

    suspend fun clearAllSteamGames() = withContext(Dispatchers.IO) {
        steamDao.clearAll()
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

    suspend fun clearAllCaches(context: android.content.Context): Long = withContext(Dispatchers.IO) {
        (context.applicationContext as? android.app.Application)?.let { app ->
            com.example.LootDropApplication.deepCleanAll(app)
        } ?: 0L
    }

    suspend fun clearImageCache(context: android.content.Context): Long = withContext(Dispatchers.IO) {
        (context.applicationContext as? android.app.Application)?.let { app ->
            com.example.LootDropApplication.clearImageDiskCache(app)
        } ?: 0L
    }

    suspend fun clearHttpCache(context: android.content.Context): Long = withContext(Dispatchers.IO) {
        ApiClient.clearHttpCache(context)
    }

    suspend fun vacuumDatabase(context: android.content.Context) = withContext(Dispatchers.IO) {
        com.example.data.local.AppDatabase.vacuumAndCompact(context)
    }

    suspend fun pruneExpiredGiveaways(context: android.content.Context): Int = withContext(Dispatchers.IO) {
        val isoFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val todayStr = isoFormat.format(java.util.Date())
        val deleted = giveawayDao.pruneExpiredGiveaways(todayStr)
        if (deleted > 0) {
            com.example.data.local.AppDatabase.vacuumAndCompact(context)
        }
        deleted
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
