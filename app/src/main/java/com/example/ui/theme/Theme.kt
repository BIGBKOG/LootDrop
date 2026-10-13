package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.preferences.AccentTheme
import com.example.preferences.BackgroundTheme

@Composable
fun LootDropTheme(
    accentTheme: AccentTheme = AccentTheme.CYBER_CYAN,
    backgroundTheme: BackgroundTheme = BackgroundTheme.CYBER_SLATE,
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = accentTheme.primaryColor,
        onPrimary = backgroundTheme.backgroundColor,
        primaryContainer = backgroundTheme.surfaceVariantColor,
        onPrimaryContainer = accentTheme.primaryColor,
        secondary = accentTheme.secondaryColor,
        onSecondary = backgroundTheme.backgroundColor,
        secondaryContainer = backgroundTheme.surfaceVariantColor,
        onSecondaryContainer = accentTheme.secondaryColor,
        tertiary = NeonPurple,
        onTertiary = backgroundTheme.backgroundColor,
        background = backgroundTheme.backgroundColor,
        onBackground = TextPrimary,
        surface = backgroundTheme.surfaceColor,
        onSurface = TextPrimary,
        surfaceVariant = backgroundTheme.surfaceVariantColor,
        onSurfaceVariant = TextSecondary,
        outline = backgroundTheme.borderColor,
        outlineVariant = backgroundTheme.borderColor
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = backgroundTheme.backgroundColor.toArgb()
                window.navigationBarColor = backgroundTheme.surfaceColor.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
