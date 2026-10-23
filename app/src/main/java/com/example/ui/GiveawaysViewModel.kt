package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.GiveawayItem
import com.example.data.model.LauncherType
import com.example.data.repository.GiveawaysRepository
import com.example.notifications.NotificationHelper
import com.example.notifications.NotificationScheduler
import com.example.data.local.NotificationHistoryEntity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.preferences.AccentTheme
import com.example.preferences.AppPreferences
import com.example.preferences.BackgroundTheme
import com.example.preferences.CardDensity
import com.example.preferences.DpiScaleMode
import com.example.preferences.TargetFrameRate
import com.example.preferences.ThemeMode
import com.example.data.model.LauncherOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import androidx.compose.runtime.Immutable
import com.example.LootDropApplication
import com.example.StorageMetrics

enum class MainTab {
    DISCOVER,
    CLAIMED,
    CALENDAR,
    SETTINGS
}

enum class SortOption(val label: String) {
    NEWEST("Newest First"),
    HIGHEST_VALUE("Highest Value ($)"),
    MOST_POPULAR("Most Claimed"),
    ENDING_SOON("Ending Soon")
}

enum class TypeFilter(val label: String, val apiMatch: String?) {
    ALL("All Loot", null),
    FULL_GAMES("Full Games", "Game"),
    DLC_LOOT("DLC & Loot", "DLC"),
    EARLY_ACCESS("Beta / Early", "Early Access")
}

data class FilterCriteria(
    val tab: MainTab = MainTab.DISCOVER,
    val launchers: Set<LauncherType> = emptySet(),
    val typeFilter: TypeFilter = TypeFilter.ALL,
    val sort: SortOption = SortOption.NEWEST,
    val query: String = ""
)
@Immutable
data class UiStats(
    val totalFreeWorthUsd: Double = 0.0,
    val activeGiveawaysCount: Int = 0,
    val newFreebiesCount: Int = 0,
    val platformCounts: Map<LauncherType, Int> = emptyMap(),
    val claimedTotalSavedUsd: Double = 0.0,
    val claimedCount: Int = 0
)

class GiveawaysViewModel(application: Application) : AndroidViewModel(application) {

    val notificationHelper = NotificationHelper(application)
    val preferences = AppPreferences(application)
    private val database = AppDatabase.getInstance(application)
    private val repository = GiveawaysRepository(database.giveawayDao(), notificationHelper)

    private val _selectedTab = MutableStateFlow(MainTab.DISCOVER)
    val selectedTab: StateFlow<MainTab> = _selectedTab.asStateFlow()

    private val _selectedLaunchers = MutableStateFlow<Set<LauncherType>>(
        if (preferences.defaultLauncher == LauncherType.ALL) emptySet() else setOf(preferences.defaultLauncher)
    )
    val selectedLaunchers: StateFlow<Set<LauncherType>> = _selectedLaunchers.asStateFlow()

    // Backward-compatibility property
    val selectedLauncher: StateFlow<LauncherType> = _selectedLaunchers.map { set ->
        if (set.isEmpty() || set.contains(LauncherType.ALL)) LauncherType.ALL else set.first()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LauncherType.ALL)

    private val _selectedTypeFilter = MutableStateFlow(TypeFilter.ALL)
    val selectedTypeFilter: StateFlow<TypeFilter> = _selectedTypeFilter.asStateFlow()

    private val _selectedSort = MutableStateFlow(SortOption.NEWEST)
    val selectedSort: StateFlow<SortOption> = _selectedSort.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _detailItem = MutableStateFlow<GiveawayItem?>(null)
    val detailItem: StateFlow<GiveawayItem?> = _detailItem.asStateFlow()

    private val _isFilterSheetOpen = MutableStateFlow(false)
    val isFilterSheetOpen: StateFlow<Boolean> = _isFilterSheetOpen.asStateFlow()

