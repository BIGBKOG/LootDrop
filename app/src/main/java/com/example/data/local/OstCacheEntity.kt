package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.util.OfficialOstInfo

/**
 * Local cache entity for Official Game Soundtrack (OST) lookups.
 * Stores confirmed soundtrack links (Spotify, YouTube Music, Discogs) or flags
 * items that definitively have "No OST" to avoid repeated network lookups.
 */
@Entity(
    tableName = "ost_cache",
    indices = [
        Index(value = ["hasOst"]),
        Index(value = ["cachedTimestamp"])
    ]
)
data class OstCacheEntity(
    @PrimaryKey val gameKey: String,
    val hasOst: Boolean,
    val albumTitle: String? = null,
    val composer: String? = null,
    val spotifyUrl: String? = null,
    val youtubeMusicUrl: String? = null,
    val discogsUrl: String? = null,
    val releaseYear: String? = null,
    val source: String = "NONE",
    val isVerified: Boolean = true,
    val cachedTimestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(gameTitle: String): OfficialOstInfo? {
        if (!hasOst || albumTitle.isNullOrBlank()) return null
        return OfficialOstInfo(
            gameTitle = gameTitle,
            albumTitle = albumTitle,
            composer = composer ?: "Original Game Soundtrack",
            spotifyUrl = spotifyUrl ?: "",
            youtubeMusicUrl = youtubeMusicUrl ?: "",
            releaseYear = releaseYear,
            discogsUrl = discogsUrl,
            source = source,
            isVerified = isVerified
        )
    }
}
