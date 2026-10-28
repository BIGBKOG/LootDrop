package com.example.shortcuts

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.MainActivity
import com.example.R

/**
 * Android Dynamic Shortcuts Helper (ShortcutManagerCompat)
 *
 * Publishes and updates Android home screen long-press shortcuts:
 * 1. "Expiring Soon" -> jumps to Calendar tab
 * 2. "Today's Drops" -> jumps to Discover tab sorted by Newest
 * 3. "My Vault" -> jumps to Claimed tab
 */
object AppShortcutHelper {

    private const val TAG = "AppShortcutHelper"

    const val ID_EXPIRING_SOON = "shortcut_expiring_soon"
    const val ID_TODAYS_DROPS = "shortcut_todays_drops"
    const val ID_MY_VAULT = "shortcut_my_vault"

    const val ACTION_EXPIRING_SOON = "com.example.action.EXPIRING_SOON"
    const val ACTION_TODAYS_DROPS = "com.example.action.TODAYS_DROPS"
    const val ACTION_MY_VAULT = "com.example.action.MY_VAULT"

    const val EXTRA_TARGET_TAB = "EXTRA_TARGET_TAB"
    const val EXTRA_SORT_OPTION = "EXTRA_SORT_OPTION"

    /**
     * Registers or refreshes the 3 dynamic shortcuts via ShortcutManagerCompat.
     * Optionally attaches dynamic counts if available from the data repository.
     */
    fun updateDynamicShortcuts(
        context: Context,
        expiringCount: Int? = null,
        newDropsCount: Int? = null,
        vaultCount: Int? = null
    ) {
        try {
            val expiringIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_EXPIRING_SOON
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_TARGET_TAB, "CALENDAR")
            }

            val expiringLongLabel = if (expiringCount != null && expiringCount > 0) {
                "Expiring Soon ($expiringCount Deals)"
            } else {
                "Expiring Soon Deals"
            }

            val expiringShortcut = ShortcutInfoCompat.Builder(context, ID_EXPIRING_SOON)
                .setShortLabel("Expiring Soon")
                .setLongLabel(expiringLongLabel)
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_calendar))
                .setIntent(expiringIntent)
                .setRank(1)
                .build()

            val dropsIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_TODAYS_DROPS
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_TARGET_TAB, "DISCOVER")
                putExtra(EXTRA_SORT_OPTION, "NEWEST")
            }

            val dropsLongLabel = if (newDropsCount != null && newDropsCount > 0) {
                "Today's Drops ($newDropsCount New)"
            } else {
                "Today's Free Drops (Newest)"
            }

            val dropsShortcut = ShortcutInfoCompat.Builder(context, ID_TODAYS_DROPS)
                .setShortLabel("Today's Drops")
                .setLongLabel(dropsLongLabel)
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_drops))
                .setIntent(dropsIntent)
                .setRank(2)
                .build()

            val vaultIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_MY_VAULT
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_TARGET_TAB, "CLAIMED")
            }

            val vaultLongLabel = if (vaultCount != null && vaultCount > 0) {
                "My Vault ($vaultCount Claimed)"
            } else {
                "My Vault (Claimed Library)"
            }

            val vaultShortcut = ShortcutInfoCompat.Builder(context, ID_MY_VAULT)
                .setShortLabel("My Vault")
                .setLongLabel(vaultLongLabel)
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_vault))
                .setIntent(vaultIntent)
                .setRank(3)
                .build()

            ShortcutManagerCompat.setDynamicShortcuts(
                context,
                listOf(expiringShortcut, dropsShortcut, vaultShortcut)
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to publish dynamic shortcuts: ${e.message}")
        }
    }

    /**
     * Reports shortcut usage back to Android OS launcher for adaptive ranking.
     */
    fun reportShortcutUsed(context: Context, shortcutId: String) {
        try {
            ShortcutManagerCompat.reportShortcutUsed(context, shortcutId)
        } catch (_: Exception) {
        }
    }
}