    val activeFilterCount: StateFlow<Int> = combine(
        _selectedLaunchers,
        _selectedTypeFilter,
        _selectedSort
    ) { launchers, typeFilter, sort ->
        var count = 0
        if (launchers.isNotEmpty() && !launchers.contains(LauncherType.ALL)) {
            count += launchers.size
        }
        if (typeFilter != TypeFilter.ALL) count++
        if (sort != SortOption.NEWEST) count++
        count
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Preferences & Theme flows
    private val _themeMode = MutableStateFlow(preferences.themeMode)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _accentTheme = MutableStateFlow(preferences.accentTheme)
    val accentTheme: StateFlow<AccentTheme> = _accentTheme.asStateFlow()

    private val _darkBackgroundTheme = MutableStateFlow(preferences.darkBackgroundTheme)
    val darkBackgroundTheme: StateFlow<BackgroundTheme> = _darkBackgroundTheme.asStateFlow()

    private val _lightBackgroundTheme = MutableStateFlow(preferences.lightBackgroundTheme)
    val lightBackgroundTheme: StateFlow<BackgroundTheme> = _lightBackgroundTheme.asStateFlow()

    private val _cardDensity = MutableStateFlow(preferences.cardDensity)
    val cardDensity: StateFlow<CardDensity> = _cardDensity.asStateFlow()

    private val _defaultLauncher = MutableStateFlow(preferences.defaultLauncher)
    val defaultLauncher: StateFlow<LauncherType> = _defaultLauncher.asStateFlow()

    private val _isShowOriginalPrice = MutableStateFlow(preferences.isShowOriginalPrice)
    val isShowOriginalPrice: StateFlow<Boolean> = _isShowOriginalPrice.asStateFlow()

    private val _isShowExpiration = MutableStateFlow(preferences.isShowExpiration)
    val isShowExpiration: StateFlow<Boolean> = _isShowExpiration.asStateFlow()

    private val _isConfirmToastOnClaim = MutableStateFlow(preferences.isConfirmToastOnClaim)
    val isConfirmToastOnClaim: StateFlow<Boolean> = _isConfirmToastOnClaim.asStateFlow()

    private val _isAutoCheckOnReopen = MutableStateFlow(preferences.isAutoCheckOnReopen)
    val isAutoCheckOnReopen: StateFlow<Boolean> = _isAutoCheckOnReopen.asStateFlow()

    private val _isAnimationsEnabled = MutableStateFlow(preferences.isAnimationsEnabled)
    val isAnimationsEnabled: StateFlow<Boolean> = _isAnimationsEnabled.asStateFlow()

    private val _isLowEndOptimization = MutableStateFlow(preferences.isLowEndOptimization)
    val isLowEndOptimization: StateFlow<Boolean> = _isLowEndOptimization.asStateFlow()

    private val _targetFrameRate = MutableStateFlow(preferences.targetFrameRate)
    val targetFrameRate: StateFlow<TargetFrameRate> = _targetFrameRate.asStateFlow()

    private val _detectedRefreshRate = MutableStateFlow(90)
    val detectedRefreshRate: StateFlow<Int> = _detectedRefreshRate.asStateFlow()

    private val _isDpiOptimizationEnabled = MutableStateFlow(preferences.isDpiOptimizationEnabled)
    val isDpiOptimizationEnabled: StateFlow<Boolean> = _isDpiOptimizationEnabled.asStateFlow()

    private val _dpiScaleMode = MutableStateFlow(preferences.dpiScaleMode)
    val dpiScaleMode: StateFlow<DpiScaleMode> = _dpiScaleMode.asStateFlow()

    private val _detectedScreenDpi = MutableStateFlow(preferences.detectedScreenDpi)
    val detectedScreenDpi: StateFlow<Int> = _detectedScreenDpi.asStateFlow()

    private val _isDataSaverMode = MutableStateFlow(preferences.isDataSaverMode)
    val isDataSaverMode: StateFlow<Boolean> = _isDataSaverMode.asStateFlow()

    private val _isAutoPruneExpired = MutableStateFlow(preferences.isAutoPruneExpired)
    val isAutoPruneExpired: StateFlow<Boolean> = _isAutoPruneExpired.asStateFlow()

    private val _isNewTodayDismissed = MutableStateFlow(preferences.isNewTodayDismissed)
    val isNewTodayDismissed: StateFlow<Boolean> = _isNewTodayDismissed.asStateFlow()

    private val _showOnboardingWalkthrough = MutableStateFlow(!preferences.hasSeenOnboarding)
    val showOnboardingWalkthrough: StateFlow<Boolean> = _showOnboardingWalkthrough.asStateFlow()

    private val _reopenStatusMessage = MutableStateFlow<String?>(null)
    val reopenStatusMessage: StateFlow<String?> = _reopenStatusMessage.asStateFlow()

    private var lastRefreshTimestamp: Long = 0L

    // Notification State Flows for Settings Screen
    private val _isNotificationsEnabled = MutableStateFlow(notificationHelper.isNotificationsEnabled)
    val isNotificationsEnabled: StateFlow<Boolean> = _isNotificationsEnabled.asStateFlow()

    private val _isNotifyPlayStation = MutableStateFlow(notificationHelper.isNotifyPlayStation)
    val isNotifyPlayStation: StateFlow<Boolean> = _isNotifyPlayStation.asStateFlow()

    private val _isNotifyXbox = MutableStateFlow(notificationHelper.isNotifyXbox)
    val isNotifyXbox: StateFlow<Boolean> = _isNotifyXbox.asStateFlow()

    private val _isNotifyNintendo = MutableStateFlow(notificationHelper.isNotifyNintendo)
    val isNotifyNintendo: StateFlow<Boolean> = _isNotifyNintendo.asStateFlow()

    private val _isNotifySteam = MutableStateFlow(notificationHelper.isNotifySteam)
    val isNotifySteam: StateFlow<Boolean> = _isNotifySteam.asStateFlow()

    private val _isNotifyEpic = MutableStateFlow(notificationHelper.isNotifyEpic)
    val isNotifyEpic: StateFlow<Boolean> = _isNotifyEpic.asStateFlow()

    private val _isNotifyGog = MutableStateFlow(notificationHelper.isNotifyGog)
    val isNotifyGog: StateFlow<Boolean> = _isNotifyGog.asStateFlow()

    private val _isNotifyOthers = MutableStateFlow(notificationHelper.isNotifyOthers)
    val isNotifyOthers: StateFlow<Boolean> = _isNotifyOthers.asStateFlow()

    private val _isGamesOnly = MutableStateFlow(notificationHelper.isGamesOnly)
    val isGamesOnly: StateFlow<Boolean> = _isGamesOnly.asStateFlow()

    private val _isNotifyEndingSoon = MutableStateFlow(preferences.isNotifyEndingSoon)
    val isNotifyEndingSoon: StateFlow<Boolean> = _isNotifyEndingSoon.asStateFlow()

    private val _minWorthAlertThreshold = MutableStateFlow(preferences.minWorthAlertThreshold)
    val minWorthAlertThreshold: StateFlow<Float> = _minWorthAlertThreshold.asStateFlow()

    private val _isQuietHoursEnabled = MutableStateFlow(preferences.isQuietHoursEnabled)
    val isQuietHoursEnabled: StateFlow<Boolean> = _isQuietHoursEnabled.asStateFlow()

    private val _isNotificationSoundEnabled = MutableStateFlow(notificationHelper.isNotificationSoundEnabled)
    val isNotificationSoundEnabled: StateFlow<Boolean> = _isNotificationSoundEnabled.asStateFlow()

    private val _isNotificationVibrateEnabled = MutableStateFlow(notificationHelper.isNotificationVibrateEnabled)
    val isNotificationVibrateEnabled: StateFlow<Boolean> = _isNotificationVibrateEnabled.asStateFlow()

    private val _isKeepForeverOnly = MutableStateFlow(notificationHelper.isKeepForeverOnly)
    val isKeepForeverOnly: StateFlow<Boolean> = _isKeepForeverOnly.asStateFlow()

    private val _isNotificationRichArtwork = MutableStateFlow(notificationHelper.isNotificationRichArtwork)
    val isNotificationRichArtwork: StateFlow<Boolean> = _isNotificationRichArtwork.asStateFlow()

    private val _isNotificationActionsEnabled = MutableStateFlow(notificationHelper.isNotificationActionsEnabled)
    val isNotificationActionsEnabled: StateFlow<Boolean> = _isNotificationActionsEnabled.asStateFlow()

    private val _quietHoursStartHour = MutableStateFlow(preferences.quietHoursStartHour)
    val quietHoursStartHour: StateFlow<Int> = _quietHoursStartHour.asStateFlow()

    private val _quietHoursEndHour = MutableStateFlow(preferences.quietHoursEndHour)
    val quietHoursEndHour: StateFlow<Int> = _quietHoursEndHour.asStateFlow()

    private val _backgroundSyncIntervalHours = MutableStateFlow(preferences.backgroundSyncIntervalHours)
    val backgroundSyncIntervalHours: StateFlow<Int> = _backgroundSyncIntervalHours.asStateFlow()

    private val _backgroundSyncWifiOnly = MutableStateFlow(preferences.backgroundSyncWifiOnly)
    val backgroundSyncWifiOnly: StateFlow<Boolean> = _backgroundSyncWifiOnly.asStateFlow()

    private val _isNotificationHistorySheetOpen = MutableStateFlow(false)
    val isNotificationHistorySheetOpen: StateFlow<Boolean> = _isNotificationHistorySheetOpen.asStateFlow()

    val notificationHistory: StateFlow<List<NotificationHistoryEntity>> =
        database.notificationHistoryDao().getAllNotificationsFlow().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val unreadNotificationCount: StateFlow<Int> =
        database.notificationHistoryDao().getUnreadCountFlow().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0
        )

