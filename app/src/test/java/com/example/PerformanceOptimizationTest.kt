package com.example

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SteamAchievementEntity
import com.example.data.local.SteamGameEntity
import com.example.data.model.BacklogChallengeCategory
import com.example.data.model.BacklogChallengeGrid
import com.example.data.model.BacklogChallengeTile
import com.example.data.model.GiveawayItem
import com.example.data.model.LauncherType
import com.example.data.model.PlayStatus
import com.example.data.model.UserProfile
import com.example.i18n.LocalAppStrings
import com.example.i18n.StringsEn
import com.example.preferences.AppPreferences
import com.example.ui.AchievementOverviewStats
import com.example.ui.GiveawayFilterEngine
import com.example.ui.MainTab
import com.example.ui.UiStats
import com.example.ui.calendar.ExpirationCalendarUtils
import com.example.ui.components.BacklogChallengeGridDialog
import com.example.ui.components.DirectClaimAnimation
import com.example.ui.components.ExpressiveFloatingNavBar
import com.example.ui.components.ExpressiveTopActionBar
import com.example.ui.components.ProfileDialog
import com.example.ui.components.SavingsTrackerAnimation
import com.example.ui.components.StatsBanner
import com.example.ui.components.SteamTrackerBanner
import com.example.ui.components.TrophyShowcaseContent
import com.example.ui.theme.LootDropTheme
import com.example.util.HardwareReadinessCatalog
import com.example.util.LocalLowEndOptimization
import com.example.util.MetacriticCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PerformanceOptimizationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun sampleGiveaway(
        id: Int,
        title: String = "Portal 2",
        worth: Double = 19.99,
        platforms: String = "PC, Steam",
        endDate: String = "2026-12-31 12:00:00",
        isClaimed: Boolean = false,
        isNew: Boolean = false,
        playStatus: PlayStatus = PlayStatus.NOT_STARTED,
        steamAppId: Int? = 620
    ) = GiveawayItem(
        id = id,
        title = title,
        worth = "$$worth",
        numericWorth = worth,
        thumbnail = "https://example.com/thumb_$id.jpg",
        image = "https://example.com/img_$id.jpg",
        description = "Test giveaway $id",
        instructions = "Redeem on store",
        openGiveawayUrl = "https://store.steampowered.com/app/620",
        publishedDate = "2026-10-01 00:00:00",
        type = "Game",
        platforms = platforms,
        endDate = endDate,
        users = 1500,
        status = "Active",
        isClaimed = isClaimed,
        isNew = isNew,
        playStatus = playStatus,
        steamAppId = steamAppId
    )

    @Test
    fun `computeUiStats handles empty list and fallback hardware metrics in single pass`() {
        val emptyStats = GiveawayFilterEngine.computeUiStats(emptyList(), emptyList())
        assertEquals(0, emptyStats.activeGiveawaysCount)
        assertEquals(0, emptyStats.claimedCount)
        assertEquals(0.0, emptyStats.totalFreeWorthUsd, 0.001)
        assertEquals(0, emptyStats.deckVerifiedCount)

        // When no giveaways are claimed, hardware counters fall back to all items
        val unclaimedPortal = sampleGiveaway(
            id = 1,
            title = "Portal 2",
            worth = 19.99,
            platforms = "PC, Steam",
            endDate = "2026-11-30 10:00:00",
            isClaimed = false,
            isNew = true,
            steamAppId = 620
        )
        val unclaimedEpic = sampleGiveaway(
            id = 2,
            title = "Hades",
            worth = 24.99,
            platforms = "Epic Games Store, PC",
            endDate = "N/A",
            isClaimed = false,
            isNew = false,
            steamAppId = 1145360
        )
        val fallbackStats = GiveawayFilterEngine.computeUiStats(
            all = listOf(unclaimedPortal, unclaimedEpic),
            steamGames = emptyList()
        )
        assertEquals(2, fallbackStats.activeGiveawaysCount)
        assertEquals(44.98, fallbackStats.totalFreeWorthUsd, 0.01)
        assertEquals(1, fallbackStats.newFreebiesCount)
        assertEquals(1, fallbackStats.expiringSoon)
        assertEquals(2, fallbackStats.platformCounts[LauncherType.ALL])
        assertEquals(1, fallbackStats.platformCounts[LauncherType.STEAM])
        assertEquals(1, fallbackStats.platformCounts[LauncherType.EPIC])
        assertEquals(2, fallbackStats.deckVerifiedCount)

        // Once an item is claimed, hardware counters reflect claimed subset only
        val claimedOnlyStats = GiveawayFilterEngine.computeUiStats(
            all = listOf(unclaimedPortal.copy(isClaimed = true, playStatus = PlayStatus.PLAYING), unclaimedEpic),
            steamGames = listOf(
                SteamGameEntity(
                    appId = 620,
                    name = "Portal 2",
                    playtimeMinutes = 300,
                    playStatus = PlayStatus.BEATEN,
                    achievementsUnlocked = 51,
                    achievementsTotal = 51
                )
            )
        )
        assertEquals(1, claimedOnlyStats.claimedCount)
        assertEquals(19.99, claimedOnlyStats.claimedTotalSavedUsd, 0.01)
        assertEquals(1, claimedOnlyStats.playingCount)
        assertEquals(1, claimedOnlyStats.deckVerifiedCount)
        assertEquals(1, claimedOnlyStats.steamTotalCount)
        assertEquals(1, claimedOnlyStats.steamFinishedCount)
    }

    @Test
    fun `computeAchievementOverviewStats aggregates single-pass game and trophy counters accurately`() {
        val games = listOf(
            SteamGameEntity(appId = 1, name = "Mastered", playtimeMinutes = 120, achievementsUnlocked = 20, achievementsTotal = 20),
            SteamGameEntity(appId = 2, name = "Almost", playtimeMinutes = 90, achievementsUnlocked = 9, achievementsTotal = 10),
            SteamGameEntity(appId = 3, name = "Early", playtimeMinutes = 30, achievementsUnlocked = 2, achievementsTotal = 10),
            SteamGameEntity(appId = 4, name = "NoAch", playtimeMinutes = 10, achievementsUnlocked = 0, achievementsTotal = 0)
        )
        val trophies = listOf(
            SteamAchievementEntity(appId = 1, apiName = "m1", displayName = "Mythic", description = "", isAchieved = true, globalPercentage = 1.8f),
            SteamAchievementEntity(appId = 2, apiName = "g1", displayName = "Gold", description = "", isAchieved = true, globalPercentage = 11.5f),
            SteamAchievementEntity(appId = 3, apiName = "b1", displayName = "Bronze", description = "", isAchieved = true, globalPercentage = 55.0f)
        )

        val stats = GiveawayFilterEngine.computeAchievementOverviewStats(trophies, games)
        assertEquals(3, stats.totalUnlockedTrophies)
        assertEquals(1, stats.mythicTrophiesCount)
        assertEquals(1, stats.goldTrophiesCount)
        assertEquals(1, stats.masteredGamesCount)
        assertEquals(1, stats.closestToHundredCount)
        // Average of 100%, 90%, 20% = 210 / 3 = 70
        assertEquals(70, stats.averageCompletionPercentage)
    }

    @Test
    fun `metacritic hardware and calendar caches populate invalidate and clear safely`() {
        MetacriticCatalog.clearCaches()
        val initialScore = MetacriticCatalog.resolveScore("Portal 2", "Game", "Co-op puzzle", 620)
        assertEquals(95, initialScore)

        // Register dynamic override for a custom title and confirm cache invalidation updates it immediately
        val customTitle = "LootDrop Synthetic Optimizer Quest"
        MetacriticCatalog.registerScore(customTitle, 81)
        assertEquals(81, MetacriticCatalog.resolveScore(customTitle))
        MetacriticCatalog.registerScore(customTitle, 92)
        assertEquals(92, MetacriticCatalog.resolveScore(customTitle))
        MetacriticCatalog.clearCaches()
        assertEquals(92, MetacriticCatalog.resolveScore(customTitle))

        HardwareReadinessCatalog.clearCaches()
        val item = sampleGiveaway(id = 42, title = "Portal 2", steamAppId = 620)
        val p1 = HardwareReadinessCatalog.resolveProfile(item)
        val p2 = HardwareReadinessCatalog.resolveProfile(item)
        assertEquals(p1, p2)
        HardwareReadinessCatalog.clearCaches()
        assertEquals(p1, HardwareReadinessCatalog.resolveProfile(item))

        ExpirationCalendarUtils.clearCache()
        assertEquals(LocalDate.of(2026, 12, 25), ExpirationCalendarUtils.parseExpirationDate("2026-12-25 18:00:00"))
        assertEquals(LocalDate.of(2026, 12, 25), ExpirationCalendarUtils.parseExpirationDate("2026-12-25T18:00:00"))
        assertNull(ExpirationCalendarUtils.parseExpirationDate("not-a-date"))
        assertNull(ExpirationCalendarUtils.parseExpirationDate("not-a-date"))
        assertNull(ExpirationCalendarUtils.parseExpirationDate("N/A"))
        ExpirationCalendarUtils.clearCache()
    }

    @Test
    fun `trimMemoryCaches and lowEndOptimization preference work without crashing`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val prefs = AppPreferences(app)
        prefs.isLowEndOptimization = true
        assertTrue(prefs.isLowEndOptimization)
        val trimmed = LootDropApplication.trimMemoryCaches(app, aggressive = true)
        assertNotNull(trimmed)
        prefs.isLowEndOptimization = false
    }

    @Test
    fun `chrome banners and challenge dialog render intact under LocalLowEndOptimization`() {
        val stats = UiStats(
            totalFreeWorthUsd = 149.95,
            activeGiveawaysCount = 7,
            newFreebiesCount = 2,
            claimedTotalSavedUsd = 49.99,
            claimedCount = 3,
            steamTotalCount = 12
        )
        val grid = BacklogChallengeGrid(
            weekKey = "2026-W47",
            weekTitle = "Week 47",
            daysRemaining = 4,
            tiles = listOf(
                BacklogChallengeTile(
                    id = "tile_0",
                    index = 0,
                    title = "Finish 1 Game",
                    description = "Complete any game",
                    emoji = "🏆",
                    category = BacklogChallengeCategory.COMPLETION,
                    isCompleted = true
                )
            ),
            isBlackout = true,
            completedCount = 1,
            totalLinesCompleted = 0
        )

        composeTestRule.setContent {
            LootDropTheme {
                CompositionLocalProvider(
                    LocalLowEndOptimization provides true,
                    LocalAppStrings provides StringsEn
                ) {
                    Column {
                        ExpressiveTopActionBar(
                            isSearchExpanded = false,
                            searchQuery = "",
                            onToggleSearch = {},
                            activeFilterCount = 2,
                            onFilterClick = {},
                            unreadNotificationCount = 3,
                            onNotificationClick = {},
                            isAnimationsEnabled = false,
                            isLowEndOptimization = true
                        )
                        StatsBanner(
                            stats = stats,
                            isRefreshing = true,
                            onRefresh = {},
                            isAnimationsEnabled = false
                        )
                        SteamTrackerBanner(
                            isSyncing = true,
                            steamUserId = "76561198000000000",
                            onOpenDialog = {},
                            onQuickSync = {}
                        )
                        ExpressiveFloatingNavBar(
                            selectedTab = MainTab.HOME,
                            onTabSelected = {},
                            claimedCount = 3,
                            expiringSoonCount = 2,
                            isLowEndOptimization = true
                        )
                        BacklogChallengeGridDialog(
                            grid = grid,
                            onDismiss = {},
                            onToggleManualTile = { false },
                            onRefreshCheck = {},
                            onClaimMilestones = {},
                            onEquipTheme = {}
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag("top_action_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("stats_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_nav_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("steam_quick_sync_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bingo_tile_0").assertIsDisplayed()
    }

    @Test
    fun `trophy showcase profile dialog and onboarding animations render intact under LocalLowEndOptimization`() {
        val trophies = listOf(
            SteamAchievementEntity(
                appId = 620,
                apiName = "ach_mythic",
                displayName = "Portal Master",
                description = "Rare completion",
                isAchieved = true,
                globalPercentage = 2.4f,
                gameName = "Portal 2"
            ),
            SteamAchievementEntity(
                appId = 620,
                apiName = "ach_gold",
                displayName = "Chamber Ace",
                description = "Gold completion",
                isAchieved = true,
                globalPercentage = 10.2f,
                gameName = "Portal 2"
            )
        )
        val overview = AchievementOverviewStats(
            totalUnlockedTrophies = 2,
            mythicTrophiesCount = 1,
            goldTrophiesCount = 1,
            masteredGamesCount = 1,
            closestToHundredCount = 0,
            averageCompletionPercentage = 100
        )

        composeTestRule.setContent {
            LootDropTheme {
                CompositionLocalProvider(
                    LocalLowEndOptimization provides true,
                    LocalAppStrings provides StringsEn
                ) {
                    Column {
                        Row {
                            Box(modifier = Modifier.size(140.dp)) {
                                SavingsTrackerAnimation()
                            }
                            Box(modifier = Modifier.size(140.dp)) {
                                DirectClaimAnimation()
                            }
                        }
                        TrophyShowcaseContent(
                            overviewStats = overview,
                            rarestTrophies = trophies,
                            closestGames = listOf(
                                SteamGameEntity(
                                    appId = 620,
                                    name = "Portal 2",
                                    playtimeMinutes = 240,
                                    achievementsUnlocked = 45,
                                    achievementsTotal = 51
                                )
                            ),
                            isSyncing = true,
                            steamUserId = "76561198000000000",
                            onSyncClick = {},
                            onOpenSteamSettings = {},
                            onSelectGameForAchievements = {},
                            isAnimationsEnabled = false,
                            isLowEndOptimization = true,
                            modifier = Modifier.height(350.dp)
                        )
                        ProfileDialog(
                            profile = UserProfile(name = "PlayerOne"),
                            uiStats = UiStats(claimedCount = 4, claimedTotalSavedUsd = 59.96, steamTotalCount = 10),
                            steamUserId = "76561198000000000",
                            steamTrophies = trophies,
                            isSyncingSteam = false,
                            onSaveProfile = { _, _, _ -> },
                            onReplayTour = {},
                            onDismiss = {}
                        )
                    }
                }
            }
        }

        // Low-end SavingsTrackerAnimation displays target saved total immediately without waiting on loops
        composeTestRule.onNodeWithText("$489.92").assertIsDisplayed()
        composeTestRule.onNodeWithTag("trophy_showcase_list").assertIsDisplayed()
        composeTestRule.onNodeWithTag("gamer_profile_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_steam_trophies_card").performScrollTo().assertIsDisplayed()
    }
}
