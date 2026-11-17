package com.example.ui

import androidx.compose.runtime.Immutable
import com.example.data.model.GameCategory
import com.example.data.model.LauncherType
import com.example.data.model.formatStorageGb

enum class MainTab {
    HOME,
    DISCOVER,
    CLAIMED,
    CALENDAR,
    SETTINGS
}

enum class SortOption(val label: String) {
    NEWEST("Newest First"),
    HIGHEST_METACRITIC("Metascore (Top Rated)"),
    HIGHEST_VALUE("Highest Value ($)"),
    MOST_POPULAR("Most Claimed"),
    ENDING_SOON("Ending Soon")
}

enum class TypeFilter(val label: String, val apiMatch: String?) {
    ALL("All Loot", null),
    FULL_GAMES("Full Games", "Game"),
    DLC_LOOT("DLC & Loot", "DLC"),
    EARLY_ACCESS("Beta / Early", "Early Access")
}

data class FilterCriteria(
    val tab: MainTab = MainTab.HOME,
    val launchers: Set<LauncherType> = emptySet(),
    val typeFilter: TypeFilter = TypeFilter.ALL,
    val sort: SortOption = SortOption.NEWEST,
    val query: String = ""
)

@Immutable
data class AchievementOverviewStats(
    val totalUnlockedTrophies: Int = 0,
    val mythicTrophiesCount: Int = 0,
    val goldTrophiesCount: Int = 0,
    val masteredGamesCount: Int = 0,
    val closestToHundredCount: Int = 0,
    val averageCompletionPercentage: Int = 0
)

@Immutable
data class UiStats(
    val totalFreeWorthUsd: Double = 0.0,
    val activeGiveawaysCount: Int = 0,
    val newFreebiesCount: Int = 0,
    val platformCounts: Map<LauncherType, Int> = emptyMap(),
    val claimedTotalSavedUsd: Double = 0.0,
    val claimedCount: Int = 0,
    val expiringSoon: Int = 0,
    val unplayedCount: Int = 0,
    val playingCount: Int = 0,
    val finishedCount: Int = 0,
    val droppedCount: Int = 0,
    val steamTotalCount: Int = 0,
    val steamUnplayedCount: Int = 0,
    val steamPlayingCount: Int = 0,
    val steamFinishedCount: Int = 0,
    val steamDroppedCount: Int = 0,
    val deckVerifiedCount: Int = 0,
    val controllerFriendlyCount: Int = 0,
    val quickSessionCount: Int = 0,
    val storageUnder2GbCount: Int = 0,
    val storage2To10GbCount: Int = 0,
    val storage10To40GbCount: Int = 0,
    val storageOver40GbCount: Int = 0,
    val claimedTotalStorageGb: Float = 0f,
    val steamTotalStorageGb: Float = 0f
) {
    val claimedStorageFormatted: String get() = formatStorageGb(claimedTotalStorageGb)
    val steamStorageFormatted: String get() = formatStorageGb(steamTotalStorageGb)
}

data class GiveawayOwnershipStatus(
    val isOwned: Boolean = false,
    val inVault: Boolean = false,
    val inSteam: Boolean = false,
    val matchedSteamGameName: String? = null,
    val badgeLabel: String = ""
)
