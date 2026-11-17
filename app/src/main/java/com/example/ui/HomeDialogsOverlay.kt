package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.data.local.SteamAchievementEntity
import com.example.data.local.SteamGameEntity
import com.example.data.model.AppUpdateInfo
import com.example.data.model.BacklogChallengeGrid
import com.example.data.model.GameCategory
import com.example.data.model.GiveawayItem
import com.example.data.model.LauncherType
import com.example.data.model.PlayabilityFilter
import com.example.data.model.PublicReview
import com.example.data.model.SteamDlcRecommendation
import com.example.data.model.StorageFilter
import com.example.data.model.UpdateDownloadState
import com.example.data.model.UserProfile
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.i18n.localizedTitle
import com.example.ui.components.AppUpdateDialog
import com.example.ui.components.BacklogChallengeGridDialog
import com.example.ui.components.CategoryMultiSelectSheet
import com.example.ui.components.ChooseLauncherDialog
import com.example.ui.components.FilterSortBottomSheet
import com.example.ui.components.GameAchievementsDialog
import com.example.ui.components.GiveawayDetailDialog
import com.example.ui.components.InAppBrowserDialog
import com.example.ui.components.InitialLanguageDialog
import com.example.ui.components.MythicAchievementToast
import com.example.ui.components.NotificationHistorySheet
import com.example.ui.components.NotificationSettingsSheet
import com.example.ui.components.OnboardingWalkthroughDialog
import com.example.ui.components.ProfileDialog
import com.example.ui.components.SteamDlcHubDialog
import com.example.ui.components.SteamSyncDialog

