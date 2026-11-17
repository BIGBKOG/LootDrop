package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.data.model.SteamDlcRecommendation
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.util.LocalLowEndOptimization

internal val DlcPurple = Color(0xFFA855F7)
internal val DlcGold = Color(0xFFF59E0B)

internal fun dlcCardBorderColor(dlc: SteamDlcRecommendation): Color = when {
    dlc.isFree -> NeonGreen.copy(alpha = 0.65f)
    dlc.isOnSale -> DlcGold.copy(alpha = 0.65f)
    else -> Color(0x66475569)
}

@Composable
internal fun DlcReasonBadge(dlc: SteamDlcRecommendation, modifier: Modifier = Modifier) {
    val borderColor = when {
        dlc.isFree -> NeonGreen
        dlc.isOnSale -> DlcGold
        else -> DlcPurple
    }
    val textColor = when {
        dlc.isFree -> NeonGreen
        dlc.isOnSale -> DlcGold
        else -> Color(0xFFD8B4FE)
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xE60B0F19))
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(dlc.reasonBadge, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
    }
}

@Composable
internal fun DlcPriceDisplay(dlc: SteamDlcRecommendation, stackVertical: Boolean) {
    val isEs = LocalAppStrings.current.language == AppLanguage.SPANISH
    val content: @Composable () -> Unit = {
        if (dlc.initialPriceFormatted.isNotBlank() && dlc.initialPriceFormatted != dlc.priceFormatted) {
            Text(
                text = dlc.initialPriceFormatted,
                fontSize = 11.sp,
                color = if (stackVertical) TextMuted else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textDecoration = TextDecoration.LineThrough
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(
                    when {
                        dlc.isFree -> NeonGreen.copy(alpha = 0.16f)
                        dlc.isOnSale -> DlcGold.copy(alpha = 0.18f)
                        else -> MaterialTheme.colorScheme.surfaceContainerHigh
                    }
                )
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = if (dlc.isFree) (if (isEs) "GRATIS" else "FREE") else dlc.priceFormatted,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = when {
                    dlc.isFree -> NeonGreen
                    dlc.isOnSale -> DlcGold
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
    if (stackVertical) {
        Column(horizontalAlignment = Alignment.End) { content() }
    } else {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) { content() }
    }
}

@Composable
fun HomeDlcRecommendationsSection(
    recommendations: List<SteamDlcRecommendation>,
    steamUserId: String,
    hasSteamGames: Boolean,
    isScanning: Boolean,
    scanStatus: String?,
    onOpenSteamDialog: () -> Unit,
    onRefreshDlcs: () -> Unit,
    onOpenAllDlcs: () -> Unit,
    onSelectDlc: (SteamDlcRecommendation) -> Unit,
    onDismissDlc: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    Column(
        modifier = modifier.fillMaxWidth().testTag("home_dlc_recommendations_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier.size(22.dp).clip(RoundedCornerShape(7.dp)).background(DlcPurple.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Extension, contentDescription = null, tint = DlcPurple, modifier = Modifier.size(13.dp))
                    }
                    Text(
                        text = if (isEs) "DLCS PARA TUS JUEGOS DE STEAM" else "DLC FOR YOUR STEAM GAMES",
                        style = MaterialTheme.typography.titleSmall, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground, letterSpacing = 0.6.sp
                    )
                }
                Text(
                    text = if (isEs) {
                        if (recommendations.isNotEmpty()) "${recommendations.size} expansiones y drops gratis para tus juegos"
                        else "Selección personalizada de DLCs y extras gratis para tu Steam"
                    } else {
                        if (recommendations.isNotEmpty()) "${recommendations.size} expansions & free drops matched to games you own"
                        else "Personalized DLC picks & free add-ons for your Steam library"
                    },
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (hasSteamGames) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isScanning) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = DlcPurple, strokeWidth = 2.dp)
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                                .clickable { onRefreshDlcs() }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                                .testTag("home_dlc_scan_btn")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Refresh, contentDescription = s.dialogs.dlcScanBtn, tint = DlcPurple, modifier = Modifier.size(13.dp))
                                Text(if (isEs) "Escanear" else "Scan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DlcPurple)
                            }
                        }
                    }

                    if (recommendations.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(DlcPurple.copy(alpha = 0.14f))
                                .border(1.dp, DlcPurple.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                                .clickable { onOpenAllDlcs() }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                                .testTag("home_dlc_see_all_btn")
                        ) {
                            Text("${s.home.homeBestFreeSeeAll} ➔", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DlcPurple)
                        }
                    }
                }
            }
        }

        when {
            !hasSteamGames && steamUserId.isBlank() -> {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("home_dlc_link_steam_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, DlcPurple.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier.size(38.dp).clip(CircleShape).background(DlcPurple.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = DlcPurple, modifier = Modifier.size(20.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(if (isEs) "Descubre DLCs para tus Juegos" else "Discover DLCs for Games You Own", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    text = if (isEs) "Vincula tu Steam ID para detectar al instante DLCs gratis, ofertas de expansiones y mejores extras." else "Link your Steam ID to automatically spot free DLC drops, expansion sales, and top-rated add-ons for your library.",
                                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp
                                )
                            }
                        }
                        Button(
                            onClick = onOpenSteamDialog,
                            modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().testTag("home_dlc_connect_steam_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DlcPurple, contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.SportsEsports, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(s.home.vaultSyncSteamBtn, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            isScanning && recommendations.isEmpty() -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, DlcPurple.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = DlcPurple, strokeWidth = 2.5.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(s.system.dlcScanningStore, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                text = scanStatus ?: (if (isEs) "Buscando expansiones y ofertas para tus juegos" else "Finding expansions & deals for your owned games"),
                                fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            recommendations.isEmpty() -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                            Text(if (isEs) "Listo para Escanear Steam" else "Ready to Scan Your Steam Library", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(scanStatus ?: (if (isEs) "Toca Escanear para buscar expansiones y DLCs gratis." else "Tap Scan to find expansions, sales & free DLCs for your games."), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = onRefreshDlcs,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DlcPurple, contentColor = Color.White),
                            modifier = Modifier.testTag("home_dlc_empty_scan_btn")
                        ) {
                            Text(if (isEs) "Escanear" else "Scan Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            else -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(end = 4.dp),
                    modifier = Modifier.fillMaxWidth().testTag("home_dlc_carousel")
                ) {
                    items(recommendations.take(10), key = { it.dlcAppId }) { dlc ->
                        HomeDlcCarouselCard(dlc = dlc, onClick = { onSelectDlc(dlc) }, onDismiss = { onDismissDlc(dlc.dlcAppId) })
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeDlcCarouselCard(
    dlc: SteamDlcRecommendation,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    val lowEnd = LocalLowEndOptimization.current
    val headerImageRequest = remember(context, dlc.headerImageUrl, lowEnd) {
        buildGiveawayImageRequest(
            context = context,
            url = dlc.headerImageUrl,
            isLowEndOptimization = lowEnd,
            width = 460,
            height = 215
        )
    }
    Card(
        modifier = Modifier.width(278.dp).clickable { onClick() }.testTag("home_dlc_card_${dlc.dlcAppId}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, dlcCardBorderColor(dlc))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(460f / 215f).background(MaterialTheme.colorScheme.surfaceContainerLow)) {
                AsyncImage(
                    model = headerImageRequest,
                    contentDescription = dlc.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color(0x33000000), MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)), startY = 40f)
                    )
                )

                DlcReasonBadge(dlc = dlc, modifier = Modifier.align(Alignment.TopStart).padding(8.dp))

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color(0xBB000000))
                        .clickable { onDismiss() }
                        .testTag("dismiss_home_dlc_${dlc.dlcAppId}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = "${s.common.dismiss} ${dlc.name}", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(13.dp))
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xCC171A21))
                        .border(1.dp, Color(0xFF66C0F4).copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${if (isEs) "Para" else "For"}: ${dlc.parentGameName} • ${dlc.parentPlaytimeFormatted}",
                        fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF66C0F4),
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(dlc.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(dlc.recommendationReason, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 15.sp)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DlcPriceDisplay(dlc = dlc, stackVertical = false)
                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(34.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (dlc.isFree) NeonGreen else DlcPurple,
                            contentColor = if (dlc.isFree) Color(0xFF0A0F14) else Color.White
                        )
                    ) {
                        Text(if (dlc.isFromFreeGiveaway) (if (isEs) "Reclamar Gratis" else "Claim Free") else (if (isEs) "Ver DLC" else "View DLC"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
