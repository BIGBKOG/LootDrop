package com.example.util

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.SteamAchievementEntity
import com.example.data.model.TrophyRarityTier
import com.example.data.remote.SteamApiClient
import com.example.data.remote.SteamOwnedGame
import com.example.notifications.NotificationHelper
import com.example.preferences.AppPreferences

/**
 * Handles Steam synchronization for achievements and detects newly unlocked
 * Mythic tier achievements (< 5% global unlock rate).
 */
object SteamSyncHelper {
    private const val TAG = "SteamSyncHelper"

    /**
     * Inspects updated achievements for newly unlocked mythic trophies.
     * Compares against existing database state so that only achievements
     * that transitioned from locked to unlocked trigger notifications.
     */
    suspend fun checkAndNotifyMythicAchievements(
        context: Context,
        db: AppDatabase,
        achievements: List<SteamAchievementEntity>,
        sendSystemNotification: Boolean = true
    ): List<SteamAchievementEntity> {
        val mythicNewlyUnlocked = mutableListOf<SteamAchievementEntity>()
        val dao = db.steamAchievementDao()

        for (ach in achievements) {
            if (ach.isAchieved && ach.rarityTier == TrophyRarityTier.MYTHIC) {
                val existing = dao.getAchievement(ach.appId, ach.apiName)
                if (existing == null || !existing.isAchieved) {
                    mythicNewlyUnlocked.add(ach)
                }
            }
        }

        if (mythicNewlyUnlocked.isNotEmpty()) {
            val firstMythic = mythicNewlyUnlocked.first()

            // Trigger in-app notification popup / toast
            MythicAchievementNotificationManager.postMythicUnlock(firstMythic, context)

            // Trigger system notification if enabled
            if (sendSystemNotification) {
                try {
                    val helper = NotificationHelper(context)
                    if (helper.isNotificationsEnabled && !helper.isQuietHoursActive()) {
                        helper.sendMythicAchievementNotification(firstMythic)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to send system notification for mythic achievement", e)
                }
            }
        }

        return mythicNewlyUnlocked
    }

    /**
     * Executes background Steam sync: resolves user ID, queries top played games,
     * updates achievements in DB, and checks for newly unlocked mythic trophies.
     */
    suspend fun performBackgroundSteamSync(
        context: Context,
        db: AppDatabase,
        prefs: AppPreferences,
        helper: NotificationHelper
    ): List<SteamAchievementEntity> {
        val rawUserId = prefs.steamUserId.trim()
        if (rawUserId.isBlank()) return emptyList()

        val apiKey = SteamApiClient.resolveApiKey(prefs.steamApiKey)
        val resolvedSteamId = SteamApiClient.resolveSteamId(rawUserId, apiKey) ?: return emptyList()

        val ownedGames = try {
            SteamApiClient.getOwnedGames(resolvedSteamId, apiKey)
        } catch (_: Exception) {
            emptyList()
        }

        val gamesToScan: List<SteamOwnedGame> = if (ownedGames.isNotEmpty()) {
            ownedGames.filter { it.playtimeForeverMinutes > 0 }
                .sortedByDescending { it.playtimeForeverMinutes }
                .take(12)
        } else {
            db.steamGameDao().getAllSteamGamesList().filter { it.playtimeMinutes > 0 }
                .sortedByDescending { it.playtimeMinutes }
                .take(12)
                .map {
                    SteamOwnedGame(
                        appId = it.appId,
                        name = it.name,
                        playtimeForeverMinutes = it.playtimeMinutes,
                        rtimeLastPlayed = it.lastPlayedTimestamp
                    )
                }
        }

        val newlyUnlockedMythics = mutableListOf<SteamAchievementEntity>()

        for (game in gamesToScan) {
            val detailed = try {
                SteamApiClient.getDetailedPlayerAchievements(
                    appId = game.appId,
                    steamId = resolvedSteamId,
                    apiKey = apiKey,
                    gameName = game.name
                )
            } catch (_: Exception) {
                emptyList()
            }

            if (detailed.isNotEmpty()) {
                val detected = checkAndNotifyMythicAchievements(
                    context = context,
                    db = db,
                    achievements = detailed,
                    sendSystemNotification = true
                )
                newlyUnlockedMythics.addAll(detected)
                db.steamAchievementDao().insertAll(detailed)
            }
        }

        return newlyUnlockedMythics
    }
}
