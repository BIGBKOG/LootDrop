package com.example.util

import com.example.data.local.AppDatabase
import com.example.data.local.SteamAchievementEntity
import com.example.data.model.BacklogChallengeCategory
import com.example.data.model.BacklogChallengeGrid
import com.example.data.model.BacklogChallengeTile
import com.example.data.model.BacklogMilestoneReward
import com.example.data.model.BacklogMilestoneType
import com.example.data.model.GameCategory
import com.example.data.model.PlayStatus
import com.example.data.model.TrophyRarityTier
import com.example.data.model.UnifiedLibraryItem
import com.example.preferences.AccentTheme
import com.example.preferences.AppPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Random

object BacklogChallengeManager {

    const val APP_ID_BACKLOG_CHALLENGE = 777000

    const val MILESTONE_ROW_RAIDER = "milestone_row_raider"
    const val MILESTONE_COLUMN_CONQUEROR = "milestone_column_conqueror"
    const val MILESTONE_BACKLOG_SLAYER = "milestone_backlog_slayer"
    const val MILESTONE_VAULT_CLEANER = "milestone_vault_cleaner"
    const val MILESTONE_BLACKOUT_CHAMPION = "milestone_blackout_champion"

    data class ChallengeBlueprint(
        val typeKey: String,
        val title: String,
        val description: String,
        val emoji: String,
        val category: BacklogChallengeCategory,
        val targetRequirement: Int = 1
    )

