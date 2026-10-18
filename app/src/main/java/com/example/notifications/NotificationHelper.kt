package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.GiveawayItem

class NotificationHelper(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("lootdrop_notification_prefs", Context.MODE_PRIVATE)

    companion object {
        const val CHANNEL_ID = "lootdrop_free_games_channel"
        const val CHANNEL_NAME = "Free Game Alerts"
        const val EXTRA_GIVEAWAY_ID = "extra_giveaway_id"
        const val EXTRA_CLAIM_URL = "extra_claim_url"

        private const val PREF_NOTIF_ENABLED = "pref_notif_enabled"
        private const val PREF_NOTIFY_PLAYSTATION = "pref_notify_playstation"
        private const val PREF_NOTIFY_XBOX = "pref_notify_xbox"
        private const val PREF_NOTIFY_NINTENDO = "pref_notify_nintendo"
        private const val PREF_NOTIFY_STEAM = "pref_notify_steam"
        private const val PREF_NOTIFY_EPIC = "pref_notify_epic"
        private const val PREF_NOTIFY_GOG = "pref_notify_gog"
        private const val PREF_NOTIFY_OTHERS = "pref_notify_others"
        private const val PREF_GAMES_ONLY = "pref_games_only"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when new free games drop on PlayStation, Xbox, Nintendo Switch, Steam, Epic & PC"
                enableVibration(true)
                setShowBadge(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    var isNotificationsEnabled: Boolean
        get() = prefs.getBoolean(PREF_NOTIF_ENABLED, true)
        set(value) = prefs.edit().putBoolean(PREF_NOTIF_ENABLED, value).apply()

    var isNotifyPlayStation: Boolean
        get() = prefs.getBoolean(PREF_NOTIFY_PLAYSTATION, true)
        set(value) = prefs.edit().putBoolean(PREF_NOTIFY_PLAYSTATION, value).apply()

    var isNotifyXbox: Boolean
        get() = prefs.getBoolean(PREF_NOTIFY_XBOX, true)
        set(value) = prefs.edit().putBoolean(PREF_NOTIFY_XBOX, value).apply()

    var isNotifyNintendo: Boolean
        get() = prefs.getBoolean(PREF_NOTIFY_NINTENDO, true)
        set(value) = prefs.edit().putBoolean(PREF_NOTIFY_NINTENDO, value).apply()

    var isNotifySteam: Boolean
        get() = prefs.getBoolean(PREF_NOTIFY_STEAM, true)
        set(value) = prefs.edit().putBoolean(PREF_NOTIFY_STEAM, value).apply()

    var isNotifyEpic: Boolean
        get() = prefs.getBoolean(PREF_NOTIFY_EPIC, true)
        set(value) = prefs.edit().putBoolean(PREF_NOTIFY_EPIC, value).apply()

    var isNotifyGog: Boolean
        get() = prefs.getBoolean(PREF_NOTIFY_GOG, true)
        set(value) = prefs.edit().putBoolean(PREF_NOTIFY_GOG, value).apply()

    var isNotifyOthers: Boolean
        get() = prefs.getBoolean(PREF_NOTIFY_OTHERS, true)
        set(value) = prefs.edit().putBoolean(PREF_NOTIFY_OTHERS, value).apply()

    var isGamesOnly: Boolean
        get() = prefs.getBoolean(PREF_GAMES_ONLY, false)
        set(value) = prefs.edit().putBoolean(PREF_GAMES_ONLY, value).apply()

    fun shouldNotifyForGiveaway(item: GiveawayItem): Boolean {
        if (!isNotificationsEnabled) return false
        if (isGamesOnly && !item.type.contains("Game", ignoreCase = true)) return false

        val lowerPlat = item.platforms.lowercase()
        val isPs = item.isPlayStation
        val isXb = item.isXbox
        val isNin = item.isNintendo
        val isSteam = lowerPlat.contains("steam")
        val isEpic = lowerPlat.contains("epic")
        val isGog = lowerPlat.contains("gog")

        if (isPs && isNotifyPlayStation) return true
        if (isXb && isNotifyXbox) return true
        if (isNin && isNotifyNintendo) return true
        if (isSteam && isNotifySteam) return true
        if (isEpic && isNotifyEpic) return true
        if (isGog && isNotifyGog) return true
        if (!isSteam && !isEpic && !isGog && !isPs && !isXb && !isNin && isNotifyOthers) return true

        return false
    }

    fun sendNewGameNotification(item: GiveawayItem) {
        if (!shouldNotifyForGiveaway(item)) return

        val title = "Free on ${item.launcherCategory.displayName}: ${item.title}"
        val worthText = if (item.worth.isNotBlank() && item.worth != "N/A") " (Worth ${item.worth})" else ""
        val content = "Claim it now for free!$worthText Tap to view details."

        // Content intent: Opens app and targets the game
        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_GIVEAWAY_ID, item.id)
            putExtra(EXTRA_CLAIM_URL, item.openGiveawayUrl)
        }
        val appPendingIntent = PendingIntent.getActivity(
            context,
            item.id,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action intent: Directly opens the claim URL
        val claimIntent = Intent(Intent.ACTION_VIEW, Uri.parse(item.openGiveawayUrl)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val claimPendingIntent = PendingIntent.getActivity(
            context,
            item.id + 100000,
            claimIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_logo)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText("${item.description}\n\nPlatform: ${item.platforms}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(appPendingIntent)
            .addAction(0, "Claim Free", claimPendingIntent)
            .addAction(0, "View in App", appPendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(item.id, notification)
        } catch (_: SecurityException) {
            // Permission not granted by user yet
        }
    }

    fun sendTestNotification() {
        val testIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            99999,
            testIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_logo)
            .setContentTitle("LootDrop Alerts Activated! 🚀")
            .setContentText("You will receive instant alerts whenever Steam, Epic, or GOG drops a new free game.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(99999, notification)
        } catch (_: SecurityException) {
            // Permission check caught gracefully
        }
    }
}
