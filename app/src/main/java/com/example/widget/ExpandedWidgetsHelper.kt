package com.example.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.model.UnifiedLibraryItem
import com.example.i18n.AppLanguage
import com.example.i18n.LocaleSyncHelper
import com.example.notifications.NotificationHelper
import com.example.preferences.AppPreferences
import com.example.ui.calendar.ExpirationCalendarUtils
import com.example.util.BacklogChallengeManager
import com.example.util.MetacriticCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.util.Locale

/**
 * Populates the expanded suite of LootDrop home screen widgets:
 * - Ending Soon Radar (4x2)
 * - Top 3 Free Drops (4x2)
 * - Weekly 3x3 Backlog Challenge (3x2)
 * - Compact Vault & Wishlist (2x2)
 */
internal object ExpandedWidgetsHelper {

    private fun mainTabPendingIntent(
        context: Context,
        requestCode: Int,
        targetTab: String? = null,
        giveawayId: Int? = null,
        openBacklogChallenge: Boolean = false
    ): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            if (targetTab != null) putExtra("EXTRA_TARGET_TAB", targetTab)
            if (giveawayId != null) putExtra(NotificationHelper.EXTRA_GIVEAWAY_ID, giveawayId)
            if (openBacklogChallenge) putExtra("EXTRA_OPEN_BACKLOG_CHALLENGE", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun refreshPendingIntent(
        context: Context,
        providerClass: Class<*>,
        requestCode: Int
    ): PendingIntent {
        val intent = Intent(context, providerClass).apply {
            action = WidgetUpdateHelper.ACTION_REFRESH_WIDGETS
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun populateExpiringSoon(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        db: AppDatabase = AppDatabase.getInstance(context)
    ) {
        val localizedContext = LocaleSyncHelper.localizedContextFromPrefs(context)
        val res = localizedContext.resources

        views.setTextViewText(R.id.widget_exp_header_title, res.getString(R.string.widget_expiring_header))
        views.setTextViewText(R.id.widget_exp_calendar_text, res.getString(R.string.widget_open_calendar_btn))
        views.setTextViewText(R.id.widget_exp_empty, res.getString(R.string.widget_expiring_empty))
        views.setContentDescription(R.id.widget_exp_refresh, res.getString(R.string.widget_refresh_drops))

        views.setOnClickPendingIntent(
            R.id.widget_exp_refresh,
            refreshPendingIntent(context, ExpiringSoonWidgetProvider::class.java, appWidgetId * 100 + 10)
        )
        val calendarPending = mainTabPendingIntent(context, appWidgetId * 100 + 11, targetTab = "CALENDAR")
        views.setOnClickPendingIntent(R.id.widget_exp_btn_calendar, calendarPending)
        views.setOnClickPendingIntent(R.id.widget_root_expiring, calendarPending)

        val rowIds = intArrayOf(R.id.widget_exp_row_1, R.id.widget_exp_row_2, R.id.widget_exp_row_3)
        val timeIds = intArrayOf(R.id.widget_exp_time_1, R.id.widget_exp_time_2, R.id.widget_exp_time_3)
        val titleIds = intArrayOf(R.id.widget_exp_title_1, R.id.widget_exp_title_2, R.id.widget_exp_title_3)
        val metaIds = intArrayOf(R.id.widget_exp_meta_1, R.id.widget_exp_meta_2, R.id.widget_exp_meta_3)

        try {
            val list = runBlocking(Dispatchers.IO) { db.giveawayDao().getAllGiveawaysList() }
            val expiring = list
                .filter { it.status != "Ended" && it.status != "expired" }
                .mapNotNull { entity ->
                    val days = ExpirationCalendarUtils.daysRemaining(entity.endDate)
                    if (days != null && days >= 0L) entity to days else null
                }
                .sortedWith(
                    compareBy<Pair<com.example.data.local.GiveawayEntity, Long>> { it.second }
                        .thenBy { it.first.isClaimed }
                        .thenByDescending { it.first.numericWorth }
                )

            views.setTextViewText(R.id.widget_exp_count_badge, expiring.size.toString())

            if (expiring.isEmpty()) {
                rowIds.forEach { views.setViewVisibility(it, View.GONE) }
                views.setViewVisibility(R.id.widget_exp_empty, View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.widget_exp_empty, View.GONE)
                val todayLabel = res.getString(R.string.widget_expiring_today)
                for (i in rowIds.indices) {
                    val entry = expiring.getOrNull(i)
                    if (entry == null) {
                        views.setViewVisibility(rowIds[i], View.GONE)
                    } else {
                        val (item, days) = entry
                        views.setViewVisibility(rowIds[i], View.VISIBLE)
                        views.setTextViewText(
                            timeIds[i],
                            if (days == 0L) todayLabel else "${days}d"
                        )
                        views.setTextViewText(titleIds[i], item.title)
                        val platform = item.platforms.split(",").firstOrNull()?.trim().orEmpty().ifBlank { "PC" }
                        val worth = if (item.worth.isNotBlank() && item.worth != "N/A") " • ${item.worth}" else ""
                        views.setTextViewText(metaIds[i], "$platform$worth")
                        views.setOnClickPendingIntent(
                            rowIds[i],
                            mainTabPendingIntent(
                                context = context,
                                requestCode = appWidgetId * 100 + 12 + i,
                                giveawayId = item.id
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            views.setTextViewText(R.id.widget_exp_count_badge, "0")
            rowIds.forEach { views.setViewVisibility(it, View.GONE) }
            views.setViewVisibility(R.id.widget_exp_empty, View.VISIBLE)
        }
    }

    fun populateTopDropsList(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        db: AppDatabase = AppDatabase.getInstance(context)
    ) {
        val localizedContext = LocaleSyncHelper.localizedContextFromPrefs(context)
        val res = localizedContext.resources
        val freeWord = res.getString(R.string.widget_free_badge)

        views.setTextViewText(R.id.widget_top_header_title, res.getString(R.string.widget_top_drops_header))
        views.setTextViewText(R.id.widget_top_explore_text, res.getString(R.string.widget_explore_radar_btn))
        views.setTextViewText(R.id.widget_top_empty, res.getString(R.string.widget_top_drops_empty))
        views.setTextViewText(R.id.widget_top_free_1, freeWord)
        views.setTextViewText(R.id.widget_top_free_2, freeWord)
        views.setTextViewText(R.id.widget_top_free_3, freeWord)
        views.setContentDescription(R.id.widget_top_refresh, res.getString(R.string.widget_refresh_drops))

        views.setOnClickPendingIntent(
            R.id.widget_top_refresh,
            refreshPendingIntent(context, TopDropsListWidgetProvider::class.java, appWidgetId * 100 + 20)
        )
        val explorePending = mainTabPendingIntent(context, appWidgetId * 100 + 21, targetTab = "DISCOVER")
        views.setOnClickPendingIntent(R.id.widget_top_btn_explore, explorePending)
        views.setOnClickPendingIntent(R.id.widget_root_top_drops, explorePending)

        val rowIds = intArrayOf(R.id.widget_top_row_1, R.id.widget_top_row_2, R.id.widget_top_row_3)
        val titleIds = intArrayOf(R.id.widget_top_title_1, R.id.widget_top_title_2, R.id.widget_top_title_3)
        val metaIds = intArrayOf(R.id.widget_top_meta_1, R.id.widget_top_meta_2, R.id.widget_top_meta_3)

        try {
            val list = runBlocking(Dispatchers.IO) { db.giveawayDao().getAllGiveawaysList() }
            val active = list.filter { it.status != "Ended" && it.status != "expired" }
            val ranked = active.sortedWith(
                compareByDescending<com.example.data.local.GiveawayEntity> {
                    it.type.equals("Game", ignoreCase = true)
                }.thenByDescending {
                    MetacriticCatalog.resolveScore(it.title, it.type, it.description)
                }.thenByDescending {
                    it.numericWorth
                }
            )

            views.setTextViewText(R.id.widget_top_count_pill, "${active.size} $freeWord")

            if (ranked.isEmpty()) {
                rowIds.forEach { views.setViewVisibility(it, View.GONE) }
                views.setViewVisibility(R.id.widget_top_empty, View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.widget_top_empty, View.GONE)
                for (i in rowIds.indices) {
                    val item = ranked.getOrNull(i)
                    if (item == null) {
                        views.setViewVisibility(rowIds[i], View.GONE)
                    } else {
                        views.setViewVisibility(rowIds[i], View.VISIBLE)
                        views.setTextViewText(titleIds[i], item.title)
                        val platform = item.platforms.split(",").firstOrNull()?.trim().orEmpty().ifBlank { "PC" }
                        val score = MetacriticCatalog.resolveScore(item.title, item.type, item.description)
                        val worth = if (item.worth.isNotBlank() && item.worth != "N/A") " • ${item.worth}" else ""
                        views.setTextViewText(metaIds[i], "$platform • ★$score$worth")
                        views.setOnClickPendingIntent(
                            rowIds[i],
                            mainTabPendingIntent(
                                context = context,
                                requestCode = appWidgetId * 100 + 22 + i,
                                giveawayId = item.id
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            views.setTextViewText(R.id.widget_top_count_pill, "0 $freeWord")
            rowIds.forEach { views.setViewVisibility(it, View.GONE) }
            views.setViewVisibility(R.id.widget_top_empty, View.VISIBLE)
        }
    }

    fun populateBacklogChallenge(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        db: AppDatabase = AppDatabase.getInstance(context)
    ) {
        val localizedContext = LocaleSyncHelper.localizedContextFromPrefs(context)
        val res = localizedContext.resources
        val prefs = AppPreferences(context)
        val isEs = prefs.appLanguage == AppLanguage.SPANISH

        views.setTextViewText(R.id.widget_challenge_header_title, res.getString(R.string.widget_challenge_header))
        views.setTextViewText(R.id.widget_challenge_next_label, res.getString(R.string.widget_challenge_next_label))
        views.setTextViewText(R.id.widget_challenge_open_text, res.getString(R.string.widget_open_grid_btn))
        views.setContentDescription(R.id.widget_challenge_refresh, res.getString(R.string.widget_refresh_stats))

        views.setOnClickPendingIntent(
            R.id.widget_challenge_refresh,
            refreshPendingIntent(context, BacklogChallengeWidgetProvider::class.java, appWidgetId * 100 + 30)
        )
        val challengePending = mainTabPendingIntent(
            context = context,
            requestCode = appWidgetId * 100 + 31,
            openBacklogChallenge = true
        )
        views.setOnClickPendingIntent(R.id.widget_challenge_btn_open, challengePending)
        views.setOnClickPendingIntent(R.id.widget_root_challenge, challengePending)

        val tileIds = intArrayOf(
            R.id.widget_tile_0, R.id.widget_tile_1, R.id.widget_tile_2,
            R.id.widget_tile_3, R.id.widget_tile_4, R.id.widget_tile_5,
            R.id.widget_tile_6, R.id.widget_tile_7, R.id.widget_tile_8
        )

        try {
            val (giveaways, steamGames, trophies) = runBlocking(Dispatchers.IO) {
                Triple(
                    db.giveawayDao().getAllGiveawaysList(),
                    db.steamGameDao().getAllSteamGamesList(),
                    db.steamAchievementDao().getRarestTrophies(100)
                )
            }
            val unifiedGames = giveaways.filter { it.isClaimed }.map { UnifiedLibraryItem.Vault(it.toDomain()) } +
                steamGames.map { UnifiedLibraryItem.Steam(it) }
            val grid = BacklogChallengeManager.getOrGenerateGrid(
                preferences = prefs,
                weekKey = BacklogChallengeManager.getCurrentWeekKey(),
                games = unifiedGames,
                trophies = trophies,
                language = prefs.appLanguage
            )

            for (i in tileIds.indices) {
                val tile = grid.tiles.getOrNull(i) ?: continue
                views.setTextViewText(tileIds[i], if (tile.isCompleted) "✓" else tile.emoji)
                views.setInt(
                    tileIds[i],
                    "setBackgroundResource",
                    if (tile.isCompleted) R.drawable.widget_tile_done else R.drawable.widget_tile_pending
                )
                views.setTextColor(
                    tileIds[i],
                    if (tile.isCompleted) Color.parseColor("#00E676") else Color.WHITE
                )
            }

            views.setTextViewText(R.id.widget_challenge_progress_text, "${grid.completedCount}/9")
            val lineWord = if (isEs) {
                if (grid.totalLinesCompleted == 1) "Línea" else "Líneas"
            } else {
                if (grid.totalLinesCompleted == 1) "Line" else "Lines"
            }
            views.setTextViewText(R.id.widget_challenge_lines_text, "• ${grid.totalLinesCompleted} $lineWord")
            views.setTextViewText(R.id.widget_challenge_days_badge, "${grid.daysRemaining}d")

            val nextTile = grid.tiles.firstOrNull { !it.isCompleted }
            val nextHint = nextTile?.title ?: if (isEs) "¡Tablero 3×3 completado!" else "All 9 Tiles Completed!"
            views.setTextViewText(R.id.widget_challenge_next_hint, nextHint)
        } catch (_: Exception) {
            views.setTextViewText(R.id.widget_challenge_progress_text, "0/9")
            views.setTextViewText(R.id.widget_challenge_lines_text, if (isEs) "• 0 Líneas" else "• 0 Lines")
        }
    }

    fun populateCompactVault(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        db: AppDatabase = AppDatabase.getInstance(context)
    ) {
        val localizedContext = LocaleSyncHelper.localizedContextFromPrefs(context)
        val res = localizedContext.resources

        views.setTextViewText(R.id.widget_compact_header_title, res.getString(R.string.widget_compact_vault_header))
        views.setTextViewText(R.id.widget_compact_vault_text, res.getString(R.string.widget_vault_btn))
        views.setTextViewText(R.id.widget_compact_radar_text, res.getString(R.string.widget_radar_btn))
        views.setContentDescription(R.id.widget_compact_refresh, res.getString(R.string.widget_refresh_stats))

        views.setOnClickPendingIntent(
            R.id.widget_compact_refresh,
            refreshPendingIntent(context, CompactVaultWidgetProvider::class.java, appWidgetId * 100 + 40)
        )
        val vaultPending = mainTabPendingIntent(context, appWidgetId * 100 + 41, targetTab = "CLAIMED")
        val radarPending = mainTabPendingIntent(context, appWidgetId * 100 + 42, targetTab = "DISCOVER")
        views.setOnClickPendingIntent(R.id.widget_compact_btn_vault, vaultPending)
        views.setOnClickPendingIntent(R.id.widget_compact_btn_radar, radarPending)
        views.setOnClickPendingIntent(R.id.widget_root_compact_vault, vaultPending)

        val claimedLabel = res.getString(R.string.widget_claimed_label)
        val savedLabel = res.getString(R.string.widget_saved_label)

        try {
            val list = runBlocking(Dispatchers.IO) { db.giveawayDao().getAllGiveawaysList() }
            val claimed = list.filter { it.isClaimed }
            val savedTotal = claimed.sumOf { it.numericWorth }
            val bookmarkedOrWishlisted = list.count { it.isBookmarked || it.isWishlisted }

            views.setTextViewText(R.id.widget_compact_saved_dollars, String.format(Locale.US, "$%.2f", savedTotal))
            views.setTextViewText(
                R.id.widget_compact_counts_line,
                "${claimed.size} $claimedLabel • $bookmarkedOrWishlisted $savedLabel"
            )
        } catch (_: Exception) {
            views.setTextViewText(R.id.widget_compact_saved_dollars, "$0.00")
            views.setTextViewText(R.id.widget_compact_counts_line, "0 $claimedLabel • 0 $savedLabel")
        }
    }
}
