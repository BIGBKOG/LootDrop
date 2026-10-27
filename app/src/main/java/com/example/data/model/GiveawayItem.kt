package com.example.data.model

import androidx.compose.runtime.Immutable
import com.example.util.MetacriticCatalog

@Immutable
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
    val firstSeenTimestamp: Long = System.currentTimeMillis(),
    val metacriticScore: Int = MetacriticCatalog.resolveScore(title, type, description)
) {
    val metacriticInfo: MetacriticInfo
        get() = MetacriticCatalog.getMetacriticInfo(title, type, description, metacriticScore)
    val categories: List<GameCategory>
        get() = GameCategory.inferCategories(type, title, description)

    val isPlayStation: Boolean
        get() = platforms.lowercase().let {
            it.contains("playstation") || it.contains("ps4") || it.contains("ps5") || it.contains("ps3") || it.contains("psvr")
        }

    val isXbox: Boolean
        get() = platforms.lowercase().contains("xbox")

    val isNintendo: Boolean
        get() = platforms.lowercase().let {
            it.contains("nintendo") || it.contains("switch")
        }

    val isSteam: Boolean
        get() = platforms.lowercase().contains("steam")

    val isEpic: Boolean
        get() = platforms.lowercase().contains("epic")

    val isGog: Boolean
        get() = platforms.lowercase().contains("gog")

    val isPc: Boolean
        get() = platforms.lowercase().let {
            it.contains("pc") || it.contains("steam") || it.contains("epic") || it.contains("gog") ||
            it.contains("itch") || it.contains("origin") || it.contains("ubisoft") || it.contains("uplay") ||
            it.contains("battlenet") || it.contains("prime")
        }

    val isConsole: Boolean
        get() = isPlayStation || isXbox || isNintendo

    val isAndroid: Boolean
        get() = platforms.lowercase().contains("android")

    val isIos: Boolean
        get() = platforms.lowercase().let {
            it.contains("ios") || it.contains("iphone") || it.contains("ipad") || it.contains("apple")
        }

    val isMobile: Boolean
        get() = isAndroid || isIos

    val launcherCategory: LauncherType
        get() {
            val lower = platforms.lowercase()
            return when {
                // Console-specific if not PC
                (lower.contains("playstation") || lower.contains("ps5") || lower.contains("ps4")) &&
                        !lower.contains("steam") && !lower.contains("epic") && !lower.contains("gog") && !lower.contains("pc") -> LauncherType.PLAYSTATION
                lower.contains("xbox") && !lower.contains("steam") && !lower.contains("epic") && !lower.contains("gog") && !lower.contains("pc") -> LauncherType.XBOX
                (lower.contains("nintendo") || lower.contains("switch")) && !lower.contains("steam") && !lower.contains("epic") && !lower.contains("gog") && !lower.contains("pc") -> LauncherType.NINTENDO
                // Mobile-specific if not PC or console
                lower.contains("android") && !lower.contains("steam") && !lower.contains("epic") && !lower.contains("gog") && !lower.contains("pc") && !lower.contains("playstation") && !lower.contains("xbox") -> LauncherType.ANDROID
                (lower.contains("ios") || lower.contains("iphone") || lower.contains("ipad")) && !lower.contains("steam") && !lower.contains("epic") && !lower.contains("gog") && !lower.contains("pc") && !lower.contains("playstation") && !lower.contains("xbox") -> LauncherType.IOS
                lower.contains("steam") -> LauncherType.STEAM
                lower.contains("epic") -> LauncherType.EPIC
                lower.contains("gog") -> LauncherType.GOG
                lower.contains("playstation") || lower.contains("ps5") || lower.contains("ps4") -> LauncherType.PLAYSTATION
                lower.contains("xbox") -> LauncherType.XBOX
                lower.contains("nintendo") || lower.contains("switch") -> LauncherType.NINTENDO
                lower.contains("android") -> LauncherType.ANDROID
                lower.contains("ios") || lower.contains("iphone") || lower.contains("ipad") -> LauncherType.IOS
                lower.contains("ubisoft") || lower.contains("uplay") -> LauncherType.UBISOFT
                lower.contains("itch.io") || lower.contains("itchio") || lower.split(Regex("[,/ ]+")).any { it == "itch" } -> LauncherType.ITCH_IO
                lower.contains("amazon") || lower.contains("prime") -> LauncherType.PRIME
                lower.contains("origin") || lower.contains("ea app") || lower.split(Regex("[,/ ]+")).any { it == "ea" } -> LauncherType.EA_ORIGIN
                lower.contains("battlenet") || lower.contains("battle.net") -> LauncherType.BATTLENET
                else -> LauncherType.OTHER
            }
        }

    val hasMultipleLaunchers: Boolean
        get() = availableLaunchers.filter { it.launcherType != LauncherType.OTHER }.size > 1

    val availableLaunchers: List<LauncherOption>
        get() {
            val lower = platforms.lowercase()
            val tokens = lower.split(Regex("[,/|]+")).map { it.trim() }
        val encodedTitle = try {
            java.net.URLEncoder.encode(title.replace(Regex("(?i)\\b(giveaway|free|dlc|pack|key)\\b"), "").trim(), "UTF-8")
        } catch (e: Exception) {
            title
        }
        val options = mutableListOf<LauncherOption>()

        if (lower.contains("steam")) {
            val steamUrl = if (openGiveawayUrl.contains("steampowered.com")) openGiveawayUrl else "https://store.steampowered.com/search/?term=$encodedTitle"
            options.add(
                LauncherOption(
                    launcherType = LauncherType.STEAM,
                    name = "Steam",
                    subtitle = "Claim on Steam Store (PC)",
                    url = steamUrl,
                    badgeColorHex = 0xFF66C0F4
                )
            )
        }

        if (lower.contains("epic")) {
            val epicUrl = if (openGiveawayUrl.contains("epicgames.com")) openGiveawayUrl else "https://store.epicgames.com/browse?q=$encodedTitle"
            options.add(
                LauncherOption(
                    launcherType = LauncherType.EPIC,
                    name = "Epic Games Store",
                    subtitle = "Claim on Epic Games Store",
                    url = epicUrl,
                    badgeColorHex = 0xFF0078F2
                )
            )
        }

        if (lower.contains("playstation") || lower.contains("ps5") || lower.contains("ps4")) {
            val psSub = when {
                lower.contains("ps5") && lower.contains("ps4") -> "PlayStation Store (PS5 & PS4)"
                lower.contains("ps5") -> "PlayStation Store (PS5)"
                lower.contains("ps4") -> "PlayStation Store (PS4)"
                else -> "PlayStation Store"
            }
            val psUrl = if (openGiveawayUrl.contains("playstation.com")) openGiveawayUrl else "https://store.playstation.com/search/$encodedTitle"
            options.add(
                LauncherOption(
                    launcherType = LauncherType.PLAYSTATION,
                    name = "PlayStation Store",
                    subtitle = psSub,
                    url = psUrl,
                    badgeColorHex = 0xFF0070D1
                )
            )
        }

        if (lower.contains("xbox")) {
            val xbSub = when {
                lower.contains("series") && lower.contains("one") -> "Xbox Store (Series X|S & Xbox One)"
                lower.contains("series") -> "Xbox Store (Series X|S)"
                lower.contains("one") -> "Xbox Store (Xbox One)"
                else -> "Xbox Store"
            }
            val xbUrl = if (openGiveawayUrl.contains("xbox.com")) openGiveawayUrl else "https://www.xbox.com/search/games?q=$encodedTitle"
            options.add(
                LauncherOption(
                    launcherType = LauncherType.XBOX,
                    name = "Xbox Store",
                    subtitle = xbSub,
                    url = xbUrl,
                    badgeColorHex = 0xFF107C10
                )
            )
        }

        if (lower.contains("nintendo") || lower.contains("switch")) {
            val nintendoUrl = if (openGiveawayUrl.contains("nintendo.com")) openGiveawayUrl else "https://www.nintendo.com/us/search/#q=$encodedTitle&p=1&cat=gme&f=corePlatforms&corePlatforms=Nintendo+Switch"
            options.add(
                LauncherOption(
                    launcherType = LauncherType.NINTENDO,
                    name = "Nintendo eShop",
                    subtitle = "Nintendo Switch eShop (100% Off)",
                    url = nintendoUrl,
                    badgeColorHex = 0xFFE60012
                )
            )
        }

        if (lower.contains("gog")) {
            val gogUrl = if (openGiveawayUrl.contains("gog.com")) openGiveawayUrl else "https://www.gog.com/games?query=$encodedTitle"
            options.add(
                LauncherOption(
                    launcherType = LauncherType.GOG,
                    name = "GOG.com",
                    subtitle = "Claim DRM-Free on GOG",
                    url = gogUrl,
                    badgeColorHex = 0xFFBA55D3
                )
            )
        }

        if (lower.contains("ubisoft") || lower.contains("uplay")) {
            options.add(
                LauncherOption(
                    launcherType = LauncherType.UBISOFT,
                    name = "Ubisoft Connect",
                    subtitle = "Ubisoft Store page",
                    url = openGiveawayUrl,
                    badgeColorHex = 0xFF00A2FF
                )
            )
        }

        if (lower.contains("itch.io") || lower.contains("itchio") || tokens.any { it == "itch" }) {
            options.add(
                LauncherOption(
                    launcherType = LauncherType.ITCH_IO,
                    name = "itch.io",
                    subtitle = "Claim indie DRM-Free on itch.io",
                    url = if (openGiveawayUrl.contains("itch.io")) openGiveawayUrl else "https://itch.io/search?q=$encodedTitle",
                    badgeColorHex = 0xFFFA5C5C
                )
            )
        }

        if (lower.contains("amazon") || lower.contains("prime")) {
            options.add(
                LauncherOption(
                    launcherType = LauncherType.PRIME,
                    name = "Prime Gaming",
                    subtitle = "Claim via Amazon Prime Gaming",
                    url = if (openGiveawayUrl.contains("gaming.amazon.com")) openGiveawayUrl else "https://gaming.amazon.com/home",
                    badgeColorHex = 0xFFFF9900
                )
            )
        }

        if (lower.contains("origin") || lower.contains("ea app") || tokens.any { it == "ea" }) {
            options.add(
                LauncherOption(
                    launcherType = LauncherType.EA_ORIGIN,
                    name = "EA App",
                    subtitle = "Electronic Arts Store",
                    url = openGiveawayUrl,
                    badgeColorHex = 0xFFFF4500
                )
            )
        }

        if (lower.contains("battlenet") || lower.contains("battle.net")) {
            options.add(
                LauncherOption(
                    launcherType = LauncherType.BATTLENET,
                    name = "Battle.net",
                    subtitle = "Blizzard Battle.net Store",
                    url = openGiveawayUrl,
                    badgeColorHex = 0xFF00B4FF
                )
            )
        }

        if (lower.contains("android")) {
            val androidUrl = if (openGiveawayUrl.contains("play.google.com")) openGiveawayUrl else "https://play.google.com/store/search?q=$encodedTitle&c=apps"
            options.add(
                LauncherOption(
                    launcherType = LauncherType.ANDROID,
                    name = "Google Play / Android",
                    subtitle = "Claim on Google Play Store (Android)",
                    url = androidUrl,
                    badgeColorHex = 0xFF3DDC84
                )
            )
        }

        if (lower.contains("ios") || lower.contains("iphone") || lower.contains("ipad")) {
            val iosUrl = if (openGiveawayUrl.contains("apple.com")) openGiveawayUrl else "https://www.apple.com/us/search/$encodedTitle?src=serp"
            options.add(
                LauncherOption(
                    launcherType = LauncherType.IOS,
                    name = "Apple App Store / iOS",
                    subtitle = "Claim on App Store (iPhone & iPad)",
                    url = iosUrl,
                    badgeColorHex = 0xFF007AFF
                )
            )
        }

        // Always include Official Giveaway Claim page
        options.add(
            LauncherOption(
                launcherType = LauncherType.OTHER,
                name = "Official Giveaway Page",
                subtitle = "Direct promotion & key redemption",
                url = openGiveawayUrl,
                badgeColorHex = 0xFF00E5FF,
                isDirectStore = false
            )
        )

        return options
    }

    fun matchesLauncher(type: LauncherType): Boolean {
        if (type == LauncherType.ALL) return true
        val lower = platforms.lowercase()
        val tokens = lower.split(Regex("[,/| ]+")).map { it.trim() }
        return when (type) {
            LauncherType.ALL -> true
            LauncherType.PLAYSTATION -> isPlayStation
            LauncherType.XBOX -> isXbox
            LauncherType.NINTENDO -> isNintendo
            LauncherType.ANDROID -> isAndroid
            LauncherType.IOS -> isIos
            LauncherType.STEAM -> lower.contains("steam")
            LauncherType.EPIC -> lower.contains("epic")
            LauncherType.GOG -> lower.contains("gog")
            LauncherType.UBISOFT -> lower.contains("ubisoft") || lower.contains("uplay")
            LauncherType.ITCH_IO -> lower.contains("itch.io") || lower.contains("itchio") || tokens.any { it == "itch" }
            LauncherType.PRIME -> lower.contains("amazon") || lower.contains("prime")
            LauncherType.EA_ORIGIN -> lower.contains("origin") || lower.contains("ea app") || tokens.any { it == "ea" }
            LauncherType.BATTLENET -> lower.contains("battlenet") || lower.contains("battle.net")
            LauncherType.OTHER -> !lower.contains("steam") && !lower.contains("epic") && !lower.contains("gog") &&
                    !lower.contains("playstation") && !lower.contains("xbox") && !lower.contains("nintendo") &&
                    !lower.contains("switch") && !lower.contains("ubisoft") &&
                    !lower.contains("android") && !lower.contains("ios") && !lower.contains("iphone") && !lower.contains("ipad") &&
                    !(lower.contains("itch.io") || lower.contains("itchio") || tokens.any { it == "itch" }) &&
                    !lower.contains("prime") && !lower.contains("origin") && !tokens.any { it == "ea" }
        }
    }
}

