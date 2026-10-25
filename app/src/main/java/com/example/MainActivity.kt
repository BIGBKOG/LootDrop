package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.notifications.NotificationHelper
import com.example.ui.GiveawaysViewModel
import com.example.ui.HomeScreen
import com.example.ui.theme.LootDropTheme

import android.os.Build
import android.view.Surface
import androidx.compose.runtime.LaunchedEffect
import com.example.preferences.TargetFrameRate

import androidx.compose.runtime.CompositionLocalProvider
import com.example.ui.density.LocalScreenDensityInfo
import com.example.ui.density.rememberScreenDensityInfo

class MainActivity : ComponentActivity() {

    private val viewModel: GiveawaysViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val dm = resources.displayMetrics
        viewModel.setDetectedScreenDpi(dm.densityDpi)

        handleIntent(intent)

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val accentTheme by viewModel.accentTheme.collectAsState()
            val darkBgTheme by viewModel.darkBackgroundTheme.collectAsState()
            val lightBgTheme by viewModel.lightBackgroundTheme.collectAsState()
            val targetFrameRate by viewModel.targetFrameRate.collectAsState()
            val isDpiOptimizationEnabled by viewModel.isDpiOptimizationEnabled.collectAsState()
            val dpiScaleMode by viewModel.dpiScaleMode.collectAsState()

            val screenDensityInfo = rememberScreenDensityInfo(
                dpiScaleMode = dpiScaleMode,
                isOptimizationEnabled = isDpiOptimizationEnabled
            )

            LaunchedEffect(screenDensityInfo.densityDpi) {
                viewModel.setDetectedScreenDpi(screenDensityInfo.densityDpi)
            }

            LaunchedEffect(targetFrameRate) {
                applyDisplayRefreshRate(targetFrameRate)
            }

            val perfProfile = remember { com.example.util.DevicePerformanceHelper.getProfile(this@MainActivity) }

            CompositionLocalProvider(
                LocalScreenDensityInfo provides screenDensityInfo,
                com.example.util.LocalDevicePerformance provides perfProfile
            ) {
                LootDropTheme(
                    themeMode = themeMode,
                    accentTheme = accentTheme,
                    darkBackgroundTheme = darkBgTheme,
                    lightBackgroundTheme = lightBgTheme
                ) {
                    HomeScreen(viewModel = viewModel)
                }
            }
        }
    }

    private fun applyDisplayRefreshRate(targetRate: TargetFrameRate) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val currentDisplay = display
                val detectedHz = currentDisplay?.refreshRate?.toInt() ?: 90
                viewModel.setDetectedRefreshRate(detectedHz)

                val targetFps = when (targetRate) {
                    TargetFrameRate.FPS_60 -> 60f
                    TargetFrameRate.FPS_90 -> 90f
                    TargetFrameRate.FPS_120 -> 120f
                    TargetFrameRate.AUTO -> 0f
                }
                val params = window.attributes
                params.preferredRefreshRate = targetFps
                window.attributes = params
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val currentDisplay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    display
                } else {
                    @Suppress("DEPRECATION")
                    windowManager.defaultDisplay
                }
                val modes = currentDisplay?.supportedModes ?: emptyArray()

                val matchedMode = when (targetRate) {
                    TargetFrameRate.FPS_60 -> modes.find { it.refreshRate in 59.0f..61.0f }
                    TargetFrameRate.FPS_90 -> modes.find { it.refreshRate in 89.0f..91.0f }
                        ?: modes.maxByOrNull { it.refreshRate }
                    TargetFrameRate.FPS_120 -> modes.find { it.refreshRate in 119.0f..121.0f }
                        ?: modes.maxByOrNull { it.refreshRate }
                    TargetFrameRate.AUTO -> modes.maxByOrNull { it.refreshRate }
                }

                if (matchedMode != null) {
                    val params = window.attributes
                    params.preferredDisplayModeId = matchedMode.modeId
                    window.attributes = params
                }
            }
        } catch (_: Exception) {
            // Safe fallback if display mode adjustment is not supported by device/emulator
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkNewGamesOnAppReopen()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onAppClosedOrLeft()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val openUpdate = intent.getBooleanExtra(NotificationHelper.EXTRA_OPEN_UPDATE_DIALOG, false)
        if (openUpdate) {
            viewModel.setUpdateDialogVisible(true)
        }
        val giveawayId = intent.getIntExtra(NotificationHelper.EXTRA_GIVEAWAY_ID, -1)
        if (giveawayId != -1) {
            viewModel.openItemById(giveawayId)
        }
    }
}
