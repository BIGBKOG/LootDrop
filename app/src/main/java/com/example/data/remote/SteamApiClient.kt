package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.GiveawayItem
import com.example.data.model.PlayStatus
import com.example.util.MetacriticCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

data class SteamOwnedGame(
    val appId: Int,
    val name: String,
    val playtimeForeverMinutes: Int,
    val playtime2WeeksMinutes: Int = 0,
    val rtimeLastPlayed: Long = 0L
)

data class SteamAchievementsResult(
    val unlockedCount: Int,
    val totalCount: Int,
    val isComplete: Boolean
)

data class SteamSyncResult(
    val totalGamesSynced: Int,
    val unplayedCount: Int,
    val playingCount: Int,
    val finishedCount: Int,
    val message: String
)

/**
 * Steam Web API Client
 *
 * Interacts with Valve's Steam Web API to fetch:
 * - Owned games & playtime (IPlayerService/GetOwnedGames)
 * - Player achievements (ISteamUserStats/GetPlayerAchievements)
 * - Vanity profile URLs to 64-bit SteamIDs (ISteamUser/ResolveVanityURL)
 *
 * Automatically classifies claimed games into:
 * - NOT_STARTED (Backlog): 0 playtime
 * - PLAYING: > 0 playtime, in-progress achievements
 * - BEATEN: 100% achievements or high completion playtime
 * - DROPPED: user designated
 */
object SteamApiClient {

    private const val TAG = "SteamApiClient"

    // Default key provided by user, with fallback to BuildConfig.STEAM_API_KEY
    const val DEFAULT_API_KEY = "A5946B8A131FE51206631D8A0B3101BF"

    @Volatile
    private var steamHttpClient: OkHttpClient? = null

    private fun getClient(): OkHttpClient {
        return steamHttpClient ?: synchronized(this) {
            steamHttpClient ?: OkHttpClient.Builder()
                .connectTimeout(12, TimeUnit.SECONDS)
                .readTimeout(12, TimeUnit.SECONDS)
                .build().also { steamHttpClient = it }
        }
    }

    /**
     * Resolves the active Steam API key, prioritizing user preferences,
     * then BuildConfig, then the provided default key.
     */
    fun resolveApiKey(userKey: String?): String {
        if (!userKey.isNullOrBlank()) return userKey.trim()
        val buildKey = runCatching {
            val field = BuildConfig::class.java.getField("STEAM_API_KEY")
            field.get(null) as? String
        }.getOrNull()
        if (!buildKey.isNullOrBlank()) return buildKey.trim()
        return DEFAULT_API_KEY
    }

