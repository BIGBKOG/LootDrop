package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AppUpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * Robust client to check for new releases on the project's public GitHub repository.
 * Features multi-tier fallback (Atom feed -> Web redirect / expanded assets -> GitHub REST API)
 * to bypass GitHub API rate limits (HTTP 403), correctly handles the V4.5 -> V1.5.0 re-versioning,
 * and parses 1.5.X wildcard / patch releases with direct APK asset links.
 */
class GithubReleaseClient(private val context: Context) {

    // Dedicated clean OkHttpClient instance (independent of GamerPower caching and interceptors)
    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    suspend fun checkLatestRelease(
        owner: String = DEFAULT_OWNER,
        repo: String = DEFAULT_REPO,
        currentVersionName: String = BuildConfig.VERSION_NAME
    ): Result<AppUpdateInfo> = withContext(Dispatchers.IO) {
        var lastException: Exception? = null

        // Tier 1: Check GitHub Releases Atom Feed (Unmetered, zero rate limits, full timeline)
        try {
            val atomInfo = checkReleasesAtomFeed(owner, repo, currentVersionName)
            if (atomInfo != null) {
                return@withContext Result.success(atomInfo)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Atom feed check failed, attempting REST API fallback: ${e.message}")
            lastException = e
        }

        // Tier 2: Check GitHub REST API (if not rate-limited)
        try {
            val apiInfo = checkReleasesRestApi(owner, repo, currentVersionName)
            if (apiInfo != null) {
                return@withContext Result.success(apiInfo)
            }
        } catch (e: Exception) {
            Log.w(TAG, "REST API check failed (possibly rate limited 403), attempting Web fallback: ${e.message}")
            lastException = e
        }

        // Tier 3: Check Web Redirect & Expanded Assets
        try {
            val webInfo = checkReleasesWebFallback(owner, repo, currentVersionName)
            if (webInfo != null) {
                return@withContext Result.success(webInfo)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Web fallback check failed: ${e.message}", e)
            lastException = e
        }

        Result.failure(lastException ?: Exception("Could not fetch release info from GitHub"))
    }

    /**
     * Tier 1: Parses GitHub releases.atom feed.
     * Never rate limited, always contains chronological release entries.
     */
    private fun checkReleasesAtomFeed(
        owner: String,
        repo: String,
        currentVersionName: String
    ): AppUpdateInfo? {
        val url = "https://github.com/$owner/$repo/releases.atom"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "LootDrop-Android/$currentVersionName")
            .header("Accept", "application/atom+xml, text/xml, */*")
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) return null
        val body = response.body?.string() ?: return null

        val entryRegex = Pattern.compile("<entry>([\\s\\S]*?)</entry>")
        val idRegex = Pattern.compile("<id>[^<]*/([^</]+)</id>")
        val titleRegex = Pattern.compile("<title>([^<]+)</title>")
        val updatedRegex = Pattern.compile("<updated>([^<]+)</updated>")
        val contentRegex = Pattern.compile("<content[^>]*>([\\s\\S]*?)</content>")

        val matcher = entryRegex.matcher(body)
        var selectedTag: String? = null
        var selectedTitle: String? = null
        var selectedUpdated: String? = null
        var selectedNotes: String? = null

        val cleanCurrentVersion = currentVersionName.trim().removePrefix("v").removePrefix("V")
        val isCurrentModernStream = cleanCurrentVersion.startsWith("1.") || cleanCurrentVersion.startsWith("2.")

        while (matcher.find()) {
            val entryText = matcher.group(1) ?: continue
            val idMatch = idRegex.matcher(entryText)
            val rawTag = if (idMatch.find()) idMatch.group(1)?.trim() else null
            val tag = rawTag ?: continue

            val cleanTag = tag.removePrefix("v").removePrefix("V")
            // If current version is on modern stream (1.x / 2.x), filter out legacy 4.x and 3.x tags from before reset
            if (isCurrentModernStream && (cleanTag.startsWith("4.") || cleanTag.startsWith("3.") ||
                        tag.contains("V4.") || tag.contains("V3.") || tag.contains("Loot-Drop-V3."))) {
                continue
            }

            val titleMatch = titleRegex.matcher(entryText)
            val title = if (titleMatch.find()) titleMatch.group(1)?.trim() ?: "LootDrop $tag" else "LootDrop $tag"

            val updatedMatch = updatedRegex.matcher(entryText)
            val updated = if (updatedMatch.find()) updatedMatch.group(1)?.trim() else null

            val contentMatch = contentRegex.matcher(entryText)
            val notes = if (contentMatch.find()) {
                val rawHtml = contentMatch.group(1) ?: ""
                try {
                    android.text.Html.fromHtml(rawHtml, android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim()
                } catch (_: Exception) {
                    rawHtml.replace("<[^>]*>".toRegex(), " ").trim()
                }
            } else ""

            selectedTag = tag
            selectedTitle = title
            selectedUpdated = updated
            selectedNotes = notes
            break
        }

        // If no 1.x tag matched in loop, fallback to the very first tag in feed
        if (selectedTag == null) {
            val firstIdMatch = idRegex.matcher(body)
            if (firstIdMatch.find()) {
                selectedTag = firstIdMatch.group(1)?.trim()
                selectedTitle = "LootDrop $selectedTag"
            }
        }

        val finalTag = selectedTag ?: return null

        val hasUpdate = isNewerVersion(finalTag, currentVersionName)
        val apkUrl = resolveApkUrl(owner, repo, finalTag)
        val htmlUrl = "https://github.com/$owner/$repo/releases/tag/$finalTag"

        return AppUpdateInfo(
            hasUpdate = hasUpdate,
            latestVersion = finalTag,
            currentVersion = currentVersionName,
            releaseTitle = selectedTitle ?: "LootDrop $finalTag",
            releaseNotes = selectedNotes ?: "",
            releaseUrl = htmlUrl,
            downloadUrl = apkUrl ?: htmlUrl,
            publishedAt = selectedUpdated,
            isApkAvailable = apkUrl != null,
            checkedTimestamp = System.currentTimeMillis()
        )
    }

    /**
     * Tier 2: Checks GitHub REST API.
     */
    private fun checkReleasesRestApi(
        owner: String,
        repo: String,
        currentVersionName: String
    ): AppUpdateInfo? {
        val url = "https://api.github.com/repos/$owner/$repo/releases/latest"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "LootDrop-Android/$currentVersionName")
            .header("Accept", "application/vnd.github.v3+json")
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) return null

