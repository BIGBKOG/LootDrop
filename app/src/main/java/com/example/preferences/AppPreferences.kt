package com.example.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import com.example.data.model.LauncherType
import com.example.ui.SortOption
import com.example.ui.theme.EpicGamesBlue
import com.example.ui.theme.GogPurple
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SteamBlue

enum class ThemeMode(val label: String) {
    DARK("Dark Mode"),
    LIGHT("Light Mode"),
    SYSTEM("System Default")
}

enum class CardDensity(val label: String, val description: String) {
    RICH_CARDS("Rich Cards", "Large cover art with full details"),
    COMPACT_CARDS("Compact Cards", "Streamlined horizontal row cards"),
    MINIMAL_LIST("Minimal List", "Compact text-first list view")
}

enum class TargetFrameRate(val label: String, val fps: Int, val description: String) {
    AUTO("Adaptive (60/90/120)", 0, "Hardware adaptive sync up to 120Hz"),
    FPS_60("60 FPS", 60, "Standard 60Hz display refresh rate"),
    FPS_90("90 FPS", 90, "Smooth 90Hz enhanced motion (Pixel & 90Hz screens)"),
    FPS_120("120 FPS", 120, "Extreme 120Hz maximum fluidity")
}

enum class DpiScaleMode(val label: String, val shortName: String, val description: String) {
    AUTO_ADAPTIVE("Auto (DPI-Aware)", "Auto", "Automatically tunes layout, spacing & targets to screen DPI"),
    COMPACT("Compact (High-Density)", "Compact", "Condensed spacing to display maximum deals on screen"),
    BALANCED("Balanced Standard", "Balanced", "Standard Material 3 spacing and dimensions"),
    SPACIOUS("Spacious Comfort", "Spacious", "Larger touch targets and generous padding")
}

enum class AccentCategory(val label: String) {
    ALL("All (16)"),
    GAMING("Gaming (6)"),
    CYBER("Cyber (5)"),
    VAULT("Vault (5)")
}

