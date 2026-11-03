package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Result data from Discogs API database search.
 */
data class DiscogsSoundtrackResult(
    val title: String,
    val albumTitle: String,
    val composer: String,
    val year: String?,
    val discogsUrl: String,
    val style: List<String>,
    val genres: List<String>,
    val isVerifiedVideoGameScore: Boolean
)

/**
 * Discogs Web API Client
 *
 * Interacts with Discogs Database Search API to find authenticated official video game
 * soundtrack releases and vinyl/digital scores.
 *
 * Uses Discogs API token from BuildConfig / Secrets panel when provided.
 * Implements strict video game soundtrack verification to prevent false positives.
 */
object DiscogsApiClient {

    private const val TAG = "DiscogsApiClient"
    private const val BASE_URL = "https://api.discogs.com/database/search"
    private const val USER_AGENT = "LootDrop-Android/1.0 (contact@lootdrop.com)"

    @Volatile
    private var httpClient: OkHttpClient? = null

    private fun getClient(): OkHttpClient {
        return httpClient ?: synchronized(this) {
            httpClient ?: OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build().also { httpClient = it }
        }
    }

    /**
     * Resolves Discogs Personal Access Token from BuildConfig.
     */
    fun resolveToken(): String? {
        return runCatching {
            val field = BuildConfig::class.java.getField("DISCOGS_API_TOKEN")
            (field.get(null) as? String)?.trim()?.takeIf {
                it.isNotBlank() && !it.contains("PLACEHOLDER", ignoreCase = true)
            }
        }.getOrNull()
    }

    /**
     * Searches Discogs for an official video game soundtrack matching the game title.
     * Returns null if no authentic official soundtrack exists for this title.
     */
    fun searchSoundtrack(gameTitle: String): DiscogsSoundtrackResult? {
        val cleanGame = cleanTitle(gameTitle)
        if (cleanGame.isBlank()) return null

        val query = "$cleanGame soundtrack"
        val encodedQuery = try {
            URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        } catch (_: Exception) {
            query.replace(" ", "%20")
        }

        val urlBuilder = StringBuilder(BASE_URL)
            .append("?q=").append(encodedQuery)
            .append("&type=release")
            .append("&per_page=5")

        val token = resolveToken()
        val requestBuilder = Request.Builder()
            .url(urlBuilder.toString())
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json")

        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Discogs token=$token")
        }

        return try {
            val response = getClient().newCall(requestBuilder.build()).execute()
            if (!response.isSuccessful) {
                Log.w(TAG, "Discogs API error: ${response.code}")
                return null
            }

            val body = response.body?.string() ?: return null
            parseAndVerifyDiscogsResponse(body, cleanGame)
        } catch (e: Exception) {
            Log.w(TAG, "Discogs search failed for '$gameTitle': ${e.message}")
            null
        }
    }

    /**
     * Parses Discogs JSON and strictly validates whether any result is an authentic
     * official soundtrack for the specified game.
     */
    fun parseAndVerifyDiscogsResponse(jsonStr: String, cleanGameTitle: String): DiscogsSoundtrackResult? {
        return try {
            val root = JSONObject(jsonStr)
            val results = root.optJSONArray("results") ?: return null

            for (i in 0 until results.length()) {
                val item = results.optJSONObject(i) ?: continue
                val rawTitle = item.optString("title", "").trim()
                val year = item.optString("year", "").takeIf { it.isNotBlank() }
                val uri = item.optString("uri", "")

                val styles = mutableListOf<String>()
                val styleArr = item.optJSONArray("style")
                if (styleArr != null) {
                    for (j in 0 until styleArr.length()) {
                        styles.add(styleArr.optString(j, ""))
                    }
                }

                val genres = mutableListOf<String>()
                val genreArr = item.optJSONArray("genre")
                if (genreArr != null) {
                    for (j in 0 until genreArr.length()) {
                        genres.add(genreArr.optString(j, ""))
                    }
                }

                if (isAccurateMatch(rawTitle, styles, genres, cleanGameTitle)) {
                    // Discogs titles are standardly formatted: "Artist - Album Title"
                    val (composer, album) = if (rawTitle.contains(" - ")) {
                        val parts = rawTitle.split(" - ", limit = 2)
                        parts[0].trim() to parts[1].trim()
                    } else {
                        "Original Game Soundtrack" to rawTitle
                    }

                    val discogsUrl = if (uri.isNotBlank()) {
                        if (uri.startsWith("http")) uri else "https://www.discogs.com$uri"
                    } else {
                        "https://www.discogs.com/search/?q=${cleanGameTitle.replace(" ", "+")}+soundtrack"
                    }

                    return DiscogsSoundtrackResult(
                        title = rawTitle,
                        albumTitle = album,
                        composer = composer,
                        year = year,
                        discogsUrl = discogsUrl,
                        style = styles,
                        genres = genres,
                        isVerifiedVideoGameScore = styles.any { it.equals("Video Game Music", ignoreCase = true) }
                    )
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Failed parsing Discogs response: ${e.message}")
            null
        }
    }

    /**
     * Strict verification rule: ensures release is genuinely an official game soundtrack.
     */
    fun isAccurateMatch(
        releaseTitle: String,
        styles: List<String>,
        genres: List<String>,
        cleanGameTitle: String
    ): Boolean {
        val titleLower = releaseTitle.lowercase(Locale.ROOT)
        val gameLower = cleanGameTitle.lowercase(Locale.ROOT).trim()

        // 1. Exclude user covers, tributes, karaoke, fan remixes
        val invalidKeywords = listOf("tribute to", "cover version", "8-bit", "lo-fi", "lofi", "remixes", "karaoke", "performed by students")
        if (invalidKeywords.any { titleLower.contains(it) }) {
            return false
        }

        // 2. Title must contain game name
        val hasGameName = titleLower.contains(gameLower) || gameLower.split(" ").filter { it.length > 3 }.all { titleLower.contains(it) }
        if (!hasGameName) return false

        // 3. Must be soundtrack or video game score
        val isVideoGameStyle = styles.any {
            it.equals("Video Game Music", ignoreCase = true) ||
            it.equals("Soundtrack", ignoreCase = true) ||
            it.equals("Score", ignoreCase = true)
        }
        val isScreenGenre = genres.any {
            it.equals("Stage & Screen", ignoreCase = true)
        }
        val hasOstWordsInTitle = titleLower.contains("soundtrack") ||
                titleLower.contains("original score") ||
                titleLower.contains("ost") ||
                titleLower.contains("game music")

        return isVideoGameStyle || (isScreenGenre && hasOstWordsInTitle) || hasOstWordsInTitle
    }

    private fun cleanTitle(title: String): String {
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
}