        val bodyString = response.body?.string() ?: return null
        val json = JSONObject(bodyString)
        val tagName = json.optString("tag_name", "").trim()
        if (tagName.isBlank()) return null

        val cleanTag = tagName.removePrefix("v").removePrefix("V")
        val isCurrentOnOneStream = currentVersionName.trim().removePrefix("v").removePrefix("V").startsWith("1.")
        if (isCurrentOnOneStream && (cleanTag.startsWith("4.") || cleanTag.startsWith("3."))) {
            // REST API returned legacy release; return null to allow atom feed to supply active 1.x release
            return null
        }

        val releaseName = json.optString("name", "").ifBlank { "LootDrop $tagName" }
        val releaseBody = json.optString("body", "").trim()
        val htmlUrl = json.optString("html_url", "https://github.com/$owner/$repo/releases/tag/$tagName")
        val publishedAt = json.optString("published_at", "")

        var downloadUrl: String? = null
        var isApk = false
        val assetsArray = json.optJSONArray("assets")
        if (assetsArray != null) {
            for (i in 0 until assetsArray.length()) {
                val asset = assetsArray.optJSONObject(i) ?: continue
                val assetName = asset.optString("name", "")
                if (assetName.endsWith(".apk", ignoreCase = true)) {
                    downloadUrl = asset.optString("browser_download_url")
                    isApk = true
                    break
                }
            }
        }

