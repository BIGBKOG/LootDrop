package com.example.ui

import com.example.data.local.SteamAchievementEntity
import com.example.data.local.SteamGameEntity
import com.example.data.model.GameCategory
import com.example.data.model.GiveawayItem
import com.example.data.model.LauncherOption
import com.example.data.model.LauncherType
import com.example.data.model.PlayStatus
import com.example.data.model.PlayabilityFilter
import com.example.data.model.StorageFilter
import com.example.data.model.StorageTier
import com.example.data.remote.SteamApiClient
import com.example.util.MetacriticCatalog

internal object GiveawayFilterEngine {

    private val CLEAN_TITLE_LAUNCHER_REGEX = Regex(
        "(?i)\\b(giveaway|free|dlc|pack|key|bundle|edition|standard|steam|epic|gog|playstation|ps5|ps4|xbox)\\b"
    )

    fun computeUiStats(
        all: List<GiveawayItem>,
        steamGames: List<SteamGameEntity>
    ): UiStats {
        val launcherEntries = LauncherType.entries
        val platformCountsArr = IntArray(launcherEntries.size)
        platformCountsArr[LauncherType.ALL.ordinal] = all.size

        var totalFreeWorthUsd = 0.0
        var newFreebiesCount = 0
        var expiringSoon = 0
        var claimedTotalSavedUsd = 0.0
        var claimedCount = 0
        var unplayedCount = 0
        var playingCount = 0
        var finishedCount = 0
        var droppedCount = 0
        var claimedTotalStorageGb = 0.0

        // Hardware counters tracked separately for claimed vs all items to select claimed.ifEmpty { all } in one pass
        var claimedDeck = 0
        var claimedCtrl = 0
        var claimedQuick = 0
        var claimedFeather = 0
        var claimedCompact = 0
        var claimedMedium = 0
        var claimedHeavy = 0

        var allDeck = 0
        var allCtrl = 0
        var allQuick = 0
        var allFeather = 0
        var allCompact = 0
        var allMedium = 0
        var allHeavy = 0

        for (item in all) {
            totalFreeWorthUsd += item.numericWorth
            if (item.isNew) newFreebiesCount++
            if (item.endDate != "N/A" && item.endDate.isNotBlank()) expiringSoon++

            for (launcher in launcherEntries) {
                if (launcher != LauncherType.ALL && item.matchesLauncher(launcher)) {
                    platformCountsArr[launcher.ordinal]++
                }
            }

            val hw = item.hardwareProfile
            if (hw.isDeckVerified) allDeck++
            if (hw.isControllerFriendly) allCtrl++
            if (hw.isQuickSession) allQuick++
            when (hw.storageTier) {
                StorageTier.FEATHERWEIGHT -> allFeather++
                StorageTier.COMPACT -> allCompact++
                StorageTier.MEDIUM -> allMedium++
                StorageTier.HEAVYWEIGHT -> allHeavy++
            }

            if (item.isClaimed) {
                claimedCount++
                claimedTotalSavedUsd += item.numericWorth
                claimedTotalStorageGb += hw.effectiveSizeGb
                when (item.playStatus) {
                    PlayStatus.NOT_STARTED -> unplayedCount++
                    PlayStatus.PLAYING -> playingCount++
                    PlayStatus.BEATEN -> finishedCount++
                    PlayStatus.DROPPED -> droppedCount++
                }
                if (hw.isDeckVerified) claimedDeck++
                if (hw.isControllerFriendly) claimedCtrl++
                if (hw.isQuickSession) claimedQuick++
                when (hw.storageTier) {
                    StorageTier.FEATHERWEIGHT -> claimedFeather++
                    StorageTier.COMPACT -> claimedCompact++
                    StorageTier.MEDIUM -> claimedMedium++
                    StorageTier.HEAVYWEIGHT -> claimedHeavy++
                }
            }
        }

        val platformMap = LinkedHashMap<LauncherType, Int>(launcherEntries.size)
        for (launcher in launcherEntries) {
            platformMap[launcher] = platformCountsArr[launcher.ordinal]
        }

        var steamUnplayedCount = 0
        var steamPlayingCount = 0
        var steamFinishedCount = 0
        var steamDroppedCount = 0
        var steamTotalStorageGb = 0.0
        for (steam in steamGames) {
            when (steam.playStatus) {
                PlayStatus.NOT_STARTED -> steamUnplayedCount++
                PlayStatus.PLAYING -> steamPlayingCount++
                PlayStatus.BEATEN -> steamFinishedCount++
                PlayStatus.DROPPED -> steamDroppedCount++
            }
            steamTotalStorageGb += steam.hardwareProfile.effectiveSizeGb
        }

        val useClaimedHardware = claimedCount > 0
        return UiStats(
            totalFreeWorthUsd = totalFreeWorthUsd,
            activeGiveawaysCount = all.size,
            newFreebiesCount = newFreebiesCount,
            platformCounts = platformMap,
            claimedTotalSavedUsd = claimedTotalSavedUsd,
            claimedCount = claimedCount,
            expiringSoon = expiringSoon,
            unplayedCount = unplayedCount,
            playingCount = playingCount,
            finishedCount = finishedCount,
            droppedCount = droppedCount,
            steamTotalCount = steamGames.size,
            steamUnplayedCount = steamUnplayedCount,
            steamPlayingCount = steamPlayingCount,
            steamFinishedCount = steamFinishedCount,
            steamDroppedCount = steamDroppedCount,
            deckVerifiedCount = if (useClaimedHardware) claimedDeck else allDeck,
            controllerFriendlyCount = if (useClaimedHardware) claimedCtrl else allCtrl,
            quickSessionCount = if (useClaimedHardware) claimedQuick else allQuick,
            storageUnder2GbCount = if (useClaimedHardware) claimedFeather else allFeather,
            storage2To10GbCount = if (useClaimedHardware) claimedCompact else allCompact,
            storage10To40GbCount = if (useClaimedHardware) claimedMedium else allMedium,
            storageOver40GbCount = if (useClaimedHardware) claimedHeavy else allHeavy,
            claimedTotalStorageGb = claimedTotalStorageGb.toFloat(),
            steamTotalStorageGb = steamTotalStorageGb.toFloat()
        )
    }

