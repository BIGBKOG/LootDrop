package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.GiveawaysViewModel
import com.example.ui.theme.EpicGamesBlue
import com.example.ui.theme.GogPurple
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NintendoRed
import com.example.ui.theme.PlayStationBlue
import com.example.ui.theme.SteamBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.XboxGreen

private enum class AlertSubMenu(val label: String, val shortName: String) {
    ALL("All Alert Controls", "All"),
    STORES("Store Launchers", "Stores"),
    RULES("Smart Filters", "Rules"),
    AUDIO("Audio & Schedule", "Sound & Timing"),
    PREVIEW("Live Simulator", "Preview")
}

@Composable
fun AlertsSettingsCategoryContent(
    viewModel: GiveawaysViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isNotificationsEnabled by viewModel.isNotificationsEnabled.collectAsState()
    val isNotifyPlayStation by viewModel.isNotifyPlayStation.collectAsState()
    val isNotifyXbox by viewModel.isNotifyXbox.collectAsState()
    val isNotifyNintendo by viewModel.isNotifyNintendo.collectAsState()
    val isNotifySteam by viewModel.isNotifySteam.collectAsState()
    val isNotifyEpic by viewModel.isNotifyEpic.collectAsState()
    val isNotifyGog by viewModel.isNotifyGog.collectAsState()
    val isNotifyOthers by viewModel.isNotifyOthers.collectAsState()
    val isGamesOnly by viewModel.isGamesOnly.collectAsState()
    val isKeepForeverOnly by viewModel.isKeepForeverOnly.collectAsState()
    val isNotifyEndingSoon by viewModel.isNotifyEndingSoon.collectAsState()
    val minWorthThreshold by viewModel.minWorthAlertThreshold.collectAsState()
    val isQuietHours by viewModel.isQuietHoursEnabled.collectAsState()
    val quietHoursStart by viewModel.quietHoursStartHour.collectAsState()
    val quietHoursEnd by viewModel.quietHoursEndHour.collectAsState()
    val isSoundEnabled by viewModel.isNotificationSoundEnabled.collectAsState()
    val isVibrateEnabled by viewModel.isNotificationVibrateEnabled.collectAsState()
    val isRichArtEnabled by viewModel.isNotificationRichArtwork.collectAsState()
    val isActionsEnabled by viewModel.isNotificationActionsEnabled.collectAsState()
    val syncIntervalHours by viewModel.backgroundSyncIntervalHours.collectAsState()
    val wifiOnly by viewModel.backgroundSyncWifiOnly.collectAsState()

    var activeSubMenu by remember { mutableStateOf(AlertSubMenu.ALL) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Notification permission granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permission denied. Enable in device settings.", Toast.LENGTH_SHORT).show()
        }
    }

    val monitoredStoresCount = listOf(
        isNotifyPlayStation, isNotifyXbox, isNotifyNintendo,
        isNotifySteam, isNotifyEpic, isNotifyGog, isNotifyOthers
    ).count { it }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Sub-Menu Navigation Chips Bar
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(AlertSubMenu.values()) { subMenu ->
                val isSelected = subMenu == activeSubMenu
                Surface(
                    onClick = { activeSubMenu = subMenu },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = subMenu.shortName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Section: Master Switch Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isNotificationsEnabled) NeonGreen else Color.Gray)
                            )
                            Text(
                                text = "Smart Push Alerts Engine",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = if (isNotificationsEnabled)
                                "Actively monitoring $monitoredStoresCount stores • Instant alerts on drop"
                            else
                                "All background notifications & alarms are currently paused",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = isNotificationsEnabled,
                        onCheckedChange = { checked ->
                            viewModel.setNotificationsEnabled(checked)
                            if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                                if (!hasPermission) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.background,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("settings_master_notifications_switch")
                    )
                }

                // Summary status tags
                if (isNotificationsEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        NotificationStatusChip(
                            label = if (isSoundEnabled) "Sound ON" else "Silent",
                            active = isSoundEnabled,
                            modifier = Modifier.weight(1f)
                        )
                        NotificationStatusChip(
                            label = if (isVibrateEnabled) "Vibrate ON" else "Vibrate OFF",
                            active = isVibrateEnabled,
                            modifier = Modifier.weight(1f)
                        )
                        NotificationStatusChip(
                            label = if (isKeepForeverOnly) "Keep-Only" else "All Deals",
                            active = isKeepForeverOnly,
                            modifier = Modifier.weight(1f)
                        )
                        NotificationStatusChip(
                            label = if (isRichArtEnabled) "Poster Art" else "Compact",
                            active = isRichArtEnabled,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Section: Live Interactive Notification Simulator Card (Visible in PREVIEW or ALL)
        if (activeSubMenu == AlertSubMenu.ALL || activeSubMenu == AlertSubMenu.PREVIEW) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text(
                                text = "LIVE NOTIFICATION PREVIEW",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = if (isNotificationsEnabled) "How it looks on your device" else "Notifications paused",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Android System Notification Shade Simulation Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF161B26))
                            .border(1.dp, Color(0xFF28324A), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // System Header Line
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF6C5CE7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SportsEsports,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Text("LootDrop", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("•", fontSize = 10.sp, color = TextMuted)
                                Text("now", fontSize = 11.sp, color = TextMuted)
                                Spacer(modifier = Modifier.weight(1f))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF6C5CE7).copy(alpha = 0.25f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("🔥 Major Games", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB388FF))
                                }
                            }

                            // Notification Content
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "🎮 FREE NOW on Epic Games: Death Stranding",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Worth $39.99 • Claim it to keep forever! Tap to view details.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB0BAC9),
                                    lineHeight = 15.sp
                                )
                            }

                            // Optional Rich BigPicture Artwork Simulation
                            if (isRichArtEnabled) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(
                                                    Color(0xFF1E283C),
                                                    Color(0xFF0F172A),
                                                    Color(0xFF2C1654)
                                                )
                                            )
                                        )
                                        .border(1.dp, Color(0xFF384566), RoundedCornerShape(10.dp))
                                        .padding(10.dp),
                                    contentAlignment = Alignment.BottomStart
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(NeonGreen)
                                                .padding(horizontal = 7.dp, vertical = 3.dp)
                                        ) {
                                            Text("100% OFF", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0B0F19))
                                        }
                                        Text(
                                            text = "Death Stranding: Director's Cut • PC",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            // Optional Action Buttons Simulation
                            if (isActionsEnabled) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    SimulatedActionButton(label = "Claim Free", color = NeonGreen)
                                    SimulatedActionButton(label = "★ Bookmark", color = Color(0xFF38BDF8))
                                    SimulatedActionButton(label = "Share", color = Color(0xFFD946EF))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Store Platforms & Launchers (Visible in STORES or ALL)
        if (activeSubMenu == AlertSubMenu.ALL || activeSubMenu == AlertSubMenu.STORES) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "MONITORED STORES & PLATFORMS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Toggle alerts per store independently",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(
                                onClick = {
                                    viewModel.setNotifyPlayStation(true)
                                    viewModel.setNotifyXbox(true)
                                    viewModel.setNotifyNintendo(true)
                                    viewModel.setNotifySteam(true)
                                    viewModel.setNotifyEpic(true)
                                    viewModel.setNotifyGog(true)
                                    viewModel.setNotifyOthers(true)
                                },
                                enabled = isNotificationsEnabled
                            ) {
                                Text("All On", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            TextButton(
                                onClick = {
                                    viewModel.setNotifyPlayStation(false)
                                    viewModel.setNotifyXbox(false)
                                    viewModel.setNotifyNintendo(false)
                                    viewModel.setNotifySteam(false)
                                    viewModel.setNotifyEpic(false)
                                    viewModel.setNotifyGog(false)
                                    viewModel.setNotifyOthers(false)
                                },
                                enabled = isNotificationsEnabled
                            ) {
                                Text("Mute All", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)

                    AlertsToggleRow(
                        title = "PlayStation Deals & Drops",
                        subtitle = "PS5 & PS4 100% discount free games & loot",
                        color = PlayStationBlue,
                        checked = isNotifyPlayStation,
                        enabled = isNotificationsEnabled,
                        onCheckedChange = { viewModel.setNotifyPlayStation(it) }
                    )

                    AlertsToggleRow(
                        title = "Xbox Deals & Drops",
                        subtitle = "Series X|S & Xbox One free games & keys",
                        color = XboxGreen,
                        checked = isNotifyXbox,
                        enabled = isNotificationsEnabled,
                        onCheckedChange = { viewModel.setNotifyXbox(it) }
                    )

                    AlertsToggleRow(
                        title = "Nintendo Switch eShop",
                        subtitle = "Switch 100% discount freebies & drops",
                        color = NintendoRed,
                        checked = isNotifyNintendo,
                        enabled = isNotificationsEnabled,
                        onCheckedChange = { viewModel.setNotifyNintendo(it) }
                    )

                    AlertsToggleRow(
                        title = "Steam Free Promotions",
                        subtitle = "Steam store free games & 100% off keys",
                        color = SteamBlue,
                        checked = isNotifySteam,
                        enabled = isNotificationsEnabled,
                        onCheckedChange = { viewModel.setNotifySteam(it) }
                    )

                    AlertsToggleRow(
                        title = "Epic Games Store",
                        subtitle = "Weekly free games & mystery vault titles",
                        color = EpicGamesBlue,
                        checked = isNotifyEpic,
                        enabled = isNotificationsEnabled,
                        onCheckedChange = { viewModel.setNotifyEpic(it) }
                    )

                    AlertsToggleRow(
                        title = "GOG.com DRM-Free",
                        subtitle = "DRM-free classic and indie giveaways",
                        color = GogPurple,
                        checked = isNotifyGog,
                        enabled = isNotificationsEnabled,
                        onCheckedChange = { viewModel.setNotifyGog(it) }
                    )

                    AlertsToggleRow(
                        title = "Other PC Launchers",
                        subtitle = "Ubisoft, itch.io, EA App, Prime Gaming",
                        color = NeonGreen,
                        checked = isNotifyOthers,
                        enabled = isNotificationsEnabled,
                        onCheckedChange = { viewModel.setNotifyOthers(it) }
                    )
                }
            }
        }

        // Section: Smart Alert Rules & Filters (Visible in RULES or ALL)
        if (activeSubMenu == AlertSubMenu.ALL || activeSubMenu == AlertSubMenu.RULES) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "SMART ALERT RULES & FILTERS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    // Keep Forever To Own Filter (NEW)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Keep-Forever Games Only",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNotificationsEnabled) MaterialTheme.colorScheme.onSurface else TextMuted
                            )
                            Text(
                                text = "Filter out temporary free weekends and time-limited trial periods",
                                fontSize = 11.sp,
                                color = if (isNotificationsEnabled) MaterialTheme.colorScheme.onSurfaceVariant else TextMuted
                            )
                        }

                        Switch(
                            checked = isKeepForeverOnly,
                            enabled = isNotificationsEnabled,
                            onCheckedChange = { viewModel.setKeepForeverOnly(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.background,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)

                    // Full Games Only
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Full Games Only (Mute DLC)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isNotificationsEnabled) MaterialTheme.colorScheme.onSurface else TextMuted
                            )
                            Text(
                                text = "Skip alerts for DLC packs, cosmetic skins, and beta keys",
                                fontSize = 11.sp,
                                color = if (isNotificationsEnabled) MaterialTheme.colorScheme.onSurfaceVariant else TextMuted
                            )
                        }

                        Switch(
                            checked = isGamesOnly,
                            enabled = isNotificationsEnabled,
                            onCheckedChange = { viewModel.setGamesOnly(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.background,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)

                    // 24h Ending Soon Reminders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(imageVector = Icons.Default.HourglassTop, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(15.dp))
                                Text(
                                    text = "24h Expiring Soon Reminders",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isNotificationsEnabled) MaterialTheme.colorScheme.onSurface else TextMuted
                                )
                            }
                            Text(
                                text = "Urgent reminder 24 hours before saved giveaways expire",
                                fontSize = 11.sp,
                                color = if (isNotificationsEnabled) MaterialTheme.colorScheme.onSurfaceVariant else TextMuted
                            )
                        }

                        Switch(
                            checked = isNotifyEndingSoon,
                            enabled = isNotificationsEnabled,
                            onCheckedChange = { viewModel.setNotifyEndingSoon(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.background,
                                checkedTrackColor = Color(0xFFFF5252)
                            )
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)

                    // Minimum Worth Alert Threshold with $30+ AAA option
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Minimum Original Game Value:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isNotificationsEnabled) MaterialTheme.colorScheme.onSurface else TextMuted
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(
                                listOf(
                                    0f to "Any ($0+)",
                                    5f to "Worth $5+",
                                    10f to "Worth $10+",
                                    20f to "Worth $20+",
                                    30f to "Worth $30+ (AAA)"
                                )
                            ) { (threshold, label) ->
                                val selected = minWorthThreshold == threshold
                                FilterChip(
                                    selected = selected,
                                    enabled = isNotificationsEnabled,
                                    onClick = { viewModel.setMinWorthAlertThreshold(threshold) },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.background
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Audio, Haptics & Quiet Hours (Visible in AUDIO or ALL)
        if (activeSubMenu == AlertSubMenu.ALL || activeSubMenu == AlertSubMenu.AUDIO) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "DELIVERY, AUDIO & QUIET SCHEDULE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    // Audio Sound toggle (NEW)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Notification Sound Effects",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isNotificationsEnabled) MaterialTheme.colorScheme.onSurface else TextMuted
                            )
                            Text(
                                text = "Play alert tone when new freebies are discovered",
                                fontSize = 11.sp,
                                color = if (isNotificationsEnabled) MaterialTheme.colorScheme.onSurfaceVariant else TextMuted
                            )
                        }

                        Switch(
                            checked = isSoundEnabled,
                            enabled = isNotificationsEnabled,
                            onCheckedChange = { viewModel.setNotificationSoundEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.background,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)

                    // Haptic Vibration toggle (NEW)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Haptic Vibration Pulse",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isNotificationsEnabled) MaterialTheme.colorScheme.onSurface else TextMuted
                            )
                            Text(
                                text = "Vibrate device when new deals drop",
                                fontSize = 11.sp,
                                color = if (isNotificationsEnabled) MaterialTheme.colorScheme.onSurfaceVariant else TextMuted
                            )
                        }

                        Switch(
                            checked = isVibrateEnabled,
                            enabled = isNotificationsEnabled,
                            onCheckedChange = { viewModel.setNotificationVibrateEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.background,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)

                    // Rich Artwork toggle (NEW)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Rich Game Artwork (BigPicture)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isNotificationsEnabled) MaterialTheme.colorScheme.onSurface else TextMuted
                            )
                            Text(
                                text = "Download and display high-res game posters inside alerts",
                                fontSize = 11.sp,
                                color = if (isNotificationsEnabled) MaterialTheme.colorScheme.onSurfaceVariant else TextMuted
                            )
                        }

                        Switch(
                            checked = isRichArtEnabled,
                            enabled = isNotificationsEnabled,
                            onCheckedChange = { viewModel.setNotificationRichArtwork(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.background,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)

                    // Notification Action Buttons toggle (NEW)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "1-Tap Quick Action Buttons",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isNotificationsEnabled) MaterialTheme.colorScheme.onSurface else TextMuted
                            )
                            Text(
                                text = "Include 'Claim Free', '★ Bookmark', and 'Share' buttons in alerts",
                                fontSize = 11.sp,
                                color = if (isNotificationsEnabled) MaterialTheme.colorScheme.onSurfaceVariant else TextMuted
                            )
                        }

                        Switch(
                            checked = isActionsEnabled,
                            enabled = isNotificationsEnabled,
                            onCheckedChange = { viewModel.setNotificationActionsEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.background,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)

                    // Quiet Hours with Presets
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(imageVector = Icons.Default.Nightlight, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                                Column {
                                    Text(
                                        text = "Quiet Hours (Do Not Disturb)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isNotificationsEnabled) MaterialTheme.colorScheme.onSurface else TextMuted
                                    )
                                    Text(
                                        text = if (isQuietHours) "Muting alerts between $quietHoursStart:00 and $quietHoursEnd:00" else "Alerts deliver around the clock",
                                        fontSize = 11.sp,
                                        color = if (isNotificationsEnabled) MaterialTheme.colorScheme.onSurfaceVariant else TextMuted
                                    )
                                }
                            }

                            Switch(
                                checked = isQuietHours,
                                enabled = isNotificationsEnabled,
                                onCheckedChange = { viewModel.setQuietHoursEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.background,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        if (isQuietHours) {
                            Text(
                                text = "Quiet Hours Schedule Presets:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(
                                    listOf(
                                        Triple("Night (11 PM - 8 AM)", 23, 8),
                                        Triple("Late Night (1 AM - 9 AM)", 1, 9),
                                        Triple("Work (9 AM - 5 PM)", 9, 17)
                                    )
                                ) { (label, start, end) ->
                                    val isSelected = quietHoursStart == start && quietHoursEnd == end
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setQuietHoursHours(start, end) },
                                        label = { Text(label, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.background
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)

                    // Background Sync Interval
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.Sync, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(15.dp))
                            Text(
                                text = "Background Scanner Frequency:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isNotificationsEnabled) MaterialTheme.colorScheme.onSurface else TextMuted
                            )
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(listOf(2 to "2h", 4 to "4h", 8 to "8h", 24 to "Daily", 0 to "Manual")) { (hours, label) ->
                                val selected = syncIntervalHours == hours
                                FilterChip(
                                    selected = selected,
                                    enabled = isNotificationsEnabled,
                                    onClick = { viewModel.setBackgroundSyncInterval(hours) },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.background
                                    )
                                )
                            }
                        }
                    }

                    // Wi-Fi Only
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.Wifi, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                            Column {
                                Text(
                                    text = "Require Unmetered Wi-Fi",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isNotificationsEnabled) MaterialTheme.colorScheme.onSurface else TextMuted
                                )
                                Text(
                                    text = "Only run background scans when connected to Wi-Fi",
                                    fontSize = 11.sp,
                                    color = if (isNotificationsEnabled) MaterialTheme.colorScheme.onSurfaceVariant else TextMuted
                                )
                            }
                        }

                        Switch(
                            checked = wifiOnly,
                            enabled = isNotificationsEnabled,
                            onCheckedChange = { viewModel.setBackgroundSyncWifiOnly(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.background,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }

        // Section: Action Center (Inbox & Test Push) - Always visible
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "ACTIONS & NOTIFICATION TESTING",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )

                // Open Alert Inbox Button
                Button(
                    onClick = { viewModel.setNotificationHistorySheetOpen(true) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Open Alert Inbox & History", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Dual Test Alert Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                                if (!hasPermission) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    return@OutlinedButton
                                }
                            }
                            viewModel.sendTestNotification(isRichPreview = isRichArtEnabled)
                            Toast.makeText(context, "Rich game alert test sent! Check shade.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("settings_test_alert_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("Test Game Drop", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                                if (!hasPermission) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    return@OutlinedButton
                                }
                            }
                            viewModel.sendEndingSoonTestNotification()
                            Toast.makeText(context, "Expiring Soon alert test sent! Check shade.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("settings_test_ending_soon_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFFF5252)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("Test Expiring", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationStatusChip(
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
                0.5.dp,
                if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .padding(vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SimulatedActionButton(
    label: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(0.8.dp, color.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun AlertsToggleRow(
    title: String,
    subtitle: String,
    color: Color,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else TextMuted
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else TextMuted
                )
            }
        }

        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.background,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