    /**
     * Resolves a vanity profile name (e.g. "gabelogannewell") to a 64-bit Steam ID.
     * If the input is already a 17-digit numeric Steam ID, returns it immediately.
     */
    suspend fun resolveSteamId(vanityOrId: String, apiKey: String): String? = withContext(Dispatchers.IO) {
        val trimmed = vanityOrId.trim()
        if (trimmed.isBlank()) return@withContext null

        // If it's already a 17-digit numeric Steam ID (76561198...)
        if (trimmed.length == 17 && trimmed.all { it.isDigit() }) {
            return@withContext trimmed
        }

        // Clean any leading url parts (e.g. steamcommunity.com/id/gaben/ -> gaben)
        val vanityName = trimmed
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("steamcommunity.com/id/")
            .removePrefix("steamcommunity.com/profiles/")
            .trim('/')

        if (vanityName.length == 17 && vanityName.all { it.isDigit() }) {
            return@withContext vanityName
        }

        val url = "https://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/" +
                "?key=$apiKey&vanityurl=$vanityName&format=json"

        try {
            val request = Request.Builder().url(url).build()
            val response = getClient().newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val body = response.body?.string() ?: return@withContext null
            val json = JSONObject(body)
            val resp = json.optJSONObject("response") ?: return@withContext null
            if (resp.optInt("success", 0) == 1) {
                resp.optString("steamid").takeIf { it.isNotBlank() }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to resolve Steam vanity URL '$vanityOrId': ${e.message}")
            null
        }
    }

    /**
     * Fetches all games owned on the given Steam account including total playtime.
     */
    suspend fun getOwnedGames(steamId: String, apiKey: String): List<SteamOwnedGame> = withContext(Dispatchers.IO) {
        val url = "https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/" +
                "?key=$apiKey&steamid=$steamId&include_appinfo=1&include_played_free_games=1&format=json"

        try {
            val request = Request.Builder().url(url).build()
            val response = getClient().newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val body = response.body?.string() ?: return@withContext emptyList()
            val json = JSONObject(body)
            val resp = json.optJSONObject("response") ?: return@withContext emptyList()
            val gamesArray = resp.optJSONArray("games") ?: return@withContext emptyList()

            val list = mutableListOf<SteamOwnedGame>()
            for (i in 0 until gamesArray.length()) {
                val g = gamesArray.optJSONObject(i) ?: continue
                val appId = g.optInt("appid")
                val name = g.optString("name", "")
                val playtime = g.optInt("playtime_forever", 0)
                val playtime2Weeks = g.optInt("playtime_2weeks", 0)
                val rtimeLastPlayed = g.optLong("rtime_last_played", 0L)

                if (appId > 0 && name.isNotBlank()) {
                    list.add(
                        SteamOwnedGame(
                            appId = appId,
                            name = name,
                            playtimeForeverMinutes = playtime,
                            playtime2WeeksMinutes = playtime2Weeks,
                            rtimeLastPlayed = rtimeLastPlayed
                        )
                    )
                }
            }
            list
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch owned Steam games for $steamId: ${e.message}")
            emptyList()
        }
    }

    /**
     * Fetches unlocked and total achievements for a specific Steam game.
     */
    suspend fun getPlayerAchievements(appId: Int, steamId: String, apiKey: String): SteamAchievementsResult? = withContext(Dispatchers.IO) {
        val url = "https://api.steampowered.com/ISteamUserStats/GetPlayerAchievements/v0001/" +
                "?appid=$appId&key=$apiKey&steamid=$steamId"

        try {
            val request = Request.Builder().url(url).build()
            val response = getClient().newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val body = response.body?.string() ?: return@withContext null
            val json = JSONObject(body)
            val playerStats = json.optJSONObject("playerstats") ?: return@withContext null
            if (!playerStats.optBoolean("success", true)) return@withContext null

            val achievementsArray = playerStats.optJSONArray("achievements") ?: return@withContext null
            var unlocked = 0
            val total = achievementsArray.length()
            for (i in 0 until total) {
                val a = achievementsArray.optJSONObject(i) ?: continue
                if (a.optInt("achieved", 0) == 1) {
                    unlocked++
                }
            }

            SteamAchievementsResult(
                unlockedCount = unlocked,
                totalCount = total,
                isComplete = total > 0 && unlocked >= total
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extracts Steam App ID from a store or giveaway URL.
     */
    fun extractSteamAppId(url: String): Int? {
        val regex = Regex("""store\.steampowered\.com/app/(\d+)""")
        return regex.find(url)?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    /**
     * Matches a LootDrop giveaway item against a user's Steam library.
     */
    fun matchSteamGame(item: GiveawayItem, steamGames: List<SteamOwnedGame>): SteamOwnedGame? {
        // 1. Check direct URL match
        val appIdFromUrl = extractSteamAppId(item.openGiveawayUrl)
        if (appIdFromUrl != null) {
            val directMatch = steamGames.find { it.appId == appIdFromUrl }
            if (directMatch != null) return directMatch
        }

        // 2. Clean normalized title matching
        val targetClean = MetacriticCatalog.cleanTitle(item.title).lowercase(Locale.ROOT)
        return steamGames.find { steamGame ->
            val steamClean = MetacriticCatalog.cleanTitle(steamGame.name).lowercase(Locale.ROOT)
            steamClean == targetClean ||
                    (targetClean.length > 5 && steamClean.contains(targetClean)) ||
                    (steamClean.length > 5 && targetClean.contains(steamClean))
        }
    }

    /**
     * Classifies a game into its playStatus based on playtime & achievements:
     * - NOT_STARTED (Backlog): 0 playtime
     * - PLAYING: > 0 playtime, game not yet completed
     * - BEATEN: 100% achievements or high completion threshold (>= 30h)
     */
    fun classifyPlayStatus(playtimeMinutes: Int, unlockedAchievements: Int, totalAchievements: Int): PlayStatus {
        if (playtimeMinutes <= 0) {
            return PlayStatus.NOT_STARTED
        }
        val isAllAchievements = totalAchievements > 0 && unlockedAchievements >= totalAchievements
        val isSubstantialCompletion = playtimeMinutes >= 1800 // 30+ hours
        return if (isAllAchievements || isSubstantialCompletion) {
            PlayStatus.BEATEN
        } else {
            PlayStatus.PLAYING
        }
    }
}
