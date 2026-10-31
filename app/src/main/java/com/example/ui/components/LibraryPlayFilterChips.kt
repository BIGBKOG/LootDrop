package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.LibraryPlayFilter
import com.example.data.model.LibraryViewMode
import com.example.data.model.PlayStatus
import com.example.data.model.PlayabilityFilter
import com.example.data.model.StorageFilter
import com.example.ui.UiStats
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LibraryPlayFilterChips(
    selectedFilter: LibraryPlayFilter,
    uiStats: UiStats,
    onFilterSelect: (LibraryPlayFilter) -> Unit,
    isSyncingSteam: Boolean,
    steamUserId: String,
    steamSyncStatus: String?,
    onOpenSteamDialog: () -> Unit,
    onQuickSteamSync: () -> Unit,
    onDismissStatus: () -> Unit,
    viewMode: LibraryViewMode = LibraryViewMode.VAULT,
    onViewModeChange: (LibraryViewMode) -> Unit = {},
    selectedPlayability: PlayabilityFilter = PlayabilityFilter.ALL,
    onPlayabilitySelect: (PlayabilityFilter) -> Unit = {},
    selectedStorage: StorageFilter = StorageFilter.ALL,
    onStorageSelect: (StorageFilter) -> Unit = {},
    trophyCount: Int = 0,
    onRollRandomBacklog: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Sub-Tab Segmented Selector: Claimed Vault vs All+Paid (Steam Library)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val isVaultSelected = viewMode == LibraryViewMode.VAULT
            val isAllPaidSelected = viewMode == LibraryViewMode.ALL_PAID
            val isAchievementsSelected = viewMode == LibraryViewMode.ACHIEVEMENTS

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isVaultSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onViewModeChange(LibraryViewMode.VAULT) }
                    .padding(vertical = 8.dp)
                    .testTag("tab_claimed_vault"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "🎁 Vault",
                        fontSize = 11.sp,
                        fontWeight = if (isVaultSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isVaultSelected) MaterialTheme.colorScheme.onPrimary else TextSecondary
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isVaultSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "${uiStats.claimedCount}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isVaultSelected) MaterialTheme.colorScheme.onPrimary else TextSecondary
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isAllPaidSelected) Color(0xFF66C0F4) else Color.Transparent)
                    .clickable { onViewModeChange(LibraryViewMode.ALL_PAID) }
                    .padding(vertical = 8.dp)
                    .testTag("tab_all_paid"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "🎮 All+Paid",
                        fontSize = 11.sp,
                        fontWeight = if (isAllPaidSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isAllPaidSelected) Color(0xFF0B0F19) else TextSecondary
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isAllPaidSelected) Color(0xFF0B0F19).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "${uiStats.steamTotalCount + uiStats.claimedCount}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAllPaidSelected) Color(0xFF0B0F19) else Color(0xFF66C0F4)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isAchievementsSelected) Color(0xFFE040FB) else Color.Transparent)
                    .clickable { onViewModeChange(LibraryViewMode.ACHIEVEMENTS) }
                    .padding(vertical = 8.dp)
                    .testTag("tab_trophies_showcase"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "🏆 Trophies",
                        fontSize = 11.sp,
                        fontWeight = if (isAchievementsSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isAchievementsSelected) Color(0xFF0F1420) else TextSecondary
                    )
                    if (trophyCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isAchievementsSelected) Color(0xFF0F1420).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "$trophyCount",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAchievementsSelected) Color(0xFF0F1420) else Color(0xFFE040FB)
                            )
                        }
                    }
                }
            }
        }

        // Steam Game Tracker banner
        SteamTrackerBanner(
            isSyncing = isSyncingSteam,
            steamUserId = steamUserId,
            onOpenDialog = onOpenSteamDialog,
            onQuickSync = onQuickSteamSync
        )


        // Sync feedback toast/banner if message is present
        AnimatedVisibility(
            visible = steamSyncStatus != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            steamSyncStatus?.let { status ->
                val isSuccess = status.contains("Synced", ignoreCase = true)
                val isError = status.contains("failed", ignoreCase = true) || status.contains("Could not", ignoreCase = true)
                val bannerBg = when {
                    isSuccess -> NeonGreen.copy(alpha = 0.15f)
                    isError -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                val bannerBorder = when {
                    isSuccess -> NeonGreen
                    isError -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                }
                val textColor = when {
                    isSuccess -> NeonGreen
                    isError -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(bannerBg)
                        .border(1.dp, bannerBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isSyncingSteam) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = if (isSuccess) Icons.Default.Check else if (isError) Icons.Default.Close else Icons.Default.Sync,
                                contentDescription = null,
                                tint = textColor,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Text(
                            text = status,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = textColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = onDismissStatus,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = textColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        if (viewMode != LibraryViewMode.ACHIEVEMENTS) {
            var showFilterMenuDialog by remember { mutableStateOf(false) }

            val isFilterActive = selectedFilter != LibraryPlayFilter.ALL ||
                    selectedPlayability != PlayabilityFilter.ALL ||
                    selectedStorage != StorageFilter.ALL

            val activeFilterCount = (if (selectedFilter != LibraryPlayFilter.ALL) 1 else 0) +
                    (if (selectedPlayability != PlayabilityFilter.ALL) 1 else 0) +
                    (if (selectedStorage != StorageFilter.ALL) 1 else 0)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Integrated Filter Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isFilterActive) NeonCyan.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            width = 1.dp,
                            color = if (isFilterActive) NeonCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { showFilterMenuDialog = true }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                        .testTag("vault_filter_menu_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filters",
                            tint = if (isFilterActive) NeonCyan else TextPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (viewMode == LibraryViewMode.ALL_PAID) "Steam Filters" else "Vault Filters",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFilterActive) NeonCyan else TextPrimary
                        )
                        if (activeFilterCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(NeonCyan)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "$activeFilterCount",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0F1420)
                                )
                            }
                        }
                    }
                }

                // Roll Random Backlog Button in All+Paid mode to beat analysis paralysis
                if (viewMode == LibraryViewMode.ALL_PAID && onRollRandomBacklog != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF261E38))
                            .border(
                                width = 1.dp,
                                color = Color(0xFFA855F7).copy(alpha = 0.6f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onRollRandomBacklog() }
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                            .testTag("roll_random_backlog_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🎲", fontSize = 12.sp)
                            Text(
                                text = "Roll Backlog",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD8B4FE)
                            )
                        }
                    }
                }

                // Active filter tags horizontally scrollable with tap-to-remove
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedFilter != LibraryPlayFilter.ALL) {
                        ActiveFilterPill(
                            label = selectedFilter.displayName,
                            onRemove = { onFilterSelect(LibraryPlayFilter.ALL) }
                        )
                    }
                    if (selectedPlayability != PlayabilityFilter.ALL) {
                        ActiveFilterPill(
                            label = "${selectedPlayability.iconEmoji} ${selectedPlayability.label}",
                            onRemove = { onPlayabilitySelect(PlayabilityFilter.ALL) }
                        )
                    }
                    if (selectedStorage != StorageFilter.ALL) {
                        ActiveFilterPill(
                            label = "💾 ${selectedStorage.label}",
                            onRemove = { onStorageSelect(StorageFilter.ALL) }
                        )
                    }

                    if (isFilterActive) {
                        Text(
                            text = "Reset",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    onFilterSelect(LibraryPlayFilter.ALL)
                                    onPlayabilitySelect(PlayabilityFilter.ALL)
                                    onStorageSelect(StorageFilter.ALL)
                                }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                .testTag("vault_filter_quick_reset")
                        )
                    } else {
                        Text(
                            text = if (viewMode == LibraryViewMode.ALL_PAID) "All ${uiStats.steamTotalCount + uiStats.claimedCount} games • Vault & Steam • Tap filters" else "All ${uiStats.claimedCount} claimed games • Tap filters",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (showFilterMenuDialog) {
                VaultFilterMenuDialog(
                    selectedFilter = selectedFilter,
                    onFilterSelect = onFilterSelect,
                    selectedPlayability = selectedPlayability,
                    onPlayabilitySelect = onPlayabilitySelect,
                    selectedStorage = selectedStorage,
                    onStorageSelect = onStorageSelect,
                    uiStats = uiStats,
                    viewMode = viewMode,
                    onDismiss = { showFilterMenuDialog = false }
                )
            }
        }
    }
}

