package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.SteamAchievementEntity
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Custom in-app notification popup / toast banner triggered when background
 * Steam sync detects that a Mythic tier achievement (<5% rarity) has been unlocked.
 *
 * Displays the achievement's authentic icon, glowing mythic badge, achievement title,
 * game name, global unlock rarity, and interactive "View Trophy" and dismiss actions.
 */
@Composable
fun MythicAchievementToast(
    achievement: SteamAchievementEntity?,
    onDismiss: () -> Unit,
    onViewTrophy: (SteamAchievementEntity) -> Unit,
    modifier: Modifier = Modifier,
    autoDismissSeconds: Int = 8
) {
    val isVisible = achievement != null
    val progress = remember(achievement) { Animatable(1f) }

    // Auto-dismiss countdown timer
    LaunchedEffect(achievement) {
        if (achievement != null && autoDismissSeconds > 0) {
            progress.snapTo(1f)
            progress.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = autoDismissSeconds * 1000,
                    easing = LinearEasing
                )
            )
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + fadeIn(tween(250)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(200, easing = FastOutSlowInEasing)
        ) + fadeOut(tween(180)),
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        achievement?.let { ach ->
            MythicToastContent(
                achievement = ach,
                progress = progress.value,
                onDismiss = onDismiss,
                onViewTrophy = { onViewTrophy(ach) }
            )
        }
    }
}

@Composable
private fun MythicToastContent(
    achievement: SteamAchievementEntity,
    progress: Float,
    onDismiss: () -> Unit,
    onViewTrophy: () -> Unit
) {
    val context = LocalContext.current
    val mythicColor = Color(0xFFE040FB)
    val mythicGold = Color(0xFFFFD700)

    val glowingBorder = Brush.horizontalGradient(
        colors = listOf(
            mythicColor,
            mythicGold,
            Color(0xFF00E5FF),
            mythicColor
        )
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .shadow(16.dp, RoundedCornerShape(18.dp), ambientColor = mythicColor, spotColor = mythicColor)
            .clip(RoundedCornerShape(18.dp))
            .border(1.6.dp, glowingBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onViewTrophy)
            .testTag("mythic_achievement_toast"),
        color = Color(0xFF0E131F),
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 6.dp, top = 12.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Achievement Icon with glowing border & fallback
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF221133))
                        .border(1.5.dp, mythicColor.copy(alpha = 0.8f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (achievement.effectiveIconUrl.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(achievement.effectiveIconUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = achievement.displayName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MilitaryTech,
                                contentDescription = "Mythic Trophy",
                                tint = mythicGold,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Mini mythic badge overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B124D))
                            .border(0.8.dp, mythicColor, CircleShape)
                            .padding(horizontal = 3.dp, vertical = 1.dp)
                    ) {
                        Text(text = "💎", fontSize = 8.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Achievement Details
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Badge Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(mythicColor.copy(alpha = 0.2f))
                                .border(1.dp, mythicColor.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "💎 MYTHIC UNLOCKED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = mythicColor,
                                letterSpacing = 0.6.sp
                            )
                        }

                        // Unlock rarity
                        Text(
                            text = "Top ${achievement.formattedRarity}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = mythicGold
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = achievement.displayName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = achievement.gameName.ifBlank { "Steam Library" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NeonCyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (achievement.description.isNotBlank()) {
                        Text(
                            text = achievement.description,
                            fontSize = 11.sp,
                            color = TextMuted,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 14.sp
                        )
                    }
                }

                // Actions: "View" button + Dismiss "✕"
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("mythic_toast_dismiss_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss Mythic Trophy alert",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Button(
                        onClick = onViewTrophy,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = mythicColor.copy(alpha = 0.25f),
                            contentColor = mythicColor
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 10.dp,
                            vertical = 4.dp
                        ),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("mythic_toast_view_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = mythicGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "View",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Auto-dismiss duration bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp),
                color = mythicColor,
                trackColor = Color(0xFF1B2335)
            )
        }
    }
}
