package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GiveawayDto(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
    @Json(name = "worth") val worth: String?,
    @Json(name = "thumbnail") val thumbnail: String?,
    @Json(name = "image") val image: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "instructions") val instructions: String?,
    @Json(name = "open_giveaway_url") val openGiveawayUrl: String?,
    @Json(name = "published_date") val publishedDate: String?,
    @Json(name = "type") val type: String?,
    @Json(name = "platforms") val platforms: String?,
    @Json(name = "end_date") val endDate: String?,
    @Json(name = "users") val users: Int?,
    @Json(name = "status") val status: String?,
    @Json(name = "gamerpower_url") val gamerpowerUrl: String?
)

fun GiveawayDto.toEntity(): com.example.data.local.GiveawayEntity {
    val cleanWorth = worth?.trim() ?: "FREE"
    val numeric = cleanWorth.replace("$", "").replace("€", "").trim().toDoubleOrNull() ?: 0.0

    return com.example.data.local.GiveawayEntity(
        id = id,
        title = title,
        worth = cleanWorth,
        numericWorth = numeric,
        thumbnail = thumbnail ?: "",
        image = image ?: thumbnail ?: "",
        description = description ?: "Free PC giveaway available to claim.",
        instructions = instructions ?: "Click Claim to visit the official giveaway page.",
        openGiveawayUrl = openGiveawayUrl ?: gamerpowerUrl ?: "https://www.gamerpower.com",
        publishedDate = publishedDate ?: "",
        type = type ?: "Game",
        platforms = platforms ?: "PC",
        endDate = endDate ?: "N/A",
        users = users ?: 0,
        status = status ?: "Active",
        firstSeenTimestamp = System.currentTimeMillis()
    )
}

fun GiveawayDto.toDomain(): GiveawayItem = toEntity().toDomain()
