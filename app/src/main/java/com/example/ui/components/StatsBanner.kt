package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.UiStats
import com.example.ui.theme.GamingCardBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun StatsBanner(
    stats: UiStats,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    isNewTodayDismissed: Boolean = false,
    onDismissNewToday: (() -> Unit)? = null,
    onNewTodayClick: (() -> Unit)? = null,
    isAnimationsEnabled: Boolean = true
) {
    // Ambient breathing shimmer on border and glow only if animations enabled
    val borderAlpha = if (isAnimationsEnabled) {
        val infiniteTransition = rememberInfiniteTransition(label = "statsBannerInfinite")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.85f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "borderGlow"
        )
        alpha
    } else 0.6f

    val badgeScale = if (isAnimationsEnabled) {
        val infiniteTransition = rememberInfiniteTransition(label = "statsBannerInfiniteBadge")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "badgePulse"
        )
        scale
    } else 1.0f

    var localDismissed by rememberSaveable { mutableStateOf(false) }
    val effectiveDismissed = isNewTodayDismissed || localDismissed

    // Fluid smooth counter animation for total free worth
    val animatedWorth by animateFloatAsState(
        targetValue = stats.totalFreeWorthUsd.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "animatedWorth"
    )

    val animatedCount by animateFloatAsState(
        targetValue = stats.activeGiveawaysCount.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "animatedCount"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF131B2E),
                        Color(0xFF16233B),
                        Color(0xFF121826)
                    )
                )
            )
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = borderAlpha * 0.7f),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
            .testTag("stats_banner")
    ) {
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
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "TOTAL FREE VALUE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val formattedVal = String.format(Locale.US, "$%.2f", animatedWorth)
                    Text(
                        text = formattedVal,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "FREE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "${animatedCount.toInt()} active freebies",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    AnimatedVisibility(
                        visible = !effectiveDismissed && stats.newFreebiesCount > 0,
                        enter = fadeIn() + expandHorizontally(),
                        exit = fadeOut() + shrinkHorizontally()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = badgeScale
                                    scaleY = badgeScale
                                }
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonCyan.copy(alpha = 0.2f))
                                .border(0.5.dp, NeonCyan.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                                .testTag("new_today_badge")
                        ) {
                            // "NEW TODAY" button label
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable(
                                        enabled = onNewTodayClick != null,
                                        onClick = { onNewTodayClick?.invoke() }
                                    )
                                    .padding(start = 7.dp, end = 3.dp, top = 3.dp, bottom = 3.dp)
                            ) {
                                Text(
                                    text = "${stats.newFreebiesCount} NEW TODAY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan
                                )
                            }

                            // Dedicated 'X' button to dismiss and get rid of the badge
                            Box(
                                modifier = Modifier
                                    .padding(end = 4.dp, top = 2.dp, bottom = 2.dp)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan.copy(alpha = 0.25f))
                                    .clickable(
                                        onClick = {
                                            localDismissed = true
                                            onDismissNewToday?.invoke()
                                        },
                                        role = Role.Button
                                    )
                                    .testTag("dismiss_new_today_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss New Today alert",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Quick actions: Refresh with rotation animation only when refreshing
            val refreshAngle = if (isRefreshing && isAnimationsEnabled) {
                val refreshTransition = rememberInfiniteTransition(label = "refreshAnim")
                val angle by refreshTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "refreshAngle"
                )
                angle
            } else 0f

            IconButton(
                onClick = onRefresh,
                enabled = !isRefreshing,
                modifier = Modifier.testTag("refresh_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Freebies",
                    tint = if (isRefreshing) MaterialTheme.colorScheme.primary else TextPrimary,
                    modifier = if (isRefreshing && refreshAngle != 0f) {
                        Modifier.graphicsLayer { rotationZ = refreshAngle }
                    } else Modifier
                )
            }
        }
    }
}
