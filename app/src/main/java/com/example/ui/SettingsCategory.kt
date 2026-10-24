package com.example.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Categorization tabs for the Settings screen:
 * - APPEARANCE: Theme mode, 16 accent colors, background style & card density
 * - ALERTS: Smart notifications hub, sound & haptics, store platforms & rules
 * - GENERAL: Startup store, screen DPI detection, adaptive display, FPS & app guide
 * - STORAGE: Storage metrics, 1-tap deep clean, database vacuum & data saver
 */
enum class SettingsCategory(
    val label: String,
    val icon: ImageVector,
    val description: String
) {
    APPEARANCE(
        label = "Appearance",
        icon = Icons.Default.Palette,
        description = "Theme mode, 16 accent colors, background style & card density"
    ),
    ALERTS(
        label = "Alerts",
        icon = Icons.Default.NotificationsActive,
        description = "Smart notifications hub, sound & haptics, store platforms & rules"
    ),
    GENERAL(
        label = "General",
        icon = Icons.Default.Tune,
        description = "Startup store, frame rate & performance, updates, and app guide"
    ),
    STORAGE(
        label = "Storage & Data",
        icon = Icons.Default.Storage,
        description = "Storage metrics, 1-tap deep clean, database vacuum & data saver"
    )
}
