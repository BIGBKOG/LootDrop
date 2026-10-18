package com.example.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Categorization tabs for the Settings screen: Appearance, Alerts, and General.
 * Data management and feature tours are integrated directly within General.
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
        description = "Push notifications, store platforms & games-only alerts"
    ),
    GENERAL(
        label = "General",
        icon = Icons.Default.Tune,
        description = "Startup store, screen DPI detection & adaptive display, FPS performance & guide"
    )
}
