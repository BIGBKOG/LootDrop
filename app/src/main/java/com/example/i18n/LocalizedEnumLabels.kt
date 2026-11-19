package com.example.i18n

import com.example.data.model.BacklogChallengeCategory
import com.example.data.model.BacklogChallengeTile
import com.example.data.model.BacklogMilestoneReward
import com.example.data.model.ControllerSupport
import com.example.data.model.GameCategory
import com.example.data.model.LauncherType
import com.example.data.model.LibraryPlayFilter
import com.example.data.model.LibraryViewMode
import com.example.data.model.MetacriticRatingTier
import com.example.data.model.PlayStatus
import com.example.data.model.PlayabilityFilter
import com.example.data.model.SessionStyle
import com.example.data.model.SteamDeckStatus
import com.example.data.model.StorageFilter
import com.example.data.model.StorageTier
import com.example.data.model.TrophyRarityTier
import com.example.preferences.AccentCategory
import com.example.preferences.AccentTheme
import com.example.preferences.BackgroundTheme
import com.example.preferences.CardDensity
import com.example.preferences.DpiScaleMode
import com.example.preferences.TargetFrameRate
import com.example.preferences.ThemeMode
import com.example.ui.MainTab
import com.example.ui.SettingsCategory
import com.example.ui.SortOption
import com.example.ui.TypeFilter
import com.example.ui.calendar.CalendarFilterScope

fun MainTab.localizedLabel(s: AppStrings): String = when (this) {
    MainTab.HOME -> s.common.tabHome
    MainTab.DISCOVER -> s.common.tabDiscover
    MainTab.CLAIMED -> s.common.tabClaimed
    MainTab.CALENDAR -> s.common.tabCalendar
    MainTab.SETTINGS -> s.common.tabSettings
}

fun SortOption.localizedLabel(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "sortOption", name, "label", label)

fun TypeFilter.localizedLabel(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "typeFilter", name, "label", label)

fun SettingsCategory.localizedLabel(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "settingsCategory", name, "label", label)

fun SettingsCategory.localizedDescription(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "settingsCategory", name, "description", description)

fun ThemeMode.localizedLabel(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "themeMode", name, "label", label)

fun CardDensity.localizedLabel(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "cardDensity", name, "label", label)

fun CardDensity.localizedDescription(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "cardDensity", name, "description", description)

fun TargetFrameRate.localizedLabel(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "targetFrameRate", name, "label", label)

fun TargetFrameRate.localizedDescription(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "targetFrameRate", name, "description", description)

fun DpiScaleMode.localizedLabel(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "dpiScaleMode", name, "label", label)

fun DpiScaleMode.localizedShortName(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "dpiScaleMode", name, "shortName", shortName)

fun DpiScaleMode.localizedDescription(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "dpiScaleMode", name, "description", description)

fun AccentCategory.localizedLabel(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "accentCategory", name, "label", label)

fun AccentTheme.localizedTitle(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "accentTheme", name, "title", title)

fun AccentTheme.localizedDescription(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "accentTheme", name, "description", description)

fun AccentTheme.localizedShortName(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "accentTheme", name, "shortName", shortName)

fun BackgroundTheme.localizedTitle(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "backgroundTheme", name, "title", title)

fun LauncherType.localizedDisplayName(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "launcherType", name, "displayName", displayName)

fun GameCategory.localizedDisplayName(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "gameCategory", name, "displayName", displayName)

fun GameCategory.localizedDescription(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "gameCategory", name, "description", description)

fun PlayStatus.localizedLabel(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "playStatus", name, "label", label)

fun LibraryPlayFilter.localizedLabel(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "libraryPlayFilter", name, "label", label)

fun LibraryViewMode.localizedLabel(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "libraryViewMode", name, "label", label)

fun PlayabilityFilter.localizedLabel(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "playabilityFilter", name, "label", label)

fun StorageFilter.localizedLabel(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "storageFilter", name, "label", label)

fun SteamDeckStatus.localizedLabel(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "steamDeckStatus", name, "label", label)

fun SteamDeckStatus.localizedDescription(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "steamDeckStatus", name, "description", description)

fun SteamDeckStatus.localizedPillLabel(s: AppStrings): String = "$iconEmoji ${localizedLabel(s)}"

fun ControllerSupport.localizedLabel(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "controllerSupport", name, "label", label)

fun ControllerSupport.localizedDescription(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "controllerSupport", name, "description", description)

fun SessionStyle.localizedLabel(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "sessionStyle", name, "label", label)

fun SessionStyle.localizedDescription(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "sessionStyle", name, "description", description)

fun StorageTier.localizedDescription(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "storageTier", name, "description", description)

fun CalendarFilterScope.localizedLabel(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "calendarFilterScope", name, "label", label)

fun TrophyRarityTier.localizedLabel(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "trophyRarityTier", name, "label", label)

fun TrophyRarityTier.localizedDescription(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "trophyRarityTier", name, "description", description)

fun BacklogChallengeCategory.localizedDisplayName(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "challengeCategory", name, "displayName", displayName)

fun BacklogChallengeTile.localizedTitle(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "challengeTile", challengeType, "title", title)

fun BacklogChallengeTile.localizedDescription(s: AppStrings): String =
    LocaleAssetCatalog.getField(s.language, "challengeTile", challengeType, "description", description)

fun BacklogMilestoneReward.localizedTitle(s: AppStrings): String =
    LocaleAssetCatalog.getPrefixMatchedField(s.language, "challengeMilestone", id, "title", title)

fun MetacriticRatingTier.localizedLabel(s: AppStrings): String = when (this) {
    MetacriticRatingTier.UNIVERSAL_ACCLAIM -> s.library.metacriticUniversalAcclaim
    MetacriticRatingTier.GENERALLY_FAVORABLE -> s.library.metacriticGenerallyFavorable
    MetacriticRatingTier.MIXED_REVIEWS -> s.library.metacriticMixedAverage
    MetacriticRatingTier.UNFAVORABLE -> s.library.metacriticUnfavorable
}

fun BacklogMilestoneReward.localizedSubtitle(s: AppStrings): String =
    LocaleAssetCatalog.getPrefixMatchedField(s.language, "challengeMilestone", id, "subtitle", subtitle)
