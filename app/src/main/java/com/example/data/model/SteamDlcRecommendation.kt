package com.example.data.model

import androidx.compose.runtime.Immutable

@Immutable
enum class DlcFilterMode(val label: String) {
    ALL("All Picks"),
    FREE("Free DLCs"),
    ON_SALE("On Sale"),
    TOP_RATED("Top Expansions")
}

@Immutable
data class SteamDlcRecommendation(
    val dlcAppId: Int,
    val name: String,
    val parentAppId: Int,
    val parentGameName: String,
    val parentPlaytimeMinutes: Int = 0,
    val parentPlayStatus: PlayStatus = PlayStatus.NOT_STARTED,
    val headerImageUrl: String = if (dlcAppId > 0) {
        "https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/$dlcAppId/header.jpg"
    } else "",
    val shortDescription: String = "",
    val isFree: Boolean = false,
    val discountPercent: Int = 0,
    val priceFormatted: String = "",
    val initialPriceFormatted: String = "",
    val recommendationsCount: Int = 0,
    val releaseDate: String = "",
    val metacriticScore: Int? = null,
    val reasonBadge: String = "Expansion Pick",
    val recommendationReason: String = "",
    val storeUrl: String = if (dlcAppId > 0) "https://store.steampowered.com/app/$dlcAppId" else "",
    val matchedGiveawayId: Int? = null,
    val isSoundtrackOrCosmetic: Boolean = false,
    val relevanceScore: Int = 0
) {
    val isFromFreeGiveaway: Boolean
        get() = matchedGiveawayId != null

    val isOnSale: Boolean
        get() = discountPercent > 0

    val parentPlaytimeFormatted: String
        get() {
            if (parentPlaytimeMinutes <= 0) return "In Library"
            val hours = parentPlaytimeMinutes / 60.0
            return if (hours >= 10.0) {
                String.format(java.util.Locale.US, "%.0fh played", hours)
            } else {
                String.format(java.util.Locale.US, "%.1fh played", hours)
            }
        }

    val formattedRecommendations: String
        get() = when {
            recommendationsCount >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM reviews", recommendationsCount / 1_000_000.0)
            recommendationsCount >= 1_000 -> String.format(java.util.Locale.US, "%.1fk reviews", recommendationsCount / 1_000.0)
            recommendationsCount > 0 -> "$recommendationsCount reviews"
            else -> ""
        }
}
