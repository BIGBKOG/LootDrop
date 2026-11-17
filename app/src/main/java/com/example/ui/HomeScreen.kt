package com.example.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.example.data.model.GiveawayItem
import com.example.data.model.LibraryViewMode
import com.example.i18n.LocalAppStrings
import com.example.ui.calendar.CalendarTabContent
import com.example.ui.components.ExpressiveFloatingNavBar
import com.example.ui.density.LocalScreenDensityInfo
import com.example.ui.home.HomeTabContent
import com.example.ui.theme.ExpressiveMotion
import com.example.ui.theme.rememberCollapsibleHeaderNestedScrollConnection
import com.example.ui.theme.rememberCollapsibleHeaderState
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: GiveawaysViewModel) {
    val context = LocalContext.current
    val screenDensityInfo = LocalScreenDensityInfo.current
    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedLaunchers by viewModel.selectedLaunchers.collectAsState()
    val selectedTypeFilter by viewModel.selectedTypeFilter.collectAsState()
    val selectedCategories by viewModel.selectedCategories.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val displayedGiveaways by viewModel.displayedGiveaways.collectAsState()
    val discoverGiveaways by viewModel.discoverGiveaways.collectAsState()
    val claimedGiveaways by viewModel.claimedGiveaways.collectAsState()
    val uiStats by viewModel.uiStats.collectAsState()
    val detailItem by viewModel.detailItem.collectAsState()
    val cardDensity by viewModel.cardDensity.collectAsState()
    val isShowOriginalPrice by viewModel.isShowOriginalPrice.collectAsState()
    val isShowExpiration by viewModel.isShowExpiration.collectAsState()
    val isFilterSheetOpen by viewModel.isFilterSheetOpen.collectAsState()
    val isCategorySheetOpen by viewModel.isCategorySheetOpen.collectAsState()
    val activeFilterCount by viewModel.activeFilterCount.collectAsState()
    val reopenStatusMessage by viewModel.reopenStatusMessage.collectAsState()
    val s = LocalAppStrings.current
    val appLanguage by viewModel.appLanguage.collectAsState()
    val hasSelectedInitialLanguage by viewModel.hasSelectedInitialLanguage.collectAsState()
    val showOnboardingWalkthrough by viewModel.showOnboardingWalkthrough.collectAsState()
    val isAnimationsEnabled by viewModel.isAnimationsEnabled.collectAsState()
    val isLowEndOptimization by viewModel.isLowEndOptimization.collectAsState()
    val isNewTodayDismissed by viewModel.isNewTodayDismissed.collectAsState()
    val unreadNotificationCount by viewModel.unreadNotificationCount.collectAsState()
    val isNotificationHistorySheetOpen by viewModel.isNotificationHistorySheetOpen.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val appUpdateInfo by viewModel.appUpdateInfo.collectAsState()
    val isUpdateDialogVisible by viewModel.isUpdateDialogVisible.collectAsState()
    val updateDownloadState by viewModel.updateDownloadState.collectAsState()
    val selectedLibraryPlayFilter by viewModel.selectedLibraryPlayFilter.collectAsState()
    val steamUserId by viewModel.steamUserId.collectAsState()
    val steamApiKey by viewModel.steamApiKey.collectAsState()
    val steamSyncStatus by viewModel.steamSyncStatus.collectAsState()
    val isSyncingSteam by viewModel.isSyncingSteam.collectAsState()
    val libraryViewMode by viewModel.libraryViewMode.collectAsState()
    val filteredSteamGames by viewModel.filteredSteamGames.collectAsState()
    val selectedPlayabilityFilter by viewModel.selectedPlayabilityFilter.collectAsState()
    val selectedStorageFilter by viewModel.selectedStorageFilter.collectAsState()
    val rarestAchievements by viewModel.rarestAchievements.collectAsState()
    val closestToHundredGames by viewModel.closestToHundredGames.collectAsState()
    val achievementOverviewStats by viewModel.achievementOverviewStats.collectAsState()
    val selectedGameForAchievements by viewModel.selectedGameForAchievements.collectAsState()
    val selectedGameAchievements by viewModel.selectedGameAchievements.collectAsState()
    val isLoadingGameAchievements by viewModel.isLoadingGameAchievements.collectAsState()
    val recentPlayedSteamGames by viewModel.recentPlayedSteamGames.collectAsState()
    val top3FreeGamesToday by viewModel.top3FreeGamesToday.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val inAppBrowserUrl by viewModel.inAppBrowserUrl.collectAsState()
    val inAppBrowserTitle by viewModel.inAppBrowserTitle.collectAsState()
    val publicReviews by viewModel.publicReviews.collectAsState()
    val steamDlcRecommendations by viewModel.steamDlcRecommendations.collectAsState()
    val isLoadingSteamDlcs by viewModel.isLoadingSteamDlcs.collectAsState()
    val steamDlcScanStatus by viewModel.steamDlcScanStatus.collectAsState()
    val selectedGameForDlcs by viewModel.selectedGameForDlcs.collectAsState()
    val selectedGameDlcs by viewModel.selectedGameDlcs.collectAsState()
    val isLoadingGameDlcs by viewModel.isLoadingGameDlcs.collectAsState()
    val isBacklogChallengeDialogVisibleFromVm by viewModel.isBacklogChallengeDialogVisible.collectAsState()
    val backlogChallengeGrid by viewModel.backlogChallengeGrid.collectAsState()

    val reducedMotion = !isAnimationsEnabled || isLowEndOptimization
    val borderBeamAngle = rememberAnimatedBeamAngle(enabled = !reducedMotion)
    var isSearchExpanded by remember { mutableStateOf(false) }
    var isNotificationSettingsSheetOpen by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var launcherChooserItem by remember { mutableStateOf<GiveawayItem?>(null) }
    var showSteamDialog by remember { mutableStateOf(false) }
    var showBacklogChallengeDialog by remember { mutableStateOf(false) }
    var showDlcHubDialog by remember { mutableStateOf(false) }

    val tabs = remember { listOf(MainTab.HOME, MainTab.DISCOVER, MainTab.CALENDAR, MainTab.CLAIMED, MainTab.SETTINGS) }
    val pagerState = rememberPagerState(initialPage = tabs.indexOf(selectedTab).coerceAtLeast(0), pageCount = { tabs.size })

    LaunchedEffect(pagerState.currentPage) {
        val tab = tabs[pagerState.currentPage]
        if (tab != selectedTab) viewModel.selectTab(tab)
    }
    LaunchedEffect(selectedTab) {
        val targetIndex = tabs.indexOf(selectedTab)
        if (targetIndex >= 0 && targetIndex != pagerState.currentPage) {
            if (reducedMotion || abs(targetIndex - pagerState.currentPage) > 1) pagerState.scrollToPage(targetIndex)
            else pagerState.animateScrollToPage(targetIndex)
        }
    }
    LaunchedEffect(reopenStatusMessage) {
        if (reopenStatusMessage != null) {
            delay(3500L)
            viewModel.clearReopenStatusMessage()
        }
    }

    val homeListState = rememberLazyListState()
    val discoverListState = rememberLazyListState()
    val claimedListState = rememberLazyListState()
    val calendarListState = rememberLazyListState()
    val trophyListState = rememberLazyListState()
    val discoverGridState = rememberLazyGridState()
    val claimedGridState = rememberLazyGridState()
    val settingsScrollState = rememberScrollState()

    val collapsibleHeaderState = rememberCollapsibleHeaderState(reducedMotion = reducedMotion)
    val currentTab = tabs[pagerState.currentPage]
    val canCollapseCurrentTab = when (currentTab) {
        MainTab.HOME -> homeListState.canScrollForward || homeListState.canScrollBackward
        MainTab.DISCOVER -> if (screenDensityInfo.gridColumns > 1) discoverGridState.canScrollForward || discoverGridState.canScrollBackward
        else discoverListState.canScrollForward || discoverListState.canScrollBackward
        MainTab.CALENDAR -> calendarListState.canScrollForward || calendarListState.canScrollBackward
        MainTab.CLAIMED -> when {
            libraryViewMode == LibraryViewMode.ACHIEVEMENTS -> trophyListState.canScrollForward || trophyListState.canScrollBackward
            screenDensityInfo.gridColumns > 1 -> claimedGridState.canScrollForward || claimedGridState.canScrollBackward
            else -> claimedListState.canScrollForward || claimedListState.canScrollBackward
        }
        MainTab.SETTINGS -> settingsScrollState.maxValue > 0
    }

    LaunchedEffect(currentTab, isSearchExpanded) { collapsibleHeaderState.expandAnimated() }
    LaunchedEffect(canCollapseCurrentTab) { if (!canCollapseCurrentTab) collapsibleHeaderState.expandAnimated() }

    val collapsibleTopBarConnection = rememberCollapsibleHeaderNestedScrollConnection(
        state = collapsibleHeaderState,
        canCollapseProvider = { !isSearchExpanded && canCollapseCurrentTab }
    )

    BackHandler(enabled = selectedTab != MainTab.HOME) { viewModel.selectTab(MainTab.HOME) }

    val onClaimOrChoose: (GiveawayItem) -> Unit = { item ->
        if (item.hasMultipleLaunchers) launcherChooserItem = item
        else viewModel.claimAndLaunch(context, item)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, MaterialTheme.colorScheme.background.copy(alpha = 0.72f), MaterialTheme.colorScheme.background.copy(alpha = 0.96f))
                            )
                        )
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ExpressiveFloatingNavBar(
                        selectedTab = tabs[pagerState.currentPage],
                        onTabSelected = { viewModel.selectTab(it) },
                        claimedCount = claimedGiveaways.size,
                        expiringSoonCount = uiStats.expiringSoon,
                        isLowEndOptimization = isLowEndOptimization
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
                    .nestedScroll(collapsibleTopBarConnection)
            ) {
                HomeTopBarChrome(
                    currentTab = currentTab,
                    userProfile = userProfile,
                    reducedMotion = reducedMotion,
                    collapsibleHeaderState = collapsibleHeaderState,
                    isSearchExpanded = isSearchExpanded,
                    searchQuery = searchQuery,
                    activeFilterCount = activeFilterCount,
                    unreadNotificationCount = unreadNotificationCount,
                    isAnimationsEnabled = isAnimationsEnabled,
                    isLowEndOptimization = isLowEndOptimization,
                    reopenStatusMessage = reopenStatusMessage,
                    appUpdateInfo = appUpdateInfo,
                    onOpenProfile = { showProfileDialog = true },
                    onToggleSearch = {
                        if (currentTab == MainTab.SETTINGS || currentTab == MainTab.HOME) {
                            viewModel.selectTab(MainTab.DISCOVER)
                            isSearchExpanded = true
                        } else isSearchExpanded = !isSearchExpanded
                    },
                    onFilterClick = {
                        if (currentTab == MainTab.SETTINGS || currentTab == MainTab.HOME) viewModel.selectTab(MainTab.DISCOVER)
                        viewModel.setFilterSheetOpen(true)
                    },
                    onNotificationClick = { viewModel.setNotificationHistorySheetOpen(true) },
                    onOpenUpdateDialog = { viewModel.setUpdateDialogVisible(true) },
                    onQuickUpdateClick = { info ->
                        viewModel.startInAppUpdateDownload(info, context)
                        viewModel.setUpdateDialogVisible(true)
                    },
                    onDismissReopenBanner = { viewModel.clearReopenStatusMessage() }
                )

                AnimatedVisibility(
                    visible = isSearchExpanded && currentTab != MainTab.SETTINGS,
                    enter = ExpressiveMotion.expressiveExpandEnter(reducedMotion),
                    exit = ExpressiveMotion.expressiveCollapseExit(reducedMotion)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text(s.common.searchDealsPlaceholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("search_text_input"),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                        Icon(Icons.Default.Close, contentDescription = s.common.clearSearchCd, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = if (isLowEndOptimization) 0 else 1,
                    modifier = Modifier.fillMaxSize().weight(1f).testTag("main_horizontal_pager")
                ) { page ->
                    val pageModifier = if (!reducedMotion) {
                        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue.coerceIn(0f, 1f)
                        if (pageOffset > 0.001f) {
                            Modifier.fillMaxSize().graphicsLayer {
                                val pageScale = lerp(1f, 0.95f, pageOffset)
                                scaleX = pageScale; scaleY = pageScale
                                alpha = lerp(1f, 0.72f, pageOffset)
                                translationY = lerp(0f, 14f, pageOffset)
                            }
                        } else {
                            Modifier.fillMaxSize()
                        }
                    } else {
                        Modifier.fillMaxSize()
                    }
                    Box(modifier = pageModifier) {
                        when (tabs[page]) {
                            MainTab.HOME -> HomeTabContent(
                                recentSteamGames = recentPlayedSteamGames,
                                top3FreeGames = top3FreeGamesToday,
                                uiStats = uiStats,
                                steamUserId = steamUserId,
                                isLoading = isLoading,
                                isSyncingSteam = isSyncingSteam,
                                isAnimationsEnabled = isAnimationsEnabled,
                                isLowEndOptimization = isLowEndOptimization,
                                screenDensityInfo = screenDensityInfo,
                                listState = homeListState,
                                onRefresh = { viewModel.refreshGiveaways() },
                                onItemClick = { viewModel.openDetail(it) },
                                onClaimClick = onClaimOrChoose,
                                onOpenSteamDialog = { showSteamDialog = true },
                                onQuickSteamSync = { viewModel.syncSteamLibrary() },
                                onOpenAchievements = { viewModel.openGameAchievements(it) },
                                onExploreAllFreebies = { viewModel.selectTab(MainTab.DISCOVER) },
                                onViewVault = { viewModel.selectTab(MainTab.CLAIMED) },
                                publicReviews = publicReviews,
                                defaultAuthorName = userProfile.name,
                                onPostAppReview = { rating, title, comment ->
                                    viewModel.postAppReview(rating, title, comment) { _, msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
                                },
                                onVoteHelpful = { viewModel.voteReviewHelpful(it) },
                                hasVotedHelpful = { viewModel.hasVotedHelpful(it) },
                                dlcRecommendations = steamDlcRecommendations,
                                hasSteamGames = uiStats.steamTotalCount > 0 || recentPlayedSteamGames.isNotEmpty(),
                                isScanningDlcs = isLoadingSteamDlcs,
                                dlcScanStatus = steamDlcScanStatus,
                                onRefreshDlcs = { viewModel.refreshSteamDlcRecommendations(forceRefresh = true) },
                                onOpenAllDlcs = { viewModel.closeGameDlcExplorer(); showDlcHubDialog = true },
                                onSelectDlc = { viewModel.openDlcRecommendation(context, it) },
                                onDismissDlc = { viewModel.dismissDlcRecommendation(it) }
                            )
                            MainTab.DISCOVER -> GiveawaysTabContent(
                                tab = MainTab.DISCOVER, giveaways = discoverGiveaways, isLoading = isLoading, errorMessage = errorMessage,
                                searchQuery = searchQuery, uiStats = uiStats, isNewTodayDismissed = isNewTodayDismissed,
                                activeFilterCount = activeFilterCount, selectedLaunchers = selectedLaunchers, selectedTypeFilter = selectedTypeFilter,
                                selectedCategories = selectedCategories, selectedSort = selectedSort, cardDensity = cardDensity,
                                isShowOriginalPrice = isShowOriginalPrice, isShowExpiration = isShowExpiration,
                                isAnimationsEnabled = isAnimationsEnabled, isLowEndOptimization = isLowEndOptimization, beamAngle = borderBeamAngle,
                                screenDensityInfo = screenDensityInfo, listState = discoverListState, gridState = discoverGridState,
                                onRefresh = { viewModel.refreshGiveaways() }, onDismissNewToday = { viewModel.dismissNewToday() },
                                onNewTodayClick = { viewModel.selectSort(SortOption.NEWEST) }, onOpenFilterSheet = { viewModel.setFilterSheetOpen(true) },
                                onResetFilters = { viewModel.resetFilters() }, onItemClick = { viewModel.openDetail(it) },
                                onClaimClick = onClaimOrChoose, onToggleBookmark = { viewModel.toggleBookmark(it) },
                                onToggleClaimed = { viewModel.toggleClaimed(it) }, selectedPlayabilityFilter = selectedPlayabilityFilter,
                                onPlayabilityFilterSelect = { viewModel.setPlayabilityFilter(it) }, selectedStorageFilter = selectedStorageFilter,
                                onStorageFilterSelect = { viewModel.setStorageFilter(it) }
                            )
                            MainTab.CLAIMED -> GiveawaysTabContent(
                                tab = MainTab.CLAIMED, giveaways = claimedGiveaways, isLoading = isLoading, errorMessage = errorMessage,
                                searchQuery = searchQuery, uiStats = uiStats, isNewTodayDismissed = isNewTodayDismissed,
                                activeFilterCount = activeFilterCount, selectedLaunchers = selectedLaunchers, selectedTypeFilter = selectedTypeFilter,
                                selectedCategories = selectedCategories, selectedSort = selectedSort, cardDensity = cardDensity,
                                isShowOriginalPrice = isShowOriginalPrice, isShowExpiration = isShowExpiration,
                                isAnimationsEnabled = isAnimationsEnabled, isLowEndOptimization = isLowEndOptimization, beamAngle = borderBeamAngle,
                                screenDensityInfo = screenDensityInfo, listState = claimedListState, gridState = claimedGridState,
                                selectedLibraryPlayFilter = selectedLibraryPlayFilter, isSyncingSteam = isSyncingSteam,
                                steamUserId = steamUserId, steamSyncStatus = steamSyncStatus,
                                onFilterSelect = { viewModel.setLibraryPlayFilter(it) }, onOpenSteamDialog = { showSteamDialog = true },
                                onQuickSteamSync = { viewModel.syncSteamLibrary() }, onDismissSteamStatus = { viewModel.clearSteamSyncStatus() },
                                onRefresh = { viewModel.refreshGiveaways() }, onDismissNewToday = { viewModel.dismissNewToday() },
                                onNewTodayClick = { viewModel.selectSort(SortOption.NEWEST) }, onOpenFilterSheet = { viewModel.setFilterSheetOpen(true) },
                                onResetFilters = { viewModel.resetFilters() }, onItemClick = { viewModel.openDetail(it) },
                                onClaimClick = onClaimOrChoose, onToggleBookmark = { viewModel.toggleBookmark(it) },
                                onToggleClaimed = { viewModel.toggleClaimed(it) },
                                onPlayStatusChange = { item, status -> viewModel.updatePlayStatus(item, status) },
                                libraryViewMode = libraryViewMode, steamGames = filteredSteamGames,
                                onViewModeChange = { viewModel.setLibraryViewMode(it) },
                                onSteamPlayStatusChange = { game, status -> viewModel.updateSteamGamePlayStatus(game, status) },
                                selectedPlayabilityFilter = selectedPlayabilityFilter, onPlayabilityFilterSelect = { viewModel.setPlayabilityFilter(it) },
                                selectedStorageFilter = selectedStorageFilter, onStorageFilterSelect = { viewModel.setStorageFilter(it) },
                                onSteamManualStorageChange = { game, sizeGb -> viewModel.setSteamGameManualStorage(game.appId, sizeGb) },
                                achievementOverviewStats = achievementOverviewStats, rarestAchievements = rarestAchievements,
                                closestToHundredGames = closestToHundredGames, onOpenAchievements = { viewModel.openGameAchievements(it) },
                                onOpenBacklogChallenge = { showBacklogChallengeDialog = true }, dlcRecommendationsCount = steamDlcRecommendations.size,
                                onOpenDlcHub = { viewModel.closeGameDlcExplorer(); showDlcHubDialog = true },
                                onOpenGameDlcs = { viewModel.openGameDlcExplorer(it) }, trophyListState = trophyListState
                            )
                            MainTab.CALENDAR -> CalendarTabContent(
                                giveaways = discoverGiveaways, isLoading = isLoading, errorMessage = errorMessage, searchQuery = searchQuery,
                                cardDensity = cardDensity, isShowOriginalPrice = isShowOriginalPrice, isShowExpiration = isShowExpiration,
                                isAnimationsEnabled = isAnimationsEnabled, isLowEndOptimization = isLowEndOptimization, beamAngle = borderBeamAngle,
                                screenDensityInfo = screenDensityInfo, listState = calendarListState, onRefresh = { viewModel.refreshGiveaways() },
                                onItemClick = { viewModel.openDetail(it) }, onClaimClick = onClaimOrChoose,
                                onToggleBookmark = { viewModel.toggleBookmark(it) }, onToggleClaimed = { viewModel.toggleClaimed(it) }
                            )
                            MainTab.SETTINGS -> SettingsScreen(viewModel = viewModel, scrollState = settingsScrollState, modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }

        HomeDialogsOverlay(
            viewModel = viewModel, context = context, appLanguage = appLanguage, hasSelectedInitialLanguage = hasSelectedInitialLanguage,
            selectedLaunchers = selectedLaunchers, selectedTypeFilter = selectedTypeFilter,
            selectedCategories = selectedCategories, selectedSort = selectedSort, activeFilterCount = activeFilterCount,
            uiStats = uiStats, displayedGiveaways = displayedGiveaways, discoverGiveaways = discoverGiveaways,
            filteredSteamGames = filteredSteamGames, selectedPlayabilityFilter = selectedPlayabilityFilter,
            selectedStorageFilter = selectedStorageFilter, isFilterSheetOpen = isFilterSheetOpen, isCategorySheetOpen = isCategorySheetOpen,
            detailItem = detailItem, launcherChooserItem = launcherChooserItem,
            onClearLauncherChooser = { launcherChooserItem = null }, onOpenLauncherChooser = { launcherChooserItem = it },
            showSteamDialog = showSteamDialog, onDismissSteamDialog = { showSteamDialog = false }, onOpenSteamDialog = { showSteamDialog = true },
            steamUserId = steamUserId, steamApiKey = steamApiKey, isSyncingSteam = isSyncingSteam, steamSyncStatus = steamSyncStatus,
            inAppBrowserUrl = inAppBrowserUrl, inAppBrowserTitle = inAppBrowserTitle, showOnboardingWalkthrough = showOnboardingWalkthrough,
            showProfileDialog = showProfileDialog, onDismissProfileDialog = { showProfileDialog = false }, userProfile = userProfile,
            rarestAchievements = rarestAchievements, publicReviews = publicReviews,
            showBacklogChallengeDialog = showBacklogChallengeDialog, isBacklogChallengeDialogVisibleFromVm = isBacklogChallengeDialogVisibleFromVm,
            backlogChallengeGrid = backlogChallengeGrid, onDismissBacklogDialog = { showBacklogChallengeDialog = false },
            isNotificationHistorySheetOpen = isNotificationHistorySheetOpen,
            isNotificationSettingsSheetOpen = isNotificationSettingsSheetOpen,
            onSetNotificationSettingsOpen = { isNotificationSettingsSheetOpen = it },
            isUpdateDialogVisible = isUpdateDialogVisible, appUpdateInfo = appUpdateInfo, updateDownloadState = updateDownloadState,
            selectedGameForAchievements = selectedGameForAchievements, selectedGameAchievements = selectedGameAchievements,
            isLoadingGameAchievements = isLoadingGameAchievements, showDlcHubDialog = showDlcHubDialog,
            onDismissDlcHubDialog = { showDlcHubDialog = false }, steamDlcRecommendations = steamDlcRecommendations,
            selectedGameForDlcs = selectedGameForDlcs, selectedGameDlcs = selectedGameDlcs, isLoadingSteamDlcs = isLoadingSteamDlcs,
            isLoadingGameDlcs = isLoadingGameDlcs, steamDlcScanStatus = steamDlcScanStatus
        )
    }
}
