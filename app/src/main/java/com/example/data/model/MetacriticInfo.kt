package com.example.data.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Metacritic Rating Tier according to Metacritic's official review score thresholds:
 * 90-100: Universal Acclaim (Green #00CE7A)
 * 75-89: Generally Favorable (Green #66CC33)
 * 50-74: Mixed or Average (Yellow #FFCC33)
 * 20-49: Generally Unfavorable (Red #FF5252)
 * 0-19: Overwhelming Dislike (Red #D32F2F)
 */
enum class MetacriticRatingTier(
    val label: String,
    val summary: String,
    val colorHex: Long,
    val darkContainerColorHex: Long
) {
    UNIVERSAL_ACCLAIM(
        label = "Universal Acclaim",
        summary = "Must-play critical masterpiece",
        colorHex = 0xFF00E676,
        darkContainerColorHex = 0xFF003314
    ),
    GENERALLY_FAVORABLE(
        label = "Generally Favorable",
        summary = "Highly recommended by critics",
        colorHex = 0xFF66CC33,
        darkContainerColorHex = 0xFF142B0A
    ),
    MIXED_REVIEWS(
        label = "Mixed or Average",
        summary = "Diverse critical opinions",
        colorHex = 0xFFFFCC33,
        darkContainerColorHex = 0xFF332600
    ),
    UNFAVORABLE(
        label = "Generally Unfavorable",
        summary = "Mixed-to-negative reception",
        colorHex = 0xFFFF5252,
        darkContainerColorHex = 0xFF330A0A
    );

    val badgeColor: Color
        get() = Color(colorHex)

    val containerColor: Color
        get() = Color(darkContainerColorHex)

    companion object {
        fun fromScore(score: Int): MetacriticRatingTier {
            return when {
                score >= 90 -> UNIVERSAL_ACCLAIM
                score >= 75 -> GENERALLY_FAVORABLE
                score >= 50 -> MIXED_REVIEWS
                else -> UNFAVORABLE
            }
        }
    }
}

@Immutable
data class MetacriticInfo(
    val score: Int,
    val tier: MetacriticRatingTier,
    val reviewUrl: String,
    val isVerifiedCatalog: Boolean = true
) {
    val scoreText: String
        get() = score.toString()

    val badgeColor: Color
        get() = tier.badgeColor
}