    fun computeAchievementOverviewStats(
        trophies: List<SteamAchievementEntity>,
        games: List<SteamGameEntity>
    ): AchievementOverviewStats {
        var gamesWithAchCount = 0
        var completionSum = 0
        var masteredGamesCount = 0
        var closestToHundredCount = 0
        for (game in games) {
            if (game.achievementsTotal > 0) {
                gamesWithAchCount++
                val pct = game.achievementsPercentage
                completionSum += pct
                if (game.achievementsUnlocked >= game.achievementsTotal) masteredGamesCount++
                if (pct in 80..99) closestToHundredCount++
            }
        }
        val avgCompletion = if (gamesWithAchCount > 0) (completionSum / gamesWithAchCount) else 0

        var mythicTrophiesCount = 0
        var goldTrophiesCount = 0
        for (trophy in trophies) {
            val pct = trophy.globalPercentage
            if (pct in 0.001f..5.0f) mythicTrophiesCount++
            if (pct in 5.0f..15.0f) goldTrophiesCount++
        }

        return AchievementOverviewStats(
            totalUnlockedTrophies = trophies.size,
            mythicTrophiesCount = mythicTrophiesCount,
            goldTrophiesCount = goldTrophiesCount,
            masteredGamesCount = masteredGamesCount,
            closestToHundredCount = closestToHundredCount,
            averageCompletionPercentage = avgCompletion
        )
    }