@Immutable
data class LauncherOption(
    val launcherType: LauncherType,
    val name: String,
    val subtitle: String,
    val url: String,
    val badgeColorHex: Long,
    val isDirectStore: Boolean = true
)

enum class LauncherType(
    val displayName: String,
    val apiParam: String?,
    val badgeColorHex: Long,
    val iconName: String
) {
    ALL("All Platforms", null, 0xFF00E5FF, "all"),
    PLAYSTATION("PlayStation", "ps4+ps5", 0xFF0070D1, "playstation"),
    XBOX("Xbox", "xbox-one+xbox-series-xs", 0xFF107C10, "xbox"),
    NINTENDO("Nintendo Switch", "switch", 0xFFE60012, "nintendo"),
    ANDROID("Android", "android", 0xFF3DDC84, "android"),
    IOS("iOS", "ios", 0xFF007AFF, "ios"),
    STEAM("Steam", "steam", 0xFF66C0F4, "steam"),
    EPIC("Epic Games", "epic-games-store", 0xFF0078F2, "epic"),
    GOG("GOG", "gog", 0xFFBA55D3, "gog"),
    PRIME("Prime", "amazon-prime", 0xFFFF9900, "prime"),
    UBISOFT("Ubisoft", "ubisoft", 0xFF00A2FF, "ubisoft"),
    ITCH_IO("itch.io", "itchio", 0xFFFA5C5C, "itch"),
    EA_ORIGIN("EA App", "origin", 0xFFFF4500, "ea"),
    BATTLENET("Battle.net", "battlenet", 0xFF00B4FF, "bnet"),
    OTHER("DRM-Free / Other", null, 0xFF4ADE80, "other")
}
