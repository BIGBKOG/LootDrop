package com.example.ui.components

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.LibraryPlayFilter
import com.example.data.model.LibraryViewMode
import com.example.data.model.PlayabilityFilter
import com.example.data.model.StorageFilter
import com.example.ui.UiStats
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Integrated single-menu filter dialog for the Vault / In-Library tab.
 * Consolidates Play Status, Hardware Readiness (Steam Deck/Controller), and Storage Footprint
 * into a single unified menu to eliminate cluttered multi-row chips.
 */
@Composable
fun VaultFilterMenuDialog(
    selectedFilter: LibraryPlayFilter,
    onFilterSelect: (LibraryPlayFilter) -> Unit,
    selectedPlayability: PlayabilityFilter,
    onPlayabilitySelect: (PlayabilityFilter) -> Unit,
    selectedStorage: StorageFilter,
    onStorageSelect: (StorageFilter) -> Unit,
    uiStats: UiStats,
    viewMode: LibraryViewMode,
    onDismiss: () -> Unit
) {
    val isAllPaid = viewMode == LibraryViewMode.ALL_PAID
    val isAnyFilterActive = selectedFilter != LibraryPlayFilter.ALL ||
            selectedPlayability != PlayabilityFilter.ALL ||
            selectedStorage != StorageFilter.ALL

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 520.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = 0.6f),
                            NeonGreen.copy(alpha = 0.3f),
                            Color(0xFF1E293B)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .testTag("vault_filter_menu_dialog"),
            color = Color(0xFF0F1523),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonCyan.copy(alpha = 0.15f))
                                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isAllPaid) "Steam Library Filters" else "Vault Filters",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Filter by status, hardware & storage size",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isAnyFilterActive) {
                            TextButton(
                                onClick = {
                                    onFilterSelect(LibraryPlayFilter.ALL)
                                    onPlayabilitySelect(PlayabilityFilter.ALL)
                                    onStorageSelect(StorageFilter.ALL)
                                },
                                modifier = Modifier.testTag("vault_filter_reset_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable filter categories
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Category 1: Play Status
                    FilterCategorySection(
                        title = "🎮 PLAY STATUS",
                        color = NeonCyan
                    ) {
                        FlowChipRow {
                            LibraryPlayFilter.values().forEach { filter ->
                                val isSelected = filter == selectedFilter
                                val count = if (isAllPaid) {
                                    when (filter) {
                                        LibraryPlayFilter.ALL -> uiStats.steamTotalCount
                                        LibraryPlayFilter.UNPLAYED -> uiStats.steamUnplayedCount
                                        LibraryPlayFilter.PLAYING -> uiStats.steamPlayingCount
                                        LibraryPlayFilter.FINISHED -> uiStats.steamFinishedCount
                                        LibraryPlayFilter.DROPPED -> uiStats.steamDroppedCount
                                    }
                                } else {
                                    when (filter) {
                                        LibraryPlayFilter.ALL -> uiStats.claimedCount
                                        LibraryPlayFilter.UNPLAYED -> uiStats.unplayedCount
                                        LibraryPlayFilter.PLAYING -> uiStats.playingCount
                                        LibraryPlayFilter.FINISHED -> uiStats.finishedCount
                                        LibraryPlayFilter.DROPPED -> uiStats.droppedCount
                                    }
                                }

                                val chipColor = when (filter) {
                                    LibraryPlayFilter.ALL -> MaterialTheme.colorScheme.primary
                                    LibraryPlayFilter.UNPLAYED -> Color(0xFF94A3B8)
                                    LibraryPlayFilter.PLAYING -> NeonCyan
                                    LibraryPlayFilter.FINISHED -> NeonGreen
                                    LibraryPlayFilter.DROPPED -> Color(0xFFF97316)
                                }

                                SelectablePill(
                                    label = filter.displayName,
                                    count = count,
                                    isSelected = isSelected,
                                    activeColor = chipColor,
                                    onClick = { onFilterSelect(filter) },
                                    testTag = "vault_menu_status_${filter.name.lowercase()}"
                                )
                            }
                        }
                    }

                    // Category 2: Hardware Readiness & Steam Deck
                    FilterCategorySection(
                        title = "⚡ HARDWARE & PLAYABILITY",
                        color = NeonGreen
                    ) {
                        FlowChipRow {
                            PlayabilityFilter.values().forEach { playTag ->
                                val isSelected = playTag == selectedPlayability
                                val tagColor = when (playTag) {
                                    PlayabilityFilter.ALL -> MaterialTheme.colorScheme.primary
                                    PlayabilityFilter.DECK_VERIFIED -> Color(0xFF10B981)
                                    PlayabilityFilter.CONTROLLER_FRIENDLY -> Color(0xFF00E5FF)
                                    PlayabilityFilter.QUICK_SESSION -> Color(0xFFFFD700)
                                }
                                val count = when (playTag) {
                                    PlayabilityFilter.ALL -> null
                                    PlayabilityFilter.DECK_VERIFIED -> uiStats.deckVerifiedCount
                                    PlayabilityFilter.CONTROLLER_FRIENDLY -> uiStats.controllerFriendlyCount
                                    PlayabilityFilter.QUICK_SESSION -> uiStats.quickSessionCount
                                }

                                SelectablePill(
                                    label = "${playTag.iconEmoji} ${playTag.label}",
                                    count = count,
                                    isSelected = isSelected,
                                    activeColor = tagColor,
                                    onClick = { onPlayabilitySelect(playTag) },
                                    testTag = "vault_menu_playability_${playTag.name.lowercase()}"
                                )
                            }
                        }
                    }

                    // Category 3: Storage Footprint Tracker
                    FilterCategorySection(
                        title = "💾 STORAGE FOOTPRINT",
                        color = Color(0xFFFFB300)
                    ) {
                        FlowChipRow {
                            StorageFilter.values().forEach { storageTag ->
                                val isSelected = storageTag == selectedStorage
                                val storageColor = when (storageTag) {
                                    StorageFilter.ALL -> MaterialTheme.colorScheme.secondary
                                    StorageFilter.UNDER_2GB -> Color(0xFF10B981)
                                    StorageFilter.FROM_2_TO_10GB -> Color(0xFF00E5FF)
                                    StorageFilter.FROM_10_TO_40GB -> Color(0xFFFFB300)
                                    StorageFilter.OVER_40GB -> Color(0xFFFF5722)
                                }
                                val count = when (storageTag) {
                                    StorageFilter.ALL -> null
                                    StorageFilter.UNDER_2GB -> uiStats.storageUnder2GbCount
                                    StorageFilter.FROM_2_TO_10GB -> uiStats.storage2To10GbCount
                                    StorageFilter.FROM_10_TO_40GB -> uiStats.storage10To40GbCount
                                    StorageFilter.OVER_40GB -> uiStats.storageOver40GbCount
                                }

                                SelectablePill(
                                    label = "💾 ${storageTag.label}",
                                    count = count,
                                    isSelected = isSelected,
                                    activeColor = storageColor,
                                    onClick = { onStorageSelect(storageTag) },
                                    testTag = "vault_menu_storage_${storageTag.name.lowercase()}"
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Apply Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("vault_filter_apply_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = Color(0xFF0B0F19)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (isAnyFilterActive) "Apply Filters" else "Close",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterCategorySection(
    title: String,
    color: Color,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color,
            letterSpacing = 0.8.sp
        )
        content()
    }
}

@Composable
private fun SelectablePill(
    label: String,
    count: Int?,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
                if (count != null && count > 0) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (isSelected) activeColor.copy(alpha = 0.25f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "$count",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) activeColor else TextSecondary
                        )
                    }
                }
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = activeColor.copy(alpha = 0.2f),
            selectedLabelColor = activeColor,
            containerColor = Color(0xFF161F30),
            labelColor = TextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = Color(0xFF26334D),
            selectedBorderColor = activeColor,
            borderWidth = 1.dp,
            selectedBorderWidth = 1.5.dp
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag(testTag)
    )
}

/**
 * Responsive flex flow row for filter chips.
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun FlowChipRow(
    content: @Composable () -> Unit
) {
    // Standard horizontal flow using Row with wrap behavior or horizontal scroll
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        content()
    }
}
