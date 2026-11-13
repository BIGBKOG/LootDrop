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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.SteamGameEntity
import com.example.data.model.DlcFilterMode
import com.example.data.model.SteamDlcRecommendation
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

private val DlcPurple = Color(0xFFA855F7)
private val DlcGold = Color(0xFFF59E0B)

/**
 * Home Tab Section: Personalized DLC Recommendations for Owned Steam Games.
 */
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_dlc_recommendations_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(DlcPurple.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Extension,
                            contentDescription = null,
                            tint = DlcPurple,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Text(
                        text = "DLC FOR YOUR STEAM GAMES",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = if (recommendations.isNotEmpty()) {
                        "${recommendations.size} expansions & free drops matched to games you own"
                    } else {
                        "Personalized DLC picks & free add-ons for your Steam library"
                    },
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            if (hasSteamGames) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = DlcPurple,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E222D))
                                .clickable { onRefreshDlcs() }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                .testTag("home_dlc_scan_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Scan Steam DLCs",
                                    tint = DlcPurple,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Scan",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DlcPurple
                                )
                            }
                        }
                    }

                    if (recommendations.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DlcPurple.copy(alpha = 0.16f))
                                .border(1.dp, DlcPurple.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .clickable { onOpenAllDlcs() }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                                .testTag("home_dlc_see_all_btn")
                        ) {
                            Text(
                                text = "See All ➔",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD8B4FE)
                            )
                        }
                    }
                }
            }
        }

        when {
            !hasSteamGames && steamUserId.isBlank() -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_dlc_link_steam_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151822)),
                    border = BorderStroke(1.dp, DlcPurple.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(DlcPurple.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFD8B4FE),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Discover DLCs for Games You Own",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Link your Steam ID to automatically spot free DLC drops, expansion sales, and top-rated add-ons for your library.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                        Button(
                            onClick = onOpenSteamDialog,
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("home_dlc_connect_steam_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DlcPurple,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Link Steam Library",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            isScanning && recommendations.isEmpty() -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151822)),
                    border = BorderStroke(1.dp, DlcPurple.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = DlcPurple,
                            strokeWidth = 2.5.dp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Scanning Steam Store DLCs...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = scanStatus ?: "Finding expansions & deals for your owned games",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            recommendations.isEmpty() -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151822)),
                    border = BorderStroke(1.dp, Color(0xFF282D3D))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                            Text(
                                text = "Ready to Scan Your Steam Library",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = scanStatus ?: "Tap Scan to find expansions, sales & free DLCs for your games.",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Button(
                            onClick = onRefreshDlcs,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DlcPurple,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("home_dlc_empty_scan_btn")
                        ) {
                            Text("Scan Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            else -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(end = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_dlc_carousel")
                ) {
                    items(recommendations.take(10), key = { it.dlcAppId }) { dlc ->
                        HomeDlcCarouselCard(
                            dlc = dlc,
                            onClick = { onSelectDlc(dlc) },
                            onDismiss = { onDismissDlc(dlc.dlcAppId) }
                        )
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
    val borderColor = when {
        dlc.isFree -> NeonGreen.copy(alpha = 0.65f)
        dlc.isOnSale -> DlcGold.copy(alpha = 0.65f)
        else -> DlcPurple.copy(alpha = 0.5f)
    }

    Card(
        modifier = Modifier
            .width(278.dp)
            .clickable { onClick() }
            .testTag("home_dlc_card_${dlc.dlcAppId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161A25)),
        border = BorderStroke(1.2.dp, borderColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Artwork Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(460f / 215f)
                    .background(Color(0xFF0E1118))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(dlc.headerImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = dlc.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0x44000000), Color(0xDD0E1118)),
                                startY = 40f
                            )
                        )
                )

                // Top-left Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xE60B0F19))
                        .border(
                            1.dp,
                            when {
                                dlc.isFree -> NeonGreen
                                dlc.isOnSale -> DlcGold
                                else -> DlcPurple
                            },
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = dlc.reasonBadge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = when {
                            dlc.isFree -> NeonGreen
                            dlc.isOnSale -> DlcGold
                            else -> Color(0xFFD8B4FE)
                        }
                    )
                }

                // Top-right Dismiss Button
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
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss ${dlc.name}",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(13.dp)
                    )
                }

                // Bottom-left Parent Game Tag
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
                        text = "For: ${dlc.parentGameName} • ${dlc.parentPlaytimeFormatted}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF66C0F4),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = dlc.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = dlc.recommendationReason,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Price / Discount Display
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        if (dlc.initialPriceFormatted.isNotBlank() && dlc.initialPriceFormatted != dlc.priceFormatted) {
                            Text(
                                text = dlc.initialPriceFormatted,
                                fontSize = 11.sp,
                                color = TextMuted,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (dlc.isFree) NeonGreen.copy(alpha = 0.16f)
                                    else if (dlc.isOnSale) DlcGold.copy(alpha = 0.18f)
                                    else Color(0xFF232838)
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (dlc.isFree) "FREE" else dlc.priceFormatted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (dlc.isFree) NeonGreen else if (dlc.isOnSale) DlcGold else TextPrimary
                            )
                        }
                    }

                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(34.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (dlc.isFree) NeonGreen else DlcPurple,
                            contentColor = if (dlc.isFree) Color(0xFF0A0F14) else Color.White
                        )
                    ) {
                        Text(
                            text = if (dlc.isFromFreeGiveaway) "Claim Free" else "View DLC",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Full-featured Steam Library DLC Hub Dialog.
 * Supports viewing all library DLC recommendations or inspecting DLCs for a single Steam game.
 */
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
        if (selectedGame != null) {
            onClearSelectedGame()
        } else {
            onDismiss()
        }
    }

    var activeFilter by remember { mutableStateOf(DlcFilterMode.ALL) }
    val sourceList = if (selectedGame != null) selectedGameDlcs else recommendations
    val isBusy = if (selectedGame != null) isLoadingGameDlcs else isLoadingLibraryDlcs

    val filteredList = remember(sourceList, activeFilter) {
        when (activeFilter) {
            DlcFilterMode.ALL -> sourceList
            DlcFilterMode.FREE -> sourceList.filter { it.isFree }
            DlcFilterMode.ON_SALE -> sourceList.filter { it.isOnSale || it.isFree }
            DlcFilterMode.TOP_RATED -> sourceList.filter { !it.isSoundtrackOrCosmetic }
                .sortedByDescending { it.recommendationsCount }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .widthIn(max = 620.dp)
                .testTag("steam_dlc_hub_dialog"),
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF0E131F),
            border = BorderStroke(1.2.dp, DlcPurple.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Row
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
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(DlcPurple, NeonCyan)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Extension,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (selectedGame != null) {
                                    "DLCs for ${selectedGame.name}"
                                } else {
                                    "Steam DLC Recommender"
                                },
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (selectedGame != null) {
                                    "${selectedGame.playStatus.label} • ${selectedGame.playtimeHours} played"
                                } else {
                                    "Matched to games you already own in Steam"
                                },
                                fontSize = 12.sp,
                                color = Color(0xFFD8B4FE)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (selectedGame == null) {
                            IconButton(
                                onClick = onRefreshLibraryDlcs,
                                enabled = !isBusy,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("dlc_hub_refresh_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Scan Steam DLCs",
                                    tint = NeonCyan
                                )
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("dlc_hub_close_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close DLC Recommender",
                                tint = TextSecondary
                            )
                        }
                    }
                }

                // If viewing a specific game, offer one-tap toggle back to full library DLC picks
                if (selectedGame != null && recommendations.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF181F30))
                            .border(1.dp, DlcPurple.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .clickable { onClearSelectedGame() }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Showing DLCs for ${selectedGame.name}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "View All Library (${recommendations.size}) ➔",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD8B4FE)
                        )
                    }
                }

                // Filter Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DlcFilterMode.values().forEach { mode ->
                        val isSelected = activeFilter == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = { activeFilter = mode },
                            label = {
                                Text(
                                    text = mode.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DlcPurple.copy(alpha = 0.25f),
                                selectedLabelColor = Color(0xFFE9D5FF)
                            ),
                            modifier = Modifier.testTag("dlc_filter_${mode.name.lowercase()}")
                        )
                    }
                }

                // Scanning Progress Banner
                if (isBusy) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DlcPurple.copy(alpha = 0.14f))
                            .border(1.dp, DlcPurple.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = DlcPurple,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = scanStatus ?: "Fetching DLC metadata from Steam Store...",
                            fontSize = 12.sp,
                            color = Color(0xFFE9D5FF),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // DLC Recommendations List
                if (filteredList.isEmpty() && !isBusy) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Extension,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = if (activeFilter != DlcFilterMode.ALL) {
                                    "No ${activeFilter.label} Found"
                                } else if (selectedGame != null) {
                                    "No Store DLCs Listed"
                                } else {
                                    "No DLC Picks Cached Yet"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (activeFilter != DlcFilterMode.ALL) {
                                    "Try switching back to 'All Picks' or scanning your library again."
                                } else if (selectedGame != null) {
                                    "${selectedGame.name} doesn't have listed expansion DLCs on the Steam Store."
                                } else {
                                    "Tap Scan Library to check your owned Steam games for expansions, sales, and free add-ons."
                                },
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            if (selectedGame == null) {
                                Button(
                                    onClick = onRefreshLibraryDlcs,
                                    colors = ButtonDefaults.buttonColors(containerColor = DlcPurple),
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Scan Library Now", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("dlc_hub_list"),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(filteredList, key = { "${it.parentAppId}_${it.dlcAppId}" }) { dlc ->
                            SteamDlcDetailCard(
                                dlc = dlc,
                                onOpenDlc = { onOpenDlc(dlc) },
                                onDismissDlc = { onDismissDlc(dlc.dlcAppId) }
                            )
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
    val borderColor = when {
        dlc.isFree -> NeonGreen.copy(alpha = 0.65f)
        dlc.isOnSale -> DlcGold.copy(alpha = 0.65f)
        else -> Color(0xFF283044)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDlc() }
            .testTag("dlc_detail_card_${dlc.dlcAppId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151B2B)),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(460f / 190f)
                    .background(Color(0xFF0E1118))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(dlc.headerImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = dlc.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0x33000000), Color(0xDF0E1118))
                            )
                        )
                )

                // Top-left Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color(0xE60B0F19))
                        .border(
                            1.dp,
                            when {
                                dlc.isFree -> NeonGreen
                                dlc.isOnSale -> DlcGold
                                else -> DlcPurple
                            },
                            RoundedCornerShape(7.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = dlc.reasonBadge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = when {
                            dlc.isFree -> NeonGreen
                            dlc.isOnSale -> DlcGold
                            else -> Color(0xFFD8B4FE)
                        }
                    )
                }

                // Bottom-left Game Linkage Pill
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
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = Color(0xFF66C0F4),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${dlc.parentGameName} • ${dlc.parentPlaytimeFormatted}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF66C0F4),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = dlc.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        if (dlc.initialPriceFormatted.isNotBlank() && dlc.initialPriceFormatted != dlc.priceFormatted) {
                            Text(
                                text = dlc.initialPriceFormatted,
                                fontSize = 11.sp,
                                color = TextMuted,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (dlc.isFree) NeonGreen.copy(alpha = 0.16f)
                                    else if (dlc.isOnSale) DlcGold.copy(alpha = 0.18f)
                                    else Color(0xFF232838)
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (dlc.isFree) "FREE" else dlc.priceFormatted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (dlc.isFree) NeonGreen else if (dlc.isOnSale) DlcGold else TextPrimary
                            )
                        }
                    }
                }

                Text(
                    text = dlc.recommendationReason,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFD8B4FE)
                )

                if (dlc.shortDescription.isNotBlank()) {
                    Text(
                        text = dlc.shortDescription,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }

                // Meta info chips (Reviews, Release Date, Metacritic)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (dlc.formattedRecommendations.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonCyan.copy(alpha = 0.1f))
                                .padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = dlc.formattedRecommendations,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        }
                    }
                    if (dlc.metacriticScore != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonGreen.copy(alpha = 0.16f))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "MC ${dlc.metacriticScore}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeonGreen
                            )
                        }
                    }
                    if (dlc.releaseDate.isNotBlank()) {
                        Text(
                            text = dlc.releaseDate,
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                // Actions Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismissDlc,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("dlc_dismiss_btn_${dlc.dlcAppId}"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF2E3548)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("Hide", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onOpenDlc,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("dlc_open_store_btn_${dlc.dlcAppId}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (dlc.isFree) NeonGreen else DlcPurple,
                            contentColor = if (dlc.isFree) Color(0xFF0A0F14) else Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (dlc.isFromFreeGiveaway) "Claim Free DLC" else "Open Steam Store",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
