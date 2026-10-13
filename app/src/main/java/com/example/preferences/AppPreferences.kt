package com.example.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import com.example.data.model.LauncherType
import com.example.ui.theme.EpicGamesBlue
import com.example.ui.theme.GogPurple
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SteamBlue

enum class AccentTheme(
    val title: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val description: String
) {
    CYBER_CYAN("Cyber Cyan", NeonCyan, NeonGreen, "Default neon cyber style"),
    STEAM_BLUE("Steam Sky", SteamBlue, Color(0xFF38BDF8), "Classic PC gamer blue"),
    GOG_PURPLE("Ultraviolet", GogPurple, Color(0xFFD946EF), "Vibrant GOG purple glow"),
    RAZER_LIME("Toxic Lime", Color(0xFF22C55E), Color(0xFF10B981), "High-energy matrix green"),
    INFERNO_RED("Inferno Red", Color(0xFFEF4444), Color(0xFFF97316), "Warm aggressive battle red"),
    GOLDEN_LOOT("Loot Gold", Color(0xFFFBBF24), Color(0xFFF59E0B), "Legendary golden drops"),
    EPIC_INDIGO("Epic Indigo", Color(0xFF6366F1), Color(0xFF8B5CF6), "Deep launcher indigo")
}

enum class BackgroundTheme(
    val title: String,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val surfaceVariantColor: Color,
    val borderColor: Color
) {
    CYBER_SLATE(
        "Cyber Slate",
        Color(0xFF0B0F19),
        Color(0xFF131926),
        Color(0xFF1C2538),
        Color(0xFF26334D)
    ),
    OLED_BLACK(
        "OLED Midnight",
        Color(0xFF000000),
        Color(0xFF0D0D11),
        Color(0xFF16161D),
        Color(0xFF24242F)
    ),
    MIDNIGHT_NAVY(
        "Midnight Navy",
        Color(0xFF070D1E),
        Color(0xFF0F1A38),
        Color(0xFF17264F),
        Color(0xFF233668)
    )
}

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("lootdrop_app_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACCENT_THEME = "key_accent_theme"
        private const val KEY_BG_THEME = "key_bg_theme"
        private const val KEY_COMPACT_CARDS = "key_compact_cards"
        private const val KEY_DEFAULT_LAUNCHER = "key_default_launcher"
    }

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

    var backgroundTheme: BackgroundTheme
        get() {
            val saved = prefs.getString(KEY_BG_THEME, BackgroundTheme.CYBER_SLATE.name)
            return try {
                BackgroundTheme.valueOf(saved ?: BackgroundTheme.CYBER_SLATE.name)
            } catch (_: Exception) {
                BackgroundTheme.CYBER_SLATE
            }
        }
        set(value) = prefs.edit().putString(KEY_BG_THEME, value.name).apply()

    var isCompactCards: Boolean
        get() = prefs.getBoolean(KEY_COMPACT_CARDS, false)
        set(value) = prefs.edit().putBoolean(KEY_COMPACT_CARDS, value).apply()

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
}
