package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.i18n.localizedLabel
import com.example.ui.GiveawaysViewModel
import com.example.ui.theme.GamingCardBorder
import com.example.ui.theme.GamingDarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.LocalLowEndOptimization

@Composable
internal fun GiveawayDetailHeroBanner(
    item: GiveawayItem,
    context: Context,
    coverHeight: Dp,
    launcherColor: Color,
    themePrimary: Color,
    onToggleWishlist: () -> Unit,
    onToggleBookmark: () -> Unit,
    onDismiss: () -> Unit
) {
    val s = LocalAppStrings.current
    val lowEnd = LocalLowEndOptimization.current
    Box(modifier = Modifier.fillMaxWidth().height(coverHeight)) {
        val detailImageRequest = remember(context, item.id, item.image, item.thumbnail, lowEnd) {
            buildGiveawayImageRequest(
                context = context,
                url = item.image.ifBlank { item.thumbnail },
                isLowEndOptimization = lowEnd,
                width = if (lowEnd) 720 else 960,
                height = if (lowEnd) 405 else 540
            )
        }
        AsyncImage(model = detailImageRequest, contentDescription = item.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color(0x800B0F19), Color.Transparent, GamingDarkSurface))
            )
        )

        Row(
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val bookmarkScale = if (lowEnd) {
                1f
            } else {
                animateFloatAsState(
                    targetValue = if (item.isBookmarked) 1.22f else 1.0f,
                    animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
                    label = "detail_bookmark_scale"
                ).value
            }
            IconButton(
                onClick = onToggleWishlist,
                modifier = Modifier
                    .size(36.dp).clip(CircleShape)
                    .background(if (item.isWishlisted) Color(0xFFFF2D87).copy(alpha = 0.25f) else Color(0xCC0B0F19))
                    .border(1.2.dp, if (item.isWishlisted) Color(0xFFFF2D87) else Color.White.copy(alpha = 0.25f), CircleShape)
                    .testTag("detail_wishlist_button")
            ) {
                Icon(
                    imageVector = if (item.isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (item.isWishlisted) s.home.badgeWishlisted else s.dialogs.detailWishlistBadge,
                    tint = if (item.isWishlisted) Color(0xFFFF2D87) else TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(
                onClick = onToggleBookmark,
                modifier = Modifier
                    .size(36.dp).clip(CircleShape)
                    .background(if (item.isBookmarked) themePrimary.copy(alpha = 0.25f) else Color(0xCC0B0F19))
                    .border(1.2.dp, if (item.isBookmarked) themePrimary else Color.White.copy(alpha = 0.25f), CircleShape)
                    .testTag("detail_bookmark_button")
            ) {
                Icon(
                    imageVector = if (item.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = if (item.isBookmarked) s.dialogs.detailBookmarkAdded else s.dialogs.detailBookmarkAdd,
                    tint = if (item.isBookmarked) themePrimary else TextPrimary,
                    modifier = Modifier.size(18.dp).then(
                        if (!lowEnd) Modifier.graphicsLayer { scaleX = bookmarkScale; scaleY = bookmarkScale } else Modifier
                    )
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xCC0B0F19)).testTag("detail_close_button")
            ) {
                Icon(Icons.Default.Close, contentDescription = s.common.close, tint = TextPrimary)
            }
        }

        val isDarkBrand = item.launcherCategory in setOf(LauncherType.PLAYSTATION, LauncherType.XBOX, LauncherType.EPIC, LauncherType.IOS)
        Row(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(launcherColor).padding(horizontal = 10.dp, vertical = 6.dp)) {
                Text(item.launcherCategory.localizedDisplayName(s), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDarkBrand) Color.White else Color(0xFF0B0F19))
            }
            ExtraPlatformPills(item = item, horizontalPad = 10.dp, verticalPad = 6.dp, fontSizeSp = 12, withBorder = false)
        }
    }
}

