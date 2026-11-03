package com.example.data.remote

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Result data from MusicBrainz open video game soundtrack database.
 */
data class MusicBrainzSoundtrackResult(
    val albumTitle: String,
    val composer: String,
    val releaseYear: String?,
    val musicBrainzId: String
)

/**
 * Open MusicBrainz Video Game Music API Client
 *
 * Provides open, community-verified video game soundtrack database lookups
 * without requiring third-party API keys.
 */
object MusicBrainzApiClient {

    private const val TAG = "MusicBrainzApiClient"
    private const val BASE_URL = "https://musicbrainz.org/ws/2/release/"
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
     * Searches MusicBrainz for an official soundtrack release matching the game title.
     */
    fun searchSoundtrack(gameTitle: String): MusicBrainzSoundtrackResult? {
        val cleanGame = cleanTitle(gameTitle)
        if (cleanGame.isBlank()) return null

        val query = "release:\"$cleanGame\" AND primarytype:\"Soundtrack\""
        val encodedQuery = try {
            URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        } catch (_: Exception) {
            query.replace(" ", "%20")
        }

        val url = "$BASE_URL?query=$encodedQuery&fmt=json&limit=5"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json")
            .build()

        return try {
            val response = getClient().newCall(request).execute()
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            parseAndVerifyMusicBrainzResponse(body, cleanGame)
        } catch (e: Exception) {
            Log.w(TAG, "MusicBrainz lookup failed: ${e.message}")
            null
        }
    }

    fun parseAndVerifyMusicBrainzResponse(jsonStr: String, cleanGameTitle: String): MusicBrainzSoundtrackResult? {
        return try {
            val root = JSONObject(jsonStr)
            val releases = root.optJSONArray("releases") ?: return null

            for (i in 0 until releases.length()) {
                val rel = releases.optJSONObject(i) ?: continue
                val title = rel.optString("title", "").trim()
                val id = rel.optString("id", "")
                val date = rel.optString("date", "")
                val year = date.take(4).takeIf { it.length == 4 }

                val artistCredit = rel.optJSONArray("artist-credit")
                val artists = mutableListOf<String>()
                if (artistCredit != null) {
                    for (j in 0 until artistCredit.length()) {
                        val ac = artistCredit.optJSONObject(j)
                        val name = ac?.optString("name", "")
                        if (!name.isNullOrBlank()) artists.add(name)
                    }
                }

                if (isAccurateMatch(title, cleanGameTitle)) {
                    val composerStr = artists.joinToString(", ").takeIf { it.isNotBlank() } ?: "Original Game Soundtrack"
                    return MusicBrainzSoundtrackResult(
                        albumTitle = title,
                        composer = composerStr,
                        releaseYear = year,
                        musicBrainzId = id
                    )
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Failed parsing MusicBrainz response: ${e.message}")
            null
        }
    }

    fun isAccurateMatch(releaseTitle: String, cleanGameTitle: String): Boolean {
        val titleLower = releaseTitle.lowercase(Locale.ROOT)
        val gameLower = cleanGameTitle.lowercase(Locale.ROOT).trim()

        val excluded = listOf("tribute", "cover", "8-bit", "lofi", "remix")
        if (excluded.any { titleLower.contains(it) }) return false

        val hasGame = titleLower.contains(gameLower) || gameLower.split(" ").filter { it.length > 3 }.all { titleLower.contains(it) }
        val hasOst = titleLower.contains("soundtrack") || titleLower.contains("score") || titleLower.contains("ost")

        return hasGame && hasOst
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