    // Base flow from repository
    val allGiveaways: StateFlow<List<GiveawayItem>> = repository.allGiveawaysFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Compute live UI stats
    val uiStats: StateFlow<UiStats> = allGiveaways.combine(_selectedTab) { all, _ ->
        val totalWorth = all.sumOf { it.numericWorth }
        val newCount = all.count { it.isNew }
        val claimedGames = all.filter { it.isClaimed }
        val claimedTotalSaved = claimedGames.sumOf { it.numericWorth }
        val claimedCount = claimedGames.size

        val platformMap = mutableMapOf<LauncherType, Int>()
        for (launcher in LauncherType.values()) {
            if (launcher == LauncherType.ALL) {
                platformMap[launcher] = all.size
            } else {
                platformMap[launcher] = all.count { it.matchesLauncher(launcher) }
            }
        }
        UiStats(
            totalFreeWorthUsd = totalWorth,
            activeGiveawaysCount = all.size,
            newFreebiesCount = newCount,
            platformCounts = platformMap,
            claimedTotalSavedUsd = claimedTotalSaved,
            claimedCount = claimedCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiStats())

    // Helper method to apply launcher, type, search and sort filters to any giveaway list
    private fun filterAndSortList(
        source: List<GiveawayItem>,
        launchers: Set<LauncherType>,
        typeFilter: TypeFilter,
        sort: SortOption,
        query: String
    ): List<GiveawayItem> {
        var filtered = source

        // Multi-Platform / Store filter
        if (launchers.isNotEmpty() && !launchers.contains(LauncherType.ALL)) {
            filtered = filtered.filter { item ->
                launchers.any { launcher -> item.matchesLauncher(launcher) }
            }
        }

        // Type filter
        if (typeFilter != TypeFilter.ALL && typeFilter.apiMatch != null) {
            val match = typeFilter.apiMatch
            filtered = filtered.filter { it.type.contains(match, ignoreCase = true) }
        }

        // Search query
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            filtered = filtered.filter {
                it.title.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.platforms.lowercase().contains(q) ||
                (q.contains("playstation") && it.isPlayStation) ||
                (q.contains("ps5") && it.isPlayStation) ||
                (q.contains("ps4") && it.isPlayStation) ||
                (q.contains("xbox") && it.isXbox) ||
                (q.contains("nintendo") && it.isNintendo) ||
                (q.contains("switch") && it.isNintendo) ||
                (q.contains("console") && it.isConsole)
            }
        }

        // Sorting
        return when (sort) {
            SortOption.NEWEST -> filtered.sortedByDescending { it.id }
            SortOption.HIGHEST_VALUE -> filtered.sortedByDescending { it.numericWorth }
            SortOption.MOST_POPULAR -> filtered.sortedByDescending { it.users }
            SortOption.ENDING_SOON -> filtered.sortedBy {
                if (it.endDate == "N/A" || it.endDate.isBlank()) "9999" else it.endDate
            }
        }
    }