enum class AccentTheme(
    val title: String,
    val darkPrimary: Color,
    val darkSecondary: Color,
    val lightPrimary: Color,
    val lightSecondary: Color,
    val description: String,
    val shortName: String,
    val category: AccentCategory
) {
    CYBER_CYAN(
        "Cyber Cyan",
        NeonCyan,
        NeonGreen,
        Color(0xFF0284C7),
        Color(0xFF0D9488),
        "Vibrant high-contrast cyber blue",
        "Cyan",
        AccentCategory.CYBER
    ),
    STEAM_BLUE(
        "Steam Sky",
        SteamBlue,
        Color(0xFF38BDF8),
        Color(0xFF0369A1),
        Color(0xFF0284C7),
        "Classic PC gaming blue",
        "Steam",
        AccentCategory.GAMING
    ),
    GOG_PURPLE(
        "Ultraviolet",
        GogPurple,
        Color(0xFFD946EF),
        Color(0xFF7E22CE),
        Color(0xFFA855F7),
        "Deep electric violet & magenta",
        "Ultra Violet",
        AccentCategory.CYBER
    ),
    RAZER_LIME(
        "Toxic Lime",
        Color(0xFF22C55E),
        Color(0xFF10B981),
        Color(0xFF15803D),
        Color(0xFF16A34A),
        "High-energy matrix green",
        "Toxic Lime",
        AccentCategory.CYBER
    ),
    INFERNO_RED(
        "Inferno Red",
        Color(0xFFEF4444),
        Color(0xFFF97316),
        Color(0xFFB91C1C),
        Color(0xFFDC2626),
        "Warm battle red & ember",
        "Inferno",
        AccentCategory.CYBER
    ),
    GOLDEN_LOOT(
        "Loot Gold",
        Color(0xFFFBBF24),
        Color(0xFFF59E0B),
        Color(0xFFB45309),
        Color(0xFFD97706),
        "Legendary gold drops",
        "Loot Gold",
        AccentCategory.VAULT
    ),
    EPIC_INDIGO(
        "Epic Indigo",
        Color(0xFF6366F1),
        Color(0xFF8B5CF6),
        Color(0xFF4338CA),
        Color(0xFF6366F1),
        "Deep launcher indigo",
        "Epic",
        AccentCategory.GAMING
    ),
    CHERRY_ROSE(
        "Cherry Blossom",
        Color(0xFFEC4899),
        Color(0xFFF43F5E),
        Color(0xFFBE185D),
        Color(0xFFE11D48),
        "Vivid neon pink & sakura",
        "Sakura",
        AccentCategory.CYBER
    ),
    OCEAN_TEAL(
        "Ocean Teal",
        Color(0xFF14B8A6),
        Color(0xFF06B6D4),
        Color(0xFF0F766E),
        Color(0xFF0D9488),
        "Deep oceanic crystal teal",
        "Teal",
        AccentCategory.VAULT
    ),
    SUNSET_CORAL(
        "Sunset Coral",
        Color(0xFFFB923C),
        Color(0xFFF43F5E),
        Color(0xFFC2410C),
        Color(0xFFEA580C),
        "Warm twilight sunset blaze",
        "Sunset",
        AccentCategory.VAULT
    ),
    DEEP_ROYALE(
        "Royal Violet",
        Color(0xFF8B5CF6),
        Color(0xFFC084FC),
        Color(0xFF6D28D9),
        Color(0xFF7C3AED),
        "Majestic royal imperial purple",
        "Royale",
        AccentCategory.VAULT
    ),
    EMERALD_MINT(
        "Emerald Vault",
        Color(0xFF10B981),
        Color(0xFF34D399),
        Color(0xFF047857),
        Color(0xFF059669),
        "Lush emerald gemstone green",
        "Emerald",
        AccentCategory.VAULT
    ),
    PLAYSTATION_BLUE(
        "PlayStation Blue",
        Color(0xFF0070D1),
        Color(0xFF38BDF8),
        Color(0xFF003791),
        Color(0xFF0070D1),
        "Official PlayStation electric cobalt",
        "PlayStation",
        AccentCategory.GAMING
    ),
    XBOX_GREEN(
        "Xbox Green",
        Color(0xFF107C10),
        Color(0xFF22C55E),
        Color(0xFF0E5A0E),
        Color(0xFF107C10),
        "Iconic Xbox emerald neon green",
        "Xbox",
        AccentCategory.GAMING
    ),
    NINTENDO_RED(
        "Nintendo Red",
        Color(0xFFE60012),
        Color(0xFFFF4D5A),
        Color(0xFF99000C),
        Color(0xFFE60012),
        "Iconic Nintendo Switch neon crimson",
        "Nintendo",
        AccentCategory.GAMING
    ),
    DISCORD_BLURPLE(
        "Discord Blurple",
        Color(0xFF5865F2),
        Color(0xFF818CF8),
        Color(0xFF4752C4),
        Color(0xFF5865F2),
        "Iconic gamer community blurple",
        "Discord",
        AccentCategory.GAMING
    );

    fun primary(isDark: Boolean): Color = if (isDark) darkPrimary else lightPrimary
    fun secondary(isDark: Boolean): Color = if (isDark) darkSecondary else lightSecondary
}

