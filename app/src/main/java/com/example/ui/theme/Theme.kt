package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.preferences.AccentTheme
import com.example.preferences.BackgroundTheme
import com.example.preferences.ThemeMode

@Composable
fun LootDropTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    accentTheme: AccentTheme = AccentTheme.CYBER_CYAN,
    darkBackgroundTheme: BackgroundTheme = BackgroundTheme.CYBER_SLATE,
    lightBackgroundTheme: BackgroundTheme = BackgroundTheme.CLEAN_SNOW,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemDark
    }

    val activeBackground = if (isDark) darkBackgroundTheme else lightBackgroundTheme
    val primaryColor = accentTheme.primary(isDark)
    val secondaryColor = accentTheme.secondary(isDark)

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = activeBackground.backgroundColor,
            primaryContainer = activeBackground.surfaceVariantColor,
            onPrimaryContainer = primaryColor,
            secondary = secondaryColor,
            onSecondary = activeBackground.backgroundColor,
            secondaryContainer = activeBackground.surfaceVariantColor,
            onSecondaryContainer = secondaryColor,
            tertiary = NeonPurple,
            onTertiary = activeBackground.backgroundColor,
            background = activeBackground.backgroundColor,
            onBackground = activeBackground.textPrimaryColor,
            surface = activeBackground.surfaceColor,
            onSurface = activeBackground.textPrimaryColor,
            surfaceVariant = activeBackground.surfaceVariantColor,
            onSurfaceVariant = activeBackground.textSecondaryColor,
            outline = activeBackground.borderColor,
            outlineVariant = activeBackground.borderColor
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = activeBackground.surfaceColor,
            primaryContainer = activeBackground.surfaceVariantColor,
            onPrimaryContainer = primaryColor,
            secondary = secondaryColor,
            onSecondary = activeBackground.surfaceColor,
            secondaryContainer = activeBackground.surfaceVariantColor,
            onSecondaryContainer = secondaryColor,
            tertiary = NeonPurple,
            onTertiary = activeBackground.surfaceColor,
            background = activeBackground.backgroundColor,
            onBackground = activeBackground.textPrimaryColor,
            surface = activeBackground.surfaceColor,
            onSurface = activeBackground.textPrimaryColor,
            surfaceVariant = activeBackground.surfaceVariantColor,
            onSurfaceVariant = activeBackground.textSecondaryColor,
            outline = activeBackground.borderColor,
            outlineVariant = activeBackground.borderColor
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = activeBackground.backgroundColor.toArgb()
                window.navigationBarColor = activeBackground.surfaceColor.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
