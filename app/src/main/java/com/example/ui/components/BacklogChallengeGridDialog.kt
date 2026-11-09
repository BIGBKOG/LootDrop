package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BacklogChallengeCategory
import com.example.data.model.BacklogChallengeGrid
import com.example.data.model.BacklogChallengeTile
import com.example.data.model.BacklogMilestoneReward
import com.example.preferences.AccentTheme
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BacklogChallengeGridDialog(
    grid: BacklogChallengeGrid,
    onDismiss: () -> Unit,
    onToggleManualTile: (Int) -> Boolean,
    onRefreshCheck: () -> Unit,
    onClaimMilestones: ((List<BacklogMilestoneReward>) -> Unit) -> Unit,
    onEquipTheme: (AccentTheme) -> Unit,
    onSelectWeek: ((String) -> Unit)? = null,
    onSelectMonth: ((String) -> Unit)? = onSelectWeek,
    modifier: Modifier = Modifier
) {
    val weekSelector = onSelectWeek ?: onSelectMonth
    val context = LocalContext.current
    var selectedTile by remember { mutableStateOf<BacklogChallengeTile?>(null) }
    var showHowItWorks by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "blackout_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(
                    width = if (grid.isBlackout) 2.dp else 1.dp,
                    brush = if (grid.isBlackout) {
                        Brush.sweepGradient(
                            listOf(
                                Color(0xFFFFD700).copy(alpha = glowAlpha),
                                Color(0xFFE040FB).copy(alpha = glowAlpha),
                                Color(0xFF00E5FF).copy(alpha = glowAlpha),
                                Color(0xFFFFD700).copy(alpha = glowAlpha)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            listOf(
                                Color(0xFFF59E0B).copy(alpha = 0.7f),
                                Color(0xFF8B5CF6).copy(alpha = 0.4f)
                            )
                        )
                    },
                    shape = RoundedCornerShape(24.dp)
                )
                .testTag("dialog_backlog_challenge_grid"),
            color = Color(0xFF0B101D)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "🎯 ${grid.weekTitle}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            if (grid.isBlackout) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFFFFD700), Color(0xFFFFA000))
                                            )
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "BLACKOUT!",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (grid.isCurrentWeek) {
                                "Auto-refreshes every Monday • ⏳ ${grid.daysRemaining} days left"
                            } else {
                                val diff = com.example.util.BacklogChallengeManager.getWeeksBetween(
                                    com.example.util.BacklogChallengeManager.getCurrentWeekKey(),
                                    grid.weekKey
                                )
                                when {
                                    diff == 1 -> "Upcoming Challenge Preview • Starts next Monday"
                                    diff == 2 -> "Upcoming Challenge Preview • Starts in 2 weeks"
                                    diff < 0 -> "Weekly Challenge Archive"
                                    else -> "Upcoming Challenge Preview"
                                }
                            },
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { showHowItWorks = !showHowItWorks },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "How it works",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = onRefreshCheck,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Auto Check",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Week Selector Bar (Auto-refresh every Monday + Navigation)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF131D33))
                        .border(1.dp, Color(0xFF1E2E4E), RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (weekSelector != null && grid.previousWeekKey.isNotBlank()) {
                        IconButton(
                            onClick = { weekSelector(grid.previousWeekKey) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Previous Week",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(28.dp))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = grid.weekTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        if (grid.isCurrentWeek) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF0284C7).copy(alpha = 0.25f))
                                    .border(0.5.dp, Color(0xFF38BDF8).copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF7C3AED).copy(alpha = 0.25f))
                                    .border(0.5.dp, Color(0xFFA78BFA).copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                    .clickable { weekSelector?.invoke(com.example.util.BacklogChallengeManager.getCurrentWeekKey()) }
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "PREVIEW (Tap for Active)",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA78BFA)
                                )
                            }
                        }
                    }

                    if (weekSelector != null && grid.nextWeekKey.isNotBlank()) {
                        IconButton(
                            onClick = { weekSelector(grid.nextWeekKey) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Next Week",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(28.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF131D33))
                        .border(1.dp, Color(0xFF1E2E4E), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${grid.completedCount} / 9 Challenges Completed",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (grid.isBlackout) Color(0xFFFFD700) else Color(0xFF38BDF8)
                            )
                            Text(
                                text = when {
                                    grid.isBlackout -> "🌟 FULL BLACKOUT CLEARED"
                                    grid.totalLinesCompleted > 0 -> "⚡ ${grid.totalLinesCompleted} Line(s) Bingo!"
                                    else -> "Complete any row, col, or diag!"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (grid.totalLinesCompleted > 0) Color(0xFF4ADE80) else TextMuted
                            )
                        }

                        LinearProgressIndicator(
                            progress = { grid.completedCount / 9f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (grid.isBlackout) Color(0xFFFFD700) else Color(0xFF38BDF8),
                            trackColor = Color(0xFF0F172A)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🎯 Goal: Complete 3-in-a-row",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "Manual Unlocks: ${grid.manualOverridesUsed}/${grid.maxManualOverrides} used",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (grid.manualOverridesUsed >= grid.maxManualOverrides) Color(0xFFFBBF24) else Color(0xFF38BDF8)
                            )
                        }
                    }
                }

                // Expandable How it Works
                AnimatedVisibility(visible = showHowItWorks) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "🎮 How Backlog Bingo Works:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFBBF24)
                            )
                            Text(
                                text = "• Weekly Auto-Refresh: Every Monday at 00:00, your 3×3 challenge grid automatically refreshes with a fresh set of challenges sampled across all categories. Your unlocked themes and trophies remain permanently unlocked!\n" +
                                        "• Auto-Tracked: Playtime, beaten status, and Steam achievements automatically fill tiles.\n" +
                                        "• Line Bingos: Any 3-in-a-row (horizontal, vertical, or diagonal) unlocks the Backlog Slayer Trophy & Theme!\n" +
                                        "• Blackout: Clear all 9 tiles to claim the 24K Vault Cleaner Theme and Nebula Blackout Champion Trophy!\n" +
                                        "• Manual Override: Playing on console/offline? You can manually unlock up to 2 challenges per week (tap an unlocked tile anytime to uncheck and free up a slot).",
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Content: 3x3 Grid + Milestones Shelf
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 3x3 Bingo Grid
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (rowIndex in 0..2) {
                                val isRowComplete = grid.completedRows.contains(rowIndex)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    for (colIndex in 0..2) {
                                        val tileIndex = rowIndex * 3 + colIndex
                                        val tile = grid.tiles.getOrNull(tileIndex)
                                        if (tile != null) {
                                            val isColComplete = grid.completedColumns.contains(colIndex)
                                            val isDiagComplete = (rowIndex == colIndex && grid.completedDiagonals.contains(0)) ||
                                                    (rowIndex + colIndex == 2 && grid.completedDiagonals.contains(1))
                                            val isInWinningLine = isRowComplete || isColComplete || isDiagComplete || grid.isBlackout

                                            BingoTileCard(
                                                tile = tile,
                                                isInWinningLine = isInWinningLine,
                                                onClick = { selectedTile = tile },
                                                modifier = Modifier.weight(1f)
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Milestone Rewards Shelf
                    item {
                        MilestonesShelf(
                            milestones = grid.availableMilestones,
                            onClaimAll = {
                                onClaimMilestones { newlyAwarded ->
                                    val count = newlyAwarded.size
                                    val titles = newlyAwarded.joinToString(", ") { it.title }
                                    Toast.makeText(
                                        context,
                                        "🎉 Unlocked $count Milestone(s): $titles!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            onEquipTheme = onEquipTheme
                        )
                    }
                }
            }
        }
    }

    // Tile Detail Dialog
    selectedTile?.let { tile ->
        TileDetailDialog(
            tile = tile,
            grid = grid,
            onDismiss = { selectedTile = null },
            onToggleManual = {
                val wasManual = tile.manualOverrideDone
                val success = onToggleManualTile(tile.index)
                if (success) {
                    val newManual = !wasManual
                    selectedTile = selectedTile?.copy(
                        isCompleted = if (newManual) true else tile.isCompleted,
                        manualOverrideDone = newManual
                    )
                    if (newManual) {
                        Toast.makeText(
                            context,
                            "✅ Challenge manually unlocked (${grid.manualOverridesUsed + 1}/${grid.maxManualOverrides} used)",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Manual unlock cleared (${(grid.manualOverridesUsed - 1).coerceAtLeast(0)}/${grid.maxManualOverrides} used)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        context,
                        "⚠️ Manual unlock limit reached! You can only manually unlock 2 challenges per week.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }
}

@Composable
private fun BingoTileCard(
    tile: BacklogChallengeTile,
    isInWinningLine: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            tile.isCompleted && isInWinningLine -> Color(0xFFFFD700)
            tile.isCompleted -> Color(0xFF10B981)
            else -> Color(0xFF1E293B)
        },
        label = "tile_border"
    )

    val backgroundBrush = if (tile.isCompleted) {
        if (isInWinningLine) {
            Brush.linearGradient(
                listOf(Color(0xFF2E2208), Color(0xFF14241B))
            )
        } else {
            Brush.linearGradient(
                listOf(Color(0xFF0F291E), Color(0xFF0D1B2A))
            )
        }
    } else {
        Brush.linearGradient(
            listOf(Color(0xFF111827), Color(0xFF0F172A))
        )
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundBrush)
            .border(
                width = if (tile.isCompleted && isInWinningLine) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(8.dp)
            .testTag("bingo_tile_${tile.index}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Emoji & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = tile.emoji,
                    fontSize = 18.sp
                )

                if (tile.isCompleted) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(if (isInWinningLine) Color(0xFFFFD700) else Color(0xFF10B981)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.Black,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "${tile.currentProgress}/${tile.targetRequirement}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Center: Title
            Text(
                text = tile.title,
                fontSize = 11.sp,
                fontWeight = if (tile.isCompleted) FontWeight.Bold else FontWeight.SemiBold,
                color = if (tile.isCompleted) {
                    if (isInWinningLine) Color(0xFFFFE082) else Color(0xFF86EFAC)
                } else Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Bottom pill: Category / Manual indicator
            if (tile.manualOverrideDone) {
                Text(
                    text = "Manual Done",
                    fontSize = 8.sp,
                    color = Color(0xFFFBBF24),
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = tile.category.displayName,
                    fontSize = 8.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MilestonesShelf(
    milestones: List<BacklogMilestoneReward>,
    onClaimAll: () -> Unit,
    onEquipTheme: (AccentTheme) -> Unit
) {
    val unclaimedCount = milestones.count { it.isUnlocked && !it.isClaimed }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF111827))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "🏆 Milestone Rewards",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (unclaimedCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$unclaimedCount NEW",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }

            if (unclaimedCount > 0) {
                Button(
                    onClick = onClaimAll,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("claim_all_backlog_milestones")
                ) {
                    Text(
                        text = "Claim All",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }

        milestones.forEach { milestone ->
            MilestoneItemRow(
                milestone = milestone,
                onEquipTheme = onEquipTheme
            )
        }
    }
}

@Composable
private fun MilestoneItemRow(
    milestone: BacklogMilestoneReward,
    onEquipTheme: (AccentTheme) -> Unit
) {
    val isUnlocked = milestone.isUnlocked
    val isClaimed = milestone.isClaimed

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isClaimed) Color(0xFF14241B) else if (isUnlocked) Color(0xFF1E293B) else Color(0xFF0F172A))
            .border(
                1.dp,
                if (isClaimed) Color(0xFF10B981).copy(alpha = 0.5f) else if (isUnlocked) Color(0xFFF59E0B).copy(alpha = 0.6f) else Color(0xFF1E293B),
                RoundedCornerShape(10.dp)
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Icon / Status badge
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (isClaimed) Color(0xFF10B981).copy(alpha = 0.2f)
                    else if (isUnlocked) Color(0xFFF59E0B).copy(alpha = 0.2f)
                    else Color(0xFF1E293B)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isClaimed) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Claimed",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(20.dp)
                )
            } else if (isUnlocked) {
                Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = "Unlocked",
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = milestone.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isClaimed) Color(0xFF86EFAC) else if (isUnlocked) Color(0xFFFDE68A) else Color(0xFF94A3B8)
            )
            Text(
                text = milestone.subtitle,
                fontSize = 10.sp,
                color = TextSecondary
            )
        }

        // Action Buttons: Equip Theme or Claimed Badge
        if (isClaimed && milestone.themeReward != null) {
            Button(
                onClick = { onEquipTheme(milestone.themeReward) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color.White
                    )
                    Text("Equip", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else if (isClaimed) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF10B981).copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "In Showcase",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF34D399)
                )
            }
        } else {
            Text(
                text = if (isUnlocked) "Ready to Claim" else "Locked",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isUnlocked) Color(0xFFFBBF24) else TextMuted
            )
        }
    }
}

@Composable
private fun TileDetailDialog(
    tile: BacklogChallengeTile,
    grid: BacklogChallengeGrid,
    onDismiss: () -> Unit,
    onToggleManual: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp)),
            color = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = tile.emoji, fontSize = 28.sp)
                        Column {
                            Text(
                                text = tile.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Category: ${tile.category.displayName}",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Description
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E293B))
                        .padding(12.dp)
                ) {
                    Text(
                        text = tile.description,
                        fontSize = 13.sp,
                        color = Color(0xFFE2E8F0),
                        lineHeight = 18.sp
                    )
                }

                // Progress Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progress: ${tile.currentProgress} / ${tile.targetRequirement}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (tile.isCompleted) Color(0xFF34D399) else Color(0xFF38BDF8)
                    )

                    if (tile.isCompleted) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (tile.manualOverrideDone) "Manual Done" else "Auto-Completed",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF34D399)
                            )
                        }
                    }
                }

                // Recommended Game if present
                tile.recommendedGameTitle?.let { title ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B).copy(alpha = 0.6f))
                            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "💡 Recommended From Library:",
                                    fontSize = 10.sp,
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (tile.recommendedAppId != null && tile.recommendedAppId > 0) {
                                TextButton(
                                    onClick = {
                                        try {
                                            val steamUri = Uri.parse("steam://run/${tile.recommendedAppId}")
                                            val intent = Intent(Intent.ACTION_VIEW, steamUri)
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            val webUri = Uri.parse("https://store.steampowered.com/app/${tile.recommendedAppId}")
                                            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                                        }
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("Launch", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Icon(
                                            imageVector = Icons.Default.OpenInNew,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Manual Toggle Action (Offline / Console Play)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedButton(
                        onClick = onToggleManual,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("toggle_manual_tile_${tile.index}"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = when {
                                tile.manualOverrideDone -> Color(0xFFEF4444)
                                grid.canManualOverrideMore -> Color(0xFFF59E0B)
                                else -> Color(0xFF94A3B8)
                            }
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = when {
                                tile.manualOverrideDone -> "Clear Manual Unlock (Free up slot)"
                                grid.canManualOverrideMore -> "Mark Done Manually (${grid.remainingManualOverrides} left)"
                                else -> "Manual Unlock Limit Reached (2/2 Used)"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = when {
                            tile.manualOverrideDone -> "Tap to uncheck and free up 1 of your 2 weekly manual unlocks"
                            grid.canManualOverrideMore -> "Allowance: ${grid.manualOverridesUsed}/${grid.maxManualOverrides} used this week"
                            else -> "Limit of 2 manual unlocks reached this week. Clear another tile first to use here."
                        },
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