@Composable
internal fun BoxScope.HomeDialogsOverlay(
    viewModel: GiveawaysViewModel,
    context: Context,
    appLanguage: AppLanguage,
    hasSelectedInitialLanguage: Boolean,
    selectedLaunchers: Set<LauncherType>,
    selectedTypeFilter: TypeFilter,
    selectedCategories: Set<GameCategory>,
    selectedSort: SortOption,
    activeFilterCount: Int,
    uiStats: UiStats,
    displayedGiveaways: List<GiveawayItem>,
    discoverGiveaways: List<GiveawayItem>,
    filteredSteamGames: List<SteamGameEntity>,
    selectedPlayabilityFilter: PlayabilityFilter,
    selectedStorageFilter: StorageFilter,
    isFilterSheetOpen: Boolean,
    isCategorySheetOpen: Boolean,
    detailItem: GiveawayItem?,
    launcherChooserItem: GiveawayItem?,
    onClearLauncherChooser: () -> Unit,
    onOpenLauncherChooser: (GiveawayItem) -> Unit,
    showSteamDialog: Boolean,
    onDismissSteamDialog: () -> Unit,
    onOpenSteamDialog: () -> Unit,
    steamUserId: String,
    steamApiKey: String,
    isSyncingSteam: Boolean,
    steamSyncStatus: String?,
    inAppBrowserUrl: String?,
    inAppBrowserTitle: String,
    showOnboardingWalkthrough: Boolean,
    showProfileDialog: Boolean,
    onDismissProfileDialog: () -> Unit,
    userProfile: UserProfile,
    rarestAchievements: List<SteamAchievementEntity>,
    publicReviews: List<PublicReview>,
    showBacklogChallengeDialog: Boolean,
    isBacklogChallengeDialogVisibleFromVm: Boolean,
    backlogChallengeGrid: BacklogChallengeGrid,
    onDismissBacklogDialog: () -> Unit,
    isNotificationHistorySheetOpen: Boolean,
    isNotificationSettingsSheetOpen: Boolean,
    onSetNotificationSettingsOpen: (Boolean) -> Unit,
    isUpdateDialogVisible: Boolean,
    appUpdateInfo: AppUpdateInfo?,
    updateDownloadState: UpdateDownloadState,
    selectedGameForAchievements: SteamGameEntity?,
    selectedGameAchievements: List<SteamAchievementEntity>,
    isLoadingGameAchievements: Boolean,
    showDlcHubDialog: Boolean,
    onDismissDlcHubDialog: () -> Unit,
    steamDlcRecommendations: List<SteamDlcRecommendation>,
    selectedGameForDlcs: SteamGameEntity?,
    selectedGameDlcs: List<SteamDlcRecommendation>,
    isLoadingSteamDlcs: Boolean,
    isLoadingGameDlcs: Boolean,
    steamDlcScanStatus: String?
) {
    val s = LocalAppStrings.current
    if (isFilterSheetOpen) {
        FilterSortBottomSheet(
            selectedLaunchers = selectedLaunchers,
            onLauncherToggle = { viewModel.toggleLauncher(it) },
            selectedTypeFilter = selectedTypeFilter,
            onTypeFilterSelected = { viewModel.selectTypeFilter(it) },
            selectedCategories = selectedCategories,
            onCategoryToggle = { viewModel.toggleCategoryFilter(it) },
            onOpenCategoryMenu = { viewModel.setCategorySheetOpen(true) },
            selectedSort = selectedSort,
            onSortSelected = { viewModel.selectSort(it) },
            activeFilterCount = activeFilterCount,
            onResetFilters = { viewModel.resetFilters() },
            platformCounts = uiStats.platformCounts,
            totalFilteredCount = displayedGiveaways.size,
            selectedPlayability = selectedPlayabilityFilter,
            onPlayabilitySelect = { viewModel.setPlayabilityFilter(it) },
            selectedStorage = selectedStorageFilter,
            onStorageSelect = { viewModel.setStorageFilter(it) },
            onDismiss = { viewModel.setFilterSheetOpen(false) }
        )
    }

    if (isCategorySheetOpen) {
        CategoryMultiSelectSheet(
            selectedCategories = selectedCategories,
            onCategoryToggle = { viewModel.toggleCategoryFilter(it) },
            onSelectAll = { viewModel.setCategoryFilters(GameCategory.entries.toSet()) },
            onClearAll = { viewModel.clearCategoryFilters() },
            giveaways = discoverGiveaways,
            onDismiss = { viewModel.setCategorySheetOpen(false) }
        )
    }

    detailItem?.let { item ->
        val ownershipStatus = remember(item, filteredSteamGames) { viewModel.checkOwnership(item) }
        val gameReviews = remember(item, publicReviews) { viewModel.getReviewsForGame(item.id, item.title) }
        GiveawayDetailDialog(
            item = item,
            ownershipStatus = ownershipStatus,
            reviews = gameReviews,
            defaultAuthorName = userProfile.name,
            onDismiss = { viewModel.closeDetail() },
            onClaimClick = {
                if (item.hasMultipleLaunchers) onOpenLauncherChooser(item)
                else viewModel.claimAndLaunch(context, item)
            },
            onLauncherOptionClick = { url, launcherName -> viewModel.claimAndLaunch(context, item, targetUrl = url, launcherName = launcherName) },
            onToggleBookmark = { viewModel.toggleBookmark(item) },
            onToggleClaimed = { viewModel.toggleClaimed(item) },
            onToggleWishlist = { viewModel.toggleWishlist(item) },
            onPlayStatusChange = { status -> viewModel.updatePlayStatus(item, status) },
            onUpdateManualStorage = { sizeGb -> viewModel.setGiveawayManualStorage(item.id, sizeGb) },
            onPostReview = { rating, title, comment ->
                viewModel.postPublicReview(item.id, item.title, rating, title, comment) { _, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            onVoteHelpful = { reviewId ->
                viewModel.voteReviewHelpful(reviewId) { _, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            hasVotedHelpful = { viewModel.hasVotedHelpful(it) }
        )
    }

    launcherChooserItem?.let { item ->
        val launcherOwnership = remember(item, filteredSteamGames) { viewModel.checkOwnership(item) }
        ChooseLauncherDialog(
            item = item,
            ownershipStatus = launcherOwnership,
            launchers = viewModel.getLauncherOptionsFor(item),
            onDismiss = onClearLauncherChooser,
            onLauncherClaimed = { targetUrl, launcherName ->
                viewModel.claimAndLaunch(context, item, targetUrl = targetUrl, launcherName = launcherName)
                onClearLauncherChooser()
            }
        )
    }

    if (showSteamDialog) {
        SteamSyncDialog(
            currentUserId = steamUserId,
            currentApiKey = steamApiKey,
            isSyncing = isSyncingSteam,
            statusMessage = steamSyncStatus,
            onDismiss = onDismissSteamDialog,
            onSync = { uid, key ->
                viewModel.syncSteamLibrary(customSteamId = uid, customApiKey = if (key.isBlank()) null else key)
            }
        )
    }

    inAppBrowserUrl?.let { url ->
        InAppBrowserDialog(
            url = url,
            title = inAppBrowserTitle,
            onDismiss = { viewModel.closeInAppBrowser() },
            onOpenExternal = { viewModel.launchExternalBrowser(context, it) }
        )
    }

    if (!hasSelectedInitialLanguage) {
        InitialLanguageDialog(
            initialLanguage = appLanguage,
            onSelectLanguagePreview = { viewModel.setAppLanguage(it) },
            onConfirmLanguage = { viewModel.completeInitialLanguageSelection(it) }
        )
    } else if (showOnboardingWalkthrough) {
        OnboardingWalkthroughDialog(
            initialProfile = userProfile,
            currentLanguage = appLanguage,
            onLanguageChange = { viewModel.setAppLanguage(it) },
            onSaveProfile = { name, uri, remove -> viewModel.updateUserProfile(name, uri, remove) },
            onDismiss = { viewModel.dismissOnboarding() },
            onFinish = { viewModel.completeOnboarding() }
        )
    }

    if (showProfileDialog) {
        ProfileDialog(
            profile = userProfile,
            uiStats = uiStats,
            steamUserId = steamUserId,
            steamTrophies = rarestAchievements,
            isSyncingSteam = isSyncingSteam,
            onSyncSteam = { viewModel.syncSteamLibrary() },
            onOpenSteamDialog = onOpenSteamDialog,
            onSaveProfile = { name, uri, remove -> viewModel.updateUserProfile(name, uri, remove) },
            onSaveDisplayedTrophies = { hasCustom, keys -> viewModel.updateDisplayedTrophies(hasCustom, keys) },
            onReplayTour = { viewModel.replayOnboarding() },
            onDismiss = onDismissProfileDialog
        )
    }

    if (showBacklogChallengeDialog || isBacklogChallengeDialogVisibleFromVm) {
        BacklogChallengeGridDialog(
            grid = backlogChallengeGrid,
            onDismiss = {
                onDismissBacklogDialog()
                viewModel.setBacklogChallengeDialogVisible(false)
            },
            onToggleManualTile = { viewModel.toggleBacklogTileManual(it) },
            onRefreshCheck = { viewModel.refreshBacklogChallengeGrid() },
            onClaimMilestones = { viewModel.claimBacklogMilestones(it) },
            onEquipTheme = { theme ->
                viewModel.setAccentTheme(theme)
                Toast.makeText(context, String.format(s.system.vmEquippedThemeFormat, theme.localizedTitle(s)), Toast.LENGTH_SHORT).show()
            },
            onSelectWeek = { viewModel.selectBacklogWeek(it) }
        )
    }

    if (isNotificationHistorySheetOpen) {
        NotificationHistorySheet(
            viewModel = viewModel,
            onDismiss = { viewModel.setNotificationHistorySheetOpen(false) },
            onOpenSettings = {
                viewModel.setNotificationHistorySheetOpen(false)
                onSetNotificationSettingsOpen(true)
            }
        )
    }

    if (isNotificationSettingsSheetOpen) {
        NotificationSettingsSheet(
            notificationHelper = viewModel.notificationHelper,
            onDismiss = { onSetNotificationSettingsOpen(false) },
            onOpenInbox = {
                onSetNotificationSettingsOpen(false)
                viewModel.setNotificationHistorySheetOpen(true)
            }
        )
    }

    if (isUpdateDialogVisible && appUpdateInfo != null) {
        AppUpdateDialog(
            updateInfo = appUpdateInfo,
            downloadState = updateDownloadState,
            onDismiss = {
                viewModel.dismissUpdateDialog()
                if (updateDownloadState is UpdateDownloadState.Error) viewModel.resetUpdateDownloadState()
            },
            onStartDownload = { viewModel.startInAppUpdateDownload(appUpdateInfo, context) },
            onInstallApk = { viewModel.installDownloadedApk(context) },
            onViewReleases = { releasesUrl ->
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(releasesUrl)).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                viewModel.dismissUpdateDialog()
            }
        )
    }

    selectedGameForAchievements?.let { game ->
        GameAchievementsDialog(
            game = game,
            achievements = selectedGameAchievements,
            isLoading = isLoadingGameAchievements,
            onDismiss = { viewModel.closeGameAchievements() }
        )
    }

    if (showDlcHubDialog || selectedGameForDlcs != null) {
        SteamDlcHubDialog(
            recommendations = steamDlcRecommendations,
            selectedGame = selectedGameForDlcs,
            selectedGameDlcs = selectedGameDlcs,
            isLoadingLibraryDlcs = isLoadingSteamDlcs,
            isLoadingGameDlcs = isLoadingGameDlcs,
            scanStatus = steamDlcScanStatus,
            onClearSelectedGame = { viewModel.closeGameDlcExplorer() },
            onRefreshLibraryDlcs = { viewModel.refreshSteamDlcRecommendations(forceRefresh = true) },
            onOpenDlc = { viewModel.openDlcRecommendation(context, it) },
            onDismissDlc = { viewModel.dismissDlcRecommendation(it) },
            onDismiss = {
                onDismissDlcHubDialog()
                viewModel.closeGameDlcExplorer()
            }
        )
    }

    val mythicNotification by viewModel.mythicAchievementNotification.collectAsState()
    MythicAchievementToast(
        achievement = mythicNotification,
        onDismiss = { viewModel.dismissMythicToast() },
        onViewTrophy = { ach ->
            viewModel.dismissMythicToast()
            viewModel.openAchievementsForAppId(ach.appId, ach.gameName)
        },
        modifier = Modifier.align(Alignment.TopCenter)
    )
}