    fun filterAndSortList(
        source: List<GiveawayItem>,
        launchers: Set<LauncherType>,
        typeFilter: TypeFilter,
        categories: Set<GameCategory>,
        sort: SortOption,
        query: String,
        playability: PlayabilityFilter = PlayabilityFilter.ALL,
        storage: StorageFilter = StorageFilter.ALL
    ): List<GiveawayItem> {
        var filtered = source

        if (launchers.isNotEmpty() && !launchers.contains(LauncherType.ALL)) {
            filtered = filtered.filter { item -> launchers.any { item.matchesLauncher(it) } }
        }

        if (typeFilter != TypeFilter.ALL && typeFilter.apiMatch != null) {
            val match = typeFilter.apiMatch
            filtered = filtered.filter { it.type.contains(match, ignoreCase = true) }
        }

        if (categories.isNotEmpty()) {
            filtered = filtered.filter { item -> item.categories.any { it in categories } }
        }

        filtered = when (playability) {
            PlayabilityFilter.ALL -> filtered
            PlayabilityFilter.DECK_VERIFIED -> filtered.filter { it.hardwareProfile.isDeckVerified }
            PlayabilityFilter.CONTROLLER_FRIENDLY -> filtered.filter { it.hardwareProfile.isControllerFriendly }
            PlayabilityFilter.QUICK_SESSION -> filtered.filter { it.hardwareProfile.isQuickSession }
        }

        if (storage != StorageFilter.ALL && storage.tier != null) {
            filtered = filtered.filter { storage.tier.matches(it.hardwareProfile.effectiveStorageGb) }
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            filtered = filtered.filter {
                it.title.lowercase().contains(q) ||
                    it.description.lowercase().contains(q) ||
                    it.platforms.lowercase().contains(q) ||
                    (q.contains("playstation") && it.isPlayStation) ||
                    (q.contains("ps5") && it.isPlayStation) ||
                    (q.contains("ps4") && it.isPlayStation) ||
                    (q.contains("xbox") && it.isXbox) ||
                    (q.contains("nintendo") && it.isNintendo) ||
                    (q.contains("switch") && it.isNintendo) ||
                    (q.contains("android") && it.isAndroid) ||
                    (q.contains("ios") && it.isIos) ||
                    (q.contains("iphone") && it.isIos) ||
                    (q.contains("ipad") && it.isIos) ||
                    (q.contains("mobile") && it.isMobile) ||
                    (q.contains("console") && it.isConsole)
            }
        }

        return when (sort) {
            SortOption.NEWEST -> filtered.sortedByDescending { it.id }
            SortOption.HIGHEST_METACRITIC -> filtered.sortedByDescending { it.metacriticScore }
            SortOption.HIGHEST_VALUE -> filtered.sortedByDescending { it.numericWorth }
            SortOption.MOST_POPULAR -> filtered.sortedByDescending { it.users }
            SortOption.ENDING_SOON -> filtered.sortedBy {
                if (it.endDate == "N/A" || it.endDate.isBlank()) "9999" else it.endDate
            }
        }
    }

    fun checkOwnership(
        item: GiveawayItem,
        steamGamesList: List<SteamGameEntity>
    ): GiveawayOwnershipStatus {
        val inVault = item.isClaimed || item.playStatus != PlayStatus.NOT_STARTED
        val matchedSteam = if (steamGamesList.isNotEmpty()) {
            val itemAppId = item.steamAppId?.takeIf { it > 0 }
            val urlAppId = SteamApiClient.extractSteamAppId(item.openGiveawayUrl)
            val launcherAppIds = item.availableLaunchers.mapNotNullTo(HashSet()) { SteamApiClient.extractSteamAppId(it.url) }
            val normItem = SteamApiClient.normalizeGameTitle(item.title)
            val cleanItem = MetacriticCatalog.cleanTitle(item.title).lowercase()

            steamGamesList.firstOrNull { steamGame ->
                val appId = steamGame.appId
                (itemAppId != null && itemAppId == appId) ||
                    (urlAppId == appId) ||
                    (appId in launcherAppIds) ||
                    (normItem.length >= 3 && SteamApiClient.normalizeGameTitle(steamGame.name) == normItem) ||
                    MetacriticCatalog.cleanTitle(steamGame.name).lowercase().let { cleanSteam ->
                        cleanItem == cleanSteam ||
                            (cleanItem.length > 5 && cleanSteam.contains(cleanItem)) ||
                            (cleanSteam.length > 5 && cleanItem.contains(cleanSteam))
                    }
            }
        } else null

        val inSteam = matchedSteam != null
        val isOwned = inVault || inSteam
        val label = when {
            inVault && inSteam -> "ALREADY IN YOUR VAULT & STEAM"
            inVault -> "ALREADY IN YOUR VAULT"
            inSteam -> "ALREADY IN YOUR STEAM LIBRARY"
            else -> ""
        }

        return GiveawayOwnershipStatus(
            isOwned = isOwned,
            inVault = inVault,
            inSteam = inSteam,
            matchedSteamGameName = matchedSteam?.name,
            badgeLabel = label
        )
    }

    fun getLauncherOptionsFor(
        item: GiveawayItem,
        allGiveaways: List<GiveawayItem>
    ): List<LauncherOption> {
        val baseOptions = item.availableLaunchers.toMutableList()
        val cleanTitle = item.title.trim().lowercase()
            .replace(CLEAN_TITLE_LAUNCHER_REGEX, "")
            .trim()

        if (cleanTitle.length >= 3) {
            for (other in allGiveaways) {
                if (other.id != item.id && other.title.trim().lowercase().contains(cleanTitle)) {
                    for (opt in other.availableLaunchers) {
                        if (opt.launcherType != LauncherType.OTHER && baseOptions.none { it.launcherType == opt.launcherType }) {
                            baseOptions.add(opt)
                        }
                    }
                }
            }
        }
        return baseOptions
    }
}
