package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.widget.RemoteViews
import coil.Coil
import coil.request.ImageRequest
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.notifications.NotificationHelper
import com.example.util.MetacriticCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.Locale

object WidgetUpdateHelper {

    const val ACTION_REFRESH_WIDGETS = "com.example.widget.ACTION_REFRESH_WIDGETS"

    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context.applicationContext)

        // Update Latest Drop widgets
        val latestIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, LatestDropWidgetProvider::class.java)
        )
        if (latestIds.isNotEmpty()) {
            LatestDropWidgetProvider.updateAppWidgets(context, appWidgetManager, latestIds)
        }

        // Update Radar Stats widgets
        val statsIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, RadarStatsWidgetProvider::class.java)
        )
        if (statsIds.isNotEmpty()) {
            RadarStatsWidgetProvider.updateAppWidgets(context, appWidgetManager, statsIds)
        }
    }

    fun triggerManualSync(context: Context) {
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(appContext)
                val notificationHelper = NotificationHelper(appContext)
                val repository = com.example.data.repository.GiveawaysRepository(db.giveawayDao(), notificationHelper)
                repository.refreshGiveaways()
            } catch (_: Exception) {
            } finally {
                updateAllWidgets(appContext)
            }
        }
    }

    fun createRoundedBitmap(
        src: Bitmap,
        cornerRadiusPx: Float = 16f,
        targetWidth: Int = 240,
        targetHeight: Int = 160
    ): Bitmap {
        val scaled = Bitmap.createScaledBitmap(src, targetWidth, targetHeight, true)
        val output = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = Rect(0, 0, targetWidth, targetHeight)
        val rectF = RectF(rect)
        canvas.drawRoundRect(rectF, cornerRadiusPx, cornerRadiusPx, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(scaled, rect, rect, paint)
        return output
    }

    suspend fun loadGameBitmap(
        context: Context,
        imageUrl: String,
        targetWidth: Int = 240,
        targetHeight: Int = 160
    ): Bitmap? = withContext(Dispatchers.IO) {
        if (imageUrl.isBlank()) return@withContext null
        try {
            val loader = Coil.imageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false) // Mandatory for RemoteViews Binder IPC
                .size(targetWidth, targetHeight)
                .build()
            val result = loader.execute(request)
            val drawable = result.drawable ?: return@withContext null
            val rawBitmap = when (drawable) {
                is BitmapDrawable -> drawable.bitmap
                else -> {
                    val bmp = Bitmap.createBitmap(
                        targetWidth.coerceAtLeast(1),
                        targetHeight.coerceAtLeast(1),
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(bmp)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)
                    bmp
                }
            } ?: return@withContext null

            createRoundedBitmap(rawBitmap, cornerRadiusPx = 16f, targetWidth, targetHeight)
        } catch (_: Throwable) {
            null
        }
    }

    fun buildLatestDropRemoteViews(context: Context, appWidgetId: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_latest_drop)
        views.setImageViewResource(R.id.widget_game_image, R.drawable.widget_image_placeholder)

        // Refresh intent
        val refreshIntent = Intent(context, LatestDropWidgetProvider::class.java).apply {
            action = ACTION_REFRESH_WIDGETS
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)

        // Open app when tapping the widget root
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPending = PendingIntent.getActivity(
            context,
            appWidgetId * 10 + 1,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root_latest, openAppPending)

        views.setTextViewText(R.id.widget_game_title, "Loading free drops...")
        views.setTextViewText(R.id.widget_platform_badge, "PC / Console")
        views.setTextViewText(R.id.widget_original_price, "FREE")
        views.setTextViewText(R.id.widget_metascore, "★ NEW")

        return views
    }

    fun populateLatestDrop(context: Context, views: RemoteViews, appWidgetId: Int) {
        // Refresh intent
        val refreshIntent = Intent(context, LatestDropWidgetProvider::class.java).apply {
            action = ACTION_REFRESH_WIDGETS
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)

        // Root intent to open main app
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPending = PendingIntent.getActivity(
            context,
            appWidgetId * 10 + 1,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root_latest, openAppPending)

        try {
            val db = AppDatabase.getInstance(context)
            val list = runBlocking(Dispatchers.IO) {
                db.giveawayDao().getAllGiveawaysList()
            }
            val active = list.filter { it.status != "Ended" && it.status != "expired" }
            val item = active.maxByOrNull { it.firstSeenTimestamp } ?: active.firstOrNull()

            if (item != null) {
                views.setTextViewText(R.id.widget_game_title, item.title)

                val platformLabel = item.platforms.split(",").firstOrNull()?.trim() ?: "PC"
                views.setTextViewText(R.id.widget_platform_badge, platformLabel)

                if (item.worth.isNotBlank() && item.worth != "N/A" && item.worth != "FREE") {
                    views.setTextViewText(R.id.widget_original_price, item.worth)
                } else {
                    views.setTextViewText(R.id.widget_original_price, "$19.99")
                }

                val score = MetacriticCatalog.resolveScore(item.title, item.type, item.description)
                views.setTextViewText(R.id.widget_metascore, "★ $score")

                // Claim Button launches the giveaway URL
                val claimUrl = if (item.openGiveawayUrl.isNotBlank()) item.openGiveawayUrl else "https://www.gamerpower.com"
                val claimIntent = Intent(Intent.ACTION_VIEW, Uri.parse(claimUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                val claimPendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId * 10 + 2,
                    claimIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_claim, claimPendingIntent)

                // Details Button opens the game dialog in app
                val detailsIntent = Intent(context, MainActivity::class.java).apply {
                    putExtra(NotificationHelper.EXTRA_GIVEAWAY_ID, item.id)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val detailsPendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId * 10 + 3,
                    detailsIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_details, detailsPendingIntent)
                views.setOnClickPendingIntent(R.id.widget_game_image, detailsPendingIntent)

                // Load Game Picture with Coil
                val imageCandidates = listOf(item.thumbnail, item.image).filter { it.isNotBlank() }
                var bitmap: Bitmap? = null
                runBlocking(Dispatchers.IO) {
                    for (url in imageCandidates) {
                        bitmap = loadGameBitmap(context, url)
                        if (bitmap != null) break
                    }
                }

                if (bitmap != null) {
                    views.setImageViewBitmap(R.id.widget_game_image, bitmap)
                } else {
                    views.setImageViewResource(R.id.widget_game_image, R.drawable.widget_image_placeholder)
                }
            } else {
                views.setTextViewText(R.id.widget_game_title, "Tap to check free games")
                views.setTextViewText(R.id.widget_platform_badge, "LootDrop")
                views.setTextViewText(R.id.widget_original_price, "")
                views.setTextViewText(R.id.widget_metascore, "★ 100%")
                views.setImageViewResource(R.id.widget_game_image, R.drawable.widget_image_placeholder)

                // If database has never been populated, trigger initial background sync
                if (list.isEmpty()) {
                    triggerManualSync(context)
                }
            }
        } catch (_: Exception) {
            views.setImageViewResource(R.id.widget_game_image, R.drawable.widget_image_placeholder)
        }
    }

    fun populateRadarStats(context: Context, views: RemoteViews, appWidgetId: Int) {
        // Refresh intent
        val refreshIntent = Intent(context, RadarStatsWidgetProvider::class.java).apply {
            action = ACTION_REFRESH_WIDGETS
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_radar_refresh, refreshPendingIntent)

        // Open Radar (Discover tab) intent
        val radarIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("EXTRA_TARGET_TAB", "DISCOVER")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val radarPending = PendingIntent.getActivity(
            context,
            appWidgetId * 10 + 4,
            radarIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_open_radar, radarPending)

        // Open Vault (Claimed tab) intent
        val vaultIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("EXTRA_TARGET_TAB", "CLAIMED")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val vaultPending = PendingIntent.getActivity(
            context,
            appWidgetId * 10 + 5,
            vaultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_open_vault, vaultPending)

        // Root click navigates to main app
        views.setOnClickPendingIntent(R.id.widget_root_radar, radarPending)

        try {
            val db = AppDatabase.getInstance(context)
            val list = runBlocking(Dispatchers.IO) {
                db.giveawayDao().getAllGiveawaysList()
            }
            val activeCount = list.count { it.status != "Ended" && it.status != "expired" }
            val savedTotal = list.filter { it.isClaimed }.sumOf { it.numericWorth }

            views.setTextViewText(R.id.widget_stat_active_count, "$activeCount Active")
            views.setTextViewText(R.id.widget_stat_saved_dollars, String.format(Locale.US, "$%.2f", savedTotal))
        } catch (_: Exception) {
            views.setTextViewText(R.id.widget_stat_active_count, "Radar Online")
            views.setTextViewText(R.id.widget_stat_saved_dollars, "$0.00")
        }
    }
}
