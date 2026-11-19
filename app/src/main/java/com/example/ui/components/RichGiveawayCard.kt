package com.example.ui.components

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
internal fun RichGiveawayCard(
    item: GiveawayItem,
    context: Context,
    screenDensityInfo: ScreenDensityInfo,
    launcherColor: Color,
    themePrimary: Color,
    cardScale: Float,
    cardBorderWidth: Dp,
    cardBorderColor: Color,
    beamAngle: Float,
    cardAngleOffset: Float,
    showBorderBeam: Boolean,
    pulseGlow: Float,
    pulseScale: Float,
    claimedScale: Float,
    claimButtonScale: Float,
    interactionSource: MutableInteractionSource,
    claimInteractionSource: MutableInteractionSource,
    showOriginalPrice: Boolean,
    showExpiration: Boolean,
    isAnimationsEnabled: Boolean,
    isLowEndOptimization: Boolean,
    onClick: () -> Unit,
    onClaimClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleClaimed: () -> Unit,
    onPlayStatusChange: ((PlayStatus) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    val lang = s.language
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (cardScale != 1f) Modifier.graphicsLayer { scaleX = cardScale; scaleY = cardScale } else Modifier)
            .clip(RoundedCornerShape(20.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .borderBeam(cardBorderWidth, 20.dp, cardBorderColor, { (beamAngle + cardAngleOffset) % 360f }, showBorderBeam)
            .testTag("giveaway_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column {
            val richRequest = remember(item.id, item.image, item.thumbnail, isLowEndOptimization) {
                buildGiveawayImageRequest(context, item.image.ifBlank { item.thumbnail }, isLowEndOptimization, if (isLowEndOptimization) 360 else 800, if (isLowEndOptimization) 200 else 450)
            }
            Box(modifier = Modifier.fillMaxWidth().height(screenDensityInfo.cardCoverHeight)) {
                AsyncImage(model = richRequest, contentDescription = item.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color.Transparent, Color(0x60000000), Color(0xD9000000)))
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isDarkBrand = item.launcherCategory in setOf(LauncherType.PLAYSTATION, LauncherType.XBOX, LauncherType.EPIC, LauncherType.IOS)
                    Row(modifier = Modifier.weight(1f, fill = false), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(launcherColor.copy(alpha = 0.95f))
                                .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(item.launcherCategory.localizedDisplayName(s), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isDarkBrand) Color.White else Color(0xFF0B0F19))
                        }
                        MetacriticScoreCircle(score = item.metacriticScore, size = 22.dp)
                        ExtraPlatformPills(item = item)
                        if (item.hasMultipleLaunchers) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NeonGreen)
                                    .border(1.dp, Color(0xFF0B0F19).copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 7.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = when (lang) {
                                        AppLanguage.SPANISH -> "Multi-Tienda 100% Gratis"
                                        AppLanguage.FRENCH -> "Multi-lanceur 100% gratuit"
                                        AppLanguage.ENGLISH -> "Multi-Launcher 100% Off"
                                    },
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
                            Text(item.type, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.White)
                        }
                    }

                    val bookmarkInteraction = remember { MutableInteractionSource() }
                    val isBookmarkPressed by bookmarkInteraction.collectIsPressedAsState()
                    val cardBookmarkScale = if (isAnimationsEnabled && !isLowEndOptimization) {
                        val anim by animateFloatAsState(
                            targetValue = when { isBookmarkPressed -> 0.85f; item.isBookmarked -> 1.18f; else -> 1.0f },
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                            label = "card_bookmark_scale"
                        )
                        anim
                    } else when { isBookmarkPressed -> 0.85f; item.isBookmarked -> 1.18f; else -> 1.0f }

                    IconButton(
                        onClick = onToggleBookmark,
                        interactionSource = bookmarkInteraction,
                        modifier = Modifier
                            .size(32.dp)
                            .then(if (cardBookmarkScale != 1f) Modifier.graphicsLayer { scaleX = cardBookmarkScale; scaleY = cardBookmarkScale } else Modifier)
                            .clip(CircleShape)
                            .background(if (item.isBookmarked) themePrimary.copy(alpha = 0.28f) else Color(0xD90B0F19))
                            .border(1.2.dp, if (item.isBookmarked) themePrimary else Color.White.copy(alpha = 0.35f), CircleShape)
                            .testTag("bookmark_button_${item.id}")
                    ) {
                        Icon(
                            imageVector = if (item.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (item.isBookmarked) {
                                when (lang) {
                                    AppLanguage.SPANISH -> "Guardado"
                                    AppLanguage.FRENCH -> "Enregistré"
                                    AppLanguage.ENGLISH -> "Bookmarked"
                                }
                            } else s.home.cardBookmarkCd,
                            tint = if (item.isBookmarked) themePrimary else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.align(Alignment.BottomStart).padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xD90B0F19))
                            .border(1.2.dp, launcherColor.copy(alpha = 0.9f), CircleShape)
                            .clickable(
                                onClick = onClick,
                                onClickLabel = when (lang) {
                                    AppLanguage.SPANISH -> "Ver detalles de ${item.title}"
                                    AppLanguage.FRENCH -> "Voir les infos de ${item.title}"
                                    AppLanguage.ENGLISH -> "View info for ${item.title}"
                                }
                            )
                            .testTag("info_symbol_${item.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = when (lang) {
                                AppLanguage.SPANISH -> "Detalles de ${item.title}"
                                AppLanguage.FRENCH -> "Infos de ${item.title}"
                                AppLanguage.ENGLISH -> "Game info for ${item.title}"
                            },
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
                            Text(item.worth, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFCBD5E1), textDecoration = TextDecoration.LineThrough)
                        }
                    }
                    if (item.isWishlisted) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFF2D87).copy(alpha = 0.35f))
                                .border(1.2.dp, Color(0xFFFF2D87), RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = when (lang) {
                                    AppLanguage.SPANISH -> "❤️ EN TU LISTA DE DESEOS"
                                    AppLanguage.FRENCH -> "❤️ SUR VOTRE LISTE"
                                    AppLanguage.ENGLISH -> "❤️ ON YOUR WISHLIST"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFF2D87),
                                letterSpacing = 0.4.sp
                            )
                        }
                    }
                    if (item.isNew) {
                        Box(
                            modifier = Modifier
                                .then(if (pulseScale != 1f || pulseGlow != 1f) Modifier.graphicsLayer { scaleX = pulseScale; scaleY = pulseScale; alpha = pulseGlow } else Modifier)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFF9100).copy(alpha = 0.25f))
                                .border(1.2.dp, Color(0xFFFF9100), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = when (lang) {
                                    AppLanguage.SPANISH -> "✨ NUEVO DROP"
                                    AppLanguage.FRENCH -> "✨ NOUVEAU DROP"
                                    AppLanguage.ENGLISH -> "✨ NEW DROP"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                Text(item.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(6.dp))
                Text(item.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 18.sp)

                if (item.categories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        item.categories.take(3).forEach { cat ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(cat.colorHex).copy(alpha = 0.15f))
                                    .border(0.8.dp, Color(cat.colorHex).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text("${cat.iconEmoji} ${cat.localizedDisplayName(s)}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(cat.colorHex))
                            }
                        }
                        if (item.categories.size > 3) {
                            Text("+${item.categories.size - 3}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.SportsEsports, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Text(item.platforms, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 160.dp))
                    }
                    if (showExpiration && item.endDate.isNotBlank() && item.endDate != "N/A") {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                            val endsLabel = when (lang) {
                                AppLanguage.SPANISH -> "Termina"
                                AppLanguage.FRENCH -> "Expire"
                                AppLanguage.ENGLISH -> "Ends"
                            }
                            Text(
                                text = "$endsLabel: ${item.endDate.split(" ").firstOrNull() ?: item.endDate}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (item.isClaimed || item.hasSteamTelemetry) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        if (item.isClaimed) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    when (lang) {
                                        AppLanguage.SPANISH -> "ESTADO:"
                                        AppLanguage.FRENCH -> "STATUT :"
                                        AppLanguage.ENGLISH -> "STATUS:"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                PlayStatusBadge(status = item.playStatus, onStatusSelected = onPlayStatusChange)
                            }
                        } else Spacer(modifier = Modifier.width(1.dp))
                        if (item.hasSteamTelemetry) SteamTelemetryBadge(item = item)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HardwareReadinessPillsRow(hardware = item.hardwareProfile)
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onClaimClick,
                        interactionSource = claimInteractionSource,
                        modifier = Modifier.weight(1f).height(44.dp).graphicsLayer { scaleX = claimButtonScale; scaleY = claimButtonScale }.testTag("claim_button_${item.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(if (item.hasMultipleLaunchers) Icons.Default.SportsEsports else Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(16.dp))
                            Text(
                                text = if (item.hasMultipleLaunchers) {
                                    when (lang) {
                                        AppLanguage.SPANISH -> "Elegir Tienda ▾"
                                        AppLanguage.FRENCH -> "Choisir lanceur ▾"
                                        AppLanguage.ENGLISH -> "Choose Launcher ▾"
                                    }
                                } else {
                                    when (lang) {
                                        AppLanguage.SPANISH -> "Reclamar"
                                        AppLanguage.FRENCH -> "Réclamer"
                                        AppLanguage.ENGLISH -> "Claim"
                                    }
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    val claimedBorderColor = if (isAnimationsEnabled && !isLowEndOptimization) {
                        val anim by animateColorAsState(if (item.isClaimed) NeonGreen else MaterialTheme.colorScheme.outline, label = "claimed_border")
                        anim
                    } else if (item.isClaimed) NeonGreen else MaterialTheme.colorScheme.outline

                    val haveItInteraction = remember { MutableInteractionSource() }
                    val isHaveItPressed by haveItInteraction.collectIsPressedAsState()
                    val haveItScale = if (isAnimationsEnabled && !isLowEndOptimization) {
                        val anim by animateFloatAsState(if (isHaveItPressed) 0.93f else 1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "have_it_scale")
                        anim
                    } else if (isHaveItPressed) 0.93f else 1f

                    OutlinedButton(
                        onClick = onToggleClaimed,
                        interactionSource = haveItInteraction,
                        modifier = Modifier.height(44.dp).then(if (haveItScale != 1f) Modifier.graphicsLayer { scaleX = haveItScale; scaleY = haveItScale } else Modifier).testTag("claimed_toggle_${item.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (item.isClaimed) NeonGreen.copy(alpha = 0.15f) else Color.Transparent,
                            contentColor = if (item.isClaimed) NeonGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = BorderStroke(1.dp, claimedBorderColor)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                imageVector = if (item.isClaimed) Icons.Default.CheckCircle else Icons.Outlined.CheckCircleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp).then(if (claimedScale != 1f) Modifier.graphicsLayer { scaleX = claimedScale; scaleY = claimedScale } else Modifier),
                                tint = if (item.isClaimed) NeonGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (item.isClaimed) {
                                    when (lang) {
                                        AppLanguage.SPANISH -> "Reclamado"
                                        AppLanguage.FRENCH -> "Réclamé"
                                        AppLanguage.ENGLISH -> "Claimed"
                                    }
                                } else {
                                    when (lang) {
                                        AppLanguage.SPANISH -> "¿Lo tienes?"
                                        AppLanguage.FRENCH -> "Déjà acquis ?"
                                        AppLanguage.ENGLISH -> "Have It?"
                                    }
                                },
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