    private val CHALLENGE_CATALOG = listOf(
        ChallengeBlueprint(
            typeKey = "GOLD_TROPHY",
            title = "Unlock a Gold Trophy",
            description = "Earn any Gold or Mythic tier Steam trophy (<15% global unlock rate).",
            emoji = "🏆",
            category = BacklogChallengeCategory.TROPHY,
            targetRequirement = 1
        ),
        ChallengeBlueprint(
            typeKey = "INDIE_GAME",
            title = "Play an Indie Game",
            description = "Play or advance an indie title in your collection for 20+ mins or beat it.",
            emoji = "🕹️",
            category = BacklogChallengeCategory.GENRE,
            targetRequirement = 1
        ),
        ChallengeBlueprint(
            typeKey = "IMMERSION_SESSION",
            title = "1-Hour Immersion",
            description = "Play an unplayed or classic backlog game for 60+ minutes.",
            emoji = "⏳",
            category = BacklogChallengeCategory.PLAYTIME,
            targetRequirement = 1
        ),
        ChallengeBlueprint(
            typeKey = "VAULT_STARTER",
            title = "Vault Starter",
            description = "Start or play a claimed free game from your LootDrop Vault.",
            emoji = "📦",
            category = BacklogChallengeCategory.EXPLORATION,
            targetRequirement = 1
        ),
        ChallengeBlueprint(
            typeKey = "BEAT_GAME",
            title = "Beat a Game",
            description = "Finish any backlog game and mark it as Beaten in your library!",
            emoji = "👑",
            category = BacklogChallengeCategory.COMPLETION,
            targetRequirement = 1
        ),
        ChallengeBlueprint(
            typeKey = "CONTROLLER_READY",
            title = "Controller Ready",
            description = "Play any game with verified Controller support for 25+ mins.",
            emoji = "🎮",
            category = BacklogChallengeCategory.GENRE,
            targetRequirement = 1
        ),
        ChallengeBlueprint(
            typeKey = "TROPHY_COLLECTOR",
            title = "Trophy Collector",
            description = "Unlock 3 total trophies across any games in your collection.",
            emoji = "💎",
            category = BacklogChallengeCategory.TROPHY,
            targetRequirement = 3
        ),
        ChallengeBlueprint(
            typeKey = "MYTHIC_HUNTER",
            title = "Mythic Hunter",
            description = "Unlock an ultra-rare Mythic trophy (<5% global unlock rate).",
            emoji = "⚡",
            category = BacklogChallengeCategory.TROPHY,
            targetRequirement = 1
        ),
        ChallengeBlueprint(
            typeKey = "TRI_PLAY",
            title = "Backlog Tri-Play",
            description = "Play or log playtime in 2 different games from your library this week.",
            emoji = "🌟",
            category = BacklogChallengeCategory.EXPLORATION,
            targetRequirement = 2
        ),
        ChallengeBlueprint(
            typeKey = "METACRITIC_80",
            title = "Critically Acclaimed",
            description = "Play a high-rated title with Metacritic 80+ for at least 30 minutes.",
            emoji = "🎖️",
            category = BacklogChallengeCategory.PLAYTIME,
            targetRequirement = 1
        ),
        ChallengeBlueprint(
            typeKey = "DECK_VERIFIED",
            title = "Deck Verified Hero",
            description = "Play any Steam Deck Verified title from your collection.",
            emoji = "🔋",
            category = BacklogChallengeCategory.GENRE,
            targetRequirement = 1
        ),
        ChallengeBlueprint(
            typeKey = "QUICK_SESSION",
            title = "Bite-Sized Sprint",
            description = "Play a quick-session friendly game (<30 min loop) for 15+ mins.",
            emoji = "☕",
            category = BacklogChallengeCategory.PLAYTIME,
            targetRequirement = 1
        ),
        ChallengeBlueprint(
            typeKey = "UNPLAYED_REVIVAL",
            title = "Fresh Start",
            description = "Boot up an unplayed backlog game and log 30+ minutes of play.",
            emoji = "🚀",
            category = BacklogChallengeCategory.EXPLORATION,
            targetRequirement = 1
        ),
        ChallengeBlueprint(
            typeKey = "FIVE_TROPHIES",
            title = "Trophy Sprint",
            description = "Unlock 5 total trophies across your entire gaming library.",
            emoji = "🥇",
            category = BacklogChallengeCategory.TROPHY,
            targetRequirement = 5
        ),
        ChallengeBlueprint(
            typeKey = "COMPLETION_JOURNEY",
            title = "Weekly Finisher",
            description = "Have 1 or more backlog games marked as Beaten in your library.",
            emoji = "🎖️",
            category = BacklogChallengeCategory.COMPLETION,
            targetRequirement = 1
        ),
        ChallengeBlueprint(
            typeKey = "COMPACT_STORAGE",
            title = "Lightweight Gem",
            description = "Play any game under 2GB storage footprint for 20+ minutes.",
            emoji = "💾",
            category = BacklogChallengeCategory.EXPLORATION,
            targetRequirement = 1
        ),
        ChallengeBlueprint(
            typeKey = "HEAVYWEIGHT_EPIC",
            title = "Triple-A Immersion",
            description = "Play a large 10GB+ title from your library for at least 45 minutes.",
            emoji = "🛡️",
            category = BacklogChallengeCategory.PLAYTIME,
            targetRequirement = 1
        ),
        ChallengeBlueprint(
            typeKey = "GENRE_HOPPER",
            title = "Genre Explorer",
            description = "Play games across 2 distinct genres this week.",
            emoji = "🗺️",
            category = BacklogChallengeCategory.EXPLORATION,
            targetRequirement = 2
        ),
        ChallengeBlueprint(
            typeKey = "DROPPED_REVIVAL",
            title = "Second Chance",
            description = "Revisit a Dropped or paused game and advance its status to Playing or Beaten.",
            emoji = "🔄",
            category = BacklogChallengeCategory.COMPLETION,
            targetRequirement = 1
        ),
        ChallengeBlueprint(
            typeKey = "CLOSE_TO_HUNDRED",
            title = "Mastery Push",
            description = "Play or unlock achievements in a game with 75%+ completion.",
            emoji = "🎯",
            category = BacklogChallengeCategory.TROPHY,
            targetRequirement = 1
        )
    )

    const val ANCHOR_WEEK_KEY = "2026-W46"
    const val MAX_FUTURE_WEEKS_PREVIEW = 2
    const val MAX_MANUAL_OVERRIDES = 2

