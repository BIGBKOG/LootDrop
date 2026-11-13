package com.example.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.SteamGameEntity
import com.example.data.model.GiveawayItem
import com.example.data.model.SteamDlcRecommendation
import com.example.ui.UiStats
import com.example.ui.components.HomeDlcRecommendationsSection
import com.example.ui.density.ScreenDensityInfo
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTabContent(
    recentSteamGames: List<SteamGameEntity>,
    top3FreeGames: List<GiveawayItem>,
    uiStats: UiStats,
    steamUserId: String,
    isLoading: Boolean,
    isSyncingSteam: Boolean,
    isAnimationsEnabled: Boolean,
    isLowEndOptimization: Boolean,
    screenDensityInfo: ScreenDensityInfo,
    listState: LazyListState,
    onRefresh: () -> Unit,
    onItemClick: (GiveawayItem) -> Unit,
    onClaimClick: (GiveawayItem) -> Unit,
    onOpenSteamDialog: () -> Unit,
    onQuickSteamSync: () -> Unit,
    onOpenAchievements: (SteamGameEntity) -> Unit,
    onExploreAllFreebies: () -> Unit,
    onViewVault: () -> Unit,
    publicReviews: List<com.example.data.model.PublicReview> = emptyList(),
    defaultAuthorName: String = "",
    onPostAppReview: (Int, String, String) -> Unit = { _, _, _ -> },
    onVoteHelpful: (String) -> Unit = {},
    hasVotedHelpful: (String) -> Boolean = { false },
    dlcRecommendations: List<SteamDlcRecommendation> = emptyList(),
    hasSteamGames: Boolean = false,
    isScanningDlcs: Boolean = false,
    dlcScanStatus: String? = null,
    onRefreshDlcs: () -> Unit = {},
    onOpenAllDlcs: () -> Unit = {},
    onSelectDlc: (SteamDlcRecommendation) -> Unit = {},
    onDismissDlc: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val pullRefreshState = rememberPullToRefreshState()
    val context = LocalContext.current

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = onRefresh,
        state = pullRefreshState,
        modifier = modifier
            .fillMaxSize()
            .testTag("home_tab_pull_refresh")
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_content_list"),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 10.dp,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Quick Action & Overview Stats Banner
            item(key = "home_quick_stats_strip") {
                HomeQuickStatsStrip(
                    uiStats = uiStats,
                    steamGameCount = recentSteamGames.size,
                    onExploreAllFreebies = onExploreAllFreebies,
                    onViewVault = onViewVault
                )
            }

            // Section 1: Best 3 Free Games Today (Swipeable Carousel)
            item(key = "home_top_free_carousel_section") {
                HomeBestFreeGamesSection(
                    topGames = top3FreeGames,
                    onItemClick = onItemClick,
                    onClaimClick = onClaimClick,
                    onExploreAll = onExploreAllFreebies,
                    isAnimationsEnabled = isAnimationsEnabled,
                    isLowEndOptimization = isLowEndOptimization
                )
            }

            // Section 2: 3 Most Recently Played Steam Games
            item(key = "home_recent_steam_section") {
                HomeRecentSteamGamesSection(
                    games = recentSteamGames,
                    steamUserId = steamUserId,
                    isSyncingSteam = isSyncingSteam,
                    onOpenSteamDialog = onOpenSteamDialog,
                    onQuickSteamSync = onQuickSteamSync,
                    onOpenAchievements = onOpenAchievements,
                    isAnimationsEnabled = isAnimationsEnabled
                )
            }

            // Section 2.5: Personalized DLC Recommendations for Owned Steam Games
            item(key = "home_dlc_recommendations_section") {
                HomeDlcRecommendationsSection(
                    recommendations = dlcRecommendations,
                    steamUserId = steamUserId,
                    hasSteamGames = hasSteamGames || recentSteamGames.isNotEmpty() || dlcRecommendations.isNotEmpty(),
                    isScanning = isScanningDlcs,
                    scanStatus = dlcScanStatus,
                    onOpenSteamDialog = onOpenSteamDialog,
                    onRefreshDlcs = onRefreshDlcs,
                    onOpenAllDlcs = onOpenAllDlcs,
                    onSelectDlc = onSelectDlc,
                    onDismissDlc = onDismissDlc
                )
            }

            // Section 3: LootDrop App Rating & Community Reviews
            item(key = "home_app_rating_reviews_section") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "LOOTDROP APP RATING & REVIEWS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700),
                            letterSpacing = 0.8.sp
                        )
                    }
                    com.example.ui.components.LootDropAppRatingCard(
                        reviews = publicReviews,
                        defaultAuthorName = defaultAuthorName,
                        onPostAppReview = onPostAppReview,
                        onVoteHelpful = onVoteHelpful,
                        hasVotedHelpful = hasVotedHelpful
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeQuickStatsStrip(
    uiStats: UiStats,
    steamGameCount: Int,
    onExploreAllFreebies: () -> Unit,
    onViewVault: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_quick_stats_strip"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF161922)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF282D3D))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Active Freebies
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onExploreAllFreebies() }
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    val availableCount = if (uiStats.activeGiveawaysCount > 0) uiStats.activeGiveawaysCount else uiStats.newFreebiesCount
                    Text(
                        text = "$availableCount Available",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Explore Free ➔",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NeonCyan
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(30.dp)
                    .background(Color(0xFF2B3042))
            )

            // Saved Worth / Vault
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onViewVault() }
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NeonGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    val savedFormatted = String.format(java.util.Locale.US, "$%.2f", uiStats.claimedTotalSavedUsd)
                    Text(
                        text = savedFormatted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen
                    )
                    Text(
                        text = "Claimed Vault ➔",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

/**
 * Best 3 Free Games Today - Swipeable Carousel
 */
@Composable
private fun HomeBestFreeGamesSection(
    topGames: List<GiveawayItem>,
    onItemClick: (GiveawayItem) -> Unit,
    onClaimClick: (GiveawayItem) -> Unit,
    onExploreAll: () -> Unit,
    isAnimationsEnabled: Boolean,
    isLowEndOptimization: Boolean,
    modifier: Modifier = Modifier
) {
    val items = remember(topGames) { topGames.take(3) }
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { items.size.coerceAtLeast(1) }
    )
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_top_free_section"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(NeonGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Text(
                        text = "BEST 3 FREE GAMES TODAY",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = "Top-rated 100% free drops • Swipe cards",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            if (items.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E222D))
                        .border(1.dp, Color(0xFF2C3244), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1} of ${items.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen
                    )
                }
            }
        }

        if (items.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141720)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262B3B))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = NeonGreen,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Loading today's best free games...",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            // Swipeable Carousel Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_top_free_pager"),
                pageSpacing = 12.dp
            ) { page ->
                val game = items[page]
                BestFreeGameCard(
                    game = game,
                    index = page,
                    onClick = { onItemClick(game) },
                    onClaimClick = { onClaimClick(game) }
                )
            }

            // Indicator Dots
            if (items.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.indices.forEach { index ->
                        val isSelected = pagerState.currentPage == index
                        val dotWidth by animateFloatAsState(
                            targetValue = if (isSelected) 22f else 6f,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "carousel_dot_width_$index"
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .height(6.dp)
                                .width(dotWidth.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) NeonGreen else Color(0xFF32384A))
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual Card in Best Free Games Carousel
 */
@Composable
private fun BestFreeGameCard(
    game: GiveawayItem,
    index: Int,
    onClick: () -> Unit,
    onClaimClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("home_top_free_card_$index"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF151821)
        ),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF282E40))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Image with Gradient & Floating Badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 8.5f)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(game.image)
                        .crossfade(true)
                        .build(),
                    contentDescription = game.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark Bottom Gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x800D0F14),
                                    Color(0xFF151821)
                                )
                            )
                        )
                )

                // Top Badges Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 100% Free / Strikethrough Price
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xE60D0F15))
                            .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (game.worth.isNotBlank() && game.worth != "N/A" && game.worth != "$0.00") {
                                Text(
                                    text = game.worth,
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }
                            Text(
                                text = "100% FREE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonGreen
                            )
                        }
                    }

                    // Metacritic Score (if available)
                    if (game.metacriticScore > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xE60D0F15))
                                .border(
                                    1.dp,
                                    if (game.metacriticScore >= 75) NeonGreen.copy(alpha = 0.7f) else NeonGold.copy(alpha = 0.7f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "METASCORE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "${game.metacriticScore}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (game.metacriticScore >= 75) NeonGreen else NeonGold
                                )
                            }
                        }
                    }
                }

                // Bottom Platform & End date info on image
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xCC000000))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = game.platforms,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonCyan,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (game.endDate != "N/A" && game.endDate.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xCC000000))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Ends: ${game.endDate}",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Body: Title & Action
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = game.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (game.description.isNotBlank()) {
                    Text(
                        text = game.description,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Actions row: [Smaller Claim Loot] [Details (Middle)] [Share (Right)]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Smaller Claim Free Loot button (Left)
                    Button(
                        onClick = onClaimClick,
                        modifier = Modifier
                            .weight(1.15f)
                            .height(40.dp)
                            .testTag("home_top_free_claim_btn_$index"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGreen,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Claim Loot",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }

                    // 2. Details button (Middle)
                    OutlinedButton(
                        onClick = onClick,
                        modifier = Modifier
                            .weight(0.95f)
                            .height(40.dp)
                            .testTag("home_top_free_details_btn_$index"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF384055)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0x22151821),
                            contentColor = TextSecondary
                        )
                    ) {
                        Text(
                            text = "Details",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    // 3. Share button (Right)
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    com.example.util.ShareGraphicGenerator.shareGraphicCard(
                                        context = context,
                                        item = game,
                                        includeTextLink = true
                                    )
                                } catch (_: Exception) {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "🔥 Free Game Deal: ${game.title} (${game.worth})!\nClaim here: ${game.openGiveawayUrl}\nTracked with LootDrop"
                                        )
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Free Game Deal"))
                                }
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("home_top_free_share_btn_$index"),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF384055)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0x22151821),
                            contentColor = NeonCyan
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share ${game.title}",
                            tint = NeonCyan,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3 Most Recent Games Played on Steam
 */
