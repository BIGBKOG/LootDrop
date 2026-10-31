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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import com.example.data.model.SteamDeckStatus
import com.example.data.model.ControllerSupport
import com.example.data.model.SessionStyle
import com.example.data.model.StorageTier
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun SteamGameCard(
    game: SteamGameEntity,
    onStatusChange: (PlayStatus) -> Unit,
    onUpdateManualStorage: ((Float?) -> Unit)? = null,
    onOpenAchievements: ((SteamGameEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isStatusMenuOpen by remember { mutableStateOf(false) }
    var isStorageDialogOpen by remember { mutableStateOf(false) }
    var storageInputText by remember(game.manualInstallSizeGb) {
        mutableStateOf(game.manualInstallSizeGb?.toString() ?: "")
    }

    val hardware = game.hardwareProfile
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .then(if (onOpenAchievements != null) Modifier.clickable { onOpenAchievements(game) } else Modifier)
                            .padding(vertical = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Achievements: ${game.achievementsUnlocked}/${game.achievementsTotal}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                if (onOpenAchievements != null) {
                                    Text(
                                        text = "• Walkthroughs ➔",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonCyan
                                    )
                                }
                            }
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

                // Hardware Readiness & Storage Footprint Tags Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Deck status tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when (hardware.deckStatus) {
                                    SteamDeckStatus.VERIFIED -> Color(0x2210B981)
                                    SteamDeckStatus.PLAYABLE -> Color(0x22F59E0B)
                                    SteamDeckStatus.UNSUPPORTED -> Color(0x22EF4444)
                                    SteamDeckStatus.UNKNOWN -> Color(0x2264748B)
                                }
                            )
                            .border(
                                1.dp,
                                when (hardware.deckStatus) {
                                    SteamDeckStatus.VERIFIED -> Color(0xFF10B981)
                                    SteamDeckStatus.PLAYABLE -> Color(0xFFF59E0B)
                                    SteamDeckStatus.UNSUPPORTED -> Color(0xFFEF4444)
                                    SteamDeckStatus.UNKNOWN -> Color(0xFF64748B)
                                },
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            text = hardware.deckStatus.pillLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = when (hardware.deckStatus) {
                                SteamDeckStatus.VERIFIED -> Color(0xFF10B981)
                                SteamDeckStatus.PLAYABLE -> Color(0xFFF59E0B)
                                SteamDeckStatus.UNSUPPORTED -> Color(0xFFEF4444)
                                SteamDeckStatus.UNKNOWN -> Color(0xFF94A3B8)
                            }
                        )
                    }

                    // Controller support tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x2266C0F4))
                            .border(1.dp, Color(0xFF66C0F4).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            text = hardware.controllerSupport.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF66C0F4)
                        )
                    }

                    // Session style tag (if quick session)
                    if (hardware.sessionStyle == SessionStyle.QUICK_SESSION) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x22F59E0B))
                                .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.5.dp)
                        ) {
                            Text(
                                text = "⚡ Quick Session",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }

                    // Storage footprint tag with edit affordance
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x228B5CF6))
                            .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .clickable {
                                storageInputText = game.manualInstallSizeGb?.toString() ?: ""
                                isStorageDialogOpen = true
                            }
                            .padding(horizontal = 6.dp, vertical = 2.5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "💾 ${hardware.formattedSize}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFA78BFA)
                        )
                        if (hardware.isManualOverride) {
                            Text(
                                text = "(custom)",
                                fontSize = 9.sp,
                                color = Color(0xFFC4B5FD)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit install size",
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(10.dp)
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

    if (isStorageDialogOpen) {
        Dialog(
            onDismissRequest = { isStorageDialogOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SdStorage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Storage Footprint Tracker",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = game.name,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Text(
                        text = "Track the exact install size on your Steam Deck, ROG Ally, or PC drive. Enter manual gigabytes (GB) or reset to restore estimated size (${hardware.estimatedSizeGb} GB).",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    OutlinedTextField(
                        value = storageInputText,
                        onValueChange = { storageInputText = it },
                        label = { Text("Install Size (GB)") },
                        placeholder = { Text("e.g. 15.5") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (game.manualInstallSizeGb != null) {
                            TextButton(
                                onClick = {
                                    onUpdateManualStorage?.invoke(null)
                                    isStorageDialogOpen = false
                                }
                            ) {
                                Text("Reset to Auto", color = Color(0xFFEF4444))
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        TextButton(onClick = { isStorageDialogOpen = false }) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val parsed = storageInputText.trim().toFloatOrNull()
                                onUpdateManualStorage?.invoke(parsed)
                                isStorageDialogOpen = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Save Size")
                        }
                    }
                }
            }
        }
    }
}
