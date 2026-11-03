package com.example.data.remote

import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Result data from Spotify Web API album search.
 */
data class SpotifySoundtrackResult(
    val albumTitle: String,
    val composer: String,
    val spotifyAlbumUrl: String,
    val releaseYear: String?,
    val totalTracks: Int,
    val isOfficialScore: Boolean
)

/**
 * Spotify Web API Client
 *
 * Implements Spotify Client Credentials Flow to authenticate and query the
 * Spotify Web API for official game soundtracks and scores.
 *
 * Provides accurate validation to prevent false positives and fan covers.
 */
object SpotifyApiClient {

    private const val TAG = "SpotifyApiClient"
    private const val TOKEN_URL = "https://accounts.spotify.com/api/token"
    private const val SEARCH_URL = "https://api.spotify.com/v1/search"

    @Volatile
    private var httpClient: OkHttpClient? = null

    @Volatile
    private var cachedAccessToken: String? = null

    @Volatile
    private var tokenExpiryTimestamp: Long = 0L

    private fun getClient(): OkHttpClient {
        return httpClient ?: synchronized(this) {
            httpClient ?: OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build().also { httpClient = it }
        }
    }

    /**
     * Resolves Spotify Client ID from BuildConfig.
     */
    fun resolveClientId(): String? {
        return runCatching {
            val field = BuildConfig::class.java.getField("SPOTIFY_CLIENT_ID")
            (field.get(null) as? String)?.trim()?.takeIf {
                it.isNotBlank() && !it.contains("PLACEHOLDER", ignoreCase = true)
            }
        }.getOrNull()
    }

    /**
     * Resolves Spotify Client Secret from BuildConfig.
     */
    fun resolveClientSecret(): String? {
        return runCatching {
            val field = BuildConfig::class.java.getField("SPOTIFY_CLIENT_SECRET")
            (field.get(null) as? String)?.trim()?.takeIf {
                it.isNotBlank() && !it.contains("PLACEHOLDER", ignoreCase = true)
            }
        }.getOrNull()
    }

    /**
     * Obtains or reuses an active Spotify OAuth access token via Client Credentials.
     */
    @Synchronized
    fun getAccessToken(): String? {
        val now = System.currentTimeMillis()
        if (!cachedAccessToken.isNullOrBlank() && now < tokenExpiryTimestamp - 60_000L) {
            return cachedAccessToken
        }

        val clientId = resolveClientId() ?: return null
        val clientSecret = resolveClientSecret() ?: return null

        return try {
            val credentials = "$clientId:$clientSecret"
            val basicAuth = "Basic " + Base64.encodeToString(credentials.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)

            val requestBody = FormBody.Builder()
                .add("grant_type", "client_credentials")
                .build()

            val request = Request.Builder()
                .url(TOKEN_URL)
                .header("Authorization", basicAuth)
                .post(requestBody)
                .build()

            val response = getClient().newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w(TAG, "Spotify auth failed with code: ${response.code}")
                return null
            }

            val body = response.body?.string() ?: return null
            val json = JSONObject(body)
            val token = json.optString("access_token", "")
            val expiresIn = json.optInt("expires_in", 3600)

            if (token.isNotBlank()) {
                cachedAccessToken = token
                tokenExpiryTimestamp = now + (expiresIn * 1000L)
                token
            } else null
        } catch (e: Exception) {
            Log.w(TAG, "Error obtaining Spotify access token: ${e.message}")
            null
        }
    }

    /**
     * Searches Spotify Web API for an official soundtrack matching the game title.
     */
    fun searchSoundtrack(gameTitle: String): SpotifySoundtrackResult? {
        val token = getAccessToken() ?: return null
        val cleanGame = cleanTitle(gameTitle)
        if (cleanGame.isBlank()) return null

        val query = "$cleanGame soundtrack"
        val encodedQuery = try {
            URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        } catch (_: Exception) {
            query.replace(" ", "%20")
        }

        val url = "$SEARCH_URL?q=$encodedQuery&type=album&limit=5"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/json")
            .build()

        return try {
            val response = getClient().newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w(TAG, "Spotify search failed: ${response.code}")
                return null
            }

            val body = response.body?.string() ?: return null
            parseAndVerifySpotifyResponse(body, cleanGame)
        } catch (e: Exception) {
            Log.w(TAG, "Spotify search error for '$gameTitle': ${e.message}")
            null
        }
    }

    /**
     * Parses Spotify JSON and validates authentic official soundtrack results.
     */
    fun parseAndVerifySpotifyResponse(jsonStr: String, cleanGameTitle: String): SpotifySoundtrackResult? {
        return try {
            val root = JSONObject(jsonStr)
            val albumsObj = root.optJSONObject("albums") ?: return null
            val items = albumsObj.optJSONArray("items") ?: return null

            for (i in 0 until items.length()) {
                val album = items.optJSONObject(i) ?: continue
                val name = album.optString("name", "").trim()
                val totalTracks = album.optInt("total_tracks", 0)
                val releaseDate = album.optString("release_date", "")
                val releaseYear = releaseDate.take(4).takeIf { it.length == 4 }

                val externalUrls = album.optJSONObject("external_urls")
                val spotifyUrl = externalUrls?.optString("spotify", "") ?: ""

                val artists = mutableListOf<String>()
                val artistsArr = album.optJSONArray("artists")
                if (artistsArr != null) {
                    for (j in 0 until artistsArr.length()) {
                        val a = artistsArr.optJSONObject(j)
                        val aName = a?.optString("name", "")
                        if (!aName.isNullOrBlank()) artists.add(aName)
                    }
                }

                if (isAccurateSpotifyMatch(name, cleanGameTitle, artists)) {
                    val composerStr = artists.joinToString(", ").takeIf { it.isNotBlank() } ?: "Original Game Soundtrack"
                    return SpotifySoundtrackResult(
                        albumTitle = name,
                        composer = composerStr,
                        spotifyAlbumUrl = spotifyUrl,
                        releaseYear = releaseYear,
                        totalTracks = totalTracks,
                        isOfficialScore = true
                    )
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Failed parsing Spotify response: ${e.message}")
            null
        }
    }

    /**
     * Strict verification rule: ensures Spotify album is genuinely an official game soundtrack.
     */
    fun isAccurateSpotifyMatch(albumName: String, cleanGameTitle: String, artists: List<String>): Boolean {
        val albumLower = albumName.lowercase(Locale.ROOT)
        val gameLower = cleanGameTitle.lowercase(Locale.ROOT).trim()

        // 1. Exclude cover/tribute artists and albums
        val excludedKeywords = listOf("tribute to", "cover", "8-bit", "lo-fi", "lofi", "remix", "metal version", "chiptune", "synthwave")
        if (excludedKeywords.any { albumLower.contains(it) }) {
            return false
        }

        // 2. Must contain game name
        val hasGameName = albumLower.contains(gameLower) || gameLower.split(" ").filter { it.length > 3 }.all { albumLower.contains(it) }
        if (!hasGameName) return false

        // 3. Must be an official soundtrack indicator
        val hasOstWords = albumLower.contains("soundtrack") ||
                albumLower.contains("original score") ||
                albumLower.contains("video game soundtrack") ||
                albumLower.contains("ost") ||
                albumLower.contains("original game score") ||
                albumLower.contains("music from")

        return hasOstWords
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