enum class BackgroundTheme(
    val title: String,
    val isDark: Boolean,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val surfaceVariantColor: Color,
    val borderColor: Color,
    val textPrimaryColor: Color,
    val textSecondaryColor: Color
) {
    // Dark Themes
    CYBER_SLATE(
        "Cyber Slate",
        true,
        Color(0xFF0B0F19),
        Color(0xFF131926),
        Color(0xFF1C2538),
        Color(0xFF26334D),
        Color(0xFFF1F5F9),
        Color(0xFF94A3B8)
    ),
    OLED_BLACK(
        "OLED Midnight",
        true,
        Color(0xFF000000),
        Color(0xFF0D0D11),
        Color(0xFF16161D),
        Color(0xFF24242F),
        Color(0xFFF8FAFC),
        Color(0xFF94A3B8)
    ),
    MIDNIGHT_NAVY(
        "Midnight Navy",
        true,
        Color(0xFF070D1E),
        Color(0xFF0F1A38),
        Color(0xFF17264F),
        Color(0xFF233668),
        Color(0xFFF1F5F9),
        Color(0xFF94A3B8)
    ),

    // Light Themes
    CLEAN_SNOW(
        "Clean Snow",
        false,
        Color(0xFFF8FAFC),
        Color(0xFFFFFFFF),
        Color(0xFFF1F5F9),
        Color(0xFFE2E8F0),
        Color(0xFF0F172A),
        Color(0xFF475569)
    ),
    COOL_FROST(
        "Cool Frost",
        false,
        Color(0xFFF0F4F8),
        Color(0xFFFFFFFF),
        Color(0xFFE2E8F0),
        Color(0xFFCBD5E1),
        Color(0xFF0F172A),
        Color(0xFF475569)
    ),
    WARM_SAND(
        "Warm Sand",
        false,
        Color(0xFFFAF7F2),
        Color(0xFFFFFFFF),
        Color(0xFFF3EDE2),
        Color(0xFFE5DCCF),
        Color(0xFF1C1917),
        Color(0xFF57534E)
    )
}

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("lootdrop_app_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_ACCENT_THEME = "key_accent_theme"
        private const val KEY_BG_THEME_DARK = "key_bg_theme_dark"
        private const val KEY_BG_THEME_LIGHT = "key_bg_theme_light"
        private const val KEY_CARD_DENSITY = "key_card_density"
        private const val KEY_DEFAULT_LAUNCHER = "key_default_launcher"
        private const val KEY_SHOW_ORIGINAL_PRICE = "key_show_original_price"
        private const val KEY_SHOW_EXPIRATION = "key_show_expiration"
        private const val KEY_CONFIRM_ON_CLAIM = "key_confirm_on_claim"
        private const val KEY_AUTO_CHECK_ON_REOPEN = "key_auto_check_on_reopen"
        private const val KEY_HAS_SEEN_ONBOARDING = "key_has_seen_onboarding"
        private const val KEY_UI_ANIMATIONS_ENABLED = "key_ui_animations_enabled"
        private const val KEY_NEW_TODAY_DISMISSED = "key_new_today_dismissed"
        private const val KEY_TARGET_FRAME_RATE = "key_target_frame_rate"
        private const val KEY_DPI_OPTIMIZATION_ENABLED = "key_dpi_optimization_enabled"
        private const val KEY_DPI_SCALE_MODE = "key_dpi_scale_mode"
        private const val KEY_DETECTED_SCREEN_DPI = "key_detected_screen_dpi"
    }

    var themeMode: ThemeMode
        get() {
            val saved = prefs.getString(KEY_THEME_MODE, ThemeMode.DARK.name)
            return try {
                ThemeMode.valueOf(saved ?: ThemeMode.DARK.name)
            } catch (_: Exception) {
                ThemeMode.DARK
            }
        }
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value.name).apply()

    var accentTheme: AccentTheme
        get() {
            val saved = prefs.getString(KEY_ACCENT_THEME, AccentTheme.CYBER_CYAN.name)
            return try {
                AccentTheme.valueOf(saved ?: AccentTheme.CYBER_CYAN.name)
            } catch (_: Exception) {
                AccentTheme.CYBER_CYAN
            }
        }
        set(value) = prefs.edit().putString(KEY_ACCENT_THEME, value.name).apply()

    var darkBackgroundTheme: BackgroundTheme
        get() {
            val saved = prefs.getString(KEY_BG_THEME_DARK, BackgroundTheme.CYBER_SLATE.name)
            return try {
                BackgroundTheme.valueOf(saved ?: BackgroundTheme.CYBER_SLATE.name)
            } catch (_: Exception) {
                BackgroundTheme.CYBER_SLATE
            }
        }
        set(value) = prefs.edit().putString(KEY_BG_THEME_DARK, value.name).apply()

    var lightBackgroundTheme: BackgroundTheme
        get() {
            val saved = prefs.getString(KEY_BG_THEME_LIGHT, BackgroundTheme.CLEAN_SNOW.name)
            return try {
                BackgroundTheme.valueOf(saved ?: BackgroundTheme.CLEAN_SNOW.name)
            } catch (_: Exception) {
                BackgroundTheme.CLEAN_SNOW
            }
        }
        set(value) = prefs.edit().putString(KEY_BG_THEME_LIGHT, value.name).apply()

    var cardDensity: CardDensity
        get() {
            val saved = prefs.getString(KEY_CARD_DENSITY, CardDensity.RICH_CARDS.name)
            return try {
                CardDensity.valueOf(saved ?: CardDensity.RICH_CARDS.name)
            } catch (_: Exception) {
                CardDensity.RICH_CARDS
            }
        }
        set(value) = prefs.edit().putString(KEY_CARD_DENSITY, value.name).apply()

    var defaultLauncher: LauncherType
        get() {
            val saved = prefs.getString(KEY_DEFAULT_LAUNCHER, LauncherType.ALL.name)
            return try {
                LauncherType.valueOf(saved ?: LauncherType.ALL.name)
            } catch (_: Exception) {
                LauncherType.ALL
            }
        }
        set(value) = prefs.edit().putString(KEY_DEFAULT_LAUNCHER, value.name).apply()

    var isShowOriginalPrice: Boolean
        get() = prefs.getBoolean(KEY_SHOW_ORIGINAL_PRICE, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_ORIGINAL_PRICE, value).apply()

    var isShowExpiration: Boolean
        get() = prefs.getBoolean(KEY_SHOW_EXPIRATION, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_EXPIRATION, value).apply()

    var isConfirmToastOnClaim: Boolean
        get() = prefs.getBoolean(KEY_CONFIRM_ON_CLAIM, true)
        set(value) = prefs.edit().putBoolean(KEY_CONFIRM_ON_CLAIM, value).apply()

    var isAutoCheckOnReopen: Boolean
        get() = prefs.getBoolean(KEY_AUTO_CHECK_ON_REOPEN, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_CHECK_ON_REOPEN, value).apply()

    var hasSeenOnboarding: Boolean
        get() = prefs.getBoolean(KEY_HAS_SEEN_ONBOARDING, false)
        set(value) = prefs.edit().putBoolean(KEY_HAS_SEEN_ONBOARDING, value).apply()

    var isAnimationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_UI_ANIMATIONS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_UI_ANIMATIONS_ENABLED, value).apply()

    var isNewTodayDismissed: Boolean
        get() = prefs.getBoolean(KEY_NEW_TODAY_DISMISSED, false)
        set(value) = prefs.edit().putBoolean(KEY_NEW_TODAY_DISMISSED, value).apply()

    var targetFrameRate: TargetFrameRate
        get() {
            val saved = prefs.getString(KEY_TARGET_FRAME_RATE, TargetFrameRate.AUTO.name)
            return try {
                TargetFrameRate.valueOf(saved ?: TargetFrameRate.AUTO.name)
            } catch (_: Exception) {
                TargetFrameRate.AUTO
            }
        }
        set(value) = prefs.edit().putString(KEY_TARGET_FRAME_RATE, value.name).apply()

    var isDpiOptimizationEnabled: Boolean
        get() = prefs.getBoolean(KEY_DPI_OPTIMIZATION_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_DPI_OPTIMIZATION_ENABLED, value).apply()

    var dpiScaleMode: DpiScaleMode
        get() {
            val saved = prefs.getString(KEY_DPI_SCALE_MODE, DpiScaleMode.AUTO_ADAPTIVE.name)
            return try {
                DpiScaleMode.valueOf(saved ?: DpiScaleMode.AUTO_ADAPTIVE.name)
            } catch (_: Exception) {
                DpiScaleMode.AUTO_ADAPTIVE
            }
        }
        set(value) = prefs.edit().putString(KEY_DPI_SCALE_MODE, value.name).apply()

    var detectedScreenDpi: Int
        get() = prefs.getInt(KEY_DETECTED_SCREEN_DPI, 420)
        set(value) = prefs.edit().putInt(KEY_DETECTED_SCREEN_DPI, value).apply()
}
