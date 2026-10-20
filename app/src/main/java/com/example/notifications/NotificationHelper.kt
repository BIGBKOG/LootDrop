package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import coil.Coil
import coil.request.ImageRequest
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.NotificationHistoryEntity
import com.example.data.model.GiveawayItem
import com.example.preferences.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class NotificationHelper(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("lootdrop_notification_prefs", Context.MODE_PRIVATE)
    private val appPreferences = AppPreferences(context)

    companion object {
        const val CHANNEL_MAJOR_GAMES = "lootdrop_major_games_channel"
        const val CHANNEL_ENDING_SOON = "lootdrop_ending_soon_channel"
        const val CHANNEL_DLC = "lootdrop_dlc_channel"
        const val CHANNEL_SYSTEM = "lootdrop_system_channel"

        // Keep legacy alias for backward compatibility
        const val CHANNEL_ID = CHANNEL_MAJOR_GAMES
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
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val majorGamesChannel = NotificationChannel(
                CHANNEL_MAJOR_GAMES,
                "🔥 Major Free Games",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Instant alerts with game art when 100% free PC and console games drop"
                enableVibration(true)
                setShowBadge(true)
            }

            val endingSoonChannel = NotificationChannel(
                CHANNEL_ENDING_SOON,
                "⏳ Expiring Soon Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent alerts 24 hours before free game offers expire"
                enableVibration(true)
                setShowBadge(true)
            }

            val dlcChannel = NotificationChannel(
                CHANNEL_DLC,
                "🎁 DLC, Add-ons & Loot",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for free DLC packs, cosmetic skins, and in-game loot"
                setShowBadge(true)
            }

            val systemChannel = NotificationChannel(
                CHANNEL_SYSTEM,
                "🔔 System & Test Alerts",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Sync status, test previews, and configuration confirmation"
                setShowBadge(false)
            }

            manager.createNotificationChannels(
                listOf(majorGamesChannel, endingSoonChannel, dlcChannel, systemChannel)
            )
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

    var isNotifyEndingSoon: Boolean
        get() = appPreferences.isNotifyEndingSoon
        set(value) {
            appPreferences.isNotifyEndingSoon = value
        }

    var minWorthAlertThreshold: Float
        get() = appPreferences.minWorthAlertThreshold
        set(value) {
            appPreferences.minWorthAlertThreshold = value
        }

    var isQuietHoursEnabled: Boolean
        get() = appPreferences.isQuietHoursEnabled
        set(value) {
            appPreferences.isQuietHoursEnabled = value
        }

    var isNotificationSoundEnabled: Boolean
        get() = appPreferences.isNotificationSoundEnabled
        set(value) {
            appPreferences.isNotificationSoundEnabled = value
        }

    var isNotificationVibrateEnabled: Boolean
        get() = appPreferences.isNotificationVibrateEnabled
        set(value) {
            appPreferences.isNotificationVibrateEnabled = value
        }

    var isKeepForeverOnly: Boolean
        get() = appPreferences.isKeepForeverOnly
        set(value) {
            appPreferences.isKeepForeverOnly = value
        }

    var isNotificationRichArtwork: Boolean
        get() = appPreferences.isNotificationRichArtwork
        set(value) {
            appPreferences.isNotificationRichArtwork = value
        }

    var isNotificationActionsEnabled: Boolean
        get() = appPreferences.isNotificationActionsEnabled
        set(value) {
            appPreferences.isNotificationActionsEnabled = value
        }

    fun isQuietHoursActive(): Boolean {
        if (!isQuietHoursEnabled) return false
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val start = appPreferences.quietHoursStartHour
        val end = appPreferences.quietHoursEndHour

        return if (start > end) {
            // e.g. 23:00 to 08:00
            currentHour >= start || currentHour < end
        } else {
            // e.g. 01:00 to 07:00
            currentHour in start until end
        }
    }

    fun shouldNotifyForGiveaway(item: GiveawayItem): Boolean {
        if (!isNotificationsEnabled) return false
        if (isQuietHoursActive()) return false

        // Filter games only
        val isGame = item.type.contains("Game", ignoreCase = true)
        if (isGamesOnly && !isGame) return false

        // Keep Forever filter (skip temporary free-to-play periods or free weekend promotions)
        if (isKeepForeverOnly) {
            val titleLower = item.title.lowercase()
            val descLower = item.description.lowercase()
            val instLower = item.instructions.lowercase()
            val isTemporary = titleLower.contains("free weekend") ||
                    titleLower.contains("trial") ||
                    descLower.contains("free weekend") ||
                    descLower.contains("play for free until") ||
                    descLower.contains("limited time trial") ||
                    instLower.contains("free weekend")
            if (isTemporary) return false
        }

        // Minimum worth threshold
        val minWorth = minWorthAlertThreshold
        if (minWorth > 0f && item.numericWorth < minWorth) {
            return false
        }

        // Platform filters
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

        val isGame = item.type.contains("Game", ignoreCase = true)
        val channelId = if (isGame) CHANNEL_MAJOR_GAMES else CHANNEL_DLC

        val storeName = item.launcherCategory.displayName
        val worthText = if (item.worth.isNotBlank() && item.worth != "N/A") "Worth ${item.worth}" else "100% OFF"
        val title = "🎮 FREE NOW on $storeName: ${item.title}"
        val content = "$worthText • Claim it to keep forever! Tap to view details."

        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = if (isNotificationRichArtwork) {
                fetchBitmap(item.image.ifBlank { item.thumbnail })
            } else null

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

            val claimIntent = Intent(Intent.ACTION_VIEW, Uri.parse(item.openGiveawayUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val claimPendingIntent = PendingIntent.getActivity(
                context,
                item.id + 100000,
                claimIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val bookmarkIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_BOOKMARK
                putExtra(NotificationActionReceiver.EXTRA_GIVEAWAY_ID, item.id)
                putExtra(NotificationActionReceiver.EXTRA_GIVEAWAY_TITLE, item.title)
            }
            val bookmarkPendingIntent = PendingIntent.getBroadcast(
                context,
                item.id + 200000,
                bookmarkIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT,
                    "🔥 Free Game Alert: ${item.title} is currently 100% FREE on $storeName ($worthText)!\nClaim here: ${item.openGiveawayUrl}"
                )
            }
            val sharePendingIntent = PendingIntent.getActivity(
                context,
                item.id + 300000,
                Intent.createChooser(shareIntent, "Share Free Game Deal"),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_stat_gamepad)
                .setContentTitle(title)
                .setContentText(content)
                .setSubText("$storeName • $worthText")
                .setColor(0xFF6C5CE7.toInt())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(appPendingIntent)

            if (!isNotificationSoundEnabled) {
                builder.setSilent(true)
            }
            if (isNotificationVibrateEnabled) {
                builder.setVibrate(longArrayOf(0, 250, 150, 250))
            } else {
                builder.setVibrate(longArrayOf(0))
            }

            if (isNotificationActionsEnabled) {
                builder.addAction(0, "Claim Free", claimPendingIntent)
                    .addAction(0, "★ Bookmark", bookmarkPendingIntent)
                    .addAction(0, "Share", sharePendingIntent)
            }

            if (bitmap != null) {
                builder.setLargeIcon(bitmap)
                builder.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .setBigContentTitle("🎮 ${item.title} is Free!")
                        .setSummaryText("${item.description.take(120)}... • Platform: ${item.platforms}")
                )
            } else {
                builder.setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("${item.description}\n\nPlatform: ${item.platforms}\nValue: $worthText")
                )
            }

            try {
                NotificationManagerCompat.from(context).notify(item.id, builder.build())
                recordNotificationHistory(
                    item = item,
                    channelType = if (isGame) "MAJOR_GAME" else "DLC"
                )
            } catch (_: SecurityException) {
                // Permission not granted
            }
        }
    }

    fun sendEndingSoonNotification(item: GiveawayItem) {
        if (!isNotificationsEnabled || isQuietHoursActive()) return

        val title = "⏳ ENDING SOON: ${item.title}"
        val content = "Last chance! This freebie on ${item.launcherCategory.displayName} expires in less than 24h."

        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = if (isNotificationRichArtwork) {
                fetchBitmap(item.image.ifBlank { item.thumbnail })
            } else null

            val appIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_GIVEAWAY_ID, item.id)
                putExtra(EXTRA_CLAIM_URL, item.openGiveawayUrl)
            }
            val appPendingIntent = PendingIntent.getActivity(
                context,
                item.id + 400000,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val claimIntent = Intent(Intent.ACTION_VIEW, Uri.parse(item.openGiveawayUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val claimPendingIntent = PendingIntent.getActivity(
                context,
                item.id + 500000,
                claimIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ENDING_SOON)
                .setSmallIcon(R.drawable.ic_stat_gamepad)
                .setContentTitle(title)
                .setContentText(content)
                .setSubText("Ends Soon • ${item.launcherCategory.displayName}")
                .setColor(0xFFFF5252.toInt())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(appPendingIntent)

            if (!isNotificationSoundEnabled) {
                builder.setSilent(true)
            }
            if (isNotificationVibrateEnabled) {
                builder.setVibrate(longArrayOf(0, 250, 150, 250))
            } else {
                builder.setVibrate(longArrayOf(0))
            }

            if (isNotificationActionsEnabled) {
                builder.addAction(0, "Claim Now", claimPendingIntent)
                    .addAction(0, "View Details", appPendingIntent)
            }

            if (bitmap != null) {
                builder.setLargeIcon(bitmap)
                builder.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .setBigContentTitle("⏳ Don't Miss Out: ${item.title}")
                        .setSummaryText("Offer ends soon on ${item.platforms}. Tap to claim before price returns.")
                )
            } else {
                builder.setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("This 100% free giveaway for ${item.title} on ${item.platforms} is expiring soon. Grab it before the free offer ends!")
                )
            }

            try {
                NotificationManagerCompat.from(context).notify(item.id + 900000, builder.build())
                recordNotificationHistory(
                    item = item,
                    channelType = "ENDING_SOON"
                )
            } catch (_: SecurityException) {
                // Permission not granted
            }
        }
    }

    fun sendTestNotification(isRichPreview: Boolean = true) {
        val testIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            99999,
            testIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val claimIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://store.epicgames.com")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val claimPendingIntent = PendingIntent.getActivity(
            context,
            99998,
            claimIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        CoroutineScope(Dispatchers.IO).launch {
            val sampleItem = GiveawayItem(
                id = 99999,
                title = "Death Stranding: Director's Cut",
                worth = "$39.99",
                numericWorth = 39.99,
                thumbnail = "https://www.gamerpower.com/offers/1/64070a996dc8e.jpg",
                image = "https://www.gamerpower.com/offers/1/64070a996dc8e.jpg",
                description = "From legendary game creator Hideo Kojima comes a genre-defying experience. Grab the full PC edition completely free!",
                instructions = "Claim on Epic Games Store before promotion ends.",
                openGiveawayUrl = "https://store.epicgames.com",
                publishedDate = "2026-10-19",
                type = "Game",
                platforms = "Epic Games, PC",
                endDate = "2026-12-31",
                users = 15200,
                status = "Active"
            )

            val bitmap = if (isRichPreview) {
                fetchBitmap(sampleItem.image)
            } else null

            val builder = NotificationCompat.Builder(context, CHANNEL_MAJOR_GAMES)
                .setSmallIcon(R.drawable.ic_stat_gamepad)
                .setContentTitle("🎮 FREE NOW on Epic Games: Death Stranding")
                .setContentText("Worth $39.99 • 100% OFF! Claim now to keep forever.")
                .setSubText("Epic Games • Worth $39.99")
                .setColor(0xFF6C5CE7.toInt())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            if (!isNotificationSoundEnabled) {
                builder.setSilent(true)
            }
            if (isNotificationVibrateEnabled) {
                builder.setVibrate(longArrayOf(0, 250, 150, 250))
            } else {
                builder.setVibrate(longArrayOf(0))
            }

            if (isNotificationActionsEnabled) {
                builder.addAction(0, "Claim Free", claimPendingIntent)
                    .addAction(0, "View in App", pendingIntent)
            }

            if (bitmap != null) {
                builder.setLargeIcon(bitmap)
                builder.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .setBigContentTitle("🎮 Death Stranding: Director's Cut is Free!")
                        .setSummaryText("Free for a limited time on Epic Games Store • Worth $39.99")
                )
            } else {
                builder.setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("From legendary game creator Hideo Kojima comes a genre-defying experience. Now expanded in this definitive Director's Cut. 100% Free on Epic Games Store (Worth $39.99).")
                )
            }

            try {
                NotificationManagerCompat.from(context).notify(99999, builder.build())
                recordNotificationHistory(sampleItem, "MAJOR_GAME")
            } catch (_: SecurityException) {
                // Permission not granted
            }
        }
    }

    fun sendTestEndingSoonNotification() {
        val sampleItem = GiveawayItem(
            id = 99998,
            title = "Fallout: New Vegas Ultimate Edition",
            worth = "$19.99",
            numericWorth = 19.99,
            thumbnail = "https://www.gamerpower.com/offers/1/64070a996dc8e.jpg",
            image = "https://www.gamerpower.com/offers/1/64070a996dc8e.jpg",
            description = "Last call to claim Fallout: New Vegas for free! Deal expires within 24 hours.",
            instructions = "Redeem on Steam",
            openGiveawayUrl = "https://store.steampowered.com",
            publishedDate = "2026-10-18",
            type = "Game",
            platforms = "Steam, PC",
            endDate = "2026-10-20",
            users = 8400,
            status = "Active"
        )
        sendEndingSoonNotification(sampleItem)
    }

    private suspend fun fetchBitmap(url: String): Bitmap? = withContext(Dispatchers.IO) {
        if (url.isBlank()) return@withContext null
        try {
            val loader = Coil.imageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .size(640, 360)
                .build()
            val result = loader.execute(request)
            (result.drawable as? BitmapDrawable)?.bitmap
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun recordNotificationHistory(item: GiveawayItem, channelType: String) =
        withContext(Dispatchers.IO) {
            try {
                val db = AppDatabase.getInstance(context)
                val entry = NotificationHistoryEntity(
                    giveawayId = item.id,
                    title = item.title,
                    worth = item.worth,
                    platforms = item.platforms,
                    thumbnail = item.thumbnail,
                    image = item.image,
                    openGiveawayUrl = item.openGiveawayUrl,
                    channelType = channelType,
                    timestamp = System.currentTimeMillis(),
                    isRead = false
                )
                db.notificationHistoryDao().insertNotification(entry)
                db.notificationHistoryDao().trimOldNotifications()
            } catch (_: Exception) {
                // Ignore history recording failures
            }
        }
}
