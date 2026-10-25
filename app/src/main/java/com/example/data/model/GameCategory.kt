package com.example.data.model

import androidx.compose.ui.graphics.Color

/**
 * Supported game genres & categories for filtering and card metadata.
 */
enum class GameCategory(
    val id: String,
    val displayName: String,
    val badgeColorHex: Long,
    val iconEmoji: String,
    val description: String
) {
    FULL_GAME("full_game", "Full Game", 0xFF00E5FF, "🎮", "Complete base games (100% free)"),
    DLC_LOOT("dlc_loot", "DLC & Packs", 0xFFFFD700, "📦", "Add-ons, expansions, skins & starter packs"),
    ACTION("action", "Action", 0xFFFF5722, "⚔️", "Fast-paced combat, hack & slash, brawlers"),
    RPG("rpg", "RPG", 0xFFAB47BC, "🛡️", "Role-playing, character progression & story quests"),
    SHOOTER("shooter", "Shooter", 0xFFE91E63, "🎯", "FPS, third-person shooter & tactical gunplay"),
    STRATEGY("strategy", "Strategy", 0xFF3F51B5, "♟️", "RTS, turn-based, 4X, tactical & tower defense"),
    ADVENTURE("adventure", "Adventure", 0xFF00BFA5, "🧭", "Narrative, exploration & story-rich journeys"),
    INDIE("indie", "Indie", 0xFFFF9800, "💎", "Creative independent titles, pixel art & roguelikes"),
    SIMULATION("simulation", "Simulation", 0xFF8BC34A, "🚜", "Simulators, building, tycoon & sandboxes"),
    HORROR("horror", "Horror", 0xFFE53935, "👻", "Survival horror, dark atmosphere & thrillers"),
    MULTIPLAYER("multiplayer", "Multiplayer", 0xFF00E676, "🌐", "Online co-op, PvP, MMO & team multiplayer"),
    RACING_SPORTS("racing_sports", "Racing & Sports", 0xFFFFC107, "🏎️", "Motorsports, driving, athletic & sports games"),
    PUZZLE("puzzle", "Puzzle & Casual", 0xFF26A69A, "🧩", "Logic, brain teasers, casual & board games"),
    SCI_FI("sci_fi", "Sci-Fi & Cyberpunk", 0xFF00B0FF, "🚀", "Futuristic, space, aliens & cybernetic worlds"),
    FANTASY("fantasy", "Fantasy", 0xFF8E24AA, "🧙", "Magic, medieval, dragons & mythical realms");

    val colorHex: Long get() = badgeColorHex

    companion object {
        private val ACTION_REGEX = Regex("(?i)\\b(action|combat|hack and slash|hack-and-slash|beat 'em up|brawler|melee|fighting|swordfight|assassin)\\b")
        private val RPG_REGEX = Regex("(?i)\\b(rpg|role-playing|role playing|jrpg|dungeon crawler|character progression|quest|quests|level up|loot\\b.*stats|inventory)\\b")
        private val SHOOTER_REGEX = Regex("(?i)\\b(shooter|fps|tps|first-person shooter|third-person shooter|sniper|gunplay|guns|bullet|battle royale|crosshair)\\b")
        private val STRATEGY_REGEX = Regex("(?i)\\b(strategy|rts|turn-based|turn based|tactics|tactical|tower defense|4x|grand strategy|deckbuilder|card battle|commander)\\b")
        private val ADVENTURE_REGEX = Regex("(?i)\\b(adventure|exploration|explore|narrative|story-driven|story-rich|journey|point-and-click|quest)\\b")
        private val INDIE_REGEX = Regex("(?i)\\b(indie|pixel art|pixel-art|retro|roguelike|roguelite|platformer|metroidvania|hand-drawn)\\b")
        private val SIMULATION_REGEX = Regex("(?i)\\b(simulator|simulation|tycoon|management|sandbox|city builder|builder|farming|crafting)\\b")
        private val HORROR_REGEX = Regex("(?i)\\b(horror|spooky|survival horror|zombie|zombies|undead|apocalyptic|terror|thriller|nightmare)\\b")
        private val MULTIPLAYER_REGEX = Regex("(?i)\\b(multiplayer|co-op|cooperative|pvp|mmo|mmorpg|online team|squad|crossplay|multi-player)\\b")
        private val RACING_SPORTS_REGEX = Regex("(?i)\\b(racing|driving|motorsport|drifting|cars|vehicle|soccer|football|basketball|sports|athletics|tennis|golf)\\b")
        private val PUZZLE_REGEX = Regex("(?i)\\b(puzzle|puzzles|brain|riddle|physics-based|casual|card game|board game|match-3)\\b")
        private val SCI_FI_REGEX = Regex("(?i)\\b(sci-fi|scifi|cyberpunk|space|spaceship|galaxy|alien|aliens|futuristic|robot|robots|cyborg)\\b")
        private val FANTASY_REGEX = Regex("(?i)\\b(fantasy|magic|wizard|wizards|dragon|dragons|medieval|mythical|sword|kingdom|spell|spells)\\b")

        /**
         * Infers a rich list of categories for any giveaway item based on its type, title, and description.
         */
        fun inferCategories(type: String, title: String, description: String): List<GameCategory> {
            val list = mutableListOf<GameCategory>()
            val combinedText = "$title $description $type"

            // 1. Core Format category
            if (type.contains("DLC", ignoreCase = true) ||
                type.contains("Loot", ignoreCase = true) ||
                type.contains("Pack", ignoreCase = true) ||
                type.contains("Item", ignoreCase = true) ||
                title.contains("Pack", ignoreCase = true) ||
                title.contains("DLC", ignoreCase = true)
            ) {
                list.add(DLC_LOOT)
            } else {
                list.add(FULL_GAME)
            }

            // 2. Genre detection
            if (ACTION_REGEX.containsMatchIn(combinedText)) list.add(ACTION)
            if (RPG_REGEX.containsMatchIn(combinedText)) list.add(RPG)
            if (SHOOTER_REGEX.containsMatchIn(combinedText)) list.add(SHOOTER)
            if (STRATEGY_REGEX.containsMatchIn(combinedText)) list.add(STRATEGY)
            if (ADVENTURE_REGEX.containsMatchIn(combinedText)) list.add(ADVENTURE)
            if (INDIE_REGEX.containsMatchIn(combinedText)) list.add(INDIE)
            if (SIMULATION_REGEX.containsMatchIn(combinedText)) list.add(SIMULATION)
            if (HORROR_REGEX.containsMatchIn(combinedText)) list.add(HORROR)
            if (MULTIPLAYER_REGEX.containsMatchIn(combinedText)) list.add(MULTIPLAYER)
            if (RACING_SPORTS_REGEX.containsMatchIn(combinedText)) list.add(RACING_SPORTS)
            if (PUZZLE_REGEX.containsMatchIn(combinedText)) list.add(PUZZLE)
            if (SCI_FI_REGEX.containsMatchIn(combinedText)) list.add(SCI_FI)
            if (FANTASY_REGEX.containsMatchIn(combinedText)) list.add(FANTASY)

            // Ensure every game has at least 2 categories
            if (list.size == 1) {
                if (list.contains(FULL_GAME)) {
                    list.add(ACTION)
                    list.add(ADVENTURE)
                } else {
                    list.add(MULTIPLAYER)
                }
            }

            return list.distinct()
        }
    }
}
