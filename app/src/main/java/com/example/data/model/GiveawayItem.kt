package com.example.data.model

data class GiveawayItem(
    val id: Int,
    val title: String,
    val worth: String,
    val numericWorth: Double,
    val thumbnail: String,
    val image: String,
    val description: String,
    val instructions: String,
    val openGiveawayUrl: String,
    val publishedDate: String,
    val type: String,
    val platforms: String,
    val endDate: String,
    val users: Int,
    val status: String,
    val isClaimed: Boolean = false,
    val isBookmarked: Boolean = false,
    val isNew: Boolean = false,
    val firstSeenTimestamp: Long = System.currentTimeMillis()
) {
    val launcherCategory: LauncherType
        get() {
            val lower = platforms.lowercase()
            return when {
                lower.contains("steam") -> LauncherType.STEAM
                lower.contains("epic") -> LauncherType.EPIC
                lower.contains("gog") -> LauncherType.GOG
                lower.contains("ubisoft") || lower.contains("uplay") -> LauncherType.UBISOFT
                lower.contains("itch") -> LauncherType.ITCH_IO
                lower.contains("amazon") || lower.contains("prime") -> LauncherType.PRIME
                lower.contains("origin") || lower.contains("ea") -> LauncherType.EA_ORIGIN
                lower.contains("battlenet") || lower.contains("battle.net") -> LauncherType.BATTLENET
                else -> LauncherType.OTHER
            }
        }
}

enum class LauncherType(
    val displayName: String,
    val apiParam: String?,
    val badgeColorHex: Long,
    val iconName: String
) {
    ALL("All Launchers", "pc", 0xFF00E5FF, "all"),
    STEAM("Steam", "steam", 0xFF66C0F4, "steam"),
    EPIC("Epic Games", "epic-games-store", 0xFF0078F2, "epic"),
    GOG("GOG", "gog", 0xFFBA55D3, "gog"),
    UBISOFT("Ubisoft", "ubisoft", 0xFF00A2FF, "ubisoft"),
    ITCH_IO("itch.io", "itchio", 0xFFFA5C5C, "itch"),
    PRIME("Prime", "amazon-prime", 0xFFFF9900, "prime"),
    EA_ORIGIN("EA App", "origin", 0xFFFF4500, "ea"),
    BATTLENET("Battle.net", "battlenet", 0xFF00B4FF, "bnet"),
    OTHER("PC / DRM-Free", "pc", 0xFF4ADE80, "other")
}
