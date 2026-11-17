package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LibraryPlayFilter
import com.example.data.model.LibraryViewMode
import com.example.data.model.PlayabilityFilter
import com.example.data.model.StorageFilter
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.i18n.localizedLabel
import com.example.ui.UiStats
import com.example.ui.theme.ExpressiveMotion
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.expressivePressScale
import com.example.ui.theme.rememberExpressiveCornerRadius
import kotlin.math.abs

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
    dlcRecommendationsCount: Int = 0,
    onOpenDlcHub: (() -> Unit)? = null,
    onRollRandomBacklog: (() -> Unit)? = null,
    onOpenBacklogChallenge: (() -> Unit)? = null,
    isAnimationsEnabled: Boolean = true,
    isLowEndOptimization: Boolean = false,
    isCompactHeader: Boolean = false,
    onToggleCompactHeader: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    val reducedMotion = !isAnimationsEnabled || isLowEndOptimization

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val selectedModeIndex = when (viewMode) {
            LibraryViewMode.VAULT -> 0
            LibraryViewMode.ALL_PAID -> 1
            LibraryViewMode.ACHIEVEMENTS -> 2
        }
        val animatedModeIndex by animateFloatAsState(
            targetValue = selectedModeIndex.toFloat(),
            animationSpec = ExpressiveMotion.spatialDefault(reducedMotion),
            label = "library_mode_slider_idx"
        )
        val glideDelta = abs(selectedModeIndex.toFloat() - animatedModeIndex).coerceIn(0f, 1f)
        val targetIndicatorColor = when (viewMode) {
            LibraryViewMode.VAULT -> MaterialTheme.colorScheme.primary
            LibraryViewMode.ALL_PAID -> Color(0xFF66C0F4)
            LibraryViewMode.ACHIEVEMENTS -> Color(0xFFE040FB)
        }
        val animatedIndicatorColor by animateColorAsState(
            targetValue = targetIndicatorColor,
            animationSpec = ExpressiveMotion.effectsDefault(reducedMotion),
            label = "library_mode_indicator_color"
        )
        val indicatorCorner by animateDpAsState(
            targetValue = if (glideDelta > 0.08f) 14.dp else 9.dp,
            animationSpec = ExpressiveMotion.spatialFast(reducedMotion),
            label = "library_mode_indicator_corner"
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                .padding(4.dp)
        ) {
            val segmentWidth = maxWidth / 3
            Box(
                modifier = Modifier
                    .offset(x = segmentWidth * animatedModeIndex)
                    .width(segmentWidth)
                    .fillMaxHeight()
                    .graphicsLayer {
                        if (!reducedMotion && glideDelta > 0.01f) {
                            scaleX = 1f + glideDelta * 0.10f
                            scaleY = 1f - glideDelta * 0.05f
                        }
                    }
                    .clip(RoundedCornerShape(indicatorCorner))
                    .background(animatedIndicatorColor)
            )

            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                LibrarySegmentItem(
                    label = s.library.segmentVault,
                    count = uiStats.claimedCount,
                    isSelected = viewMode == LibraryViewMode.VAULT,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                    selectedBadgeBg = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                    unselectedBadgeTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    reducedMotion = reducedMotion,
                    testTag = "tab_claimed_vault",
                    modifier = Modifier.weight(1f),
                    onClick = { onViewModeChange(LibraryViewMode.VAULT) }
                )
                LibrarySegmentItem(
                    label = s.library.segmentAllPaid,
                    count = uiStats.steamTotalCount + uiStats.claimedCount,
                    isSelected = viewMode == LibraryViewMode.ALL_PAID,
                    selectedTextColor = Color(0xFF0B0F19),
                    selectedBadgeBg = Color(0xFF0B0F19).copy(alpha = 0.15f),
                    unselectedBadgeTextColor = Color(0xFF66C0F4),
                    reducedMotion = reducedMotion,
                    testTag = "tab_all_paid",
                    modifier = Modifier.weight(1f),
                    onClick = { onViewModeChange(LibraryViewMode.ALL_PAID) }
                )
                LibrarySegmentItem(
                    label = s.library.segmentTrophies,
                    count = if (trophyCount > 0) trophyCount else null,
                    isSelected = viewMode == LibraryViewMode.ACHIEVEMENTS,
                    selectedTextColor = Color(0xFF0F1420),
                    selectedBadgeBg = Color(0xFF0F1420).copy(alpha = 0.2f),
                    unselectedBadgeTextColor = Color(0xFFE040FB),
                    reducedMotion = reducedMotion,
                    testTag = "tab_trophies_showcase",
                    modifier = Modifier.weight(1f),
                    onClick = { onViewModeChange(LibraryViewMode.ACHIEVEMENTS) }
                )
            }
        }

        AnimatedVisibility(
            visible = !isCompactHeader,
            enter = ExpressiveMotion.expressiveExpandEnter(reducedMotion),
            exit = ExpressiveMotion.expressiveCollapseExit(reducedMotion)
        ) {
            SteamTrackerBanner(
                isSyncing = isSyncingSteam,
                steamUserId = steamUserId,
                onOpenDialog = onOpenSteamDialog,
                onQuickSync = onQuickSteamSync
            )
        }

        AnimatedVisibility(
            visible = steamSyncStatus != null,
            enter = ExpressiveMotion.expressiveExpandEnter(reducedMotion),
            exit = ExpressiveMotion.expressiveCollapseExit(reducedMotion)
        ) {
            steamSyncStatus?.let { status ->
                val isSuccess = status.contains("Synced", ignoreCase = true) ||
                    status.contains("Sincroniz", ignoreCase = true)
                val isError = status.contains("failed", ignoreCase = true) ||
                    status.contains("Could not", ignoreCase = true) ||
                    status.contains("Error", ignoreCase = true) ||
                    status.contains("No se pudo", ignoreCase = true)
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
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                        } else {
                            Icon(
                                imageVector = if (isSuccess) Icons.Default.Check else if (isError) Icons.Default.Close else Icons.Default.Sync,
                                contentDescription = null,
                                tint = textColor,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Text(status, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = textColor, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton(onClick = onDismissStatus, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = s.common.dismiss, tint = textColor.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
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
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filterBtnInteraction = remember { MutableInteractionSource() }
                val isFilterPressed by filterBtnInteraction.collectIsPressedAsState()
                val filterBtnCorner = rememberExpressiveCornerRadius(
                    isSelected = isFilterActive,
                    isPressed = isFilterPressed,
                    baseRadius = 10.dp,
                    selectedRadius = 14.dp,
                    pressedRadius = 18.dp,
                    enabled = !reducedMotion
                )
                Box(
                    modifier = Modifier
                        .expressivePressScale(filterBtnInteraction, enabled = !reducedMotion, pressedScale = 0.93f)
                        .clip(RoundedCornerShape(filterBtnCorner))
                        .background(if (isFilterActive) NeonCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, if (isFilterActive) NeonCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(filterBtnCorner))
                        .clickable(interactionSource = filterBtnInteraction, indication = null) { showFilterMenuDialog = true }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                        .testTag("vault_filter_menu_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = s.common.filters,
                            tint = if (isFilterActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (viewMode == LibraryViewMode.ALL_PAID) s.library.steamFiltersBtn else s.library.vaultFiltersBtn,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFilterActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        if (activeFilterCount > 0) {
                            Box(modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.primary).padding(horizontal = 5.dp, vertical = 1.dp)) {
                                Text("$activeFilterCount", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                }

                if (onToggleCompactHeader != null) {
                    val compactInteraction = remember { MutableInteractionSource() }
                    val isCompactPressed by compactInteraction.collectIsPressedAsState()
                    val compactCorner = rememberExpressiveCornerRadius(
                        isSelected = isCompactHeader,
                        isPressed = isCompactPressed,
                        baseRadius = 10.dp,
                        selectedRadius = 16.dp,
                        pressedRadius = 18.dp,
                        enabled = !reducedMotion
                    )
                    Box(
                        modifier = Modifier
                            .expressivePressScale(compactInteraction, enabled = !reducedMotion, pressedScale = 0.93f)
                            .clip(RoundedCornerShape(compactCorner))
                            .background(if (isCompactHeader) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceContainerHigh)
                            .border(1.dp, if (isCompactHeader) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(compactCorner))
                            .clickable(interactionSource = compactInteraction, indication = null) { onToggleCompactHeader() }
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                            .testTag("toggle_compact_library_header"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isCompactHeader) s.library.showBannersBtn else s.library.hideBannersBtn,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompactHeader) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (onOpenDlcHub != null && (viewMode == LibraryViewMode.ALL_PAID || uiStats.steamTotalCount > 0 || steamUserId.isNotBlank())) {
                    LibraryActionChip(
                        emoji = "🧩",
                        label = s.library.dlcPicksChip,
                        accentColor = Color(0xFFA855F7),
                        badgeCount = dlcRecommendationsCount,
                        reducedMotion = reducedMotion,
                        testTag = "open_dlc_hub_button",
                        onClick = onOpenDlcHub
                    )
                }

                if (onOpenBacklogChallenge != null) {
                    LibraryActionChip(
                        emoji = "🎯",
                        label = s.library.challengeChip,
                        accentColor = Color(0xFFF59E0B),
                        badgeCount = 0,
                        reducedMotion = reducedMotion,
                        testTag = "open_backlog_challenge_button",
                        onClick = onOpenBacklogChallenge
                    )
                }

                if (viewMode == LibraryViewMode.ALL_PAID && onRollRandomBacklog != null) {
                    LibraryActionChip(
                        emoji = "🎲",
                        label = s.library.rollBacklogChip,
                        accentColor = Color(0xFFA855F7),
                        badgeCount = 0,
                        reducedMotion = reducedMotion,
                        testTag = "roll_random_backlog_button",
                        onClick = onRollRandomBacklog
                    )
                }

                if (!isFilterActive) {
                    Text(
                        text = if (viewMode == LibraryViewMode.ALL_PAID) {
                            s.library.totalCountFormat.format(uiStats.steamTotalCount + uiStats.claimedCount)
                        } else {
                            s.library.claimedCountFormat.format(uiStats.claimedCount)
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }

            if (isFilterActive) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedFilter != LibraryPlayFilter.ALL) {
                        val filterLabel = if (isEs) selectedFilter.localizedLabel(s) else selectedFilter.displayName
                        ActiveFilterPill(filterLabel) { onFilterSelect(LibraryPlayFilter.ALL) }
                    }
                    if (selectedPlayability != PlayabilityFilter.ALL) {
                        ActiveFilterPill("${selectedPlayability.iconEmoji} ${selectedPlayability.localizedLabel(s)}") { onPlayabilitySelect(PlayabilityFilter.ALL) }
                    }
                    if (selectedStorage != StorageFilter.ALL) {
                        ActiveFilterPill("💾 ${selectedStorage.localizedLabel(s)}") { onStorageSelect(StorageFilter.ALL) }
                    }
                    Text(
                        text = s.common.reset,
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
private fun LibrarySegmentItem(
    label: String,
    count: Int?,
    isSelected: Boolean,
    selectedTextColor: Color,
    selectedBadgeBg: Color,
    unselectedBadgeTextColor: Color,
    reducedMotion: Boolean,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .fillMaxHeight()
            .expressivePressScale(interaction, enabled = !reducedMotion, pressedScale = 0.93f)
            .clip(RoundedCornerShape(9.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) selectedTextColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (count != null) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) selectedBadgeBg else MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "$count",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) selectedTextColor else unselectedBadgeTextColor
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryActionChip(
    emoji: String,
    label: String,
    accentColor: Color,
    badgeCount: Int,
    reducedMotion: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val corner = rememberExpressiveCornerRadius(
        isPressed = isPressed,
        baseRadius = 10.dp,
        pressedRadius = 18.dp,
        enabled = !reducedMotion
    )
    Box(
        modifier = Modifier
            .expressivePressScale(interaction, enabled = !reducedMotion, pressedScale = 0.93f)
            .clip(RoundedCornerShape(corner))
            .background(accentColor.copy(alpha = 0.14f))
            .border(1.dp, accentColor.copy(alpha = 0.55f), RoundedCornerShape(corner))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(emoji, fontSize = 12.sp)
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentColor)
            if (badgeCount > 0) {
                Box(modifier = Modifier.clip(CircleShape).background(accentColor).padding(horizontal = 5.dp, vertical = 1.dp)) {
                    Text("$badgeCount", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ActiveFilterPill(label: String, onRemove: () -> Unit) {
    val s = LocalAppStrings.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
            .clickable { onRemove() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        Icon(Icons.Default.Close, contentDescription = s.library.removeFilterCd, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
    }
}
