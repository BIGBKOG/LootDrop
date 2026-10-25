package com.example.util

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.os.PowerManager
import androidx.compose.runtime.staticCompositionLocalOf

data class DevicePerformanceProfile(
    val isLowEndDevice: Boolean,
    val isLowRamDevice: Boolean,
    val totalRamMb: Int,
    val memoryClassMb: Int,
    val processorCores: Int,
    val isPowerSaveMode: Boolean,
    val recommendedBitmapConfig: Bitmap.Config,
    val recommendedMemoryCachePercent: Double,
    val telemetrySummary: String
)

object DevicePerformanceHelper {
    @Volatile
    private var cachedProfile: DevicePerformanceProfile? = null

    fun getProfile(context: Context): DevicePerformanceProfile {
        cachedProfile?.let { return it }

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val isLowRam = activityManager?.isLowRamDevice == true
        val memoryClass = activityManager?.memoryClass ?: 256
        val cores = Runtime.getRuntime().availableProcessors()

        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)
        val totalRamMb = (memoryInfo.totalMem / (1024L * 1024L)).toInt()

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isPowerSave = powerManager?.isPowerSaveMode == true

        // Comprehensive Low-end thresholds:
        // 1. Android OS flagged low RAM device (Android Go / budget builds)
        // 2. Total physical RAM <= 3500 MB (budget 2GB-3GB RAM hardware or low-tier 4GB phones)
        // 3. Memory class <= 192 MB (devices with limited heap capacity)
        // 4. Quad-core or fewer processors (budget entry SoC)
        // 5. System Battery Saver active (conserves CPU cycles)
        val isLowEnd = isLowRam || totalRamMb <= 3500 || memoryClass <= 192 || cores <= 4 || isPowerSave

        val bitmapConfig = if (isLowEnd) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888
        val memoryPercent = if (isLowEnd) 0.08 else 0.18

        val summary = buildString {
            append("${cores} Cores • ${totalRamMb}MB RAM • ${memoryClass}MB Heap")
            if (isLowRam) append(" • Low-RAM Mode")
            if (isPowerSave) append(" • Battery Saver")
            if (isLowEnd) append(" • Budget Optimized") else append(" • High Performance")
        }

        val profile = DevicePerformanceProfile(
            isLowEndDevice = isLowEnd,
            isLowRamDevice = isLowRam,
            totalRamMb = totalRamMb,
            memoryClassMb = memoryClass,
            processorCores = cores,
            isPowerSaveMode = isPowerSave,
            recommendedBitmapConfig = bitmapConfig,
            recommendedMemoryCachePercent = memoryPercent,
            telemetrySummary = summary
        )
        cachedProfile = profile
        return profile
    }

    fun isLowEndDevice(context: Context): Boolean = getProfile(context).isLowEndDevice
}

val LocalDevicePerformance = staticCompositionLocalOf {
    DevicePerformanceProfile(
        isLowEndDevice = false,
        isLowRamDevice = false,
        totalRamMb = 4096,
        memoryClassMb = 256,
        processorCores = 8,
        isPowerSaveMode = false,
        recommendedBitmapConfig = Bitmap.Config.ARGB_8888,
        recommendedMemoryCachePercent = 0.18,
        telemetrySummary = "Standard Profile"
    )
}
