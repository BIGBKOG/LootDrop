package com.example.util

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.staticCompositionLocalOf

data class DevicePerformanceProfile(
    val isLowEndDevice: Boolean,
    val isLowRamDevice: Boolean,
    val memoryClassMb: Int,
    val processorCores: Int,
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

        // Low-end thresholds:
        // 1. Android OS flagged low RAM device (Android Go / budget builds)
        // 2. Memory class <= 192 MB (devices with 2GB-3GB RAM or less available to heap)
        // 3. Quad-core or fewer processors (budget SoC)
        val isLowEnd = isLowRam || memoryClass <= 192 || cores <= 4

        val bitmapConfig = if (isLowEnd) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888
        val memoryPercent = if (isLowEnd) 0.10 else 0.18

        val summary = buildString {
            append("${cores} Cores • ${memoryClass}MB Heap")
            if (isLowRam) append(" • Low-RAM Mode")
            if (isLowEnd) append(" • Budget Optimized") else append(" • High Performance")
        }

        val profile = DevicePerformanceProfile(
            isLowEndDevice = isLowEnd,
            isLowRamDevice = isLowRam,
            memoryClassMb = memoryClass,
            processorCores = cores,
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
        memoryClassMb = 256,
        processorCores = 8,
        recommendedBitmapConfig = Bitmap.Config.ARGB_8888,
        recommendedMemoryCachePercent = 0.18,
        telemetrySummary = "Standard Profile"
    )
}
