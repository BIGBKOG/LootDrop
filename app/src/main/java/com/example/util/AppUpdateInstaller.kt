package com.example.util

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.remote.GithubReleaseClient
import com.example.i18n.AppLanguage
import com.example.preferences.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

object AppUpdateInstaller {

    private const val TAG = "AppUpdateInstaller"
    internal const val MIN_VALID_APK_BYTES = 50_000L

    // Dedicated clean unbuffered download client (independent of GamerPower HTTP cache and network interceptors)
    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    internal fun isZipMagicHeader(b0: Byte, b1: Byte): Boolean {
        return b0 == 0x50.toByte() && b1 == 0x4B.toByte()
    }

    internal fun hasValidApkFile(file: File, minBytes: Long = MIN_VALID_APK_BYTES): Boolean {
        if (!file.exists() || file.length() < minBytes) return false
        return try {
            FileInputStream(file).use { input ->
                val header = ByteArray(2)
                val read = input.read(header)
                read == 2 && isZipMagicHeader(header[0], header[1])
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Downloads an APK file inside the app, updating progress via callback.
     * Downloads to a temporary file, closes all streams cleanly, and validates APK ZIP magic bytes
     * before promoting to the final `.apk` file.
     */
    suspend fun downloadApk(
        context: Context,
        downloadUrl: String,
        versionTag: String,
        onProgress: (progress: Float, downloaded: Long, total: Long) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        var tempFile: File? = null
        try {
            var actualUrl = downloadUrl.trim()
            val cleanUrlPath = actualUrl.substringBefore("?").substringBefore("#")
            // If downloadUrl is a web release link, resolve the direct APK asset URL
            if (!cleanUrlPath.endsWith(".apk", ignoreCase = true)) {
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

            fun buildDownloadRequest(url: String) = Request.Builder()
                .url(url)
                .header("User-Agent", "LootDrop-Android-App")
                .header("Accept", "application/octet-stream, application/vnd.android.package-archive, */*")
                .build()

            val isEs = AppPreferences(context).appLanguage == AppLanguage.SPANISH
            var initialResponse = httpClient.newCall(buildDownloadRequest(actualUrl)).execute()
            if (initialResponse.code == 404 && versionTag.isNotBlank()) {
                val resolvedRetry = GithubReleaseClient(context).resolveApkUrl(
                    GithubReleaseClient.DEFAULT_OWNER,
                    GithubReleaseClient.DEFAULT_REPO,
                    versionTag
                )
                if (!resolvedRetry.isNullOrBlank() && resolvedRetry != actualUrl) {
                    initialResponse.close()
                    actualUrl = resolvedRetry
                    initialResponse = httpClient.newCall(buildDownloadRequest(actualUrl)).execute()
                }
            }

            initialResponse.use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception(
                            if (isEs) "Error HTTP ${response.code} al descargar la actualización"
                            else "HTTP error ${response.code} downloading update from GitHub"
                        )
                    )
                }

                val contentType = response.header("Content-Type")?.lowercase().orEmpty()
                if (contentType.contains("text/html")) {
                    return@withContext Result.failure(
                        Exception(
                            if (isEs) "El enlace devolvió HTML en lugar de un archivo APK."
                            else "Download URL returned an HTML web page instead of an APK file. Please check GitHub release assets."
                        )
                    )
                }

                val body = response.body
                    ?: return@withContext Result.failure(
                        Exception(if (isEs) "Respuesta vacía al descargar actualización" else "Empty response body from update download")
                    )

                val totalBytes = body.contentLength()
                val targetDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    ?: File(context.cacheDir, "updates")
                targetDir.mkdirs()

                val cleanTag = versionTag.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
                val targetFile = File(targetDir, "lootdrop_$cleanTag.apk")
                val stagingFile = File(targetDir, "lootdrop_$cleanTag.apk.tmp")
                tempFile = stagingFile

                if (stagingFile.exists()) {
                    stagingFile.delete()
                }

                var promoted = false
                try {
                    body.byteStream().use { inputStream ->
                        FileOutputStream(stagingFile).use { outputStream ->
                            val buffer = ByteArray(32 * 1024)
                            var bytesRead: Int
                            var downloadedBytes = 0L
                            var lastProgressUpdate = System.currentTimeMillis()
                            var verifiedHeader = false
                            var pendingFirstByte: Byte? = null

                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                if (bytesRead <= 0) continue

                                if (!verifiedHeader) {
                                    if (downloadedBytes == 0L && bytesRead >= 2) {
                                        if (!isZipMagicHeader(buffer[0], buffer[1])) {
                                            val msg = if (buffer[0] == '<'.code.toByte()) {
                                                if (isEs) "El enlace devolvió HTML en lugar de un archivo APK." else "Download URL returned an HTML web page instead of an APK file. Please check GitHub release assets."
                                            } else {
                                                if (isEs) "El archivo descargado no es un paquete APK válido." else "Downloaded file is not a valid Android APK package."
                                            }
                                            return@withContext Result.failure(Exception(msg))
                                        }
                                        verifiedHeader = true
                                    } else if (downloadedBytes == 0L && bytesRead == 1) {
                                        pendingFirstByte = buffer[0]
                                    } else if (downloadedBytes == 1L && pendingFirstByte != null) {
                                        val first = pendingFirstByte
                                        if (!isZipMagicHeader(first, buffer[0])) {
                                            val msg = if (first == '<'.code.toByte()) {
                                                if (isEs) "El enlace devolvió HTML en lugar de un archivo APK." else "Download URL returned an HTML web page instead of an APK file. Please check GitHub release assets."
                                            } else {
                                                if (isEs) "El archivo descargado no es un paquete APK válido." else "Downloaded file is not a valid Android APK package."
                                            }
                                            return@withContext Result.failure(Exception(msg))
                                        }
                                        verifiedHeader = true
                                    }
                                }

                                outputStream.write(buffer, 0, bytesRead)
                                downloadedBytes += bytesRead

                                val now = System.currentTimeMillis()
                                if (now - lastProgressUpdate >= 60 || (totalBytes > 0 && downloadedBytes == totalBytes)) {
                                    lastProgressUpdate = now
                                    val progress = if (totalBytes > 0) {
                                        (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                                    } else 0f
                                    onProgress(progress, downloadedBytes, totalBytes)
                                }
                            }

                            outputStream.flush()
                            onProgress(1f, downloadedBytes, totalBytes)
                        }
                    }

                    if (!hasValidApkFile(stagingFile)) {
                        val actualLen = stagingFile.length()
                        return@withContext Result.failure(
                            Exception(
                                if (isEs) "El archivo APK descargado está incompleto o dañado ($actualLen bytes)"
                                else "Downloaded APK file is incomplete or corrupt ($actualLen bytes)"
                            )
                        )
                    }

                    if (targetFile.exists()) {
                        targetFile.delete()
                    }
                    if (!stagingFile.renameTo(targetFile)) {
                        stagingFile.copyTo(targetFile, overwrite = true)
                    }
                    promoted = true

                    // Clean up older downloaded update APKs to avoid wasting storage
                    runCatching {
                        targetDir.listFiles()?.forEach { file ->
                            if (file.absolutePath != targetFile.absolutePath &&
                                file.name.startsWith("lootdrop_") &&
                                (file.name.endsWith(".apk") || file.name.endsWith(".apk.tmp"))
                            ) {
                                file.delete()
                            }
                        }
                    }

                    Log.d(
                        TAG,
                        "Update APK successfully downloaded to: ${targetFile.absolutePath} (${targetFile.length()} bytes)"
                    )
                    Result.success(targetFile)
                } finally {
                    if (!promoted || stagingFile.exists()) {
                        stagingFile.delete()
                    }
                }
            }
        } catch (e: Exception) {
            tempFile?.delete()
            Log.e(TAG, "Failed downloading update APK", e)
            Result.failure(e)
        }
    }

    private fun showToast(context: Context, message: String, duration: Int) {
        runCatching {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                Toast.makeText(context, message, duration).show()
            } else {
                Handler(Looper.getMainLooper()).post {
                    runCatching { Toast.makeText(context, message, duration).show() }
                }
            }
        }
    }

