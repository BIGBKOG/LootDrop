package com.example.data.model

import androidx.compose.runtime.Immutable
import org.json.JSONObject

@Immutable
data class PublicReview(
    val id: String = "",
    val giveawayId: Long = 0L,
    val gameTitle: String = "",
    val authorName: String = "Anonymous Gamer",
    val authorTag: String = "Gamer",
    val rating: Int = 5, // 1 to 5 stars
    val title: String = "",
    val comment: String = "",
    val platform: String = "All Platforms",
    val timestamp: Long = System.currentTimeMillis(),
    val helpfulCount: Int = 0,
    val isVerifiedClaimer: Boolean = false,
    val isAppReview: Boolean = false
) {
    val isForApp: Boolean
        get() = isAppReview || giveawayId == -1L || gameTitle.equals("LootDrop App", ignoreCase = true) || gameTitle.equals("LootDrop", ignoreCase = true)

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("giveawayId", giveawayId)
        put("gameTitle", gameTitle)
        put("authorName", authorName)
        put("authorTag", authorTag)
        put("rating", rating)
        put("title", title)
        put("comment", comment)
        put("platform", platform)
        put("timestamp", timestamp)
        put("helpfulCount", helpfulCount)
        put("isVerifiedClaimer", isVerifiedClaimer)
        put("isAppReview", isAppReview || isForApp)
    }

    companion object {
        fun fromJson(json: JSONObject): PublicReview {
            val gTitle = json.optString("gameTitle", "")
            val gId = json.optLong("giveawayId", 0L)
            val flag = json.optBoolean("isAppReview", false) || gId == -1L || gTitle.contains("LootDrop", ignoreCase = true)
            return PublicReview(
                id = json.optString("id", ""),
                giveawayId = gId,
                gameTitle = gTitle,
                authorName = json.optString("authorName", "Anonymous Gamer"),
                authorTag = json.optString("authorTag", "Gamer"),
                rating = json.optInt("rating", 5).coerceIn(1, 5),
                title = json.optString("title", ""),
                comment = json.optString("comment", ""),
                platform = json.optString("platform", "All Platforms"),
                timestamp = json.optLong("timestamp", System.currentTimeMillis()),
                helpfulCount = json.optInt("helpfulCount", 0),
                isVerifiedClaimer = json.optBoolean("isVerifiedClaimer", false),
                isAppReview = flag
            )
        }
    }
}