    // Discover giveaways: always all items filtered & sorted (never empties or switches to claimed on tab change)
    val discoverGiveaways: StateFlow<List<GiveawayItem>> = combine(
        allGiveaways,
        _selectedLaunchers,
        _selectedTypeFilter,
        _selectedSort,
        _searchQuery
    ) { all, launchers, typeFilter, sort, query ->
        filterAndSortList(all, launchers, typeFilter, sort, query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Claimed giveaways: always claimed library items filtered & sorted
    val claimedGiveaways: StateFlow<List<GiveawayItem>> = combine(
        allGiveaways,
        _selectedLaunchers,
        _selectedTypeFilter,
        _selectedSort,
        _searchQuery
    ) { all, launchers, typeFilter, sort, query ->
        filterAndSortList(all.filter { it.isClaimed }, launchers, typeFilter, sort, query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered & sorted list matching the currently selected tab
    val displayedGiveaways: StateFlow<List<GiveawayItem>> = combine(
        _selectedTab,
        discoverGiveaways,
        claimedGiveaways
    ) { tab, discover, claimed ->
        when (tab) {
            MainTab.DISCOVER -> discover
            MainTab.CLAIMED -> claimed
            MainTab.CALENDAR -> discover
            MainTab.SETTINGS -> emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _storageMetrics = MutableStateFlow(
        LootDropApplication.getStorageMetrics(getApplication())
    )
    val storageMetrics: StateFlow<StorageMetrics> = _storageMetrics.asStateFlow()

    private val _storageFootprintBytes = MutableStateFlow(0L)
    val storageFootprintBytes: StateFlow<Long> = _storageFootprintBytes.asStateFlow()

    init {
        refreshGiveaways()
        updateStorageFootprint()
        NotificationScheduler.schedulePeriodicSync(application)
    }

    fun updateStorageFootprint() {
        viewModelScope.launch(Dispatchers.IO) {
            val metrics = LootDropApplication.getStorageMetrics(getApplication())
            _storageMetrics.value = metrics
            _storageFootprintBytes.value = metrics.totalFootprintBytes
        }
    }

    fun clearAllCaches() {
        performDeepClean()
    }

    fun performDeepClean() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val reclaimed = repository.clearAllCaches(getApplication())
            if (preferences.isAutoPruneExpired) {
                repository.pruneExpiredGiveaways(getApplication())
            }
            updateStorageFootprint()
            _isLoading.value = false
            val formatted = _storageMetrics.value.formatBytes(reclaimed)
            _reopenStatusMessage.value = "Deep clean complete! Reclaimed $formatted of disk storage."
        }
    }

    fun clearImageCache() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val reclaimed = repository.clearImageCache(getApplication())
            updateStorageFootprint()
            _isLoading.value = false
            val formatted = _storageMetrics.value.formatBytes(reclaimed)
            _reopenStatusMessage.value = "Image cache cleared! Reclaimed $formatted."
        }
    }

    fun clearNetworkCache() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val reclaimed = repository.clearHttpCache(getApplication())
            updateStorageFootprint()
            _isLoading.value = false
            val formatted = _storageMetrics.value.formatBytes(reclaimed)
            _reopenStatusMessage.value = "Network API cache cleared! Reclaimed $formatted."
        }
    }

