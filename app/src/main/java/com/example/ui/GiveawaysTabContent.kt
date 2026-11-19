package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SteamAchievementEntity
import com.example.data.local.SteamGameEntity
import com.example.data.model.GameCategory
import com.example.data.model.GiveawayItem
import com.example.data.model.LauncherType
import com.example.data.model.LibraryPlayFilter
import com.example.data.model.LibraryViewMode
import com.example.data.model.PlayStatus
import com.example.data.model.PlayabilityFilter
import com.example.data.model.StorageFilter
import com.example.data.model.UnifiedLibraryItem
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.preferences.CardDensity
import com.example.ui.components.BacklogRouletteDialog
import com.example.ui.components.GiveawayCard
import com.example.ui.components.LibraryPlayFilterChips
import com.example.ui.components.StatsBanner
import com.example.ui.components.SteamGameCard
import com.example.ui.components.TrophyShowcaseContent
import com.example.ui.density.ScreenDensityInfo
import com.example.ui.theme.ExpressiveMotion
import com.example.ui.theme.ExpressivePlacementSpec
import com.example.ui.theme.NeonGreen
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiveawaysTabContent(
    tab: MainTab,
    giveaways: List<GiveawayItem>,
    isLoading: Boolean,
    errorMessage: String? = null,
    searchQuery: String = "",
    uiStats: UiStats,
    isNewTodayDismissed: Boolean,
    activeFilterCount: Int,
    selectedLaunchers: Set<LauncherType>,
    selectedTypeFilter: TypeFilter,
    selectedCategories: Set<GameCategory> = emptySet(),
    selectedSort: SortOption,
    cardDensity: CardDensity,
    isShowOriginalPrice: Boolean,
    isShowExpiration: Boolean,
    isAnimationsEnabled: Boolean,
    isLowEndOptimization: Boolean = false,
    beamAngle: Float = 0f,
    screenDensityInfo: ScreenDensityInfo,
    listState: LazyListState,
    gridState: LazyGridState,
    onRefresh: () -> Unit,
    onDismissNewToday: () -> Unit,
    onNewTodayClick: () -> Unit,
    onOpenFilterSheet: () -> Unit,
    onResetFilters: () -> Unit,
    onItemClick: (GiveawayItem) -> Unit,
    onClaimClick: (GiveawayItem) -> Unit,
    onToggleBookmark: (GiveawayItem) -> Unit,
    onToggleClaimed: (GiveawayItem) -> Unit,
    selectedLibraryPlayFilter: LibraryPlayFilter = LibraryPlayFilter.ALL,
    isSyncingSteam: Boolean = false,
    steamUserId: String = "",
    steamSyncStatus: String? = null,
    onFilterSelect: (LibraryPlayFilter) -> Unit = {},
    onOpenSteamDialog: () -> Unit = {},
    onQuickSteamSync: () -> Unit = {},
    onDismissSteamStatus: () -> Unit = {},
    onPlayStatusChange: ((GiveawayItem, PlayStatus) -> Unit)? = null,
    libraryViewMode: LibraryViewMode = LibraryViewMode.VAULT,
    steamGames: List<SteamGameEntity> = emptyList(),
    onViewModeChange: (LibraryViewMode) -> Unit = {},
    onSteamPlayStatusChange: ((SteamGameEntity, PlayStatus) -> Unit)? = null,
    selectedPlayabilityFilter: PlayabilityFilter = PlayabilityFilter.ALL,
    onPlayabilityFilterSelect: (PlayabilityFilter) -> Unit = {},
    selectedStorageFilter: StorageFilter = StorageFilter.ALL,
    onStorageFilterSelect: (StorageFilter) -> Unit = {},
    onSteamManualStorageChange: ((SteamGameEntity, Float?) -> Unit)? = null,
    achievementOverviewStats: AchievementOverviewStats = AchievementOverviewStats(),
    rarestAchievements: List<SteamAchievementEntity> = emptyList(),
    closestToHundredGames: List<SteamGameEntity> = emptyList(),
    onOpenAchievements: ((SteamGameEntity) -> Unit)? = null,
    onOpenBacklogChallenge: (() -> Unit)? = null,
    dlcRecommendationsCount: Int = 0,
    onOpenDlcHub: (() -> Unit)? = null,
    onOpenGameDlcs: ((SteamGameEntity) -> Unit)? = null,
    trophyListState: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier
) {
    val isAllPaidMode = tab == MainTab.CLAIMED && libraryViewMode == LibraryViewMode.ALL_PAID
    val allPaidCombinedItems: List<UnifiedLibraryItem> = remember(giveaways, steamGames, selectedSort, isAllPaidMode) {
        if (!isAllPaidMode) emptyList()
        else {
            val steamAppIds = steamGames.map { it.appId }.toSet()
            val steamTitles = steamGames.map { it.name.trim().lowercase() }.toSet()
            val nonDuplicateVault = giveaways.filter { g ->
                !(g.steamAppId != null && steamAppIds.contains(g.steamAppId)) && !steamTitles.contains(g.title.trim().lowercase())
            }.map { UnifiedLibraryItem.Vault(it) }
            val steamItems = steamGames.map { UnifiedLibraryItem.Steam(it) }
            (steamItems + nonDuplicateVault).sortedWith(
                compareByDescending<UnifiedLibraryItem> {
                    when (it) {
                        is UnifiedLibraryItem.Steam -> it.game.lastPlayedTimestamp
                        is UnifiedLibraryItem.Vault -> it.giveaway.firstSeenTimestamp
                    }
                }.thenBy { it.name.lowercase() }
            )
        }
    }

    val context = LocalContext.current
    val s = LocalAppStrings.current
    val lang = s.language
    var showBacklogRoulette by remember { mutableStateOf(false) }
    val backlogCandidates = remember(allPaidCombinedItems) {
        allPaidCombinedItems.filter { it.playStatus == PlayStatus.NOT_STARTED || it.playStatus == PlayStatus.PLAYING }
    }

    val reducedMotion = !isAnimationsEnabled || isLowEndOptimization
    val isListEmpty = if (isAllPaidMode) allPaidCombinedItems.isEmpty() else giveaways.isEmpty()
    val showLibraryTopMenusInline = tab == MainTab.CLAIMED && !isListEmpty
    val showDiscoverTopHeaderInline = tab == MainTab.DISCOVER && !isListEmpty

    val discoverTopHeaderContent: @Composable () -> Unit = {
        Column(modifier = Modifier.fillMaxWidth()) {
            StatsBanner(
                stats = uiStats, isRefreshing = isLoading, onRefresh = onRefresh,
                isNewTodayDismissed = isNewTodayDismissed, onDismissNewToday = onDismissNewToday,
                onNewTodayClick = onNewTodayClick, isAnimationsEnabled = isAnimationsEnabled && !isLowEndOptimization
            )
            if (activeFilterCount > 0) {
                ActiveFiltersSummaryPill(
                    selectedLaunchers = selectedLaunchers, selectedTypeFilter = selectedTypeFilter,
                    selectedCategories = selectedCategories, selectedSort = selectedSort,
                    onOpenFilterSheet = onOpenFilterSheet, onResetFilters = onResetFilters
                )
            }
        }
    }

    val libraryTopMenusContent: @Composable () -> Unit = {
        InLibraryTopMenus(
            libraryViewMode = libraryViewMode, uiStats = uiStats, isAnimationsEnabled = isAnimationsEnabled,
            isLowEndOptimization = isLowEndOptimization, selectedLibraryPlayFilter = selectedLibraryPlayFilter,
            onFilterSelect = onFilterSelect, isSyncingSteam = isSyncingSteam, steamUserId = steamUserId,
            steamSyncStatus = steamSyncStatus, onOpenSteamDialog = onOpenSteamDialog,
            onQuickSteamSync = onQuickSteamSync, onDismissSteamStatus = onDismissSteamStatus,
            onViewModeChange = onViewModeChange, selectedPlayabilityFilter = selectedPlayabilityFilter,
            onPlayabilityFilterSelect = onPlayabilityFilterSelect, selectedStorageFilter = selectedStorageFilter,
            onStorageFilterSelect = onStorageFilterSelect, trophyCount = achievementOverviewStats.totalUnlockedTrophies,
            activeFilterCount = activeFilterCount, selectedLaunchers = selectedLaunchers,
            selectedTypeFilter = selectedTypeFilter, selectedCategories = selectedCategories,
            selectedSort = selectedSort, onOpenFilterSheet = onOpenFilterSheet, onResetFilters = onResetFilters,
            onRollRandomBacklog = { showBacklogRoulette = true }, onOpenBacklogChallenge = onOpenBacklogChallenge,
            dlcRecommendationsCount = dlcRecommendationsCount, onOpenDlcHub = onOpenDlcHub
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (tab == MainTab.DISCOVER && !showDiscoverTopHeaderInline) discoverTopHeaderContent()
        if (tab == MainTab.CLAIMED && !showLibraryTopMenusInline) libraryTopMenusContent()

        if (tab == MainTab.CLAIMED && libraryViewMode == LibraryViewMode.ACHIEVEMENTS) {
            TrophyShowcaseContent(
                overviewStats = achievementOverviewStats, rarestTrophies = rarestAchievements,
                closestGames = closestToHundredGames, isSyncing = isSyncingSteam, steamUserId = steamUserId,
                onSyncClick = onQuickSteamSync, onOpenSteamSettings = onOpenSteamDialog,
                onSelectGameForAchievements = { onOpenAchievements?.invoke(it) },
                topHeader = if (showLibraryTopMenusInline) ({ libraryTopMenusContent() }) else null,
                listState = trophyListState, isAnimationsEnabled = isAnimationsEnabled,
                isLowEndOptimization = isLowEndOptimization, modifier = Modifier.fillMaxSize()
            )
        } else {
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize().testTag("pull_to_refresh_box")
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (errorMessage != null && giveaways.isNotEmpty()) {
                        ErrorBannerRow(errorMessage = errorMessage, onRefresh = onRefresh)
                    }
                    when {
                        isLoading && isListEmpty -> LoadingFeedState(isAllPaidMode)
                        isListEmpty -> EmptyFeedState(
                            tab = tab, isAllPaidMode = isAllPaidMode, activeFilterCount = activeFilterCount,
                            searchQuery = searchQuery, errorMessage = errorMessage,
                            isAnimationsEnabled = isAnimationsEnabled, isLowEndOptimization = isLowEndOptimization,
                            onRefresh = onRefresh, onResetFilters = onResetFilters, onOpenSteamDialog = onOpenSteamDialog
                        )
                        isAllPaidMode -> AllPaidCollectionView(
                            items = allPaidCombinedItems, showLibraryTopMenusInline = showLibraryTopMenusInline,
                            libraryTopMenusContent = libraryTopMenusContent, screenDensityInfo = screenDensityInfo,
                            listState = listState, gridState = gridState, cardDensity = cardDensity,
                            isShowOriginalPrice = isShowOriginalPrice, isShowExpiration = isShowExpiration,
                            isAnimationsEnabled = isAnimationsEnabled, isLowEndOptimization = isLowEndOptimization,
                            reducedMotion = reducedMotion, beamAngle = beamAngle,
                            onSteamPlayStatusChange = onSteamPlayStatusChange, onSteamManualStorageChange = onSteamManualStorageChange,
                            onOpenAchievements = onOpenAchievements, onOpenGameDlcs = onOpenGameDlcs,
                            onItemClick = onItemClick, onClaimClick = onClaimClick,
                            onToggleBookmark = onToggleBookmark, onToggleClaimed = onToggleClaimed,
                            onPlayStatusChange = onPlayStatusChange
                        )
                        else -> GiveawaysCollectionView(
                            giveaways = giveaways, showDiscoverTopHeaderInline = showDiscoverTopHeaderInline,
                            discoverTopHeaderContent = discoverTopHeaderContent,
                            showLibraryTopMenusInline = showLibraryTopMenusInline,
                            libraryTopMenusContent = libraryTopMenusContent,
                            screenDensityInfo = screenDensityInfo, listState = listState, gridState = gridState,
                            cardDensity = cardDensity, isShowOriginalPrice = isShowOriginalPrice,
                            isShowExpiration = isShowExpiration, isAnimationsEnabled = isAnimationsEnabled,
                            isLowEndOptimization = isLowEndOptimization, reducedMotion = reducedMotion,
                            beamAngle = beamAngle, onItemClick = onItemClick, onClaimClick = onClaimClick,
                            onToggleBookmark = onToggleBookmark, onToggleClaimed = onToggleClaimed,
                            onPlayStatusChange = onPlayStatusChange
                        )
                    }
                }
            }
        }
    }

    if (showBacklogRoulette) {
        BacklogRouletteDialog(
            candidates = backlogCandidates,
            onDismiss = { showBacklogRoulette = false },
            onLaunchGame = { item ->
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.launchUri)).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                } catch (_: Exception) {
                    val fallbackUri = when (item) {
                        is UnifiedLibraryItem.Steam -> Uri.parse(item.game.storeUrl)
                        is UnifiedLibraryItem.Vault -> Uri.parse(item.giveaway.openGiveawayUrl)
                    }
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, fallbackUri).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                    } catch (_: Exception) {
                        Toast.makeText(
                            context,
                            when (lang) {
                                AppLanguage.SPANISH -> "No se pudo abrir ${item.name}"
                                AppLanguage.FRENCH -> "Impossible d'ouvrir ${item.name}"
                                AppLanguage.ENGLISH -> "Could not open ${item.name}"
                            },
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                showBacklogRoulette = false
            }
        )
    }
}

