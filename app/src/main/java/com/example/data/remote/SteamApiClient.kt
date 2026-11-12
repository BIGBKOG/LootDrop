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
    val rtimeLastPlayed: Long = 0L,
    val imgIconUrl: String = ""
)

sealed class SteamOwnedGamesResult {
    data class Success(val games: List<SteamOwnedGame>) : SteamOwnedGamesResult()
    object PrivacyRestricted : SteamOwnedGamesResult()
    data class Error(val message: String) : SteamOwnedGamesResult()
}

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

    // Default key fallback
    // Pre-configured Steam API Key fallback
    const val DEFAULT_API_KEY = "22F46E31080D84AE934E0D1A142E6209"

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
     * then BuildConfig, then the pre-configured default key.
     */
    fun resolveApiKey(userKey: String?): String {
        if (!userKey.isNullOrBlank()) return userKey.trim()
        val buildKey = runCatching {
            val field = BuildConfig::class.java.getField("STEAM_API_KEY")
            field.get(null) as? String
        }.getOrNull()
        if (!buildKey.isNullOrBlank() && buildKey != "YOUR_STEAM_API_KEY_HERE" && !buildKey.contains("PLACEHOLDER")) {
            return buildKey.trim()
        }
        return DEFAULT_API_KEY
    }

    /**
     * Resolves a vanity profile name (e.g. "gabelogannewell") to a 64-bit Steam ID.
     * If the input is already a 17-digit numeric Steam ID, returns it immediately.
     * Supports Steam Web API ResolveVanityURL as well as direct XML profile parsing fallback.
     */
    suspend fun resolveSteamId(vanityOrId: String, apiKey: String): String? = withContext(Dispatchers.IO) {
        val trimmed = vanityOrId.trim()
        if (trimmed.isBlank()) return@withContext null

        // 1. Direct 17-digit numeric SteamID (e.g. 76561198000000000)
        val idRegex = Regex("""\b(7656119\d{10}|\d{17})\b""")
        val directMatch = idRegex.find(trimmed)
        if (directMatch != null) {
            return@withContext directMatch.value
        }

        // 2. Clean URLs (e.g. https://steamcommunity.com/id/gaben/ -> gaben)
        var vanity = trimmed
            .substringBefore("?")
            .substringBefore("#")
            .trim()

        if (vanity.contains("/id/")) {
            vanity = vanity.substringAfter("/id/").trim('/')
            vanity = vanity.substringBefore('/')
        } else if (vanity.contains("/profiles/")) {
            vanity = vanity.substringAfter("/profiles/").trim('/')
            vanity = vanity.substringBefore('/')
        } else {
            vanity = vanity
                .removePrefix("https://")
                .removePrefix("http://")
                .removePrefix("www.")
                .removePrefix("steamcommunity.com/")
                .trim('/')
        }

        if (vanity.length == 17 && vanity.all { it.isDigit() }) {
            return@withContext vanity
        }

        val effectiveKey = resolveApiKey(apiKey)

        // 3. Steam Web API ResolveVanityURL
        if (effectiveKey.isNotBlank()) {
            val url = "https://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/" +
                    "?key=$effectiveKey&vanityurl=$vanity&format=json"

            try {
                val request = Request.Builder().url(url).build()
                val response = getClient().newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val json = JSONObject(body)
                    val resp = json.optJSONObject("response")
                    if (resp?.optInt("success", 0) == 1) {
                        val steamId = resp.optString("steamid").takeIf { it.isNotBlank() }
                        if (steamId != null) return@withContext steamId
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Steam Web API ResolveVanityURL failed for '$vanity': ${e.message}")
            }
        }

        // 4. Fallback 1: Parse public Steam community profile XML (steamcommunity.com/id/{vanity}/?xml=1)
        try {
            val xmlUrl = "https://steamcommunity.com/id/$vanity/?xml=1"
            val req = Request.Builder()
                .url(xmlUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()
            val resp = getClient().newCall(req).execute()
            if (resp.isSuccessful) {
                val body = resp.body?.string() ?: ""
                val idMatch = Regex("""<steamID64>(\d{17})</steamID64>""").find(body)
                val resolvedId = idMatch?.groupValues?.getOrNull(1)
                if (!resolvedId.isNullOrBlank()) {
                    return@withContext resolvedId
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Steam community XML resolve failed for '$vanity': ${e.message}")
        }

        // 5. Fallback 2: Parse profiles XML (steamcommunity.com/profiles/{vanity}/?xml=1)
        try {
            val xmlUrl = "https://steamcommunity.com/profiles/$vanity/?xml=1"
            val req = Request.Builder()
                .url(xmlUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()
            val resp = getClient().newCall(req).execute()
            if (resp.isSuccessful) {
                val body = resp.body?.string() ?: ""
                val idMatch = Regex("""<steamID64>(\d{17})</steamID64>""").find(body)
                val resolvedId = idMatch?.groupValues?.getOrNull(1)
                if (!resolvedId.isNullOrBlank()) {
                    return@withContext resolvedId
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Steam community XML profiles resolve failed for '$vanity': ${e.message}")
        }

        null
    }

    /**
     * Fetches owned games with explicit privacy status reporting.
     */
    suspend fun getOwnedGamesWithStatus(steamId: String, apiKey: String): SteamOwnedGamesResult = withContext(Dispatchers.IO) {
        val effectiveKey = resolveApiKey(apiKey)
        val url = "https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/" +
                "?key=$effectiveKey&steamid=$steamId&include_appinfo=1&include_played_free_games=1&format=json"

        try {
            val request = Request.Builder().url(url).build()
            val response = getClient().newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext SteamOwnedGamesResult.Error("HTTP ${response.code}: ${response.message}")
            }

            val body = response.body?.string() ?: return@withContext SteamOwnedGamesResult.Error("Empty response body")
            val json = JSONObject(body)
            val resp = json.optJSONObject("response") ?: return@withContext SteamOwnedGamesResult.PrivacyRestricted

            // Check if games or game_count was omitted by Valve due to privacy restrictions
            if (!resp.has("games") && !resp.has("game_count")) {
                return@withContext SteamOwnedGamesResult.PrivacyRestricted
            }

            val gamesArray = resp.optJSONArray("games")
            if (gamesArray == null) {
                return@withContext SteamOwnedGamesResult.Success(emptyList())
            }

            val list = mutableListOf<SteamOwnedGame>()
            for (i in 0 until gamesArray.length()) {
                val g = gamesArray.optJSONObject(i) ?: continue
                val appId = g.optInt("appid")
                val name = g.optString("name", "")
                val playtime = g.optInt("playtime_forever", 0)
                val playtime2Weeks = g.optInt("playtime_2weeks", 0)
                val rtimeLastPlayed = g.optLong("rtime_last_played", 0L)
                val imgIconUrl = g.optString("img_icon_url", "")

                if (appId > 0 && name.isNotBlank()) {
                    list.add(
                        SteamOwnedGame(
                            appId = appId,
                            name = name,
                            playtimeForeverMinutes = playtime,
                            playtime2WeeksMinutes = playtime2Weeks,
                            rtimeLastPlayed = rtimeLastPlayed,
                            imgIconUrl = imgIconUrl
                        )
                    )
                }
            }
            SteamOwnedGamesResult.Success(list)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch owned Steam games for $steamId: ${e.message}")
            SteamOwnedGamesResult.Error(e.message ?: "Network error connecting to Steam")
        }
    }

    /**
     * Fetches all games owned on the given Steam account including total playtime.
     */
    suspend fun getOwnedGames(steamId: String, apiKey: String): List<SteamOwnedGame> {
        return when (val res = getOwnedGamesWithStatus(steamId, apiKey)) {
            is SteamOwnedGamesResult.Success -> res.games
            else -> emptyList()
        }
    }

    /**
     * Fetches unlocked and total achievements for a specific Steam game.
     */
    suspend fun getPlayerAchievements(appId: Int, steamId: String, apiKey: String): SteamAchievementsResult? = withContext(Dispatchers.IO) {
        val effectiveKey = resolveApiKey(apiKey)
        val url = "https://api.steampowered.com/ISteamUserStats/GetPlayerAchievements/v0001/" +
                "?appid=$appId&key=$effectiveKey&steamid=$steamId"

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
     * Valve's official API to fetch global achievement unlock percentages for a game:
     * ISteamUserStats/GetGlobalAchievementPercentagesForApp/v0002/
     *
     * Returns a map of achievement apiName to its global unlock percentage (0.0% to 100.0%).
     */
    suspend fun getGlobalAchievementPercentagesForApp(appId: Int): Map<String, Float> = withContext(Dispatchers.IO) {
        val url = "https://api.steampowered.com/ISteamUserStats/GetGlobalAchievementPercentagesForApp/v0002/?gameid=$appId&format=json"
        val resultMap = mutableMapOf<String, Float>()

        try {
            val request = Request.Builder().url(url).build()
            val response = getClient().newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyMap()

            val body = response.body?.string() ?: return@withContext emptyMap()
            val json = JSONObject(body)
            val percentagesObj = json.optJSONObject("achievementpercentages") ?: return@withContext emptyMap()
            val achievementsArray = percentagesObj.optJSONArray("achievements") ?: return@withContext emptyMap()

            for (i in 0 until achievementsArray.length()) {
                val ach = achievementsArray.optJSONObject(i) ?: continue
                val name = ach.optString("name", "")
                val percent = ach.optDouble("percent", 0.0).toFloat()
                if (name.isNotBlank()) {
                    resultMap[name] = percent
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch global achievement percentages for appId $appId: ${e.message}")
        }
        resultMap
    }

    /**
     * Fetches detailed player achievements for a game, merging localized titles,
     * descriptions, unlock times, and global rarity percentages.
     */
    suspend fun getDetailedPlayerAchievements(
        appId: Int,
        steamId: String,
        apiKey: String,
        gameName: String = ""
    ): List<com.example.data.local.SteamAchievementEntity> = withContext(Dispatchers.IO) {
        val effectiveKey = resolveApiKey(apiKey)
        val url = "https://api.steampowered.com/ISteamUserStats/GetPlayerAchievements/v0001/" +
                "?appid=$appId&key=$effectiveKey&steamid=$steamId&l=en"

        try {
            val request = Request.Builder().url(url).build()
            val response = getClient().newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val body = response.body?.string() ?: return@withContext emptyList()
            val json = JSONObject(body)
            val playerStats = json.optJSONObject("playerstats") ?: return@withContext emptyList()
            if (!playerStats.optBoolean("success", true)) return@withContext emptyList()

            val resolvedGameName = playerStats.optString("gameName", gameName).ifBlank { gameName }
            val achievementsArray = playerStats.optJSONArray("achievements") ?: return@withContext emptyList()

            // Fetch global rarity percentages via GetGlobalAchievementPercentagesForApp
            val globalPercentages = getGlobalAchievementPercentagesForApp(appId)

            val list = mutableListOf<com.example.data.local.SteamAchievementEntity>()
            for (i in 0 until achievementsArray.length()) {
                val ach = achievementsArray.optJSONObject(i) ?: continue
                val apiName = ach.optString("apiname", "")
                if (apiName.isBlank()) continue

                val isAchieved = ach.optInt("achieved", 0) == 1
                val unlockTime = ach.optLong("unlocktime", 0L)
                val displayName = ach.optString("name", apiName).ifBlank { apiName }
                val description = ach.optString("description", "")
                val globalPercent = globalPercentages[apiName] ?: 0f

                list.add(
                    com.example.data.local.SteamAchievementEntity(
                        appId = appId,
                        apiName = apiName,
                        displayName = displayName,
                        description = description,
                        isAchieved = isAchieved,
                        unlockTimestamp = unlockTime,
                        globalPercentage = globalPercent,
                        gameName = resolvedGameName
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch detailed player achievements for appId $appId: ${e.message}")
            emptyList()
        }
    }

    /**
     * Extracts Steam App ID from a store, community, or steam:// URL.
     */
    fun extractSteamAppId(url: String): Int? {
        val regex = Regex("""(?:store\.steampowered\.com/app/|steamcommunity\.com/app/|steam://run/)(\d+)""")
        return regex.find(url)?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    fun normalizeGameTitle(title: String): String {
        return title
            .replace(Regex("[™®©]"), "")
            .replace(Regex("\\([^)]*\\)"), "")
            .replace(Regex("\\[[^\\]]*\\]"), "")
            .replace(
                Regex("(?i)\\b(giveaway|free|dlc|pack|key|keys|bundle|edition|deluxe|definitive|remastered|goty|starter|loot|bonus|steam|epic games|gog)\\b"),
                ""
            )
            .replace(Regex("[^a-zA-Z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .lowercase(Locale.ROOT)
    }

    /**
     * Matches a LootDrop giveaway item against a user's Steam library with multi-stage verification.
     */
    fun matchSteamGame(item: GiveawayItem, steamGames: List<SteamOwnedGame>): SteamOwnedGame? {
        // 1. Direct Steam App ID match if already known on the item
        if (item.steamAppId != null && item.steamAppId > 0) {
            val directMatch = steamGames.find { it.appId == item.steamAppId }
            if (directMatch != null) return directMatch
        }

        // 2. Direct App ID extracted from openGiveawayUrl
        val appIdFromUrl = extractSteamAppId(item.openGiveawayUrl)
        if (appIdFromUrl != null) {
            val directMatch = steamGames.find { it.appId == appIdFromUrl }
            if (directMatch != null) return directMatch
        }

        // 3. Direct App ID from any launcher options
        for (launcher in item.availableLaunchers) {
            val launcherAppId = extractSteamAppId(launcher.url)
            if (launcherAppId != null) {
                val directMatch = steamGames.find { it.appId == launcherAppId }
                if (directMatch != null) return directMatch
            }
        }

        // 4. Exact normalized title matching
        val targetNorm = normalizeGameTitle(item.title)
        if (targetNorm.length >= 3) {
            val exactNormMatch = steamGames.find { normalizeGameTitle(it.name) == targetNorm }
            if (exactNormMatch != null) return exactNormMatch
        }

        // 5. Clean MetacriticCatalog matching
        val targetClean = MetacriticCatalog.cleanTitle(item.title).lowercase(Locale.ROOT)
        return steamGames.find { steamGame ->
            val steamClean = MetacriticCatalog.cleanTitle(steamGame.name).lowercase(Locale.ROOT)
            steamClean == targetClean ||
                    (targetClean.length > 5 && steamClean.contains(targetClean)) ||
                    (steamClean.length > 5 && targetClean.contains(steamClean))
        }
    }

    /**
     * Fetches the user's Steam Wishlist App IDs and game titles.
     * Uses Steam Web API IWishlistService with fallback to store wishlist_data JSON.
     */
    suspend fun getWishlist(steamId: String, apiKey: String): Map<Int, String> = withContext(Dispatchers.IO) {
        val resultMap = mutableMapOf<Int, String>()
        val effectiveKey = resolveApiKey(apiKey)

        // 1. Try store endpoint first to get both appId and game names
        try {
            val storeUrl = "https://store.steampowered.com/wishlist/profiles/$steamId/wishlist_data/"
            val request = Request.Builder().url(storeUrl).build()
            val response = getClient().newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank() && body.trim().startsWith("{")) {
                    val json = JSONObject(body)
                    val keys = json.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val appId = key.toIntOrNull() ?: continue
                        val itemObj = json.optJSONObject(key)
                        val name = itemObj?.optString("name", "") ?: ""
                        resultMap[appId] = name
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Store wishlist endpoint unavailable, falling back: ${e.message}")
        }

        // 2. Fallback to IWishlistService/GetWishlist/v1 if store returned empty
        if (resultMap.isEmpty() && effectiveKey.isNotBlank()) {
            try {
                val serviceUrl = "https://api.steampowered.com/IWishlistService/GetWishlist/v1/?key=$effectiveKey&steamid=$steamId"
                val request = Request.Builder().url(serviceUrl).build()
                val response = getClient().newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val json = JSONObject(body)
                        val resp = json.optJSONObject("response")
                        val items = resp?.optJSONArray("items")
                        if (items != null) {
                            for (i in 0 until items.length()) {
                                val item = items.optJSONObject(i) ?: continue
                                val appId = item.optInt("appid", 0)
                                if (appId > 0) {
                                    resultMap[appId] = ""
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "IWishlistService request failed: ${e.message}")
            }
        }

        resultMap
    }

    /**
     * Checks if a giveaway item is on the user's Steam wishlist by App ID or title.
     */
    fun isItemOnWishlist(item: GiveawayItem, wishlistMap: Map<Int, String>): Boolean {
        if (wishlistMap.isEmpty()) return false

        // 1. Check Steam App ID from URL
        val appIdFromUrl = extractSteamAppId(item.openGiveawayUrl)
        if (appIdFromUrl != null && wishlistMap.containsKey(appIdFromUrl)) {
            return true
        }

        // 2. Check title matching against wishlist names
        val targetClean = MetacriticCatalog.cleanTitle(item.title).lowercase(Locale.ROOT)
        for ((_, name) in wishlistMap) {
            if (name.isBlank()) continue
            val wishClean = MetacriticCatalog.cleanTitle(name).lowercase(Locale.ROOT)
            if (wishClean == targetClean ||
                (targetClean.length > 5 && wishClean.contains(targetClean)) ||
                (wishClean.length > 5 && targetClean.contains(wishClean))
            ) {
                return true
            }
        }

        return false
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
