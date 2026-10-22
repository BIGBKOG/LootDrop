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
import androidx.compose.ui.graphics.SolidColor
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
import com.example.ui.density.LocalScreenDensityInfo
import com.example.ui.theme.NeonCyan
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
    isAnimationsEnabled: Boolean = true,
    isLowEndOptimization: Boolean = false,
    beamAngle: Float = 0f,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val screenDensityInfo = LocalScreenDensityInfo.current
    val launcherColor = Color(item.launcherCategory.badgeColorHex)

    val cardAngleOffset = remember(item.id) {
        val h = item.id.hashCode()
        ((if (h == Int.MIN_VALUE) 0 else kotlin.math.abs(h)) % 360).toFloat()
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale = if (isAnimationsEnabled && !isLowEndOptimization) {
        val anim by animateFloatAsState(
            targetValue = if (isPressed) 0.985f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "card_press_scale"
        )
        anim
    } else {
        if (isPressed) 0.985f else 1f
    }

    // Pulsing breathing animation ONLY if item is new and animations are enabled (avoids ticking for non-new cards)
    val shouldAnimatePulse = item.isNew && isAnimationsEnabled && !isLowEndOptimization
    val pulseGlow = if (shouldAnimatePulse) {
        val infiniteTransition = rememberInfiniteTransition(label = "card_infinite_${item.id}")
        val glow by infiniteTransition.animateFloat(
            initialValue = 0.65f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_glow"
        )
        glow
    } else 1.0f

    val pulseScale = if (shouldAnimatePulse) {
        val infiniteTransition = rememberInfiniteTransition(label = "card_infinite_scale_${item.id}")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.96f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )
        scale
    } else 1.0f

    // Fluid Claimed status animation
    val claimedScale = if (isAnimationsEnabled && !isLowEndOptimization) {
        val anim by animateFloatAsState(
            targetValue = if (item.isClaimed) 1.15f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
            label = "claimed_scale"
        )
        anim
    } else {
        if (item.isClaimed) 1.15f else 1f
    }

    val claimInteractionSource = remember { MutableInteractionSource() }
    val isClaimPressed by claimInteractionSource.collectIsPressedAsState()
    val claimButtonScale = if (isAnimationsEnabled && !isLowEndOptimization) {
        val anim by animateFloatAsState(
            targetValue = if (isClaimPressed) 0.94f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "claim_button_press_scale"
        )
        anim
    } else {
        if (isClaimPressed) 0.94f else 1f
    }

    val themePrimary = MaterialTheme.colorScheme.primary
    val themeSecondary = MaterialTheme.colorScheme.secondary

    // Expressive M3 popping outlines: dynamically synced to the user's chosen theme!
    val expressiveBorderColor = when {
        item.isClaimed -> NeonGreen
        item.isNew -> Color(0xFFFF9100)
        item.isBookmarked -> themePrimary
        else -> themePrimary
    }

    val cardBorderColor = if (isAnimationsEnabled && !isLowEndOptimization) {
        val anim by animateColorAsState(
            targetValue = expressiveBorderColor,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "card_border_color"
        )
        anim
    } else expressiveBorderColor

    val cardBorderWidth = if (item.isClaimed || item.isNew || item.isBookmarked || isPressed) 2.2.dp else 1.8.dp

    when (cardDensity) {
        CardDensity.MINIMAL_LIST -> {
            // Minimal Streamlined List Card (~72dp) with expressive popping outline & moving bright beam
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .then(if (cardScale != 1f) Modifier.graphicsLayer { scaleX = cardScale; scaleY = cardScale } else Modifier)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                    .borderBeam(
                        strokeWidth = cardBorderWidth,
                        cornerRadius = 16.dp,
                        borderColor = cardBorderColor,
                        beamAngleProvider = { (beamAngle + cardAngleOffset) % 360f },
                        isAnimated = isAnimationsEnabled && !isLowEndOptimization
                    )
                    .testTag("giveaway_card_minimal_${item.id}"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141924)),
                shape = RoundedCornerShape(16.dp)
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
                        val minimalImageRequest = remember(item.id, item.thumbnail, item.image, isLowEndOptimization) {
                            ImageRequest.Builder(context)
                                .data(if (item.thumbnail.isNotBlank()) item.thumbnail else item.image)
                                .crossfade(!isLowEndOptimization)
                                .size(if (isLowEndOptimization) 100 else 150, if (isLowEndOptimization) 100 else 150)
                                .build()
                        }
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = minimalImageRequest,
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
                    .then(if (cardScale != 1f) Modifier.graphicsLayer { scaleX = cardScale; scaleY = cardScale } else Modifier)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                    .borderBeam(
                        strokeWidth = cardBorderWidth,
                        cornerRadius = 16.dp,
                        borderColor = cardBorderColor,
                        beamAngleProvider = { (beamAngle + cardAngleOffset) % 360f },
                        isAnimated = isAnimationsEnabled && !isLowEndOptimization
                    )
                    .testTag("giveaway_card_compact_${item.id}"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141924)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                ) {
                    // Left thumbnail
                    val compactImageRequest = remember(item.id, item.thumbnail, item.image, isLowEndOptimization) {
                        ImageRequest.Builder(context)
                            .data(if (item.thumbnail.isNotBlank()) item.thumbnail else item.image)
                            .crossfade(!isLowEndOptimization)
                            .size(if (isLowEndOptimization) 240 else 375, if (isLowEndOptimization) 190 else 300)
                            .build()
                    }
                    Box(
                        modifier = Modifier
                            .width(if (screenDensityInfo.isNarrowScreen) 110.dp else 125.dp)
                            .fillMaxHeight()
                    ) {
                        AsyncImage(
                            model = compactImageRequest,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Launcher Tag with popping outline
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(6.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(launcherColor.copy(alpha = 0.95f))
                                .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
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
                                .border(1.2.dp, launcherColor.copy(alpha = 0.9f), CircleShape)
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
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = item.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth()
                                )
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
            // Full Hero Cover Card with expressive popping outline
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .then(if (cardScale != 1f) Modifier.graphicsLayer { scaleX = cardScale; scaleY = cardScale } else Modifier)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                    .borderBeam(
                        strokeWidth = cardBorderWidth,
                        cornerRadius = 20.dp,
                        borderColor = cardBorderColor,
                        beamAngleProvider = { (beamAngle + cardAngleOffset) % 360f },
                        isAnimated = isAnimationsEnabled && !isLowEndOptimization
                    )
                    .testTag("giveaway_card_${item.id}"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141924)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column {
                    // Thumbnail Header
                    val richImageRequest = remember(item.id, item.image, item.thumbnail, isLowEndOptimization) {
                        ImageRequest.Builder(context)
                            .data(if (item.image.isNotBlank()) item.image else item.thumbnail)
                            .crossfade(!isLowEndOptimization)
                            .size(if (isLowEndOptimization) 480 else 800, if (isLowEndOptimization) 270 else 450)
                            .build()
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(screenDensityInfo.cardCoverHeight)
                    ) {
                        AsyncImage(
                            model = richImageRequest,
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

                        // Top row: Launcher & Type badges on the left, Animated Bookmark on the right
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
                            Row(
                                modifier = Modifier.weight(1f, fill = false),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(launcherColor.copy(alpha = 0.95f))
                                        .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
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
                                            .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
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
                                            .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
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
                                            .border(1.dp, Color(0xFF0B0F19).copy(alpha = 0.35f), RoundedCornerShape(8.dp))
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
                                        .border(1.dp, launcherColor.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
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

                            // Quick Bookmark button with bouncy spring scale pop
                            val bookmarkInteraction = remember { MutableInteractionSource() }
                            val isBookmarkPressed by bookmarkInteraction.collectIsPressedAsState()
                            val cardBookmarkScale = if (isAnimationsEnabled && !isLowEndOptimization) {
                                val anim by animateFloatAsState(
                                    targetValue = when {
                                        isBookmarkPressed -> 0.85f
                                        item.isBookmarked -> 1.18f
                                        else -> 1.0f
                                    },
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    label = "card_bookmark_scale"
                                )
                                anim
                            } else {
                                when {
                                    isBookmarkPressed -> 0.85f
                                    item.isBookmarked -> 1.18f
                                    else -> 1.0f
                                }
                            }
                            IconButton(
                                onClick = onToggleBookmark,
                                interactionSource = bookmarkInteraction,
                                modifier = Modifier
                                    .size(32.dp)
                                    .then(if (cardBookmarkScale != 1f) Modifier.graphicsLayer { scaleX = cardBookmarkScale; scaleY = cardBookmarkScale } else Modifier)
                                    .clip(CircleShape)
                                    .background(if (item.isBookmarked) themePrimary.copy(alpha = 0.28f) else Color(0xD90B0F19))
                                    .border(
                                        1.2.dp,
                                        if (item.isBookmarked) themePrimary else Color.White.copy(alpha = 0.35f),
                                        CircleShape
                                    )
                                    .testTag("bookmark_button_${item.id}")
                            ) {
                                Icon(
                                    imageVector = if (item.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = if (item.isBookmarked) "Bookmarked" else "Bookmark",
                                    tint = if (item.isBookmarked) themePrimary else Color.White,
                                    modifier = Modifier.size(16.dp)
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
                                    .border(1.2.dp, launcherColor.copy(alpha = 0.9f), CircleShape)
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
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xD90B0F19))
                                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = item.worth,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFCBD5E1),
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                }
                            }

                            if (item.isNew) {
                                Box(
                                    modifier = Modifier
                                        .then(
                                            if (pulseScale != 1f || pulseGlow != 1f) {
                                                Modifier.graphicsLayer {
                                                    scaleX = pulseScale
                                                    scaleY = pulseScale
                                                    alpha = pulseGlow
                                                }
                                            } else Modifier
                                        )
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFFF9100).copy(alpha = 0.25f))
                                        .border(1.2.dp, Color(0xFFFF9100), RoundedCornerShape(6.dp))
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

                            val claimedBorderColor = if (isAnimationsEnabled && !isLowEndOptimization) {
                                val anim by animateColorAsState(
                                    targetValue = if (item.isClaimed) NeonGreen else MaterialTheme.colorScheme.outline,
                                    label = "claimed_border"
                                )
                                anim
                            } else {
                                if (item.isClaimed) NeonGreen else MaterialTheme.colorScheme.outline
                            }

                            val haveItInteraction = remember { MutableInteractionSource() }
                            val isHaveItPressed by haveItInteraction.collectIsPressedAsState()
                            val haveItScale = if (isAnimationsEnabled && !isLowEndOptimization) {
                                val anim by animateFloatAsState(
                                    targetValue = if (isHaveItPressed) 0.93f else 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    label = "have_it_scale"
                                )
                                anim
                            } else {
                                if (isHaveItPressed) 0.93f else 1f
                            }

                            OutlinedButton(
                                onClick = onToggleClaimed,
                                interactionSource = haveItInteraction,
                                modifier = Modifier
                                    .height(44.dp)
                                    .then(if (haveItScale != 1f) Modifier.graphicsLayer { scaleX = haveItScale; scaleY = haveItScale } else Modifier)
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
                                        modifier = Modifier
                                            .size(16.dp)
                                            .then(if (claimedScale != 1f) Modifier.graphicsLayer { scaleX = claimedScale; scaleY = claimedScale } else Modifier),
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
