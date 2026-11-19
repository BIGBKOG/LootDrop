package com.example.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.SteamAchievementEntity
import com.example.data.local.SteamGameEntity
import com.example.data.model.PlayStatus
import com.example.data.model.TrophyRarityTier
import com.example.data.model.UnifiedLibraryItem
import com.example.preferences.AccentTheme
import com.example.preferences.AppPreferences
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BacklogChallengeManagerTest {

    private lateinit var context: Context
    private lateinit var preferences: AppPreferences
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        preferences = AppPreferences(context)
        database = AppDatabase.getInstance(context)
    }

    @Test
    fun testGridGenerationContainsNineTilesAndMarksWeekOne() {
        val weekKey = "2026-W46"
        val grid = BacklogChallengeManager.getOrGenerateGrid(preferences, weekKey)
        assertEquals(9, grid.tiles.size)
        assertEquals("2026-W46", grid.weekKey)
        assertTrue("Week title should be Week 1", grid.weekTitle.startsWith("Week 1"))
    }

    @Test
    fun testCurrentWeekIsWeekOneAndFollowingWeeksIncrement() {
        val currentWeek = "2026-W46"
        val weekTwo = "2026-W47"
        val weekThree = "2026-W48"

        assertEquals(1, BacklogChallengeManager.getRelativeWeekNumber(currentWeek))
        assertEquals(2, BacklogChallengeManager.getRelativeWeekNumber(weekTwo))
        assertEquals(3, BacklogChallengeManager.getRelativeWeekNumber(weekThree))

        assertTrue(BacklogChallengeManager.getWeekDisplayName(currentWeek).startsWith("Week 1"))
        assertTrue(BacklogChallengeManager.getWeekDisplayName(weekTwo).startsWith("Week 2"))
        assertTrue(BacklogChallengeManager.getWeekDisplayName(weekThree).startsWith("Week 3"))
    }

    @Test
    fun testNavigationBoundariesOnlyAllowSeeingNextTwoWeeks() {
        val week1 = "2026-W46" // Anchor / current week
        val week2 = "2026-W47" // Next week (+1)
        val week3 = "2026-W48" // Next 2 weeks (+2, maximum allowed preview)

        // At Week 1, cannot navigate to past weeks prior to Week 1
        assertEquals("", BacklogChallengeManager.getPreviousWeekKey(week1))
        // At Week 1, can navigate to Week 2
        assertEquals(week2, BacklogChallengeManager.getNextWeekKey(week1))

        // At Week 2, can navigate back to Week 1 and forward to Week 3
        assertEquals(week1, BacklogChallengeManager.getPreviousWeekKey(week2))
        assertEquals(week3, BacklogChallengeManager.getNextWeekKey(week2))

        // At Week 3 (the 2nd week ahead), previous is Week 2, but next must be empty to limit preview to next 2 weeks!
        assertEquals(week2, BacklogChallengeManager.getPreviousWeekKey(week3))
        assertEquals("", BacklogChallengeManager.getNextWeekKey(week3))
    }

    @Test
    fun testWeeklyAutomaticRefreshGeneratesFreshChallenges() {
        // Week 1: 2026-W46
        val gridWeek1 = BacklogChallengeManager.getOrGenerateGrid(preferences, "2026-W46")
        // Week 2: 2026-W47
        val gridWeek2 = BacklogChallengeManager.getOrGenerateGrid(preferences, "2026-W47")

        assertEquals(9, gridWeek1.tiles.size)
        assertEquals(9, gridWeek2.tiles.size)

        val week1Types = gridWeek1.tiles.map { it.challengeType }
        val week2Types = gridWeek2.tiles.map { it.challengeType }

        // Challenges should differ between weeks (weekly auto-refresh)
        assertNotEquals(week1Types, week2Types)

        // Calling same week again is completely deterministic and stable
        val gridWeek1Again = BacklogChallengeManager.getOrGenerateGrid(preferences, "2026-W46")
        assertEquals(week1Types, gridWeek1Again.tiles.map { it.challengeType })
    }

    @Test
    fun testManualOverrideLimitIsEnforcedAtTwo() {
        val weekKey = "2026-W46"

        // 1st manual unlock succeeds
        val firstToggle = BacklogChallengeManager.toggleManualOverride(preferences, weekKey, 0)
        assertTrue("First manual unlock should succeed", firstToggle)

        // 2nd manual unlock succeeds
        val secondToggle = BacklogChallengeManager.toggleManualOverride(preferences, weekKey, 1)
        assertTrue("Second manual unlock should succeed", secondToggle)

        // 3rd manual unlock FAILS because user is only allowed 2 manual unlocks per week
        val thirdToggle = BacklogChallengeManager.toggleManualOverride(preferences, weekKey, 2)
        assertFalse("Third manual unlock must be rejected (limit 2)", thirdToggle)

        val grid = BacklogChallengeManager.getOrGenerateGrid(preferences, weekKey)
        assertTrue(grid.tiles[0].manualOverrideDone)
        assertTrue(grid.tiles[1].manualOverrideDone)
        assertFalse(grid.tiles[2].manualOverrideDone)
        assertEquals(2, grid.manualOverridesUsed)
        assertEquals(0, grid.remainingManualOverrides)
        assertFalse(grid.canManualOverrideMore)

        // Untoggling tile 1 frees up a manual unlock slot
        val untoggle = BacklogChallengeManager.toggleManualOverride(preferences, weekKey, 1)
        assertTrue("Untoggling an active manual tile should succeed", untoggle)

        val gridAfterUntoggle = BacklogChallengeManager.getOrGenerateGrid(preferences, weekKey)
        assertFalse(gridAfterUntoggle.tiles[1].manualOverrideDone)
        assertEquals(1, gridAfterUntoggle.manualOverridesUsed)
        assertEquals(1, gridAfterUntoggle.remainingManualOverrides)
        assertTrue(gridAfterUntoggle.canManualOverrideMore)

        // Now toggling tile 2 succeeds because a slot was freed up
        val retryTileTwo = BacklogChallengeManager.toggleManualOverride(preferences, weekKey, 2)
        assertTrue("Toggling after freeing slot should succeed", retryTileTwo)
    }

    @Test
    fun testManualOverrideAndLineCompletion() = runBlocking {
        val weekKey = "2026-W46"
        val testGame = UnifiedLibraryItem.Steam(
            SteamGameEntity(
                appId = 12345,
                name = "Celeste",
                playStatus = PlayStatus.BEATEN,
                playtimeMinutes = 180,
                achievementsUnlocked = 10,
                achievementsTotal = 10
            )
        )
        val testTrophy = SteamAchievementEntity(
            appId = 12345,
            apiName = "ACH_TEST_GOLD",
            displayName = "Champion",
            description = "Won all challenges",
            isAchieved = true,
            unlockTimestamp = System.currentTimeMillis() / 1000L,
            globalPercentage = 4.0f,
            iconUrl = "",
            gameName = "Celeste"
        )

        // Evaluate grid with game data - at least one tile will be auto-completed (e.g. BEAT_GAME or IMMERSION_SESSION)
        val initialGrid = BacklogChallengeManager.getOrGenerateGrid(
            preferences = preferences,
            weekKey = weekKey,
            games = listOf(testGame),
            trophies = listOf(testTrophy)
        )

        val autoCompletedTile = initialGrid.tiles.firstOrNull { it.isCompleted }
        assertNotNull("Should have at least one auto-completed tile with beaten game and trophy data", autoCompletedTile)
        val autoIndex = autoCompletedTile!!.index
        val targetRow = autoIndex / 3

        // Use our 2 allowed manual unlocks on the remaining 2 tiles in this same row
        val rowTiles = listOf(targetRow * 3, targetRow * 3 + 1, targetRow * 3 + 2)
        val otherTwoTiles = rowTiles.filter { it != autoIndex }
        assertEquals(2, otherTwoTiles.size)

        val toggleFirst = BacklogChallengeManager.toggleManualOverride(preferences, weekKey, otherTwoTiles[0])
        val toggleSecond = BacklogChallengeManager.toggleManualOverride(preferences, weekKey, otherTwoTiles[1])
        assertTrue("First manual unlock in row should succeed", toggleFirst)
        assertTrue("Second manual unlock in row should succeed", toggleSecond)

        val updatedGrid = BacklogChallengeManager.getOrGenerateGrid(
            preferences = preferences,
            weekKey = weekKey,
            games = listOf(testGame),
            trophies = listOf(testTrophy)
        )

        assertTrue(updatedGrid.tiles[rowTiles[0]].isCompleted)
        assertTrue(updatedGrid.tiles[rowTiles[1]].isCompleted)
        assertTrue(updatedGrid.tiles[rowTiles[2]].isCompleted)
        assertTrue(updatedGrid.completedRows.contains(targetRow))
        assertTrue(updatedGrid.totalLinesCompleted >= 1)
        assertEquals(2, updatedGrid.manualOverridesUsed)
        assertEquals(0, updatedGrid.remainingManualOverrides)
        assertFalse(updatedGrid.isBlackout)

        // Backlog Slayer milestone should be unlocked because a line was completed
        val slayerMilestone = updatedGrid.availableMilestones.first { it.id.contains(BacklogChallengeManager.MILESTONE_BACKLOG_SLAYER) }
        assertTrue(slayerMilestone.isUnlocked)

        // Award milestones
        val awarded = BacklogChallengeManager.checkAndAwardMilestones(updatedGrid, database, preferences)
        assertTrue(awarded.any { it.id.contains(BacklogChallengeManager.MILESTONE_BACKLOG_SLAYER) })

        // Theme should now be unlocked
        assertTrue(preferences.isAccentThemeUnlocked(AccentTheme.BACKLOG_SLAYER))

        // Trophy should be inserted into achievements
        val unlockedTrophies = database.steamAchievementDao().getAllUnlockedAchievements()
        val slayerTrophy = unlockedTrophies.firstOrNull { it.apiName == "ACH_BACKLOG_SLAYER" }
        assertNotNull(slayerTrophy)
        assertEquals(BacklogChallengeManager.APP_ID_BACKLOG_CHALLENGE, slayerTrophy?.appId)
    }

    @Test
    fun testAutoEvaluationWithBeatenGame() {
        val weekKey = "2026-W46"
        val beatenGame = UnifiedLibraryItem.Steam(
            SteamGameEntity(
                appId = 12345,
                name = "Celeste",
                playStatus = PlayStatus.BEATEN,
                playtimeMinutes = 180
            )
        )
        val grid = BacklogChallengeManager.getOrGenerateGrid(
            preferences = preferences,
            weekKey = weekKey,
            games = listOf(beatenGame)
        )

        // Find BEAT_GAME or IMMERSION_SESSION tile if present in this week
        val beatGameTile = grid.tiles.firstOrNull { it.challengeType == "BEAT_GAME" }
        if (beatGameTile != null) {
            assertTrue(beatGameTile.isCompleted)
        }

        val immersionTile = grid.tiles.firstOrNull { it.challengeType == "IMMERSION_SESSION" }
        if (immersionTile != null) {
            assertTrue(immersionTile.isCompleted)
        }
    }

    @Test
    fun testExpandedMilestonesAndRewardThemes() = runBlocking {
        val weekKey = "2026-W50"

        // All 7 reward themes start locked
        assertFalse(preferences.isAccentThemeUnlocked(AccentTheme.BACKLOG_SLAYER))
        assertFalse(preferences.isAccentThemeUnlocked(AccentTheme.VAULT_CLEANER))
        assertFalse(preferences.isAccentThemeUnlocked(AccentTheme.NEBULA_BLACKOUT))
        assertFalse(preferences.isAccentThemeUnlocked(AccentTheme.PRISM_DRIFT))
        assertFalse(preferences.isAccentThemeUnlocked(AccentTheme.SOLAR_NOVA))
        assertFalse(preferences.isAccentThemeUnlocked(AccentTheme.TOXIC_OVERDRIVE))
        assertFalse(preferences.isAccentThemeUnlocked(AccentTheme.AURORA_SOVEREIGN))

        // Base grid exposes all 10 milestone tiers
        val emptyGrid = BacklogChallengeManager.getOrGenerateGrid(preferences, weekKey)
        assertEquals(10, emptyGrid.availableMilestones.size)

        // Set up rich library & 5 trophies so several tiles auto-complete
        val richGame = UnifiedLibraryItem.Steam(
            SteamGameEntity(
                appId = 620,
                name = "Hades",
                playStatus = PlayStatus.BEATEN,
                playtimeMinutes = 240,
                achievementsUnlocked = 10,
                achievementsTotal = 12
            )
        )
        val secondGame = UnifiedLibraryItem.Steam(
            SteamGameEntity(
                appId = 504230,
                name = "Celeste",
                playStatus = PlayStatus.PLAYING,
                playtimeMinutes = 90,
                achievementsUnlocked = 8,
                achievementsTotal = 10
            )
        )
        val sampleTrophies = (1..5).map { idx ->
            SteamAchievementEntity(
                appId = 620,
                apiName = "ACH_HADES_$idx",
                displayName = "Hades Trophy $idx",
                description = "Earned trophy $idx",
                isAchieved = true,
                unlockTimestamp = System.currentTimeMillis() / 1000L,
                globalPercentage = if (idx == 1) 3.2f else 9.0f,
                iconUrl = "",
                gameName = "Hades"
            )
        }
        val gridWithStats = BacklogChallengeManager.getOrGenerateGrid(
            preferences = preferences,
            weekKey = weekKey,
            games = listOf(richGame, secondGame),
            trophies = sampleTrophies
        )
        assertTrue(gridWithStats.completedCount >= 2)

        val momentum = gridWithStats.availableMilestones.first { it.id.startsWith(BacklogChallengeManager.MILESTONE_MOMENTUM_SPARK) }
        assertTrue("Momentum Spark should unlock at >= 2 completed tiles", momentum.isUnlocked)

        // Check a diagonal (0,4,8) or anti-diagonal (2,4,6) that needs at most 2 manual unlocks
        val mainDiagPending = listOf(0, 4, 8).filter { !gridWithStats.tiles[it].isCompleted }
        val antiDiagPending = listOf(2, 4, 6).filter { !gridWithStats.tiles[it].isCompleted }
        val chosenPending = if (mainDiagPending.size <= 2) mainDiagPending else antiDiagPending
        assertTrue("At least one diagonal should be completable within 2 manual overrides", chosenPending.size <= 2)

        chosenPending.forEach { BacklogChallengeManager.toggleManualOverride(preferences, weekKey, it) }
        val diagGrid = BacklogChallengeManager.getOrGenerateGrid(
            preferences = preferences,
            weekKey = weekKey,
            games = listOf(richGame, secondGame),
            trophies = sampleTrophies
        )
        assertTrue(diagGrid.completedDiagonals.isNotEmpty())
        val diagonalMilestone = diagGrid.availableMilestones.first { it.id.startsWith(BacklogChallengeManager.MILESTONE_DIAGONAL_DRIFTER) }
        assertTrue("Diagonal Drifter should unlock on diagonal completion", diagonalMilestone.isUnlocked)

        BacklogChallengeManager.checkAndAwardMilestones(diagGrid, database, preferences)
        assertTrue("Prism Drift theme should unlock on Diagonal Drifter claim", preferences.isAccentThemeUnlocked(AccentTheme.PRISM_DRIFT))
        val unlocked = database.steamAchievementDao().getAllUnlockedAchievements()
        assertTrue(unlocked.any { it.apiName == "ACH_MOMENTUM_SPARK" })
        assertTrue(unlocked.any { it.apiName == "ACH_DIAGONAL_DRIFTER" })
    }
}