    fun optimizeDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val pruned = if (preferences.isAutoPruneExpired) {
                repository.pruneExpiredGiveaways(getApplication())
            } else 0
            repository.vacuumDatabase(getApplication())
            updateStorageFootprint()
            _isLoading.value = false
            _reopenStatusMessage.value = if (pruned > 0) {
                "Database compacted with VACUUM. Purged $pruned expired freebies."
            } else {
                "Database compacted & defragmented successfully."
            }
        }
    }

    fun refreshGiveaways(isAutoCheck: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            lastRefreshTimestamp = System.currentTimeMillis()
            val result = repository.refreshGiveaways()
            _isLoading.value = false
            if (result.isFailure) {
                if (!isAutoCheck || allGiveaways.value.isEmpty()) {
                    _errorMessage.value = result.exceptionOrNull()?.localizedMessage
                        ?: "Could not fetch latest free games. Check connection."
                }
            } else {
                updateStorageFootprint()
                val syncResult = result.getOrNull()
                if (syncResult != null && syncResult.newGamesCount > 0) {
                    _reopenStatusMessage.value = "🎯 Found ${syncResult.newGamesCount} new free game${if (syncResult.newGamesCount > 1) "s" else ""}!"
                    preferences.isNewTodayDismissed = false
                    _isNewTodayDismissed.value = false
                } else if (isAutoCheck) {
                    _reopenStatusMessage.value = "⚡ Auto-check complete: Games are up to date"
                }
            }
        }
    }

    fun checkNewGamesOnAppReopen() {
        if (!preferences.isAutoCheckOnReopen) return

        val now = System.currentTimeMillis()
        // If already refreshing or refreshed less than 4 seconds ago, skip redundant check
        if (_isLoading.value || (now - lastRefreshTimestamp < 4000L && lastRefreshTimestamp != 0L)) {
            return
        }

        refreshGiveaways(isAutoCheck = true)
    }

    fun clearReopenStatusMessage() {
        _reopenStatusMessage.value = null
    }

    fun onAppClosedOrLeft() {
        _reopenStatusMessage.value = null
    }

    fun selectTab(tab: MainTab) {
        _selectedTab.value = tab
    }

    fun toggleLauncher(launcher: LauncherType) {
        if (launcher == LauncherType.ALL) {
            _selectedLaunchers.value = emptySet()
            return
        }
        val current = _selectedLaunchers.value.toMutableSet()
        current.remove(LauncherType.ALL)
        if (current.contains(launcher)) {
            current.remove(launcher)
        } else {
            current.add(launcher)
        }
        _selectedLaunchers.value = current
    }

    fun selectLauncher(launcher: LauncherType) {
        toggleLauncher(launcher)
    }

    fun clearLauncherFilters() {
        _selectedLaunchers.value = emptySet()
    }

    fun selectTypeFilter(type: TypeFilter) {
        _selectedTypeFilter.value = type
    }

    fun selectSort(sort: SortOption) {
        _selectedSort.value = sort
    }

    fun setFilterSheetOpen(open: Boolean) {
        _isFilterSheetOpen.value = open
    }

    fun resetFilters() {
        _selectedLaunchers.value = emptySet()
        _selectedTypeFilter.value = TypeFilter.ALL
        _selectedSort.value = SortOption.NEWEST
        _searchQuery.value = ""
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openDetail(item: GiveawayItem) {
        _detailItem.value = item
    }

    fun closeDetail() {
        _detailItem.value = null
    }

    // Theme & Appearance Actions
    fun setThemeMode(mode: ThemeMode) {
        preferences.themeMode = mode
        _themeMode.value = mode
    }

    fun setAccentTheme(theme: AccentTheme) {
        preferences.accentTheme = theme
        _accentTheme.value = theme
    }

    fun setDarkBackgroundTheme(theme: BackgroundTheme) {
        preferences.darkBackgroundTheme = theme
        _darkBackgroundTheme.value = theme
    }

    fun setLightBackgroundTheme(theme: BackgroundTheme) {
        preferences.lightBackgroundTheme = theme
        _lightBackgroundTheme.value = theme
    }

    fun setCardDensity(density: CardDensity) {
        preferences.cardDensity = density
        _cardDensity.value = density
    }

    fun setShowOriginalPrice(show: Boolean) {
        preferences.isShowOriginalPrice = show
        _isShowOriginalPrice.value = show
    }

    fun setShowExpiration(show: Boolean) {
        preferences.isShowExpiration = show
        _isShowExpiration.value = show
    }

    fun setConfirmToastOnClaim(confirm: Boolean) {
        preferences.isConfirmToastOnClaim = confirm
        _isConfirmToastOnClaim.value = confirm
    }

    fun setAutoCheckOnReopen(enabled: Boolean) {
        preferences.isAutoCheckOnReopen = enabled
        _isAutoCheckOnReopen.value = enabled
    }

    fun setDataSaverMode(enabled: Boolean) {
        preferences.isDataSaverMode = enabled
        _isDataSaverMode.value = enabled
    }

    fun setAutoPruneExpired(enabled: Boolean) {
        preferences.isAutoPruneExpired = enabled
        _isAutoPruneExpired.value = enabled
    }

    fun setDefaultLauncher(launcher: LauncherType) {
        preferences.defaultLauncher = launcher
        _defaultLauncher.value = launcher
    }

    fun setAnimationsEnabled(enabled: Boolean) {
        preferences.isAnimationsEnabled = enabled
        _isAnimationsEnabled.value = enabled
    }

    fun setLowEndOptimization(enabled: Boolean) {
        preferences.isLowEndOptimization = enabled
        _isLowEndOptimization.value = enabled
    }

    fun setTargetFrameRate(frameRate: TargetFrameRate) {
        preferences.targetFrameRate = frameRate
        _targetFrameRate.value = frameRate
    }

    fun setDetectedRefreshRate(rate: Int) {
        _detectedRefreshRate.value = rate
    }

    fun setDpiOptimizationEnabled(enabled: Boolean) {
        preferences.isDpiOptimizationEnabled = enabled
        _isDpiOptimizationEnabled.value = enabled
    }

    fun setDpiScaleMode(mode: DpiScaleMode) {
        preferences.dpiScaleMode = mode
        _dpiScaleMode.value = mode
    }

    fun setDetectedScreenDpi(dpi: Int) {
        preferences.detectedScreenDpi = dpi
        _detectedScreenDpi.value = dpi
    }

    fun completeOnboarding() {
        preferences.hasSeenOnboarding = true
        _showOnboardingWalkthrough.value = false
    }

    fun dismissOnboarding() {
        preferences.hasSeenOnboarding = true
        _showOnboardingWalkthrough.value = false
    }

    fun replayOnboarding() {
        _showOnboardingWalkthrough.value = true
    }

    fun dismissNewToday() {
        preferences.isNewTodayDismissed = true
        _isNewTodayDismissed.value = true
    }

    fun resetNewTodayDismissed() {
        preferences.isNewTodayDismissed = false
        _isNewTodayDismissed.value = false
    }

    // Notification Preferences Actions
    fun setNotificationsEnabled(enabled: Boolean) {
        notificationHelper.isNotificationsEnabled = enabled
        _isNotificationsEnabled.value = enabled
    }

    fun setNotifyPlayStation(enabled: Boolean) {
        notificationHelper.isNotifyPlayStation = enabled
        _isNotifyPlayStation.value = enabled
    }

    fun setNotifyXbox(enabled: Boolean) {
        notificationHelper.isNotifyXbox = enabled
        _isNotifyXbox.value = enabled
    }

    fun setNotifyNintendo(enabled: Boolean) {
        notificationHelper.isNotifyNintendo = enabled
        _isNotifyNintendo.value = enabled
    }

    fun setNotifySteam(enabled: Boolean) {
        notificationHelper.isNotifySteam = enabled
        _isNotifySteam.value = enabled
    }

    fun setNotifyEpic(enabled: Boolean) {
        notificationHelper.isNotifyEpic = enabled
        _isNotifyEpic.value = enabled
    }

    fun setNotifyGog(enabled: Boolean) {
        notificationHelper.isNotifyGog = enabled
        _isNotifyGog.value = enabled
    }

    fun setNotifyOthers(enabled: Boolean) {
        notificationHelper.isNotifyOthers = enabled
        _isNotifyOthers.value = enabled
    }

    fun setGamesOnly(enabled: Boolean) {
        notificationHelper.isGamesOnly = enabled
        _isGamesOnly.value = enabled
    }

    fun setNotifyEndingSoon(enabled: Boolean) {
        notificationHelper.isNotifyEndingSoon = enabled
        _isNotifyEndingSoon.value = enabled
    }

    fun setMinWorthAlertThreshold(threshold: Float) {
        notificationHelper.minWorthAlertThreshold = threshold
        _minWorthAlertThreshold.value = threshold
    }

    fun setQuietHoursEnabled(enabled: Boolean) {
        notificationHelper.isQuietHoursEnabled = enabled
        _isQuietHoursEnabled.value = enabled
    }

    fun setNotificationSoundEnabled(enabled: Boolean) {
        notificationHelper.isNotificationSoundEnabled = enabled
        _isNotificationSoundEnabled.value = enabled
    }

    fun setNotificationVibrateEnabled(enabled: Boolean) {
        notificationHelper.isNotificationVibrateEnabled = enabled
        _isNotificationVibrateEnabled.value = enabled
    }

    fun setKeepForeverOnly(enabled: Boolean) {
        notificationHelper.isKeepForeverOnly = enabled
        _isKeepForeverOnly.value = enabled
    }

    fun setNotificationRichArtwork(enabled: Boolean) {
        notificationHelper.isNotificationRichArtwork = enabled
        _isNotificationRichArtwork.value = enabled
    }

    fun setNotificationActionsEnabled(enabled: Boolean) {
        notificationHelper.isNotificationActionsEnabled = enabled
        _isNotificationActionsEnabled.value = enabled
    }

    fun setQuietHoursHours(start: Int, end: Int) {
        preferences.quietHoursStartHour = start
        preferences.quietHoursEndHour = end
        _quietHoursStartHour.value = start
        _quietHoursEndHour.value = end
    }

    fun setBackgroundSyncInterval(hours: Int) {
        preferences.backgroundSyncIntervalHours = hours
        _backgroundSyncIntervalHours.value = hours
        NotificationScheduler.schedulePeriodicSync(getApplication())
    }

    fun setBackgroundSyncWifiOnly(wifiOnly: Boolean) {
        preferences.backgroundSyncWifiOnly = wifiOnly
        _backgroundSyncWifiOnly.value = wifiOnly
    }

    fun setNotificationHistorySheetOpen(open: Boolean) {
        _isNotificationHistorySheetOpen.value = open
    }

    fun sendTestNotification(isRichPreview: Boolean = true) {
        notificationHelper.sendTestNotification(isRichPreview)
    }

    fun sendEndingSoonTestNotification() {
        notificationHelper.sendTestEndingSoonNotification()
    }

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            database.notificationHistoryDao().markAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch(Dispatchers.IO) {
            database.notificationHistoryDao().markAllAsRead()
        }
    }

    fun clearAllNotificationHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            database.notificationHistoryDao().clearAllNotifications()
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            database.notificationHistoryDao().deleteNotification(id)
        }
    }

    // Library Data Management Actions
    fun clearClaimedGames() {
        viewModelScope.launch {
            repository.clearAllClaimed()
        }
    }

    fun clearBookmarkedGames() {
        viewModelScope.launch {
            repository.clearAllBookmarks()
        }
    }

    fun forceResync() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.forceResync()
            _isLoading.value = false
        }
    }

    fun toggleClaimed(item: GiveawayItem) {
        viewModelScope.launch {
            repository.toggleClaimed(item.id, item.isClaimed)
            if (_detailItem.value?.id == item.id) {
                _detailItem.value = _detailItem.value?.copy(isClaimed = !item.isClaimed)
            }
        }
    }

    fun toggleBookmark(item: GiveawayItem) {
        viewModelScope.launch {
            repository.toggleBookmarked(item.id, item.isBookmarked)
            if (_detailItem.value?.id == item.id) {
                _detailItem.value = _detailItem.value?.copy(isBookmarked = !item.isBookmarked)
            }
        }
    }

    fun toggleBookmarkById(id: Int) {
        viewModelScope.launch {
            val item = repository.getGiveawayById(id)
            if (item != null) {
                repository.toggleBookmarked(item.id, item.isBookmarked)
            } else {
                database.giveawayDao().setBookmarked(id, true)
            }
        }
    }

    fun openItemById(id: Int) {
        viewModelScope.launch {
            val item = repository.getGiveawayById(id)
            if (item != null) {
                _detailItem.value = item
            }
        }
    }

    fun getLauncherOptionsFor(item: GiveawayItem): List<LauncherOption> {
        val baseOptions = item.availableLaunchers.toMutableList()
        val cleanTitle = item.title.trim().lowercase()
            .replace(Regex("(?i)\\b(giveaway|free|dlc|pack|key|bundle|edition|standard|steam|epic|gog|playstation|ps5|ps4|xbox)\\b"), "")
            .trim()

        if (cleanTitle.length >= 3) {
            val matchingGiveaways = allGiveaways.value.filter { other ->
                other.id != item.id && other.title.trim().lowercase().contains(cleanTitle)
            }
            for (match in matchingGiveaways) {
                for (opt in match.availableLaunchers) {
                    if (opt.launcherType != LauncherType.OTHER && baseOptions.none { it.launcherType == opt.launcherType }) {
                        baseOptions.add(opt)
                    }
                }
            }
        }
        return baseOptions
    }

    fun claimAndLaunch(
        context: Context,
        item: GiveawayItem,
        targetUrl: String? = null,
        launcherName: String? = null
    ) {
        if (!item.isClaimed) {
            viewModelScope.launch {
                repository.toggleClaimed(item.id, false)
                if (_detailItem.value?.id == item.id) {
                    _detailItem.value = _detailItem.value?.copy(isClaimed = true)
                }
            }
            if (_isConfirmToastOnClaim.value) {
                val label = if (launcherName != null) " on $launcherName" else ""
                Toast.makeText(context, "Claiming '${item.title}'$label! Added to library.", Toast.LENGTH_SHORT).show()
            }
        }
        val url = targetUrl ?: item.openGiveawayUrl
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
            clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("Giveaway URL", url))
            Toast.makeText(
                context,
                "No browser found. Link copied to clipboard!",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
