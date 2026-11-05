package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.local.SteamAchievementEntity
import com.example.data.model.TrophyRarityTier
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SteamBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Modal dialog that allows users to pick exactly which unlocked Steam trophies
 * appear on their Gamer Profile showcase.
 */
@Composable
fun ChooseDisplayedTrophiesDialog(
    allTrophies: List<SteamAchievementEntity>,
    hasCustomSelection: Boolean,
    currentSelectedKeys: Set<String>,
    onSave: (hasCustom: Boolean, selectedKeys: Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    // If user has not yet customized, pre-populate with currently showcased items (top 15) so they can easily tweak
    var tempSelectedKeys by remember {
        mutableStateOf(
            if (hasCustomSelection) {
                currentSelectedKeys
            } else {
                allTrophies.take(15).map { "${it.appId}_${it.apiName}" }.toSet()
            }
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedTierFilter by remember { mutableStateOf<TrophyRarityTier?>(null) }

    val filteredTrophies = remember(allTrophies, searchQuery, selectedTierFilter) {
        allTrophies.filter { trophy ->
            val matchesQuery = searchQuery.isBlank() ||
                trophy.displayName.contains(searchQuery, ignoreCase = true) ||
                trophy.description.contains(searchQuery, ignoreCase = true) ||
                trophy.gameName.contains(searchQuery, ignoreCase = true)
            val matchesTier = selectedTierFilter == null || trophy.rarityTier == selectedTierFilter
            matchesQuery && matchesTier
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .widthIn(max = 580.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .testTag("choose_displayed_trophies_dialog"),
            color = Color(0xFF131722),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x33FFD700)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Choose Showcase Trophies",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Select which achievements appear on your profile",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("choose_trophies_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Selection Counter & Quick Presets
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F131E))
                        .border(1.dp, Color(0xFF222B3F), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (tempSelectedKeys.isNotEmpty()) NeonCyan.copy(alpha = 0.2f) else Color(0x33718096))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${tempSelectedKeys.size} / ${allTrophies.size} Selected",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (tempSelectedKeys.isNotEmpty()) NeonCyan else TextMuted
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                tempSelectedKeys = allTrophies.map { "${it.appId}_${it.apiName}" }.toSet()
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("choose_trophies_select_all_btn")
                        ) {
                            Text("All", fontSize = 11.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                        }

                        TextButton(
                            onClick = {
                                tempSelectedKeys = allTrophies
                                    .filter { it.rarityTier == TrophyRarityTier.MYTHIC || it.rarityTier == TrophyRarityTier.GOLD }
                                    .map { "${it.appId}_${it.apiName}" }
                                    .toSet()
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("choose_trophies_rarest_btn")
                        ) {
                            Text("Rarest", fontSize = 11.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                        }

                        TextButton(
                            onClick = {
                                tempSelectedKeys = emptySet()
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("choose_trophies_clear_btn")
                        ) {
                            Text("Clear", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                        }

                        TextButton(
                            onClick = {
                                onSave(false, emptySet())
                                onDismiss()
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("choose_trophies_reset_auto_btn")
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Auto", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search trophies or games...", color = TextMuted, fontSize = 12.sp) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("choose_trophies_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color(0xFF222B3F),
                        focusedContainerColor = Color(0xFF0F131E),
                        unfocusedContainerColor = Color(0xFF0F131E),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                // Rarity Tier Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedTierFilter == null,
                        onClick = { selectedTierFilter = null },
                        label = { Text("All (${allTrophies.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                            selectedLabelColor = NeonCyan
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = Color(0xFF252E44),
                            selectedBorderColor = NeonCyan,
                            enabled = true,
                            selected = selectedTierFilter == null
                        )
                    )

                    val mythicCount = remember(allTrophies) { allTrophies.count { it.rarityTier == TrophyRarityTier.MYTHIC } }
                    if (mythicCount > 0) {
                        FilterChip(
                            selected = selectedTierFilter == TrophyRarityTier.MYTHIC,
                            onClick = {
                                selectedTierFilter = if (selectedTierFilter == TrophyRarityTier.MYTHIC) null else TrophyRarityTier.MYTHIC
                            },
                            label = { Text("💎 Mythic ($mythicCount)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0x33E040FB),
                                selectedLabelColor = Color(0xFFE040FB)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = Color(0xFF252E44),
                                selectedBorderColor = Color(0xFFE040FB),
                                enabled = true,
                                selected = selectedTierFilter == TrophyRarityTier.MYTHIC
                            )
                        )
                    }

                    val goldCount = remember(allTrophies) { allTrophies.count { it.rarityTier == TrophyRarityTier.GOLD } }
                    if (goldCount > 0) {
                        FilterChip(
                            selected = selectedTierFilter == TrophyRarityTier.GOLD,
                            onClick = {
                                selectedTierFilter = if (selectedTierFilter == TrophyRarityTier.GOLD) null else TrophyRarityTier.GOLD
                            },
                            label = { Text("🥇 Gold ($goldCount)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0x33FFD700),
                                selectedLabelColor = Color(0xFFFFD700)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = Color(0xFF252E44),
                                selectedBorderColor = Color(0xFFFFD700),
                                enabled = true,
                                selected = selectedTierFilter == TrophyRarityTier.GOLD
                            )
                        )
                    }

                    val silverCount = remember(allTrophies) { allTrophies.count { it.rarityTier == TrophyRarityTier.SILVER } }
                    if (silverCount > 0) {
                        FilterChip(
                            selected = selectedTierFilter == TrophyRarityTier.SILVER,
                            onClick = {
                                selectedTierFilter = if (selectedTierFilter == TrophyRarityTier.SILVER) null else TrophyRarityTier.SILVER
                            },
                            label = { Text("🥈 Silver ($silverCount)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0x33CFD8DC),
                                selectedLabelColor = Color(0xFFCFD8DC)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = Color(0xFF252E44),
                                selectedBorderColor = Color(0xFFCFD8DC),
                                enabled = true,
                                selected = selectedTierFilter == TrophyRarityTier.SILVER
                            )
                        )
                    }

                    val bronzeCount = remember(allTrophies) { allTrophies.count { it.rarityTier == TrophyRarityTier.BRONZE } }
                    if (bronzeCount > 0) {
                        FilterChip(
                            selected = selectedTierFilter == TrophyRarityTier.BRONZE,
                            onClick = {
                                selectedTierFilter = if (selectedTierFilter == TrophyRarityTier.BRONZE) null else TrophyRarityTier.BRONZE
                            },
                            label = { Text("🥉 Bronze ($bronzeCount)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0x33CD7F32),
                                selectedLabelColor = Color(0xFFCD7F32)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = Color(0xFF252E44),
                                selectedBorderColor = Color(0xFFCD7F32),
                                enabled = true,
                                selected = selectedTierFilter == TrophyRarityTier.BRONZE
                            )
                        )
                    }
                }

                // Trophies List
                if (filteredTrophies.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "No matching trophies found",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (searchQuery.isNotBlank() || selectedTierFilter != null) {
                                TextButton(onClick = {
                                    searchQuery = ""
                                    selectedTierFilter = null
                                }) {
                                    Text("Reset Filters", color = NeonCyan, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("choose_trophies_list"),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(
                            items = filteredTrophies,
                            key = { "choose_trophy_${it.appId}_${it.apiName}" }
                        ) { trophy ->
                            val trophyKey = "${trophy.appId}_${trophy.apiName}"
                            val isSelected = trophyKey in tempSelectedKeys
                            val tier = trophy.rarityTier

                            TrophySelectItemRow(
                                trophy = trophy,
                                isSelected = isSelected,
                                onToggle = {
                                    tempSelectedKeys = if (isSelected) {
                                        tempSelectedKeys - trophyKey
                                    } else {
                                        tempSelectedKeys + trophyKey
                                    }
                                }
                            )
                        }
                    }
                }

                // Footer Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("choose_trophies_cancel_btn"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF252E44))
                    ) {
                        Text("Cancel", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            onSave(true, tempSelectedKeys)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(46.dp)
                            .testTag("choose_trophies_save_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (tempSelectedKeys.isNotEmpty()) NeonGreen else NeonCyan
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF0F131F),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save Showcase (${tempSelectedKeys.size})",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F131F)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrophySelectItemRow(
    trophy: SteamAchievementEntity,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val tier = trophy.rarityTier
    val tierColor = tier.color

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (isSelected) NeonCyan.copy(alpha = 0.6f) else Color(0xFF1E2638),
                RoundedCornerShape(12.dp)
            )
            .clickable { onToggle() }
            .testTag("trophy_select_row_${trophy.appId}_${trophy.apiName}"),
        color = if (isSelected) Color(0xFF161E30) else Color(0xFF0E121C),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = NeonCyan,
                    checkmarkColor = Color(0xFF0F131F),
                    uncheckedColor = TextMuted
                ),
                modifier = Modifier.size(24.dp)
            )

            // Trophy Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(tierColor.copy(alpha = 0.15f))
                    .border(1.dp, tierColor.copy(alpha = 0.45f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (trophy.effectiveIconUrl.isNotBlank()) {
                    AsyncImage(
                        model = trophy.effectiveIconUrl,
                        contentDescription = trophy.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = tierColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Info Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Top row: Game name & rarity tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = trophy.gameName.ifBlank { "Steam Game" },
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SteamBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(tier.containerColor)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "${tier.iconEmoji} ${trophy.formattedRarity}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = tierColor
                        )
                    }
                }

                // Title
                Text(
                    text = trophy.displayName,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) TextPrimary else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Description
                if (trophy.description.isNotBlank()) {
                    Text(
                        text = trophy.description,
                        fontSize = 10.5.sp,
                        color = TextMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 13.sp
                    )
                }
            }
        }
    }
}
