package com.example.data.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Trophy & Achievement rarity tiers based on global Steam player unlock percentages.
 * Used for the Trophy Wall and Rarest Unlocked Trophies showcase.
 */
@Immutable
enum class TrophyRarityTier(
    val label: String,
    val iconEmoji: String,
    val colorHex: Long,
    val bgHex: Long,
    val description: String
) {
    MYTHIC(
        label = "Mythic Trophy",
        iconEmoji = "💎",
        colorHex = 0xFFE040FB, // Neon Purple / Diamond
        bgHex = 0x33E040FB,
        description = "Ultra-rare: Unlocked by fewer than 5% of global Steam players."
    ),
    GOLD(
        label = "Gold Trophy",
        iconEmoji = "🥇",
        colorHex = 0xFFFFD700, // Gold
        bgHex = 0x33FFD700,
        description = "Rare: Unlocked by 5% to 15% of global players."
    ),
    SILVER(
        label = "Silver Trophy",
        iconEmoji = "🥈",
        colorHex = 0xFFCFD8DC, // Silver
        bgHex = 0x33CFD8DC,
        description = "Uncommon: Unlocked by 15% to 35% of global players."
    ),
    BRONZE(
        label = "Bronze Trophy",
        iconEmoji = "🥉",
        colorHex = 0xFFCD7F32, // Bronze
        bgHex = 0x33CD7F32,
        description = "Common: Unlocked by 35% to 65% of global players."
    ),
    COMMON(
        label = "Standard",
        iconEmoji = "⚪",
        colorHex = 0xFF90A4AE, // Slate
        bgHex = 0x2290A4AE,
        description = "Story / Base: Unlocked by over 65% of global players."
    );

    val color: Color get() = Color(colorHex)
    val containerColor: Color get() = Color(bgHex)
}