        if (downloadUrl == null) {
            downloadUrl = resolveApkUrl(owner, repo, tagName)
            if (downloadUrl != null) isApk = true
        }

        val hasUpdate = isNewerVersion(tagName, currentVersionName)
        return AppUpdateInfo(
            hasUpdate = hasUpdate,
            latestVersion = tagName,
            currentVersion = currentVersionName,
            releaseTitle = releaseName,
            releaseNotes = releaseBody,
            releaseUrl = htmlUrl,
            downloadUrl = downloadUrl ?: htmlUrl,
            publishedAt = publishedAt,
            isApkAvailable = isApk,
            checkedTimestamp = System.currentTimeMillis()
        )
    }

    /**
     * Tier 3: Follows GitHub web redirect (/releases/latest -> /releases/tag/{tag}).
     */
    private fun checkReleasesWebFallback(
        owner: String,
        repo: String,
        currentVersionName: String
    ): AppUpdateInfo? {
        val latestUrl = "https://github.com/$owner/$repo/releases/latest"
        val request = Request.Builder()
            .url(latestUrl)
            .header("User-Agent", "LootDrop-Android/$currentVersionName")
            .header("Accept", "text/html, */*")
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) return null

        val finalUrl = response.request.url.toString()
        val tag = if (finalUrl.contains("/releases/tag/")) {
            finalUrl.substringAfter("/releases/tag/").substringBefore("/").substringBefore("?")
        } else {
            return null
        }

        val hasUpdate = isNewerVersion(tag, currentVersionName)
        val apkUrl = resolveApkUrl(owner, repo, tag)
        val htmlUrl = "https://github.com/$owner/$repo/releases/tag/$tag"

        return AppUpdateInfo(
            hasUpdate = hasUpdate,
            latestVersion = tag,
            currentVersion = currentVersionName,
            releaseTitle = "LootDrop $tag",
            releaseNotes = "New LootDrop update released on GitHub ($tag)",
            releaseUrl = htmlUrl,
            downloadUrl = apkUrl ?: htmlUrl,
            publishedAt = null,
            isApkAvailable = apkUrl != null,
            checkedTimestamp = System.currentTimeMillis()
        )
    }

    /**
     * Resolves the direct download URL for the release APK by inspecting GitHub expanded_assets.
     */
    fun resolveApkUrl(owner: String, repo: String, tag: String): String? {
        try {
            val expandedUrl = "https://github.com/$owner/$repo/releases/expanded_assets/$tag"
            val request = Request.Builder()
                .url(expandedUrl)
                .header("User-Agent", "LootDrop-Android-App")
                .header("Accept", "text/html, */*")
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val html = response.body?.string() ?: ""
                val apkRegex = Pattern.compile("href=\"(/$owner/$repo/releases/download/[^\"]+\\.apk)\"", Pattern.CASE_INSENSITIVE)
                val m = apkRegex.matcher(html)
                if (m.find()) {
                    val relativePath = m.group(1)
                    return "https://github.com$relativePath"
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed resolving expanded_assets for $tag: ${e.message}")
        }

        // Predictable fallback: LootDrop releases follow predictable asset naming conventions
        val cleanTag = tag.removePrefix("v").removePrefix("V")
        return "https://github.com/$owner/$repo/releases/download/$tag/LootDrop-V$cleanTag.apk"
    }

    companion object {
        private const val TAG = "GithubReleaseClient"
        const val DEFAULT_OWNER = "BIGBKOG"
        const val DEFAULT_REPO = "LootDrop"
        const val REPO_URL = "https://github.com/$DEFAULT_OWNER/$DEFAULT_REPO"
        const val RELEASES_URL = "https://github.com/$DEFAULT_OWNER/$DEFAULT_REPO/releases"

        fun isCurrentModernStream(version: String): Boolean {
            val clean = version.trim().removePrefix("v").removePrefix("V")
            val major = clean.substringBefore(".").toIntOrNull() ?: 0
            return clean.startsWith("1.") || clean.startsWith("2.") || major >= 5
        }

        /**
         * Compares semantic versions (e.g. "1.5.X" vs "1.5.0", "V4.1" vs "4.0").
         * Returns true if remote is strictly newer than current.
         * Correctly handles wildcard releases ("1.5.X"), and protects against legacy 4.x/3.x
         * versions being falsely reported as newer when the app was reset to 1.5.0.
         */
        fun isNewerVersion(remoteTag: String, currentVersion: String): Boolean {
            if (remoteTag.isBlank()) return false
            val cleanRemote = remoteTag.trim().removePrefix("v").removePrefix("V")
            val cleanCurrent = currentVersion.trim().removePrefix("v").removePrefix("V")

            if (cleanRemote.equals(cleanCurrent, ignoreCase = true)) return false

            // The app transitioned its versioning scheme from legacy 4.5 down to 1.5.0.
            // If the user was on legacy 4.x/3.x, any 1.x release is the new release branch.
            val isRemoteOneSeries = cleanRemote.startsWith("1.")
            val isCurrentLegacy = cleanCurrent.startsWith("4.") || cleanCurrent.startsWith("3.")
            if (isCurrentLegacy && isRemoteOneSeries) {
                return true
            }

            // Any older 4.x/3.x releases on GitHub are legacy releases from before the reset
            // and should NOT be treated as newer when running on modern streams (1.x, 2.x).
            if (isCurrentModernStream(cleanCurrent)) {
                if (cleanRemote.startsWith("4.") || cleanRemote.startsWith("3.") ||
                    cleanRemote.contains("V4.") || cleanRemote.contains("V3.") ||
                    cleanRemote.contains("Loot-Drop-V3.")) {
                    return false
                }
            }

            fun parseTokens(s: String): List<Pair<Int, String>> {
                val parts = s.replace("-", ".").replace("_", ".").split(".")
                val tokens = mutableListOf<Pair<Int, String>>()
                for (p in parts) {
                    val trimmed = p.trim()
                    if (trimmed.isEmpty()) continue
                    if (trimmed.all { it.isDigit() }) {
                        tokens.add((trimmed.toIntOrNull() ?: 0) to "")
                    } else if (trimmed.equals("x", ignoreCase = true)) {
                        // Wildcard X / next build placeholder (e.g. 1.5.X is newer than 1.5.0)
                        tokens.add(9999 to "x")
                    } else {
                        val numPart = trimmed.filter { it.isDigit() }.toIntOrNull() ?: 0
                        val suffPart = trimmed.filter { !it.isDigit() }.lowercase()
                        tokens.add(numPart to suffPart)
                    }
                }
                return tokens
            }

            val rTokens = parseTokens(cleanRemote)
            val cTokens = parseTokens(cleanCurrent)

            val maxLen = maxOf(rTokens.size, cTokens.size)
            for (i in 0 until maxLen) {
                val (rNum, rSuff) = rTokens.getOrElse(i) { 0 to "" }
                val (cNum, cSuff) = cTokens.getOrElse(i) { 0 to "" }

                if (rNum > cNum) return true
                if (rNum < cNum) return false

                if (rSuff != cSuff) {
                    if (rSuff == "x" && cSuff.isEmpty()) return true
                    if (cSuff == "x" && rSuff.isEmpty()) return false
                    if (rSuff.isNotEmpty() && cSuff.isEmpty()) return false
                    if (cSuff.isNotEmpty() && rSuff.isEmpty()) return true
                    if (rSuff > cSuff) return true
                    if (rSuff < cSuff) return false
                }
            }

            return false
        }
    }
}
