package com.example.util

import com.example.data.local.SteamGameEntity
import com.example.data.model.ControllerSupport
import com.example.data.model.GameCategory
import com.example.data.model.GameHardwareProfile
import com.example.data.model.GiveawayItem
import com.example.data.model.SessionStyle
import com.example.data.model.SteamDeckStatus
import java.util.Locale
import java.util.regex.Pattern

/**
 * Intelligent catalog and heuristics engine providing Steam Deck readiness ratings,
 * controller friendliness, session styles, and storage footprints.
 */
object HardwareReadinessCatalog {

    data class PredefinedProfile(
        val deckStatus: SteamDeckStatus,
        val controllerSupport: ControllerSupport,
        val sessionStyle: SessionStyle,
        val estimatedStorageGb: Float
    )

    // Known curated database indexed by Steam App ID
    private val APP_ID_MAP = mapOf(
        // Blockbusters & Classics
        1091500 to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.DEEP_DIVE, 70.0f), // Cyberpunk 2077
        220 to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 6.5f), // Half-Life 2
        400 to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 4.0f), // Portal
        620 to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 8.0f), // Portal 2
        1145360 to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 15.0f), // Hades
        264710 to PredefinedProfile(SteamDeckStatus.PLAYABLE, ControllerSupport.FULL, SessionStyle.DEEP_DIVE, 20.0f), // Subnautica
        1190460 to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.DEEP_DIVE, 80.0f), // Death Stranding
        271590 to PredefinedProfile(SteamDeckStatus.PLAYABLE, ControllerSupport.FULL, SessionStyle.DEEP_DIVE, 105.0f), // Grand Theft Auto V
        292030 to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.DEEP_DIVE, 50.0f), // The Witcher 3
        22380 to PredefinedProfile(SteamDeckStatus.PLAYABLE, ControllerSupport.PARTIAL, SessionStyle.DEEP_DIVE, 10.0f), // Fallout: New Vegas
        377160 to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.DEEP_DIVE, 30.0f), // Fallout 4
        413150 to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 0.6f), // Stardew Valley
        1794680 to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 0.5f), // Vampire Survivors
        588650 to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 2.0f), // Dead Cells
        504230 to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 1.2f), // Celeste
        367520 to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 9.0f), // Hollow Knight
        105600 to PredefinedProfile(SteamDeckStatus.PLAYABLE, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 0.5f), // Terraria
        1492070 to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 15.0f), // Sker Ritual
        289070 to PredefinedProfile(SteamDeckStatus.PLAYABLE, ControllerSupport.PARTIAL, SessionStyle.DEEP_DIVE, 25.0f), // Sid Meier's Civilization VI
        412020 to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.DEEP_DIVE, 59.0f), // Metro Exodus
        230410 to PredefinedProfile(SteamDeckStatus.PLAYABLE, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 40.0f), // Warframe
        1086940 to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.DEEP_DIVE, 150.0f), // Baldur's Gate 3
        1245620 to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.DEEP_DIVE, 60.0f), // Elden Ring
        730 to PredefinedProfile(SteamDeckStatus.PLAYABLE, ControllerSupport.KEYBOARD_MOUSE, SessionStyle.QUICK_SESSION, 35.0f), // Counter-Strike 2
        570 to PredefinedProfile(SteamDeckStatus.PLAYABLE, ControllerSupport.KEYBOARD_MOUSE, SessionStyle.QUICK_SESSION, 60.0f), // Dota 2
        1085660 to PredefinedProfile(SteamDeckStatus.UNSUPPORTED, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 105.0f), // Destiny 2 (BattlEye anti-cheat)
        359550 to PredefinedProfile(SteamDeckStatus.UNSUPPORTED, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 85.0f), // Rainbow Six Siege
        814380 to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.DEEP_DIVE, 25.0f), // Sekiro
        546560 to PredefinedProfile(SteamDeckStatus.UNSUPPORTED, ControllerSupport.FULL, SessionStyle.DEEP_DIVE, 67.0f), // Half-Life: Alyx (VR)
        1238810 to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 120.0f), // Battlefield 2042
        1238840 to PredefinedProfile(SteamDeckStatus.PLAYABLE, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 50.0f) // Battlefield V
    )

    // Curated title map for cross-platform matches (e.g. Epic, GOG, PlayStation giveaways)
    private val TITLE_MAP = mapOf(
        "cyberpunk 2077" to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.DEEP_DIVE, 70.0f),
        "half-life 2" to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 6.5f),
        "portal" to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 4.0f),
        "portal 2" to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 8.0f),
        "hades" to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 15.0f),
        "subnautica" to PredefinedProfile(SteamDeckStatus.PLAYABLE, ControllerSupport.FULL, SessionStyle.DEEP_DIVE, 20.0f),
        "death stranding" to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.DEEP_DIVE, 80.0f),
        "death stranding director's cut" to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.DEEP_DIVE, 80.0f),
        "grand theft auto v" to PredefinedProfile(SteamDeckStatus.PLAYABLE, ControllerSupport.FULL, SessionStyle.DEEP_DIVE, 105.0f),
        "gta v" to PredefinedProfile(SteamDeckStatus.PLAYABLE, ControllerSupport.FULL, SessionStyle.DEEP_DIVE, 105.0f),
        "the witcher 3" to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.DEEP_DIVE, 50.0f),
        "fallout new vegas" to PredefinedProfile(SteamDeckStatus.PLAYABLE, ControllerSupport.PARTIAL, SessionStyle.DEEP_DIVE, 10.0f),
        "fallout 4" to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.DEEP_DIVE, 30.0f),
        "stardew valley" to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 0.6f),
        "vampire survivors" to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 0.5f),
        "dead cells" to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 2.0f),
        "celeste" to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 1.2f),
        "hollow knight" to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 9.0f),
        "terraria" to PredefinedProfile(SteamDeckStatus.PLAYABLE, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 0.5f),
        "sker ritual" to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 15.0f),
        "civilization vi" to PredefinedProfile(SteamDeckStatus.PLAYABLE, ControllerSupport.PARTIAL, SessionStyle.DEEP_DIVE, 25.0f),
        "metro exodus" to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.DEEP_DIVE, 59.0f),
        "warframe" to PredefinedProfile(SteamDeckStatus.PLAYABLE, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 40.0f),
        "destiny 2" to PredefinedProfile(SteamDeckStatus.UNSUPPORTED, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 105.0f),
        "control" to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.DEEP_DIVE, 42.0f),
        "prey" to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.DEEP_DIVE, 20.0f),
        "dishonored" to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.DEEP_DIVE, 9.0f),
        "borderlands 3" to PredefinedProfile(SteamDeckStatus.PLAYABLE, ControllerSupport.FULL, SessionStyle.QUICK_SESSION, 75.0f),
        "bioshock remastered" to PredefinedProfile(SteamDeckStatus.PLAYABLE, ControllerSupport.FULL, SessionStyle.DEEP_DIVE, 25.0f),
        "tomb raider" to PredefinedProfile(SteamDeckStatus.VERIFIED, ControllerSupport.FULL, SessionStyle.DEEP_DIVE, 12.0f)
    )

    private val STORAGE_REGEX = Pattern.compile(
        """(?i)(\d+(?:\.\d+)?)\s*(?:GB|MB)\s*(?:available|free|storage|space|disk|required|hd|install)"""
    )

    /**
     * Resolves the hardware readiness profile by App ID, clean title, or heuristics.
     */
    fun getProfile(
        steamAppId: Int? = null,
        title: String,
        platforms: String = "PC",
        description: String = "",
        manualInstallSizeGb: Float? = null
    ): GameHardwareProfile {
        if (steamAppId != null) {
            val curated = APP_ID_MAP[steamAppId]
            if (curated != null) {
                return GameHardwareProfile(
                    deckStatus = curated.deckStatus,
                    controllerSupport = curated.controllerSupport,
                    sessionStyle = curated.sessionStyle,
                    estimatedStorageGb = curated.estimatedStorageGb,
                    manualStorageGb = manualInstallSizeGb
                )
            }
        }

        val cleanTitle = MetacriticCatalog.cleanTitle(title).lowercase(Locale.ROOT)
        for ((key, curated) in TITLE_MAP) {
            if (cleanTitle == key || (cleanTitle.length > 5 && cleanTitle.contains(key))) {
                return GameHardwareProfile(
                    deckStatus = curated.deckStatus,
                    controllerSupport = curated.controllerSupport,
                    sessionStyle = curated.sessionStyle,
                    estimatedStorageGb = curated.estimatedStorageGb,
                    manualStorageGb = manualInstallSizeGb
                )
            }
        }

        return inferHardwareProfile(
            title = title,
            type = "Game",
            platforms = platforms,
            description = description,
            categories = GameCategory.inferCategories("Game", title, description),
            numericWorth = 0.0,
            manualStorageGb = manualInstallSizeGb
        )
    }

    /**
     * Resolves the hardware readiness profile for a giveaway item.
     */
    fun resolveProfile(item: GiveawayItem): GameHardwareProfile {
        // 1. Check direct Steam App ID
        if (item.steamAppId != null) {
            val curated = APP_ID_MAP[item.steamAppId]
            if (curated != null) {
                return GameHardwareProfile(
                    deckStatus = curated.deckStatus,
                    controllerSupport = curated.controllerSupport,
                    sessionStyle = curated.sessionStyle,
                    estimatedStorageGb = curated.estimatedStorageGb,
                    manualStorageGb = item.manualInstallSizeGb
                )
            }
        }

        // 2. Check title against curated title list
        val cleanTitle = MetacriticCatalog.cleanTitle(item.title).lowercase(Locale.ROOT)
        for ((key, curated) in TITLE_MAP) {
            if (cleanTitle == key || (cleanTitle.length > 5 && cleanTitle.contains(key))) {
                return GameHardwareProfile(
                    deckStatus = curated.deckStatus,
                    controllerSupport = curated.controllerSupport,
                    sessionStyle = curated.sessionStyle,
                    estimatedStorageGb = curated.estimatedStorageGb,
                    manualStorageGb = item.manualInstallSizeGb
                )
            }
        }

        // 3. Fallback inference through heuristics
        return inferHardwareProfile(
            title = item.title,
            type = item.type,
            platforms = item.platforms,
            description = item.description,
            categories = item.categories,
            numericWorth = item.numericWorth,
            manualStorageGb = item.manualInstallSizeGb
        )
    }

    /**
     * Resolves the hardware readiness profile for a Steam owned game.
     */
    fun resolveProfile(game: SteamGameEntity): GameHardwareProfile {
        // 1. Direct App ID lookup
        val curated = APP_ID_MAP[game.appId]
        if (curated != null) {
            return GameHardwareProfile(
                deckStatus = curated.deckStatus,
                controllerSupport = curated.controllerSupport,
                sessionStyle = curated.sessionStyle,
                estimatedStorageGb = curated.estimatedStorageGb,
                manualStorageGb = game.manualInstallSizeGb
            )
        }

        // 2. Title lookup
        val cleanTitle = MetacriticCatalog.cleanTitle(game.name).lowercase(Locale.ROOT)
        for ((key, predefined) in TITLE_MAP) {
            if (cleanTitle == key || (cleanTitle.length > 5 && cleanTitle.contains(key))) {
                return GameHardwareProfile(
                    deckStatus = predefined.deckStatus,
                    controllerSupport = predefined.controllerSupport,
                    sessionStyle = predefined.sessionStyle,
                    estimatedStorageGb = predefined.estimatedStorageGb,
                    manualStorageGb = game.manualInstallSizeGb
                )
            }
        }

        // 3. Inference
        return inferHardwareProfile(
            title = game.name,
            type = "Game",
            platforms = "Steam, PC",
            description = "",
            categories = GameCategory.inferCategories("Game", game.name, ""),
            numericWorth = 19.99,
            manualStorageGb = game.manualInstallSizeGb
        )
    }

    private fun inferHardwareProfile(
        title: String,
        type: String,
        platforms: String,
        description: String,
        categories: List<GameCategory>,
        numericWorth: Double,
        manualStorageGb: Float?
    ): GameHardwareProfile {
        val lowerText = "$title $description $type $platforms".lowercase(Locale.ROOT)
        val isDlcOrPack = type.contains("DLC", ignoreCase = true) ||
                type.contains("Loot", ignoreCase = true) ||
                title.contains("Pack", ignoreCase = true) ||
                title.contains("DLC", ignoreCase = true)

        // A. Storage Extraction from text
        var estimatedGb: Float? = null
        val matcher = STORAGE_REGEX.matcher(description)
        if (matcher.find()) {
            val numStr = matcher.group(1)
            val fullMatch = matcher.group(0)?.lowercase(Locale.ROOT) ?: ""
            val parsedNum = numStr?.toFloatOrNull()
            if (parsedNum != null && parsedNum > 0f) {
                estimatedGb = if (fullMatch.contains("mb")) (parsedNum / 1024f) else parsedNum
            }
        }

        if (estimatedGb == null) {
            estimatedGb = when {
                isDlcOrPack -> 0.4f
                categories.contains(GameCategory.INDIE) || categories.contains(GameCategory.PUZZLE) -> 1.5f
                categories.contains(GameCategory.STRATEGY) || categories.contains(GameCategory.SIMULATION) -> 6.0f
                numericWorth >= 40.0 || categories.contains(GameCategory.RPG) -> 45.0f
                numericWorth >= 20.0 -> 22.0f
                else -> 8.0f
            }
        }

        // B. Controller Support
        val isConsolePort = platforms.contains("PlayStation", ignoreCase = true) ||
                platforms.contains("Xbox", ignoreCase = true) ||
                platforms.contains("Switch", ignoreCase = true) ||
                platforms.contains("Nintendo", ignoreCase = true)

        val controllerSupport = when {
            isConsolePort -> ControllerSupport.FULL
            lowerText.contains("controller") || lowerText.contains("gamepad") -> ControllerSupport.FULL
            categories.contains(GameCategory.RACING_SPORTS) || categories.contains(GameCategory.ACTION) ||
                    categories.contains(GameCategory.SHOOTER) || categories.contains(GameCategory.INDIE) -> ControllerSupport.FULL
            categories.contains(GameCategory.STRATEGY) || categories.contains(GameCategory.SIMULATION) -> ControllerSupport.PARTIAL
            else -> ControllerSupport.FULL
        }

        // C. Session Style
        val sessionStyle = when {
            isDlcOrPack -> SessionStyle.QUICK_SESSION
            categories.contains(GameCategory.INDIE) ||
                    categories.contains(GameCategory.PUZZLE) ||
                    categories.contains(GameCategory.RACING_SPORTS) ||
                    lowerText.contains("roguelike") ||
                    lowerText.contains("roguelite") ||
                    lowerText.contains("arcade") ||
                    lowerText.contains("card") ||
                    lowerText.contains("casual") -> SessionStyle.QUICK_SESSION
            categories.contains(GameCategory.RPG) ||
                    categories.contains(GameCategory.STRATEGY) ||
                    lowerText.contains("open world") ||
                    lowerText.contains("mmo") -> SessionStyle.DEEP_DIVE
            else -> SessionStyle.QUICK_SESSION
        }

        // D. Steam Deck Status
        val deckStatus = when {
            lowerText.contains("vr only") || lowerText.contains("requires vr") -> SteamDeckStatus.UNSUPPORTED
            lowerText.contains("easy anti-cheat") || lowerText.contains("vanguard") -> SteamDeckStatus.UNSUPPORTED
            isDlcOrPack -> SteamDeckStatus.VERIFIED
            controllerSupport == ControllerSupport.FULL && estimatedGb <= 30.0f -> SteamDeckStatus.VERIFIED
            controllerSupport == ControllerSupport.FULL -> SteamDeckStatus.PLAYABLE
            controllerSupport == ControllerSupport.PARTIAL -> SteamDeckStatus.PLAYABLE
            else -> SteamDeckStatus.PLAYABLE
        }

        return GameHardwareProfile(
            deckStatus = deckStatus,
            controllerSupport = controllerSupport,
            sessionStyle = sessionStyle,
            estimatedStorageGb = estimatedGb,
            manualStorageGb = manualStorageGb
        )
    }
}
