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

/**
 * Lightweight client to check for new releases on the project's public GitHub repository.
 * 100% FOSS & F-Droid compatible (no proprietary Google Play Core In-App Update SDK).
 */
class GithubReleaseClient(private val context: Context) {

    private val httpClient: OkHttpClient by lazy {
        ApiClient.getOkHttpClient(context).newBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    suspend fun checkLatestRelease(
        owner: String = DEFAULT_OWNER,
        repo: String = DEFAULT_REPO,
        currentVersionName: String = BuildConfig.VERSION_NAME
    ): Result<AppUpdateInfo> = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.github.com/repos/$owner/$repo/releases/latest"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "LootDrop-Android/$currentVersionName")
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("GitHub API HTTP error: ${response.code}"))
            }

            val bodyString = response.body?.string()
                ?: return@withContext Result.failure(Exception("Empty response from GitHub API"))

            val json = JSONObject(bodyString)
            val tagName = json.optString("tag_name", "").trim()
            val releaseName = json.optString("name", "").ifBlank { "LootDrop $tagName" }
            val releaseBody = json.optString("body", "").trim()
            val htmlUrl = json.optString("html_url", "https://github.com/$owner/$repo/releases/latest")
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

            val hasUpdate = isNewerVersion(tagName, currentVersionName)
            val updateInfo = AppUpdateInfo(
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

            Result.success(updateInfo)
        } catch (e: Exception) {
            Log.e("GithubReleaseClient", "Failed to check release", e)
            Result.failure(e)
        }
    }

    companion object {
        const val DEFAULT_OWNER = "BIGBKOG"
        const val DEFAULT_REPO = "LootDrop"
        const val REPO_URL = "https://github.com/$DEFAULT_OWNER/$DEFAULT_REPO"
        const val RELEASES_URL = "https://github.com/$DEFAULT_OWNER/$DEFAULT_REPO/releases"

        /**
         * Compares semantic versions (e.g. "V4.1" vs "4.0").
         * Returns true if remote is strictly newer than current.
         */
        fun isNewerVersion(remoteTag: String, currentVersion: String): Boolean {
            if (remoteTag.isBlank()) return false
            val cleanRemote = remoteTag.trim().removePrefix("v").removePrefix("V")
            val cleanCurrent = currentVersion.trim().removePrefix("v").removePrefix("V")

            if (cleanRemote.equals(cleanCurrent, ignoreCase = true)) return false

            val remoteParts = cleanRemote.split(".", "-", "_").mapNotNull { it.toIntOrNull() }
            val currentParts = cleanCurrent.split(".", "-", "_").mapNotNull { it.toIntOrNull() }

            if (remoteParts.isEmpty() || currentParts.isEmpty()) {
                return cleanRemote != cleanCurrent
            }

            val maxLen = maxOf(remoteParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val r = remoteParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (r > c) return true
                if (r < c) return false
            }
            return false
        }
    }
}