@Composable
internal fun DetailOwnershipBanner(ownershipStatus: GiveawaysViewModel.OwnershipStatus) {
    val s = LocalAppStrings.current
    val lang = s.language
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF10B981).copy(alpha = 0.22f), Color(0xFF06B6D4).copy(alpha = 0.18f))))
            .border(1.5.dp, Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF06B6D4))), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp)
            .testTag("ownership_pill_banner")
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFF10B981).copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
                Text(if (ownershipStatus.inSteam) "🎮" else "💎", fontSize = 17.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    when (lang) {
                        AppLanguage.SPANISH -> "YA TIENES ESTE JUEGO"
                        AppLanguage.FRENCH -> "VOUS POSSÉDEZ DÉJÀ CE JEU"
                        AppLanguage.ENGLISH -> "YOU ALREADY HAVE THIS"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF10B981),
                    letterSpacing = 0.6.sp
                )
                val sourceText = when (lang) {
                    AppLanguage.SPANISH -> when {
                        ownershipStatus.inVault && ownershipStatus.inSteam -> "¡Guardado en tu Bóveda de LootDrop y en tu biblioteca de Steam!"
                        ownershipStatus.inVault -> "Ya está guardado en tu Bóveda de LootDrop."
                        ownershipStatus.inSteam && ownershipStatus.matchedSteamGameName != null -> "Encontrado en tu Steam como '${ownershipStatus.matchedSteamGameName}'."
                        else -> "Encontrado en tu biblioteca vinculada de Steam."
                    }
                    AppLanguage.FRENCH -> when {
                        ownershipStatus.inVault && ownershipStatus.inSteam -> "Enregistré dans votre coffre LootDrop et trouvé dans votre bibliothèque Steam !"
                        ownershipStatus.inVault -> "Déjà enregistré dans votre coffre LootDrop."
                        ownershipStatus.inSteam && ownershipStatus.matchedSteamGameName != null -> "Trouvé dans votre bibliothèque Steam sous « ${ownershipStatus.matchedSteamGameName} »."
                        else -> "Trouvé dans votre bibliothèque Steam liée."
                    }
                    AppLanguage.ENGLISH -> when {
                        ownershipStatus.inVault && ownershipStatus.inSteam -> "Saved in your LootDrop Vault and found in your Steam Library!"
                        ownershipStatus.inVault -> "Already saved in your LootDrop Vault."
                        ownershipStatus.inSteam && ownershipStatus.matchedSteamGameName != null -> "Found in your linked Steam library as '${ownershipStatus.matchedSteamGameName}'."
                        else -> "Found in your linked Steam library."
                    }
                }
                Text(sourceText, fontSize = 11.sp, color = Color(0xFFD1FAE5), fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
internal fun DetailActionButtonsBlock(
    item: GiveawayItem,
    context: Context,
    themePrimary: Color,
    isSharingCard: Boolean,
    onClaimClick: () -> Unit,
    onLauncherOptionClick: ((url: String, launcherName: String) -> Unit)?,
    onShareCardClick: () -> Unit,
    onPreviewCardClick: () -> Unit,
    onToggleClaimed: () -> Unit
) {
    val s = LocalAppStrings.current
    val lang = s.language
    val lowEnd = LocalLowEndOptimization.current
    val claimSource = remember { MutableInteractionSource() }
    val claimScale = if (lowEnd) {
        1f
    } else {
        val isClaimPressed by claimSource.collectIsPressedAsState()
        animateFloatAsState(if (isClaimPressed) 0.96f else 1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "detail_claim_scale").value
    }

    Button(
        onClick = onClaimClick,
        interactionSource = claimSource,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .then(if (!lowEnd) Modifier.graphicsLayer { scaleX = claimScale; scaleY = claimScale } else Modifier)
            .testTag("detail_claim_button"),
        colors = ButtonDefaults.buttonColors(containerColor = themePrimary, contentColor = MaterialTheme.colorScheme.background),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(20.dp))
            val claimDestination = when {
                item.hasMultipleLaunchers -> s.dialogs.chooseLauncherTitle
                item.isPlayStation && !item.isPc -> "PlayStation Store"
                item.isXbox && !item.isPc -> "Xbox Store"
                item.isAndroid && !item.isPc -> "Google Play Store"
                item.isIos && !item.isPc -> "Apple App Store"
                else -> item.launcherCategory.localizedDisplayName(s)
            }
            Text(
                text = if (item.hasMultipleLaunchers) {
                    when (lang) {
                        AppLanguage.SPANISH -> "Reclamar • Elegir Tienda ▾"
                        AppLanguage.FRENCH -> "Réclamer • Choisir la boutique ▾"
                        AppLanguage.ENGLISH -> "Claim • Choose Launcher ▾"
                    }
                } else {
                    when (lang) {
                        AppLanguage.SPANISH -> "Reclamar en $claimDestination"
                        AppLanguage.FRENCH -> "Réclamer sur $claimDestination"
                        AppLanguage.ENGLISH -> "Claim on $claimDestination"
                    }
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (item.hasMultipleLaunchers) {
        Spacer(modifier = Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(NeonGreen))
            Text(
                text = when (lang) {
                    AppLanguage.SPANISH -> "TIENDAS DISPONIBLES (100% GRATIS)"
                    AppLanguage.FRENCH -> "LANCEURS DISPONIBLES (100 % GRATUIT)"
                    AppLanguage.ENGLISH -> "AVAILABLE LAUNCHERS (100% OFF)"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NeonGreen,
                letterSpacing = 1.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item.getAvailableLaunchers(lang).forEach { launcherOption ->
                DetailLauncherCardRow(launcherOption = launcherOption) {
                    if (onLauncherOptionClick != null) onLauncherOptionClick(launcherOption.url, launcherOption.name)
                    else onClaimClick()
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    val copyInteraction = remember { MutableInteractionSource() }
    val copyScale = if (lowEnd) {
        1f
    } else {
        val isCopyPressed by copyInteraction.collectIsPressedAsState()
        animateFloatAsState(if (isCopyPressed) 0.94f else 1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "copy_scale").value
    }

    val shareInteraction = remember { MutableInteractionSource() }
    val shareScale = if (lowEnd) {
        1f
    } else {
        val isSharePressed by shareInteraction.collectIsPressedAsState()
        animateFloatAsState(if (isSharePressed) 0.94f else 1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "share_scale").value
    }

    val libraryInteraction = remember { MutableInteractionSource() }
    val libraryScale = if (lowEnd) {
        1f
    } else {
        val isLibraryPressed by libraryInteraction.collectIsPressedAsState()
        animateFloatAsState(if (isLibraryPressed) 0.96f else 1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "library_scale").value
    }
    val libraryCheckScale = if (lowEnd) {
        1f
    } else {
        animateFloatAsState(if (item.isClaimed) 1.25f else 1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow), label = "library_check_scale").value
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Giveaway Link", item.openGiveawayUrl))
                Toast.makeText(context, s.common.copiedLink, Toast.LENGTH_SHORT).show()
            },
            interactionSource = copyInteraction,
            modifier = Modifier.weight(1f).then(if (!lowEnd) Modifier.graphicsLayer { scaleX = copyScale; scaleY = copyScale } else Modifier),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
            border = BorderStroke(1.dp, themePrimary.copy(alpha = 0.4f))
        ) {
            Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(s.common.copyLink, fontSize = 12.sp)
        }

        Button(
            onClick = onShareCardClick,
            interactionSource = shareInteraction,
            modifier = Modifier
                .weight(1.2f)
                .then(if (!lowEnd) Modifier.graphicsLayer { scaleX = shareScale; scaleY = shareScale } else Modifier)
                .testTag("dialog_share_card_button"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = themePrimary, contentColor = MaterialTheme.colorScheme.onPrimary),
            enabled = !isSharingCard
        ) {
            if (isSharingCard) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    when (lang) {
                        AppLanguage.SPANISH -> "Creando..."
                        AppLanguage.FRENCH -> "Création..."
                        AppLanguage.ENGLISH -> "Rendering..."
                    },
                    fontSize = 12.sp
                )
            } else {
                Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(s.dialogs.detailShareCardCta, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        OutlinedButton(
            onClick = onPreviewCardClick,
            modifier = Modifier.size(40.dp).testTag("dialog_preview_card_button"),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = themePrimary),
            border = BorderStroke(1.dp, themePrimary.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.Visibility, contentDescription = s.dialogs.sharePreviewTitle, modifier = Modifier.size(18.dp))
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedButton(
        onClick = onToggleClaimed,
        interactionSource = libraryInteraction,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!lowEnd) Modifier.graphicsLayer { scaleX = libraryScale; scaleY = libraryScale } else Modifier)
            .testTag("dialog_toggle_library_button"),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (item.isClaimed) NeonGreen.copy(alpha = 0.15f) else Color.Transparent,
            contentColor = if (item.isClaimed) NeonGreen else TextSecondary
        ),
        border = BorderStroke(1.dp, if (item.isClaimed) NeonGreen else GamingCardBorder)
    ) {
        Icon(
            imageVector = if (item.isClaimed) Icons.Default.CheckCircle else Icons.Outlined.CheckCircleOutline,
            contentDescription = null,
            modifier = Modifier
                .size(16.dp)
                .then(if (!lowEnd) Modifier.graphicsLayer { scaleX = libraryCheckScale; scaleY = libraryCheckScale } else Modifier)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (item.isClaimed) {
                s.dialogs.detailClaimed
            } else {
                when (lang) {
                    AppLanguage.SPANISH -> "Añadir a 'Mi Bóveda'"
                    AppLanguage.FRENCH -> "Ajouter à « Ma ludothèque »"
                    AppLanguage.ENGLISH -> "Add to 'In Library'"
                }
            },
            fontSize = 13.sp
        )
    }
}

@Composable
internal fun DetailGameTrackerCard(
    item: GiveawayItem,
    onPlayStatusChange: ((PlayStatus) -> Unit)?
) {
    val s = LocalAppStrings.current
    val lang = s.language
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF161F30))
            .border(1.dp, Color(0xFF2A3D59), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(s.dialogs.detailPlayStatusTitle, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = NeonCyan, letterSpacing = 0.8.sp)
                Text(item.playStatus.localizedLabel(s), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(item.playStatus.badgeColorHex))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PlayStatus.entries.forEach { status ->
                    val isCurrent = item.playStatus == status
                    val statusColor = Color(status.badgeColorHex)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCurrent) statusColor.copy(alpha = 0.2f) else Color(0xFF0F172A))
                            .border(if (isCurrent) 1.5.dp else 1.dp, if (isCurrent) statusColor else Color(0xFF334155), RoundedCornerShape(8.dp))
                            .clickable { onPlayStatusChange?.invoke(status) }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (status) {
                                PlayStatus.NOT_STARTED -> when (lang) {
                                    AppLanguage.SPANISH -> "⏳ Pendiente"
                                    AppLanguage.FRENCH -> "⏳ À faire"
                                    AppLanguage.ENGLISH -> "⏳ Backlog"
                                }
                                PlayStatus.PLAYING -> when (lang) {
                                    AppLanguage.SPANISH -> "🎮 Jugando"
                                    AppLanguage.FRENCH -> "🎮 En cours"
                                    AppLanguage.ENGLISH -> "🎮 Playing"
                                }
                                PlayStatus.BEATEN -> when (lang) {
                                    AppLanguage.SPANISH -> "🏆 Terminado"
                                    AppLanguage.FRENCH -> "🏆 Terminé"
                                    AppLanguage.ENGLISH -> "🏆 Finished"
                                }
                                PlayStatus.DROPPED -> when (lang) {
                                    AppLanguage.SPANISH -> "⏸️ Dejado"
                                    AppLanguage.FRENCH -> "⏸️ Abandonné"
                                    AppLanguage.ENGLISH -> "⏸️ Dropped"
                                }
                            },
                            fontSize = 10.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCurrent) statusColor else TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun DetailSteamTelemetryCard(item: GiveawayItem, context: Context) {
    val s = LocalAppStrings.current
    val lang = s.language
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF171A21))
            .border(1.dp, Color(0xFF2A475E), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.SportsEsports, contentDescription = "Steam", tint = Color(0xFF66C0F4), modifier = Modifier.size(16.dp))
                    Text(
                        when (lang) {
                            AppLanguage.SPANISH -> "Actividad y Progreso en Steam"
                            AppLanguage.FRENCH -> "Activité et progression Steam"
                            AppLanguage.ENGLISH -> "Steam Activity & Progress"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                if (item.steamAppId != null) {
                    Text(
                        text = "App #${item.steamAppId}",
                        fontSize = 10.sp,
                        color = Color(0xFF66C0F4),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1B2838))
                            .clickable {
                                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://store.steampowered.com/app/${item.steamAppId}"))) }
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        when (lang) {
                            AppLanguage.SPANISH -> "Tiempo Jugado"
                            AppLanguage.FRENCH -> "Temps de jeu"
                            AppLanguage.ENGLISH -> "Playtime Logged"
                        },
                        fontSize = 10.sp,
                        color = Color(0xFF8F98A0)
                    )
                    Text("${item.steamPlaytimeHours} (${item.steamPlaytimeMinutes} min)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                if (item.steamAchievementsTotal > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            when (lang) {
                                AppLanguage.SPANISH -> "Logros"
                                AppLanguage.FRENCH -> "Succès"
                                AppLanguage.ENGLISH -> "Achievements"
                            },
                            fontSize = 10.sp,
                            color = Color(0xFF8F98A0)
                        )
                        Text(
                            text = "${item.steamAchievementsUnlocked} / ${item.steamAchievementsTotal} (${item.steamAchievementsPercentage}%)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.steamAchievementsPercentage == 100) NeonGreen else Color.White
                        )
                    }
                }
            }
            if (item.steamAchievementsTotal > 0) {
                LinearProgressIndicator(
                    progress = { (item.steamAchievementsUnlocked.toFloat() / item.steamAchievementsTotal.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = if (item.steamAchievementsPercentage == 100) NeonGreen else Color(0xFF66C0F4),
                    trackColor = Color(0xFF1B2838)
                )
            }
        }
    }
}
