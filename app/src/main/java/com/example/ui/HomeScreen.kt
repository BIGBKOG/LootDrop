package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.offset
import com.example.ui.components.OnboardingWalkthroughDialog
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.ui.density.ScreenDensityInfo
import com.example.ui.density.LocalScreenDensityInfo
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.GiveawayItem
import com.example.data.model.LauncherType
import com.example.preferences.CardDensity
import com.example.ui.calendar.CalendarTabContent
import com.example.ui.components.ChooseLauncherDialog
import com.example.ui.components.ExpressiveFloatingNavBar
import com.example.ui.components.FilterSortBottomSheet
import com.example.ui.components.GiveawayCard
import com.example.ui.components.GiveawayDetailDialog
import com.example.ui.components.StatsBanner
import com.example.ui.components.NotificationHistorySheet
import com.example.ui.components.NotificationSettingsSheet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: GiveawaysViewModel
) {
    val context = LocalContext.current
    val screenDensityInfo = LocalScreenDensityInfo.current
    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedLaunchers by viewModel.selectedLaunchers.collectAsState()
    val selectedLauncher by viewModel.selectedLauncher.collectAsState()
    val selectedTypeFilter by viewModel.selectedTypeFilter.collectAsState()
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
    val activeFilterCount by viewModel.activeFilterCount.collectAsState()
    val reopenStatusMessage by viewModel.reopenStatusMessage.collectAsState()
    val showOnboardingWalkthrough by viewModel.showOnboardingWalkthrough.collectAsState()
    val isAnimationsEnabled by viewModel.isAnimationsEnabled.collectAsState()
    val isLowEndOptimization by viewModel.isLowEndOptimization.collectAsState()
    val isNewTodayDismissed by viewModel.isNewTodayDismissed.collectAsState()
    val unreadNotificationCount by viewModel.unreadNotificationCount.collectAsState()
    val isNotificationHistorySheetOpen by viewModel.isNotificationHistorySheetOpen.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val borderBeamTransition = rememberInfiniteTransition(label = "borderBeamTransition")
    val borderBeamAngle by if (isAnimationsEnabled && !isLowEndOptimization) {
        borderBeamTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "borderBeamAngle"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    var isSearchExpanded by remember { mutableStateOf(false) }
    var isNotificationSettingsSheetOpen by remember { mutableStateOf(false) }
    var launcherChooserItem by remember { mutableStateOf<GiveawayItem?>(null) }

    val tabs = remember { listOf(MainTab.DISCOVER, MainTab.CALENDAR, MainTab.CLAIMED, MainTab.SETTINGS) }
    val pagerState = rememberPagerState(
        initialPage = tabs.indexOf(selectedTab).coerceAtLeast(0),
        pageCount = { tabs.size }
    )
    val coroutineScope = rememberCoroutineScope()

    // Sync pager page changes (when user swipes) to ViewModel's selectedTab
    LaunchedEffect(pagerState.currentPage) {
        val tab = tabs[pagerState.currentPage]
        if (tab != selectedTab) {
            viewModel.selectTab(tab)
        }
    }

    // Sync ViewModel's selectedTab (e.g. from bottom bar taps, back button) to pager
    LaunchedEffect(selectedTab) {
        val targetIndex = tabs.indexOf(selectedTab)
        if (targetIndex >= 0 && targetIndex != pagerState.currentPage) {
            if (kotlin.math.abs(targetIndex - pagerState.currentPage) > 1) {
                pagerState.scrollToPage(targetIndex)
            } else {
                pagerState.animateScrollToPage(targetIndex)
            }
        }
    }

    LaunchedEffect(reopenStatusMessage) {
        if (reopenStatusMessage != null) {
            delay(3500L)
            viewModel.clearReopenStatusMessage()
        }
    }

    val discoverListState = rememberLazyListState()
    val claimedListState = rememberLazyListState()
    val calendarListState = rememberLazyListState()
    val discoverGridState = rememberLazyGridState()
    val claimedGridState = rememberLazyGridState()

    BackHandler(enabled = selectedTab != MainTab.DISCOVER) {
        viewModel.selectTab(MainTab.DISCOVER)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                val currentTab = tabs[pagerState.currentPage]
                ExpressiveFloatingNavBar(
                    selectedTab = currentTab,
                    onTabSelected = { tab ->
                        viewModel.selectTab(tab)
                    },
                    claimedCount = claimedGiveaways.size,
                    expiringSoonCount = uiStats.expiringSoon
                )
            }
        }
    ) { innerPadding ->
        val currentTab = tabs[pagerState.currentPage]
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
                    // Top App Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "LootDrop Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Loot",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Drop",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                        .padding(horizontal = 5.dp, vertical = 1.5.dp)
                                ) {
                                    Text(
                                        text = when (currentTab) {
                                            MainTab.SETTINGS -> "SETTINGS"
                                            MainTab.CALENDAR -> "CALENDAR"
                                            else -> "${screenDensityInfo.densityDpi} DPI"
                                        },
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Text(
                                text = when (currentTab) {
                                    MainTab.SETTINGS -> "APP PREFERENCES & CONTROLS"
                                    MainTab.CALENDAR -> "EXPIRATION RADAR & TIMELINE"
                                    MainTab.CLAIMED -> "MY CLAIMED GAME VAULT"
                                    MainTab.DISCOVER -> "PC & CONSOLE FREE RADAR"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentTab == MainTab.SETTINGS || currentTab == MainTab.CALENDAR) MaterialTheme.colorScheme.primary else NeonGreen,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    if (currentTab != MainTab.SETTINGS) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Search Button
                            IconButton(
                                onClick = { isSearchExpanded = !isSearchExpanded },
                                modifier = Modifier.testTag("toggle_search_button")
                            ) {
                                Icon(
                                    imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = "Search Games",
                                    tint = if (isSearchExpanded) MaterialTheme.colorScheme.primary else TextPrimary
                                )
                            }

                            // Consolidated Material 3 Filter Menu Button (3 lines / tune icon)
                            IconButton(
                                onClick = { viewModel.setFilterSheetOpen(true) },
                                modifier = Modifier.testTag("filter_menu_button")
                            ) {
                                Box {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Filter & Sort Menu",
                                        tint = if (activeFilterCount > 0) MaterialTheme.colorScheme.primary else TextPrimary
                                    )
                                    if (activeFilterCount > 0) {
                                        val badgeScale by animateFloatAsState(
                                            targetValue = 1f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMediumLow
                                            ),
                                            label = "filter_badge_scale"
                                        )
                                        Box(
                                            modifier = Modifier
                                                .graphicsLayer {
                                                    scaleX = badgeScale
                                                    scaleY = badgeScale
                                                }
                                                .size(9.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary)
                                                .align(Alignment.TopEnd)
                                        )
                                    }
                                }
                            }

                            // Notification Alert Inbox Button with playful wiggle when unread alerts arrive
                            val bellInfinite = rememberInfiniteTransition(label = "bellWiggleTransition")
                            val bellRotation by bellInfinite.animateFloat(
                                initialValue = if (unreadNotificationCount > 0) -9f else 0f,
                                targetValue = if (unreadNotificationCount > 0) 9f else 0f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(380, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "bellRotation"
                            )
                            val dotPulseScale by bellInfinite.animateFloat(
                                initialValue = 0.85f,
                                targetValue = 1.25f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(750, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "dotPulseScale"
                            )

                            IconButton(
                                onClick = { viewModel.setNotificationHistorySheetOpen(true) },
                                modifier = Modifier.testTag("notification_inbox_button")
                            ) {
                                Box {
                                    Icon(
                                        imageVector = if (unreadNotificationCount > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                        contentDescription = "Alert Inbox & History",
                                        tint = if (unreadNotificationCount > 0) NeonCyan else TextPrimary,
                                        modifier = Modifier.graphicsLayer {
                                            rotationZ = bellRotation
                                        }
                                    )
                                    if (unreadNotificationCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .graphicsLayer {
                                                    scaleX = dotPulseScale
                                                    scaleY = dotPulseScale
                                                }
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(NeonGreen)
                                                .align(Alignment.TopEnd)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Expandable Search Bar with fluid spring animation
                AnimatedVisibility(
                    visible = isSearchExpanded && currentTab != MainTab.SETTINGS,
                    enter = fadeIn(tween(200)) + expandVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ),
                    exit = fadeOut(tween(160)) + shrinkVertically(
                        animationSpec = tween(180, easing = FastOutSlowInEasing)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Search by game name, store, genre...", color = TextMuted) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_text_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                                    }
                                }
                            }
                        )
                    }
                }

                // Reopen Auto-Check Notification Banner
                AnimatedVisibility(
                    visible = reopenStatusMessage != null && currentTab != MainTab.SETTINGS,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    reopenStatusMessage?.let { msg ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("reopen_status_banner"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = msg,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(
                                onClick = { viewModel.clearReopenStatusMessage() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }



            // HorizontalPager allowing fluid swiping right and left between all tabs
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .testTag("main_horizontal_pager")
            ) { page ->
                when (tabs[page]) {
                    MainTab.DISCOVER -> {
                        GiveawaysTabContent(
                            tab = MainTab.DISCOVER,
                            giveaways = discoverGiveaways,
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                            searchQuery = searchQuery,
                            uiStats = uiStats,
                            isNewTodayDismissed = isNewTodayDismissed,
                            activeFilterCount = activeFilterCount,
                            selectedLaunchers = selectedLaunchers,
                            selectedTypeFilter = selectedTypeFilter,
                            selectedSort = selectedSort,
                            cardDensity = cardDensity,
                            isShowOriginalPrice = isShowOriginalPrice,
                            isShowExpiration = isShowExpiration,
                            isAnimationsEnabled = isAnimationsEnabled,
                            isLowEndOptimization = isLowEndOptimization,
                            beamAngle = borderBeamAngle,
                            screenDensityInfo = screenDensityInfo,
                            listState = discoverListState,
                            gridState = discoverGridState,
                            onRefresh = { viewModel.refreshGiveaways() },
                            onDismissNewToday = { viewModel.dismissNewToday() },
                            onNewTodayClick = { viewModel.selectSort(SortOption.NEWEST) },
                            onOpenFilterSheet = { viewModel.setFilterSheetOpen(true) },
                            onResetFilters = { viewModel.resetFilters() },
                            onItemClick = { viewModel.openDetail(it) },
                            onClaimClick = { item ->
                                if (item.hasMultipleLaunchers) {
                                    launcherChooserItem = item
                                } else {
                                    viewModel.claimAndLaunch(context, item)
                                }
                            },
                            onToggleBookmark = { viewModel.toggleBookmark(it) },
                            onToggleClaimed = { viewModel.toggleClaimed(it) }
                        )
                    }
                    MainTab.CLAIMED -> {
                        GiveawaysTabContent(
                            tab = MainTab.CLAIMED,
                            giveaways = claimedGiveaways,
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                            searchQuery = searchQuery,
                            uiStats = uiStats,
                            isNewTodayDismissed = isNewTodayDismissed,
                            activeFilterCount = activeFilterCount,
                            selectedLaunchers = selectedLaunchers,
                            selectedTypeFilter = selectedTypeFilter,
                            selectedSort = selectedSort,
                            cardDensity = cardDensity,
                            isShowOriginalPrice = isShowOriginalPrice,
                            isShowExpiration = isShowExpiration,
                            isAnimationsEnabled = isAnimationsEnabled,
                            isLowEndOptimization = isLowEndOptimization,
                            beamAngle = borderBeamAngle,
                            screenDensityInfo = screenDensityInfo,
                            listState = claimedListState,
                            gridState = claimedGridState,
                            onRefresh = { viewModel.refreshGiveaways() },
                            onDismissNewToday = { viewModel.dismissNewToday() },
                            onNewTodayClick = { viewModel.selectSort(SortOption.NEWEST) },
                            onOpenFilterSheet = { viewModel.setFilterSheetOpen(true) },
                            onResetFilters = { viewModel.resetFilters() },
                            onItemClick = { viewModel.openDetail(it) },
                            onClaimClick = { item ->
                                if (item.hasMultipleLaunchers) {
                                    launcherChooserItem = item
                                } else {
                                    viewModel.claimAndLaunch(context, item)
                                }
                            },
                            onToggleBookmark = { viewModel.toggleBookmark(it) },
                            onToggleClaimed = { viewModel.toggleClaimed(it) }
                        )
                    }
                    MainTab.CALENDAR -> {
                        CalendarTabContent(
                            giveaways = discoverGiveaways,
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                            searchQuery = searchQuery,
                            cardDensity = cardDensity,
                            isShowOriginalPrice = isShowOriginalPrice,
                            isShowExpiration = isShowExpiration,
                            isAnimationsEnabled = isAnimationsEnabled,
                            isLowEndOptimization = isLowEndOptimization,
                            beamAngle = borderBeamAngle,
                            screenDensityInfo = screenDensityInfo,
                            listState = calendarListState,
                            onRefresh = { viewModel.refreshGiveaways() },
                            onItemClick = { viewModel.openDetail(it) },
                            onClaimClick = { item ->
                                if (item.hasMultipleLaunchers) {
                                    launcherChooserItem = item
                                } else {
                                    viewModel.claimAndLaunch(context, item)
                                }
                            },
                            onToggleBookmark = { viewModel.toggleBookmark(it) },
                            onToggleClaimed = { viewModel.toggleClaimed(it) }
                        )
                    }
                    MainTab.SETTINGS -> {
                        SettingsScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    // Material 3 Filter & Sort Bottom Sheet
    if (isFilterSheetOpen) {
        FilterSortBottomSheet(
            selectedLaunchers = selectedLaunchers,
            onLauncherToggle = { viewModel.toggleLauncher(it) },
            selectedTypeFilter = selectedTypeFilter,
            onTypeFilterSelected = { viewModel.selectTypeFilter(it) },
            selectedSort = selectedSort,
            onSortSelected = { viewModel.selectSort(it) },
            activeFilterCount = activeFilterCount,
            onResetFilters = { viewModel.resetFilters() },
            platformCounts = uiStats.platformCounts,
            totalFilteredCount = displayedGiveaways.size,
            onDismiss = { viewModel.setFilterSheetOpen(false) }
        )
    }

    // Detail Dialog
    detailItem?.let { item ->
        GiveawayDetailDialog(
            item = item,
            onDismiss = { viewModel.closeDetail() },
            onClaimClick = {
                if (item.hasMultipleLaunchers) {
                    launcherChooserItem = item
                } else {
                    viewModel.claimAndLaunch(context, item)
                }
            },
            onLauncherOptionClick = { url, launcherName ->
                viewModel.claimAndLaunch(context, item, targetUrl = url, launcherName = launcherName)
            },
            onToggleBookmark = { viewModel.toggleBookmark(item) },
            onToggleClaimed = { viewModel.toggleClaimed(item) }
        )
    }

    // Choose Launcher Popup Menu (when a game has multiple launchers at 100% off)
    launcherChooserItem?.let { item ->
        ChooseLauncherDialog(
            item = item,
            launchers = viewModel.getLauncherOptionsFor(item),
            onDismiss = { launcherChooserItem = null },
            onLauncherClaimed = { targetUrl, launcherName ->
                viewModel.claimAndLaunch(context, item, targetUrl = targetUrl, launcherName = launcherName)
                launcherChooserItem = null
            }
        )
    }

    // Onboarding Feature Tour Walkthrough (first launch animation or replayed from settings)
    if (showOnboardingWalkthrough) {
        OnboardingWalkthroughDialog(
            onDismiss = { viewModel.dismissOnboarding() },
            onFinish = { viewModel.completeOnboarding() }
        )
    }

    // Notification History Inbox Sheet
    if (isNotificationHistorySheetOpen) {
        NotificationHistorySheet(
            viewModel = viewModel,
            onDismiss = { viewModel.setNotificationHistorySheetOpen(false) },
            onOpenSettings = {
                viewModel.setNotificationHistorySheetOpen(false)
                isNotificationSettingsSheetOpen = true
            }
        )
    }

    // Notification Settings Sheet
    if (isNotificationSettingsSheetOpen) {
        NotificationSettingsSheet(
            notificationHelper = viewModel.notificationHelper,
            onDismiss = { isNotificationSettingsSheetOpen = false },
            onOpenInbox = {
                isNotificationSettingsSheetOpen = false
                viewModel.setNotificationHistorySheetOpen(true)
            }
        )
    }
}

@Composable
fun ClaimedSavingsBanner(
    savingsUsd: Double,
    claimedCount: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "claimedBannerAnim")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "claimedBorderAlpha"
    )
    val medalBob by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "medalBob"
    )
    val medalRot by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "medalRot"
    )

    val animatedSavings by animateFloatAsState(
        targetValue = savingsUsd.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "animatedSavings"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF0B2816),
                        Color(0xFF113820),
                        Color(0xFF0D2218)
                    )
                )
            )
            .border(1.dp, NeonGreen.copy(alpha = borderAlpha), RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("claimed_savings_banner")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonGreen.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Text(
                        text = "TOTAL MONEY SAVED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val formattedVal = String.format(Locale.US, "$%.2f", animatedSavings)
                    Text(
                        text = formattedVal,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "SAVED",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                val avgSaved = if (claimedCount > 0) animatedSavings.toDouble() / claimedCount else 0.0
                Text(
                    text = if (claimedCount > 0) {
                        "$claimedCount claimed games • avg ${String.format(Locale.US, "$%.2f", avgSaved)} / game"
                    } else {
                        "Claim games to track your savings backlog"
                    },
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationY = medalBob
                        rotationZ = medalRot
                    }
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(NeonGreen.copy(alpha = 0.15f))
                    .border(1.dp, NeonGreen.copy(alpha = borderAlpha), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MilitaryTech,
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

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
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Stats Banner on Discover
        if (tab == MainTab.DISCOVER) {
            StatsBanner(
                stats = uiStats,
                isRefreshing = isLoading,
                onRefresh = onRefresh,
                isNewTodayDismissed = isNewTodayDismissed,
                onDismissNewToday = onDismissNewToday,
                onNewTodayClick = onNewTodayClick,
                isAnimationsEnabled = isAnimationsEnabled && !isLowEndOptimization
            )
        }

        // Total Money Saved Banner on In Library
        if (tab == MainTab.CLAIMED) {
            ClaimedSavingsBanner(
                savingsUsd = uiStats.claimedTotalSavedUsd,
                claimedCount = uiStats.claimedCount
            )
        }

        // Active Filters Summary Pill (if any non-defaults are selected)
        if (activeFilterCount > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .clickable { onOpenFilterSheet() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = buildString {
                            if (selectedLaunchers.isNotEmpty() && !selectedLaunchers.contains(LauncherType.ALL)) {
                                append(selectedLaunchers.joinToString(", ") { it.displayName })
                            }
                            if (selectedTypeFilter != TypeFilter.ALL && selectedTypeFilter.apiMatch != null) {
                                if (isNotEmpty()) append(" • ")
                                append(selectedTypeFilter.label)
                            }
                            if (selectedSort != SortOption.NEWEST) {
                                if (isNotEmpty()) append(" • ")
                                append(selectedSort.label)
                            }
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "Reset",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onResetFilters() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Main List with Pull-to-Refresh
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .testTag("pull_to_refresh_box")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // If there is an error message and we have cached giveaways, show an alert banner
                if (errorMessage != null && giveaways.isNotEmpty()) {
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = errorMessage,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                maxLines = 2
                            )
                        }
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                if (isLoading && giveaways.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Scanning PC & Console for Free Loot...",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                } else if (giveaways.isEmpty()) {
                    val emptyIconBob by rememberInfiniteTransition(label = "emptyBob").animateFloat(
                        initialValue = -4f,
                        targetValue = 4f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1600, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "emptyIconBob"
                    )

                    val isFiltered = activeFilterCount > 0 || searchQuery.isNotBlank()
                    val hasError = errorMessage != null

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    translationY = emptyIconBob
                                }
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when {
                                    tab == MainTab.CLAIMED -> Icons.Default.CheckCircle
                                    hasError -> Icons.Default.CloudOff
                                    isFiltered -> Icons.Default.FilterAltOff
                                    else -> Icons.Default.SportsEsports
                                },
                                contentDescription = null,
                                tint = if (hasError) MaterialTheme.colorScheme.error else TextMuted,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = when {
                                tab == MainTab.CLAIMED -> "No Claimed Games Yet"
                                hasError -> "Unable to Fetch Giveaways"
                                isFiltered -> "No Matches Found"
                                else -> "No Giveaways Found"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = when {
                                tab == MainTab.CLAIMED -> "Tap 'Claim Free' on any game to automatically add it to your library and track total dollars saved!"
                                hasError -> errorMessage ?: "Network issue or server unavailable. Tap retry to check again."
                                isFiltered -> if (searchQuery.isNotBlank()) "No games match \"${searchQuery.take(20)}\" or your active launcher filters." else "No free games match your active filters in the top menu."
                                else -> "Currently no active giveaways reported. Pull down to refresh or check back soon!"
                            },
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (hasError) {
                            Button(
                                onClick = onRefresh,
                                modifier = Modifier.testTag("retry_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Retry")
                            }
                        } else if (isFiltered && tab == MainTab.DISCOVER) {
                            OutlinedButton(
                                onClick = onResetFilters,
                                modifier = Modifier.testTag("reset_filters_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterAltOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Reset All Filters")
                            }
                        } else if (tab == MainTab.DISCOVER) {
                            Button(
                                onClick = onRefresh,
                                modifier = Modifier.testTag("refresh_giveaways_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Refresh Feed")
                            }
                        }
                    }
                } else {
                    if (screenDensityInfo.gridColumns > 1) {
                        LazyVerticalGrid(
                            state = gridState,
                            columns = GridCells.Fixed(screenDensityInfo.gridColumns),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("giveaways_list"),
                            contentPadding = PaddingValues(
                                start = screenDensityInfo.adaptiveHorizontalPadding,
                                end = screenDensityInfo.adaptiveHorizontalPadding,
                                top = 6.dp,
                                bottom = 24.dp
                            ),
                            horizontalArrangement = Arrangement.spacedBy(screenDensityInfo.adaptiveCardSpacing),
                            verticalArrangement = Arrangement.spacedBy(screenDensityInfo.adaptiveCardSpacing)
                        ) {
                            items(giveaways, key = { it.id }, contentType = { cardDensity.name }) { item ->
                                GiveawayCard(
                                    item = item,
                                    cardDensity = cardDensity,
                                    showOriginalPrice = isShowOriginalPrice,
                                    showExpiration = isShowExpiration,
                                    onClick = { onItemClick(item) },
                                    onClaimClick = { onClaimClick(item) },
                                    onToggleBookmark = { onToggleBookmark(item) },
                                    onToggleClaimed = { onToggleClaimed(item) },
                                    isAnimationsEnabled = isAnimationsEnabled,
                                    isLowEndOptimization = isLowEndOptimization,
                                    beamAngle = beamAngle,
                                    modifier = if (isAnimationsEnabled && !isLowEndOptimization) Modifier.animateItem() else Modifier
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("giveaways_list"),
                            contentPadding = PaddingValues(
                                start = screenDensityInfo.adaptiveHorizontalPadding,
                                end = screenDensityInfo.adaptiveHorizontalPadding,
                                top = 6.dp,
                                bottom = 24.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(
                                when (cardDensity) {
                                    CardDensity.RICH_CARDS -> screenDensityInfo.adaptiveCardSpacing
                                    CardDensity.COMPACT_CARDS -> (screenDensityInfo.adaptiveCardSpacing.value * 0.75f).dp
                                    CardDensity.MINIMAL_LIST -> 6.dp
                                }
                            )
                        ) {
                            items(giveaways, key = { it.id }, contentType = { cardDensity.name }) { item ->
                                GiveawayCard(
                                    item = item,
                                    cardDensity = cardDensity,
                                    showOriginalPrice = isShowOriginalPrice,
                                    showExpiration = isShowExpiration,
                                    onClick = { onItemClick(item) },
                                    onClaimClick = { onClaimClick(item) },
                                    onToggleBookmark = { onToggleBookmark(item) },
                                    onToggleClaimed = { onToggleClaimed(item) },
                                    isAnimationsEnabled = isAnimationsEnabled,
                                    isLowEndOptimization = isLowEndOptimization,
                                    beamAngle = beamAngle,
                                    modifier = if (isAnimationsEnabled && !isLowEndOptimization) Modifier.animateItem() else Modifier
                                )
                            }
                        }
                    }
            }
        }
    }
}
}

