package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.SteamAchievementEntity
import com.example.data.local.SteamGameEntity
import com.example.data.model.TrophyRarityTier
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.AchievementWalkthroughHelper

private enum class AchievementFilterTab(val label: String) {
    ALL("All"),
    LOCKED("Locked (Walkthroughs)"),
    UNLOCKED("Unlocked")
}

@Composable
fun GameAchievementsDialog(
    game: SteamGameEntity,
    achievements: List<SteamAchievementEntity>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilterTab by remember { mutableStateOf(AchievementFilterTab.ALL) }

    val unlockedCount = achievements.count { it.isAchieved }
    val totalCount = achievements.size.coerceAtLeast(game.achievementsTotal)
    val percent = if (totalCount > 0) ((unlockedCount.toDouble() / totalCount.toDouble()) * 100).toInt() else 0
    val remainingToHundred = (totalCount - unlockedCount).coerceAtLeast(0)

    val displayedAchievements = remember(achievements, selectedFilterTab) {
        when (selectedFilterTab) {
            AchievementFilterTab.ALL -> achievements.sortedWith(
                compareBy<SteamAchievementEntity> { it.isAchieved } // Locked first so walkthroughs are immediately accessible
                    .thenBy { it.globalPercentage }
            )
            AchievementFilterTab.LOCKED -> achievements.filter { !it.isAchieved }.sortedBy { it.globalPercentage }
            AchievementFilterTab.UNLOCKED -> achievements.filter { it.isAchieved }.sortedBy { it.globalPercentage }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.2.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
            color = Color(0xFF0F1420),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                // Header Image Banner with Overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(game.headerImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = game.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Dark gradient scrim
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.3f),
                                        Color(0xFF0F1420).copy(alpha = 0.95f)
                                    )
                                )
                            )
                    )

                    // Close Button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .testTag("dialog_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Game Title & Stats inside banner
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = game.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            if (remainingToHundred in 1..5 && percent >= 80) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NeonCyan.copy(alpha = 0.2f))
                                        .border(1.dp, NeonCyan, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "🔥 $remainingToHundred AWAY FROM 100%!",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonCyan
                                    )
                                }
                            } else if (percent == 100 && totalCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NeonGreen.copy(alpha = 0.2f))
                                        .border(1.dp, NeonGreen, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "👑 100% MASTERED",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonGreen
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${game.playtimeHours} played • $unlockedCount / $totalCount Achievements ($percent%)",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Progress Bar
                LinearProgressIndicator(
                    progress = { (percent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = if (percent == 100) NeonGreen else NeonCyan,
                    trackColor = Color(0xFF1E293B)
                )

                // Filter Tabs & External Hub Link Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AchievementFilterTab.values().forEach { tab ->
                        val isSelected = tab == selectedFilterTab
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilterTab = tab },
                            label = {
                                Text(
                                    text = tab.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                                selectedLabelColor = NeonCyan,
                                containerColor = Color(0xFF172033),
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color(0xFF2A3B56),
                                selectedBorderColor = NeonCyan,
                                borderWidth = 1.dp
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // Open All Steam Guides Button
                    OutlinedButton(
                        onClick = {
                            AchievementWalkthroughHelper.openSteamCommunityGuide(context, game.appId)
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF66C0F4).copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = Color(0xFF66C0F4),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("All Steam Guides", fontSize = 11.sp, color = Color(0xFF66C0F4), fontWeight = FontWeight.SemiBold)
                    }
                }

                // Achievements List
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Fetching achievements & global percentages from Steam...", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                } else if (displayedAchievements.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No achievements match this filter.",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(displayedAchievements, key = { "${it.appId}_${it.apiName}" }) { ach ->
                            AchievementItemRow(
                                achievement = ach,
                                gameTitle = game.name,
                                onOpenSteamGuide = {
                                    AchievementWalkthroughHelper.openSteamCommunityGuide(context, ach.appId, ach.displayName)
                                },
                                onOpenYouTubeGuide = {
                                    AchievementWalkthroughHelper.openYouTubeWalkthrough(context, game.name, ach.displayName)
                                },
                                onOpenWebGuide = {
                                    AchievementWalkthroughHelper.openWebGuide(context, game.name, ach.displayName)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementItemRow(
    achievement: SteamAchievementEntity,
    gameTitle: String,
    onOpenSteamGuide: () -> Unit,
    onOpenYouTubeGuide: () -> Unit,
    onOpenWebGuide: () -> Unit
) {
    val tier = achievement.rarityTier
    val tierColor = tier.color
    val isUnlocked = achievement.isAchieved

    val cardBg = if (isUnlocked) Color(0xFF131D2E) else Color(0xFF181C28)
    val cardBorder = if (isUnlocked) {
        if (tier == TrophyRarityTier.MYTHIC) tierColor.copy(alpha = 0.7f) else Color(0xFF22364F)
    } else {
        Color(0xFF242A38)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Status Icon: Green checkmark if achieved, Lock if locked
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isUnlocked) NeonGreen.copy(alpha = 0.15f) else Color(0xFF282F3F))
                        .border(
                            1.dp,
                            if (isUnlocked) NeonGreen.copy(alpha = 0.5f) else Color(0xFF384359),
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUnlocked) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Unlocked",
                            tint = NeonGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Title & Description
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = achievement.displayName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUnlocked) TextPrimary else Color(0xFFD1D5DB)
                        )
                    }

                    if (achievement.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = achievement.description,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Global Percentage Rarity Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(tier.containerColor)
                    .border(1.dp, tierColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${tier.iconEmoji} ${achievement.formattedRarity}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = tierColor
                )
            }
        }

        // ONE-TAP WALKTHROUGH SHORTCUTS FOR LOCKED ACHIEVEMENTS
        if (!isUnlocked) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F1420))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Walkthrough Guides:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF94A3B8)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Steam Community Guide Shortcut
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1B2838))
                            .border(1.dp, Color(0xFF66C0F4).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .clickable(onClick = onOpenSteamGuide)
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(imageVector = Icons.Default.SportsEsports, contentDescription = null, tint = Color(0xFF66C0F4), modifier = Modifier.size(11.dp))
                            Text(text = "Steam", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF66C0F4))
                        }
                    }

                    // YouTube Walkthrough Shortcut
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF381216))
                            .border(1.dp, Color(0xFFFF4E4E).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .clickable(onClick = onOpenYouTubeGuide)
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(imageVector = Icons.Default.PlayCircle, contentDescription = null, tint = Color(0xFFFF4E4E), modifier = Modifier.size(11.dp))
                            Text(text = "YouTube", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF4E4E))
                        }
                    }

                    // Google Web Guide Shortcut
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF162536))
                            .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .clickable(onClick = onOpenWebGuide)
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(11.dp))
                            Text(text = "Guide", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                        }
                    }
                }
            }
        }
    }
}
