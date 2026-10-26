package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.remote.GithubReleaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

object AppUpdateInstaller {

    private const val TAG = "AppUpdateInstaller"

    // Dedicated clean unbuffered download client (independent of GamerPower HTTP cache and network interceptors)
    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    /**
     * Downloads an APK file inside the app, updating progress via callback.
     * Validates headers and APK binary integrity so corrupt or HTML files are not downloaded.
     */
    suspend fun downloadApk(
        context: Context,
        downloadUrl: String,
        versionTag: String,
        onProgress: (progress: Float, downloaded: Long, total: Long) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            var actualUrl = downloadUrl.trim()
            // If downloadUrl is a web release link, resolve the direct APK asset URL
            if (!actualUrl.endsWith(".apk", ignoreCase = true)) {
                val resolved = GithubReleaseClient(context).resolveApkUrl(
                    GithubReleaseClient.DEFAULT_OWNER,
                    GithubReleaseClient.DEFAULT_REPO,
                    versionTag
                )
                if (!resolved.isNullOrBlank()) {
                    actualUrl = resolved
                }
            }

            Log.d(TAG, "Starting APK download from: $actualUrl (tag: $versionTag)")

            val request = Request.Builder()
                .url(actualUrl)
                .header("User-Agent", "LootDrop-Android-App")
                .header("Accept", "application/octet-stream, application/vnd.android.package-archive, */*")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error ${response.code} downloading update from GitHub"))
            }

            val body = response.body
                ?: return@withContext Result.failure(Exception("Empty response body from update download"))

            val totalBytes = body.contentLength()
            val targetDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: File(context.cacheDir, "updates")
            targetDir.mkdirs()

            val cleanTag = versionTag.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
            val targetFile = File(targetDir, "lootdrop_$cleanTag.apk")

            if (targetFile.exists()) {
                targetFile.delete()
            }

            var inputStream: InputStream? = null
            var outputStream: FileOutputStream? = null

            try {
                inputStream = body.byteStream()
                outputStream = FileOutputStream(targetFile)

                val buffer = ByteArray(32 * 1024)
                var bytesRead: Int
                var downloadedBytes: Long = 0
                var lastProgressUpdate = System.currentTimeMillis()
                var verifiedHeader = false

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    if (!verifiedHeader && bytesRead >= 4) {
                        // Verify that file begins with ZIP magic numbers (PK\x03\x04 or PK\x05\x06)
                        val isZip = buffer[0] == 0x50.toByte() && buffer[1] == 0x4B.toByte()
                        if (!isZip) {
                            if (buffer[0] == '<'.code.toByte()) {
                                targetFile.delete()
                                return@withContext Result.failure(
                                    Exception("Download URL returned an HTML web page instead of an APK file. Please check GitHub release assets.")
                                )
                            }
                        }
                        verifiedHeader = true
                    }

                    outputStream.write(buffer, 0, bytesRead)
                    downloadedBytes += bytesRead

                    val now = System.currentTimeMillis()
                    if (now - lastProgressUpdate >= 60 || downloadedBytes == totalBytes) {
                        lastProgressUpdate = now
                        val progress = if (totalBytes > 0) {
                            (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                        } else 0f
                        onProgress(progress, downloadedBytes, totalBytes)
                    }
                }

                outputStream.flush()
                onProgress(1f, downloadedBytes, totalBytes)

                if (targetFile.length() < 50_000) {
                    targetFile.delete()
                    return@withContext Result.failure(Exception("Downloaded APK file is incomplete (${targetFile.length()} bytes)"))
                }

                Log.d(TAG, "Update APK successfully downloaded to: ${targetFile.absolutePath} ($downloadedBytes bytes)")
                Result.success(targetFile)
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed downloading update APK", e)
            Result.failure(e)
        }
    }

    /**
     * Triggers the Android system package installer to install the downloaded APK.
     */
    fun installApk(context: Context, apkFile: File): Boolean {
        return try {
            if (!apkFile.exists() || apkFile.length() <= 0) {
                Log.e(TAG, "Cannot install: APK file does not exist or is empty")
                return false
            }

            // Check Unknown Sources permission on Android 8.0+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    try {
                        val manageUnknownSourcesIntent = Intent(
                            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                            Uri.parse("package:${context.packageName}")
                        ).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(manageUnknownSourcesIntent)
                        Toast.makeText(
                            context,
                            "Allow LootDrop to install updates, then return to complete install.",
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not launch ACTION_MANAGE_UNKNOWN_APP_SOURCES", e)
                    }
                }
            }

            val authority = "${context.packageName}.fileprovider"
            val apkUri = FileProvider.getUriForFile(context, authority, apkFile)

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
            }

            context.startActivity(installIntent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch package installer", e)
            try {
                Toast.makeText(context, "Install error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {}
            false
        }
    }
}
