package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GamingCardBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.OfficialOstCatalog
import com.example.util.OfficialOstInfo

/**
 * Official Game Soundtrack (OST) section for the game info / detail dialog.
 *
 * Provides quick actions to jump directly to official soundtracks on YouTube Music
 * and Spotify if available, or clearly marks "No OST" if the item does not have one.
 */
@Composable
fun OfficialOstSection(
    ostInfo: OfficialOstInfo?,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    val context = LocalContext.current
    val spotifyGreen = Color(0xFF1DB954)
    val youtubeRed = Color(0xFFFF0000)
    val discogsOrange = Color(0xFFFF851B)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                Icon(
                    imageVector = if (ostInfo != null) Icons.Default.Headphones else Icons.Default.MusicOff,
                    contentDescription = null,
                    tint = if (ostInfo != null) NeonCyan else TextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "OFFICIAL SOUNDTRACK (OST)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (ostInfo != null) NeonCyan else TextMuted,
                    letterSpacing = 1.sp
                )
            }

            // Quick Status Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        when {
                            isLoading -> Color(0x2238BDF8)
                            ostInfo != null -> Color(0x221DB954)
                            else -> Color(0x2264748B)
                        }
                    )
                    .border(
                        1.dp,
                        when {
                            isLoading -> Color(0xFF38BDF8).copy(alpha = 0.6f)
                            ostInfo != null -> Color(0xFF1DB954).copy(alpha = 0.6f)
                            else -> Color(0xFF475569)
                        },
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 7.dp, vertical = 2.dp)
                    .testTag(if (ostInfo != null) "detail_has_ost_badge" else "detail_no_ost_badge")
            ) {
                Text(
                    text = when {
                        isLoading -> "Checking OST..."
                        ostInfo != null -> "🎵 Official OST"
                        else -> "No OST"
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isLoading -> Color(0xFF38BDF8)
                        ostInfo != null -> Color(0xFF1DB954)
                        else -> Color(0xFF94A3B8)
                    }
                )
            }
        }

        if (ostInfo != null) {
            // Interactive OST Card with Quick Actions
            val borderGradient = Brush.horizontalGradient(
                listOf(
                    Color(0xFF6C5CE7).copy(alpha = 0.7f),
                    spotifyGreen.copy(alpha = 0.5f),
                    youtubeRed.copy(alpha = 0.5f)
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F1522))
                    .border(1.2.dp, borderGradient, RoundedCornerShape(12.dp))
                    .padding(14.dp)
                    .testTag("official_ost_card")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Album & Composer Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E1738))
                                .border(1.dp, Color(0xFF6C5CE7).copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = Color(0xFFA29BFE),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ostInfo.albumTitle,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (ostInfo.composer.isNotBlank()) "Composed by ${ostInfo.composer}" else "Official Video Game Soundtrack",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (!ostInfo.releaseYear.isNullOrBlank()) {
                                    Text(
                                        text = "• ${ostInfo.releaseYear}",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                                if (ostInfo.source.isNotBlank()) {
                                    Text(
                                        text = "• ${ostInfo.source}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = NeonCyan
                                    )
                                }
                            }
                        }
                    }

                    // Quick Action Buttons: YouTube Music, Spotify & Discogs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // YouTube Music Quick Action
                        val ytInteraction = remember { MutableInteractionSource() }
                        val isYtPressed by ytInteraction.collectIsPressedAsState()
                        val ytScale by animateFloatAsState(
                            targetValue = if (isYtPressed) 0.95f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "yt_scale"
                        )

                        Button(
                            onClick = {
                                OfficialOstCatalog.openYouTubeMusic(context, ostInfo.youtubeMusicUrl)
                            },
                            interactionSource = ytInteraction,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF261214),
                                contentColor = Color(0xFFFF5252)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f)),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 8.dp,
                                vertical = 8.dp
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .graphicsLayer {
                                    scaleX = ytScale
                                    scaleY = ytScale
                                }
                                .testTag("ost_youtube_music_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "YouTube Music",
                                    tint = Color(0xFFFF0000),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (!ostInfo.discogsUrl.isNullOrBlank()) "YT Music" else "YouTube Music",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }

                        // Spotify Quick Action
                        val spotifyInteraction = remember { MutableInteractionSource() }
                        val isSpotifyPressed by spotifyInteraction.collectIsPressedAsState()
                        val spotifyScale by animateFloatAsState(
                            targetValue = if (isSpotifyPressed) 0.95f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "spotify_scale"
                        )

                        Button(
                            onClick = {
                                OfficialOstCatalog.openSpotify(context, ostInfo.spotifyUrl)
                            },
                            interactionSource = spotifyInteraction,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0F2618),
                                contentColor = spotifyGreen
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, spotifyGreen.copy(alpha = 0.5f)),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 8.dp,
                                vertical = 8.dp
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .graphicsLayer {
                                    scaleX = spotifyScale
                                    scaleY = spotifyScale
                                }
                                .testTag("ost_spotify_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Headphones,
                                    contentDescription = "Spotify",
                                    tint = spotifyGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Spotify",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }

                        // Discogs Quick Action if available
                        if (!ostInfo.discogsUrl.isNullOrBlank()) {
                            val discogsInteraction = remember { MutableInteractionSource() }
                            val isDiscogsPressed by discogsInteraction.collectIsPressedAsState()
                            val discogsScale by animateFloatAsState(
                                targetValue = if (isDiscogsPressed) 0.95f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "discogs_scale"
                            )

                            Button(
                                onClick = {
                                    OfficialOstCatalog.openDiscogs(context, ostInfo.discogsUrl)
                                },
                                interactionSource = discogsInteraction,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF26190F),
                                    contentColor = discogsOrange
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, discogsOrange.copy(alpha = 0.5f)),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                    horizontal = 8.dp,
                                    vertical = 8.dp
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .graphicsLayer {
                                        scaleX = discogsScale
                                        scaleY = discogsScale
                                    }
                                    .testTag("ost_discogs_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = "Discogs",
                                        tint = discogsOrange,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Discogs",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else if (isLoading) {
            // Loading State while fetching Discogs API / Spotify Web API
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F1522))
                    .border(1.dp, GamingCardBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = NeonCyan,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Querying Discogs API & Spotify Web API for official soundtrack...",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        } else {
            // Muted Card explicitly marking "No OST"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F1522))
                    .border(1.dp, GamingCardBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
                    .testTag("official_ost_none_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicOff,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "No official soundtrack release found for this item.",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                    Text(
                        text = "No OST",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}
