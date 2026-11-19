package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.local.SteamGameEntity
import com.example.data.model.DlcFilterMode
import com.example.data.model.SteamDlcRecommendation
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.i18n.localizedLabel
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextPrimary
import com.example.util.LocalLowEndOptimization

@Composable
fun SteamDlcHubDialog(
    recommendations: List<SteamDlcRecommendation>,
    selectedGame: SteamGameEntity?,
    selectedGameDlcs: List<SteamDlcRecommendation>,
    isLoadingLibraryDlcs: Boolean,
    isLoadingGameDlcs: Boolean,
    scanStatus: String?,
    onClearSelectedGame: () -> Unit,
    onRefreshLibraryDlcs: () -> Unit,
    onOpenDlc: (SteamDlcRecommendation) -> Unit,
    onDismissDlc: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler {
        if (selectedGame != null) onClearSelectedGame() else onDismiss()
    }

    val s = LocalAppStrings.current
    val lang = s.language
    var activeFilter by remember { mutableStateOf(DlcFilterMode.ALL) }
    val sourceList = if (selectedGame != null) selectedGameDlcs else recommendations
    val isBusy = if (selectedGame != null) isLoadingGameDlcs else isLoadingLibraryDlcs

    val filteredList = remember(sourceList, activeFilter) {
        when (activeFilter) {
            DlcFilterMode.ALL -> sourceList
            DlcFilterMode.FREE -> sourceList.filter { it.isFree }
            DlcFilterMode.ON_SALE -> sourceList.filter { it.isOnSale || it.isFree }
            DlcFilterMode.TOP_RATED -> sourceList.filter { !it.isSoundtrackOrCosmetic }.sortedByDescending { it.recommendationsCount }
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .widthIn(max = 620.dp)
                .testTag("steam_dlc_hub_dialog"),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, DlcPurple.copy(alpha = 0.45f))
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(Brush.linearGradient(listOf(DlcPurple, NeonCyan))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Extension, contentDescription = null, tint = Color.White, modifier = Modifier.size(21.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (selectedGame != null) String.format(s.dialogs.dlcGameTitleFormat, selectedGame.name) else s.dialogs.dlcHubTitle,
                                fontSize = 17.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (selectedGame != null) "${selectedGame.playStatus.localizedLabel(s)} • ${String.format(s.home.cardPlaytimeFormat, selectedGame.playtimeHours)}"
                                else s.dialogs.dlcHubSubtitle,
                                fontSize = 12.sp, color = DlcPurple
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (selectedGame == null) {
                            IconButton(
                                onClick = onRefreshLibraryDlcs,
                                enabled = !isBusy,
                                modifier = Modifier.minimumInteractiveComponentSize().testTag("dlc_hub_refresh_btn")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = s.dialogs.dlcScanBtn, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("dlc_hub_close_btn")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = s.common.close, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (selectedGame != null && recommendations.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .border(1.dp, DlcPurple.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable { onClearSelectedGame() }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (lang) {
                                AppLanguage.SPANISH -> "Mostrando DLCs de ${selectedGame.name}"
                                AppLanguage.FRENCH -> "DLC de ${selectedGame.name}"
                                AppLanguage.ENGLISH -> "Showing DLCs for ${selectedGame.name}"
                            },
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            when (lang) {
                                AppLanguage.SPANISH -> "Ver toda la biblioteca (${recommendations.size}) ➔"
                                AppLanguage.FRENCH -> "Toute la bibliothèque (${recommendations.size}) ➔"
                                AppLanguage.ENGLISH -> "View All Library (${recommendations.size}) ➔"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DlcPurple
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DlcFilterMode.entries.forEach { mode ->
                        val isSelected = activeFilter == mode
                        val localizedModeLabel = when (lang) {
                            AppLanguage.SPANISH -> when (mode) {
                                DlcFilterMode.ALL -> "Todos"
                                DlcFilterMode.FREE -> "Gratis"
                                DlcFilterMode.ON_SALE -> "En Oferta"
                                DlcFilterMode.TOP_RATED -> "Mejor Valorados"
                            }
                            AppLanguage.FRENCH -> when (mode) {
                                DlcFilterMode.ALL -> "Tous"
                                DlcFilterMode.FREE -> "Gratuits"
                                DlcFilterMode.ON_SALE -> "En promo"
                                DlcFilterMode.TOP_RATED -> "Mieux notés"
                            }
                            AppLanguage.ENGLISH -> mode.label
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { activeFilter = mode },
                            label = { Text(localizedModeLabel, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DlcPurple.copy(alpha = 0.2f),
                                selectedLabelColor = DlcPurple
                            ),
                            modifier = Modifier.testTag("dlc_filter_${mode.name.lowercase()}")
                        )
                    }
                }

                if (isBusy) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DlcPurple.copy(alpha = 0.12f))
                            .border(1.dp, DlcPurple.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = DlcPurple, strokeWidth = 2.dp)
                        Text(
                            text = scanStatus ?: s.system.dlcScanningStore,
                            fontSize = 12.sp, color = DlcPurple, maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (filteredList.isEmpty() && !isBusy) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(Icons.Default.Extension, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(44.dp))
                            Text(
                                text = when {
                                    activeFilter != DlcFilterMode.ALL -> when (lang) {
                                        AppLanguage.SPANISH -> "Sin resultados en este filtro"
                                        AppLanguage.FRENCH -> "Aucun résultat pour ce filtre"
                                        AppLanguage.ENGLISH -> "No ${activeFilter.label} Found"
                                    }
                                    selectedGame != null -> when (lang) {
                                        AppLanguage.SPANISH -> "Sin DLCs en tienda"
                                        AppLanguage.FRENCH -> "Aucun DLC listé"
                                        AppLanguage.ENGLISH -> "No Store DLCs Listed"
                                    }
                                    else -> s.dialogs.dlcEmptyTitle
                                },
                                fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when {
                                    activeFilter != DlcFilterMode.ALL -> when (lang) {
                                        AppLanguage.SPANISH -> "Prueba volver a 'Todos' o escanea tu biblioteca de nuevo."
                                        AppLanguage.FRENCH -> "Essayez de revenir à « Tous » ou analysez à nouveau votre bibliothèque."
                                        AppLanguage.ENGLISH -> "Try switching back to 'All Picks' or scanning your library again."
                                    }
                                    selectedGame != null -> when (lang) {
                                        AppLanguage.SPANISH -> "${selectedGame.name} no tiene expansiones DLC listadas en Steam."
                                        AppLanguage.FRENCH -> "${selectedGame.name} n'a aucune extension DLC listée sur Steam."
                                        AppLanguage.ENGLISH -> "${selectedGame.name} doesn't have listed expansion DLCs on the Steam Store."
                                    }
                                    else -> s.dialogs.dlcEmptySubtitle
                                },
                                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center
                            )
                            if (selectedGame == null) {
                                Button(
                                    onClick = onRefreshLibraryDlcs,
                                    colors = ButtonDefaults.buttonColors(containerColor = DlcPurple),
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(s.dialogs.dlcScanBtn, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f).testTag("dlc_hub_list"),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(filteredList, key = { "${it.parentAppId}_${it.dlcAppId}" }) { dlc ->
                            SteamDlcDetailCard(dlc = dlc, onOpenDlc = { onOpenDlc(dlc) }, onDismissDlc = { onDismissDlc(dlc.dlcAppId) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SteamDlcDetailCard(
    dlc: SteamDlcRecommendation,
    onOpenDlc: () -> Unit,
    onDismissDlc: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val lowEnd = LocalLowEndOptimization.current
    val borderColor = dlcCardBorderColor(dlc)
    val headerImageRequest = remember(context, dlc.headerImageUrl, lowEnd) {
        buildGiveawayImageRequest(
            context = context,
            url = dlc.headerImageUrl,
            isLowEndOptimization = lowEnd,
            width = if (lowEnd) 460 else 640,
            height = if (lowEnd) 190 else 265
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onOpenDlc() }.testTag("dlc_detail_card_${dlc.dlcAppId}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(460f / 190f).background(MaterialTheme.colorScheme.surfaceContainerHigh)) {
                AsyncImage(
                    model = headerImageRequest,
                    contentDescription = dlc.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color(0x33000000), MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.92f))))
                )

                DlcReasonBadge(dlc = dlc, modifier = Modifier.align(Alignment.TopStart).padding(10.dp))

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color(0xCC171A21))
                        .border(1.dp, Color(0xFF66C0F4).copy(alpha = 0.6f), RoundedCornerShape(7.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Default.SportsEsports, contentDescription = null, tint = Color(0xFF66C0F4), modifier = Modifier.size(12.dp))
                    Text(
                        text = "${dlc.parentGameName} • ${dlc.parentPlaytimeFormatted}",
                        fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF66C0F4),
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(dlc.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f).padding(end = 8.dp))
                    DlcPriceDisplay(dlc = dlc, stackVertical = true)
                }

                Text(dlc.recommendationReason, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DlcPurple)

                if (dlc.shortDescription.isNotBlank()) {
                    Text(dlc.shortDescription, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 16.sp)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (dlc.formattedRecommendations.isNotBlank()) {
                        Row(
                            modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(NeonCyan.copy(alpha = 0.12f)).padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.ThumbUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(11.dp))
                            Text(dlc.formattedRecommendations, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    if (dlc.metacriticScore != null) {
                        Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(NeonGreen.copy(alpha = 0.16f)).padding(horizontal = 7.dp, vertical = 3.dp)) {
                            Text("MC ${dlc.metacriticScore}", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = NeonGreen)
                        }
                    }
                    if (dlc.releaseDate.isNotBlank()) {
                        Text(dlc.releaseDate, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onDismissDlc,
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("dlc_dismiss_btn_${dlc.dlcAppId}"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Text(s.dialogs.dlcDismiss, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onOpenDlc,
                        modifier = Modifier.weight(1f).minimumInteractiveComponentSize().testTag("dlc_open_store_btn_${dlc.dlcAppId}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (dlc.isFree) NeonGreen else DlcPurple,
                            contentColor = if (dlc.isFree) Color(0xFF0A0F14) else Color.White
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (dlc.isFromFreeGiveaway) s.home.badgeClaimFree else s.dialogs.dlcOpenStore, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
