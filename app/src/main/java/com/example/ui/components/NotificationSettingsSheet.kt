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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isNotificationsEnabled by remember { mutableStateOf(notificationHelper.isNotificationsEnabled) }
    var isNotifyPlayStation by remember { mutableStateOf(notificationHelper.isNotifyPlayStation) }
    var isNotifyXbox by remember { mutableStateOf(notificationHelper.isNotifyXbox) }
    var isNotifyNintendo by remember { mutableStateOf(notificationHelper.isNotifyNintendo) }
    var isNotifySteam by remember { mutableStateOf(notificationHelper.isNotifySteam) }
    var isNotifyEpic by remember { mutableStateOf(notificationHelper.isNotifyEpic) }
    var isNotifyGog by remember { mutableStateOf(notificationHelper.isNotifyGog) }
    var isNotifyOthers by remember { mutableStateOf(notificationHelper.isNotifyOthers) }
    var isGamesOnly by remember { mutableStateOf(notificationHelper.isGamesOnly) }

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
                .padding(20.dp)
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
                            .size(36.dp)
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
                            text = "Free Game Alerts",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Get notified when new games drop",
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
                            text = "Enable Push Alerts",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Receive notifications when new giveaways appear",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Switch(
                        checked = isNotificationsEnabled,
                        onCheckedChange = { checked ->
                            isNotificationsEnabled = checked
                            notificationHelper.isNotificationsEnabled = checked
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
                            checkedThumbColor = GamingDarkBackground,
                            checkedTrackColor = NeonCyan,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = Color(0xFF1E283C)
                        ),
                        modifier = Modifier.testTag("master_notification_switch")
                    )
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

            // Launcher switches in a card
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
                        title = "Xbox & Microsoft Store",
                        subtitle = "Xbox Series X|S, Xbox One & Windows Store",
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
                        subtitle = "Nintendo Switch 100% off deals & freebies",
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
                        title = "Steam Freebies",
                        subtitle = "Steam store giveaways & 100% discount keys",
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
                        subtitle = "Weekly free games & holiday vaults",
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
                        subtitle = "DRM-free classic & indie giveaways",
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
                        title = "Other PC Stores",
                        subtitle = "Ubisoft, itch.io, Prime, EA App",
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

            // Preference: Full Games Only
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
                            text = "Full Games Only",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Ignore DLC, add-on packs, and beta keys",
                            fontSize = 12.sp,
                            color = TextSecondary
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
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Test Alert Button
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
                    notificationHelper.sendTestNotification()
                    Toast.makeText(context, "Test notification dispatched!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("send_test_notification_button"),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Send Test Alert Now",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
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
