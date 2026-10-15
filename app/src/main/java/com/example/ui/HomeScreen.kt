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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import com.example.ui.components.ChooseLauncherDialog
import com.example.ui.components.FilterSortBottomSheet
import com.example.ui.components.GiveawayCard
import com.example.ui.components.GiveawayDetailDialog
import com.example.ui.components.StatsBanner
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
    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedLaunchers by viewModel.selectedLaunchers.collectAsState()
    val selectedLauncher by viewModel.selectedLauncher.collectAsState()
    val selectedTypeFilter by viewModel.selectedTypeFilter.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val displayedGiveaways by viewModel.displayedGiveaways.collectAsState()
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

    var isSearchExpanded by remember { mutableStateOf(false) }
    var launcherChooserItem by remember { mutableStateOf<GiveawayItem?>(null) }

    LaunchedEffect(reopenStatusMessage) {
        if (reopenStatusMessage != null) {
            delay(3500L)
            viewModel.clearReopenStatusMessage()
        }
    }

    BackHandler(enabled = selectedTab != MainTab.DISCOVER) {
        viewModel.selectTab(MainTab.DISCOVER)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val discoverScale by animateFloatAsState(
                    targetValue = if (selectedTab == MainTab.DISCOVER) 1.15f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
                    label = "nav_discover_scale"
                )
                val claimedScale by animateFloatAsState(
                    targetValue = if (selectedTab == MainTab.CLAIMED) 1.15f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
                    label = "nav_claimed_scale"
                )
                val settingsScale by animateFloatAsState(
                    targetValue = if (selectedTab == MainTab.SETTINGS) 1.15f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
                    label = "nav_settings_scale"
                )

                NavigationBarItem(
                    selected = selectedTab == MainTab.DISCOVER,
                    onClick = { viewModel.selectTab(MainTab.DISCOVER) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = "Discover",
                            modifier = Modifier.graphicsLayer {
                                scaleX = discoverScale
                                scaleY = discoverScale
                            }
                        )
                    },
                    label = { Text("Discover") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.background,
                        indicatorColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_discover")
                )

                NavigationBarItem(
                    selected = selectedTab == MainTab.CLAIMED,
                    onClick = { viewModel.selectTab(MainTab.CLAIMED) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "In Library",
                            modifier = Modifier.graphicsLayer {
                                scaleX = claimedScale
                                scaleY = claimedScale
                            }
                        )
                    },
                    label = { Text("In Library") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.background,
                        indicatorColor = NeonGreen,
                        selectedTextColor = NeonGreen,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_claimed")
                )

                NavigationBarItem(
                    selected = selectedTab == MainTab.SETTINGS,
                    onClick = { viewModel.selectTab(MainTab.SETTINGS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.graphicsLayer {
                                scaleX = settingsScale
                                scaleY = settingsScale
                            }
                        )
                    },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.background,
                        indicatorColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                if (!isAnimationsEnabled) {
                    EnterTransition.None togetherWith ExitTransition.None
                } else {
                    val isForward = targetState.ordinal > initialState.ordinal
                    (slideInHorizontally(
                        initialOffsetX = { if (isForward) (it * 0.28f).toInt() else (-it * 0.28f).toInt() },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeIn(
                        animationSpec = tween(220, easing = FastOutSlowInEasing)
                    ) + scaleIn(
                        initialScale = 0.95f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )).togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { if (isForward) (-it * 0.28f).toInt() else (it * 0.28f).toInt() },
                            animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
                        ) + fadeOut(
                            animationSpec = tween(160)
                        ) + scaleOut(
                            targetScale = 0.96f,
                            animationSpec = tween(durationMillis = 180)
                        )
                    )
                }
            },
            label = "tab_animated_content",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { currentTab ->
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                if (currentTab == MainTab.SETTINGS) {
                    // Render Settings Tab
                    SettingsScreen(
                        viewModel = viewModel,
                        modifier = Modifier.statusBarsPadding()
                    )
                } else {
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
                            }
                            Text(
                                text = "PC, PS & XBOX FREE RADAR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonGreen,
                                letterSpacing = 1.sp
                            )
                        }
                    }

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

                        // Jump to Settings
                        IconButton(
                            onClick = { viewModel.selectTab(MainTab.SETTINGS) },
                            modifier = Modifier.testTag("top_bar_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Expandable Search Bar with fluid spring animation
                AnimatedVisibility(
                    visible = isSearchExpanded,
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
                    visible = reopenStatusMessage != null,
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

                // Stats Banner on Discover
                if (currentTab == MainTab.DISCOVER) {
                    StatsBanner(
                        stats = uiStats,
                        isRefreshing = isLoading,
                        onRefresh = { viewModel.refreshGiveaways() }
                    )
                }

                // Total Money Saved Banner on In Library
                if (currentTab == MainTab.CLAIMED) {
                    ClaimedSavingsBanner(
                        savingsUsd = uiStats.claimedTotalSavedUsd,
                        claimedCount = uiStats.claimedCount
                    )
                }

                // Active Filters Summary Pill (if any non-defaults are selected)
                if (activeFilterCount > 0 && currentTab != MainTab.SETTINGS) {
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
                                .clickable { viewModel.setFilterSheetOpen(true) }
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
                                    if (selectedTypeFilter != TypeFilter.ALL) {
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
                                .clickable { viewModel.resetFilters() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Main List
                Box(modifier = Modifier.fillMaxSize()) {
                    if (isLoading && displayedGiveaways.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Scanning PC, PlayStation & Xbox for Free Loot...",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    } else if (displayedGiveaways.isEmpty()) {
                        val emptyIconBob by rememberInfiniteTransition(label = "emptyBob").animateFloat(
                            initialValue = -4f,
                            targetValue = 4f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1600, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "emptyIconBob"
                        )

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
                                    imageVector = when (currentTab) {
                                        MainTab.DISCOVER -> Icons.Default.SportsEsports
                                        MainTab.CLAIMED -> Icons.Default.CheckCircle
                                        MainTab.SETTINGS -> Icons.Default.Settings
                                    },
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = when (currentTab) {
                                    MainTab.DISCOVER -> "No Giveaways Found"
                                    MainTab.CLAIMED -> "No Claimed Games Yet"
                                    MainTab.SETTINGS -> ""
                                },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = when (currentTab) {
                                    MainTab.DISCOVER -> "Try adjusting your filters in the top menu or clearing your search term."
                                    MainTab.CLAIMED -> "Tap 'Claim Free' on any game to automatically add it to your library and track total dollars saved!"
                                    MainTab.SETTINGS -> ""
                                },
                                fontSize = 13.sp,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("giveaways_list"),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 6.dp,
                                bottom = 24.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(
                                when (cardDensity) {
                                    CardDensity.RICH_CARDS -> 16.dp
                                    CardDensity.COMPACT_CARDS -> 10.dp
                                    CardDensity.MINIMAL_LIST -> 6.dp
                                }
                            )
                        ) {
                            items(displayedGiveaways, key = { it.id }) { item ->
                                GiveawayCard(
                                    item = item,
                                    cardDensity = cardDensity,
                                    showOriginalPrice = isShowOriginalPrice,
                                    showExpiration = isShowExpiration,
                                    onClick = { viewModel.openDetail(item) },
                                    onClaimClick = {
                                        if (item.hasMultipleLaunchers) {
                                            launcherChooserItem = item
                                        } else {
                                            viewModel.claimAndLaunch(context, item)
                                        }
                                    },
                                    onToggleBookmark = { viewModel.toggleBookmark(item) },
                                    onToggleClaimed = { viewModel.toggleClaimed(item) },
                                    modifier = if (isAnimationsEnabled) Modifier.animateItem() else Modifier
                                )
                            }
                        }
                    }
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