    fun getCalendarForWeek(weekKey: String): Calendar {
        val parts = weekKey.split("-W")
        val year = parts[0].toInt()
        val week = parts[1].toInt()
        return Calendar.getInstance(Locale.US).apply {
            firstDayOfWeek = Calendar.MONDAY
            minimalDaysInFirstWeek = 4
            clear()
            set(Calendar.YEAR, year)
            set(Calendar.WEEK_OF_YEAR, week)
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
    }

    fun getWeeksBetween(startWeekKey: String, targetWeekKey: String): Int {
        return try {
            val startCal = getCalendarForWeek(startWeekKey)
            val targetCal = getCalendarForWeek(targetWeekKey)
            val diffMillis = targetCal.timeInMillis - startCal.timeInMillis
            Math.round(diffMillis.toDouble() / (7.0 * 24 * 60 * 60 * 1000.0)).toInt()
        } catch (_: Exception) {
            0
        }
    }

    /**
     * Calculates the sequential relative week number where the launch week (2026-W45) is Week 1.
     */
    fun getRelativeWeekNumber(weekKey: String = getCurrentWeekKey()): Int {
        val diff = getWeeksBetween(ANCHOR_WEEK_KEY, weekKey)
        return (1 + diff).coerceAtLeast(1)
    }

    /**
     * Returns the formatted week key (e.g. "2026-W45").
     */
    fun getCurrentWeekKey(): String {
        val cal = Calendar.getInstance(Locale.US).apply {
            firstDayOfWeek = Calendar.MONDAY
            minimalDaysInFirstWeek = 4
        }
        val year = cal.get(Calendar.YEAR)
        val week = cal.get(Calendar.WEEK_OF_YEAR)
        return String.format(Locale.US, "%04d-W%02d", year, week)
    }

    /**
     * Backward-compatible alias for getCurrentWeekKey.
     */
    fun getCurrentMonthKey(): String = getCurrentWeekKey()

    /**
     * Returns a human-friendly week display title marking the launch week as Week 1
     * (e.g. "Week 1 (Nov 9 – Nov 15)").
     */
    fun getWeekDisplayName(weekKey: String = getCurrentWeekKey()): String {
        return try {
            val cal = getCalendarForWeek(weekKey)
            val relativeWeekNum = getRelativeWeekNumber(weekKey)
            val startFormat = SimpleDateFormat("MMM d", Locale.US)
            val endFormat = SimpleDateFormat("MMM d", Locale.US)
            val startDateStr = startFormat.format(cal.time)
            cal.add(Calendar.DAY_OF_WEEK, 6)
            val endDateStr = endFormat.format(cal.time)
            "Week $relativeWeekNum ($startDateStr – $endDateStr)"
        } catch (_: Exception) {
            "Weekly Backlog Challenge"
        }
    }

    /**
     * Backward-compatible alias for getWeekDisplayName.
     */
    fun getMonthDisplayName(key: String = getCurrentWeekKey()): String = getWeekDisplayName(key)

    /**
     * Computes the preceding week key (e.g. "2026-W44" from "2026-W45").
     * Returns empty string if already at Week 1 (cannot navigate prior to Week 1).
     */
    fun getPreviousWeekKey(weekKey: String = getCurrentWeekKey()): String {
        return try {
            val diffFromAnchor = getWeeksBetween(ANCHOR_WEEK_KEY, weekKey)
            if (diffFromAnchor <= 0) return "" // Cannot navigate before Week 1
            val cal = getCalendarForWeek(weekKey).apply {
                add(Calendar.WEEK_OF_YEAR, -1)
            }
            val prevYear = cal.get(Calendar.YEAR)
            val prevWeek = cal.get(Calendar.WEEK_OF_YEAR)
            String.format(Locale.US, "%04d-W%02d", prevYear, prevWeek)
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Backward-compatible alias for getPreviousWeekKey.
     */
    fun getPreviousMonthKey(key: String = getCurrentWeekKey()): String = getPreviousWeekKey(key)

    /**
     * Computes the subsequent week key.
     * Constrained to only allow viewing the next 2 weeks beyond the current active week.
     * Returns empty string when maximum forward preview limit is reached.
     */
    fun getNextWeekKey(weekKey: String = getCurrentWeekKey()): String {
        return try {
            val currentWeek = getCurrentWeekKey()
            val diffFromCurrent = getWeeksBetween(currentWeek, weekKey)
            // Limit preview to next 2 weeks only
            if (diffFromCurrent >= MAX_FUTURE_WEEKS_PREVIEW) return ""
            val cal = getCalendarForWeek(weekKey).apply {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
            val nextYear = cal.get(Calendar.YEAR)
            val nextWeek = cal.get(Calendar.WEEK_OF_YEAR)
            String.format(Locale.US, "%04d-W%02d", nextYear, nextWeek)
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Backward-compatible alias for getNextWeekKey.
     */
    fun getNextMonthKey(key: String = getCurrentWeekKey()): String = getNextWeekKey(key)

    fun isCurrentWeek(weekKey: String): Boolean {
        return weekKey == getCurrentWeekKey()
    }

    /**
     * Backward-compatible alias for isCurrentWeek.
     */
    fun isCurrentMonth(key: String): Boolean = isCurrentWeek(key)

    /**
     * Computes how many days remain in the active weekly period (until Sunday midnight).
     */
    fun getDaysRemainingInWeek(weekKey: String = getCurrentWeekKey()): Int {
        return try {
            if (weekKey != getCurrentWeekKey()) return 0
            val cal = Calendar.getInstance(Locale.US).apply {
                firstDayOfWeek = Calendar.MONDAY
            }
            val day = cal.get(Calendar.DAY_OF_WEEK)
            if (day == Calendar.SUNDAY) 0 else (Calendar.SATURDAY - day + 1)
        } catch (_: Exception) {
            0
        }
    }

    /**
     * Backward-compatible alias for getDaysRemainingInWeek.
     */
    fun getDaysRemainingInMonth(key: String = getCurrentWeekKey()): Int = getDaysRemainingInWeek(key)

    /**
     * Procedurally selects 9 diverse challenges for the given weekKey using a deterministic seed.
     * This guarantees every week gets a fresh and balanced 3x3 challenge layout automatically.
     */
    fun getWeeklyChallenges(weekKey: String): List<ChallengeBlueprint> {
        val seed = weekKey.hashCode().toLong()
        val random = Random(seed)

        val byCategory = CHALLENGE_CATALOG.groupBy { it.category }
        val selected = mutableListOf<ChallengeBlueprint>()

        // 1. Ensure at least one from each of the 5 core categories
        BacklogChallengeCategory.values().forEach { cat ->
            val list = byCategory[cat].orEmpty()
            if (list.isNotEmpty()) {
                val pick = list[random.nextInt(list.size)]
                selected.add(pick)
            }
        }

        // 2. Pick remaining 4 items from the rest of the pool
        val remainingPool = CHALLENGE_CATALOG.filter { bp -> selected.none { it.typeKey == bp.typeKey } }.toMutableList()
        remainingPool.shuffle(random)

        val needed = 9 - selected.size
        for (i in 0 until minOf(needed, remainingPool.size)) {
            selected.add(remainingPool[i])
        }

        // 3. Shuffle final 9 deterministically so their positions vary every week
        selected.shuffle(random)
        return selected.take(9)
    }

    /**
     * Backward-compatible alias for getWeeklyChallenges.
     */
    fun getMonthlyChallenges(key: String): List<ChallengeBlueprint> = getWeeklyChallenges(key)

    /**
     * Generates or restores the 3x3 challenge grid for the given week.
     * Evaluates dynamically against real library games and achievements.
     */
    fun getOrGenerateGrid(
        preferences: AppPreferences,
        weekKey: String = getCurrentWeekKey(),
        games: List<UnifiedLibraryItem> = emptyList(),
        trophies: List<SteamAchievementEntity> = emptyList()
    ): BacklogChallengeGrid {
        val rawSaved = preferences.getBacklogChallengeProgress(weekKey)
        val manualOverrides = parseManualOverrides(rawSaved)

        val unplayedCandidate = games.firstOrNull { it.playStatus == PlayStatus.NOT_STARTED }
        val indieCandidate = games.firstOrNull { isIndieGame(it) && it.playStatus != PlayStatus.BEATEN }
        val vaultCandidate = games.firstOrNull { it is UnifiedLibraryItem.Vault && it.playStatus == PlayStatus.NOT_STARTED }
        val controllerCandidate = games.firstOrNull { it.hasControllerSupport && it.playStatus != PlayStatus.BEATEN }

        val weeklyBlueprints = getWeeklyChallenges(weekKey)

        val templateTiles = weeklyBlueprints.mapIndexed { index, bp ->
            val rec = when (bp.typeKey) {
                "GOLD_TROPHY" -> trophies.firstOrNull { !it.isAchieved && it.rarityTier == TrophyRarityTier.GOLD }?.gameName to null
                "INDIE_GAME" -> indieCandidate?.title to (indieCandidate as? UnifiedLibraryItem.Steam)?.game?.appId
                "IMMERSION_SESSION" -> unplayedCandidate?.title to (unplayedCandidate as? UnifiedLibraryItem.Steam)?.game?.appId
                "VAULT_STARTER" -> vaultCandidate?.title to null
                "BEAT_GAME" -> (unplayedCandidate ?: games.firstOrNull())?.title to null
                "CONTROLLER_READY" -> controllerCandidate?.title to (controllerCandidate as? UnifiedLibraryItem.Steam)?.game?.appId
                "MYTHIC_HUNTER" -> trophies.firstOrNull { !it.isAchieved && it.rarityTier == TrophyRarityTier.MYTHIC }?.gameName to null
                "METACRITIC_80" -> games.firstOrNull { (it.metacriticScore ?: 0) >= 80 }?.title to null
                "DECK_VERIFIED" -> games.firstOrNull { it is UnifiedLibraryItem.Steam && it.game.hardwareProfile.isDeckVerified }?.title to null
                else -> unplayedCandidate?.title to null
            }

            BacklogChallengeTile(
                id = "tile_${weekKey}_${index}_${bp.typeKey.lowercase()}",
                index = index,
                title = bp.title,
                description = bp.description,
                emoji = bp.emoji,
                category = bp.category,
                targetRequirement = bp.targetRequirement,
                recommendedGameTitle = rec.first,
                recommendedAppId = rec.second,
                challengeType = bp.typeKey
            )
        }

        // Evaluate tiles against data and manual overrides
        val evaluatedTiles = templateTiles.map { tile ->
            val manual = manualOverrides[tile.index] ?: false
            val autoEval = evaluateChallenge(tile.challengeType, tile.targetRequirement, games, trophies)
            val isDone = manual || autoEval.first
            val progress = if (manual) tile.targetRequirement else maxOf(autoEval.second, if (isDone) tile.targetRequirement else 0)
            tile.copy(
                isCompleted = isDone,
                currentProgress = progress,
                manualOverrideDone = manual
            )
        }

        // Win conditions: Rows, Columns, Diagonals
        val completedRows = mutableSetOf<Int>()
        for (r in 0..2) {
            if (evaluatedTiles[r * 3].isCompleted && evaluatedTiles[r * 3 + 1].isCompleted && evaluatedTiles[r * 3 + 2].isCompleted) {
                completedRows.add(r)
            }
        }

        val completedCols = mutableSetOf<Int>()
        for (c in 0..2) {
            if (evaluatedTiles[c].isCompleted && evaluatedTiles[c + 3].isCompleted && evaluatedTiles[c + 6].isCompleted) {
                completedCols.add(c)
            }
        }

        val completedDiags = mutableSetOf<Int>()
        if (evaluatedTiles[0].isCompleted && evaluatedTiles[4].isCompleted && evaluatedTiles[8].isCompleted) {
            completedDiags.add(0)
        }
        if (evaluatedTiles[2].isCompleted && evaluatedTiles[4].isCompleted && evaluatedTiles[6].isCompleted) {
            completedDiags.add(1)
        }

        val totalLines = completedRows.size + completedCols.size + completedDiags.size
        val isBlackout = evaluatedTiles.all { it.isCompleted }
        val completedCount = evaluatedTiles.count { it.isCompleted }

        // Milestones
        val hasAnyLine = totalLines > 0
        val milestones = listOf(
            BacklogMilestoneReward(
                id = "${MILESTONE_BACKLOG_SLAYER}_$weekKey",
                title = "Backlog Slayer Trophy & Theme",
                subtitle = "Complete any 3-tile line this week (row, col, or diag)",
                type = BacklogMilestoneType.LINE_ROW,
                trophyTitle = "Backlog Slayer",
                trophyApiName = "ACH_BACKLOG_SLAYER",
                trophyRarity = TrophyRarityTier.MYTHIC,
                themeReward = AccentTheme.BACKLOG_SLAYER,
                isUnlocked = hasAnyLine,
                isClaimed = preferences.isBacklogMilestoneAwarded(MILESTONE_BACKLOG_SLAYER) || preferences.isBacklogMilestoneAwarded("${MILESTONE_BACKLOG_SLAYER}_$weekKey")
            ),
            BacklogMilestoneReward(
                id = "${MILESTONE_ROW_RAIDER}_$weekKey",
                title = "Row Raider Trophy",
                subtitle = "Complete any horizontal row this week",
                type = BacklogMilestoneType.LINE_ROW,
                trophyTitle = "Row Raider",
                trophyApiName = "ACH_ROW_RAIDER",
                trophyRarity = TrophyRarityTier.GOLD,
                isUnlocked = completedRows.isNotEmpty(),
                isClaimed = preferences.isBacklogMilestoneAwarded(MILESTONE_ROW_RAIDER) || preferences.isBacklogMilestoneAwarded("${MILESTONE_ROW_RAIDER}_$weekKey")
            ),
            BacklogMilestoneReward(
                id = "${MILESTONE_COLUMN_CONQUEROR}_$weekKey",
                title = "Column Conqueror Trophy",
                subtitle = "Complete any vertical column this week",
                type = BacklogMilestoneType.LINE_COLUMN,
                trophyTitle = "Column Conqueror",
                trophyApiName = "ACH_COLUMN_CONQUEROR",
                trophyRarity = TrophyRarityTier.MYTHIC,
                isUnlocked = completedCols.isNotEmpty(),
                isClaimed = preferences.isBacklogMilestoneAwarded(MILESTONE_COLUMN_CONQUEROR) || preferences.isBacklogMilestoneAwarded("${MILESTONE_COLUMN_CONQUEROR}_$weekKey")
            ),
            BacklogMilestoneReward(
                id = "${MILESTONE_VAULT_CLEANER}_$weekKey",
                title = "Vault Cleaner Gold Trophy & 24K Theme",
                subtitle = "Complete all 9 tiles on the weekly card (Blackout)",
                type = BacklogMilestoneType.BLACKOUT_ALL,
                trophyTitle = "Vault Cleaner",
                trophyApiName = "ACH_VAULT_CLEANER",
                trophyRarity = TrophyRarityTier.MYTHIC,
                themeReward = AccentTheme.VAULT_CLEANER,
                isUnlocked = isBlackout,
                isClaimed = preferences.isBacklogMilestoneAwarded(MILESTONE_VAULT_CLEANER) || preferences.isBacklogMilestoneAwarded("${MILESTONE_VAULT_CLEANER}_$weekKey")
            ),
            BacklogMilestoneReward(
                id = "${MILESTONE_BLACKOUT_CHAMPION}_$weekKey",
                title = "Nebula Blackout Theme & Champion Trophy",
                subtitle = "Master all 9 challenges in a single week",
                type = BacklogMilestoneType.BLACKOUT_ALL,
                trophyTitle = "Blackout Champion",
                trophyApiName = "ACH_BLACKOUT_CHAMPION",
                trophyRarity = TrophyRarityTier.MYTHIC,
                themeReward = AccentTheme.NEBULA_BLACKOUT,
                isUnlocked = isBlackout,
                isClaimed = preferences.isBacklogMilestoneAwarded(MILESTONE_BLACKOUT_CHAMPION) || preferences.isBacklogMilestoneAwarded("${MILESTONE_BLACKOUT_CHAMPION}_$weekKey")
            )
        )

        return BacklogChallengeGrid(
            weekKey = weekKey,
            weekTitle = getWeekDisplayName(weekKey),
            daysRemaining = getDaysRemainingInWeek(weekKey),
            tiles = evaluatedTiles,
            completedRows = completedRows,
            completedColumns = completedCols,
            completedDiagonals = completedDiags,
            isBlackout = isBlackout,
            completedCount = completedCount,
            totalLinesCompleted = totalLines,
            availableMilestones = milestones,
            isCurrentWeek = isCurrentWeek(weekKey),
            previousWeekKey = getPreviousWeekKey(weekKey),
            nextWeekKey = getNextWeekKey(weekKey)
        )
    }

    /**
     * Toggles manual completion status for a tile in a specific week.
     * Enforces that manual unlocks can only be used at most 2 times per week.
     * Returns true if toggled successfully, or false if the 2-use limit was exceeded.
     */
    fun toggleManualOverride(
        preferences: AppPreferences,
        weekKey: String,
        tileIndex: Int
    ): Boolean {
        val raw = preferences.getBacklogChallengeProgress(weekKey)
        val map = parseManualOverrides(raw).toMutableMap()
        val current = map[tileIndex] ?: false

        if (!current) {
            // Turning ON manual unlock: verify limit
            val activeCount = map.count { it.value }
            if (activeCount >= MAX_MANUAL_OVERRIDES) {
                return false // Limit reached! Only 2 manual unlocks allowed
            }
            map[tileIndex] = true
        } else {
            // Turning OFF manual unlock: always allowed to free up the allowance slot
            map.remove(tileIndex)
        }

        val json = JSONObject()
        val arr = JSONArray()
        map.forEach { (idx, state) ->
            if (state) arr.put(idx)
        }
        json.put("manualOverrides", arr)
        preferences.saveBacklogChallengeProgress(weekKey, json.toString())
        return true
    }

    /**
     * Checks if any milestones were newly unlocked, awards in-app trophies to database,
     * unlocks themes in preferences, and returns newly awarded rewards.
     */
    suspend fun checkAndAwardMilestones(
        grid: BacklogChallengeGrid,
        database: AppDatabase,
        preferences: AppPreferences
    ): List<BacklogMilestoneReward> {
        val newlyAwarded = mutableListOf<BacklogMilestoneReward>()
        val trophiesToInsert = mutableListOf<SteamAchievementEntity>()
        val nowSec = System.currentTimeMillis() / 1000L

        for (m in grid.availableMilestones) {
            if (m.isUnlocked && !m.isClaimed) {
                // Award in preferences (both generic and week-specific id)
                preferences.markBacklogMilestoneAwarded(m.id)
                val baseId = m.id.substringBeforeLast("_")
                preferences.markBacklogMilestoneAwarded(baseId)

                // Unlock theme if applicable
                m.themeReward?.let { theme ->
                    preferences.unlockAccentTheme(theme)
                }

                // Award in-app trophy if applicable
                if (m.trophyTitle != null && m.trophyApiName != null) {
                    val entity = SteamAchievementEntity(
                        appId = APP_ID_BACKLOG_CHALLENGE,
                        apiName = m.trophyApiName,
                        displayName = m.trophyTitle,
                        description = m.subtitle,
                        isAchieved = true,
                        unlockTimestamp = nowSec,
                        globalPercentage = when (m.trophyRarity) {
                            TrophyRarityTier.MYTHIC -> 1.0f
                            TrophyRarityTier.GOLD -> 8.0f
                            else -> 15.0f
                        },
                        iconUrl = "",
                        gameName = "Backlog Challenge"
                    )
                    trophiesToInsert.add(entity)
                }

                newlyAwarded.add(m.copy(isClaimed = true))
            }
        }

        if (trophiesToInsert.isNotEmpty()) {
            database.steamAchievementDao().insertAll(trophiesToInsert)
        }

        return newlyAwarded
    }

    private fun evaluateChallenge(
        challengeType: String,
        targetRequirement: Int,
        games: List<UnifiedLibraryItem>,
        trophies: List<SteamAchievementEntity>
    ): Pair<Boolean, Int> {
        return when (challengeType) {
            "GOLD_TROPHY" -> {
                val goldCount = trophies.count { it.isAchieved && (it.rarityTier == TrophyRarityTier.GOLD || it.rarityTier == TrophyRarityTier.MYTHIC) }
                (goldCount >= targetRequirement) to minOf(goldCount, targetRequirement)
            }
            "INDIE_GAME" -> {
                val beatenIndie = games.any { isIndieGame(it) && it.playStatus == PlayStatus.BEATEN }
                val playingIndie = games.any { isIndieGame(it) && (it.playStatus == PlayStatus.PLAYING || it.playtimeMinutes >= 20) }
                (beatenIndie || playingIndie) to (if (beatenIndie || playingIndie) 1 else 0)
            }
            "IMMERSION_SESSION", "TWO_HOUR_IMMERSION" -> {
                val immersed = games.any { it.playtimeMinutes >= 60 }
                immersed to (if (immersed) 1 else 0)
            }
            "VAULT_STARTER" -> {
                val vaultPlayed = games.any { it is UnifiedLibraryItem.Vault && (it.playStatus == PlayStatus.PLAYING || it.playStatus == PlayStatus.BEATEN || it.playtimeMinutes > 0) }
                vaultPlayed to (if (vaultPlayed) 1 else 0)
            }
            "BEAT_GAME" -> {
                val beatenCount = games.count { it.playStatus == PlayStatus.BEATEN }
                (beatenCount >= targetRequirement) to minOf(beatenCount, targetRequirement)
            }
            "CONTROLLER_READY" -> {
                val ctrl = games.any { it.hasControllerSupport && (it.playStatus == PlayStatus.PLAYING || it.playStatus == PlayStatus.BEATEN || it.playtimeMinutes >= 25) }
                ctrl to (if (ctrl) 1 else 0)
            }
            "TROPHY_COLLECTOR" -> {
                val unlockedCount = trophies.count { it.isAchieved }
                (unlockedCount >= targetRequirement) to minOf(unlockedCount, targetRequirement)
            }
            "MYTHIC_HUNTER" -> {
                val mythicCount = trophies.count { it.isAchieved && it.rarityTier == TrophyRarityTier.MYTHIC }
                (mythicCount >= targetRequirement) to minOf(mythicCount, targetRequirement)
            }
            "TRI_PLAY" -> {
                val playedCount = games.count { it.playStatus == PlayStatus.PLAYING || it.playStatus == PlayStatus.BEATEN || it.playtimeMinutes >= 15 }
                (playedCount >= targetRequirement) to minOf(playedCount, targetRequirement)
            }
            "METACRITIC_80" -> {
                val highRated = games.any { (it.metacriticScore ?: 0) >= 80 && (it.playStatus == PlayStatus.PLAYING || it.playStatus == PlayStatus.BEATEN || it.playtimeMinutes >= 30) }
                highRated to (if (highRated) 1 else 0)
            }
            "DECK_VERIFIED" -> {
                val deck = games.any { it is UnifiedLibraryItem.Steam && it.game.hardwareProfile.isDeckVerified && (it.playStatus == PlayStatus.PLAYING || it.playStatus == PlayStatus.BEATEN || it.playtimeMinutes >= 15) }
                deck to (if (deck) 1 else 0)
            }
            "QUICK_SESSION" -> {
                val quick = games.any { it is UnifiedLibraryItem.Steam && it.game.hardwareProfile.isQuickSession && (it.playStatus == PlayStatus.PLAYING || it.playStatus == PlayStatus.BEATEN || it.playtimeMinutes >= 15) }
                quick to (if (quick) 1 else 0)
            }
            "UNPLAYED_REVIVAL" -> {
                val unplayed = games.any { it.playStatus == PlayStatus.PLAYING && it.playtimeMinutes in 15..300 }
                unplayed to (if (unplayed) 1 else 0)
            }
            "FIVE_TROPHIES", "TEN_TROPHIES" -> {
                val count = trophies.count { it.isAchieved }
                (count >= targetRequirement) to minOf(count, targetRequirement)
            }
            "COMPLETION_JOURNEY" -> {
                val beaten = games.count { it.playStatus == PlayStatus.BEATEN }
                (beaten >= targetRequirement) to minOf(beaten, targetRequirement)
            }
            "COMPACT_STORAGE" -> {
                val compact = games.any {
                    val sizeGb = when (it) {
                        is UnifiedLibraryItem.Steam -> it.game.hardwareProfile.effectiveStorageGb
                        is UnifiedLibraryItem.Vault -> it.giveaway.hardwareProfile.effectiveStorageGb
                    }
                    sizeGb < 2.5f && (it.playStatus == PlayStatus.PLAYING || it.playStatus == PlayStatus.BEATEN || it.playtimeMinutes >= 20)
                }
                compact to (if (compact) 1 else 0)
            }
            "HEAVYWEIGHT_EPIC" -> {
                val heavy = games.any {
                    val sizeGb = when (it) {
                        is UnifiedLibraryItem.Steam -> it.game.hardwareProfile.effectiveStorageGb
                        is UnifiedLibraryItem.Vault -> it.giveaway.hardwareProfile.effectiveStorageGb
                    }
                    sizeGb >= 10.0f && (it.playStatus == PlayStatus.PLAYING || it.playStatus == PlayStatus.BEATEN || it.playtimeMinutes >= 45)
                }
                heavy to (if (heavy) 1 else 0)
            }
            "GENRE_HOPPER" -> {
                val activeCount = games.count { it.playStatus == PlayStatus.PLAYING || it.playStatus == PlayStatus.BEATEN }
                val hopper = activeCount >= targetRequirement
                hopper to minOf(activeCount, targetRequirement)
            }
            "DROPPED_REVIVAL" -> {
                val resumed = games.any { it.playStatus == PlayStatus.PLAYING && it.playtimeMinutes >= 25 }
                resumed to (if (resumed) 1 else 0)
            }
            "CLOSE_TO_HUNDRED" -> {
                val close = games.any { it is UnifiedLibraryItem.Steam && it.game.achievementsPercentage >= 75 }
                close to (if (close) 1 else 0)
            }
            else -> false to 0
        }
    }

    private fun isIndieGame(item: UnifiedLibraryItem): Boolean {
        val title = item.title.lowercase()
        val isIndieTitle = title.contains("hollow knight") || title.contains("hades") ||
            title.contains("stardew") || title.contains("subnautica") ||
            title.contains("vampire survivors") || title.contains("celeste") ||
            title.contains("dead cells") || title.contains("slay the spire")
        if (isIndieTitle) return true
        if (item is UnifiedLibraryItem.Vault && item.giveaway.categories.contains(GameCategory.INDIE)) return true
        return false
    }

    private fun parseManualOverrides(rawJson: String): Map<Int, Boolean> {
        if (rawJson.isBlank()) return emptyMap()
        return try {
            val json = JSONObject(rawJson)
            val arr = json.optJSONArray("manualOverrides") ?: return emptyMap()
            val map = mutableMapOf<Int, Boolean>()
            for (i in 0 until arr.length()) {
                map[arr.getInt(i)] = true
            }
            map
        } catch (_: Exception) {
            emptyMap()
        }
    }
}
