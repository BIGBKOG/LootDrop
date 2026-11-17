package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.GiveawayItem
import com.example.data.model.LauncherType
import com.example.data.model.PlayStatus
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.i18n.localizedDisplayName
import com.example.ui.density.ScreenDensityInfo
import com.example.ui.theme.NeonGreen

@Composable
internal fun MinimalGiveawayCard(
    item: GiveawayItem,
    context: Context,
    launcherColor: Color,
    cardScale: Float,
    cardBorderWidth: Dp,
    cardBorderColor: Color,
    beamAngle: Float,
    cardAngleOffset: Float,
    showBorderBeam: Boolean,
    claimedScale: Float,
    claimButtonScale: Float,
    interactionSource: MutableInteractionSource,
    claimInteractionSource: MutableInteractionSource,
    showOriginalPrice: Boolean,
    isLowEndOptimization: Boolean,
    onClick: () -> Unit,
    onClaimClick: () -> Unit,
    onToggleClaimed: () -> Unit,
    onPlayStatusChange: ((PlayStatus) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (cardScale != 1f) Modifier.graphicsLayer { scaleX = cardScale; scaleY = cardScale } else Modifier)
            .clip(RoundedCornerShape(16.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .borderBeam(cardBorderWidth, 16.dp, cardBorderColor, { (beamAngle + cardAngleOffset) % 360f }, showBorderBeam)
            .testTag("giveaway_card_minimal_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val minimalRequest = remember(item.id, item.thumbnail, item.image, isLowEndOptimization) {
                    buildGiveawayImageRequest(context, item.thumbnail.ifBlank { item.image }, isLowEndOptimization, if (isLowEndOptimization) 80 else 150, if (isLowEndOptimization) 80 else 150)
                }
                Box(modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp))) {
                    AsyncImage(model = minimalRequest, contentDescription = item.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
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
                            Icons.Outlined.Info,
                            contentDescription = if (isEs) "Detalles de ${item.title}" else "Game info for ${item.title}",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(launcherColor.copy(alpha = 0.2f)).padding(horizontal = 5.dp, vertical = 1.dp)) {
                            Text(item.launcherCategory.localizedDisplayName(s), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = launcherColor)
                        }
                        MetacriticScoreCircle(score = item.metacriticScore, size = 18.dp)
                        if (item.isWishlisted) WishlistBadgePill()
                        if (item.isClaimed) PlayStatusBadge(status = item.playStatus, onStatusSelected = onPlayStatusChange)
                        if (showOriginalPrice && item.worth.isNotBlank() && item.worth != "N/A" && item.worth != "FREE") {
                            Text(item.worth, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), textDecoration = TextDecoration.LineThrough)
                        }
                    }
                    if (item.hasSteamTelemetry) {
                        Spacer(modifier = Modifier.height(2.dp))
                        SteamTelemetryBadge(item = item)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(item.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(item.platforms, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onToggleClaimed, modifier = Modifier.size(32.dp).testTag("library_toggle_minimal_${item.id}")) {
                    Icon(
                        imageVector = if (item.isClaimed) Icons.Default.CheckCircle else Icons.Outlined.CheckCircleOutline,
                        contentDescription = if (item.isClaimed) s.home.badgeInVault else (if (isEs) "Añadir a Bóveda" else "Add to Library"),
                        tint = if (item.isClaimed) NeonGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp).graphicsLayer { scaleX = claimedScale; scaleY = claimedScale }
                    )
                }
                Button(
                    onClick = onClaimClick,
                    interactionSource = claimInteractionSource,
                    modifier = Modifier.height(32.dp).graphicsLayer { scaleX = claimButtonScale; scaleY = claimButtonScale }.testTag("claim_minimal_${item.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = if (item.hasMultipleLaunchers) {
                            if (isEs) "Tiendas ▾" else "Launchers ▾"
                        } else {
                            if (isEs) "Reclamar" else "Claim"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
internal fun CompactGiveawayCard(
    item: GiveawayItem,
    context: Context,
    screenDensityInfo: ScreenDensityInfo,
    launcherColor: Color,
    cardScale: Float,
    cardBorderWidth: Dp,
    cardBorderColor: Color,
    beamAngle: Float,
    cardAngleOffset: Float,
    showBorderBeam: Boolean,
    claimButtonScale: Float,
    interactionSource: MutableInteractionSource,
    claimInteractionSource: MutableInteractionSource,
    showOriginalPrice: Boolean,
    isLowEndOptimization: Boolean,
    onClick: () -> Unit,
    onClaimClick: () -> Unit,
    onPlayStatusChange: ((PlayStatus) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (cardScale != 1f) Modifier.graphicsLayer { scaleX = cardScale; scaleY = cardScale } else Modifier)
            .clip(RoundedCornerShape(16.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .borderBeam(cardBorderWidth, 16.dp, cardBorderColor, { (beamAngle + cardAngleOffset) % 360f }, showBorderBeam)
            .testTag("giveaway_card_compact_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(115.dp)) {
            val compactRequest = remember(item.id, item.thumbnail, item.image, isLowEndOptimization) {
                buildGiveawayImageRequest(context, item.thumbnail.ifBlank { item.image }, isLowEndOptimization, if (isLowEndOptimization) 180 else 375, if (isLowEndOptimization) 140 else 300)
            }
            Box(modifier = Modifier.width(if (screenDensityInfo.isNarrowScreen) 110.dp else 125.dp).fillMaxHeight()) {
                AsyncImage(model = compactRequest, contentDescription = item.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                Row(modifier = Modifier.align(Alignment.TopStart).padding(5.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    val isDarkBrand = item.launcherCategory == LauncherType.PLAYSTATION || item.launcherCategory == LauncherType.XBOX || item.launcherCategory == LauncherType.EPIC
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(launcherColor.copy(alpha = 0.95f))
                            .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(item.launcherCategory.localizedDisplayName(s), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isDarkBrand) Color.White else Color(0xFF0B0F19))
                    }
                    MetacriticScoreCircle(score = item.metacriticScore, size = 18.dp)
                }
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
                            onClickLabel = if (isEs) "Ver detalles de ${item.title}" else "View info for ${item.title}"
                        )
                        .testTag("info_symbol_compact_${item.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = if (isEs) "Detalles de ${item.title}" else "Game info for ${item.title}",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column(modifier = Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(item.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(item.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(modifier = Modifier.weight(1f, fill = false), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (item.isWishlisted) WishlistBadgePill()
                        if (item.isClaimed) PlayStatusBadge(status = item.playStatus, onStatusSelected = onPlayStatusChange)
                        if (showOriginalPrice && item.worth.isNotBlank() && item.worth != "N/A" && item.worth != "FREE") {
                            Text(item.worth, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), textDecoration = TextDecoration.LineThrough)
                        }
                    }
                    Button(
                        onClick = onClaimClick,
                        interactionSource = claimInteractionSource,
                        modifier = Modifier.height(32.dp).graphicsLayer { scaleX = claimButtonScale; scaleY = claimButtonScale }.testTag("claim_compact_${item.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(if (item.hasMultipleLaunchers) Icons.Default.SportsEsports else Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(12.dp))
                            Text(
                                text = if (item.hasMultipleLaunchers) {
                                    if (isEs) "Tiendas ▾" else "Launchers ▾"
                                } else {
                                    if (isEs) "Reclamar" else "Claim"
                                },
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
