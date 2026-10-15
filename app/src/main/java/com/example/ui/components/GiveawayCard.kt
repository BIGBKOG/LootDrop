package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.GiveawayItem
import com.example.data.model.LauncherType
import com.example.preferences.CardDensity
import com.example.ui.theme.NeonGreen

@Composable
fun GiveawayCard(
    item: GiveawayItem,
    cardDensity: CardDensity = CardDensity.RICH_CARDS,
    showOriginalPrice: Boolean = true,
    showExpiration: Boolean = true,
    onClick: () -> Unit,
    onClaimClick: () -> Unit,
    onToggleBookmark: () -> Unit = {},
    onToggleClaimed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val launcherColor = Color(item.launcherCategory.badgeColorHex)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_press_scale"
    )

    // Pulsing breathing animation for fresh new drops
    val infiniteTransition = rememberInfiniteTransition(label = "card_infinite")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Fluid Claimed status animation
    val claimedScale by animateFloatAsState(
        targetValue = if (item.isClaimed) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "claimed_scale"
    )

    val claimInteractionSource = remember { MutableInteractionSource() }
    val isClaimPressed by claimInteractionSource.collectIsPressedAsState()
    val claimButtonScale by animateFloatAsState(
        targetValue = if (isClaimPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "claim_button_press_scale"
    )

    val cardBorderColor by animateColorAsState(
        targetValue = when {
            item.isClaimed -> NeonGreen.copy(alpha = 0.65f)
            item.isNew -> MaterialTheme.colorScheme.primary.copy(alpha = pulseGlow)
            else -> MaterialTheme.colorScheme.outline
        },
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "card_border_color"
    )

    when (cardDensity) {
        CardDensity.MINIMAL_LIST -> {
            // Minimal Streamlined List Card (~72dp)
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = cardScale
                        scaleY = cardScale
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                    .border(
                        width = 1.dp,
                        color = cardBorderColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .testTag("giveaway_card_minimal_${item.id}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Square thumbnail with info icon
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(if (item.thumbnail.isNotBlank()) item.thumbnail else item.image)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = item.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Little info symbol in bottom left
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(2.dp)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xD90B0F19))
                                    .clickable(onClick = onClick)
                                    .testTag("info_symbol_minimal_${item.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = "Game info for ${item.title}",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(launcherColor.copy(alpha = 0.2f))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = item.launcherCategory.displayName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = launcherColor
                                    )
                                }
                                if (showOriginalPrice && item.worth.isNotBlank() && item.worth != "N/A" && item.worth != "FREE") {
                                    Text(
                                        text = item.worth,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = item.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = item.platforms,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Trailing claim and library toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onToggleClaimed,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("library_toggle_minimal_${item.id}")
                        ) {
                            Icon(
                                imageVector = if (item.isClaimed) Icons.Default.CheckCircle else Icons.Outlined.CheckCircleOutline,
                                contentDescription = if (item.isClaimed) "In Library" else "Add to Library",
                                tint = if (item.isClaimed) NeonGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(18.dp)
                                    .graphicsLayer {
                                        scaleX = claimedScale
                                        scaleY = claimedScale
                                    }
                            )
                        }

                        Button(
                            onClick = onClaimClick,
                            interactionSource = claimInteractionSource,
                            modifier = Modifier
                                .height(32.dp)
                                .graphicsLayer {
                                    scaleX = claimButtonScale
                                    scaleY = claimButtonScale
                                }
                                .testTag("claim_minimal_${item.id}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.background
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = if (item.hasMultipleLaunchers) "Launchers ▾" else "Claim",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        CardDensity.COMPACT_CARDS -> {
            // Compact Row Card (115dp)
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = cardScale
                        scaleY = cardScale
                    }
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                    .border(
                        width = if (item.isNew) 1.5.dp else 1.dp,
                        color = cardBorderColor,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .testTag("giveaway_card_compact_${item.id}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                ) {
                    // Left thumbnail
                    Box(
                        modifier = Modifier
                            .width(125.dp)
                            .fillMaxHeight()
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(if (item.thumbnail.isNotBlank()) item.thumbnail else item.image)
                                .crossfade(true)
                                .build(),
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Launcher Tag
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(6.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(launcherColor.copy(alpha = 0.95f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            val isDarkBrand = item.launcherCategory == LauncherType.PLAYSTATION || 
                                              item.launcherCategory == LauncherType.XBOX || 
                                              item.launcherCategory == LauncherType.EPIC
                            Text(
                                text = item.launcherCategory.displayName,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkBrand) Color.White else Color(0xFF0B0F19)
                            )
                        }

                        // Info Symbol Button (brings you to the info page)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(6.dp)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xD90B0F19))
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), CircleShape)
                                .clickable(
                                    onClick = onClick,
                                    onClickLabel = "View info for ${item.title}"
                                )
                                .testTag("info_symbol_compact_${item.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "Game info for ${item.title}",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Right details
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = item.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(
                                    onClick = onToggleClaimed,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .testTag("library_toggle_compact_${item.id}")
                                ) {
                                    Icon(
                                        imageVector = if (item.isClaimed) Icons.Default.CheckCircle else Icons.Outlined.CheckCircleOutline,
                                        contentDescription = if (item.isClaimed) "In Library" else "Add to Library",
                                        tint = if (item.isClaimed) NeonGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .graphicsLayer {
                                                scaleX = claimedScale
                                                scaleY = claimedScale
                                            }
                                    )
                                }
                            }

                            Text(
                                text = item.description,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (showOriginalPrice && item.worth.isNotBlank() && item.worth != "N/A" && item.worth != "FREE") {
                                Text(
                                    text = item.worth,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    textDecoration = TextDecoration.LineThrough
                                )
                            } else {
                                Text(item.type, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Button(
                                onClick = onClaimClick,
                                interactionSource = claimInteractionSource,
                                modifier = Modifier
                                    .height(32.dp)
                                    .graphicsLayer {
                                        scaleX = claimButtonScale
                                        scaleY = claimButtonScale
                                    }
                                    .testTag("claim_compact_${item.id}"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.background
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (item.hasMultipleLaunchers) Icons.Default.SportsEsports else Icons.Default.OpenInNew,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = if (item.hasMultipleLaunchers) "Launchers ▾" else "Claim",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        CardDensity.RICH_CARDS -> {
            // Full Hero Cover Card
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = cardScale
                        scaleY = cardScale
                    }
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                    .border(
                        width = if (item.isNew) 1.5.dp else 1.dp,
                        color = cardBorderColor,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .testTag("giveaway_card_${item.id}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    // Thumbnail Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(if (item.image.isNotBlank()) item.image else item.thumbnail)
                                .crossfade(true)
                                .build(),
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0x60000000),
                                            Color(0xD9000000)
                                        )
                                    )
                                )
                        )

                        // Top row badges: Launcher & Type & Bookmark
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isDarkBrand = item.launcherCategory == LauncherType.PLAYSTATION || 
                                              item.launcherCategory == LauncherType.XBOX || 
                                              item.launcherCategory == LauncherType.EPIC
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(launcherColor.copy(alpha = 0.9f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = item.launcherCategory.displayName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDarkBrand) Color.White else Color(0xFF0B0F19)
                                    )
                                }

                                if (item.isPlayStation && item.launcherCategory != LauncherType.PLAYSTATION) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF0070D1))
                                            .padding(horizontal = 7.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "PlayStation",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                if (item.isXbox && item.launcherCategory != LauncherType.XBOX) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF107C10))
                                            .padding(horizontal = 7.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Xbox",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                if (item.hasMultipleLaunchers) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(NeonGreen)
                                            .padding(horizontal = 7.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Multi-Launcher 100% Off",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF0B0F19)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xCC1A2333))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = item.type,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                            }

                            IconButton(
                                onClick = onToggleClaimed,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xCC131926))
                                    .testTag("library_toggle_button_${item.id}")
                            ) {
                                Icon(
                                    imageVector = if (item.isClaimed) Icons.Default.CheckCircle else Icons.Outlined.CheckCircleOutline,
                                    contentDescription = if (item.isClaimed) "In Library" else "Add to Library",
                                    tint = if (item.isClaimed) NeonGreen else Color.White,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .graphicsLayer {
                                            scaleX = claimedScale
                                            scaleY = claimedScale
                                        }
                                )
                            }
                        }

                        // Bottom row inside image: Info symbol + Price + NEW tag
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Info Symbol Button (brings you to the info page)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xD90B0F19))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), CircleShape)
                                    .clickable(
                                        onClick = onClick,
                                        onClickLabel = "View info for ${item.title}"
                                    )
                                    .testTag("info_symbol_${item.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = "Game info for ${item.title}",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            if (showOriginalPrice && item.worth.isNotBlank() && item.worth != "N/A" && item.worth != "FREE") {
                                Text(
                                    text = item.worth,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFCBD5E1),
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }

                            if (item.isNew) {
                                Box(
                                    modifier = Modifier
                                        .graphicsLayer {
                                            scaleX = pulseScale
                                            scaleY = pulseScale
                                            alpha = pulseGlow
                                        }
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "✨ NEW DROP",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // Card Body
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = item.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.description,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Metadata
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SportsEsports,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = item.platforms,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 160.dp)
                                )
                            }

                            if (showExpiration && item.endDate.isNotBlank() && item.endDate != "N/A") {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Ends: ${item.endDate.split(" ").firstOrNull() ?: item.endDate}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = onClaimClick,
                                interactionSource = claimInteractionSource,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .graphicsLayer {
                                        scaleX = claimButtonScale
                                        scaleY = claimButtonScale
                                    }
                                    .testTag("claim_button_${item.id}"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.background
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (item.hasMultipleLaunchers) Icons.Default.SportsEsports else Icons.Default.OpenInNew,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (item.hasMultipleLaunchers) "Choose Launcher ▾" else "Claim",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            val claimedBorderColor by animateColorAsState(
                                targetValue = if (item.isClaimed) NeonGreen else MaterialTheme.colorScheme.outline,
                                label = "claimed_border"
                            )
                            OutlinedButton(
                                onClick = onToggleClaimed,
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("claimed_toggle_${item.id}"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (item.isClaimed) NeonGreen.copy(alpha = 0.15f) else Color.Transparent,
                                    contentColor = if (item.isClaimed) NeonGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, claimedBorderColor)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (item.isClaimed) Icons.Default.CheckCircle else Icons.Outlined.CheckCircleOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (item.isClaimed) NeonGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (item.isClaimed) "Claimed" else "Have It?",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
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
