package com.example.util

import android.content.Context
import com.example.data.local.SteamAchievementEntity
import com.example.preferences.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

/**
 * Singleton state manager for in-app Mythic Achievement unlock alerts.
 *
 * When background or foreground Steam sync detects a newly unlocked Mythic tier achievement,
 * this manager holds the event, persists it across app process re-entries, and dispatches
 * the in-app notification popup / toast banner.
 */
object MythicAchievementNotificationManager {

    private val _pendingNotification = MutableStateFlow<SteamAchievementEntity?>(null)
    val pendingNotification: StateFlow<SteamAchievementEntity?> = _pendingNotification.asStateFlow()

    fun postMythicUnlock(achievement: SteamAchievementEntity, context: Context? = null) {
        _pendingNotification.value = achievement
        context?.let { ctx ->
            saveToPreferences(ctx, achievement)
        }
    }

    fun dismissNotification(context: Context? = null) {
        _pendingNotification.value = null
        context?.let { ctx ->
            AppPreferences(ctx).pendingMythicAchievementJson = null
        }
    }

    fun checkPendingFromPreferences(context: Context) {
        if (_pendingNotification.value != null) return
        val prefs = AppPreferences(context)
        val jsonStr = prefs.pendingMythicAchievementJson ?: return
        try {
            val json = JSONObject(jsonStr)
            val ach = SteamAchievementEntity(
                appId = json.getInt("appId"),
                apiName = json.getString("apiName"),
                displayName = json.getString("displayName"),
                description = json.optString("description", ""),
                isAchieved = json.optBoolean("isAchieved", true),
                unlockTimestamp = json.optLong("unlockTimestamp", System.currentTimeMillis() / 1000L),
                globalPercentage = json.optDouble("globalPercentage", 2.5).toFloat(),
                iconUrl = json.optString("iconUrl", ""),
                gameName = json.optString("gameName", "")
            )
            _pendingNotification.value = ach
        } catch (_: Exception) {
            prefs.pendingMythicAchievementJson = null
        }
    }

    private fun saveToPreferences(context: Context, ach: SteamAchievementEntity) {
        try {
            val json = JSONObject().apply {
                put("appId", ach.appId)
                put("apiName", ach.apiName)
                put("displayName", ach.displayName)
                put("description", ach.description)
                put("isAchieved", ach.isAchieved)
                put("unlockTimestamp", ach.unlockTimestamp)
                put("globalPercentage", ach.globalPercentage.toDouble())
                put("iconUrl", ach.iconUrl)
                put("gameName", ach.gameName)
            }
            AppPreferences(context).pendingMythicAchievementJson = json.toString()
        } catch (_: Exception) {
            // Ignore serialization failure
        }
    }
}
