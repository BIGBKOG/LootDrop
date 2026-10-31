package com.example.data.model

import com.example.data.local.GiveawayEntity
import com.example.util.MetacriticCatalog
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

@JsonClass(generateAdapter = true)
data class CheapSharkDto(
    @Json(name = "internalName") val internalName: String? = null,
    @Json(name = "title") val title: String,
    @Json(name = "metacriticLink") val metacriticLink: String? = null,
    @Json(name = "dealID") val dealID: String,
    @Json(name = "storeID") val storeID: String,
    @Json(name = "gameID") val gameID: String,
    @Json(name = "salePrice") val salePrice: String? = "0.00",
    @Json(name = "normalPrice") val normalPrice: String? = "0.00",
    @Json(name = "isOnSale") val isOnSale: String? = "1",
    @Json(name = "savings") val savings: String? = "100.0",
    @Json(name = "metacriticScore") val metacriticScore: String? = "0",
    @Json(name = "steamRatingText") val steamRatingText: String? = null,
    @Json(name = "steamRatingPercent") val steamRatingPercent: String? = "0",
    @Json(name = "steamRatingCount") val steamRatingCount: String? = "0",
    @Json(name = "steamAppID") val steamAppID: String? = null,
    @Json(name = "releaseDate") val releaseDate: Long? = 0L,
    @Json(name = "lastChange") val lastChange: Long? = 0L,
    @Json(name = "dealRating") val dealRating: String? = null,
    @Json(name = "thumb") val thumb: String? = null
) {
    fun toEntity(): GiveawayEntity {
        // Offset ID to 100,000+ range to prevent collisions with GamerPower IDs (typically < 10,000)
        val generatedId = 100000 + (dealID.hashCode().absoluteValue % 800000)
        val platformName = storeIdToPlatform(storeID)
        val price = normalPrice?.toDoubleOrNull() ?: 0.0
        val cleanWorth = if (price > 0.0) String.format(Locale.US, "$%.2f", price) else "FREE"
        val dealRedirectUrl = "https://www.cheapshark.com/redirect?dealID=$dealID"

        val metaScoreNum = metacriticScore?.toIntOrNull() ?: 0
        val steamId = steamAppID?.toIntOrNull()
        if (metaScoreNum > 0) {
            MetacriticCatalog.registerScore(title, metaScoreNum, metacriticLink, steamId)
        }

        val desc = buildString {
            append("100% discount free promotion on ").append(platformName).append(".")
            if (!steamRatingText.isNullOrBlank() && steamRatingText != "null") {
                append(" Steam Rating: ").append(steamRatingText)
                val percent = steamRatingPercent?.toIntOrNull() ?: 0
                if (percent > 0) {
                    append(" (").append(percent).append("% positive)")
                }
                append(".")
            }
        }

        val publishedFormatted = if (releaseDate != null && releaseDate > 0) {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(releaseDate * 1000))
        } else ""

        val userCount = (dealRating?.toDoubleOrNull()?.times(1000))?.toInt()?.coerceAtLeast(500) ?: 1500

        return GiveawayEntity(
            id = generatedId,
            title = title,
            worth = cleanWorth,
            numericWorth = price,
            thumbnail = thumb ?: "",
            image = thumb ?: "",
            description = desc,
            instructions = "Click Claim to visit the $platformName store page and add this game to your library for free.",
            openGiveawayUrl = dealRedirectUrl,
            publishedDate = publishedFormatted,
            type = "Game",
            platforms = platformName,
            endDate = "Limited Time",
            users = userCount,
            status = "Active",
            firstSeenTimestamp = System.currentTimeMillis(),
            steamAppId = steamId
        )
    }

    companion object {
        fun storeIdToPlatform(storeId: String): String {
            return when (storeId) {
                "1" -> "Steam"
                "2" -> "GamersGate"
                "3" -> "GreenManGaming"
                "4" -> "Amazon"
                "5" -> "GameStop"
                "6" -> "Direct2Drive"
                "7" -> "GOG"
                "8" -> "Origin"
                "11" -> "Humble Store"
                "13" -> "Uplay"
                "15" -> "Fanatical"
                "21" -> "WinGameStore"
                "23" -> "GameBillet"
                "25" -> "Epic Games Store"
                "27" -> "Gamesplanet"
                "30" -> "IndieGala"
                "31" -> "Blizzard Shop"
                else -> "PC"
            }
        }
    }
}
