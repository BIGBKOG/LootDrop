package com.example.ui.density

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.preferences.DpiScaleMode

enum class ScreenCategory(val label: String, val description: String) {
    COMPACT("Compact Phone", "Narrow display under 360dp width"),
    STANDARD("Standard Phone", "Standard handheld display (360-599dp)"),
    FOLDABLE("Foldable / Large", "Unfolded foldable display (600-839dp)"),
    TABLET("Tablet / Desktop", "Spacious large tablet display (840dp+)")
}

data class ScreenDensityInfo(
    val densityDpi: Int,
    val densityScale: Float,
    val fontScale: Float,
    val widthPx: Int,
    val heightPx: Int,
    val widthDp: Int,
    val heightDp: Int,
    val densityBucket: String,
    val screenCategory: ScreenCategory,
    val gridColumns: Int,
    val adaptiveHorizontalPadding: Dp,
    val adaptiveCardSpacing: Dp,
    val cardCoverHeight: Dp,
    val maxContentWidth: Dp,
    val maxDialogWidth: Dp,
    val isTabletOrFoldable: Boolean,
    val isNarrowScreen: Boolean,
    val isHighDensity: Boolean
) {
    val telemetrySummary: String
        get() = "$densityDpi DPI ($densityBucket) • ${widthDp}×${heightDp} dp • ${"%.1f".format(densityScale)}x"

    val isCompactHeight: Boolean
        get() = heightDp < 700

    val isUltraCompactHeight: Boolean
        get() = heightDp < 600

    val walkthroughAnimBoxSize: Dp
        get() = when {
            heightDp < 580 -> 72.dp
            heightDp < 660 -> 90.dp
            heightDp < 760 -> 112.dp
            else -> 135.dp
        }

    val walkthroughButtonHeight: Dp
        get() = when {
            heightDp < 580 -> 40.dp
            heightDp < 680 -> 44.dp
            else -> 48.dp
        }
}

val LocalScreenDensityInfo = compositionLocalOf {
    ScreenDensityInfo(
        densityDpi = 420,
        densityScale = 2.625f,
        fontScale = 1.0f,
        widthPx = 1080,
        heightPx = 2400,
        widthDp = 411,
        heightDp = 914,
        densityBucket = "XXHDPI",
        screenCategory = ScreenCategory.STANDARD,
        gridColumns = 1,
        adaptiveHorizontalPadding = 16.dp,
        adaptiveCardSpacing = 14.dp,
        cardCoverHeight = 180.dp,
        maxContentWidth = 600.dp,
        maxDialogWidth = 560.dp,
        isTabletOrFoldable = false,
        isNarrowScreen = false,
        isHighDensity = true
    )
}

fun computeScreenDensityInfo(
    densityDpi: Int,
    densityScale: Float,
    fontScale: Float,
    widthPx: Int,
    heightPx: Int,
    widthDp: Int,
    heightDp: Int,
    dpiScaleMode: DpiScaleMode = DpiScaleMode.AUTO_ADAPTIVE,
    isOptimizationEnabled: Boolean = true
): ScreenDensityInfo {
    val bucket = when {
        densityDpi <= 140 -> "LDPI"
        densityDpi <= 180 -> "MDPI"
        densityDpi <= 220 -> "TVDPI"
        densityDpi <= 280 -> "HDPI"
        densityDpi <= 360 -> "XHDPI"
        densityDpi <= 440 -> "XXHDPI (~400-440)"
        densityDpi <= 520 -> "XXHDPI (480)"
        else -> "XXXHDPI (560-640)"
    }

    val category = when {
        widthDp < 360 -> ScreenCategory.COMPACT
        widthDp < 600 -> ScreenCategory.STANDARD
        widthDp < 840 -> ScreenCategory.FOLDABLE
        else -> ScreenCategory.TABLET
    }

    val isTabletOrFoldable = widthDp >= 600
    val isNarrow = widthDp < 360
    val isHigh = densityDpi >= 400

    val columns = when {
        widthDp >= 900 -> 3
        widthDp >= 600 -> 2
        else -> 1
    }

    val basePadding = when {
        !isOptimizationEnabled -> 16.dp
        dpiScaleMode == DpiScaleMode.COMPACT -> 12.dp
        dpiScaleMode == DpiScaleMode.SPACIOUS -> 20.dp
        isTabletOrFoldable -> 24.dp
        isNarrow -> 12.dp
        else -> 16.dp
    }

    val baseSpacing = when {
        !isOptimizationEnabled -> 14.dp
        dpiScaleMode == DpiScaleMode.COMPACT -> 8.dp
        dpiScaleMode == DpiScaleMode.SPACIOUS -> 18.dp
        isTabletOrFoldable -> 16.dp
        isNarrow -> 10.dp
        else -> 14.dp
    }

    val coverHeight = when {
        !isOptimizationEnabled -> 180.dp
        dpiScaleMode == DpiScaleMode.COMPACT -> 150.dp
        dpiScaleMode == DpiScaleMode.SPACIOUS -> 200.dp
        isTabletOrFoldable -> 190.dp
        isNarrow -> 155.dp
        else -> 180.dp
    }

    val maxContentWidth = when {
        widthDp >= 900 -> 1200.dp
        widthDp >= 600 -> 900.dp
        else -> 600.dp
    }

    val maxDialogWidth = when {
        widthDp >= 840 -> 680.dp
        widthDp >= 600 -> 580.dp
        else -> 520.dp
    }

    return ScreenDensityInfo(
        densityDpi = densityDpi,
        densityScale = densityScale,
        fontScale = fontScale,
        widthPx = widthPx,
        heightPx = heightPx,
        widthDp = widthDp,
        heightDp = heightDp,
        densityBucket = bucket,
        screenCategory = category,
        gridColumns = columns,
        adaptiveHorizontalPadding = basePadding,
        adaptiveCardSpacing = baseSpacing,
        cardCoverHeight = coverHeight,
        maxContentWidth = maxContentWidth,
        maxDialogWidth = maxDialogWidth,
        isTabletOrFoldable = isTabletOrFoldable,
        isNarrowScreen = isNarrow,
        isHighDensity = isHigh
    )
}

@Composable
fun rememberScreenDensityInfo(
    dpiScaleMode: DpiScaleMode = DpiScaleMode.AUTO_ADAPTIVE,
    isOptimizationEnabled: Boolean = true
): ScreenDensityInfo {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    return remember(
        configuration.screenWidthDp,
        configuration.screenHeightDp,
        configuration.densityDpi,
        density.density,
        density.fontScale,
        dpiScaleMode,
        isOptimizationEnabled
    ) {
        val dm = context.resources.displayMetrics
        val densityDpi = if (configuration.densityDpi != 0) configuration.densityDpi else dm.densityDpi
        val densityScale = density.density
        val fontScale = density.fontScale
        val widthDp = configuration.screenWidthDp
        val heightDp = configuration.screenHeightDp
        val widthPx = (widthDp * densityScale).toInt()
        val heightPx = (heightDp * densityScale).toInt()

        computeScreenDensityInfo(
            densityDpi = densityDpi,
            densityScale = densityScale,
            fontScale = fontScale,
            widthPx = widthPx,
            heightPx = heightPx,
            widthDp = widthDp,
            heightDp = heightDp,
            dpiScaleMode = dpiScaleMode,
            isOptimizationEnabled = isOptimizationEnabled
        )
    }
}
