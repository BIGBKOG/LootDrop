package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Handles one-tap deep links from achievements to:
 * - Steam Community Guides (App-specific guides & community hubs)
 * - YouTube Walkthrough & Video Search
 * - Web Search Guides (Steam Community / IGN / TrueAchievements)
 */
object AchievementWalkthroughHelper {

    private const val TAG = "WalkthroughHelper"

    private fun encode(text: String): String {
        return runCatching {
            URLEncoder.encode(text, StandardCharsets.UTF_8.toString())
        }.getOrDefault(text)
    }

    /**
     * Builds the Steam Community Guides section URL for this game.
     */
    fun buildSteamCommunityGuidesUrl(appId: Int, achievementTitle: String? = null): String {
        val query = if (!achievementTitle.isNullOrBlank()) "?searchText=${encode(achievementTitle)}" else ""
        return "https://steamcommunity.com/app/$appId/guides/$query"
    }

    /**
     * Builds the YouTube walkthrough search URL for an achievement.
     */
    fun buildYouTubeWalkthroughUrl(gameTitle: String, achievementTitle: String): String {
        val cleanGame = MetacriticCatalog.cleanTitle(gameTitle)
        val searchQuery = "$cleanGame $achievementTitle achievement guide walkthrough"
        return "https://www.youtube.com/results?search_query=${encode(searchQuery)}"
    }

    /**
     * Builds the Google web search URL for dedicated trophy guides.
     */
    fun buildWebGuideUrl(gameTitle: String, achievementTitle: String): String {
        val cleanGame = MetacriticCatalog.cleanTitle(gameTitle)
        val searchQuery = "$cleanGame \"$achievementTitle\" achievement guide"
        return "https://www.google.com/search?q=${encode(searchQuery)}"
    }

    /**
     * Opens the Steam Community Guides section for this game, optionally filtered by achievement name.
     */
    fun openSteamCommunityGuide(context: Context, appId: Int, achievementTitle: String? = null) {
        val url = buildSteamCommunityGuidesUrl(appId, achievementTitle)
        launchBrowserIntent(context, url, "Opening Steam Community Guides...")
    }

    /**
     * Opens YouTube search for a targeted video walkthrough of this specific achievement.
     */
    fun openYouTubeWalkthrough(context: Context, gameTitle: String, achievementTitle: String) {
        val cleanGame = MetacriticCatalog.cleanTitle(gameTitle)
        val searchQuery = "$cleanGame $achievementTitle achievement guide walkthrough"
        val encodedQuery = encode(searchQuery)

        // Try native YouTube app uri first, fallback to web
        val appUri = Uri.parse("vnd.youtube.launch://results?search_query=$encodedQuery")
        val webUrl = buildYouTubeWalkthroughUrl(gameTitle, achievementTitle)

        try {
            val intent = Intent(Intent.ACTION_VIEW, appUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                return
            }
        } catch (e: Exception) {
            Log.d(TAG, "YouTube native app not found, falling back to browser: ${e.message}")
        }

        launchBrowserIntent(context, webUrl, "Searching YouTube for walkthrough...")
    }

    /**
     * Opens Google search for dedicated text/video trophy guides.
     */
    fun openWebGuide(context: Context, gameTitle: String, achievementTitle: String) {
        val webUrl = buildWebGuideUrl(gameTitle, achievementTitle)
        launchBrowserIntent(context, webUrl, "Searching web for achievement guide...")
    }

    /**
     * Opens the official Steam client or web store achievement page for this game.
     */
    fun openSteamGlobalAchievements(context: Context, appId: Int) {
        val webUrl = "https://steamcommunity.com/stats/$appId/achievements"
        launchBrowserIntent(context, webUrl, "Opening Steam global stats...")
    }

    private fun launchBrowserIntent(context: Context, url: String, toastMsg: String? = null) {
        runCatching {
            if (!toastMsg.isNullOrBlank()) {
                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }.onFailure { e ->
            Log.e(TAG, "Failed to launch intent for $url: ${e.message}")
        }
    }
}
