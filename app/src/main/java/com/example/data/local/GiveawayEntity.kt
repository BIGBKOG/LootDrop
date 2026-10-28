package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.GiveawayItem

@Entity(
    tableName = "giveaways",
    indices = [
        Index(value = ["isClaimed", "id"]),
        Index(value = ["isBookmarked", "id"]),
        Index(value = ["status", "id"]),
        Index(value = ["type"])
    ]
)
data class GiveawayEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val worth: String,
    val numericWorth: Double,
    val thumbnail: String,
    val image: String,
    val description: String,
    val instructions: String,
    val openGiveawayUrl: String,
    val publishedDate: String,
    val type: String,
    val platforms: String,
    val endDate: String,
    val users: Int,
    val status: String,
    val isClaimed: Boolean = false,
    val isBookmarked: Boolean = false,
    val firstSeenTimestamp: Long = System.currentTimeMillis(),
    val notified: Boolean = false,
    val playStatus: com.example.data.model.PlayStatus = com.example.data.model.PlayStatus.NOT_STARTED,
    val steamAppId: Int? = null,
    val steamPlaytimeMinutes: Int = 0,
    val steamAchievementsUnlocked: Int = 0,
    val steamAchievementsTotal: Int = 0,
    val steamLastPlayedTimestamp: Long = 0L,
    val isWishlisted: Boolean = false
) {
    fun toDomain(): GiveawayItem {
        // Flag as "NEW" if seen within the last 48 hours
        val isNew = (System.currentTimeMillis() - firstSeenTimestamp) < (48L * 60 * 60 * 1000)
        return GiveawayItem(
            id = id,
            title = title,
            worth = worth,
            numericWorth = numericWorth,
            thumbnail = thumbnail,
            image = image,
            description = description,
            instructions = instructions,
            openGiveawayUrl = openGiveawayUrl,
            publishedDate = publishedDate,
            type = type,
            platforms = platforms,
            endDate = endDate,
            users = users,
            status = status,
            isClaimed = isClaimed,
            isBookmarked = isBookmarked,
            isNew = isNew,
            firstSeenTimestamp = firstSeenTimestamp,
            playStatus = playStatus,
            steamAppId = steamAppId,
            steamPlaytimeMinutes = steamPlaytimeMinutes,
            steamAchievementsUnlocked = steamAchievementsUnlocked,
            steamAchievementsTotal = steamAchievementsTotal,
            steamLastPlayedTimestamp = steamLastPlayedTimestamp,
            isWishlisted = isWishlisted
        )
    }
}