@Composable
private fun ErrorBannerRow(errorMessage: String, onRefresh: () -> Unit) {
    val s = LocalAppStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            Icon(Icons.Default.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(18.dp))
            Text(errorMessage, fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer, maxLines = 2)
        }
        IconButton(onClick = onRefresh, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Refresh, contentDescription = s.common.retry, tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun LoadingFeedState(isAllPaidMode: Boolean) {
    val s = LocalAppStrings.current
    val lang = s.language
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (isAllPaidMode) {
                when (lang) {
                    AppLanguage.SPANISH -> "Cargando juegos de Steam..."
                    AppLanguage.FRENCH -> "Chargement des jeux Steam..."
                    AppLanguage.ENGLISH -> "Loading Steam library games..."
                }
            } else {
                s.common.syncingFreeGameRadar
            },
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyFeedState(
    tab: MainTab,
    isAllPaidMode: Boolean,
    activeFilterCount: Int,
    searchQuery: String,
    errorMessage: String?,
    isAnimationsEnabled: Boolean,
    isLowEndOptimization: Boolean,
    onRefresh: () -> Unit,
    onResetFilters: () -> Unit,
    onOpenSteamDialog: () -> Unit
) {
    val s = LocalAppStrings.current
    val lang = s.language
    val emptyIconBob = rememberEmptyBob(enabled = isAnimationsEnabled && !isLowEndOptimization)
    val isFiltered = activeFilterCount > 0 || searchQuery.isNotBlank()
    val hasError = errorMessage != null

    Column(
        modifier = Modifier.fillMaxSize().padding(start = 32.dp, end = 32.dp, top = 32.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.graphicsLayer { translationY = emptyIconBob }.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when {
                    isAllPaidMode -> Icons.Default.SportsEsports
                    tab == MainTab.CLAIMED -> Icons.Default.CheckCircle
                    hasError -> Icons.Default.CloudOff
                    isFiltered -> Icons.Default.FilterAltOff
                    else -> Icons.Default.SportsEsports
                },
                contentDescription = null,
                tint = if (hasError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = when {
                isAllPaidMode -> if (searchQuery.isNotBlank()) s.home.allPaidNoGamesTitle else when (lang) {
                    AppLanguage.SPANISH -> "Sin Juegos en la Biblioteca"
                    AppLanguage.FRENCH -> "Aucun Jeu dans la Bibliothèque"
                    AppLanguage.ENGLISH -> "No Games in Library"
                }
                tab == MainTab.CLAIMED -> s.home.vaultNoGamesTitle
                hasError -> when (lang) {
                    AppLanguage.SPANISH -> "No se Pudieron Cargar las Ofertas"
                    AppLanguage.FRENCH -> "Impossible de Charger les Offres"
                    AppLanguage.ENGLISH -> "Unable to Fetch Giveaways"
                }
                isFiltered -> s.home.discoverNoMatchesTitle
                else -> when (lang) {
                    AppLanguage.SPANISH -> "No Hay Ofertas Activas"
                    AppLanguage.FRENCH -> "Aucune Offre Active"
                    AppLanguage.ENGLISH -> "No Giveaways Found"
                }
            },
            fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = when {
                isAllPaidMode -> if (searchQuery.isNotBlank()) {
                    when (lang) {
                        AppLanguage.SPANISH -> "Ningún juego coincide con \"${searchQuery.take(20)}\" en tu colección."
                        AppLanguage.FRENCH -> "Aucun jeu ne correspond à « ${searchQuery.take(20)} » dans votre collection."
                        AppLanguage.ENGLISH -> "No games match \"${searchQuery.take(20)}\" in your collection."
                    }
                } else {
                    s.home.allPaidNoGamesSubtitle
                }
                tab == MainTab.CLAIMED -> s.home.vaultNoGamesSubtitle
                hasError -> errorMessage.ifBlank {
                    when (lang) {
                        AppLanguage.SPANISH -> "Problema de red o servidor no disponible. Toca reintentar."
                        AppLanguage.FRENCH -> "Problème réseau ou serveur indisponible. Appuyez sur réessayer."
                        AppLanguage.ENGLISH -> "Network issue or server unavailable. Tap retry to check again."
                    }
                }
                isFiltered -> if (searchQuery.isNotBlank()) {
                    when (lang) {
                        AppLanguage.SPANISH -> "Ningún juego coincide con \"${searchQuery.take(20)}\" o tus filtros activos."
                        AppLanguage.FRENCH -> "Aucun jeu ne correspond à « ${searchQuery.take(20)} » ou à vos filtres actifs."
                        AppLanguage.ENGLISH -> "No games match \"${searchQuery.take(20)}\" or your active launcher filters."
                    }
                } else {
                    s.home.discoverNoMatchesSubtitle
                }
                else -> when (lang) {
                    AppLanguage.SPANISH -> "No hay ofertas activas en este momento. Desliza para actualizar o vuelve pronto."
                    AppLanguage.FRENCH -> "Aucune offre active pour le moment. Tirez pour actualiser ou revenez bientôt !"
                    AppLanguage.ENGLISH -> "Currently no active giveaways reported. Pull down to refresh or check back soon!"
                }
            },
            fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp, textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        when {
            hasError -> Button(onClick = onRefresh, modifier = Modifier.testTag("retry_button")) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text(s.common.retry)
            }
            isFiltered && tab == MainTab.DISCOVER -> OutlinedButton(onClick = onResetFilters, modifier = Modifier.testTag("reset_filters_button")) {
                Icon(Icons.Default.FilterAltOff, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text(s.home.discoverClearFilters)
            }
            isAllPaidMode -> Button(onClick = onOpenSteamDialog, modifier = Modifier.testTag("sync_steam_all_paid_button"), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66C0F4), contentColor = Color(0xFF0B0F19))) {
                Icon(Icons.Default.SportsEsports, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text(s.home.vaultSyncSteamBtn, fontWeight = FontWeight.Bold)
            }
            tab == MainTab.DISCOVER -> Button(onClick = onRefresh, modifier = Modifier.testTag("refresh_giveaways_button")) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text(s.common.refresh)
            }
        }
    }
}

@Composable
private fun AllPaidCollectionView(
    items: List<UnifiedLibraryItem>,
    showLibraryTopMenusInline: Boolean,
    libraryTopMenusContent: @Composable () -> Unit,
    screenDensityInfo: ScreenDensityInfo,
    listState: LazyListState,
    gridState: LazyGridState,
    cardDensity: CardDensity,
    isShowOriginalPrice: Boolean,
    isShowExpiration: Boolean,
    isAnimationsEnabled: Boolean,
    isLowEndOptimization: Boolean,
    reducedMotion: Boolean,
    beamAngle: Float,
    onSteamPlayStatusChange: ((SteamGameEntity, PlayStatus) -> Unit)?,
    onSteamManualStorageChange: ((SteamGameEntity, Float?) -> Unit)?,
    onOpenAchievements: ((SteamGameEntity) -> Unit)?,
    onOpenGameDlcs: ((SteamGameEntity) -> Unit)?,
    onItemClick: (GiveawayItem) -> Unit,
    onClaimClick: (GiveawayItem) -> Unit,
    onToggleBookmark: (GiveawayItem) -> Unit,
    onToggleClaimed: (GiveawayItem) -> Unit,
    onPlayStatusChange: ((GiveawayItem, PlayStatus) -> Unit)?
) {
    val padding = PaddingValues(start = screenDensityInfo.adaptiveHorizontalPadding, end = screenDensityInfo.adaptiveHorizontalPadding, top = 6.dp, bottom = 104.dp)
    if (screenDensityInfo.gridColumns > 1) {
        LazyVerticalGrid(
            state = gridState, columns = GridCells.Fixed(screenDensityInfo.gridColumns),
            modifier = Modifier.fillMaxSize().testTag("steam_games_grid"), contentPadding = padding,
            horizontalArrangement = Arrangement.spacedBy(screenDensityInfo.adaptiveCardSpacing),
            verticalArrangement = Arrangement.spacedBy(screenDensityInfo.adaptiveCardSpacing)
        ) {
            if (showLibraryTopMenusInline) item(key = "in_library_top_menus", span = { GridItemSpan(maxLineSpan) }) { libraryTopMenusContent() }
            items(items, key = { it.key }) { item ->
                val animMod = if (!reducedMotion) Modifier.animateItem(ExpressiveMotion.effectsDefault(), ExpressivePlacementSpec, ExpressiveMotion.effectsFast()) else Modifier
                UnifiedCardEntry(item, cardDensity, isShowOriginalPrice, isShowExpiration, isAnimationsEnabled, isLowEndOptimization, beamAngle, animMod, onSteamPlayStatusChange, onSteamManualStorageChange, onOpenAchievements, onOpenGameDlcs, onItemClick, onClaimClick, onToggleBookmark, onToggleClaimed, onPlayStatusChange)
            }
        }
    } else {
        LazyColumn(
            state = listState, modifier = Modifier.fillMaxSize().testTag("steam_games_list"), contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(screenDensityInfo.adaptiveCardSpacing)
        ) {
            if (showLibraryTopMenusInline) item(key = "in_library_top_menus") { libraryTopMenusContent() }
            items(items, key = { it.key }) { item ->
                val animMod = if (!reducedMotion) Modifier.animateItem(ExpressiveMotion.effectsDefault(), ExpressivePlacementSpec, ExpressiveMotion.effectsFast()) else Modifier
                UnifiedCardEntry(item, cardDensity, isShowOriginalPrice, isShowExpiration, isAnimationsEnabled, isLowEndOptimization, beamAngle, animMod, onSteamPlayStatusChange, onSteamManualStorageChange, onOpenAchievements, onOpenGameDlcs, onItemClick, onClaimClick, onToggleBookmark, onToggleClaimed, onPlayStatusChange)
            }
        }
    }
}

@Composable
private fun GiveawaysCollectionView(
    giveaways: List<GiveawayItem>,
    showDiscoverTopHeaderInline: Boolean,
    discoverTopHeaderContent: @Composable () -> Unit,
    showLibraryTopMenusInline: Boolean,
    libraryTopMenusContent: @Composable () -> Unit,
    screenDensityInfo: ScreenDensityInfo,
    listState: LazyListState,
    gridState: LazyGridState,
    cardDensity: CardDensity,
    isShowOriginalPrice: Boolean,
    isShowExpiration: Boolean,
    isAnimationsEnabled: Boolean,
    isLowEndOptimization: Boolean,
    reducedMotion: Boolean,
    beamAngle: Float,
    onItemClick: (GiveawayItem) -> Unit,
    onClaimClick: (GiveawayItem) -> Unit,
    onToggleBookmark: (GiveawayItem) -> Unit,
    onToggleClaimed: (GiveawayItem) -> Unit,
    onPlayStatusChange: ((GiveawayItem, PlayStatus) -> Unit)?
) {
    val padding = PaddingValues(start = screenDensityInfo.adaptiveHorizontalPadding, end = screenDensityInfo.adaptiveHorizontalPadding, top = 6.dp, bottom = 104.dp)
    if (screenDensityInfo.gridColumns > 1) {
        LazyVerticalGrid(
            state = gridState, columns = GridCells.Fixed(screenDensityInfo.gridColumns),
            modifier = Modifier.fillMaxSize().testTag("giveaways_list"), contentPadding = padding,
            horizontalArrangement = Arrangement.spacedBy(screenDensityInfo.adaptiveCardSpacing),
            verticalArrangement = Arrangement.spacedBy(screenDensityInfo.adaptiveCardSpacing)
        ) {
            if (showDiscoverTopHeaderInline) item(key = "discover_top_header", span = { GridItemSpan(maxLineSpan) }) { discoverTopHeaderContent() }
            if (showLibraryTopMenusInline) item(key = "in_library_top_menus", span = { GridItemSpan(maxLineSpan) }) { libraryTopMenusContent() }
            items(giveaways, key = { it.id }, contentType = { cardDensity.name }) { item ->
                val animMod = if (!reducedMotion) Modifier.animateItem(ExpressiveMotion.effectsDefault(), ExpressivePlacementSpec, ExpressiveMotion.effectsFast()) else Modifier
                GiveawayCard(item, cardDensity, isShowOriginalPrice, isShowExpiration, { onItemClick(item) }, { onClaimClick(item) }, { onToggleBookmark(item) }, { onToggleClaimed(item) }, { onPlayStatusChange?.invoke(item, it) }, isAnimationsEnabled, isLowEndOptimization, beamAngle, animMod)
            }
        }
    } else {
        LazyColumn(
            state = listState, modifier = Modifier.fillMaxSize().testTag("giveaways_list"), contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(
                when (cardDensity) {
                    CardDensity.RICH_CARDS -> screenDensityInfo.adaptiveCardSpacing
                    CardDensity.COMPACT_CARDS -> (screenDensityInfo.adaptiveCardSpacing.value * 0.75f).dp
                    CardDensity.MINIMAL_LIST -> 6.dp
                }
            )
        ) {
            if (showDiscoverTopHeaderInline) item(key = "discover_top_header") { discoverTopHeaderContent() }
            if (showLibraryTopMenusInline) item(key = "in_library_top_menus") { libraryTopMenusContent() }
            items(giveaways, key = { it.id }, contentType = { cardDensity.name }) { item ->
                val animMod = if (!reducedMotion) Modifier.animateItem(ExpressiveMotion.effectsDefault(), ExpressivePlacementSpec, ExpressiveMotion.effectsFast()) else Modifier
                GiveawayCard(item, cardDensity, isShowOriginalPrice, isShowExpiration, { onItemClick(item) }, { onClaimClick(item) }, { onToggleBookmark(item) }, { onToggleClaimed(item) }, { onPlayStatusChange?.invoke(item, it) }, isAnimationsEnabled, isLowEndOptimization, beamAngle, animMod)
            }
        }
    }
}

@Composable
private fun UnifiedCardEntry(
    item: UnifiedLibraryItem,
    cardDensity: CardDensity,
    isShowOriginalPrice: Boolean,
    isShowExpiration: Boolean,
    isAnimationsEnabled: Boolean,
    isLowEndOptimization: Boolean,
    beamAngle: Float,
    modifier: Modifier,
    onSteamPlayStatusChange: ((SteamGameEntity, PlayStatus) -> Unit)?,
    onSteamManualStorageChange: ((SteamGameEntity, Float?) -> Unit)?,
    onOpenAchievements: ((SteamGameEntity) -> Unit)?,
    onOpenGameDlcs: ((SteamGameEntity) -> Unit)?,
    onItemClick: (GiveawayItem) -> Unit,
    onClaimClick: (GiveawayItem) -> Unit,
    onToggleBookmark: (GiveawayItem) -> Unit,
    onToggleClaimed: (GiveawayItem) -> Unit,
    onPlayStatusChange: ((GiveawayItem, PlayStatus) -> Unit)?
) {
    when (item) {
        is UnifiedLibraryItem.Steam -> SteamGameCard(
            game = item.game,
            onStatusChange = { onSteamPlayStatusChange?.invoke(item.game, it) },
            onUpdateManualStorage = { onSteamManualStorageChange?.invoke(item.game, it) },
            onOpenAchievements = { onOpenAchievements?.invoke(item.game) },
            onOpenDlcs = onOpenGameDlcs,
            isAnimationsEnabled = isAnimationsEnabled,
            isLowEndOptimization = isLowEndOptimization,
            modifier = modifier
        )
        is UnifiedLibraryItem.Vault -> GiveawayCard(
            item = item.giveaway, cardDensity = cardDensity, showOriginalPrice = isShowOriginalPrice,
            showExpiration = isShowExpiration, onClick = { onItemClick(item.giveaway) },
            onClaimClick = { onClaimClick(item.giveaway) }, onToggleBookmark = { onToggleBookmark(item.giveaway) },
            onToggleClaimed = { onToggleClaimed(item.giveaway) }, onPlayStatusChange = { onPlayStatusChange?.invoke(item.giveaway, it) },
            isAnimationsEnabled = isAnimationsEnabled, isLowEndOptimization = isLowEndOptimization,
            beamAngle = beamAngle, modifier = modifier
        )
    }
}

@Composable
private fun InLibraryTopMenus(
    libraryViewMode: LibraryViewMode,
    uiStats: UiStats,
    isAnimationsEnabled: Boolean,
    isLowEndOptimization: Boolean,
    selectedLibraryPlayFilter: LibraryPlayFilter,
    onFilterSelect: (LibraryPlayFilter) -> Unit,
    isSyncingSteam: Boolean,
    steamUserId: String,
    steamSyncStatus: String?,
    onOpenSteamDialog: () -> Unit,
    onQuickSteamSync: () -> Unit,
    onDismissSteamStatus: () -> Unit,
    onViewModeChange: (LibraryViewMode) -> Unit,
    selectedPlayabilityFilter: PlayabilityFilter,
    onPlayabilityFilterSelect: (PlayabilityFilter) -> Unit,
    selectedStorageFilter: StorageFilter,
    onStorageFilterSelect: (StorageFilter) -> Unit,
    trophyCount: Int,
    activeFilterCount: Int,
    selectedLaunchers: Set<LauncherType>,
    selectedTypeFilter: TypeFilter,
    selectedCategories: Set<GameCategory>,
    selectedSort: SortOption,
    onOpenFilterSheet: () -> Unit,
    onResetFilters: () -> Unit,
    onRollRandomBacklog: (() -> Unit)? = null,
    onOpenBacklogChallenge: (() -> Unit)? = null,
    dlcRecommendationsCount: Int = 0,
    onOpenDlcHub: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    val lang = s.language
    val reducedMotion = !isAnimationsEnabled || isLowEndOptimization
    var isCompactHeader by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        val totalStorage = if (libraryViewMode == LibraryViewMode.ALL_PAID) uiStats.steamTotalStorageGb + uiStats.claimedTotalStorageGb else uiStats.claimedTotalStorageGb
        val totalCount = if (libraryViewMode == LibraryViewMode.ALL_PAID) uiStats.steamTotalCount + uiStats.claimedCount else uiStats.claimedCount

        AnimatedVisibility(
            visible = libraryViewMode != LibraryViewMode.ACHIEVEMENTS && !isCompactHeader,
            enter = ExpressiveMotion.expressiveExpandEnter(reducedMotion),
            exit = ExpressiveMotion.expressiveCollapseExit(reducedMotion)
        ) {
            ClaimedSavingsBanner(uiStats.claimedTotalSavedUsd, totalCount, totalStorage, isAnimationsEnabled = !reducedMotion)
        }

        AnimatedVisibility(
            visible = libraryViewMode != LibraryViewMode.ACHIEVEMENTS && isCompactHeader,
            enter = ExpressiveMotion.expressiveExpandEnter(reducedMotion),
            exit = ExpressiveMotion.expressiveCollapseExit(reducedMotion)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .background(NeonGreen.copy(alpha = 0.12f))
                    .border(1.dp, NeonGreen.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    .clickable { isCompactHeader = false }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Savings, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(14.dp))
                    Text(
                        text = when (lang) {
                            AppLanguage.SPANISH -> "${String.format(Locale.US, "$%.2f", uiStats.claimedTotalSavedUsd)} Ahorrados • $totalCount juegos"
                            AppLanguage.FRENCH -> "${String.format(Locale.US, "$%.2f", uiStats.claimedTotalSavedUsd)} Économisés • $totalCount jeux"
                            AppLanguage.ENGLISH -> "${String.format(Locale.US, "$%.2f", uiStats.claimedTotalSavedUsd)} Saved • $totalCount games"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen
                    )
                }
                Text(
                    when (lang) {
                        AppLanguage.SPANISH -> "Expandir"
                        AppLanguage.FRENCH -> "Déplier"
                        AppLanguage.ENGLISH -> "Expand"
                    },
                    fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LibraryPlayFilterChips(
            selectedFilter = selectedLibraryPlayFilter, uiStats = uiStats, onFilterSelect = onFilterSelect,
            isSyncingSteam = isSyncingSteam, steamUserId = steamUserId, steamSyncStatus = steamSyncStatus,
            onOpenSteamDialog = onOpenSteamDialog, onQuickSteamSync = onQuickSteamSync, onDismissStatus = onDismissSteamStatus,
            viewMode = libraryViewMode, onViewModeChange = onViewModeChange, selectedPlayability = selectedPlayabilityFilter,
            onPlayabilitySelect = onPlayabilityFilterSelect, selectedStorage = selectedStorageFilter, onStorageSelect = onStorageFilterSelect,
            trophyCount = trophyCount, dlcRecommendationsCount = dlcRecommendationsCount, onOpenDlcHub = onOpenDlcHub,
            onRollRandomBacklog = onRollRandomBacklog, onOpenBacklogChallenge = onOpenBacklogChallenge,
            isAnimationsEnabled = isAnimationsEnabled, isLowEndOptimization = isLowEndOptimization,
            isCompactHeader = isCompactHeader, onToggleCompactHeader = { isCompactHeader = !isCompactHeader }
        )

        if (libraryViewMode != LibraryViewMode.ACHIEVEMENTS && activeFilterCount > 0) {
            ActiveFiltersSummaryPill(
                selectedLaunchers, selectedTypeFilter, selectedCategories, selectedSort, onOpenFilterSheet, onResetFilters
            )
        }
    }
}