@Composable
private fun HomeRecentSteamGamesSection(
    games: List<SteamGameEntity>,
    steamUserId: String,
    isSyncingSteam: Boolean,
    onOpenSteamDialog: () -> Unit,
    onQuickSteamSync: () -> Unit,
    onOpenAchievements: (SteamGameEntity) -> Unit,
    isAnimationsEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_recent_steam_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Text(
                        text = "RECENT STEAM ACTIVITY",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = "Your 3 most recent played games & achievements",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            if (steamUserId.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isSyncingSteam) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = NeonCyan,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E222D))
                                .clickable { onQuickSteamSync() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("home_steam_quick_sync_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Sync Steam",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Sync",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan
                                )
                            }
                        }
                    }
                }
            }
        }

        if (games.isEmpty()) {
            // Unconnected or no games found card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_steam_connect_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151822)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF282E40))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = if (steamUserId.isBlank()) "Connect Your Steam Account" else "No Steam Games Synced",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = if (steamUserId.isBlank()) {
                            "Link your Steam profile to display your recent games, hours played, and linked trophy achievements right here on your Home tab."
                        } else {
                            "Sync your Steam library to import your playtime, recently played games, and achievements."
                        },
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Button(
                        onClick = onOpenSteamDialog,
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("home_connect_steam_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (steamUserId.isBlank()) "Connect Steam ID" else "Manage Steam Sync",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        } else {
            // Render the 3 most recent Steam games
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                games.forEachIndexed { index, game ->
                    HomeRecentSteamGameCard(
                        game = game,
                        index = index,
                        onOpenAchievements = onOpenAchievements
                    )
                }
            }
        }
    }
}

