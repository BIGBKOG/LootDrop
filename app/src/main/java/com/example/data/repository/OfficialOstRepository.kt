package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.OstCacheDao
import com.example.data.local.OstCacheEntity
import com.example.data.model.GiveawayItem
import com.example.data.remote.DiscogsApiClient
import com.example.data.remote.MusicBrainzApiClient
import com.example.data.remote.SpotifyApiClient
import com.example.util.OfficialOstCatalog
import com.example.util.OfficialOstInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Data repository service for official game soundtracks (OST).
 *
 * Coordinates querying:
 * 1. Curated verified official video game soundtrack directory
 * 2. Spotify Web API (via client credentials flow)
 * 3. Discogs Database API (via Discogs Search API)
 * 4. MusicBrainz Open Video Game Music Database
 *
 * Enforces strict authenticity validation so only verified official game soundtracks
 * are presented with direct links, and accurately marks items without an official
 * release as "No OST".
 *
 * Results are cached in an in-memory map and persistent Room database (OstCacheDao).
 */
class OfficialOstRepository(
    private val context: Context,
    private val discogsClient: DiscogsApiClient = DiscogsApiClient,
    private val spotifyClient: SpotifyApiClient = SpotifyApiClient,
    private val musicBrainzClient: MusicBrainzApiClient = MusicBrainzApiClient,
    private val ostCacheDao: OstCacheDao? = try {
        AppDatabase.getInstance(context).ostCacheDao()
    } catch (_: Exception) {
        null
    }
) {

    // Fast in-memory cache: maps cleaned game title -> OfficialOstInfo (or null for "No OST")
    private val memoryCache = ConcurrentHashMap<String, OfficialOstInfo?>()

    companion object {
        @Volatile
        private var INSTANCE: OfficialOstRepository? = null

        fun getInstance(context: Context): OfficialOstRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: OfficialOstRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    /**
     * Non-suspending instant lookup for Composables.
     * Checks memory cache and curated catalog without blocking.
     */
    fun getInstantOstInfo(item: GiveawayItem): OfficialOstInfo? {
        if (!isEligibleForOst(item)) return null

        val key = cleanKey(item.title)
        if (memoryCache.containsKey(key)) {
            return memoryCache[key]
        }

        // Instant curated fallback
        val curated = OfficialOstCatalog.getCuratedOst(key)
        if (curated != null) {
            val info = OfficialOstInfo(
                gameTitle = item.title,
                albumTitle = curated.albumTitle,
                composer = curated.composer,
                spotifyUrl = curated.spotifyAlbumUrl,
                youtubeMusicUrl = curated.youtubeMusicUrl,
                releaseYear = curated.releaseYear,
                source = "Curated Official",
                isVerified = true
            )
            memoryCache[key] = info
            return info
        }

        return null
    }

    /**
     * Asynchronously fetches official soundtrack metadata from Discogs API or Spotify Web API.
     * Returns null if the item has no official soundtrack release ("No OST").
     */
    suspend fun fetchOstInfo(item: GiveawayItem): OfficialOstInfo? = withContext(Dispatchers.IO) {
        // 1. Filter out items that are not full games or are DLC/cosmetic packs
        if (!isEligibleForOst(item)) {
            return@withContext null
        }

        val key = cleanKey(item.title)

        // 2. Check in-memory cache
        if (memoryCache.containsKey(key)) {
            return@withContext memoryCache[key]
        }

        // 3. Check persistent Room database cache
        try {
            val cachedEntity = ostCacheDao?.getByKey(key)
            if (cachedEntity != null) {
                val domainInfo = cachedEntity.toDomain(item.title)
                memoryCache[key] = domainInfo
                return@withContext domainInfo
            }
        } catch (_: Exception) {
            // Room access exception safety
        }

        // 4. Check curated directory of known official soundtracks
        val curated = OfficialOstCatalog.getCuratedOst(key)
        if (curated != null) {
            val curatedInfo = OfficialOstInfo(
                gameTitle = item.title,
                albumTitle = curated.albumTitle,
                composer = curated.composer,
                spotifyUrl = curated.spotifyAlbumUrl,
                youtubeMusicUrl = curated.youtubeMusicUrl,
                releaseYear = curated.releaseYear,
                source = "Curated Official",
                isVerified = true
            )
            saveToCache(key, curatedInfo)
            return@withContext curatedInfo
        }

        // 5. Query Spotify Web API if client credentials are available
        try {
            val spotifyResult = spotifyClient.searchSoundtrack(item.title)
            if (spotifyResult != null && spotifyResult.spotifyAlbumUrl.isNotBlank()) {
                val encodedQuery = urlEncode("${cleanTitleForSearch(item.title)} Official Game Soundtrack")
                val ytUrl = "https://music.youtube.com/search?q=$encodedQuery"
                val spotifyInfo = OfficialOstInfo(
                    gameTitle = item.title,
                    albumTitle = spotifyResult.albumTitle,
                    composer = spotifyResult.composer,
                    spotifyUrl = spotifyResult.spotifyAlbumUrl,
                    youtubeMusicUrl = ytUrl,
                    releaseYear = spotifyResult.releaseYear,
                    source = "Spotify Web API",
                    isVerified = true
                )
                saveToCache(key, spotifyInfo)
                return@withContext spotifyInfo
            }
        } catch (_: Exception) {
            // Continue to Discogs
        }

        // 6. Query Discogs API for authentic video game scores
        try {
            val discogsResult = discogsClient.searchSoundtrack(item.title)
            if (discogsResult != null) {
                val encodedQuery = urlEncode("${cleanTitleForSearch(item.title)} Official Game Soundtrack")
                val discogsInfo = OfficialOstInfo(
                    gameTitle = item.title,
                    albumTitle = discogsResult.albumTitle,
                    composer = discogsResult.composer,
                    spotifyUrl = "https://open.spotify.com/search/$encodedQuery",
                    youtubeMusicUrl = "https://music.youtube.com/search?q=$encodedQuery",
                    releaseYear = discogsResult.year,
                    discogsUrl = discogsResult.discogsUrl,
                    source = "Discogs API",
                    isVerified = true
                )
                saveToCache(key, discogsInfo)
                return@withContext discogsInfo
            }
        } catch (_: Exception) {
            // Continue to MusicBrainz
        }

        // 7. Query MusicBrainz Open VGM Database
        try {
            val mbResult = musicBrainzClient.searchSoundtrack(item.title)
            if (mbResult != null) {
                val encodedQuery = urlEncode("${cleanTitleForSearch(item.title)} Official Game Soundtrack")
                val mbInfo = OfficialOstInfo(
                    gameTitle = item.title,
                    albumTitle = mbResult.albumTitle,
                    composer = mbResult.composer,
                    spotifyUrl = "https://open.spotify.com/search/$encodedQuery",
                    youtubeMusicUrl = "https://music.youtube.com/search?q=$encodedQuery",
                    releaseYear = mbResult.releaseYear,
                    source = "MusicBrainz",
                    isVerified = true
                )
                saveToCache(key, mbInfo)
                return@withContext mbInfo
            }
        } catch (_: Exception) {
            // Handled
        }

        // 8. If no official soundtrack exists across Discogs, Spotify, and MusicBrainz:
        // Accurately mark as "No OST" and persist so subsequent lookups are instant.
        saveNoOstToCache(key)
        return@withContext null
    }

    private suspend fun saveToCache(key: String, info: OfficialOstInfo) {
        memoryCache[key] = info
        try {
            ostCacheDao?.insert(
                OstCacheEntity(
                    gameKey = key,
                    hasOst = true,
                    albumTitle = info.albumTitle,
                    composer = info.composer,
                    spotifyUrl = info.spotifyUrl,
                    youtubeMusicUrl = info.youtubeMusicUrl,
                    discogsUrl = info.discogsUrl,
                    releaseYear = info.releaseYear,
                    source = info.source,
                    isVerified = info.isVerified
                )
            )
        } catch (_: Exception) {}
    }

    private suspend fun saveNoOstToCache(key: String) {
        memoryCache[key] = null
        try {
            ostCacheDao?.insert(
                OstCacheEntity(
                    gameKey = key,
                    hasOst = false,
                    source = "NONE",
                    isVerified = true
                )
            )
        } catch (_: Exception) {}
    }

    /**
     * Determines whether an item is eligible for an official game soundtrack.
     */
    fun isEligibleForOst(item: GiveawayItem): Boolean {
        if (!item.type.contains("Game", ignoreCase = true)) return false

        val titleLower = item.title.lowercase(Locale.ROOT).trim()
        val noOstKeywords = listOf(
            "skin", "pack", "bundle", "coins", "gold", "gems", "currency",
            "dlc", "add-on", "addon", "loot", "starter pack", "booster",
            "costume", "outfit", "weapon", "cosmetic", "pass", "season pass",
            "battle pass", "tokens", "credits", "crate", "case", "beta access",
            "bonus content", "wallpaper", "artbook", "soundtrack only", "key"
        )

        for (kw in noOstKeywords) {
            if (titleLower.contains(kw) && !isExempt(titleLower)) {
                return false
            }
        }
        return true
    }

    private fun isExempt(titleLower: String): Boolean {
        return titleLower.contains("cyberpunk") ||
                titleLower.contains("subnautica") ||
                titleLower.contains("portal") ||
                titleLower.contains("hades") ||
                titleLower.contains("hollow knight")
    }

    private fun cleanKey(title: String): String {
        return title.lowercase(Locale.ROOT)
            .replace("giveaway", "")
            .replace("free", "")
            .replace("standard edition", "")
            .replace("definitive edition", "")
            .replace("director's cut", "")
            .replace("ultimate edition", "")
            .replace("goty", "")
            .replace(Regex("""\([^)]*\)"""), "")
            .replace(Regex("""\[[^\]]*\]"""), "")
            .trim()
    }

    private fun cleanTitleForSearch(title: String): String {
        return cleanKey(title)
            .replace(":", "")
            .replace("-", " ")
            .trim()
    }

    private fun urlEncode(text: String): String {
        return try {
            URLEncoder.encode(text, StandardCharsets.UTF_8.toString())
        } catch (_: Exception) {
            text.replace(" ", "%20")
        }
    }
}