@Composable
private fun ActiveFilterPill(
    label: String,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1B2436))
            .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .clickable { onRemove() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Remove filter",
            tint = NeonCyan,
            modifier = Modifier.size(12.dp)
        )
    }
}

@Composable
private fun SteamTrackerBanner(
    isSyncing: Boolean,
    steamUserId: String,
    onOpenDialog: () -> Unit,
    onQuickSync: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "steam_spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF171A21)) // Steam brand dark slate
            .border(1.dp, Color(0xFF2A475E), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onOpenDialog),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Steam icon badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1B2838))
                    .border(1.dp, Color(0xFF66C0F4).copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = "Steam",
                    tint = Color(0xFF66C0F4), // Steam light blue
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Steam Game Tracker",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF66C0F4).copy(alpha = 0.2f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = if (steamUserId.isNotBlank()) "LINKED" else "STEAM API",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF66C0F4)
                        )
                    }
                }
                Text(
                    text = if (steamUserId.isNotBlank()) {
                        "ID: $steamUserId • Auto-categorize by playtime"
                    } else {
                        "Tap to link Steam ID & auto-sort library"
                    },
                    fontSize = 11.sp,
                    color = Color(0xFFC7D5E0),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Quick sync button
            IconButton(
                onClick = {
                    if (steamUserId.isNotBlank()) {
                        onQuickSync()
                    } else {
                        onOpenDialog()
                    }
                },
                enabled = !isSyncing,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A475E).copy(alpha = 0.6f))
                    .testTag("steam_quick_sync_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Sync Steam",
                    tint = if (isSyncing) NeonCyan else Color.White,
                    modifier = Modifier
                        .size(16.dp)
                        .then(if (isSyncing) Modifier.rotate(spinAngle) else Modifier)
                )
            }

            // Configure / settings button
            IconButton(
                onClick = onOpenDialog,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A475E).copy(alpha = 0.6f))
                    .testTag("steam_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Steam Settings",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun SteamSyncDialog(
    currentUserId: String,
    currentApiKey: String,
    isSyncing: Boolean,
    statusMessage: String?,
    onDismiss: () -> Unit,
    onSync: (userId: String, apiKey: String) -> Unit
) {
    var userIdInput by remember(currentUserId) { mutableStateOf(currentUserId) }
    var apiKeyInput by remember(currentApiKey) { mutableStateOf(currentApiKey) }
    var showAdvancedKey by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.2.dp, Color(0xFF66C0F4).copy(alpha = 0.7f), RoundedCornerShape(20.dp)),
            color = Color(0xFF171A21),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1B2838))
                                .border(1.dp, Color(0xFF66C0F4), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = Color(0xFF66C0F4),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Steam Game Tracker",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Auto-organize your claimed vault",
                                fontSize = 12.sp,
                                color = Color(0xFF8F98A0)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF8F98A0)
                        )
                    }
                }

                // Description card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1B2838))
                        .border(1.dp, Color(0xFF2A475E), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "How Auto-Categorization Works:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF66C0F4)
                        )
                        Text(
                            text = "• 0 min played ➔ Unplayed (Backlog)\n• >0 min played ➔ Playing\n• 100% achievements or 30+ hrs ➔ Finished",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFFC7D5E0)
                        )
                    }
                }

                // Steam User ID / Vanity URL Input
                OutlinedTextField(
                    value = userIdInput,
                    onValueChange = { userIdInput = it },
                    label = { Text("Steam ID or Vanity Username", color = Color(0xFF8F98A0)) },
                    placeholder = { Text("e.g. gaben or 76561198000000000", color = Color(0xFF667382)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF66C0F4)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("steam_user_id_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF66C0F4),
                        unfocusedBorderColor = Color(0xFF2A475E),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF0E141B),
                        unfocusedContainerColor = Color(0xFF0E141B)
                    )
                )

                Text(
                    text = "Tip: Found in your profile URL (steamcommunity.com/id/YOUR_NAME or /profiles/YOUR_ID64) or lookup at steamid64.net. Make sure 'Game details' is Public in Steam Privacy Settings.",
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    color = Color(0xFF8F98A0)
                )

                // Advanced API Key toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAdvancedKey = !showAdvancedKey },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (showAdvancedKey) "Hide Custom API Key ▴" else "Custom Steam API Key (Optional) ▾",
                        fontSize = 11.sp,
                        color = Color(0xFF66C0F4),
                        fontWeight = FontWeight.Medium
                    )
                }

                AnimatedVisibility(visible = showAdvancedKey) {
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text("Custom Steam Web API Key", color = Color(0xFF8F98A0)) },
                        placeholder = { Text("Leave blank to use pre-configured key", color = Color(0xFF667382)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = Color(0xFF66C0F4)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("steam_custom_api_key_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF66C0F4),
                            unfocusedBorderColor = Color(0xFF2A475E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF0E141B),
                            unfocusedContainerColor = Color(0xFF0E141B)
                        )
                    )
                }

                // Sync status message inside dialog
                if (statusMessage != null) {
                    Text(
                        text = statusMessage,
                        fontSize = 11.sp,
                        color = if (statusMessage.contains("failed", ignoreCase = true)) MaterialTheme.colorScheme.error else NeonGreen,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC7D5E0)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A475E))
                    ) {
                        Text("Close")
                    }

                    Button(
                        onClick = {
                            onSync(userIdInput, apiKeyInput)
                        },
                        enabled = !isSyncing && userIdInput.isNotBlank(),
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("steam_sync_submit_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF66C0F4),
                            contentColor = Color(0xFF0B0F19),
                            disabledContainerColor = Color(0xFF2A475E),
                            disabledContentColor = Color(0xFF8F98A0)
                        )
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF0B0F19)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Syncing...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sync Library", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
