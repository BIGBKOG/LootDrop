package com.example

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import coil.Coil
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.data.local.AppDatabase
import com.example.data.remote.ApiClient
import com.example.preferences.AppPreferences
import java.io.File
import java.util.Locale

data class StorageMetrics(
    val imageCacheBytes: Long,
    val httpCacheBytes: Long,
    val databaseBytes: Long,
    val totalFootprintBytes: Long
) {
    fun formatBytes(bytes: Long): String {
        if (bytes <= 0L) return "0 KB"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
            else -> String.format(Locale.US, "%.0f KB", kb)
        }
    }

    val formattedImageCache: String get() = formatBytes(imageCacheBytes)
    val formattedHttpCache: String get() = formatBytes(httpCacheBytes)
    val formattedDatabase: String get() = formatBytes(databaseBytes)
    val formattedTotal: String get() = formatBytes(totalFootprintBytes)
}

class LootDropApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        runCatching {
            com.example.i18n.LocaleSyncHelper.applyDefaultLocale(AppPreferences(this).appLanguage)
        }
        runCatching {
            // Pre-warm the shared network client with disk cache
            ApiClient.getOkHttpClient(this)
        }
        runCatching {
            // Initialize Android Dynamic Shortcuts
            com.example.shortcuts.AppShortcutHelper.updateDynamicShortcuts(this)
        }
    }

    override fun newImageLoader(): ImageLoader {
        val prefs = AppPreferences(this)
        val perfProfile = com.example.util.DevicePerformanceHelper.getProfile(this)
        val isDataSaver = prefs.isDataSaverMode
        val isLowEnd = perfProfile.isLowEndDevice || prefs.isLowEndOptimization || isDataSaver

        // Conservative memory cache on low-end hardware to avoid OutOfMemoryErrors
        val memoryPercent = if (isLowEnd) 0.07 else 0.18
        val diskMaxBytes = if (isDataSaver) 10L * 1024L * 1024L else if (isLowEnd) 12L * 1024L * 1024L else 24L * 1024L * 1024L

        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(memoryPercent)
                    .strongReferencesEnabled(!isLowEnd)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(diskMaxBytes)
                    .build()
            }
            .okHttpClient {
                ApiClient.getOkHttpClient(this)
            }
            .bitmapConfig(if (isLowEnd) android.graphics.Bitmap.Config.RGB_565 else perfProfile.recommendedBitmapConfig)
            .allowRgb565(true)
            .crossfade(!isLowEnd)
            .allowHardware(!isLowEnd && !perfProfile.isLowRamDevice)
            .respectCacheHeaders(false)
            .build()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW || level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            val aggressive = level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL || level >= ComponentCallbacks2.TRIM_MEMORY_COMPLETE
            trimMemoryCaches(this, aggressive = aggressive)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        trimMemoryCaches(this, aggressive = true)
    }

    companion object {
        fun trimMemoryCaches(context: Context, aggressive: Boolean = false) {
            runCatching {
                Coil.imageLoader(context).memoryCache?.clear()
            }
            if (aggressive) {
                runCatching { com.example.util.MetacriticCatalog.clearCaches() }
                runCatching { com.example.util.HardwareReadinessCatalog.clearCaches() }
                runCatching { com.example.ui.calendar.ExpirationCalendarUtils.clearCache() }
            }
        }

        fun getImageCacheSizeBytes(application: Application): Long {
            return runCatching {
                val imageCacheDir = application.cacheDir.resolve("image_cache")
                if (imageCacheDir.exists()) {
                    imageCacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
                } else 0L
            }.getOrDefault(0L)
        }

        fun getDatabaseSizeBytes(application: Application): Long {
            var dbBytes = 0L
            runCatching {
                val dbFile = application.getDatabasePath("lootdrop_games.db")
                if (dbFile.exists()) dbBytes += dbFile.length()
                val dbWal = File(dbFile.path + "-wal")
                if (dbWal.exists()) dbBytes += dbWal.length()
                val dbShm = File(dbFile.path + "-shm")
                if (dbShm.exists()) dbBytes += dbShm.length()
            }
            return dbBytes
        }

        fun getStorageMetrics(application: Application): StorageMetrics {
            val imgBytes = getImageCacheSizeBytes(application)
            val httpBytes = ApiClient.getHttpCacheSizeBytes(application)
            val dbBytes = getDatabaseSizeBytes(application)
            val total = imgBytes + httpBytes + dbBytes
            return StorageMetrics(
                imageCacheBytes = imgBytes,
                httpCacheBytes = httpBytes,
                databaseBytes = dbBytes,
                totalFootprintBytes = total
            )
        }

        fun clearImageDiskCache(application: Application): Long {
            var reclaimed = 0L
            runCatching {
                val imageCacheDir = application.cacheDir.resolve("image_cache")
                if (imageCacheDir.exists()) {
                    reclaimed = imageCacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
                    imageCacheDir.deleteRecursively()
                }
            }
            return reclaimed
        }

        fun vacuumAndCompactDatabase(application: Application) {
            AppDatabase.vacuumAndCompact(application)
        }

        fun deepCleanAll(application: Application): Long {
            val imgReclaimed = clearImageDiskCache(application)
            val httpReclaimed = ApiClient.clearHttpCache(application)
            vacuumAndCompactDatabase(application)
            return imgReclaimed + httpReclaimed
        }

        fun getStorageFootprintBytes(application: Application): Long {
            return getStorageMetrics(application).totalFootprintBytes
        }
    }
}