/**
 * Individual Steam Game Card in Home Tab
 */
@Composable
private fun HomeRecentSteamGameCard(
    game: SteamGameEntity,
    index: Int,
    onOpenAchievements: (SteamGameEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_recent_steam_card_${game.appId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF161923)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF282E42))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Row: Thumbnail + Title & Playtime
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Header Artwork
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F1117))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(game.headerImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = game.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Details: Title, Playtime
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = game.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Playtime Information
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${game.playtimeHours} total played",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonCyan
                        )
                    }

                    if (game.playtime2WeeksMinutes > 0) {
                        val recentHours = (game.playtime2WeeksMinutes / 60.0)
                        val formattedRecent = if (recentHours >= 10.0) {
                            String.format(java.util.Locale.US, "%.0fh", recentHours)
                        } else {
                            String.format(java.util.Locale.US, "%.1fh", recentHours)
                        }
                        Text(
                            text = "$formattedRecent past 2 weeks",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Quick Launch button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF222736))
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(game.launchUri))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse(game.storeUrl))
                                storeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(storeIntent)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Launch ${game.name}",
                        tint = NeonGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Bottom Section: Linked Achievements or "No Achievements" indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF12141C))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                if (game.achievementsTotal > 0) {
                    // Clickable Achievements row linking to full achievements dialog
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpenAchievements(game) }
                            .testTag("home_steam_achievements_${game.appId}")
                            .padding(2.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = if (game.achievementsPercentage == 100) NeonGreen else NeonCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Achievements: ${game.achievementsUnlocked}/${game.achievementsTotal}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "• View Guides ➔",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan
                                )
                            }

                            Text(
                                text = "${game.achievementsPercentage}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (game.achievementsPercentage == 100) NeonGreen else NeonCyan
                            )
                        }

                        LinearProgressIndicator(
                            progress = { (game.achievementsUnlocked.toFloat() / game.achievementsTotal.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (game.achievementsPercentage == 100) NeonGreen else NeonCyan,
                            trackColor = Color(0xFF232838)
                        )
                    }
                } else {
                    // Clearly marks as no achievements
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_steam_no_achievements_${game.appId}"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "No Steam Achievements for this title",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        Text(
                            text = "Unsynced",
                            fontSize = 10.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
