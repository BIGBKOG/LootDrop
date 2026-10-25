package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.notifications.NotificationHelper
import com.example.notifications.NotificationScheduler
import com.example.preferences.AppPreferences
import com.example.ui.theme.EpicGamesBlue
import com.example.ui.theme.GamingCardBorder
import com.example.ui.theme.GamingDarkBackground
import com.example.ui.theme.GamingDarkSurface
import com.example.ui.theme.GamingDarkSurfaceVariant
import com.example.ui.theme.GogPurple
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NintendoRed
import com.example.ui.theme.PlayStationBlue
import com.example.ui.theme.SteamBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.XboxGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsSheet(
    notificationHelper: NotificationHelper,
    onDismiss: () -> Unit,
    onOpenInbox: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val appPrefs = remember { AppPreferences(context) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    var isNotificationsEnabled by remember { mutableStateOf(notificationHelper.isNotificationsEnabled) }
    var isNotifyPlayStation by remember { mutableStateOf(notificationHelper.isNotifyPlayStation) }
    var isNotifyXbox by remember { mutableStateOf(notificationHelper.isNotifyXbox) }
    var isNotifyNintendo by remember { mutableStateOf(notificationHelper.isNotifyNintendo) }
    var isNotifySteam by remember { mutableStateOf(notificationHelper.isNotifySteam) }
    var isNotifyEpic by remember { mutableStateOf(notificationHelper.isNotifyEpic) }
    var isNotifyGog by remember { mutableStateOf(notificationHelper.isNotifyGog) }
    var isNotifyAndroid by remember { mutableStateOf(notificationHelper.isNotifyAndroid) }
    var isNotifyIos by remember { mutableStateOf(notificationHelper.isNotifyIos) }
    var isNotifyOthers by remember { mutableStateOf(notificationHelper.isNotifyOthers) }
    var isGamesOnly by remember { mutableStateOf(notificationHelper.isGamesOnly) }
    var isKeepForeverOnly by remember { mutableStateOf(notificationHelper.isKeepForeverOnly) }
    var isSoundEnabled by remember { mutableStateOf(notificationHelper.isNotificationSoundEnabled) }
    var isVibrateEnabled by remember { mutableStateOf(notificationHelper.isNotificationVibrateEnabled) }
    var isRichArtEnabled by remember { mutableStateOf(notificationHelper.isNotificationRichArtwork) }
    var isActionsEnabled by remember { mutableStateOf(notificationHelper.isNotificationActionsEnabled) }
    var isNotifyEndingSoon by remember { mutableStateOf(notificationHelper.isNotifyEndingSoon) }
    var minWorthThreshold by remember { mutableStateOf(notificationHelper.minWorthAlertThreshold) }
    var isQuietHours by remember { mutableStateOf(notificationHelper.isQuietHoursEnabled) }
    var syncIntervalHours by remember { mutableStateOf(appPrefs.backgroundSyncIntervalHours) }
    var wifiOnly by remember { mutableStateOf(appPrefs.backgroundSyncWifiOnly) }

    // Permission launcher for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Notification permission granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notification permission denied. Enable in system settings.", Toast.LENGTH_SHORT).show()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = GamingDarkSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(20.dp)
                .verticalScroll(scrollState)
                .testTag("notification_settings_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Notification Hub & Alerts",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Smart push alerts, rich game artwork & channels",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Notification Master Switch
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(GamingDarkSurfaceVariant)
                    .border(1.dp, GamingCardBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Smart Push Alerts",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Receive rich alerts with game artwork when free deals drop",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Switch(
                        checked = isNotificationsEnabled,
                        onCheckedChange = { checked ->
                            isNotificationsEnabled = checked
                            notificationHelper.isNotificationsEnabled = checked
                            if (checked) {
                                NotificationScheduler.schedulePeriodicSync(context)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (!hasPermission) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                            } else {
                                NotificationScheduler.cancelPeriodicSync(context)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GamingDarkBackground,
                            checkedTrackColor = NeonCyan,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = Color(0xFF1E283C)
                        ),
                        modifier = Modifier.testTag("master_notification_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Notification Channels Indicator Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(GamingDarkSurfaceVariant.copy(alpha = 0.6f))
                    .border(1.dp, GamingCardBorder, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "ACTIVE NOTIFICATION CHANNELS (ANDROID 8+)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ChannelStatusBadge("🔥 Games", NeonCyan, Modifier.weight(1f))
                        ChannelStatusBadge("⏳ Ending Soon", Color(0xFFFF5252), Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ChannelStatusBadge("🎁 DLC & Loot", Color(0xFFB388FF), Modifier.weight(1f))
                        ChannelStatusBadge("🔔 System Sync", NeonGreen, Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Platform Filters section
            Text(
                text = "STORE LAUNCHERS TO TRACK",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(GamingDarkSurfaceVariant)
                    .border(1.dp, GamingCardBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Column {
                    NotificationSettingRow(
                        title = "PlayStation Store (PS5 / PS4)",
                        subtitle = "PS Store giveaways & 100% discount promotions",
                        color = PlayStationBlue,
                        checked = isNotifyPlayStation,
                        enabled = isNotificationsEnabled,
                        onCheckedChange = {
                            isNotifyPlayStation = it
                            notificationHelper.isNotifyPlayStation = it
                        }
                    )

                    Divider(color = GamingCardBorder, thickness = 0.5.dp)

                    NotificationSettingRow(
                        title = "Xbox Series X|S & Xbox One",
                        subtitle = "Xbox Marketplace free game promotions",
                        color = XboxGreen,
                        checked = isNotifyXbox,
                        enabled = isNotificationsEnabled,
                        onCheckedChange = {
                            isNotifyXbox = it
                            notificationHelper.isNotifyXbox = it
                        }
                    )

                    Divider(color = GamingCardBorder, thickness = 0.5.dp)

                    NotificationSettingRow(
                        title = "Nintendo Switch eShop",
                        subtitle = "Nintendo Switch 100% discount freebies & drops",
                        color = NintendoRed,
                        checked = isNotifyNintendo,
                        enabled = isNotificationsEnabled,
                        onCheckedChange = {
                            isNotifyNintendo = it
                            notificationHelper.isNotifyNintendo = it
                        }
                    )

                    Divider(color = GamingCardBorder, thickness = 0.5.dp)

                    NotificationSettingRow(
                        title = "Steam",
                        subtitle = "Steam store free promotions & 100% off deals",
                        color = SteamBlue,
                        checked = isNotifySteam,
                        enabled = isNotificationsEnabled,
                        onCheckedChange = {
                            isNotifySteam = it
                            notificationHelper.isNotifySteam = it
                        }
                    )

                    Divider(color = GamingCardBorder, thickness = 0.5.dp)

                    NotificationSettingRow(
                        title = "Epic Games Store",
                        subtitle = "Weekly free games vault & mystery deals",
                        color = EpicGamesBlue,
                        checked = isNotifyEpic,
                        enabled = isNotificationsEnabled,
                        onCheckedChange = {
                            isNotifyEpic = it
                            notificationHelper.isNotifyEpic = it
                        }
                    )

                    Divider(color = GamingCardBorder, thickness = 0.5.dp)

                    NotificationSettingRow(
                        title = "GOG.com",
                        subtitle = "DRM-free PC classic and indie game drops",
                        color = GogPurple,
                        checked = isNotifyGog,
                        enabled = isNotificationsEnabled,
                        onCheckedChange = {
                            isNotifyGog = it
                            notificationHelper.isNotifyGog = it
                        }
                    )

                    Divider(color = GamingCardBorder, thickness = 0.5.dp)

                    NotificationSettingRow(
                        title = "Google Play / Android",
                        subtitle = "Android mobile games, apps & in-game pack promotions",
                        color = Color(0xFF3DDC84),
                        checked = isNotifyAndroid,
                        enabled = isNotificationsEnabled,
                        onCheckedChange = {
                            isNotifyAndroid = it
                            notificationHelper.isNotifyAndroid = it
                        }
                    )

                    Divider(color = GamingCardBorder, thickness = 0.5.dp)

                    NotificationSettingRow(
                        title = "Apple App Store / iOS",
                        subtitle = "iOS (iPhone & iPad) free titles & giveaway promos",
                        color = Color(0xFF007AFF),
                        checked = isNotifyIos,
                        enabled = isNotificationsEnabled,
                        onCheckedChange = {
                            isNotifyIos = it
                            notificationHelper.isNotifyIos = it
                        }
                    )

                    Divider(color = GamingCardBorder, thickness = 0.5.dp)

                    NotificationSettingRow(
                        title = "Other PC Launchers",
                        subtitle = "Ubisoft, itch.io, EA App, and Prime Gaming",
                        color = NeonGreen,
                        checked = isNotifyOthers,
                        enabled = isNotificationsEnabled,
                        onCheckedChange = {
                            isNotifyOthers = it
                            notificationHelper.isNotifyOthers = it
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Smart Alert Tuning
            Text(
                text = "SMART ALERT TUNING",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(GamingDarkSurfaceVariant)
                    .border(1.dp, GamingCardBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Ending Soon Alerts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.HourglassTop, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "24h Expiring Soon Reminders",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNotificationsEnabled) TextPrimary else TextMuted
                                )
                            }
                            Text(
                                text = "Alert before giveaways in your library or tracked stores expire",
                                fontSize = 11.sp,
                                color = if (isNotificationsEnabled) TextSecondary else TextMuted
                            )
                        }

                        Switch(
                            checked = isNotifyEndingSoon,
                            enabled = isNotificationsEnabled,
                            onCheckedChange = {
                                isNotifyEndingSoon = it
                                notificationHelper.isNotifyEndingSoon = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GamingDarkBackground,
                                checkedTrackColor = Color(0xFFFF5252)
                            )
                        )
                    }

                    Divider(color = GamingCardBorder, thickness = 0.5.dp)

                    // Full Games Only
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Full Games Only",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isNotificationsEnabled) TextPrimary else TextMuted
                            )
                            Text(
                                text = "Mute DLC packs, cosmetic skins, and beta keys",
                                fontSize = 11.sp,
                                color = if (isNotificationsEnabled) TextSecondary else TextMuted
                            )
                        }

                        Switch(
                            checked = isGamesOnly,
                            enabled = isNotificationsEnabled,
                            onCheckedChange = {
                                isGamesOnly = it
                                notificationHelper.isGamesOnly = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GamingDarkBackground,
                                checkedTrackColor = NeonCyan
                            )
                        )
                    }

                    Divider(color = GamingCardBorder, thickness = 0.5.dp)

                    // Keep Forever Games Only
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Keep-Forever Games Only",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isNotificationsEnabled) TextPrimary else TextMuted
                            )
                            Text(
                                text = "Filter out temporary free weekends and trial promotions",
                                fontSize = 11.sp,
                                color = if (isNotificationsEnabled) TextSecondary else TextMuted
                            )
                        }

                        Switch(
                            checked = isKeepForeverOnly,
                            enabled = isNotificationsEnabled,
                            onCheckedChange = {
                                isKeepForeverOnly = it
                                notificationHelper.isKeepForeverOnly = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GamingDarkBackground,
                                checkedTrackColor = NeonCyan
                            )
                        )
                    }

                    Divider(color = GamingCardBorder, thickness = 0.5.dp)

                    // Sound Effects
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
                                color = if (isNotificationsEnabled) TextPrimary else TextMuted
                            )
                            Text(
                                text = "Play audio chime when new giveaways drop",
                                fontSize = 11.sp,
                                color = if (isNotificationsEnabled) TextSecondary else TextMuted
                            )
                        }

                        Switch(
                            checked = isSoundEnabled,
                            enabled = isNotificationsEnabled,
                            onCheckedChange = {
                                isSoundEnabled = it
                                notificationHelper.isNotificationSoundEnabled = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GamingDarkBackground,
                                checkedTrackColor = NeonCyan
                            )
                        )
                    }

                    Divider(color = GamingCardBorder, thickness = 0.5.dp)

                    // Haptic Vibration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Haptic Vibration Feedback",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isNotificationsEnabled) TextPrimary else TextMuted
                            )
                            Text(
                                text = "Vibrate on new free game arrival",
                                fontSize = 11.sp,
                                color = if (isNotificationsEnabled) TextSecondary else TextMuted
                            )
                        }

                        Switch(
                            checked = isVibrateEnabled,
                            enabled = isNotificationsEnabled,
                            onCheckedChange = {
                                isVibrateEnabled = it
                                notificationHelper.isNotificationVibrateEnabled = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GamingDarkBackground,
                                checkedTrackColor = NeonCyan
                            )
                        )
                    }

                    Divider(color = GamingCardBorder, thickness = 0.5.dp)

                    // Rich Artwork
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Rich Game Poster Artwork",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isNotificationsEnabled) TextPrimary else TextMuted
                            )
                            Text(
                                text = "Display BigPicture artwork inside notifications",
                                fontSize = 11.sp,
                                color = if (isNotificationsEnabled) TextSecondary else TextMuted
                            )
                        }

                        Switch(
                            checked = isRichArtEnabled,
                            enabled = isNotificationsEnabled,
                            onCheckedChange = {
                                isRichArtEnabled = it
                                notificationHelper.isNotificationRichArtwork = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GamingDarkBackground,
                                checkedTrackColor = NeonCyan
                            )
                        )
                    }

                    Divider(color = GamingCardBorder, thickness = 0.5.dp)

                    // Action Buttons
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
                                color = if (isNotificationsEnabled) TextPrimary else TextMuted
                            )
                            Text(
                                text = "Include 'Claim Free', '★ Bookmark', and 'Share' buttons in alerts",
                                fontSize = 11.sp,
                                color = if (isNotificationsEnabled) TextSecondary else TextMuted
                            )
                        }

                        Switch(
                            checked = isActionsEnabled,
                            enabled = isNotificationsEnabled,
                            onCheckedChange = {
                                isActionsEnabled = it
                                notificationHelper.isNotificationActionsEnabled = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GamingDarkBackground,
                                checkedTrackColor = NeonCyan
                            )
                        )
                    }

                    Divider(color = GamingCardBorder, thickness = 0.5.dp)

                    // Minimum Value Filter
                    Column {
                        Text(
                            text = "Minimum Original Game Value:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isNotificationsEnabled) TextPrimary else TextMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                0f to "Any Value",
                                5f to "Worth $5+",
                                10f to "Worth $10+",
                                20f to "Worth $20+",
                                30f to "Worth $30+ (AAA)"
                            ).forEach { (threshold, label) ->
                                val selected = minWorthThreshold == threshold
                                FilterChip(
                                    selected = selected,
                                    enabled = isNotificationsEnabled,
                                    onClick = {
                                        minWorthThreshold = threshold
                                        notificationHelper.minWorthAlertThreshold = threshold
                                    },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonGreen,
                                        selectedLabelColor = Color.Black,
                                        containerColor = Color(0xFF141E2F),
                                        labelColor = TextSecondary
                                    )
                                )
                            }
                        }
                    }

                    Divider(color = GamingCardBorder, thickness = 0.5.dp)

                    // Quiet Hours (Do Not Disturb)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Nightlight, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Quiet Hours (11 PM - 8 AM)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isNotificationsEnabled) TextPrimary else TextMuted
                                )
                            }
                            Text(
                                text = "Silence alert sounds & notifications while you sleep",
                                fontSize = 11.sp,
                                color = if (isNotificationsEnabled) TextSecondary else TextMuted
                            )
                        }

                        Switch(
                            checked = isQuietHours,
                            enabled = isNotificationsEnabled,
                            onCheckedChange = {
                                isQuietHours = it
                                notificationHelper.isQuietHoursEnabled = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GamingDarkBackground,
                                checkedTrackColor = NeonCyan
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Background Scanning & Frequency
            Text(
                text = "BACKGROUND SCANNER INTERVAL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(GamingDarkSurfaceVariant)
                    .border(1.dp, GamingCardBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Automatic Check Frequency:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isNotificationsEnabled) TextPrimary else TextMuted
                        )
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf(2 to "2 Hours", 4 to "4 Hours", 8 to "8 Hours", 24 to "Daily", 0 to "Manual")) { (hours, label) ->
                            val selected = syncIntervalHours == hours
                            FilterChip(
                                selected = selected,
                                enabled = isNotificationsEnabled,
                                onClick = {
                                    syncIntervalHours = hours
                                    appPrefs.backgroundSyncIntervalHours = hours
                                    NotificationScheduler.schedulePeriodicSync(context)
                                    Toast.makeText(context, "Background sync set to $label", Toast.LENGTH_SHORT).show()
                                },
                                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF141E2F),
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    Divider(color = GamingCardBorder, thickness = 0.5.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.Wifi, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Require Unmetered Wi-Fi",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isNotificationsEnabled) TextPrimary else TextMuted
                                )
                                Text(
                                    text = "Only check in background when connected to Wi-Fi",
                                    fontSize = 11.sp,
                                    color = if (isNotificationsEnabled) TextSecondary else TextMuted
                                )
                            }
                        }

                        Switch(
                            checked = wifiOnly,
                            enabled = isNotificationsEnabled,
                            onCheckedChange = {
                                wifiOnly = it
                                appPrefs.backgroundSyncWifiOnly = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GamingDarkBackground,
                                checkedTrackColor = NeonCyan
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Test & Inbox Action Buttons
            if (onOpenInbox != null) {
                Button(
                    onClick = {
                        onDismiss()
                        onOpenInbox()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("open_alert_inbox_from_settings_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Open Alert Inbox & History", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                        notificationHelper.sendTestNotification(isRichPreview = true)
                        Toast.makeText(context, "Rich BigPicture notification sent! Check drawer.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("send_test_notification_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Rich Game Alert", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                        notificationHelper.sendTestEndingSoonNotification()
                        Toast.makeText(context, "Ending Soon notification sent! Check drawer.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("send_test_ending_soon_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Ending Alert", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ChannelStatusBadge(name: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF101726))
            .border(0.8.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun NotificationSettingRow(
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
            .padding(vertical = 10.dp),
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
                    .clip(RoundedCornerShape(5.dp))
                    .background(color)
            )
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) TextPrimary else TextMuted
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = if (enabled) TextSecondary else TextMuted
                )
            }
        }

        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = GamingDarkBackground,
                checkedTrackColor = NeonCyan
            )
        )
    }
}
