package com.example.data.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import java.util.Locale

/**
 * Steam Deck verification status tiers as defined by Valve.
 */
enum class SteamDeckStatus(
    val id: String,
    val label: String,
    val badgeColorHex: Long,
    val iconEmoji: String,
    val description: String
) {
    VERIFIED(
        id = "verified",
        label = "Deck Verified",
        badgeColorHex = 0xFF10B981, // Vibrant Emerald Green
        iconEmoji = "🟢",
        description = "Valve Verified: Default controller config, readable text, native 1280x800 resolution and smooth 60+ FPS on Steam Deck."
    ),
    PLAYABLE(
        id = "playable",
        label = "Deck Playable",
        badgeColorHex = 0xFFF59E0B, // Amber
        iconEmoji = "🟡",
        description = "Playable on Steam Deck: May require touchscreen keyboard, active internet for third-party launchers, or community controller mapping."
    ),
    UNSUPPORTED(
        id = "unsupported",
        label = "Unsupported",
        badgeColorHex = 0xFFEF4444, // Red
        iconEmoji = "🔴",
        description = "Unsupported on Steam Deck: Kernel-level anti-cheat or Windows OS-specific runtime dependencies prevent execution on SteamOS."
    ),
    UNKNOWN(
        id = "unknown",
        label = "Untested",
        badgeColorHex = 0xFF64748B, // Slate Grey
        iconEmoji = "⚪",
        description = "Testing in progress: Not yet officially tested or rated by Valve's Steam Deck compatibility review team."
    );

    val badgeColor: Color get() = Color(badgeColorHex)
    val pillLabel: String get() = "$iconEmoji $label"
}

/**
 * Gamepad / Controller compatibility rating for handhelds and couch gaming.
 */
enum class ControllerSupport(
    val id: String,
    val label: String,
    val iconEmoji: String,
    val description: String
) {
    FULL(
        id = "full",
        label = "Full Controller",
        iconEmoji = "🎮",
        description = "Native plug-and-play Xbox, PlayStation, and Steam Deck gamepad support throughout menus and gameplay."
    ),
    PARTIAL(
        id = "partial",
        label = "Partial Controller",
        iconEmoji = "🕹️",
        description = "Gamepad supported during gameplay, but may require touch or mouse input for initial setup, launcher, or naming characters."
    ),
    KEYBOARD_MOUSE(
        id = "kb_mouse",
        label = "Keyboard & Mouse",
        iconEmoji = "⌨️",
        description = "Designed primarily for keyboard and mouse control. Handhelds may require trackpads and custom community key bindings."
    )
}

/**
 * Session length style for planning quick handheld commutes vs extended couch sessions.
 */
enum class SessionStyle(
    val id: String,
    val label: String,
    val iconEmoji: String,
    val description: String
) {
    QUICK_SESSION(
        id = "quick_session",
        label = "Quick Session",
        iconEmoji = "⚡",
        description = "Bite-sized pick-up-and-play runs (< 30 min). Perfect for handheld gaming commutes, roguelikes, arcade, racing, and casual breaks."
    ),
    DEEP_DIVE(
        id = "deep_dive",
        label = "Deep Dive",
        iconEmoji = "⏳",
        description = "Extended immersive experience (45+ min). Narrative RPGs, open-world adventures, complex simulation, and grand strategy."
    )
}

/**
 * Storage footprint tiers for planning internal NVMe and microSD storage space.
 */
enum class StorageTier(
    val id: String,
    val label: String,
    val minGb: Float,
    val maxGb: Float,
    val iconEmoji: String,
    val description: String
) {
    FEATHERWEIGHT(
        id = "featherweight",
        label = "< 2 GB",
        minGb = 0f,
        maxGb = 2f,
        iconEmoji = "💾",
        description = "Featherweight (< 2 GB): MicroSD friendly, takes seconds to download and install on any handheld."
    ),
    COMPACT(
        id = "compact",
        label = "2 - 10 GB",
        minGb = 2f,
        maxGb = 10f,
        iconEmoji = "💾",
        description = "Compact (2 - 10 GB): Modest drive space requirement, fast downloads."
    ),
    MEDIUM(
        id = "medium",
        label = "10 - 40 GB",
        minGb = 10f,
        maxGb = 40f,
        iconEmoji = "💾",
        description = "Medium (10 - 40 GB): Standard AA/AAA PC game footprint."
    ),
    HEAVYWEIGHT(
        id = "heavyweight",
        label = "40+ GB",
        minGb = 40f,
        maxGb = Float.MAX_VALUE,
        iconEmoji = "💾",
        description = "Heavyweight (40+ GB): High-fidelity AAA blockbuster, plans ahead for drive space."
    );

    fun matches(sizeGb: Float): Boolean {
        return sizeGb >= minGb && sizeGb < maxGb
    }
}

