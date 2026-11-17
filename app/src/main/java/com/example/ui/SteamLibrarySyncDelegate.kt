package com.example.ui

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.SteamAchievementEntity
import com.example.data.local.SteamGameEntity
import com.example.data.model.GiveawayItem
import com.example.data.model.PlayStatus
import com.example.data.remote.SteamApiClient
import com.example.data.remote.SteamOwnedGamesResult
import com.example.data.repository.GiveawaysRepository
import com.example.i18n.AppStringsRegistry
import com.example.preferences.AppPreferences
import com.example.util.SteamSyncHelper

internal object SteamLibrarySyncDelegate {

    suspend fun syncSteamLibrary(
        context: Context,
        database: AppDatabase,
        repository: GiveawaysRepository,
        preferences: AppPreferences,
        rawUserId: String,
        rawApiKey: String,
        allGiveaways: List<GiveawayItem>,
        onStatus: (String) -> Unit,
        onUserIdSaved: (String) -> Unit,
        onTriggerDlcRefresh: () -> Unit
    ) {
        val s = AppStringsRegistry.forLanguage(preferences.appLanguage)
        if (rawUserId.isBlank()) {
            onStatus(s.system.steamEnterIdPrompt)
            return
        }

        val apiKey = SteamApiClient.resolveApiKey(rawApiKey)
        onStatus(String.format(s.system.steamResolvingFormat, rawUserId))
        val resolvedSteamId = SteamApiClient.resolveSteamId(rawUserId, apiKey)
        if (resolvedSteamId == null) {
            onStatus(String.format(s.system.steamNotFoundFormat, rawUserId))
            return
        }
        preferences.steamUserId = rawUserId
        onUserIdSaved(rawUserId)

        onStatus(s.system.steamFetchingGames)
        val gamesResult = SteamApiClient.getOwnedGamesWithStatus(resolvedSteamId, apiKey)
        val wishlistMap = SteamApiClient.getWishlist(resolvedSteamId, apiKey)

        val ownedGames = when (gamesResult) {
            is SteamOwnedGamesResult.Success -> gamesResult.games
            is SteamOwnedGamesResult.PrivacyRestricted -> {
                onStatus(s.system.steamPrivateGamesMsg)
                return
            }
            is SteamOwnedGamesResult.Error -> {
                onStatus(String.format(s.system.steamApiErrorFormat, gamesResult.message))
                return
            }
        }

        if (ownedGames.isEmpty() && wishlistMap.isEmpty()) {
            onStatus(String.format(s.system.steamNoGamesFoundFormat, resolvedSteamId))
            return
        }

        val claimedItems = allGiveaways.filter { it.isClaimed }
        val existingMap = database.steamGameDao().getAllSteamGamesList().associateBy { it.appId }

        val steamEntities = ownedGames.map { g ->
            val matchedClaimed = claimedItems.firstOrNull { SteamApiClient.matchSteamGame(it, listOf(g)) != null }
            val existing = existingMap[g.appId]
            val unlocked = existing?.achievementsUnlocked ?: 0
            val total = existing?.achievementsTotal ?: 0
            val preservedStatus = if (existing != null && (existing.playStatus == PlayStatus.BEATEN || existing.playStatus == PlayStatus.DROPPED)) {
                existing.playStatus
            } else {
                SteamApiClient.classifyPlayStatus(g.playtimeForeverMinutes, unlocked, total)
            }
            val icon = if (g.imgIconUrl.isNotBlank()) {
                "https://media.steampowered.com/steamcommunity/public/images/apps/${g.appId}/${g.imgIconUrl}.jpg"
            } else existing?.imgIconUrl ?: ""

            SteamGameEntity(
                appId = g.appId,
                name = g.name,
                playtimeMinutes = g.playtimeForeverMinutes,
                playtime2WeeksMinutes = g.playtime2WeeksMinutes,
                lastPlayedTimestamp = g.rtimeLastPlayed,
                playStatus = preservedStatus,
                achievementsUnlocked = unlocked,
                achievementsTotal = total,
                imgIconUrl = icon,
                matchedGiveawayId = matchedClaimed?.id ?: existing?.matchedGiveawayId,
                isLootDropClaimed = (matchedClaimed != null) || (existing?.isLootDropClaimed == true),
                manualInstallSizeGb = existing?.manualInstallSizeGb
            )
        }

        if (steamEntities.isNotEmpty()) {
            repository.insertSteamGames(steamEntities)
        }

        var wishlistMatchCount = 0
        val wishlistAppIds = wishlistMap.keys.toList()
        if (wishlistAppIds.isNotEmpty()) {
            repository.markWishlistedBySteamAppIds(wishlistAppIds)
        }
        for (giveaway in allGiveaways) {
            if (SteamApiClient.isItemOnWishlist(giveaway, wishlistMap)) {
                wishlistMatchCount++
                if (!giveaway.isWishlisted) {
                    repository.toggleWishlisted(giveaway.id, false)
                }
            }
        }

        var matchedCount = 0
        for (claimed in claimedItems) {
            val steamGame = SteamApiClient.matchSteamGame(claimed, ownedGames) ?: continue
            matchedCount++
            val achievements = if (steamGame.playtimeForeverMinutes > 0) {
                SteamApiClient.getPlayerAchievements(steamGame.appId, resolvedSteamId, apiKey)
            } else null

            val unlocked = achievements?.unlockedCount ?: 0
            val total = achievements?.totalCount ?: 0
            val classifiedStatus = SteamApiClient.classifyPlayStatus(steamGame.playtimeForeverMinutes, unlocked, total)

            repository.updateSteamTelemetry(
                id = claimed.id,
                status = classifiedStatus,
                steamAppId = steamGame.appId,
                playtimeMinutes = steamGame.playtimeForeverMinutes,
                achievementsUnlocked = unlocked,
                achievementsTotal = total,
                lastPlayed = steamGame.rtimeLastPlayed
            )
            repository.updateSteamGamePlayStatus(steamGame.appId, classifiedStatus)
            database.steamGameDao().updateTelemetry(
                appId = steamGame.appId,
                status = classifiedStatus,
                playtime = steamGame.playtimeForeverMinutes,
                unlocked = unlocked,
                total = total,
                lastPlayed = steamGame.rtimeLastPlayed
            )
        }

        onStatus(s.system.steamScanningTrophies)
        val gamesWithPlaytime = ownedGames.filter { it.playtimeForeverMinutes > 0 }
            .sortedByDescending { it.playtimeForeverMinutes }
            .take(25)

        var syncedTrophiesCount = 0
        for (sg in gamesWithPlaytime) {
            val detailedAchievements = SteamApiClient.getDetailedPlayerAchievements(
                appId = sg.appId,
                steamId = resolvedSteamId,
                apiKey = apiKey,
                gameName = sg.name
            )
            if (detailedAchievements.isNotEmpty()) {
                SteamSyncHelper.checkAndNotifyMythicAchievements(
                    context = context,
                    db = database,
                    achievements = detailedAchievements,
                    sendSystemNotification = false
                )
                repository.insertAchievements(detailedAchievements)
                val unlocked = detailedAchievements.count { it.isAchieved }
                val total = detailedAchievements.size
                syncedTrophiesCount += unlocked
                val classified = SteamApiClient.classifyPlayStatus(sg.playtimeForeverMinutes, unlocked, total)
                database.steamGameDao().updateTelemetry(
                    appId = sg.appId,
                    status = classified,
                    playtime = sg.playtimeForeverMinutes,
                    unlocked = unlocked,
                    total = total,
                    lastPlayed = sg.rtimeLastPlayed
                )
            } else {
                database.steamGameDao().updateTelemetry(
                    appId = sg.appId,
                    status = SteamApiClient.classifyPlayStatus(sg.playtimeForeverMinutes, 0, 0),
                    playtime = sg.playtimeForeverMinutes,
                    unlocked = 0,
                    total = 0,
                    lastPlayed = sg.rtimeLastPlayed
                )
            }
        }

        val trophyMsg = if (syncedTrophiesCount > 0) String.format(s.system.steamTrophiesMsgFormat, syncedTrophiesCount) else ""
        val wishlistMsg = if (wishlistMap.isNotEmpty()) String.format(s.system.steamWishlistMsgFormat, wishlistMap.size, wishlistMatchCount) else ""
        onStatus(
            if (ownedGames.isNotEmpty()) {
                String.format(s.system.steamSyncedSummaryFormat, ownedGames.size, matchedCount, trophyMsg, wishlistMsg)
            } else {
                String.format(s.system.steamWishlistSyncedFormat, wishlistMatchCount)
            }
        )
        if (ownedGames.isNotEmpty()) {
            onTriggerDlcRefresh()
        }
    }

    suspend fun loadAchievementsForGame(
        context: Context,
        database: AppDatabase,
        repository: GiveawaysRepository,
        game: SteamGameEntity,
        userId: String,
        steamApiKey: String,
        onAchievementsLoaded: (List<SteamAchievementEntity>) -> Unit
    ) {
        val local = repository.getAchievementsForApp(game.appId, game.name)
        onAchievementsLoaded(local)

        val trimmedUser = userId.trim()
        if (trimmedUser.isNotBlank()) {
            val apiKey = SteamApiClient.resolveApiKey(steamApiKey)
            val resolvedId = SteamApiClient.resolveSteamId(trimmedUser, apiKey)
            if (resolvedId != null) {
                val remote = SteamApiClient.getDetailedPlayerAchievements(game.appId, resolvedId, apiKey, game.name)
                if (remote.isNotEmpty()) {
                    SteamSyncHelper.checkAndNotifyMythicAchievements(
                        context = context,
                        db = database,
                        achievements = remote,
                        sendSystemNotification = false
                    )
                    repository.insertAchievements(remote)
                    onAchievementsLoaded(remote)
                }
            }
        }
    }
}
