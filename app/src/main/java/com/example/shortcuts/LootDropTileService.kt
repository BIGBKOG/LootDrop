package com.example.shortcuts

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.example.MainActivity
import com.example.R

/**
 * Quick Settings Tile for LootDrop Free Games Radar
 *
 * Allows gamers to quickly check today's drops or active freebies directly
 * from the Android notification shade and Quick Settings panel.
 */
class LootDropTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()

        // Launch app into Today's Drops (Discover sorted by Newest)
        val intent = Intent(this, MainActivity::class.java).apply {
            action = AppShortcutHelper.ACTION_TODAYS_DROPS
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AppShortcutHelper.EXTRA_TARGET_TAB, "DISCOVER")
            putExtra(AppShortcutHelper.EXTRA_SORT_OPTION, "NEWEST")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }

        AppShortcutHelper.reportShortcutUsed(this, AppShortcutHelper.ID_TODAYS_DROPS)
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        tile.state = Tile.STATE_ACTIVE
        tile.label = getString(R.string.tile_free_games_label)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = getString(R.string.tile_free_games_subtitle)
        }
        tile.icon = Icon.createWithResource(this, R.drawable.ic_stat_gamepad)
        tile.updateTile()
    }
}