    /**
     * Triggers the Android system package installer to install the downloaded APK.
     */
    fun installApk(context: Context, apkFile: File): Boolean {
        val isEs = runCatching { AppPreferences(context).appLanguage == AppLanguage.SPANISH }.getOrDefault(false)
        return try {
            if (!hasValidApkFile(apkFile)) {
                Log.e(TAG, "Cannot install: APK file is missing, incomplete, or corrupt (${apkFile.absolutePath})")
                showToast(
                    context,
                    if (isEs) "El archivo APK falta o está incompleto. Vuelve a descargarlo."
                    else "APK file is missing or incomplete. Please download again.",
                    Toast.LENGTH_SHORT
                )
                return false
            }

            // Ensure Unknown Sources permission is granted before launching package installer
            if (!context.packageManager.canRequestPackageInstalls()) {
                try {
                    val manageUnknownSourcesIntent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${context.packageName}")
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(manageUnknownSourcesIntent)
                    showToast(
                        context,
                        if (isEs) "Permite que LootDrop instale apps desconocidas — la instalación continuará al volver."
                        else "Allow LootDrop to install unknown apps — installation will resume automatically when you return.",
                        Toast.LENGTH_LONG
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Could not launch ACTION_MANAGE_UNKNOWN_APP_SOURCES", e)
                    try {
                        val fallbackIntent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(fallbackIntent)
                    } catch (_: Exception) {}
                }
                return false
            }

            apkFile.setReadable(true, false)

            val authority = "${context.packageName}.fileprovider"
            val apkUri = FileProvider.getUriForFile(context, authority, apkFile)

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                clipData = ClipData.newRawUri("LootDrop Update APK", apkUri)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            }

            // Grant read permission explicitly to any package installer matching the intent
            try {
                val resolvedActivities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager.queryIntentActivities(
                        installIntent,
                        PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
                    )
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.queryIntentActivities(installIntent, PackageManager.MATCH_DEFAULT_ONLY)
                }
                for (resolveInfo in resolvedActivities) {
                    val packageName = resolveInfo.activityInfo.packageName
                    context.grantUriPermission(packageName, apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } catch (_: Exception) {}

            try {
                context.startActivity(installIntent)
            } catch (notFound: ActivityNotFoundException) {
                @Suppress("DEPRECATION")
                val fallbackInstall = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                    data = apkUri
                    clipData = ClipData.newRawUri("LootDrop Update APK", apkUri)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                }
                context.startActivity(fallbackInstall)
            }
            true
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "No package installer activity available", e)
            showToast(
                context,
                if (isEs) "No hay instalador de paquetes disponible." else "No package installer found on this device.",
                Toast.LENGTH_SHORT
            )
            false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch package installer", e)
            showToast(
                context,
                if (isEs) "Error de instalación: ${e.localizedMessage}" else "Install error: ${e.localizedMessage}",
                Toast.LENGTH_SHORT
            )
            false
        }
    }
}
