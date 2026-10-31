package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MetacriticInfo
import com.example.data.model.MetacriticRatingTier
import com.example.ui.theme.GamingCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Compact Metacritic score pill for game cards and list items.
 */
@Composable
fun MetacriticScorePill(
    score: Int,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    showPrefix: Boolean = false
) {
    val tier = MetacriticRatingTier.fromScore(score)
    val isLightColor = tier == MetacriticRatingTier.MIXED_REVIEWS ||
            tier == MetacriticRatingTier.GENERALLY_FAVORABLE ||
            tier == MetacriticRatingTier.UNIVERSAL_ACCLAIM

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(tier.badgeColor)
            .border(0.8.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .testTag("metacritic_score_pill_$score"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (showPrefix) {
                Text(
                    text = "META",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isLightColor) Color(0xFF0B0F19) else Color.White,
                    letterSpacing = 0.5.sp
                )
            }
            Text(
                text = score.toString(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = if (isLightColor) Color(0xFF0B0F19) else Color.White
            )
        }
    }
}

/**
 * Circular Metacritic score badge for game cards placed next to the launcher type.
 */
@Composable
fun MetacriticScoreCircle(
    score: Int,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    if (score <= 0) return
    val tier = MetacriticRatingTier.fromScore(score)
    val isLightColor = tier == MetacriticRatingTier.MIXED_REVIEWS ||
            tier == MetacriticRatingTier.GENERALLY_FAVORABLE ||
            tier == MetacriticRatingTier.UNIVERSAL_ACCLAIM

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(tier.badgeColor)
            .border(0.8.dp, Color.White.copy(alpha = 0.45f), CircleShape)
            .testTag("metacritic_score_circle_$score"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = score.toString(),
            fontSize = if (score >= 100) 8.sp else 9.5.sp,
            fontWeight = FontWeight.Black,
            color = if (isLightColor) Color(0xFF0B0F19) else Color.White,
            letterSpacing = (-0.3).sp
        )
    }
}

/**
 * Prominent Metacritic badge for the game info / detail dialog.
 */
@Composable
fun MetacriticDetailBadge(
    info: MetacriticInfo,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val tier = info.tier
    val isLightColor = tier != MetacriticRatingTier.UNFAVORABLE

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0B101D))
            .border(1.dp, tier.badgeColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("metacritic_detail_badge"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Metacritic Colored Square
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(tier.badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = info.score.toString(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = if (isLightColor) Color(0xFF0B0F19) else Color.White
            )
        }

        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "METASCORE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    letterSpacing = 0.8.sp
                )
                if (info.score >= 90) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Must-Play",
                        tint = tier.badgeColor,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
            Text(
                text = tier.label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = tier.badgeColor
            )
        }
    }
}

/**
 * Comprehensive Metacritic Critic Card shown inside the Game Info dialog.
 */
@Composable
fun MetacriticDetailSection(
    info: MetacriticInfo,
    gameTitle: String,
    context: Context,
    modifier: Modifier = Modifier
) {
    val tier = info.tier
    val isLightColor = tier != MetacriticRatingTier.UNFAVORABLE

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0F1522))
            .border(1.2.dp, tier.badgeColor.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .padding(14.dp)
            .testTag("metacritic_info_section")
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Official Metascore Badge Icon
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(tier.badgeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = info.score.toString(),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isLightColor) Color(0xFF0B0F19) else Color.White
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "METACRITIC SCORE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(tier.badgeColor.copy(alpha = 0.2f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${info.score} / 100",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = tier.badgeColor
                            )
                        }
                    }
                    Text(
                        text = "${tier.label} • ${tier.summary}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = tier.badgeColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Accurate critic consensus verified through CheapShark & GamerPower APIs from top editorial review publications worldwide.",
            fontSize = 11.sp,
            color = TextSecondary,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Open on Metacritic CTA
        OutlinedButton(
            onClick = {
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(info.reviewUrl)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(browserIntent)
                } catch (_: Exception) {
                    Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .testTag("open_metacritic_review_button"),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
            border = androidx.compose.foundation.BorderStroke(1.dp, tier.badgeColor.copy(alpha = 0.6f))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = tier.badgeColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "View '$gameTitle' on Metacritic",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }
    }
}
