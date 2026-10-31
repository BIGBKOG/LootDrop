package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.ControllerSupport
import com.example.data.model.PlayStatus
import com.example.data.model.SteamDeckStatus
import com.example.data.model.UnifiedLibraryItem
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Modal dialog that helps gamers overcome analysis paralysis by picking and showcasing
 * a random unplayed or partially completed game from their unified backlog (All + Paid).
 */
@Composable
fun BacklogRouletteDialog(
    candidates: List<UnifiedLibraryItem>,
    onDismiss: () -> Unit,
    onLaunchGame: (UnifiedLibraryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val diceRotation = remember { Animatable(0f) }

    var selectedIndex by remember(candidates) {
        mutableIntStateOf(if (candidates.isNotEmpty()) Random.nextInt(candidates.size) else -1)
    }

    val currentItem = if (selectedIndex in candidates.indices) candidates[selectedIndex] else null

    fun reroll() {
        if (candidates.size <= 1) return
        coroutineScope.launch {
            diceRotation.animateTo(
                targetValue = diceRotation.value + 360f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )
        }
        var nextIndex = Random.nextInt(candidates.size)
        while (nextIndex == selectedIndex && candidates.size > 1) {
            nextIndex = Random.nextInt(candidates.size)
        }
        selectedIndex = nextIndex
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = 0.8f),
                            Color(0xFFA855F7).copy(alpha = 0.7f),
                            Color(0xFF3B82F6).copy(alpha = 0.6f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .testTag("backlog_roulette_dialog"),
            color = Color(0xFF111625),
            tonalElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with Tag & Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF2E1065),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text("🎲", fontSize = 12.sp)
                                Text(
                                    text = "BACKLOG ROULETTE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFE9D5FF),
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("backlog_roulette_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close roulette dialog",
                            tint = TextMuted
                        )
                    }
                }

                if (candidates.isEmpty() || currentItem == null) {
                    // Empty state: All games beaten or dropped!
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                .border(1.5.dp, Color(0xFF10B981), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Text(
                            text = "Backlog Conquered! 🎉",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "You don't have any unplayed or in-progress games left in your All+Paid library. High five! Grab more free games from the Discover tab or replay an old favorite.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .padding(top = 12.dp)
                                .height(46.dp)
                        ) {
                            Text("Awesome", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Title & Paralysis Breaker Message
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "What To Play Next",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Text(
                            text = "Analysis paralysis solved! Fate picked this candidate from your ${candidates.size} backlog titles:",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }

                    // Rolled Game Showcase Card
                    AnimatedContent(
                        targetState = currentItem,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(250)) + scaleIn(initialScale = 0.92f))
                                .togetherWith(fadeOut(animationSpec = tween(150)))
                        },
                        label = "RouletteGameTransition"
                    ) { targetGame ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    width = 1.dp,
                                    color = NeonCyan.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF182032)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Cover Artwork with Badges
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9f)
                                        .background(Color(0xFF0F1420))
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(targetGame.coverImageUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = targetGame.name,
                                        modifier = Modifier.fillMaxWidth(),
                                        contentScale = ContentScale.Crop
                                    )

                                    // Gradient overlay at top and bottom of artwork
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .align(Alignment.TopCenter)
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(Color.Black.copy(alpha = 0.75f), Color.Transparent)
                                                )
                                            )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .align(Alignment.BottomCenter)
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                                )
                                            )
                                    )

                                    // Top Left: Launcher Badge + Metacritic Score
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xDD0B0F19),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.SportsEsports,
                                                    contentDescription = null,
                                                    tint = NeonCyan,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = targetGame.launcherLabel.uppercase(),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }

                                        // Metacritic score circle
                                        val score = targetGame.metacriticScore
                                        if (score != null && score > 0) {
                                            val scoreColor = when {
                                                score >= 75 -> Color(0xFF10B981)
                                                score >= 50 -> Color(0xFFF59E0B)
                                                else -> Color(0xFFEF4444)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xE60F172A))
                                                    .border(1.2.dp, scoreColor, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "$score",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = scoreColor
                                                )
                                            }
                                        }
                                    }

                                    // Bottom Left: Play Status Pill
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(10.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xDD0B0F19),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (targetGame.playStatus == PlayStatus.PLAYING) NeonCyan else Color(0xFFF59E0B)
                                        )
                                    ) {
                                        Text(
                                            text = if (targetGame.playStatus == PlayStatus.PLAYING) "🎮 In Progress" else "⏳ Unplayed",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (targetGame.playStatus == PlayStatus.PLAYING) NeonCyan else Color(0xFFF59E0B),
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }

                                    // Bottom Right: Playtime if available
                                    if (targetGame is UnifiedLibraryItem.Steam && targetGame.game.playtimeMinutes > 0) {
                                        Surface(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(10.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xDD000000),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                                        ) {
                                            Text(
                                                text = "${targetGame.game.playtimeHours} played",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF94A3B8),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                // Details Section
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = targetGame.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    // Platform / Hardware tags
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        when (targetGame) {
                                            is UnifiedLibraryItem.Steam -> {
                                                val hw = targetGame.game.hardwareProfile
                                                // Deck badge
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = when (hw.deckStatus) {
                                                        SteamDeckStatus.VERIFIED -> Color(0x2210B981)
                                                        SteamDeckStatus.PLAYABLE -> Color(0x22F59E0B)
                                                        else -> Color(0x2264748B)
                                                    },
                                                    border = androidx.compose.foundation.BorderStroke(
                                                        1.dp,
                                                        when (hw.deckStatus) {
                                                            SteamDeckStatus.VERIFIED -> Color(0xFF10B981)
                                                            SteamDeckStatus.PLAYABLE -> Color(0xFFF59E0B)
                                                            else -> Color(0xFF64748B)
                                                        }
                                                    )
                                                ) {
                                                    Text(
                                                        text = hw.deckStatus.pillLabel,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = when (hw.deckStatus) {
                                                            SteamDeckStatus.VERIFIED -> Color(0xFF10B981)
                                                            SteamDeckStatus.PLAYABLE -> Color(0xFFF59E0B)
                                                            else -> Color(0xFF94A3B8)
                                                        },
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                    )
                                                }

                                                // Controller badge
                                                if (hw.controllerSupport == ControllerSupport.FULL || hw.controllerSupport == ControllerSupport.PARTIAL) {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = Color(0x2238BDF8),
                                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f))
                                                    ) {
                                                        Text(
                                                            text = "${hw.controllerSupport.iconEmoji} ${hw.controllerSupport.label}",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = Color(0xFF38BDF8),
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }

                                                // Storage footprint
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0x228B5CF6),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.6f))
                                                ) {
                                                    Text(
                                                        text = "💾 ${hw.formattedStorage}",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = Color(0xFFA78BFA),
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            is UnifiedLibraryItem.Vault -> {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0x2210B981),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                                                ) {
                                                    Text(
                                                        text = "Claimed Free (${targetGame.giveaway.worth})",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = NeonGreen,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                if (targetGame.giveaway.platforms.isNotEmpty()) {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = Color(0x2238BDF8),
                                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
                                                    ) {
                                                        Text(
                                                            text = targetGame.giveaway.platforms.take(24),
                                                            fontSize = 10.sp,
                                                            color = Color(0xFF7DD3FC),
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Action Buttons Row: Launch Game & Reroll
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Launch Game Button (Primary)
                        Button(
                            onClick = {
                                onLaunchGame(currentItem)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan,
                                contentColor = Color(0xFF0F172A)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("backlog_roulette_launch_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Launch & Play",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        // Reroll Button (Secondary)
                        OutlinedButton(
                            onClick = { reroll() },
                            enabled = candidates.size > 1,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (candidates.size > 1) Color(0xFFA855F7) else Color.Gray.copy(alpha = 0.3f)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFE9D5FF)
                            ),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("backlog_roulette_reroll_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = "Reroll game",
                                modifier = Modifier
                                    .size(18.dp)
                                    .rotate(diceRotation.value),
                                tint = Color(0xFFC084FC)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Reroll",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Close Button
                    Text(
                        text = "Maybe Later",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onDismiss() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
