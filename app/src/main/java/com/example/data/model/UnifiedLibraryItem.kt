package com.example.data.model

import androidx.compose.runtime.Immutable
import com.example.data.local.SteamGameEntity
import com.example.util.MetacriticCatalog

/**
 * Unified representation of a game in the "All + Paid" library view,
 * supporting both Steam library games and LootDrop claimed Vault games.
 */
@Immutable
sealed interface UnifiedLibraryItem {
    val key: String
    val name: String
    val playStatus: PlayStatus
    val coverImageUrl: String
    val launcherLabel: String
    val metacriticScore: Int?
    val launchUri: String

    data class Steam(val game: SteamGameEntity) : UnifiedLibraryItem {
        override val key: String get() = "steam_${game.appId}"
        override val name: String get() = game.name
        override val playStatus: PlayStatus get() = game.playStatus
        override val coverImageUrl: String get() = game.headerImageUrl
        override val launcherLabel: String get() = "Steam"
        override val metacriticScore: Int?
            get() = MetacriticCatalog.resolveScore(game.name, "game", "", game.appId).takeIf { it > 0 }
        override val launchUri: String get() = game.launchUri
    }

    data class Vault(val giveaway: GiveawayItem) : UnifiedLibraryItem {
        override val key: String get() = "vault_${giveaway.id}"
        override val name: String get() = giveaway.title
        override val playStatus: PlayStatus get() = giveaway.playStatus
        override val coverImageUrl: String get() = giveaway.image.ifEmpty { giveaway.thumbnail }
        override val launcherLabel: String get() = giveaway.launcherCategory.displayName
        override val metacriticScore: Int? get() = giveaway.metacriticScore.takeIf { it > 0 }
        override val launchUri: String get() = giveaway.openGiveawayUrl
    }
}
