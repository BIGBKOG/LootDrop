package com.example.ui

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.SteamGameEntity
import com.example.data.model.GiveawayItem
import com.example.data.model.PublicReview
import com.example.data.model.SteamDlcRecommendation
import com.example.i18n.AppStringsRegistry
import com.example.preferences.AppPreferences
import com.example.util.PublicReviewsManager
import com.example.util.SteamDlcRecommender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal object DlcAndReviewsDelegate {

    fun mergeDlcRecommendations(
        giveaways: List<GiveawayItem>,
        steamGames: List<SteamGameEntity>,
        storeDlcs: List<SteamDlcRecommendation>,
        dismissedIds: Set<Int>
    ): List<SteamDlcRecommendation> {
        if (steamGames.isEmpty()) return emptyList()
        val ownedAppIds = steamGames.map { it.appId }.toSet()
        val freeGiveawayMatches = SteamDlcRecommender.findFreeDlcGiveawaysForSteamLibrary(giveaways, steamGames)
        val filteredStore = storeDlcs.filter { dlc ->
            dlc.dlcAppId !in ownedAppIds &&
                dlc.dlcAppId !in dismissedIds &&
                dlc.parentAppId in ownedAppIds
        }
        val giveawayAppIds = freeGiveawayMatches.map { it.dlcAppId }.toSet()
        return (freeGiveawayMatches + filteredStore.filter { it.dlcAppId !in giveawayAppIds })
            .filter { it.dlcAppId !in dismissedIds }
            .sortedWith(
                compareByDescending<SteamDlcRecommendation> { it.isFromFreeGiveaway }
                    .thenBy { it.isSoundtrackOrCosmetic }
                    .thenByDescending { it.relevanceScore }
            )
    }

    suspend fun scanLibraryDlcs(
        database: AppDatabase,
        preferences: AppPreferences,
        forceRefresh: Boolean,
        dismissedIds: Set<Int>,
        onCached: (List<SteamDlcRecommendation>) -> Unit,
        onLoading: (Boolean) -> Unit,
        onStatus: (String) -> Unit
    ) {
        if (!forceRefresh && SteamDlcRecommender.isCacheFresh(preferences)) {
            onCached(SteamDlcRecommender.loadCachedDlcs(preferences))
            return
        }
        val s = AppStringsRegistry.forLanguage(preferences.appLanguage)
        val ownedGames = database.steamGameDao().getAllSteamGamesList()
        if (ownedGames.isEmpty()) {
            onStatus(s.system.dlcSyncSteamFirst)
            return
        }
        onLoading(true)
        onStatus(s.system.dlcScanningStore)
        try {
            val fetched = SteamDlcRecommender.fetchRecommendationsForLibrary(
                steamGames = ownedGames,
                dismissedIds = dismissedIds,
                onProgress = onStatus
            )
            if (fetched.isNotEmpty()) {
                onCached(fetched)
                SteamDlcRecommender.saveCachedDlcs(preferences, fetched)
                onStatus(String.format(s.system.dlcFoundCountFormat, fetched.size))
            } else {
                onStatus(s.system.dlcNoneFound)
            }
        } catch (e: Exception) {
            onStatus(String.format(s.system.dlcScanErrorFormat, e.message ?: ""))
        } finally {
            onLoading(false)
        }
    }

    suspend fun loadGameDlcs(
        database: AppDatabase,
        preferences: AppPreferences,
        game: SteamGameEntity,
        allGiveaways: List<GiveawayItem>,
        dismissedIds: Set<Int>,
        existingStoreDlcs: List<SteamDlcRecommendation>,
        onGameDlcs: (List<SteamDlcRecommendation>) -> Unit,
        onStoreDlcsUpdated: (List<SteamDlcRecommendation>) -> Unit
    ) {
        val ownedIds = database.steamGameDao().getAllSteamGamesList().map { it.appId }.toSet()
        val freeMatches = SteamDlcRecommender.findFreeDlcGiveawaysForSteamLibrary(allGiveaways, listOf(game))
        val storeDlcs = SteamDlcRecommender.fetchDlcsForSingleGame(game, ownedIds, dismissedIds)
        val freeIds = freeMatches.map { it.dlcAppId }.toSet()
        val combined = (freeMatches + storeDlcs.filter { it.dlcAppId !in freeIds })
            .filter { it.dlcAppId !in dismissedIds }
            .sortedWith(
                compareByDescending<SteamDlcRecommendation> { it.isFromFreeGiveaway }
                    .thenBy { it.isSoundtrackOrCosmetic }
                    .thenByDescending { it.relevanceScore }
            )
        onGameDlcs(combined)
        if (storeDlcs.isNotEmpty()) {
            val merged = (existingStoreDlcs + storeDlcs).distinctBy { it.dlcAppId }.take(50)
            onStoreDlcsUpdated(merged)
            SteamDlcRecommender.saveCachedDlcs(preferences, merged)
        }
    }

    fun filterReviewsForGame(all: List<PublicReview>, giveawayId: Long, gameTitle: String): List<PublicReview> {
        val cleanGame = gameTitle.trim().lowercase()
        return all.filter { review ->
            (giveawayId > 0 && review.giveawayId == giveawayId) ||
                (cleanGame.isNotBlank() && review.gameTitle.trim().lowercase() == cleanGame) ||
                (cleanGame.length >= 4 && (review.gameTitle.lowercase().contains(cleanGame) || cleanGame.contains(review.gameTitle.lowercase())))
        }
    }

    suspend fun submitReview(
        context: Context,
        giveawayId: Long,
        gameTitle: String,
        authorName: String,
        authorTag: String,
        rating: Int,
        title: String,
        comment: String,
        platform: String,
        isVerifiedClaimer: Boolean,
        isAppReview: Boolean,
        successMessage: String,
        onSuccess: (PublicReview) -> Unit,
        onComplete: (Boolean, String) -> Unit
    ) {
        val result = PublicReviewsManager.postReview(
            context = context,
            giveawayId = giveawayId,
            gameTitle = gameTitle,
            authorName = authorName,
            authorTag = authorTag,
            rating = rating,
            title = title,
            comment = comment,
            platform = platform,
            isVerifiedClaimer = isVerifiedClaimer,
            isAppReview = isAppReview
        )
        result.onSuccess { review ->
            onSuccess(review)
            withContext(Dispatchers.Main) { onComplete(true, successMessage) }
        }.onFailure { err ->
            val s = AppStringsRegistry.forContext(context)
            withContext(Dispatchers.Main) { onComplete(false, err.message ?: s.dialogs.reviewsPostFailed) }
        }
    }
}
