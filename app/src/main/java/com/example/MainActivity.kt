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
        com.example.shortcuts.AppShortcutHelper.updateDynamicShortcuts(this)

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val accentTheme by viewModel.accentTheme.collectAsState()
            val darkBgTheme by viewModel.darkBackgroundTheme.collectAsState()
            val lightBgTheme by viewModel.lightBackgroundTheme.collectAsState()
            val targetFrameRate by viewModel.targetFrameRate.collectAsState()
            val isDpiOptimizationEnabled by viewModel.isDpiOptimizationEnabled.collectAsState()
            val dpiScaleMode by viewModel.dpiScaleMode.collectAsState()
            val uiStats by viewModel.uiStats.collectAsState()

            LaunchedEffect(uiStats) {
                com.example.shortcuts.AppShortcutHelper.updateDynamicShortcuts(
                    context = this@MainActivity,
                    expiringCount = uiStats.expiringSoon,
                    newDropsCount = uiStats.newFreebiesCount,
                    vaultCount = uiStats.claimedCount
                )
            }

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

        // Action-based shortcut handling
        when (intent.action) {
            com.example.shortcuts.AppShortcutHelper.ACTION_EXPIRING_SOON -> {
                viewModel.selectTab(com.example.ui.MainTab.CALENDAR)
                com.example.shortcuts.AppShortcutHelper.reportShortcutUsed(this, com.example.shortcuts.AppShortcutHelper.ID_EXPIRING_SOON)
            }
            com.example.shortcuts.AppShortcutHelper.ACTION_TODAYS_DROPS -> {
                viewModel.selectTab(com.example.ui.MainTab.DISCOVER)
                viewModel.selectSort(com.example.ui.SortOption.NEWEST)
                com.example.shortcuts.AppShortcutHelper.reportShortcutUsed(this, com.example.shortcuts.AppShortcutHelper.ID_TODAYS_DROPS)
            }
            com.example.shortcuts.AppShortcutHelper.ACTION_MY_VAULT -> {
                viewModel.selectTab(com.example.ui.MainTab.CLAIMED)
                com.example.shortcuts.AppShortcutHelper.reportShortcutUsed(this, com.example.shortcuts.AppShortcutHelper.ID_MY_VAULT)
            }
        }

        // Extra-based target tab handling
        val targetTab = intent.getStringExtra(com.example.shortcuts.AppShortcutHelper.EXTRA_TARGET_TAB)
            ?: intent.getStringExtra("EXTRA_TARGET_TAB")
        if (targetTab != null) {
            when (targetTab) {
                "CLAIMED" -> {
                    viewModel.selectTab(com.example.ui.MainTab.CLAIMED)
                    com.example.shortcuts.AppShortcutHelper.reportShortcutUsed(this, com.example.shortcuts.AppShortcutHelper.ID_MY_VAULT)
                }
                "DISCOVER" -> {
                    viewModel.selectTab(com.example.ui.MainTab.DISCOVER)
                    val sortOption = intent.getStringExtra(com.example.shortcuts.AppShortcutHelper.EXTRA_SORT_OPTION)
                        ?: intent.getStringExtra("EXTRA_SORT_OPTION")
                    if (sortOption == "NEWEST") {
                        viewModel.selectSort(com.example.ui.SortOption.NEWEST)
                    }
                    com.example.shortcuts.AppShortcutHelper.reportShortcutUsed(this, com.example.shortcuts.AppShortcutHelper.ID_TODAYS_DROPS)
                }
                "CALENDAR" -> {
                    viewModel.selectTab(com.example.ui.MainTab.CALENDAR)
                    com.example.shortcuts.AppShortcutHelper.reportShortcutUsed(this, com.example.shortcuts.AppShortcutHelper.ID_EXPIRING_SOON)
                }
                "SETTINGS" -> viewModel.selectTab(com.example.ui.MainTab.SETTINGS)
            }
        }
    }
}
