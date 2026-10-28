package com.example.data.model

import androidx.compose.runtime.Immutable

/**
 * Interactive Game Tracker Play Status
 *
 * Supported states:
 * - NOT_STARTED (Backlog / Unplayed)
 * - PLAYING (In Progress)
 * - BEATEN (Finished / 100% / Story Completed)
 * - DROPPED (Abandoned)
 */
@Immutable
enum class PlayStatus(
    val label: String,
    val filterChipLabel: String,
    val badgeColorHex: Long
) {
    NOT_STARTED("Backlog", "Unplayed", 0xFF94A3B8),
    PLAYING("Playing", "Playing", 0xFF00E5FF),
    BEATEN("Finished", "Finished", 0xFF00E676),
    DROPPED("Dropped", "Dropped", 0xFFEF4444);

    val displayName: String get() = label

    companion object {
        fun fromString(value: String?): PlayStatus {
            return when (value?.trim()?.uppercase()) {
                "PLAYING" -> PLAYING
                "BEATEN", "FINISHED" -> BEATEN
                "DROPPED" -> DROPPED
                else -> NOT_STARTED
            }
        }
    }
}

/**
 * Library Filter Chips in the "In Library" / Claimed Tab
 */
enum class LibraryPlayFilter(val label: String, val status: PlayStatus?) {
    ALL("All", null),
    UNPLAYED("Unplayed", PlayStatus.NOT_STARTED),
    PLAYING("Playing", PlayStatus.PLAYING),
    FINISHED("Finished", PlayStatus.BEATEN),
    DROPPED("Dropped", PlayStatus.DROPPED);

    val displayName: String get() = label
}

/**
 * In Library View Mode: Claimed Free Vault vs All + Paid Steam Games
 */
@Immutable
enum class LibraryViewMode(val label: String, val subtitle: String) {
    VAULT("Claimed Vault", "Freebies claimed in LootDrop"),
    ALL_PAID("All + Paid", "All Steam games (Paid + Free)");

    val displayName: String get() = label
}

