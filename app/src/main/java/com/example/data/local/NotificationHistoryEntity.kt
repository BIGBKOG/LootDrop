package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notification_history",
    indices = [
        Index(value = ["giveawayId"]),
        Index(value = ["timestamp"]),
        Index(value = ["isRead"])
    ]
)
data class NotificationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val giveawayId: Int,
    val title: String,
    val worth: String,
    val platforms: String,
    val thumbnail: String,
    val image: String,
    val openGiveawayUrl: String,
    val channelType: String, // "MAJOR_GAME", "DLC", "ENDING_SOON", "SYSTEM"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
