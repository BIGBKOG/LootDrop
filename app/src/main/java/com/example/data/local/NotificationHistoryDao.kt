package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationHistoryDao {

    @Query("SELECT * FROM notification_history ORDER BY timestamp DESC")
    fun getAllNotificationsFlow(): Flow<List<NotificationHistoryEntity>>

    @Query("SELECT * FROM notification_history ORDER BY timestamp DESC LIMIT 50")
    suspend fun getRecentNotifications(): List<NotificationHistoryEntity>

    @Query("SELECT COUNT(*) FROM notification_history WHERE isRead = 0")
    fun getUnreadCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notification_history")
    suspend fun getTotalCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(entry: NotificationHistoryEntity): Long

    @Query("UPDATE notification_history SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notification_history SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notification_history WHERE id = :id")
    suspend fun deleteNotification(id: Long)

    @Query("DELETE FROM notification_history")
    suspend fun clearAllNotifications()

    @Query("DELETE FROM notification_history WHERE id NOT IN (SELECT id FROM notification_history ORDER BY timestamp DESC LIMIT 100)")
    suspend fun trimOldNotifications()
}
