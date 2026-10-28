package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.SteamGameEntity
import com.example.data.model.PlayStatus
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SteamGameCard(
    game: SteamGameEntity,
    onStatusChange: (PlayStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isStatusMenuOpen by remember { mutableStateOf(false) }

    val statusColor = Color(game.playStatus.badgeColorHex)
    val cardBorderColor = when (game.playStatus) {
        PlayStatus.BEATEN -> NeonGreen.copy(alpha = 0.7f)
        PlayStatus.PLAYING -> NeonCyan.copy(alpha = 0.7f)
        PlayStatus.NOT_STARTED -> MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        PlayStatus.DROPPED -> Color(0xFFEF4444).copy(alpha = 0.5f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("steam_game_card_${game.appId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, cardBorderColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Image Box with Overlays
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(460f / 215f)
                    .background(Color(0xFF171A21))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(game.headerImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = game.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark gradient scrim at the bottom
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xCC0B0F19)),
                                startY = 80f
                            )
                        )
                )

                // Top left badge: Steam Brand badge
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xCC171A21))
                            .border(1.dp, Color(0xFF66C0F4).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 2.5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = Color(0xFF66C0F4),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "STEAM",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF66C0F4)
                            )
                        }
                    }

                    if (game.isLootDropClaimed) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonGreen.copy(alpha = 0.25f))
                                .border(1.dp, NeonGreen, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.5.dp)
                        ) {
                            Text(
                                text = "🎁 LOOTDROP CLAIMED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeonGreen
                            )
                        }
                    }
                }

                // Top right: Playtime Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xDD000000))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (game.playtimeMinutes > 0) "${game.playtimeHours} played" else "Unplayed",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (game.playtimeMinutes > 0) NeonCyan else Color(0xFF94A3B8)
                    )
                }

                // Bottom left: Interactive Play Status Pill
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xE60B0F19))
                            .border(1.2.dp, statusColor, RoundedCornerShape(8.dp))
                            .clickable { isStatusMenuOpen = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Text(
                            text = game.playStatus.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Change status",
                            tint = statusColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = isStatusMenuOpen,
                        onDismissRequest = { isStatusMenuOpen = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        PlayStatus.values().forEach { status ->
                            val isCurrent = status == game.playStatus
                            val itemColor = Color(status.badgeColorHex)
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(itemColor)
                                        )
                                        Text(
                                            text = status.label,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isCurrent) itemColor else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isCurrent) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = itemColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    isStatusMenuOpen = false
                                    onStatusChange(status)
                                }
                            )
                        }
                    }
                }
            }

            // Card Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Game Name
                Text(
                    text = game.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Achievements bar (if available)
                if (game.achievementsTotal > 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Achievements: ${game.achievementsUnlocked}/${game.achievementsTotal}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "${game.achievementsPercentage}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (game.achievementsPercentage == 100) NeonGreen else NeonCyan
                            )
                        }

                        LinearProgressIndicator(
                            progress = { (game.achievementsUnlocked.toFloat() / game.achievementsTotal.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = if (game.achievementsPercentage == 100) NeonGreen else NeonCyan,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }

                // Action Buttons Row: Launch Game & Store Link
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            try {
                                val launchIntent = Intent(Intent.ACTION_VIEW, Uri.parse(game.launchUri)).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(launchIntent)
                            } catch (_: Exception) {
                                // Fallback to store page if Steam client isn't installed
                                val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse(game.storeUrl)).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(storeIntent)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("steam_launch_btn_${game.appId}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF66C0F4),
                            contentColor = Color(0xFF0B0F19)
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Play Game",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(game.storeUrl)).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("steam_store_btn_${game.appId}"),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A475E)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFC7D5E0)
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Store",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Store",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
