package com.example.data.model

import androidx.compose.runtime.Immutable
import com.example.preferences.AccentTheme

enum class BacklogChallengeCategory(val label: String) {
    TROPHY("Trophy Hunter"),
    COMPLETION("Game Completion"),
    GENRE("Genre Specialist"),
    PLAYTIME("Deep Dive"),
    EXPLORATION("Backlog Discovery");

    val displayName: String get() = label
}

@Immutable
data class BacklogChallengeTile(
    val id: String,
    val index: Int, // 0 to 8 in the 3x3 grid
    val title: String,
    val description: String,
    val emoji: String,
    val category: BacklogChallengeCategory,
    val currentProgress: Int = 0,
    val targetRequirement: Int = 1,
    val isCompleted: Boolean = false,
    val recommendedGameTitle: String? = null,
    val recommendedAppId: Int? = null,
    val manualOverrideDone: Boolean = false,
    val challengeType: String = ""
) {
    val progressFraction: Float
        get() = if (targetRequirement > 0) (currentProgress.toFloat() / targetRequirement.toFloat()).coerceIn(0f, 1f) else if (isCompleted) 1f else 0f

    val formattedProgress: String
        get() = if (targetRequirement == 1) {
            if (isCompleted) "Completed" else "Not Started"
        } else {
            "$currentProgress / $targetRequirement"
        }
}

enum class BacklogMilestoneType {
    LINE_ROW,
    LINE_COLUMN,
    LINE_DIAGONAL,
    BLACKOUT_ALL
}

@Immutable
data class BacklogMilestoneReward(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: BacklogMilestoneType,
    val trophyTitle: String? = null,
    val trophyApiName: String? = null,
    val trophyRarity: TrophyRarityTier? = null,
    val themeReward: AccentTheme? = null,
    val isClaimed: Boolean = false,
    val isUnlocked: Boolean = false
)

@Immutable
data class BacklogChallengeGrid(
    val weekKey: String, // e.g. "2026-W46"
    val weekTitle: String, // e.g. "Week 1 (Nov 9 – 15)"
    val daysRemaining: Int,
    val tiles: List<BacklogChallengeTile>,
    val completedRows: Set<Int> = emptySet(), // 0, 1, 2
    val completedColumns: Set<Int> = emptySet(), // 0, 1, 2
    val completedDiagonals: Set<Int> = emptySet(), // 0, 1
    val isBlackout: Boolean = false,
    val completedCount: Int = 0,
    val totalLinesCompleted: Int = 0,
    val availableMilestones: List<BacklogMilestoneReward> = emptyList(),
    val isCurrentWeek: Boolean = true,
    val previousWeekKey: String = "",
    val nextWeekKey: String = "",
    val monthKey: String = weekKey,
    val monthTitle: String = weekTitle,
    val isCurrentMonth: Boolean = isCurrentWeek,
    val previousMonthKey: String = previousWeekKey,
    val nextMonthKey: String = nextWeekKey,
    val maxManualOverrides: Int = 2
) {
    val manualOverridesUsed: Int
        get() = tiles.count { it.manualOverrideDone }

    val remainingManualOverrides: Int
        get() = (maxManualOverrides - manualOverridesUsed).coerceAtLeast(0)

    val canManualOverrideMore: Boolean
        get() = manualOverridesUsed < maxManualOverrides
}
