package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.preferences.AccentTheme
import com.example.preferences.BackgroundTheme
import com.example.preferences.ThemeMode
import com.example.util.CustomFontHelper

@Composable
fun LootDropTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    accentTheme: AccentTheme = AccentTheme.CYBER_CYAN,
    darkBackgroundTheme: BackgroundTheme = BackgroundTheme.CYBER_SLATE,
    lightBackgroundTheme: BackgroundTheme = BackgroundTheme.CLEAN_SNOW,
    customFontPath: String = "",
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemDark
    }

    val activeBackground = if (isDark) darkBackgroundTheme else lightBackgroundTheme
    val colorScheme = remember(isDark, accentTheme, activeBackground) {
        val primaryColor = accentTheme.primary(isDark)
        val secondaryColor = accentTheme.secondary(isDark)
        // Smooth tonal surface hierarchy derived from activeBackground so chrome bars, cards, and banners meld cleanly
        val surfaceLow = lerp(activeBackground.backgroundColor, activeBackground.surfaceColor, 0.72f)
        val surfaceContainer = activeBackground.surfaceColor
        val surfaceHigh = lerp(activeBackground.surfaceColor, activeBackground.surfaceVariantColor, 0.55f)
        val surfaceHighest = activeBackground.surfaceVariantColor
        val calmOutlineVariant = lerp(activeBackground.surfaceColor, activeBackground.borderColor, 0.6f)

        if (isDark) {
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
                surfaceContainerLowest = activeBackground.backgroundColor,
                surfaceContainerLow = surfaceLow,
                surfaceContainer = surfaceContainer,
                surfaceContainerHigh = surfaceHigh,
                surfaceContainerHighest = surfaceHighest,
                outline = activeBackground.borderColor,
                outlineVariant = calmOutlineVariant
            )
        } else {
            lightColorScheme(
                primary = primaryColor,
                onPrimary = Color.White,
                primaryContainer = activeBackground.surfaceVariantColor,
                onPrimaryContainer = primaryColor,
                secondary = secondaryColor,
                onSecondary = Color.White,
                secondaryContainer = activeBackground.surfaceVariantColor,
                onSecondaryContainer = secondaryColor,
                tertiary = NeonPurple,
                onTertiary = Color.White,
                background = activeBackground.backgroundColor,
                onBackground = activeBackground.textPrimaryColor,
                surface = activeBackground.surfaceColor,
                onSurface = activeBackground.textPrimaryColor,
                surfaceVariant = activeBackground.surfaceVariantColor,
                onSurfaceVariant = activeBackground.textSecondaryColor,
                surfaceContainerLowest = activeBackground.surfaceColor,
                surfaceContainerLow = surfaceLow,
                surfaceContainer = surfaceContainer,
                surfaceContainerHigh = surfaceHigh,
                surfaceContainerHighest = surfaceHighest,
                outline = activeBackground.borderColor,
                outlineVariant = calmOutlineVariant
            )
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                @Suppress("DEPRECATION")
                window.statusBarColor = Color.Transparent.toArgb()
                @Suppress("DEPRECATION")
                window.navigationBarColor = Color.Transparent.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    val customFontFamily = remember(customFontPath) {
        CustomFontHelper.loadComposeFontFamily(customFontPath)
    }
    val activeTypography = remember(customFontFamily) {
        createLootDropTypography(customFontFamily)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = activeTypography,
        content = content
    )
}