/**
 * Filter tags for organizing the library and discovering hardware-ready freebies.
 */
enum class PlayabilityFilter(
    val id: String,
    val label: String,
    val iconEmoji: String
) {
    ALL("all", "All Playability", "✨"),
    DECK_VERIFIED("deck_verified", "Deck Verified", "🟢"),
    CONTROLLER_FRIENDLY("controller_friendly", "Controller Friendly", "🎮"),
    QUICK_SESSION("quick_session", "Quick Session", "⚡");
}

/**
 * Storage size filter categories for filtering games by footprint.
 */
enum class StorageFilter(
    val id: String,
    val label: String,
    val tier: StorageTier?
) {
    ALL("all", "All Sizes", null),
    UNDER_2GB("under_2gb", "< 2 GB", StorageTier.FEATHERWEIGHT),
    FROM_2_TO_10GB("2_to_10gb", "2 - 10 GB", StorageTier.COMPACT),
    FROM_10_TO_40GB("10_to_40gb", "10 - 40 GB", StorageTier.MEDIUM),
    OVER_40GB("over_40gb", "40+ GB", StorageTier.HEAVYWEIGHT);
}

/**
 * Resolved hardware readiness profile for a game.
 */
@Immutable
data class GameHardwareProfile(
    val deckStatus: SteamDeckStatus,
    val controllerSupport: ControllerSupport,
    val sessionStyle: SessionStyle,
    val estimatedStorageGb: Float,
    val manualStorageGb: Float? = null
) {
    val effectiveStorageGb: Float
        get() = manualStorageGb ?: estimatedStorageGb

    val isManualStorage: Boolean
        get() = manualStorageGb != null

    val formattedStorage: String
        get() {
            val gb = effectiveStorageGb
            return if (gb < 1.0f) {
                val mb = (gb * 1024f).toInt().coerceAtLeast(50)
                "${mb} MB"
            } else if (gb % 1f == 0f) {
                String.format(Locale.US, "%.0f GB", gb)
            } else {
                String.format(Locale.US, "%.1f GB", gb)
            }
        }

    val isDeckVerified: Boolean get() = deckStatus == SteamDeckStatus.VERIFIED
    val isDeckPlayableOrVerified: Boolean get() = deckStatus == SteamDeckStatus.VERIFIED || deckStatus == SteamDeckStatus.PLAYABLE
    val isControllerFriendly: Boolean get() = controllerSupport == ControllerSupport.FULL || controllerSupport == ControllerSupport.PARTIAL
    val isQuickSession: Boolean get() = sessionStyle == SessionStyle.QUICK_SESSION

    val storageTier: StorageTier
        get() = when {
            effectiveStorageGb < 2f -> StorageTier.FEATHERWEIGHT
            effectiveStorageGb <= 10f -> StorageTier.COMPACT
            effectiveStorageGb <= 40f -> StorageTier.MEDIUM
            else -> StorageTier.HEAVYWEIGHT
        }

    val formattedSize: String get() = formattedStorage
    val isManualOverride: Boolean get() = isManualStorage
    val estimatedSizeGb: Float get() = estimatedStorageGb
    val effectiveSizeGb: Float get() = effectiveStorageGb
}

/**
 * Formats a storage size in gigabytes into a human-readable string (MB, GB, TB).
 */
fun formatStorageGb(sizeGb: Float): String {
    return when {
        sizeGb <= 0f -> "0 GB"
        sizeGb < 1.0f -> {
            val mb = (sizeGb * 1024f).toInt().coerceAtLeast(1)
            "${mb} MB"
        }
        sizeGb >= 1000f -> String.format(Locale.US, "%.1f TB", sizeGb / 1024f)
        sizeGb % 1f == 0f -> String.format(Locale.US, "%.0f GB", sizeGb)
        else -> String.format(Locale.US, "%.1f GB", sizeGb)
    }
}
