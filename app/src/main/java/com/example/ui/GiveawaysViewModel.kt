package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.LootDropApplication
import com.example.StorageMetrics
import com.example.data.local.AppDatabase
import com.example.data.local.NotificationHistoryEntity
import com.example.data.local.SteamAchievementEntity
import com.example.data.local.SteamGameEntity
import com.example.data.model.AppUpdateInfo
import com.example.data.model.BacklogChallengeGrid
import com.example.data.model.BacklogMilestoneReward
import com.example.data.model.GameCategory
import com.example.data.model.GiveawayItem
import com.example.data.model.LauncherOption
import com.example.data.model.LauncherType
import com.example.data.model.LibraryPlayFilter
import com.example.data.model.LibraryViewMode
import com.example.data.model.PlayStatus
import com.example.data.model.PlayabilityFilter
import com.example.data.model.PublicReview
import com.example.data.model.SteamDlcRecommendation
import com.example.data.model.StorageFilter
import com.example.data.model.TrophyRarityTier
import com.example.data.model.UnifiedLibraryItem
import com.example.data.model.UpdateDownloadState
import com.example.data.model.UserProfile
import com.example.data.remote.GithubReleaseClient
import com.example.data.repository.GiveawaysRepository
import com.example.notifications.AutoBackupScheduler
import com.example.notifications.NotificationHelper
import com.example.notifications.NotificationScheduler
import com.example.preferences.AccentTheme
import com.example.preferences.AppPreferences
import com.example.preferences.BackgroundTheme
import com.example.preferences.CardDensity
import com.example.preferences.DpiScaleMode
import com.example.preferences.TargetFrameRate
import com.example.i18n.AppLanguage
import com.example.i18n.AppStringsRegistry
import com.example.i18n.LocaleSyncHelper
import com.example.preferences.ThemeMode
import com.example.util.BacklogChallengeManager
import com.example.util.BackupRestoreManager
import com.example.util.CloudBackupManager
import com.example.util.CuratedAchievementsCatalog
import com.example.util.CustomFontHelper
import com.example.util.MythicAchievementNotificationManager
import com.example.util.PublicReviewsManager
import com.example.util.SteamDlcRecommender
import com.example.util.SupabaseKeepAliveManager
import com.example.util.UserProfileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class GiveawaysViewModel(application: Application) : AndroidViewModel(application) {

    typealias OwnershipStatus = GiveawayOwnershipStatus

    val notificationHelper = NotificationHelper(application)
    val preferences = AppPreferences(application)
    private val database = AppDatabase.getInstance(application)
    private val repository = GiveawaysRepository(
        giveawayDao = database.giveawayDao(),
        notificationHelper = notificationHelper,
        steamGameDao = database.steamGameDao(),
        steamAchievementDao = database.steamAchievementDao()
    )
    private val updateDelegate = AppUpdateDelegate(
        appContext = application,
        preferences = preferences,
        githubReleaseClient = GithubReleaseClient(application),
        notificationHelper = notificationHelper
    )

    // --- Navigation & Filtering State ---
    private val _selectedTab = MutableStateFlow(preferences.defaultTab)
    val selectedTab: StateFlow<MainTab> = _selectedTab.asStateFlow()

    private val _selectedLaunchers = MutableStateFlow(
        if (preferences.defaultLauncher == LauncherType.ALL) emptySet() else setOf(preferences.defaultLauncher)
    )
    val selectedLaunchers: StateFlow<Set<LauncherType>> = _selectedLaunchers.asStateFlow()

    val selectedLauncher: StateFlow<LauncherType> = _selectedLaunchers.map { set ->
        if (set.isEmpty() || set.contains(LauncherType.ALL)) LauncherType.ALL else set.first()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LauncherType.ALL)

    private val _selectedTypeFilter = MutableStateFlow(TypeFilter.ALL)
    val selectedTypeFilter: StateFlow<TypeFilter> = _selectedTypeFilter.asStateFlow()

    private val _selectedSort = MutableStateFlow(SortOption.NEWEST)
    val selectedSort: StateFlow<SortOption> = _selectedSort.asStateFlow()

    private val _selectedLibraryPlayFilter = MutableStateFlow(preferences.libraryPlayFilter)
    val selectedLibraryPlayFilter: StateFlow<LibraryPlayFilter> = _selectedLibraryPlayFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategories = MutableStateFlow<Set<GameCategory>>(emptySet())
    val selectedCategories: StateFlow<Set<GameCategory>> = _selectedCategories.asStateFlow()

    private val _isFilterSheetOpen = MutableStateFlow(false)
    val isFilterSheetOpen: StateFlow<Boolean> = _isFilterSheetOpen.asStateFlow()

    private val _isCategorySheetOpen = MutableStateFlow(false)
    val isCategorySheetOpen: StateFlow<Boolean> = _isCategorySheetOpen.asStateFlow()

    private val _selectedPlayabilityFilter = MutableStateFlow(PlayabilityFilter.ALL)
    val selectedPlayabilityFilter: StateFlow<PlayabilityFilter> = _selectedPlayabilityFilter.asStateFlow()

    private val _selectedStorageFilter = MutableStateFlow(StorageFilter.ALL)
    val selectedStorageFilter: StateFlow<StorageFilter> = _selectedStorageFilter.asStateFlow()

    val activeFilterCount: StateFlow<Int> = combine(
        _selectedLaunchers, _selectedTypeFilter, _selectedCategories,
        _selectedSort, _selectedPlayabilityFilter, _selectedStorageFilter
    ) { args: Array<Any> ->
        @Suppress("UNCHECKED_CAST")
        val launchers = args[0] as Set<LauncherType>
        val typeFilter = args[1] as TypeFilter
        @Suppress("UNCHECKED_CAST")
        val categories = args[2] as Set<GameCategory>
        val sort = args[3] as SortOption
        val playability = args[4] as PlayabilityFilter
        val storage = args[5] as StorageFilter
        var count = 0
        if (launchers.isNotEmpty() && !launchers.contains(LauncherType.ALL)) count += launchers.size
        if (typeFilter != TypeFilter.ALL) count++
        if (categories.isNotEmpty()) count += categories.size
        if (sort != SortOption.NEWEST) count++
        if (playability != PlayabilityFilter.ALL) count++
        if (storage != StorageFilter.ALL) count++
        count
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- Steam, Loading, Detail & Update Flows ---
    private val _steamUserId = MutableStateFlow(preferences.steamUserId)
    val steamUserId: StateFlow<String> = _steamUserId.asStateFlow()

    private val _steamApiKey = MutableStateFlow(preferences.steamApiKey)
    val steamApiKey: StateFlow<String> = _steamApiKey.asStateFlow()

    private val _steamSyncStatus = MutableStateFlow<String?>(null)
    val steamSyncStatus: StateFlow<String?> = _steamSyncStatus.asStateFlow()

    private val _isSyncingSteam = MutableStateFlow(false)
    val isSyncingSteam: StateFlow<Boolean> = _isSyncingSteam.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _detailItem = MutableStateFlow<GiveawayItem?>(null)
    val detailItem: StateFlow<GiveawayItem?> = _detailItem.asStateFlow()

    private val _appUpdateInfo = MutableStateFlow<AppUpdateInfo?>(null)
    val appUpdateInfo: StateFlow<AppUpdateInfo?> = _appUpdateInfo.asStateFlow()

    private val _isCheckingUpdates = MutableStateFlow(false)
    val isCheckingUpdates: StateFlow<Boolean> = _isCheckingUpdates.asStateFlow()

    private val _updateCheckStatus = MutableStateFlow<String?>(null)
    val updateCheckStatus: StateFlow<String?> = _updateCheckStatus.asStateFlow()

    private val _isUpdateDialogVisible = MutableStateFlow(false)
    val isUpdateDialogVisible: StateFlow<Boolean> = _isUpdateDialogVisible.asStateFlow()

    private val _updateDownloadState = MutableStateFlow<UpdateDownloadState>(UpdateDownloadState.Idle)
    val updateDownloadState: StateFlow<UpdateDownloadState> = _updateDownloadState.asStateFlow()

    // --- Theme, Appearance & Behavior Flows ---
    private val _themeMode = MutableStateFlow(preferences.themeMode)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _accentTheme = MutableStateFlow(preferences.accentTheme)
    val accentTheme: StateFlow<AccentTheme> = _accentTheme.asStateFlow()

    private val _darkBackgroundTheme = MutableStateFlow(preferences.darkBackgroundTheme)
    val darkBackgroundTheme: StateFlow<BackgroundTheme> = _darkBackgroundTheme.asStateFlow()

    private val _lightBackgroundTheme = MutableStateFlow(preferences.lightBackgroundTheme)
    val lightBackgroundTheme: StateFlow<BackgroundTheme> = _lightBackgroundTheme.asStateFlow()

    private val _customFontPath = MutableStateFlow(
        preferences.customFontPath.takeIf { it.isNotBlank() && File(it).exists() }
            .also { valid ->
                if (preferences.customFontPath.isNotBlank() && valid == null) {
                    preferences.customFontPath = ""
                    preferences.customFontName = ""
                }
            } ?: ""
    )
    val customFontPath: StateFlow<String> = _customFontPath.asStateFlow()

    private val _customFontName = MutableStateFlow(if (_customFontPath.value.isNotBlank()) preferences.customFontName else "")
    val customFontName: StateFlow<String> = _customFontName.asStateFlow()

    private val _cardDensity = MutableStateFlow(preferences.cardDensity)
    val cardDensity: StateFlow<CardDensity> = _cardDensity.asStateFlow()

    private val _defaultLauncher = MutableStateFlow(preferences.defaultLauncher)
    val defaultLauncher: StateFlow<LauncherType> = _defaultLauncher.asStateFlow()

    private val _defaultTab = MutableStateFlow(preferences.defaultTab)
    val defaultTab: StateFlow<MainTab> = _defaultTab.asStateFlow()

    private val _isShowOriginalPrice = MutableStateFlow(preferences.isShowOriginalPrice)
    val isShowOriginalPrice: StateFlow<Boolean> = _isShowOriginalPrice.asStateFlow()

    private val _isShowExpiration = MutableStateFlow(preferences.isShowExpiration)
    val isShowExpiration: StateFlow<Boolean> = _isShowExpiration.asStateFlow()

    private val _isConfirmToastOnClaim = MutableStateFlow(preferences.isConfirmToastOnClaim)
    val isConfirmToastOnClaim: StateFlow<Boolean> = _isConfirmToastOnClaim.asStateFlow()

    private val _isUseInAppBrowser = MutableStateFlow(preferences.isUseInAppBrowser)
    val isUseInAppBrowser: StateFlow<Boolean> = _isUseInAppBrowser.asStateFlow()

    private val _inAppBrowserUrl = MutableStateFlow<String?>(null)
    val inAppBrowserUrl: StateFlow<String?> = _inAppBrowserUrl.asStateFlow()

    private val _inAppBrowserTitle = MutableStateFlow("")
    val inAppBrowserTitle: StateFlow<String> = _inAppBrowserTitle.asStateFlow()

    private val _isAutoCheckOnReopen = MutableStateFlow(preferences.isAutoCheckOnReopen)
    val isAutoCheckOnReopen: StateFlow<Boolean> = _isAutoCheckOnReopen.asStateFlow()

    private val _isAutoCheckGithubUpdates = MutableStateFlow(preferences.isAutoCheckGithubUpdates)
    val isAutoCheckGithubUpdates: StateFlow<Boolean> = _isAutoCheckGithubUpdates.asStateFlow()

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

    private val _appLanguage = MutableStateFlow(preferences.appLanguage)
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    private val _hasSelectedInitialLanguage = MutableStateFlow(preferences.hasSelectedInitialLanguage)
    val hasSelectedInitialLanguage: StateFlow<Boolean> = _hasSelectedInitialLanguage.asStateFlow()

    private val _showOnboardingWalkthrough = MutableStateFlow(!preferences.hasSeenOnboarding)
    val showOnboardingWalkthrough: StateFlow<Boolean> = _showOnboardingWalkthrough.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfileManager.getProfile(preferences))
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _reopenStatusMessage = MutableStateFlow<String?>(null)
    val reopenStatusMessage: StateFlow<String?> = _reopenStatusMessage.asStateFlow()

    private var lastRefreshTimestamp: Long = 0L

    // --- Notification Flows ---
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

    private val _isNotifyAndroid = MutableStateFlow(notificationHelper.isNotifyAndroid)
    val isNotifyAndroid: StateFlow<Boolean> = _isNotifyAndroid.asStateFlow()

    private val _isNotifyIos = MutableStateFlow(notificationHelper.isNotifyIos)
    val isNotifyIos: StateFlow<Boolean> = _isNotifyIos.asStateFlow()

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
        database.notificationHistoryDao().getAllNotificationsFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationCount: StateFlow<Int> =
        database.notificationHistoryDao().getUnreadCountFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _libraryViewMode = MutableStateFlow(preferences.libraryViewMode)
    val libraryViewMode: StateFlow<LibraryViewMode> = _libraryViewMode.asStateFlow()

    // --- Core Giveaway & Steam Library Flows ---
    val allGiveaways: StateFlow<List<GiveawayItem>> = repository.allGiveawaysFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSteamGames: StateFlow<List<SteamGameEntity>> = repository.allSteamGamesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentPlayedSteamGames: StateFlow<List<SteamGameEntity>> = allSteamGames.map { games ->
        val played = games.filter { it.playtimeMinutes > 0 || it.lastPlayedTimestamp > 0 }
            .sortedWith(
                compareByDescending<SteamGameEntity> { it.lastPlayedTimestamp }
                    .thenByDescending { it.playtime2WeeksMinutes }
                    .thenByDescending { it.playtimeMinutes }
            )
        if (played.isNotEmpty()) played.take(3) else games.take(3)
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val top3FreeGamesToday: StateFlow<List<GiveawayItem>> = allGiveaways.map { list ->
        val active = list.filter { it.status.equals("Active", ignoreCase = true) }
        val source = active.ifEmpty { list }
        source.sortedWith(
            compareByDescending<GiveawayItem> { it.metacriticScore }
                .thenByDescending { it.numericWorth }
                .thenByDescending { it.users }
        ).take(3)
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredSteamGames: StateFlow<List<SteamGameEntity>> = combine(
        allSteamGames, _selectedLibraryPlayFilter, _searchQuery, _selectedPlayabilityFilter, _selectedStorageFilter
    ) { steamGames, playFilter, query, playability, storage ->
        var list = steamGames
        if (playFilter.status != null) list = list.filter { it.playStatus == playFilter.status }
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter { it.name.lowercase().contains(q) }
        }
        list = when (playability) {
            PlayabilityFilter.ALL -> list
            PlayabilityFilter.DECK_VERIFIED -> list.filter { it.hardwareProfile.isDeckVerified }
            PlayabilityFilter.CONTROLLER_FRIENDLY -> list.filter { it.hardwareProfile.isControllerFriendly }
            PlayabilityFilter.QUICK_SESSION -> list.filter { it.hardwareProfile.isQuickSession }
        }
        if (storage != StorageFilter.ALL && storage.tier != null) {
            list = list.filter { storage.tier.matches(it.hardwareProfile.effectiveStorageGb) }
        }
        list
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rarestAchievements: StateFlow<List<SteamAchievementEntity>> = repository.rarestAchievementsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val closestToHundredGames: StateFlow<List<SteamGameEntity>> = allSteamGames.map { games ->
        games.filter { it.achievementsTotal > 0 }
            .sortedWith(
                compareByDescending<SteamGameEntity> { it.achievementsPercentage }
                    .thenBy { it.achievementsTotal - it.achievementsUnlocked }
                    .thenByDescending { it.playtimeMinutes }
            )
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievementOverviewStats: StateFlow<AchievementOverviewStats> = combine(rarestAchievements, allSteamGames) { trophies, games ->
        GiveawayFilterEngine.computeAchievementOverviewStats(trophies, games)
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AchievementOverviewStats())

    private val _selectedGameForAchievements = MutableStateFlow<SteamGameEntity?>(null)
    val selectedGameForAchievements: StateFlow<SteamGameEntity?> = _selectedGameForAchievements.asStateFlow()

    private val _selectedGameAchievements = MutableStateFlow<List<SteamAchievementEntity>>(emptyList())
    val selectedGameAchievements: StateFlow<List<SteamAchievementEntity>> = _selectedGameAchievements.asStateFlow()

    private val _isLoadingGameAchievements = MutableStateFlow(false)
    val isLoadingGameAchievements: StateFlow<Boolean> = _isLoadingGameAchievements.asStateFlow()

    val allUnifiedLibraryItems: StateFlow<List<UnifiedLibraryItem>> = combine(allGiveaways, allSteamGames) { giveaways, steamGames ->
        giveaways.filter { it.isClaimed }.map { UnifiedLibraryItem.Vault(it) } + steamGames.map { UnifiedLibraryItem.Steam(it) }
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Backlog Challenge State ---
    private val _selectedBacklogWeek = MutableStateFlow(BacklogChallengeManager.getCurrentWeekKey())
    val selectedBacklogWeek: StateFlow<String> = _selectedBacklogWeek.asStateFlow()
    val selectedBacklogMonth: StateFlow<String> = selectedBacklogWeek

    private val _isBacklogChallengeDialogVisible = MutableStateFlow(false)
    val isBacklogChallengeDialogVisible: StateFlow<Boolean> = _isBacklogChallengeDialogVisible.asStateFlow()

    private val _backlogGridRefreshTrigger = MutableStateFlow(0)

    val backlogChallengeGrid: StateFlow<BacklogChallengeGrid> = combine(
        allUnifiedLibraryItems, rarestAchievements, _selectedBacklogWeek, _backlogGridRefreshTrigger, _appLanguage
    ) { games, trophies, weekKey, _, lang ->
        BacklogChallengeManager.getOrGenerateGrid(preferences, weekKey, games, trophies, lang)
    }.flowOn(Dispatchers.Default).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        BacklogChallengeManager.getOrGenerateGrid(preferences, language = preferences.appLanguage)
    )

    val mythicAchievementNotification: StateFlow<SteamAchievementEntity?> =
        MythicAchievementNotificationManager.pendingNotification

    val uiStats: StateFlow<UiStats> = combine(allGiveaways, allSteamGames) { all, steamGames ->
        GiveawayFilterEngine.computeUiStats(all, steamGames)
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiStats())

    val discoverGiveaways: StateFlow<List<GiveawayItem>> = combine(
        allGiveaways, _selectedLaunchers, _selectedTypeFilter, _selectedCategories,
        _selectedSort, _searchQuery, _selectedPlayabilityFilter, _selectedStorageFilter
    ) { args: Array<Any> ->
        @Suppress("UNCHECKED_CAST")
        GiveawayFilterEngine.filterAndSortList(
            source = args[0] as List<GiveawayItem>,
            launchers = args[1] as Set<LauncherType>,
            typeFilter = args[2] as TypeFilter,
            categories = args[3] as Set<GameCategory>,
            sort = args[4] as SortOption,
            query = args[5] as String,
            playability = args[6] as PlayabilityFilter,
            storage = args[7] as StorageFilter
        )
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val claimedGiveaways: StateFlow<List<GiveawayItem>> = combine(
        allGiveaways, _selectedLaunchers, _selectedTypeFilter, _selectedCategories,
        _selectedSort, _searchQuery, _selectedLibraryPlayFilter, _selectedPlayabilityFilter, _selectedStorageFilter
    ) { args: Array<Any> ->
        @Suppress("UNCHECKED_CAST")
        val all = args[0] as List<GiveawayItem>
        @Suppress("UNCHECKED_CAST")
        val launchers = args[1] as Set<LauncherType>
        val typeFilter = args[2] as TypeFilter
        @Suppress("UNCHECKED_CAST")
        val categories = args[3] as Set<GameCategory>
        val sort = args[4] as SortOption
        val query = args[5] as String
        val playFilter = args[6] as LibraryPlayFilter
        val playability = args[7] as PlayabilityFilter
        val storage = args[8] as StorageFilter
        val claimed = all.filter { it.isClaimed }
        val statusFiltered = if (playFilter.status == null) claimed else claimed.filter { it.playStatus == playFilter.status }
        GiveawayFilterEngine.filterAndSortList(statusFiltered, launchers, typeFilter, categories, sort, query, playability, storage)
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val displayedGiveaways: StateFlow<List<GiveawayItem>> = combine(_selectedTab, discoverGiveaways, claimedGiveaways) { tab, discover, claimed ->
        when (tab) {
            MainTab.HOME, MainTab.DISCOVER, MainTab.CALENDAR -> discover
            MainTab.CLAIMED -> claimed
            MainTab.SETTINGS -> emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Storage, Reviews & Steam DLC Flows ---
    private val _storageMetrics = MutableStateFlow(StorageMetrics(0L, 0L, 0L, 0L))
    val storageMetrics: StateFlow<StorageMetrics> = _storageMetrics.asStateFlow()

    private val _storageFootprintBytes = MutableStateFlow(0L)
    val storageFootprintBytes: StateFlow<Long> = _storageFootprintBytes.asStateFlow()

    private val _publicReviews = MutableStateFlow<List<PublicReview>>(emptyList())
    val publicReviews: StateFlow<List<PublicReview>> = _publicReviews.asStateFlow()

    private val _storeDlcRecommendations = MutableStateFlow<List<SteamDlcRecommendation>>(emptyList())
    private val _dismissedDlcIds = MutableStateFlow(preferences.dismissedDlcAppIds.mapNotNull { it.toIntOrNull() }.toSet())

    private val _isLoadingSteamDlcs = MutableStateFlow(false)
    val isLoadingSteamDlcs: StateFlow<Boolean> = _isLoadingSteamDlcs.asStateFlow()

    private val _steamDlcScanStatus = MutableStateFlow<String?>(null)
    val steamDlcScanStatus: StateFlow<String?> = _steamDlcScanStatus.asStateFlow()

    private val _selectedGameForDlcs = MutableStateFlow<SteamGameEntity?>(null)
    val selectedGameForDlcs: StateFlow<SteamGameEntity?> = _selectedGameForDlcs.asStateFlow()

    private val _selectedGameDlcs = MutableStateFlow<List<SteamDlcRecommendation>>(emptyList())
    val selectedGameDlcs: StateFlow<List<SteamDlcRecommendation>> = _selectedGameDlcs.asStateFlow()

    private val _isLoadingGameDlcs = MutableStateFlow(false)
    val isLoadingGameDlcs: StateFlow<Boolean> = _isLoadingGameDlcs.asStateFlow()

    val steamDlcRecommendations: StateFlow<List<SteamDlcRecommendation>> = combine(
        allGiveaways, allSteamGames, _storeDlcRecommendations, _dismissedDlcIds
    ) { giveaways, steamGames, storeDlcs, dismissedIds ->
        DlcAndReviewsDelegate.mergeDlcRecommendations(giveaways, steamGames, storeDlcs, dismissedIds)
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Backup & Cloud Sync State ---
    private val _backupRestoreStatus = MutableStateFlow<String?>(null)
    val backupRestoreStatus: StateFlow<String?> = _backupRestoreStatus.asStateFlow()

    private val _isBackingUpOrRestoring = MutableStateFlow(false)
    val isBackingUpOrRestoring: StateFlow<Boolean> = _isBackingUpOrRestoring.asStateFlow()

    private val _isAutoBackupEnabled = MutableStateFlow(preferences.isAutoBackupEnabled)
    val isAutoBackupEnabled: StateFlow<Boolean> = _isAutoBackupEnabled.asStateFlow()

    private val _autoBackupFolderUri = MutableStateFlow(preferences.autoBackupFolderUri)
    val autoBackupFolderUri: StateFlow<String> = _autoBackupFolderUri.asStateFlow()

    private val _autoBackupFolderDisplayName = MutableStateFlow(preferences.autoBackupFolderDisplayName)
    val autoBackupFolderDisplayName: StateFlow<String> = _autoBackupFolderDisplayName.asStateFlow()

    private val _autoBackupIntervalDays = MutableStateFlow(preferences.autoBackupIntervalDays)
    val autoBackupIntervalDays: StateFlow<Int> = _autoBackupIntervalDays.asStateFlow()

    private val _lastAutoBackupTimestamp = MutableStateFlow(preferences.lastAutoBackupTimestamp)
    val lastAutoBackupTimestamp: StateFlow<Long> = _lastAutoBackupTimestamp.asStateFlow()

    private val _lastAutoBackupStatus = MutableStateFlow(preferences.lastAutoBackupStatus)
    val lastAutoBackupStatus: StateFlow<String> = _lastAutoBackupStatus.asStateFlow()

    private val _isAutoBackingUp = MutableStateFlow(false)
    val isAutoBackingUp: StateFlow<Boolean> = _isAutoBackingUp.asStateFlow()

    val isCloudBackupConfigured: Boolean get() = CloudBackupManager.isConfigured()
    val cloudBackupProviderHost: String get() = CloudBackupManager.getProviderHost()
    val cloudAccountEmail: String get() = preferences.cloudAccountEmail
    val lastRememberedCloudEmail: String get() = preferences.lastCloudAccountEmail
    val isCloudSessionActive: Boolean
        get() = preferences.cloudAccountToken.isNotBlank() ||
            preferences.cloudAccountRefreshToken.isNotBlank() ||
            (preferences.isCloudAccountLoggedIn && CloudBackupManager.resolveCloudBackupUrl().isNotBlank())

    private val _isCloudAccountLoggedIn = MutableStateFlow(preferences.isCloudAccountLoggedIn)
    val isCloudAccountLoggedIn: StateFlow<Boolean> = _isCloudAccountLoggedIn.asStateFlow()

    private val _cloudAccountUsername = MutableStateFlow(preferences.cloudAccountUsername)
    val cloudAccountUsername: StateFlow<String> = _cloudAccountUsername.asStateFlow()

    private val _isCloudAuthenticating = MutableStateFlow(false)
    val isCloudAuthenticating: StateFlow<Boolean> = _isCloudAuthenticating.asStateFlow()

    private val _isCloudBackingUp = MutableStateFlow(false)
    val isCloudBackingUp: StateFlow<Boolean> = _isCloudBackingUp.asStateFlow()

    private val _isCloudRestoring = MutableStateFlow(false)
    val isCloudRestoring: StateFlow<Boolean> = _isCloudRestoring.asStateFlow()

    private val _cloudBackupStatus = MutableStateFlow<String?>(preferences.lastCloudBackupStatus.ifBlank { null })
    val cloudBackupStatus: StateFlow<String?> = _cloudBackupStatus.asStateFlow()

    private val _lastCloudBackupTimestamp = MutableStateFlow(preferences.lastCloudBackupTimestamp)
    val lastCloudBackupTimestamp: StateFlow<Long> = _lastCloudBackupTimestamp.asStateFlow()

    private val _isCloudAutoBackupEnabled = MutableStateFlow(preferences.isCloudAutoBackupEnabled)
    val isCloudAutoBackupEnabled: StateFlow<Boolean> = _isCloudAutoBackupEnabled.asStateFlow()

    private val _isCloudSessionActive = MutableStateFlow(isCloudSessionActive)
    val isCloudSessionActiveFlow: StateFlow<Boolean> = _isCloudSessionActive.asStateFlow()

    private val _lastSupabaseKeepAliveTimestamp = MutableStateFlow(preferences.lastSupabaseKeepAliveTimestamp)
    val lastSupabaseKeepAliveTimestamp: StateFlow<Long> = _lastSupabaseKeepAliveTimestamp.asStateFlow()

    private val _lastSupabaseKeepAliveStatus = MutableStateFlow(preferences.lastSupabaseKeepAliveStatus)
    val lastSupabaseKeepAliveStatus: StateFlow<String> = _lastSupabaseKeepAliveStatus.asStateFlow()

    private val _isPingingSupabase = MutableStateFlow(false)
    val isPingingSupabase: StateFlow<Boolean> = _isPingingSupabase.asStateFlow()

    init {
        runCatching { refreshGiveaways() }
        runCatching { updateStorageFootprint() }
        runCatching { refreshPublicReviews() }
        runCatching { checkGithubReleases(isManual = false) }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                _storeDlcRecommendations.value = SteamDlcRecommender.loadCachedDlcs(preferences)
            }
            runCatching { NotificationScheduler.schedulePeriodicSync(application) }
            runCatching { AutoBackupScheduler.schedulePeriodicAutoBackup(application) }
            runCatching { checkAndTriggerAutoBackupIfNeeded(application) }
            runCatching { checkAndTriggerCloudAutoBackupIfNeeded(application) }
            runCatching {
                SupabaseKeepAliveManager.pingIfDue(application, preferences)?.onSuccess {
                    _lastSupabaseKeepAliveTimestamp.value = preferences.lastSupabaseKeepAliveTimestamp
                    _lastSupabaseKeepAliveStatus.value = preferences.lastSupabaseKeepAliveStatus
                }
            }
            runCatching { MythicAchievementNotificationManager.checkPendingFromPreferences(application) }
            runCatching { checkWeeklyBacklogRollover() }
            runCatching {
                val curatedAppIds = listOf(1091500, 620, 367520, 1145360, 264710, 1794680, 413150)
                database.steamGameDao().deleteByAppIds(curatedAppIds)
                database.steamAchievementDao().deleteByAppIds(curatedAppIds)
                if (_steamUserId.value.isBlank()) {
                    database.steamGameDao().clearAll()
                    database.steamAchievementDao().clearAll()
                } else if (!SteamDlcRecommender.isCacheFresh(preferences) && database.steamGameDao().getAllSteamGamesList().isNotEmpty()) {
                    refreshSteamDlcRecommendations(forceRefresh = false)
                }
            }
        }
    }

    // --- Backlog & Trophy Actions ---
    fun setBacklogChallengeDialogVisible(visible: Boolean) { _isBacklogChallengeDialogVisible.value = visible }

    fun selectBacklogWeek(weekKey: String) {
        val currentWeek = BacklogChallengeManager.getCurrentWeekKey()
        val diffFromCurrent = BacklogChallengeManager.getWeeksBetween(currentWeek, weekKey)
        val diffFromAnchor = BacklogChallengeManager.getWeeksBetween(BacklogChallengeManager.ANCHOR_WEEK_KEY, weekKey)
        if (diffFromAnchor >= 0 && diffFromCurrent <= BacklogChallengeManager.MAX_FUTURE_WEEKS_PREVIEW) {
            _selectedBacklogWeek.value = weekKey
        }
    }

    fun selectBacklogMonth(monthKey: String) = selectBacklogWeek(monthKey)

    fun checkWeeklyBacklogRollover() {
        val currentWeek = BacklogChallengeManager.getCurrentWeekKey()
        val lastWeek = preferences.lastActiveBacklogWeekKey
        if (lastWeek.isNotBlank() && lastWeek != currentWeek) {
            preferences.lastActiveBacklogWeekKey = currentWeek
            _selectedBacklogWeek.value = currentWeek
            _backlogGridRefreshTrigger.value += 1
            notificationHelper.sendWeeklyBacklogChallengeNotification(
                BacklogChallengeManager.getWeekDisplayName(currentWeek, _appLanguage.value)
            )
        } else if (lastWeek.isBlank()) {
            preferences.lastActiveBacklogWeekKey = currentWeek
        }
    }

    fun checkMonthlyBacklogRollover() = checkWeeklyBacklogRollover()

    fun refreshBacklogChallengeGrid() {
        checkWeeklyBacklogRollover()
        _backlogGridRefreshTrigger.value += 1
    }

    fun toggleBacklogTileManual(tileIndex: Int, weekKey: String = _selectedBacklogWeek.value): Boolean {
        val success = BacklogChallengeManager.toggleManualOverride(preferences, weekKey, tileIndex)
        if (success) {
            _backlogGridRefreshTrigger.value += 1
            runCatching { com.example.widget.WidgetUpdateHelper.updateAllWidgets(getApplication()) }
        }
        return success
    }

    fun claimBacklogMilestones(onClaimed: (List<BacklogMilestoneReward>) -> Unit = {}) {
        viewModelScope.launch {
            val newlyAwarded = BacklogChallengeManager.checkAndAwardMilestones(backlogChallengeGrid.value, database, preferences)
            if (newlyAwarded.isNotEmpty()) {
                _backlogGridRefreshTrigger.value += 1
                runCatching { com.example.widget.WidgetUpdateHelper.updateAllWidgets(getApplication()) }
                onClaimed(newlyAwarded)
            }
        }
    }

    fun openGameAchievements(game: SteamGameEntity) {
        _selectedGameForAchievements.value = game
        loadAchievementsForGame(game)
    }

    fun closeGameAchievements() {
        _selectedGameForAchievements.value = null
        _selectedGameAchievements.value = emptyList()
    }

    fun dismissMythicToast() = MythicAchievementNotificationManager.dismissNotification(getApplication())

    fun previewMythicUnlockToast(customAch: SteamAchievementEntity? = null) {
        val ach = customAch ?: CuratedAchievementsCatalog.getInitialAchievements()
            .firstOrNull { it.rarityTier == TrophyRarityTier.MYTHIC }
            ?: SteamAchievementEntity(
                appId = 1091500, apiName = "ACH_CP_AUTODIDACT", displayName = "Autodidact",
                description = "Develop any skill to the maximum level in Night City.",
                isAchieved = true, globalPercentage = 4.2f, gameName = "Cyberpunk 2077"
            )
        MythicAchievementNotificationManager.postMythicUnlock(ach, getApplication())
    }

    fun openAchievementsForAppId(appId: Int, gameName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val game = database.steamGameDao().getSteamGameById(appId) ?: SteamGameEntity(
                appId = appId, name = gameName.ifBlank { "Steam Game" }, playtimeMinutes = 60,
                lastPlayedTimestamp = System.currentTimeMillis() / 1000L, playStatus = PlayStatus.PLAYING
            )
            withContext(Dispatchers.Main) { openGameAchievements(game) }
        }
    }

    fun loadAchievementsForGame(game: SteamGameEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingGameAchievements.value = true
            SteamLibrarySyncDelegate.loadAchievementsForGame(
                context = getApplication(), database = database, repository = repository,
                game = game, userId = _steamUserId.value, steamApiKey = _steamApiKey.value
            ) { _selectedGameAchievements.value = it }
            _isLoadingGameAchievements.value = false
        }
    }

    // --- DLC & Reviews Actions ---
    fun refreshSteamDlcRecommendations(forceRefresh: Boolean = true) {
        if (_isLoadingSteamDlcs.value) return
        viewModelScope.launch(Dispatchers.IO) {
            DlcAndReviewsDelegate.scanLibraryDlcs(
                database = database, preferences = preferences, forceRefresh = forceRefresh,
                dismissedIds = _dismissedDlcIds.value,
                onCached = { _storeDlcRecommendations.value = it },
                onLoading = { _isLoadingSteamDlcs.value = it },
                onStatus = { _steamDlcScanStatus.value = it }
            )
        }
    }

    fun openGameDlcExplorer(game: SteamGameEntity) {
        _selectedGameForDlcs.value = game
        _selectedGameDlcs.value = steamDlcRecommendations.value.filter { it.parentAppId == game.appId }
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingGameDlcs.value = true
            try {
                DlcAndReviewsDelegate.loadGameDlcs(
                    database = database, preferences = preferences, game = game,
                    allGiveaways = allGiveaways.value, dismissedIds = _dismissedDlcIds.value,
                    existingStoreDlcs = _storeDlcRecommendations.value,
                    onGameDlcs = { _selectedGameDlcs.value = it },
                    onStoreDlcsUpdated = { _storeDlcRecommendations.value = it }
                )
            } finally {
                _isLoadingGameDlcs.value = false
            }
        }
    }

    fun closeGameDlcExplorer() {
        _selectedGameForDlcs.value = null
        _selectedGameDlcs.value = emptyList()
        _isLoadingGameDlcs.value = false
    }

    fun dismissDlcRecommendation(dlcAppId: Int) {
        preferences.dismissDlcAppId(dlcAppId)
        _dismissedDlcIds.value += dlcAppId
        _selectedGameDlcs.value = _selectedGameDlcs.value.filterNot { it.dlcAppId == dlcAppId }
    }

    fun openDlcRecommendation(context: Context, dlc: SteamDlcRecommendation) {
        val matched = dlc.matchedGiveawayId?.let { id -> allGiveaways.value.firstOrNull { it.id == id } }
        if (matched != null) {
            openDetail(matched)
            return
        }
        val url = dlc.storeUrl.ifBlank { "https://store.steampowered.com/app/${dlc.dlcAppId}" }
        if (_isUseInAppBrowser.value) openInAppBrowser(url, dlc.name) else launchExternalBrowser(context, url)
    }

    fun refreshPublicReviews() {
        viewModelScope.launch(Dispatchers.IO) { _publicReviews.value = PublicReviewsManager.fetchReviews(getApplication()) }
    }

    fun getReviewsForGame(giveawayId: Long, gameTitle: String): List<PublicReview> =
        DlcAndReviewsDelegate.filterReviewsForGame(_publicReviews.value, giveawayId, gameTitle)

    fun getReviewsForGame(giveawayId: Int, gameTitle: String): List<PublicReview> = getReviewsForGame(giveawayId.toLong(), gameTitle)

    fun getAppReviews(): List<PublicReview> = _publicReviews.value.filter { it.isForApp }

    fun postAppReview(rating: Int, title: String, comment: String, onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        val s = AppStringsRegistry.forLanguage(_appLanguage.value)
        viewModelScope.launch(Dispatchers.IO) {
            DlcAndReviewsDelegate.submitReview(
                context = getApplication(), giveawayId = -1L, gameTitle = "LootDrop App",
                authorName = preferences.userProfileName.ifBlank { "Gamer" }, authorTag = "LootDrop Player",
                rating = rating, title = title, comment = comment, platform = "Android",
                isVerifiedClaimer = true, isAppReview = true,
                successMessage = s.dialogs.reviewsPostedToast,
                onSuccess = { review ->
                    _publicReviews.value = listOf(review) + _publicReviews.value.filterNot { it.id == review.id }
                },
                onComplete = onComplete
            )
        }
    }

    fun postPublicReview(giveawayId: Long, gameTitle: String, rating: Int, title: String, comment: String, onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        val s = AppStringsRegistry.forLanguage(_appLanguage.value)
        viewModelScope.launch(Dispatchers.IO) {
            val isClaimed = claimedGiveaways.value.any { it.id == giveawayId.toInt() }
            DlcAndReviewsDelegate.submitReview(
                context = getApplication(), giveawayId = giveawayId, gameTitle = gameTitle,
                authorName = preferences.userProfileName.ifBlank { "Gamer" },
                authorTag = if (isClaimed) s.dialogs.reviewsVerifiedClaimer else "Community",
                rating = rating, title = title, comment = comment, platform = "Android",
                isVerifiedClaimer = isClaimed, isAppReview = false,
                successMessage = s.dialogs.reviewsPostedToast,
                onSuccess = { review ->
                    _publicReviews.value = listOf(review) + _publicReviews.value.filterNot { it.id == review.id }
                },
                onComplete = onComplete
            )
        }
    }

    fun postPublicReview(giveawayId: Int, gameTitle: String, rating: Int, title: String, comment: String, onComplete: (Boolean, String) -> Unit = { _, _ -> }) =
        postPublicReview(giveawayId.toLong(), gameTitle, rating, title, comment, onComplete)

    fun voteReviewHelpful(reviewId: String, onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        val s = AppStringsRegistry.forLanguage(_appLanguage.value)
        viewModelScope.launch(Dispatchers.IO) {
            PublicReviewsManager.voteHelpful(getApplication(), reviewId).onSuccess { newCount ->
                _publicReviews.value = _publicReviews.value.map { if (it.id == reviewId) it.copy(helpfulCount = newCount) else it }
                withContext(Dispatchers.Main) { onComplete(true, s.dialogs.reviewsVotedHelpfulToast) }
            }.onFailure { err ->
                withContext(Dispatchers.Main) { onComplete(false, err.message ?: s.dialogs.reviewsVoteFailed) }
            }
        }
    }

    fun hasVotedHelpful(reviewId: String): Boolean = PublicReviewsManager.hasVotedHelpful(getApplication(), reviewId)

    // --- Storage, Cache & Refresh Actions ---
    fun updateStorageFootprint() {
        viewModelScope.launch(Dispatchers.IO) {
            val metrics = LootDropApplication.getStorageMetrics(getApplication())
            _storageMetrics.value = metrics
            _storageFootprintBytes.value = metrics.totalFootprintBytes
        }
    }

    fun clearAllCaches() = performDeepClean()

    private inline fun runStorageAction(crossinline block: suspend () -> String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val msg = block()
            updateStorageFootprint()
            _isLoading.value = false
            _reopenStatusMessage.value = msg
        }
    }

    fun performDeepClean() = runStorageAction {
        val s = AppStringsRegistry.forLanguage(_appLanguage.value)
        val reclaimed = repository.clearAllCaches(getApplication())
        if (preferences.isAutoPruneExpired) repository.pruneExpiredGiveaways(getApplication())
        String.format(s.system.vmDeepCleanDoneFormat, _storageMetrics.value.formatBytes(reclaimed))
    }

    fun clearImageCache() = runStorageAction {
        val s = AppStringsRegistry.forLanguage(_appLanguage.value)
        val reclaimed = repository.clearImageCache(getApplication())
        String.format(s.system.vmImageCacheDoneFormat, _storageMetrics.value.formatBytes(reclaimed))
    }

    fun clearNetworkCache() = runStorageAction {
        val s = AppStringsRegistry.forLanguage(_appLanguage.value)
        val reclaimed = repository.clearHttpCache(getApplication())
        String.format(s.system.vmNetworkCacheDoneFormat, _storageMetrics.value.formatBytes(reclaimed))
    }

    fun optimizeDatabase() = runStorageAction {
        val s = AppStringsRegistry.forLanguage(_appLanguage.value)
        val pruned = if (preferences.isAutoPruneExpired) repository.pruneExpiredGiveaways(getApplication()) else 0
        repository.vacuumDatabase(getApplication())
        if (pruned > 0) String.format(s.system.vmDbCompactedPrunedFormat, pruned)
        else s.system.vmDbCompactedDone
    }

    fun refreshGiveaways(isAutoCheck: Boolean = false) {
        viewModelScope.launch {
            val s = AppStringsRegistry.forLanguage(_appLanguage.value)
            _isLoading.value = true
            _errorMessage.value = null
            lastRefreshTimestamp = System.currentTimeMillis()
            val result = repository.refreshGiveaways()
            _isLoading.value = false
            if (result.isFailure) {
                if (!isAutoCheck || allGiveaways.value.isEmpty()) {
                    _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: s.system.vmFetchGamesError
                }
            } else {
                updateStorageFootprint()
                val syncResult = result.getOrNull()
                if (syncResult != null && syncResult.newGamesCount > 0) {
                    val unitWord = if (syncResult.newGamesCount > 1) s.common.newLootPluralSuffix else s.common.newLootSingleSuffix
                    _reopenStatusMessage.value = String.format(s.system.vmFoundNewGamesFormat, syncResult.newGamesCount, unitWord)
                    preferences.isNewTodayDismissed = false
                    _isNewTodayDismissed.value = false
                } else if (isAutoCheck) {
                    _reopenStatusMessage.value = s.system.vmAutoCheckUpToDate
                }
            }
            checkGithubReleases(isManual = false)
        }
    }

    fun checkNewGamesOnAppReopen() {
        if (!preferences.isAutoCheckOnReopen) return
        val now = System.currentTimeMillis()
        if (_isLoading.value || (now - lastRefreshTimestamp < 4000L && lastRefreshTimestamp != 0L)) return
        refreshGiveaways(isAutoCheck = true)
        checkGithubReleases(isManual = false)
    }

    // --- GitHub Releases & Updater ---
    fun setAutoCheckGithubUpdates(enabled: Boolean) { preferences.isAutoCheckGithubUpdates = enabled; _isAutoCheckGithubUpdates.value = enabled }
    fun setUpdateDialogVisible(visible: Boolean) { _isUpdateDialogVisible.value = visible }

    fun dismissUpdateDialog(neverRemindThisVersion: Boolean = false) {
        _isUpdateDialogVisible.value = false
        if (neverRemindThisVersion) _appUpdateInfo.value?.latestVersion?.let { preferences.lastDismissedUpdateTag = it }
    }

    fun startInAppUpdateDownload(updateInfo: AppUpdateInfo, context: Context? = null) {
        val currentState = _updateDownloadState.value
        if (currentState is UpdateDownloadState.Downloading) return
        val installContext = context ?: getApplication()
        if (currentState is UpdateDownloadState.ReadyToInstall &&
            currentState.versionTag == updateInfo.latestVersion &&
            com.example.util.AppUpdateInstaller.hasValidApkFile(currentState.apkFile)
        ) {
            updateDelegate.triggerInstallWithAutoResume(installContext, currentState.apkFile)
            return
        }
        viewModelScope.launch {
            updateDelegate.downloadAndInstallUpdate(updateInfo, installContext) { _updateDownloadState.value = it }
        }
    }

    fun resumePendingUpdateInstallIfPermitted(context: Context) =
        updateDelegate.resumePendingUpdateInstallIfPermitted(context, _updateDownloadState.value)

    fun installDownloadedApk(context: Context) {
        val state = _updateDownloadState.value
        if (state is UpdateDownloadState.ReadyToInstall) updateDelegate.triggerInstallWithAutoResume(context, state.apkFile)
    }

    fun resetUpdateDownloadState() {
        updateDelegate.pendingInstallAfterPermissionGrant = false
        _updateDownloadState.value = UpdateDownloadState.Idle
    }

    fun checkGithubReleases(isManual: Boolean = false) {
        viewModelScope.launch {
            updateDelegate.checkGithubReleases(
                isManual = isManual,
                onChecking = { _isCheckingUpdates.value = it },
                onUpdateInfo = { _appUpdateInfo.value = it },
                onStatus = { _updateCheckStatus.value = it },
                onReopenMessage = { _reopenStatusMessage.value = it }
            )
        }
    }

    // --- Tab, Filter, Sort & Detail Actions ---
    fun clearReopenStatusMessage() { _reopenStatusMessage.value = null }
    fun onAppClosedOrLeft() { _reopenStatusMessage.value = null }
    fun selectTab(tab: MainTab) { _selectedTab.value = tab }

    fun toggleLauncher(launcher: LauncherType) {
        if (launcher == LauncherType.ALL) { _selectedLaunchers.value = emptySet(); return }
        val current = _selectedLaunchers.value.toMutableSet().apply { remove(LauncherType.ALL) }
        if (!current.add(launcher)) current.remove(launcher)
        _selectedLaunchers.value = current
    }

    fun selectLauncher(launcher: LauncherType) = toggleLauncher(launcher)
    fun clearLauncherFilters() { _selectedLaunchers.value = emptySet() }
    fun selectTypeFilter(type: TypeFilter) { _selectedTypeFilter.value = type }

    fun toggleCategoryFilter(category: GameCategory) {
        val current = _selectedCategories.value.toMutableSet()
        if (!current.add(category)) current.remove(category)
        _selectedCategories.value = current
    }

    fun setCategoryFilters(categories: Set<GameCategory>) { _selectedCategories.value = categories }
    fun clearCategoryFilters() { _selectedCategories.value = emptySet() }
    fun setCategorySheetOpen(open: Boolean) { _isCategorySheetOpen.value = open }
    fun selectSort(sort: SortOption) { _selectedSort.value = sort }
    fun setFilterSheetOpen(open: Boolean) { _isFilterSheetOpen.value = open }
    fun setPlayabilityFilter(filter: PlayabilityFilter) { _selectedPlayabilityFilter.value = filter }
    fun setStorageFilter(filter: StorageFilter) { _selectedStorageFilter.value = filter }
    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun openDetail(item: GiveawayItem) { _detailItem.value = item }
    fun closeDetail() { _detailItem.value = null }

    fun resetFilters() {
        _selectedLaunchers.value = emptySet()
        _selectedTypeFilter.value = TypeFilter.ALL
        _selectedCategories.value = emptySet()
        _selectedSort.value = SortOption.NEWEST
        _selectedPlayabilityFilter.value = PlayabilityFilter.ALL
        _selectedStorageFilter.value = StorageFilter.ALL
        _searchQuery.value = ""
    }

    fun setGiveawayManualStorage(id: Int, sizeGb: Float?) {
        viewModelScope.launch {
            repository.updateGiveawayManualStorage(id, sizeGb)
            _detailItem.value?.takeIf { it.id == id }?.let { _detailItem.value = it.copy(manualInstallSizeGb = sizeGb) }
        }
    }

    fun setSteamGameManualStorage(appId: Int, sizeGb: Float?) {
        viewModelScope.launch { repository.updateSteamGameManualStorage(appId, sizeGb) }
    }

    // --- Theme & Appearance Actions ---
    fun setThemeMode(mode: ThemeMode) { preferences.themeMode = mode; _themeMode.value = mode }
    fun setAccentTheme(theme: AccentTheme) { preferences.accentTheme = theme; _accentTheme.value = theme }
    fun setDarkBackgroundTheme(theme: BackgroundTheme) { preferences.darkBackgroundTheme = theme; _darkBackgroundTheme.value = theme }
    fun setLightBackgroundTheme(theme: BackgroundTheme) { preferences.lightBackgroundTheme = theme; _lightBackgroundTheme.value = theme }

    fun importCustomFont(context: Context, uri: Uri, onResult: (Boolean, String) -> Unit) {
        val s = AppStringsRegistry.forLanguage(_appLanguage.value)
        viewModelScope.launch {
            when (val res = withContext(Dispatchers.IO) { CustomFontHelper.importFontFromUri(context, uri) }) {
                is CustomFontHelper.ImportResult.Success -> {
                    preferences.customFontPath = res.filePath
                    preferences.customFontName = res.displayName
                    _customFontPath.value = res.filePath
                    _customFontName.value = res.displayName
                    onResult(true, String.format(s.system.vmCustomFontAppliedFormat, res.displayName))
                }
                is CustomFontHelper.ImportResult.Error -> onResult(false, res.message)
            }
        }
    }

    fun resetCustomFont(context: Context) {
        CustomFontHelper.clearCustomFonts(context)
        preferences.customFontPath = ""; preferences.customFontName = ""
        _customFontPath.value = ""; _customFontName.value = ""
    }

    fun setAppLanguage(language: AppLanguage) {
        preferences.appLanguage = language
        _appLanguage.value = language
        LocaleSyncHelper.applyAndRefreshSystemSurfaces(getApplication(), language)
    }

    fun completeInitialLanguageSelection(language: AppLanguage) {
        setAppLanguage(language)
        preferences.hasSelectedInitialLanguage = true
        _hasSelectedInitialLanguage.value = true
    }

    fun setCardDensity(density: CardDensity) { preferences.cardDensity = density; _cardDensity.value = density }
    fun setDefaultTab(tab: MainTab) { preferences.defaultTab = tab; _defaultTab.value = tab }
    fun setShowOriginalPrice(show: Boolean) { preferences.isShowOriginalPrice = show; _isShowOriginalPrice.value = show }
    fun setShowExpiration(show: Boolean) { preferences.isShowExpiration = show; _isShowExpiration.value = show }
    fun setConfirmToastOnClaim(confirm: Boolean) { preferences.isConfirmToastOnClaim = confirm; _isConfirmToastOnClaim.value = confirm }
    fun setUseInAppBrowser(enabled: Boolean) { preferences.isUseInAppBrowser = enabled; _isUseInAppBrowser.value = enabled }
    fun openInAppBrowser(url: String, title: String = "") { _inAppBrowserTitle.value = title; _inAppBrowserUrl.value = url }
    fun closeInAppBrowser() { _inAppBrowserUrl.value = null; _inAppBrowserTitle.value = "" }
    fun setAutoCheckOnReopen(enabled: Boolean) { preferences.isAutoCheckOnReopen = enabled; _isAutoCheckOnReopen.value = enabled }
    fun setDataSaverMode(enabled: Boolean) { preferences.isDataSaverMode = enabled; _isDataSaverMode.value = enabled }
    fun setAutoPruneExpired(enabled: Boolean) { preferences.isAutoPruneExpired = enabled; _isAutoPruneExpired.value = enabled }
    fun setDefaultLauncher(launcher: LauncherType) { preferences.defaultLauncher = launcher; _defaultLauncher.value = launcher }
    fun setAnimationsEnabled(enabled: Boolean) { preferences.isAnimationsEnabled = enabled; _isAnimationsEnabled.value = enabled }
    fun setLowEndOptimization(enabled: Boolean) {
        preferences.isLowEndOptimization = enabled
        _isLowEndOptimization.value = enabled
        if (enabled) {
            LootDropApplication.trimMemoryCaches(getApplication(), aggressive = false)
        }
    }
    fun setTargetFrameRate(frameRate: TargetFrameRate) { preferences.targetFrameRate = frameRate; _targetFrameRate.value = frameRate }
    fun setDetectedRefreshRate(rate: Int) { _detectedRefreshRate.value = rate }
    fun setDpiOptimizationEnabled(enabled: Boolean) { preferences.isDpiOptimizationEnabled = enabled; _isDpiOptimizationEnabled.value = enabled }
    fun setDpiScaleMode(mode: DpiScaleMode) { preferences.dpiScaleMode = mode; _dpiScaleMode.value = mode }
    fun setDetectedScreenDpi(dpi: Int) { preferences.detectedScreenDpi = dpi; _detectedScreenDpi.value = dpi }
    fun completeOnboarding() { preferences.hasSeenOnboarding = true; _showOnboardingWalkthrough.value = false }
    fun dismissOnboarding() { preferences.hasSeenOnboarding = true; _showOnboardingWalkthrough.value = false }
    fun replayOnboarding() { _showOnboardingWalkthrough.value = true }

    fun updateUserProfile(name: String, avatarUri: Uri? = null, removeAvatar: Boolean = false) {
        _userProfile.value = UserProfileManager.saveProfile(getApplication(), preferences, name, avatarUri, removeAvatar)
        if (preferences.isCloudAccountLoggedIn) {
            viewModelScope.launch { CloudBackupManager.updateSupabaseDisplayName(name, preferences) }
        }
    }

    fun updateDisplayedTrophies(hasCustom: Boolean, keys: Set<String>) {
        _userProfile.value = UserProfileManager.saveDisplayedTrophies(preferences, hasCustom, keys)
    }

    fun refreshUserProfile() { _userProfile.value = UserProfileManager.getProfile(preferences) }
    fun dismissNewToday() { preferences.isNewTodayDismissed = true; _isNewTodayDismissed.value = true }
    fun resetNewTodayDismissed() { preferences.isNewTodayDismissed = false; _isNewTodayDismissed.value = false }

    // --- Notification Preferences Actions ---
    fun setNotificationsEnabled(enabled: Boolean) { notificationHelper.isNotificationsEnabled = enabled; _isNotificationsEnabled.value = enabled }
    fun setNotifyPlayStation(enabled: Boolean) { notificationHelper.isNotifyPlayStation = enabled; _isNotifyPlayStation.value = enabled }
    fun setNotifyXbox(enabled: Boolean) { notificationHelper.isNotifyXbox = enabled; _isNotifyXbox.value = enabled }
    fun setNotifyNintendo(enabled: Boolean) { notificationHelper.isNotifyNintendo = enabled; _isNotifyNintendo.value = enabled }
    fun setNotifySteam(enabled: Boolean) { notificationHelper.isNotifySteam = enabled; _isNotifySteam.value = enabled }
    fun setNotifyEpic(enabled: Boolean) { notificationHelper.isNotifyEpic = enabled; _isNotifyEpic.value = enabled }
    fun setNotifyGog(enabled: Boolean) { notificationHelper.isNotifyGog = enabled; _isNotifyGog.value = enabled }
    fun setNotifyAndroid(enabled: Boolean) { notificationHelper.isNotifyAndroid = enabled; _isNotifyAndroid.value = enabled }
    fun setNotifyIos(enabled: Boolean) { notificationHelper.isNotifyIos = enabled; _isNotifyIos.value = enabled }
    fun setNotifyOthers(enabled: Boolean) { notificationHelper.isNotifyOthers = enabled; _isNotifyOthers.value = enabled }
    fun setGamesOnly(enabled: Boolean) { notificationHelper.isGamesOnly = enabled; _isGamesOnly.value = enabled }
    fun setNotifyEndingSoon(enabled: Boolean) { notificationHelper.isNotifyEndingSoon = enabled; _isNotifyEndingSoon.value = enabled }
    fun setMinWorthAlertThreshold(threshold: Float) { notificationHelper.minWorthAlertThreshold = threshold; _minWorthAlertThreshold.value = threshold }
    fun setQuietHoursEnabled(enabled: Boolean) { notificationHelper.isQuietHoursEnabled = enabled; _isQuietHoursEnabled.value = enabled }
    fun setNotificationSoundEnabled(enabled: Boolean) { notificationHelper.isNotificationSoundEnabled = enabled; _isNotificationSoundEnabled.value = enabled }
    fun setNotificationVibrateEnabled(enabled: Boolean) { notificationHelper.isNotificationVibrateEnabled = enabled; _isNotificationVibrateEnabled.value = enabled }
    fun setKeepForeverOnly(enabled: Boolean) { notificationHelper.isKeepForeverOnly = enabled; _isKeepForeverOnly.value = enabled }
    fun setNotificationRichArtwork(enabled: Boolean) { notificationHelper.isNotificationRichArtwork = enabled; _isNotificationRichArtwork.value = enabled }
    fun setNotificationActionsEnabled(enabled: Boolean) { notificationHelper.isNotificationActionsEnabled = enabled; _isNotificationActionsEnabled.value = enabled }

    fun setQuietHoursHours(start: Int, end: Int) {
        preferences.quietHoursStartHour = start; preferences.quietHoursEndHour = end
        _quietHoursStartHour.value = start; _quietHoursEndHour.value = end
    }

    fun setBackgroundSyncInterval(hours: Int) {
        preferences.backgroundSyncIntervalHours = hours
        _backgroundSyncIntervalHours.value = hours
        NotificationScheduler.schedulePeriodicSync(getApplication())
    }

    fun setBackgroundSyncWifiOnly(wifiOnly: Boolean) { preferences.backgroundSyncWifiOnly = wifiOnly; _backgroundSyncWifiOnly.value = wifiOnly }
    fun setNotificationHistorySheetOpen(open: Boolean) { _isNotificationHistorySheetOpen.value = open }
    fun sendTestNotification(isRichPreview: Boolean = true) = notificationHelper.sendTestNotification(isRichPreview)
    fun sendEndingSoonTestNotification() = notificationHelper.sendTestEndingSoonNotification()
    fun markNotificationAsRead(id: Long) { viewModelScope.launch(Dispatchers.IO) { database.notificationHistoryDao().markAsRead(id) } }
    fun markAllNotificationsAsRead() { viewModelScope.launch(Dispatchers.IO) { database.notificationHistoryDao().markAllAsRead() } }
    fun clearAllNotificationHistory() { viewModelScope.launch(Dispatchers.IO) { database.notificationHistoryDao().clearAllNotifications() } }
    fun deleteNotification(id: Long) { viewModelScope.launch(Dispatchers.IO) { database.notificationHistoryDao().deleteNotification(id) } }

    // --- Library & Steam Sync Actions ---
    fun clearClaimedGames() { viewModelScope.launch { repository.clearAllClaimed() } }
    fun clearBookmarkedGames() { viewModelScope.launch { repository.clearAllBookmarks() } }

    fun forceResync() {
        viewModelScope.launch {
            _isLoading.value = true; _errorMessage.value = null
            repository.forceResync()
            _isLoading.value = false
        }
    }

    fun toggleClaimed(item: GiveawayItem) {
        viewModelScope.launch {
            repository.toggleClaimed(item.id, item.isClaimed)
            _detailItem.value?.takeIf { it.id == item.id }?.let { _detailItem.value = it.copy(isClaimed = !item.isClaimed) }
        }
    }

    fun updatePlayStatus(item: GiveawayItem, status: PlayStatus) = updatePlayStatusById(item.id, status)

    fun updatePlayStatusById(id: Int, status: PlayStatus) {
        viewModelScope.launch {
            repository.updatePlayStatus(id, status)
            _detailItem.value?.takeIf { it.id == id }?.let { _detailItem.value = it.copy(playStatus = status) }
        }
    }

    fun setLibraryPlayFilter(filter: LibraryPlayFilter) { preferences.libraryPlayFilter = filter; _selectedLibraryPlayFilter.value = filter }
    fun setSteamUserId(userId: String) { preferences.steamUserId = userId; _steamUserId.value = userId }
    fun setSteamApiKey(key: String) { preferences.steamApiKey = key; _steamApiKey.value = key }
    fun clearSteamSyncStatus() { _steamSyncStatus.value = null }

    fun syncSteamLibrary(customSteamId: String? = null, customApiKey: String? = null) {
        val s = AppStringsRegistry.forLanguage(_appLanguage.value)
        viewModelScope.launch {
            _isSyncingSteam.value = true
            _steamSyncStatus.value = s.system.vmSteamSyncingConnecting
            try {
                SteamLibrarySyncDelegate.syncSteamLibrary(
                    context = getApplication(), database = database, repository = repository,
                    preferences = preferences, rawUserId = (customSteamId ?: _steamUserId.value).trim(),
                    rawApiKey = customApiKey ?: _steamApiKey.value, allGiveaways = allGiveaways.value,
                    onStatus = { _steamSyncStatus.value = it },
                    onUserIdSaved = { _steamUserId.value = it },
                    onTriggerDlcRefresh = { refreshSteamDlcRecommendations(forceRefresh = true) }
                )
            } catch (e: Exception) {
                _steamSyncStatus.value = String.format(s.system.vmSteamSyncFailedFormat, e.message ?: "")
            } finally {
                _isSyncingSteam.value = false
                runCatching { com.example.widget.WidgetUpdateHelper.updateAllWidgets(getApplication()) }
            }
        }
    }

    fun setLibraryViewMode(mode: LibraryViewMode) { preferences.libraryViewMode = mode; _libraryViewMode.value = mode }

    fun updateSteamGamePlayStatus(game: SteamGameEntity, status: PlayStatus) {
        viewModelScope.launch {
            repository.updateSteamGamePlayStatus(game.appId, status)
            game.matchedGiveawayId?.let { repository.updatePlayStatus(it, status) }
        }
    }

    fun toggleWishlist(item: GiveawayItem) {
        viewModelScope.launch {
            repository.toggleWishlisted(item.id, item.isWishlisted)
            _detailItem.value?.takeIf { it.id == item.id }?.let { _detailItem.value = it.copy(isWishlisted = !item.isWishlisted) }
        }
    }

    fun toggleBookmark(item: GiveawayItem) {
        viewModelScope.launch {
            repository.toggleBookmarked(item.id, item.isBookmarked)
            _detailItem.value?.takeIf { it.id == item.id }?.let { _detailItem.value = it.copy(isBookmarked = !item.isBookmarked) }
        }
    }

    fun toggleBookmarkById(id: Int) {
        viewModelScope.launch {
            val item = repository.getGiveawayById(id)
            if (item != null) repository.toggleBookmarked(item.id, item.isBookmarked)
            else database.giveawayDao().setBookmarked(id, true)
        }
    }

    fun openItemById(id: Int) {
        viewModelScope.launch { repository.getGiveawayById(id)?.let { _detailItem.value = it } }
    }

    fun checkOwnership(item: GiveawayItem): OwnershipStatus =
        GiveawayFilterEngine.checkOwnership(item, allSteamGames.value)

    fun getLauncherOptionsFor(item: GiveawayItem): List<LauncherOption> =
        GiveawayFilterEngine.getLauncherOptionsFor(item, allGiveaways.value)

    fun claimAndLaunch(context: Context, item: GiveawayItem, targetUrl: String? = null, launcherName: String? = null) {
        val s = AppStringsRegistry.forLanguage(_appLanguage.value)
        if (!item.isClaimed) {
            viewModelScope.launch {
                repository.toggleClaimed(item.id, false)
                _detailItem.value?.takeIf { it.id == item.id }?.let { _detailItem.value = it.copy(isClaimed = true) }
            }
            if (_isConfirmToastOnClaim.value) {
                val label = if (launcherName != null) String.format(s.system.vmOnStoreFormat, launcherName) else ""
                Toast.makeText(context, String.format(s.system.vmClaimToastFormat, item.title, label), Toast.LENGTH_SHORT).show()
            }
        }
        val url = targetUrl ?: item.openGiveawayUrl
        if (_isUseInAppBrowser.value && url.isNotBlank()) openInAppBrowser(url, item.title) else launchExternalBrowser(context, url)
    }

    fun launchExternalBrowser(context: Context, url: String) {
        if (url.isBlank()) return
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
        } catch (_: Exception) {
            val s = AppStringsRegistry.forLanguage(_appLanguage.value)
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
            clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("Giveaway URL", url))
            Toast.makeText(context, s.system.vmNoBrowserCopiedToast, Toast.LENGTH_LONG).show()
        }
    }

    // --- Backup & Cloud Sync Actions ---
    fun dismissBackupRestoreStatus() { _backupRestoreStatus.value = null }

    fun exportBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            _isBackingUpOrRestoring.value = true
            CloudAndBackupDelegate.exportLocalBackup(context, uri, database, preferences, { _backupRestoreStatus.value = it }, { updateStorageFootprint() })
            _isBackingUpOrRestoring.value = false
        }
    }

    fun restoreBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            _isBackingUpOrRestoring.value = true
            CloudAndBackupDelegate.restoreLocalBackup(context, uri, database, preferences, { _backupRestoreStatus.value = it }) {
                updateStorageFootprint(); refreshUserProfile()
            }
            _isBackingUpOrRestoring.value = false
        }
    }

    fun setAutoBackupEnabled(enabled: Boolean, context: Context) {
        preferences.isAutoBackupEnabled = enabled; _isAutoBackupEnabled.value = enabled
        if (enabled) {
            AutoBackupScheduler.schedulePeriodicAutoBackup(context)
            if (preferences.autoBackupFolderUri.isNotBlank()) triggerManualAutoBackup(context)
        } else AutoBackupScheduler.cancelPeriodicAutoBackup(context)
    }

    fun setAutoBackupFolder(uri: Uri, context: Context) {
        val folderName = BackupRestoreManager.getFolderDisplayName(context, uri)
        preferences.autoBackupFolderUri = uri.toString(); preferences.autoBackupFolderDisplayName = folderName
        _autoBackupFolderUri.value = uri.toString(); _autoBackupFolderDisplayName.value = folderName
        AutoBackupScheduler.schedulePeriodicAutoBackup(context)
        triggerManualAutoBackup(context)
    }

    fun clearAutoBackupFolder(context: Context) {
        preferences.autoBackupFolderUri = ""; preferences.autoBackupFolderDisplayName = ""
        _autoBackupFolderUri.value = ""; _autoBackupFolderDisplayName.value = ""
        AutoBackupScheduler.cancelPeriodicAutoBackup(context)
    }

    fun setAutoBackupIntervalDays(days: Int, context: Context) {
        preferences.autoBackupIntervalDays = days; _autoBackupIntervalDays.value = days
        AutoBackupScheduler.schedulePeriodicAutoBackup(context)
    }

    fun triggerManualAutoBackup(context: Context) {
        val s = AppStringsRegistry.forLanguage(_appLanguage.value)
        val folderUri = preferences.autoBackupFolderUri.takeIf { it.isNotBlank() }?.let { runCatching { Uri.parse(it) }.getOrNull() }
        if (folderUri == null) {
            showShortToast(context, if (preferences.autoBackupFolderUri.isBlank()) s.system.vmSelectBackupFolderFirst else s.system.vmInvalidFolderConfig)
            return
        }
        viewModelScope.launch {
            _isAutoBackingUp.value = true
            CloudAndBackupDelegate.performManualAutoBackup(
                context, folderUri, database, preferences,
                { ts, st -> _lastAutoBackupTimestamp.value = ts; _lastAutoBackupStatus.value = st },
                { updateStorageFootprint() }
            )
            _isAutoBackingUp.value = false
        }
    }

    fun checkAndTriggerAutoBackupIfNeeded(context: Context) {
        if (!preferences.isAutoBackupEnabled || preferences.autoBackupFolderUri.isBlank()) return
        val intervalMillis = preferences.autoBackupIntervalDays.coerceAtLeast(1).toLong() * 86_400_000L
        val elapsed = System.currentTimeMillis() - preferences.lastAutoBackupTimestamp
        if (preferences.lastAutoBackupTimestamp <= 0L || elapsed >= intervalMillis) {
            viewModelScope.launch {
                val result = runCatching { BackupRestoreManager.performScheduledAutoBackup(context) }.getOrNull()
                _lastAutoBackupTimestamp.value = preferences.lastAutoBackupTimestamp
                _lastAutoBackupStatus.value = preferences.lastAutoBackupStatus
                if (result?.isSuccess == true) updateStorageFootprint()
            }
        }
    }

    fun dismissCloudBackupStatus() { _cloudBackupStatus.value = null }
    fun setCloudAutoBackupEnabled(enabled: Boolean) { preferences.isCloudAutoBackupEnabled = enabled; _isCloudAutoBackupEnabled.value = enabled }

    private fun syncCloudAccountFlows() {
        _isCloudAccountLoggedIn.value = preferences.isCloudAccountLoggedIn
        _cloudAccountUsername.value = preferences.cloudAccountUsername
        _isCloudSessionActive.value = isCloudSessionActive
        _cloudBackupStatus.value = preferences.lastCloudBackupStatus
    }

    fun createCloudAccount(username: String, password: String, context: Context, email: String = "", onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        val s = AppStringsRegistry.forLanguage(_appLanguage.value)
        viewModelScope.launch {
            _isCloudAuthenticating.value = true
            val result = CloudBackupManager.createAccount(username, password, preferences, email)
            _isCloudAuthenticating.value = false; syncCloudAccountFlows()
            result.onSuccess { user ->
                updateUserProfile(name = user)
                val msg = String.format(s.system.vmCloudCreatedFormat, user)
                showLongToast(context, msg); onResult(true, msg)
            }.onFailure { e ->
                val err = e.message ?: s.system.vmCloudCreateFailed
                showLongToast(context, err); onResult(false, err)
            }
        }
    }

    fun signInCloudAccount(username: String, password: String, context: Context, email: String = "", onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        val s = AppStringsRegistry.forLanguage(_appLanguage.value)
        viewModelScope.launch {
            _isCloudAuthenticating.value = true
            val result = CloudBackupManager.signInAccount(username, password, preferences, email)
            _isCloudAuthenticating.value = false; syncCloudAccountFlows()
            result.onSuccess { user ->
                if (preferences.userProfileName.isBlank() || preferences.userProfileName == "Gamer") updateUserProfile(name = user)
                val msg = String.format(s.system.vmCloudSignedInFormat, user)
                showLongToast(context, msg); onResult(true, msg)
            }.onFailure { e ->
                val err = e.message ?: s.system.vmCloudSignInFailed
                showLongToast(context, err); onResult(false, err)
            }
        }
    }

    fun signOutCloudAccount(context: Context) {
        val s = AppStringsRegistry.forLanguage(_appLanguage.value)
        CloudBackupManager.signOut(preferences)
        _isCloudAccountLoggedIn.value = false; _cloudAccountUsername.value = ""
        _isCloudSessionActive.value = false; _cloudBackupStatus.value = preferences.lastCloudBackupStatus
        showShortToast(context, s.system.vmCloudSignedOut)
    }

    fun backupToCloud(context: Context, isAutomated: Boolean = false) {
        viewModelScope.launch {
            _isCloudBackingUp.value = true
            CloudAndBackupDelegate.uploadToCloud(context, database, preferences, isAutomated, {
                _isCloudSessionActive.value = isCloudSessionActive
                _lastCloudBackupTimestamp.value = preferences.lastCloudBackupTimestamp
                _cloudBackupStatus.value = preferences.lastCloudBackupStatus
            }, { updateStorageFootprint() })
            _isCloudBackingUp.value = false
        }
    }

    fun restoreFromCloud(context: Context) {
        viewModelScope.launch {
            _isCloudRestoring.value = true
            CloudAndBackupDelegate.restoreFromCloud(context, database, preferences, {
                _isCloudSessionActive.value = isCloudSessionActive
                _cloudBackupStatus.value = preferences.lastCloudBackupStatus
            }) { updateStorageFootprint(); refreshUserProfile() }
            _isCloudRestoring.value = false
        }
    }

    fun checkAndTriggerCloudAutoBackupIfNeeded(context: Context) {
        if (!preferences.isCloudAutoBackupEnabled || !CloudBackupManager.isConfigured() || !preferences.isCloudAccountLoggedIn) return
        val elapsed = System.currentTimeMillis() - preferences.lastCloudBackupTimestamp
        if (preferences.lastCloudBackupTimestamp <= 0L || elapsed >= 86_400_000L) backupToCloud(context, isAutomated = true)
    }

    fun pingSupabaseNow(context: Context) {
        val isEs = _appLanguage.value == AppLanguage.SPANISH
        viewModelScope.launch {
            _isPingingSupabase.value = true
            val result = SupabaseKeepAliveManager.pingNow(preferences)
            _lastSupabaseKeepAliveTimestamp.value = preferences.lastSupabaseKeepAliveTimestamp
            _lastSupabaseKeepAliveStatus.value = preferences.lastSupabaseKeepAliveStatus
            _isPingingSupabase.value = false
            result.onSuccess { res ->
                val msg = if (isEs) {
                    "✅ Base de datos Supabase activa (HTTP ${res.httpCode} • ${res.latencyMs}ms)"
                } else {
                    "✅ Supabase DB active (HTTP ${res.httpCode} • ${res.latencyMs}ms)"
                }
                showShortToast(context, msg)
            }.onFailure { err ->
                val msg = if (isEs) {
                    "⚠️ Error al hacer ping a Supabase: ${err.message ?: "Sin conexión"}"
                } else {
                    "⚠️ Supabase ping failed: ${err.message ?: "Offline"}"
                }
                showShortToast(context, msg)
            }
        }
    }
}
