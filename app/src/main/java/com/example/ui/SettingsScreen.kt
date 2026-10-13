package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.LauncherType
import com.example.preferences.AccentTheme
import com.example.preferences.BackgroundTheme
import com.example.ui.theme.EpicGamesBlue
import com.example.ui.theme.GamingCardBorder
import com.example.ui.theme.GamingDarkBackground
import com.example.ui.theme.GamingDarkSurface
import com.example.ui.theme.GamingDarkSurfaceVariant
import com.example.ui.theme.GogPurple
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SteamBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    viewModel: GiveawaysViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accentTheme by viewModel.accentTheme.collectAsState()
    val backgroundTheme by viewModel.backgroundTheme.collectAsState()
    val isCompactCards by viewModel.isCompactCards.collectAsState()
    val defaultLauncher by viewModel.defaultLauncher.collectAsState()

    val isNotificationsEnabled by viewModel.isNotificationsEnabled.collectAsState()
    val isNotifySteam by viewModel.isNotifySteam.collectAsState()
    val isNotifyEpic by viewModel.isNotifyEpic.collectAsState()
    val isNotifyGog by viewModel.isNotifyGog.collectAsState()
    val isNotifyOthers by viewModel.isNotifyOthers.collectAsState()
    val isGamesOnly by viewModel.isGamesOnly.collectAsState()

    var showClearClaimedDialog by remember { mutableStateOf(false) }
    var showClearBookmarksDialog by remember { mutableStateOf(false) }
    var showResyncDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Notification permission granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permission denied. Enable in device settings.", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("settings_screen")
    ) {
        // Section: Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = "Settings & Customization",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Personalize colors, push alerts, and launcher preferences",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION 1: Accent Color Themes
        SectionHeader(title = "ACCENT COLOR THEME", icon = Icons.Default.Palette)

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Choose your neon gaming color palette:",
                fontSize = 13.sp,
                color = TextSecondary
            )

            AccentTheme.values().forEach { theme ->
                val isSelected = theme == accentTheme
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) theme.primaryColor.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) theme.primaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.setAccentTheme(theme) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("accent_theme_${theme.name.lowercase()}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Two-tone color dot preview
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(theme.primaryColor)
                            )
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(theme.secondaryColor)
                            )
                        }

                        Column {
                            Text(
                                text = theme.title,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) theme.primaryColor else TextPrimary
                            )
                            Text(
                                text = theme.description,
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(theme.primaryColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.background,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 2: Background Dark Theme
        SectionHeader(title = "DARK THEME STYLE", icon = Icons.Default.ColorLens)

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BackgroundTheme.values().forEach { bg ->
                val isSelected = bg == backgroundTheme
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.setBackgroundTheme(bg) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("bg_theme_${bg.name.lowercase()}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(bg.backgroundColor)
                                .border(1.dp, bg.borderColor, RoundedCornerShape(6.dp))
                        )
                        Text(
                            text = bg.title,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 3: Card Layout Density
        SectionHeader(title = "LAYOUT & VIEW", icon = Icons.Default.ViewAgenda)

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Compact Card Layout",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Show streamlined cards to fit more freebies on screen",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = isCompactCards,
                    onCheckedChange = { viewModel.setCompactCards(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.background,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("compact_cards_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 4: Push Notifications & Alerts (MOVED HERE)
        SectionHeader(title = "PUSH NOTIFICATIONS & FREEBIE ALERTS", icon = Icons.Default.NotificationsActive)

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Master Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "New Free Game Alerts",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Notify immediately when new freebies are discovered",
                        fontSize = 12.sp,
                        color = TextSecondary
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

            Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

            Text(
                text = "Store Launchers to Notify:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )

            // Launcher switches
            SettingToggleRow(
                title = "Steam Freebies",
                subtitle = "Steam store drops & 100% discount keys",
                color = SteamBlue,
                checked = isNotifySteam,
                enabled = isNotificationsEnabled,
                onCheckedChange = { viewModel.setNotifySteam(it) }
            )

            SettingToggleRow(
                title = "Epic Games Store",
                subtitle = "Weekly free games & vault specials",
                color = EpicGamesBlue,
                checked = isNotifyEpic,
                enabled = isNotificationsEnabled,
                onCheckedChange = { viewModel.setNotifyEpic(it) }
            )

            SettingToggleRow(
                title = "GOG.com",
                subtitle = "DRM-free classic and indie giveaways",
                color = GogPurple,
                checked = isNotifyGog,
                enabled = isNotificationsEnabled,
                onCheckedChange = { viewModel.setNotifyGog(it) }
            )

            SettingToggleRow(
                title = "Other PC Launchers",
                subtitle = "Ubisoft, itch.io, EA App, Prime Gaming",
                color = NeonGreen,
                checked = isNotifyOthers,
                enabled = isNotificationsEnabled,
                onCheckedChange = { viewModel.setNotifyOthers(it) }
            )

            Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

            // Games only switch
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
                        text = "Do not notify for DLC, add-ons, or beta keys",
                        fontSize = 11.sp,
                        color = if (isNotificationsEnabled) TextSecondary else TextMuted
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

            Spacer(modifier = Modifier.height(4.dp))

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
                    viewModel.sendTestNotification()
                    Toast.makeText(context, "Test alert sent! Check your notifications bar.", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("settings_test_alert_button"),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Send Test Notification Now", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 5: Default Launcher Filter
        SectionHeader(title = "DEFAULT LAUNCHER ON STARTUP", icon = Icons.Default.SportsEsports)

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Select which store to open automatically:",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                listOf(LauncherType.ALL, LauncherType.STEAM, LauncherType.EPIC, LauncherType.GOG).forEach { launcher ->
                    val isSelected = launcher == defaultLauncher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
                            .border(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.setDefaultLauncher(launcher) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = launcher.displayName,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else TextPrimary
                        )
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 6: Library & Data Reset
        SectionHeader(title = "DATA & LIBRARY MANAGEMENT", icon = Icons.Default.Delete)

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { showResyncDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Force Re-sync All Freebies from Store APIs", fontSize = 13.sp)
                }
            }

            OutlinedButton(
                onClick = { showClearClaimedDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text("Clear 'In Library' Claimed Games", fontSize = 13.sp)
            }

            OutlinedButton(
                onClick = { showClearBookmarksDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text("Clear Saved Bookmarks", fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // App Info Footer
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "LootDrop v1.1 • PC Free Games Radar",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Real-time giveaways data powered by GamerPower API",
                fontSize = 11.sp,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // Confirmation Dialogs
    if (showClearClaimedDialog) {
        AlertDialog(
            onDismissRequest = { showClearClaimedDialog = false },
            title = { Text("Clear Claimed Games?") },
            text = { Text("This will remove all games from your 'In Library' tab. You can still re-mark them at any time.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearClaimedGames()
                        showClearClaimedDialog = false
                        Toast.makeText(context, "Claimed library cleared", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearClaimedDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showClearBookmarksDialog) {
        AlertDialog(
            onDismissRequest = { showClearBookmarksDialog = false },
            title = { Text("Clear Saved Bookmarks?") },
            text = { Text("This will remove all saved games from your bookmarks tab.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearBookmarkedGames()
                        showClearBookmarksDialog = false
                        Toast.makeText(context, "Bookmarks cleared", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearBookmarksDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showResyncDialog) {
        AlertDialog(
            onDismissRequest = { showResyncDialog = false },
            title = { Text("Re-sync Store Freebies?") },
            text = { Text("This refreshes all giveaways directly from Steam, Epic, GOG, and other launchers.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.forceResync()
                        showResyncDialog = false
                        Toast.makeText(context, "Refreshing all giveaways...", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Re-sync", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResyncDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun SettingToggleRow(
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
            .padding(vertical = 6.dp),
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
                checkedThumbColor = MaterialTheme.colorScheme.background,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
