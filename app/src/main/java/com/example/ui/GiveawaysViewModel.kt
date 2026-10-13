package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.GiveawayItem
import com.example.data.model.LauncherType
import com.example.data.repository.GiveawaysRepository
import com.example.notifications.NotificationHelper
import com.example.preferences.AccentTheme
import com.example.preferences.AppPreferences
import com.example.preferences.BackgroundTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    DISCOVER,
    BOOKMARKS,
    CLAIMED,
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
    val launcher: LauncherType = LauncherType.ALL,
    val typeFilter: TypeFilter = TypeFilter.ALL,
    val sort: SortOption = SortOption.NEWEST,
    val query: String = ""
)

data class UiStats(
    val totalFreeWorthUsd: Double = 0.0,
    val activeGiveawaysCount: Int = 0,
    val newFreebiesCount: Int = 0,
    val platformCounts: Map<LauncherType, Int> = emptyMap()
)

class GiveawaysViewModel(application: Application) : AndroidViewModel(application) {

    val notificationHelper = NotificationHelper(application)
    val preferences = AppPreferences(application)
    private val database = AppDatabase.getInstance(application)
    private val repository = GiveawaysRepository(database.giveawayDao(), notificationHelper)

    private val _selectedTab = MutableStateFlow(MainTab.DISCOVER)
    val selectedTab: StateFlow<MainTab> = _selectedTab.asStateFlow()

    private val _selectedLauncher = MutableStateFlow(preferences.defaultLauncher)
    val selectedLauncher: StateFlow<LauncherType> = _selectedLauncher.asStateFlow()

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

    // Preferences & Theme flows
    private val _accentTheme = MutableStateFlow(preferences.accentTheme)
    val accentTheme: StateFlow<AccentTheme> = _accentTheme.asStateFlow()

    private val _backgroundTheme = MutableStateFlow(preferences.backgroundTheme)
    val backgroundTheme: StateFlow<BackgroundTheme> = _backgroundTheme.asStateFlow()

    private val _isCompactCards = MutableStateFlow(preferences.isCompactCards)
    val isCompactCards: StateFlow<Boolean> = _isCompactCards.asStateFlow()

    private val _defaultLauncher = MutableStateFlow(preferences.defaultLauncher)
    val defaultLauncher: StateFlow<LauncherType> = _defaultLauncher.asStateFlow()

    // Notification State Flows for Settings Screen
    private val _isNotificationsEnabled = MutableStateFlow(notificationHelper.isNotificationsEnabled)
    val isNotificationsEnabled: StateFlow<Boolean> = _isNotificationsEnabled.asStateFlow()

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
        val platformMap = mutableMapOf<LauncherType, Int>()
        for (launcher in LauncherType.values()) {
            if (launcher == LauncherType.ALL) {
                platformMap[launcher] = all.size
            } else {
                platformMap[launcher] = all.count { it.launcherCategory == launcher }
            }
        }
        UiStats(
            totalFreeWorthUsd = totalWorth,
            activeGiveawaysCount = all.size,
            newFreebiesCount = newCount,
            platformCounts = platformMap
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiStats())

    // Combine user filters into criteria flow
    private val filterCriteriaFlow: Flow<FilterCriteria> = combine(
        _selectedTab,
        _selectedLauncher,
        _selectedTypeFilter,
        _selectedSort,
        _searchQuery
    ) { tab, launcher, typeFilter, sort, query ->
        FilterCriteria(
            tab = tab,
            launcher = launcher,
            typeFilter = typeFilter,
            sort = sort,
            query = query
        )
    }

    // Filtered & sorted list
    val displayedGiveaways: StateFlow<List<GiveawayItem>> = combine(
        allGiveaways,
        filterCriteriaFlow
    ) { all, filters ->
        val sourceList = when (filters.tab) {
            MainTab.DISCOVER -> all
            MainTab.BOOKMARKS -> all.filter { it.isBookmarked }
            MainTab.CLAIMED -> all.filter { it.isClaimed }
            MainTab.SETTINGS -> emptyList()
        }

        var filtered = sourceList

        // Launcher filter
        if (filters.launcher != LauncherType.ALL) {
            filtered = filtered.filter { it.launcherCategory == filters.launcher }
        }

        // Type filter
        if (filters.typeFilter != TypeFilter.ALL && filters.typeFilter.apiMatch != null) {
            val match = filters.typeFilter.apiMatch
            filtered = filtered.filter { it.type.contains(match, ignoreCase = true) }
        }

        // Search query
        if (filters.query.isNotBlank()) {
            val q = filters.query.trim().lowercase()
            filtered = filtered.filter {
                it.title.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.platforms.lowercase().contains(q)
            }
        }

        // Sorting
        when (filters.sort) {
            SortOption.NEWEST -> filtered.sortedByDescending { it.id }
            SortOption.HIGHEST_VALUE -> filtered.sortedByDescending { it.numericWorth }
            SortOption.MOST_POPULAR -> filtered.sortedByDescending { it.users }
            SortOption.ENDING_SOON -> filtered.sortedBy {
                if (it.endDate == "N/A" || it.endDate.isBlank()) "9999" else it.endDate
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshGiveaways()
    }

    fun refreshGiveaways() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.refreshGiveaways()
            _isLoading.value = false
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage
                    ?: "Could not fetch latest free games. Check connection."
            }
        }
    }

    fun selectTab(tab: MainTab) {
        _selectedTab.value = tab
    }

    fun selectLauncher(launcher: LauncherType) {
        _selectedLauncher.value = launcher
    }

    fun selectTypeFilter(type: TypeFilter) {
        _selectedTypeFilter.value = type
    }

    fun selectSort(sort: SortOption) {
        _selectedSort.value = sort
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
    fun setAccentTheme(theme: AccentTheme) {
        preferences.accentTheme = theme
        _accentTheme.value = theme
    }

    fun setBackgroundTheme(theme: BackgroundTheme) {
        preferences.backgroundTheme = theme
        _backgroundTheme.value = theme
    }

    fun setCompactCards(compact: Boolean) {
        preferences.isCompactCards = compact
        _isCompactCards.value = compact
    }

    fun setDefaultLauncher(launcher: LauncherType) {
        preferences.defaultLauncher = launcher
        _defaultLauncher.value = launcher
    }

    // Notification Preferences Actions
    fun setNotificationsEnabled(enabled: Boolean) {
        notificationHelper.isNotificationsEnabled = enabled
        _isNotificationsEnabled.value = enabled
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

    fun sendTestNotification() {
        notificationHelper.sendTestNotification()
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

    fun openItemById(id: Int) {
        viewModelScope.launch {
            val item = repository.getGiveawayById(id)
            if (item != null) {
                _detailItem.value = item
            }
        }
    }
}
