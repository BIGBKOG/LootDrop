package com.example.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SteamGameEntity
import com.example.data.model.GiveawayItem
import com.example.data.model.PublicReview
import com.example.data.model.SteamDlcRecommendation
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.ui.UiStats
import com.example.ui.components.HomeDlcRecommendationsSection
import com.example.ui.components.LootDropAppRatingCard
import com.example.ui.density.ScreenDensityInfo
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.expressivePressScale
import java.util.Locale

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
    publicReviews: List<PublicReview> = emptyList(),
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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item(key = "home_quick_stats_strip") {
                HomeQuickStatsStrip(
                    uiStats = uiStats,
                    onExploreAllFreebies = onExploreAllFreebies,
                    onViewVault = onViewVault,
                    enablePressScale = isAnimationsEnabled && !isLowEndOptimization
                )
            }

            item(key = "home_top_free_carousel_section") {
                HomeBestFreeGamesSection(
                    topGames = top3FreeGames,
                    onItemClick = onItemClick,
                    onClaimClick = onClaimClick,
                    isAnimationsEnabled = isAnimationsEnabled,
                    isLowEndOptimization = isLowEndOptimization
                )
            }

            item(key = "home_recent_steam_section") {
                HomeRecentSteamGamesSection(
                    games = recentSteamGames,
                    steamUserId = steamUserId,
                    isSyncingSteam = isSyncingSteam,
                    onOpenSteamDialog = onOpenSteamDialog,
                    onQuickSteamSync = onQuickSteamSync,
                    onOpenAchievements = onOpenAchievements
                )
            }

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
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(Color(0xFFFFD700).copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB800), modifier = Modifier.size(13.dp))
                        }
                        val s = LocalAppStrings.current
                        Text(
                            text = s.settings.generalReviewsSection,
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = 0.6.sp
                        )
                    }
                    LootDropAppRatingCard(
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
    onExploreAllFreebies: () -> Unit,
    onViewVault: () -> Unit,
    enablePressScale: Boolean = true,
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    val lang = s.language
    Card(
        modifier = modifier.fillMaxWidth().testTag("home_quick_stats_strip"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val exploreInteraction = remember { MutableInteractionSource() }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .expressivePressScale(exploreInteraction, enabled = enablePressScale, pressedScale = 0.94f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(interactionSource = exploreInteraction, indication = null) { onExploreAllFreebies() }
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(NeonCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Explore, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                }
                Column {
                    val availableCount = if (uiStats.activeGiveawaysCount > 0) uiStats.activeGiveawaysCount else uiStats.newFreebiesCount
                    Text(
                        when (lang) {
                            AppLanguage.SPANISH -> "$availableCount Disponibles"
                            AppLanguage.FRENCH -> "$availableCount Disponibles"
                            AppLanguage.ENGLISH -> "$availableCount Available"
                        },
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        when (lang) {
                            AppLanguage.SPANISH -> "Explorar Gratis ➔"
                            AppLanguage.FRENCH -> "Explorer Gratuit ➔"
                            AppLanguage.ENGLISH -> "Explore Free ➔"
                        },
                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = NeonCyan
                    )
                }
            }

            Box(
                modifier = Modifier.width(1.dp).height(30.dp).background(MaterialTheme.colorScheme.outlineVariant)
            )

            val vaultInteraction = remember { MutableInteractionSource() }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .expressivePressScale(vaultInteraction, enabled = enablePressScale, pressedScale = 0.94f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(interactionSource = vaultInteraction, indication = null) { onViewVault() }
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(NeonGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Savings, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                }
                Column {
                    val savedFormatted = String.format(Locale.US, "$%.2f", uiStats.claimedTotalSavedUsd)
                    Text(savedFormatted, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                    Text(
                        when (lang) {
                            AppLanguage.SPANISH -> "Mi Bóveda ➔"
                            AppLanguage.FRENCH -> "Mon Coffre ➔"
                            AppLanguage.ENGLISH -> "Claimed Vault ➔"
                        },
                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
