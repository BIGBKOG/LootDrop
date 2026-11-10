package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.local.GiveawayEntity
import com.example.data.model.GiveawayItem
import com.example.data.model.toEntity
import com.example.data.model.LauncherType
import com.example.data.model.LibraryPlayFilter
import com.example.data.model.PlayStatus
import com.example.data.model.SteamDeckStatus
import com.example.data.model.ControllerSupport
import com.example.data.model.SessionStyle
import com.example.data.model.StorageTier
import com.example.data.model.PlayabilityFilter
import com.example.data.model.StorageFilter
import com.example.util.HardwareReadinessCatalog
import com.example.data.remote.SteamApiClient
import com.example.data.remote.SteamOwnedGame
import com.example.notifications.NotificationHelper
import com.example.preferences.AppPreferences
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("LootDrop", appName)
    }

    @Test
    fun `launcher type categorization detects steam epic gog correctly`() {
        val steamItem = GiveawayItem(
            id = 1,
            title = "Half-Life 2",
            worth = "$9.99",
            numericWorth = 9.99,
            thumbnail = "",
            image = "",
            description = "Classic shooter",
            instructions = "",
            openGiveawayUrl = "https://store.steampowered.com",
            publishedDate = "",
            type = "Game",
            platforms = "PC, Steam",
            endDate = "N/A",
            users = 100,
            status = "Active"
        )
        assertEquals(LauncherType.STEAM, steamItem.launcherCategory)

        val epicItem = steamItem.copy(platforms = "Epic Games Store")
        assertEquals(LauncherType.EPIC, epicItem.launcherCategory)

        val gogItem = steamItem.copy(platforms = "GOG, PC")
        assertEquals(LauncherType.GOG, gogItem.launcherCategory)

        val psItem = steamItem.copy(platforms = "Playstation 5, Playstation 4")
        assertEquals(LauncherType.PLAYSTATION, psItem.launcherCategory)
        assertTrue(psItem.isPlayStation)
        assertTrue(psItem.matchesLauncher(LauncherType.PLAYSTATION))

        val xboxItem = steamItem.copy(platforms = "Xbox Series X|S, Xbox One")
        assertEquals(LauncherType.XBOX, xboxItem.launcherCategory)
        assertTrue(xboxItem.isXbox)
        assertTrue(xboxItem.matchesLauncher(LauncherType.XBOX))

        val androidItem = steamItem.copy(platforms = "Android")
        assertEquals(LauncherType.ANDROID, androidItem.launcherCategory)
        assertTrue(androidItem.isAndroid)
        assertTrue(androidItem.matchesLauncher(LauncherType.ANDROID))

        val iosItem = steamItem.copy(platforms = "iOS, iPhone, iPad")
        assertEquals(LauncherType.IOS, iosItem.launcherCategory)
        assertTrue(iosItem.isIos)
        assertTrue(iosItem.matchesLauncher(LauncherType.IOS))

        // Cross-platform item supporting PC, consoles, and mobile
        val crossPlatItem = steamItem.copy(platforms = "PC, Steam, Playstation 5, Xbox Series X|S, Android, iOS")
        assertTrue(crossPlatItem.isPlayStation)
        assertTrue(crossPlatItem.isXbox)
        assertTrue(crossPlatItem.isAndroid)
        assertTrue(crossPlatItem.isIos)
        assertTrue(crossPlatItem.matchesLauncher(LauncherType.PLAYSTATION))
        assertTrue(crossPlatItem.matchesLauncher(LauncherType.XBOX))
        assertTrue(crossPlatItem.matchesLauncher(LauncherType.STEAM))
        assertTrue(crossPlatItem.matchesLauncher(LauncherType.ANDROID))
        assertTrue(crossPlatItem.matchesLauncher(LauncherType.IOS))
    }

    @Test
    fun `claimed items total saved calculation matches item worth values`() {
        val game1 = GiveawayItem(
            id = 10,
            title = "Death Stranding",
            worth = "$39.99",
            numericWorth = 39.99,
            thumbnail = "",
            image = "",
            description = "",
            instructions = "",
            openGiveawayUrl = "https://epicgames.com",
            publishedDate = "",
            type = "Game",
            platforms = "Epic Games Store",
            endDate = "N/A",
            users = 500,
            status = "Active",
            isClaimed = true
        )
        val game2 = GiveawayItem(
            id = 11,
            title = "Fallout: New Vegas",
            worth = "$19.99",
            numericWorth = 19.99,
            thumbnail = "",
            image = "",
            description = "",
            instructions = "",
            openGiveawayUrl = "https://gog.com",
            publishedDate = "",
            type = "Game",
            platforms = "GOG",
            endDate = "N/A",
            users = 300,
            status = "Active",
            isClaimed = true
        )
        val unclaimed = game1.copy(id = 12, isClaimed = false, numericWorth = 59.99)

        val library = listOf(game1, game2, unclaimed)
        val claimedTotal = library.filter { it.isClaimed }.sumOf { it.numericWorth }
        assertEquals(59.98, claimedTotal, 0.001)
    }

    @Test
    fun `notification helper initializes and respects preferences`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = NotificationHelper(context)
        assertNotNull(helper)
        helper.isNotificationsEnabled = true
        assertTrue(helper.isNotificationsEnabled)
        helper.isNotifyPlayStation = true
        assertTrue(helper.isNotifyPlayStation)
        helper.isNotifyXbox = true
        assertTrue(helper.isNotifyXbox)
        helper.isNotificationSoundEnabled = true
        assertTrue(helper.isNotificationSoundEnabled)
        helper.isNotificationVibrateEnabled = true
        assertTrue(helper.isNotificationVibrateEnabled)
        helper.isKeepForeverOnly = true
        assertTrue(helper.isKeepForeverOnly)
        helper.isNotificationRichArtwork = true
        assertTrue(helper.isNotificationRichArtwork)
        helper.isNotificationActionsEnabled = true
        assertTrue(helper.isNotificationActionsEnabled)
    }

    @Test
    fun `auto-check on reopen preference defaults to true and toggles cleanly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = com.example.preferences.AppPreferences(context)
        assertTrue(prefs.isAutoCheckOnReopen)
        prefs.isAutoCheckOnReopen = false
        org.junit.Assert.assertFalse(prefs.isAutoCheckOnReopen)
        prefs.isAutoCheckOnReopen = true
        assertTrue(prefs.isAutoCheckOnReopen)
    }

    @Test
    fun `navigation tabs contain home all the way to the left followed by discover claimed calendar and settings`() {
        val tabNames = com.example.ui.MainTab.values().map { it.name }
        assertEquals(listOf("HOME", "DISCOVER", "CLAIMED", "CALENDAR", "SETTINGS"), tabNames)
        assertEquals("HOME", tabNames.first())
        org.junit.Assert.assertFalse(tabNames.contains("SAVED"))
    }

    @Test
    fun `new today dismissal preference defaults to false and toggles correctly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = com.example.preferences.AppPreferences(context)
        org.junit.Assert.assertFalse(prefs.isNewTodayDismissed)
        prefs.isNewTodayDismissed = true
        assertTrue(prefs.isNewTodayDismissed)
        prefs.isNewTodayDismissed = false
        org.junit.Assert.assertFalse(prefs.isNewTodayDismissed)
    }

    @Test
    fun `settings categories contains appearance alerts general and storage tabs`() {
        val categories = com.example.ui.SettingsCategory.values()
        assertEquals(4, categories.size)
        val names = categories.map { it.name }
        assertEquals(listOf("APPEARANCE", "ALERTS", "GENERAL", "STORAGE"), names)
        val labels = categories.map { it.label }
        assertEquals(listOf("Appearance", "Alerts", "General", "Storage & Data"), labels)
    }

    @Test
    fun `pre-rendering target frame rate supports 60, 90, and 120 FPS`() {
        val frameRates = com.example.preferences.TargetFrameRate.values()
        assertEquals(4, frameRates.size)
        val names = frameRates.map { it.name }
        assertTrue(names.contains("FPS_60"))
        assertTrue(names.contains("FPS_90"))
        assertTrue(names.contains("FPS_120"))
        assertTrue(names.contains("AUTO"))

        val fps90 = com.example.preferences.TargetFrameRate.FPS_90
        assertEquals(90, fps90.fps)
        assertEquals("90 FPS", fps90.label)

        val fps60 = com.example.preferences.TargetFrameRate.FPS_60
        assertEquals(60, fps60.fps)

        val fps120 = com.example.preferences.TargetFrameRate.FPS_120
        assertEquals(120, fps120.fps)

        // Test persistence in AppPreferences
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = com.example.preferences.AppPreferences(context)
        prefs.targetFrameRate = com.example.preferences.TargetFrameRate.FPS_90
        assertEquals(com.example.preferences.TargetFrameRate.FPS_90, prefs.targetFrameRate)
        prefs.targetFrameRate = com.example.preferences.TargetFrameRate.FPS_120
        assertEquals(com.example.preferences.TargetFrameRate.FPS_120, prefs.targetFrameRate)
        prefs.targetFrameRate = com.example.preferences.TargetFrameRate.FPS_60
        assertEquals(com.example.preferences.TargetFrameRate.FPS_60, prefs.targetFrameRate)
    }

    @Test
    fun `screen dpi detection and adaptive computation scales correctly`() {
        // Standard phone test (420 dpi, 411 x 914 dp)
        val phoneInfo = com.example.ui.density.computeScreenDensityInfo(
            densityDpi = 420,
            densityScale = 2.625f,
            fontScale = 1.0f,
            widthPx = 1080,
            heightPx = 2400,
            widthDp = 411,
            heightDp = 914
        )
        assertEquals("XXHDPI (~400-440)", phoneInfo.densityBucket)
        assertEquals(com.example.ui.density.ScreenCategory.STANDARD, phoneInfo.screenCategory)
        assertEquals(1, phoneInfo.gridColumns)
        assertTrue(phoneInfo.isHighDensity)
        org.junit.Assert.assertFalse(phoneInfo.isTabletOrFoldable)

        // Tablet test (320 dpi, 800 x 1280 dp)
        val tabletInfo = com.example.ui.density.computeScreenDensityInfo(
            densityDpi = 320,
            densityScale = 2.0f,
            fontScale = 1.0f,
            widthPx = 1600,
            heightPx = 2560,
            widthDp = 800,
            heightDp = 1280
        )
        assertEquals("XHDPI", tabletInfo.densityBucket)
        assertEquals(com.example.ui.density.ScreenCategory.FOLDABLE, tabletInfo.screenCategory)
        assertEquals(2, tabletInfo.gridColumns)
        assertTrue(tabletInfo.isTabletOrFoldable)

        // Wide desktop/tablet test (widthDp >= 900)
        val wideInfo = com.example.ui.density.computeScreenDensityInfo(
            densityDpi = 320,
            densityScale = 2.0f,
            fontScale = 1.0f,
            widthPx = 2000,
            heightPx = 1200,
            widthDp = 1000,
            heightDp = 600
        )
        assertEquals(3, wideInfo.gridColumns)
        assertEquals(com.example.ui.density.ScreenCategory.TABLET, wideInfo.screenCategory)
    }

    @Test
    fun `dpi scale modes and preferences persist correctly`() {
        val modes = com.example.preferences.DpiScaleMode.values()
        assertEquals(4, modes.size)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = com.example.preferences.AppPreferences(context)
        prefs.isDpiOptimizationEnabled = true
        assertTrue(prefs.isDpiOptimizationEnabled)

        prefs.dpiScaleMode = com.example.preferences.DpiScaleMode.COMPACT
        assertEquals(com.example.preferences.DpiScaleMode.COMPACT, prefs.dpiScaleMode)

        prefs.dpiScaleMode = com.example.preferences.DpiScaleMode.SPACIOUS
        assertEquals(com.example.preferences.DpiScaleMode.SPACIOUS, prefs.dpiScaleMode)

        prefs.dpiScaleMode = com.example.preferences.DpiScaleMode.AUTO_ADAPTIVE
        assertEquals(com.example.preferences.DpiScaleMode.AUTO_ADAPTIVE, prefs.dpiScaleMode)

        prefs.detectedScreenDpi = 480
        assertEquals(480, prefs.detectedScreenDpi)
    }

    @Test
    fun `walkthrough adaptive sizing responds to screen dpi and height`() {
        // Ultra-compact height screen (height < 600)
        val shortPhone = com.example.ui.density.computeScreenDensityInfo(
            densityDpi = 320,
            densityScale = 2.0f,
            fontScale = 1.0f,
            widthPx = 720,
            heightPx = 1100,
            widthDp = 360,
            heightDp = 550
        )
        assertTrue(shortPhone.isUltraCompactHeight)
        assertTrue(shortPhone.isCompactHeight)
        assertEquals(40, shortPhone.walkthroughButtonHeight.value.toInt())
        assertEquals(72, shortPhone.walkthroughAnimBoxSize.value.toInt())

        // Standard phone screen (height >= 700)
        val standardPhone = com.example.ui.density.computeScreenDensityInfo(
            densityDpi = 420,
            densityScale = 2.625f,
            fontScale = 1.0f,
            widthPx = 1080,
            heightPx = 2400,
            widthDp = 411,
            heightDp = 914
        )
        org.junit.Assert.assertFalse(standardPhone.isCompactHeight)
        org.junit.Assert.assertFalse(standardPhone.isUltraCompactHeight)
        assertEquals(48, standardPhone.walkthroughButtonHeight.value.toInt())
        assertEquals(135, standardPhone.walkthroughAnimBoxSize.value.toInt())
    }

    @Test
    fun `default launcher supports Nintendo and persists across restarts`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = AppPreferences(context)
        prefs.defaultLauncher = LauncherType.NINTENDO
        assertEquals(LauncherType.NINTENDO, prefs.defaultLauncher)

        val nintendoItem = GiveawayItem(
            id = 555,
            title = "Super Kirby Clash Gem Apples",
            worth = "$10.00",
            numericWorth = 10.0,
            thumbnail = "https://example.com/thumb.jpg",
            image = "https://example.com/img.jpg",
            description = "Free gem apples on Nintendo Switch eShop",
            instructions = "Redeem on Switch",
            openGiveawayUrl = "https://nintendo.com/item",
            publishedDate = "2026-10-18",
            type = "DLC",
            platforms = "Nintendo Switch",
            endDate = "2026-12-31",
            users = 400,
            status = "Active"
        )
        assertTrue(nintendoItem.isNintendo)
        assertTrue(nintendoItem.matchesLauncher(LauncherType.NINTENDO))
        assertEquals(LauncherType.NINTENDO, nintendoItem.launcherCategory)
    }

    @Test
    fun `data saver and auto prune preferences persist correctly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = AppPreferences(context)

        prefs.isDataSaverMode = true
        assertTrue(prefs.isDataSaverMode)
        prefs.isDataSaverMode = false
        org.junit.Assert.assertFalse(prefs.isDataSaverMode)

        prefs.isAutoPruneExpired = true
        assertTrue(prefs.isAutoPruneExpired)
        prefs.isAutoPruneExpired = false
        org.junit.Assert.assertFalse(prefs.isAutoPruneExpired)
    }

    @Test
    fun `storage metrics formats byte counts accurately`() {
        val metrics = com.example.StorageMetrics(
            imageCacheBytes = 4_500_000L,
            httpCacheBytes = 256_000L,
            databaseBytes = 120_000L,
            totalFootprintBytes = 4_876_000L
        )

        assertEquals("4.3 MB", metrics.formattedImageCache)
        assertEquals("250 KB", metrics.formattedHttpCache)
        assertEquals("117 KB", metrics.formattedDatabase)
        assertEquals("4.7 MB", metrics.formattedTotal)

        val zeroMetrics = com.example.StorageMetrics(0L, 0L, 0L, 0L)
        assertEquals("0 KB", zeroMetrics.formattedTotal)
    }

    @Test
    fun `database vacuum and compaction executes cleanly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.data.local.AppDatabase.vacuumAndCompact(context)
        val metrics = com.example.LootDropApplication.getStorageMetrics(context as android.app.Application)
        assertNotNull(metrics)
    }

    @Test
    fun `notification helper evaluates filters threshold and channels correctly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = NotificationHelper(context)
        assertNotNull(helper)

        val testItem = GiveawayItem(
            id = 777,
            title = "Metro Exodus",
            worth = "$29.99",
            numericWorth = 29.99,
            thumbnail = "https://example.com/thumb.jpg",
            image = "https://example.com/img.jpg",
            description = "Post-apocalyptic masterpiece",
            instructions = "Redeem on Steam",
            openGiveawayUrl = "https://store.steampowered.com",
            publishedDate = "2026-10-19",
            type = "Game",
            platforms = "Steam, PC",
            endDate = "2026-10-25",
            users = 5000,
            status = "Active"
        )

        helper.isNotificationsEnabled = true
        helper.isNotifySteam = true
        helper.isGamesOnly = false
        helper.minWorthAlertThreshold = 0f
        assertTrue(helper.shouldNotifyForGiveaway(testItem))

        // Threshold filter test
        helper.minWorthAlertThreshold = 50f
        org.junit.Assert.assertFalse(helper.shouldNotifyForGiveaway(testItem))
        helper.minWorthAlertThreshold = 20f
        assertTrue(helper.shouldNotifyForGiveaway(testItem))

        // Platform mute test
        helper.isNotifySteam = false
        org.junit.Assert.assertFalse(helper.shouldNotifyForGiveaway(testItem))
        helper.isNotifySteam = true
        assertTrue(helper.shouldNotifyForGiveaway(testItem))

        // Keep-forever only filter test
        val weekendItem = testItem.copy(title = "Battlefield 2042 Free Weekend")
        helper.isKeepForeverOnly = true
        org.junit.Assert.assertFalse(helper.shouldNotifyForGiveaway(weekendItem))
        helper.isKeepForeverOnly = false
        assertTrue(helper.shouldNotifyForGiveaway(weekendItem))
    }

    @Test
    fun `notification history dao records and queries alert inbox`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = com.example.data.local.AppDatabase.getInstance(context)
        val dao = db.notificationHistoryDao()

        dao.clearAllNotifications()
        assertEquals(0, dao.getTotalCount())

        val entry = com.example.data.local.NotificationHistoryEntity(
            giveawayId = 1234,
            title = "Subnautica",
            worth = "$19.99",
            platforms = "Epic Games, PC",
            thumbnail = "https://example.com/sub.jpg",
            image = "https://example.com/sub_banner.jpg",
            openGiveawayUrl = "https://store.epicgames.com",
            channelType = "MAJOR_GAME",
            timestamp = System.currentTimeMillis(),
            isRead = false
        )

        val id = dao.insertNotification(entry)
        assertTrue(id > 0)
        assertEquals(1, dao.getTotalCount())

        dao.markAsRead(id)
        val list = dao.getRecentNotifications()
        assertEquals(1, list.size)
        assertTrue(list.first().isRead)

        dao.clearAllNotifications()
        assertEquals(0, dao.getTotalCount())
    }

    @Test
    fun `notification scheduler configures alarm without crash`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.notifications.NotificationScheduler.schedulePeriodicSync(context)
        com.example.notifications.NotificationScheduler.cancelPeriodicSync(context)
    }

    @Test
    fun `github release version comparison handles semver tags correctly`() {
        // Newer versions
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("V4.1", "4.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("v4.0.1", "4.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("V5.0", "4.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("4.1.0", "4.0"))

        // Same version
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V4.0", "4.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("4.0", "4.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("v4.0", "4.0"))

        // Older version
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V3.5", "4.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V3.9.9", "4.0"))

        // Transition from 4.5 down to 1.5.0 version scheme
        // When running on 1.5.0, older 4.x legacy releases are ignored and NOT considered newer
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V4.5", "1.5.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V4.4", "1.5.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("4.5", "1.5.0"))

        // When running on 1.5.0, exact same versions (with or without V prefix) return false
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V1.5.0", "1.5.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.0", "1.5.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5", "1.5.0"))

        // When running on 1.5.0, newer 1.x releases including wildcard 1.5.X or 1.5.1 are considered newer
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.X", "1.5.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("V1.5.X", "1.5.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.1", "1.5.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.6.0", "1.5.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.0.0", "1.5.0"))

        // When running on 1.5.1
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V1.5.1", "1.5.1"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.1", "1.5.1"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.0", "1.5.1"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V4.5", "1.5.1"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.2", "1.5.1"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.X", "1.5.1"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.6.0", "1.5.1"))

        // When running on 1.5.2
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V1.5.2", "1.5.2"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.2", "1.5.2"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.1", "1.5.2"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.0", "1.5.2"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V4.5", "1.5.2"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.3", "1.5.2"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.X", "1.5.2"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.6.0", "1.5.2"))

        // When running on 1.5.3
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V1.5.3", "1.5.3"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.3", "1.5.3"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.2", "1.5.3"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.1", "1.5.3"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.0", "1.5.3"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V4.5", "1.5.3"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.4", "1.5.3"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.X", "1.5.3"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.6.0", "1.5.3"))

        // When running on 1.5.4
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V1.5.4", "1.5.4"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.4", "1.5.4"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.3", "1.5.4"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.2", "1.5.4"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.1", "1.5.4"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.0", "1.5.4"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V4.5", "1.5.4"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.5", "1.5.4"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.X", "1.5.4"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.6.0", "1.5.4"))

        // When running on 1.5.5
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V1.5.5", "1.5.5"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.5", "1.5.5"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.4", "1.5.5"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.3", "1.5.5"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.2", "1.5.5"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.1", "1.5.5"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.0", "1.5.5"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V4.5", "1.5.5"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.6", "1.5.5"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.X", "1.5.5"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.6.0", "1.5.5"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.0.0", "1.5.5"))

        // When running on 2.0.0
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V2.0.0", "2.0.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.0.0", "2.0.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.5", "2.0.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.0", "2.0.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V4.5", "2.0.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.0.1", "2.0.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.1.0", "2.0.0"))

        // When running on 2.1.0
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V2.1.0", "2.1.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.1.0", "2.1.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.0.0", "2.1.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.5", "2.1.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.1.1", "2.1.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.2.0", "2.1.0"))

        // When running on 2.2.0
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V2.2.0", "2.2.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.2.0", "2.2.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.1.1", "2.2.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.1.0", "2.2.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.0.0", "2.2.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.5", "2.2.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V4.5", "2.2.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.2.1", "2.2.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.3.0", "2.2.0"))

        // When running on 2.3.0
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V2.3.0", "2.3.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.3.0", "2.3.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.2.0", "2.3.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.1.1", "2.3.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.0.0", "2.3.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.5", "2.3.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.3.1", "2.3.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.4.0", "2.3.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("36.0", "2.3.0"))

        // When running on 2.3.5
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V2.3.5", "2.3.5"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.3.5", "2.3.5"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.3.0", "2.3.5"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.2.0", "2.3.5"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.0.0", "2.3.5"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.5", "2.3.5"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.3.6", "2.3.5"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.4.0", "2.3.5"))

        // When running on 2.4.0
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V2.4.0", "2.4.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.4.0", "2.4.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.3.5", "2.4.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.3.0", "2.4.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.2.0", "2.4.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.0.0", "2.4.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.5", "2.4.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.4.1", "2.4.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.5.0", "2.4.0"))

        // When running on 2.5.0
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V2.5.0", "2.5.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.5.0", "2.5.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.4.5", "2.5.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.4.1", "2.5.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.4.0", "2.5.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.3.5", "2.5.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.3.0", "2.5.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.2.0", "2.5.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.0.0", "2.5.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.5", "2.5.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.5.1", "2.5.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("3.0.0", "2.5.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("V3.0.0", "2.5.0"))

        // When running on 3.0.0
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V3.0.0", "3.0.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("3.0.0", "3.0.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.5.1", "3.0.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.5.0", "3.0.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.4.5", "3.0.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.4.1", "3.0.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.4.0", "3.0.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.5", "3.0.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("3.0.1", "3.0.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("3.1.0", "3.0.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("3.1.1", "3.1.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("V3.1.1", "3.1.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("3.1.2", "3.1.1"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("V3.1.2", "3.1.1"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("3.2.0", "3.1.2"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("V3.2.0", "3.1.2"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("3.1.2", "3.2.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V3.1.2", "3.2.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("3.2.0", "3.2.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V3.2.0", "3.2.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("3.2.1", "3.2.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("V3.2.1", "3.2.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("3.1.1", "3.1.2"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V3.1.1", "3.1.2"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("3.1.0", "3.1.1"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V3.1.0", "3.1.1"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("V3.0.1", "3.0.0"))

        // When running on 36.0
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V36.0", "36.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("36.0", "36.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("35.0", "36.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("2.3.0", "36.0"))
        org.junit.Assert.assertFalse(com.example.data.remote.GithubReleaseClient.isNewerVersion("V4.5", "36.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("36.1", "36.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("37.0", "36.0"))

        // If a device is still on legacy 4.5, 1.5.0 / 1.5.X is recognized as the successor
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.X", "V4.5"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("V1.5.0", "V4.5"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isNewerVersion("1.5.0", "4.5"))
    }

    @Test
    fun `github release update preference toggles correctly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = com.example.preferences.AppPreferences(context)
        assertTrue(prefs.isAutoCheckGithubUpdates)
        prefs.isAutoCheckGithubUpdates = false
        org.junit.Assert.assertFalse(prefs.isAutoCheckGithubUpdates)
        prefs.isAutoCheckGithubUpdates = true
        assertTrue(prefs.isAutoCheckGithubUpdates)
    }

    @Test
    fun `update download state model transitions correctly`() {
        val idle = com.example.data.model.UpdateDownloadState.Idle
        assertTrue(idle is com.example.data.model.UpdateDownloadState.Idle)

        val downloading = com.example.data.model.UpdateDownloadState.Downloading(
            progress = 0.5f,
            bytesDownloaded = 5_000_000L,
            totalBytes = 10_000_000L
        )
        assertEquals(0.5f, downloading.progress, 0.001f)
        assertEquals(5_000_000L, downloading.bytesDownloaded)
        assertEquals(10_000_000L, downloading.totalBytes)

        val file = java.io.File("/tmp/test.apk")
        val ready = com.example.data.model.UpdateDownloadState.ReadyToInstall(
            apkFile = file,
            versionTag = "V4.2"
        )
        assertEquals("V4.2", ready.versionTag)
        assertEquals("/tmp/test.apk", ready.apkFile.path)

        val error = com.example.data.model.UpdateDownloadState.Error("Network failure")
        assertEquals("Network failure", error.message)
    }

    @Test
    fun `metacritic score resolves accurately for popular titles and DLC giveaways`() {
        // Direct title lookups
        assertEquals(87, com.example.util.MetacriticCatalog.resolveScore("Subnautica"))
        assertEquals(86, com.example.util.MetacriticCatalog.resolveScore("Death Stranding"))
        assertEquals(96, com.example.util.MetacriticCatalog.resolveScore("Half-Life 2"))
        assertEquals(88, com.example.util.MetacriticCatalog.resolveScore("Civilization VI"))
        assertEquals(93, com.example.util.MetacriticCatalog.resolveScore("Hades"))

        // Giveaway titles with platforms, DLC suffixes, and giveaway noise
        assertEquals(
            83,
            com.example.util.MetacriticCatalog.resolveScore("V Rising - Haunted Nights Castle Pack (Steam) Giveaway")
        )
        assertEquals(
            93,
            com.example.util.MetacriticCatalog.resolveScore("The Witcher 3: Wild Hunt Remastered - Scarlet Crest Armor Giveaway")
        )
        assertEquals(
            80,
            com.example.util.MetacriticCatalog.resolveScore("World of Tanks: Halloween Gift Packs")
        )
        assertEquals(
            85,
            com.example.util.MetacriticCatalog.resolveScore("Warframe Igaro Syandana and Ice Colour Picker Pack Key Giveaway")
        )

        // Tier classification
        val subnauticaInfo = com.example.util.MetacriticCatalog.getMetacriticInfo("Subnautica")
        assertEquals(87, subnauticaInfo.score)
        assertEquals(com.example.data.model.MetacriticRatingTier.GENERALLY_FAVORABLE, subnauticaInfo.tier)
        assertTrue(subnauticaInfo.reviewUrl.contains("metacritic.com"))

        val hl2Info = com.example.util.MetacriticCatalog.getMetacriticInfo("Half-Life 2")
        assertEquals(96, hl2Info.score)
        assertEquals(com.example.data.model.MetacriticRatingTier.UNIVERSAL_ACCLAIM, hl2Info.tier)

        // Tiers
        assertEquals(com.example.data.model.MetacriticRatingTier.UNIVERSAL_ACCLAIM, com.example.data.model.MetacriticRatingTier.fromScore(95))
        assertEquals(com.example.data.model.MetacriticRatingTier.GENERALLY_FAVORABLE, com.example.data.model.MetacriticRatingTier.fromScore(80))
        assertEquals(com.example.data.model.MetacriticRatingTier.MIXED_REVIEWS, com.example.data.model.MetacriticRatingTier.fromScore(65))
        assertEquals(com.example.data.model.MetacriticRatingTier.UNFAVORABLE, com.example.data.model.MetacriticRatingTier.fromScore(45))
    }

    @Test
    fun `cheapshark dto maps store ids correctly to platform names`() {
        assertEquals("Steam", com.example.data.model.CheapSharkDto.storeIdToPlatform("1"))
        assertEquals("Epic Games Store", com.example.data.model.CheapSharkDto.storeIdToPlatform("25"))
        assertEquals("GOG", com.example.data.model.CheapSharkDto.storeIdToPlatform("7"))
        assertEquals("Humble Store", com.example.data.model.CheapSharkDto.storeIdToPlatform("11"))
        assertEquals("Uplay", com.example.data.model.CheapSharkDto.storeIdToPlatform("13"))
        assertEquals("Amazon", com.example.data.model.CheapSharkDto.storeIdToPlatform("4"))
        assertEquals("Origin", com.example.data.model.CheapSharkDto.storeIdToPlatform("8"))
        assertEquals("IndieGala", com.example.data.model.CheapSharkDto.storeIdToPlatform("30"))
        assertEquals("PC", com.example.data.model.CheapSharkDto.storeIdToPlatform("999"))
    }

    @Test
    fun `cheapshark dto converts to giveaway entity with unique id and redirect url`() {
        val deal = com.example.data.model.CheapSharkDto(
            internalName = "SKERRITUAL",
            title = "Sker Ritual",
            metacriticLink = "/game/sker-ritual/",
            dealID = "testDeal123ABC",
            storeID = "25",
            gameID = "251782",
            salePrice = "0.00",
            normalPrice = "24.99",
            isOnSale = "1",
            savings = "100.0",
            metacriticScore = "84",
            steamRatingText = "Very Positive",
            steamRatingPercent = "84",
            steamRatingCount = "3997",
            steamAppID = "1492070",
            releaseDate = 1713398400L,
            thumb = "https://example.com/sker.jpg"
        )

        val entity = deal.toEntity()
        assertTrue(entity.id >= 100000)
        assertEquals("Sker Ritual", entity.title)
        assertEquals("Epic Games Store", entity.platforms)
        assertEquals("$24.99", entity.worth)
        assertEquals(24.99, entity.numericWorth, 0.01)
        assertEquals("https://www.cheapshark.com/redirect?dealID=testDeal123ABC", entity.openGiveawayUrl)
        assertEquals(1492070, entity.steamAppId)
        assertTrue(entity.description.contains("Steam Rating: Very Positive"))
        assertTrue(entity.description.contains("84% positive"))

        // Verify that deal's Metacritic score of 84 is registered and resolves
        assertEquals(84, com.example.util.MetacriticCatalog.resolveScore("Sker Ritual"))
        assertEquals(84, com.example.util.MetacriticCatalog.resolveScore("Unknown Title", steamAppId = 1492070))
        val domain = entity.toDomain()
        assertEquals(84, domain.metacriticScore)
        assertEquals("https://www.metacritic.com/game/sker-ritual/", domain.metacriticInfo.reviewUrl)
    }

    @Test
    fun `gamerpower giveaways synergize with cheapshark to resolve verified metacritic ratings and direct links`() {
        // Given a GamerPower giveaway with a Steam giveaway URL
        val gamerPowerDto = com.example.data.model.GiveawayDto(
            id = 1546,
            title = "Dead by Daylight 100k Bloodpoints Key Giveaway",
            worth = "N/A",
            thumbnail = "https://example.com/dbd.jpg",
            image = "https://example.com/dbd_large.jpg",
            description = "Grab your free Dead by Daylight bloodpoints key.",
            instructions = "Redeem code",
            openGiveawayUrl = "https://store.steampowered.com/app/381210/Dead_by_Daylight/",
            publishedDate = "2026-10-30",
            type = "DLC",
            platforms = "PC, Steam",
            endDate = "N/A",
            users = 5000,
            status = "Active",
            gamerpowerUrl = "https://www.gamerpower.com/open/dead-by-daylight"
        )

        val entity = gamerPowerDto.toEntity()
        assertEquals(381210, entity.steamAppId)

        // Register verified CheapShark rating matching Dead by Daylight's AppID and title
        com.example.util.MetacriticCatalog.registerScore(
            rawTitle = "Dead by Daylight",
            score = 71,
            metacriticPath = "/game/dead-by-daylight/",
            steamAppId = 381210
        )

        assertTrue(com.example.util.MetacriticCatalog.hasVerifiedScore("Dead by Daylight", 381210))
        val item = entity.toDomain()
        assertEquals(71, item.metacriticScore)
        assertEquals("https://www.metacritic.com/game/dead-by-daylight/", item.metacriticInfo.reviewUrl)
        assertTrue(item.metacriticInfo.isVerifiedCatalog)
    }

    @Test
    fun `latest drop widget builds remote views and populates active drop data`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = com.example.data.local.AppDatabase.getInstance(context)
        db.giveawayDao().clearAll()

        val sampleGame = com.example.data.local.GiveawayEntity(
            id = 9991,
            title = "Grand Theft Auto V",
            worth = "$29.99",
            numericWorth = 29.99,
            thumbnail = "",
            image = "",
            description = "Hit action game",
            instructions = "Redeem on Epic",
            openGiveawayUrl = "https://store.epicgames.com",
            publishedDate = "2026-10-27",
            type = "Game",
            platforms = "Epic Games, PC",
            endDate = "2026-11-05",
            users = 10000,
            status = "Active"
        )
        db.giveawayDao().insertAll(listOf(sampleGame))

        val views = android.widget.RemoteViews(context.packageName, R.layout.widget_latest_drop)
        com.example.widget.WidgetUpdateHelper.populateLatestDrop(context, views, 101)
        assertNotNull(views)
    }

    @Test
    fun `radar stats widget builds remote views and populates active count and vault savings`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = com.example.data.local.AppDatabase.getInstance(context)
        db.giveawayDao().clearAll()

        val game1 = com.example.data.local.GiveawayEntity(
            id = 9992,
            title = "Subnautica",
            worth = "$29.99",
            numericWorth = 29.99,
            thumbnail = "",
            image = "",
            description = "",
            instructions = "",
            openGiveawayUrl = "https://store.epicgames.com",
            publishedDate = "2026-10-27",
            type = "Game",
            platforms = "Epic Games",
            endDate = "N/A",
            users = 5000,
            status = "Active",
            isClaimed = true
        )
        val game2 = game1.copy(id = 9993, title = "Half-Life 2", numericWorth = 19.99, isClaimed = false)
        db.giveawayDao().insertAll(listOf(game1, game2))

        val views = android.widget.RemoteViews(context.packageName, R.layout.widget_radar_stats)
        com.example.widget.WidgetUpdateHelper.populateRadarStats(context, views, 102)
        assertNotNull(views)
    }

    @Test
    fun `widget creates rounded bitmap cleanly without hardware bitmap IPC issues`() {
        val src = android.graphics.Bitmap.createBitmap(100, 100, android.graphics.Bitmap.Config.ARGB_8888)
        val rounded = com.example.widget.WidgetUpdateHelper.createRoundedBitmap(src, cornerRadiusPx = 12f, targetWidth = 120, targetHeight = 80)
        assertNotNull(rounded)
        assertEquals(120, rounded.width)
        assertEquals(80, rounded.height)
        assertFalse(rounded.config == android.graphics.Bitmap.Config.HARDWARE)
    }

    @Test
    fun `latest drop widget builds default remote views with image placeholder`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val views = com.example.widget.WidgetUpdateHelper.buildLatestDropRemoteViews(context, 201)
        assertNotNull(views)
    }

    @Test
    fun `share graphic card generator creates 1200x675 bitmap card with required branding and badges`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val item = com.example.data.model.GiveawayItem(
            id = 101,
            title = "Cyberpunk 2077: Phantom Liberty",
            worth = "$29.99",
            numericWorth = 29.99,
            thumbnail = "",
            image = "",
            description = "A gripping spy-thriller expansion for Cyberpunk 2077.",
            instructions = "Claim on Steam / Epic Games / GOG",
            openGiveawayUrl = "https://store.steampowered.com/app/101",
            publishedDate = "2026-10-27 10:00:00",
            type = "Game",
            platforms = "PC, Steam, Epic Games, GOG, PlayStation 5, Xbox Series X|S",
            endDate = "2026-11-15 23:59:00",
            users = 4210,
            status = "Active"
        )

        val bitmap = com.example.util.ShareGraphicGenerator.generateCardBitmap(context, item)
        assertNotNull(bitmap)
        assertEquals(com.example.util.ShareGraphicGenerator.CARD_WIDTH, bitmap.width)
        assertEquals(com.example.util.ShareGraphicGenerator.CARD_HEIGHT, bitmap.height)
        assertEquals(1200, bitmap.width)
        assertEquals(675, bitmap.height)
        assertEquals(android.graphics.Bitmap.Config.ARGB_8888, bitmap.config)

        // Verify Compose ImageBitmap conversion helper functions
        val composeImage = com.example.util.ShareGraphicGenerator.toComposeImageBitmap(bitmap)
        assertNotNull(composeImage)
        val backToAndroid = com.example.util.ShareGraphicGenerator.toAndroidBitmap(composeImage)
        assertNotNull(backToAndroid)
        assertEquals(1200, backToAndroid.width)
    }

    @Test
    fun `share graphic card saves to cache and creates valid fileprovider uri`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val item = com.example.data.model.GiveawayItem(
            id = 202,
            title = "Death Stranding Director's Cut",
            worth = "$39.99",
            numericWorth = 39.99,
            thumbnail = "",
            image = "",
            description = "Epic action adventure.",
            instructions = "Redeem code",
            openGiveawayUrl = "https://store.epicgames.com/p/death-stranding",
            publishedDate = "2026-10-27",
            type = "Game",
            platforms = "Epic Games, PC",
            endDate = "2026-11-05",
            users = 1500,
            status = "Active"
        )

        val bitmap = com.example.util.ShareGraphicGenerator.generateCardBitmap(context, item)
        val cachedFile = com.example.util.ShareGraphicGenerator.saveBitmapToCache(context, bitmap, item.id)
        assertTrue(cachedFile.exists())
        assertTrue(cachedFile.length() > 0)

        val shareUri = com.example.util.ShareGraphicGenerator.getShareableUri(context, cachedFile)
        assertNotNull(shareUri)
        assertTrue(shareUri.toString().startsWith("content://"))
        assertTrue(shareUri.toString().contains("fileprovider"))
    }

    @Test
    fun `dynamic shortcuts are registered with correct IDs, labels, and target tabs`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.shortcuts.AppShortcutHelper.updateDynamicShortcuts(
            context = context,
            expiringCount = 5,
            newDropsCount = 3,
            vaultCount = 12
        )

        val shortcuts = androidx.core.content.pm.ShortcutManagerCompat.getDynamicShortcuts(context)
        assertEquals(3, shortcuts.size)

        val expiring = shortcuts.find { it.id == com.example.shortcuts.AppShortcutHelper.ID_EXPIRING_SOON }
        assertNotNull(expiring)
        assertEquals("Expiring Soon", expiring?.shortLabel)
        assertTrue(expiring?.longLabel.toString().contains("5 Deals"))
        assertEquals(com.example.shortcuts.AppShortcutHelper.ACTION_EXPIRING_SOON, expiring?.intent?.action)
        assertEquals("CALENDAR", expiring?.intent?.getStringExtra("EXTRA_TARGET_TAB"))

        val drops = shortcuts.find { it.id == com.example.shortcuts.AppShortcutHelper.ID_TODAYS_DROPS }
        assertNotNull(drops)
        assertEquals("Today's Drops", drops?.shortLabel)
        assertTrue(drops?.longLabel.toString().contains("3 New"))
        assertEquals(com.example.shortcuts.AppShortcutHelper.ACTION_TODAYS_DROPS, drops?.intent?.action)
        assertEquals("DISCOVER", drops?.intent?.getStringExtra("EXTRA_TARGET_TAB"))
        assertEquals("NEWEST", drops?.intent?.getStringExtra("EXTRA_SORT_OPTION"))

        val vault = shortcuts.find { it.id == com.example.shortcuts.AppShortcutHelper.ID_MY_VAULT }
        assertNotNull(vault)
        assertEquals("My Vault", vault?.shortLabel)
        assertTrue(vault?.longLabel.toString().contains("12 Claimed"))
        assertEquals(com.example.shortcuts.AppShortcutHelper.ACTION_MY_VAULT, vault?.intent?.action)
        assertEquals("CLAIMED", vault?.intent?.getStringExtra("EXTRA_TARGET_TAB"))
    }

    @Test
    fun `play status enum and library filter mapping operate correctly`() {
        assertEquals("Backlog", PlayStatus.NOT_STARTED.label)
        assertEquals("Playing", PlayStatus.PLAYING.label)
        assertEquals("Finished", PlayStatus.BEATEN.label)
        assertEquals("Dropped", PlayStatus.DROPPED.label)

        assertEquals(PlayStatus.NOT_STARTED, PlayStatus.fromString("NOT_STARTED"))
        assertEquals(PlayStatus.PLAYING, PlayStatus.fromString("PLAYING"))
        assertEquals(PlayStatus.BEATEN, PlayStatus.fromString("BEATEN"))
        assertEquals(PlayStatus.BEATEN, PlayStatus.fromString("FINISHED"))
        assertEquals(PlayStatus.DROPPED, PlayStatus.fromString("DROPPED"))
        assertEquals(PlayStatus.NOT_STARTED, PlayStatus.fromString("UNKNOWN"))

        assertEquals(null, LibraryPlayFilter.ALL.status)
        assertEquals(PlayStatus.NOT_STARTED, LibraryPlayFilter.UNPLAYED.status)
        assertEquals(PlayStatus.PLAYING, LibraryPlayFilter.PLAYING.status)
        assertEquals(PlayStatus.BEATEN, LibraryPlayFilter.FINISHED.status)
        assertEquals(PlayStatus.DROPPED, LibraryPlayFilter.DROPPED.status)
    }

    @Test
    fun `steam api classification categorizes unplayed playing finished games based on playtime and achievements`() {
        // 0 minutes playtime -> NOT_STARTED (Backlog)
        val unplayedStatus = SteamApiClient.classifyPlayStatus(
            playtimeMinutes = 0,
            unlockedAchievements = 0,
            totalAchievements = 20
        )
        assertEquals(PlayStatus.NOT_STARTED, unplayedStatus)

        // Some playtime (e.g. 120 min) with incomplete achievements -> PLAYING
        val playingStatus = SteamApiClient.classifyPlayStatus(
            playtimeMinutes = 120,
            unlockedAchievements = 5,
            totalAchievements = 20
        )
        assertEquals(PlayStatus.PLAYING, playingStatus)

        // 100% achievements unlocked -> BEATEN (Finished)
        val finishedAchievementsStatus = SteamApiClient.classifyPlayStatus(
            playtimeMinutes = 300,
            unlockedAchievements = 20,
            totalAchievements = 20
        )
        assertEquals(PlayStatus.BEATEN, finishedAchievementsStatus)

        // 30+ hours playtime (1800+ min) -> BEATEN (Finished)
        val finishedPlaytimeStatus = SteamApiClient.classifyPlayStatus(
            playtimeMinutes = 2400,
            unlockedAchievements = 0,
            totalAchievements = 0
        )
        assertEquals(PlayStatus.BEATEN, finishedPlaytimeStatus)
    }

    @Test
    fun `steam app id extraction and matching matches giveaways with steam games`() {
        assertEquals(105600, SteamApiClient.extractSteamAppId("https://store.steampowered.com/app/105600/Terraria/"))
        assertEquals(220, SteamApiClient.extractSteamAppId("https://store.steampowered.com/app/220/"))
        assertEquals(null, SteamApiClient.extractSteamAppId("https://epicgames.com/store/product/something"))

        val giveaway = GiveawayItem(
            id = 55,
            title = "Half-Life 2",
            worth = "$9.99",
            numericWorth = 9.99,
            thumbnail = "",
            image = "",
            description = "Iconic FPS",
            instructions = "",
            openGiveawayUrl = "https://store.steampowered.com/app/220/HalfLife_2/",
            publishedDate = "",
            type = "Game",
            platforms = "PC, Steam",
            endDate = "N/A",
            users = 500,
            status = "Active"
        )

        val ownedGames = listOf(
            SteamOwnedGame(appId = 400, name = "Portal", playtimeForeverMinutes = 100, rtimeLastPlayed = 0L),
            SteamOwnedGame(appId = 220, name = "Half-Life 2", playtimeForeverMinutes = 750, rtimeLastPlayed = 1700000000L)
        )

        val matched = SteamApiClient.matchSteamGame(giveaway, ownedGames)
        assertNotNull(matched)
        assertEquals(220, matched?.appId)
        assertEquals("Half-Life 2", matched?.name)
        assertEquals(750, matched?.playtimeForeverMinutes)
    }

    @Test
    fun `room database preserves play status and steam telemetry`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val dao = db.giveawayDao()

        val initialEntity = GiveawayEntity(
            id = 999,
            title = "Hades",
            worth = "$24.99",
            numericWorth = 24.99,
            thumbnail = "",
            image = "",
            description = "Rogue-like action",
            instructions = "",
            openGiveawayUrl = "https://store.steampowered.com/app/1145360/Hades/",
            publishedDate = "",
            type = "Game",
            platforms = "PC, Steam",
            endDate = "N/A",
            users = 1000,
            status = "Active",
            isClaimed = true,
            isBookmarked = false,
            playStatus = PlayStatus.NOT_STARTED,
            steamAppId = null,
            steamPlaytimeMinutes = 0,
            steamAchievementsUnlocked = 0,
            steamAchievementsTotal = 0,
            steamLastPlayedTimestamp = 0L
        )

        dao.insertAll(listOf(initialEntity))

        val loadedInitial = dao.getGiveawayById(999)
        assertNotNull(loadedInitial)
        assertEquals(PlayStatus.NOT_STARTED, loadedInitial?.playStatus)
        assertEquals(null, loadedInitial?.steamAppId)

        // Update status manually
        dao.updatePlayStatus(999, PlayStatus.PLAYING)
        val loadedPlaying = dao.getGiveawayById(999)
        assertEquals(PlayStatus.PLAYING, loadedPlaying?.playStatus)

        // Update telemetry via Steam Sync
        dao.updateSteamTelemetry(
            id = 999,
            status = PlayStatus.BEATEN,
            steamAppId = 1145360,
            playtimeMinutes = 3200,
            achievementsUnlocked = 49,
            achievementsTotal = 49,
            lastPlayed = 1720000000L
        )

        val loadedBeaten = dao.getGiveawayById(999)
        assertEquals(PlayStatus.BEATEN, loadedBeaten?.playStatus)
        assertEquals(1145360, loadedBeaten?.steamAppId)
        assertEquals(3200, loadedBeaten?.steamPlaytimeMinutes)
        assertEquals(49, loadedBeaten?.steamAchievementsUnlocked)
        assertEquals(49, loadedBeaten?.steamAchievementsTotal)
        assertEquals(1720000000L, loadedBeaten?.steamLastPlayedTimestamp)

        // Sync with API update: status and telemetry MUST be preserved
        val refreshedEntity = initialEntity.copy(
            users = 1500,
            playStatus = PlayStatus.NOT_STARTED,
            steamAppId = null
        )
        dao.syncGiveawaysPreservingStatus(listOf(refreshedEntity))

        val preserved = dao.getGiveawayById(999)
        assertNotNull(preserved)
        assertEquals(1500, preserved?.users)
        assertEquals(true, preserved?.isClaimed)
        assertEquals(PlayStatus.BEATEN, preserved?.playStatus)
        assertEquals(1145360, preserved?.steamAppId)
        assertEquals(3200, preserved?.steamPlaytimeMinutes)
        assertEquals(49, preserved?.steamAchievementsUnlocked)
        assertEquals(49, preserved?.steamAchievementsTotal)

        db.close()
    }

    @Test
    fun `wishlist matching helper and wishlist recognition work accurately`() {
        val steamGiveaway = GiveawayItem(
            id = 101,
            title = "Cyberpunk 2077",
            worth = "$59.99",
            numericWorth = 59.99,
            thumbnail = "",
            image = "",
            description = "RPG",
            instructions = "",
            openGiveawayUrl = "https://store.steampowered.com/app/1091500/Cyberpunk_2077/",
            publishedDate = "",
            type = "Game",
            platforms = "PC, Steam",
            endDate = "N/A",
            users = 1000,
            status = "Active"
        )

        val wishlistMap = mapOf(1091500 to "Cyberpunk 2077", 220 to "Half-Life 2", 1145360 to "Hades")
        assertTrue(SteamApiClient.isItemOnWishlist(steamGiveaway, wishlistMap))

        val nonWishlisted = steamGiveaway.copy(
            title = "Dota 2",
            openGiveawayUrl = "https://store.steampowered.com/app/570/Dota_2/"
        )
        assertFalse(SteamApiClient.isItemOnWishlist(nonWishlisted, wishlistMap))

        // Title fallback matching
        val epicGiveaway = steamGiveaway.copy(
            openGiveawayUrl = "https://store.epicgames.com/p/cyberpunk-2077",
            platforms = "Epic Games Store"
        )
        assertTrue(SteamApiClient.isItemOnWishlist(epicGiveaway, wishlistMap))
    }

    @Test
    fun `notification helper prioritizes wishlisted giveaways regardless of thresholds`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = NotificationHelper(context)
        helper.isNotificationsEnabled = true
        // Set an extremely high threshold ($100.00) that would normally block standard giveaways
        helper.minWorthAlertThreshold = 100f

        val normalCheapGiveaway = GiveawayItem(
            id = 201,
            title = "Indie Gem",
            worth = "$9.99",
            numericWorth = 9.99,
            thumbnail = "",
            image = "",
            description = "Fun platformer",
            instructions = "",
            openGiveawayUrl = "https://store.steampowered.com/app/999/",
            publishedDate = "",
            type = "Game",
            platforms = "Steam",
            endDate = "N/A",
            users = 100,
            status = "Active",
            isWishlisted = false
        )

        // Normal giveaway is suppressed by threshold
        assertFalse(helper.shouldNotifyForGiveaway(normalCheapGiveaway))

        // Wishlisted giveaway BYPASSES threshold and is always allowed
        val wishlistedGiveaway = normalCheapGiveaway.copy(id = 202, isWishlisted = true)
        assertTrue(helper.shouldNotifyForGiveaway(wishlistedGiveaway))
    }

    @Test
    fun `steam game entity room dao supports insert, queries, and play status updates`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val dao = db.steamGameDao()

        val game1 = com.example.data.local.SteamGameEntity(
            appId = 1091500,
            name = "Cyberpunk 2077",
            playtimeMinutes = 7200,
            playtime2WeeksMinutes = 120,
            imgIconUrl = "icon_hash_1",
            lastPlayedTimestamp = 1720000000L,
            playStatus = PlayStatus.BEATEN,
            achievementsUnlocked = 45,
            achievementsTotal = 45
        )

        val game2 = com.example.data.local.SteamGameEntity(
            appId = 400,
            name = "Portal",
            playtimeMinutes = 0,
            playtime2WeeksMinutes = 0,
            imgIconUrl = "icon_hash_2",
            lastPlayedTimestamp = 0L,
            playStatus = PlayStatus.NOT_STARTED,
            achievementsUnlocked = 0,
            achievementsTotal = 15
        )

        dao.insertAll(listOf(game1, game2))
        assertEquals(2, dao.getCount())

        val portal = dao.getSteamGameById(400)
        assertNotNull(portal)
        assertEquals("Portal", portal?.name)
        assertEquals(PlayStatus.NOT_STARTED, portal?.playStatus)

        // Update play status from Backlog (NOT_STARTED) to PLAYING
        dao.updatePlayStatus(400, PlayStatus.PLAYING)
        val updatedPortal = dao.getSteamGameById(400)
        assertEquals(PlayStatus.PLAYING, updatedPortal?.playStatus)

        // Update to BEATEN (Finished)
        dao.updatePlayStatus(400, PlayStatus.BEATEN)
        assertEquals(PlayStatus.BEATEN, dao.getSteamGameById(400)?.playStatus)

        // Update to DROPPED
        dao.updatePlayStatus(400, PlayStatus.DROPPED)
        assertEquals(PlayStatus.DROPPED, dao.getSteamGameById(400)?.playStatus)

        // Clear all
        dao.clearAll()
        assertEquals(0, dao.getCount())

        db.close()
    }

    @Test
    fun `library view mode preference persists between claimed vault and all plus paid`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = AppPreferences(context)

        // Defaults to VAULT
        assertEquals(com.example.data.model.LibraryViewMode.VAULT, prefs.libraryViewMode)

        // Switch to ALL_PAID
        prefs.libraryViewMode = com.example.data.model.LibraryViewMode.ALL_PAID
        assertEquals(com.example.data.model.LibraryViewMode.ALL_PAID, prefs.libraryViewMode)

        // Switch back to VAULT
        prefs.libraryViewMode = com.example.data.model.LibraryViewMode.VAULT
        assertEquals(com.example.data.model.LibraryViewMode.VAULT, prefs.libraryViewMode)
    }

    @Test
    fun `version 3 2 0 is configured and recognized as modern release stream`() {
        assertEquals("3.2.0", BuildConfig.VERSION_NAME)
        assertEquals(47, BuildConfig.VERSION_CODE)
        assertTrue(com.example.data.remote.GithubReleaseClient.isCurrentModernStream("3.2.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isCurrentModernStream("v3.2.0"))
        assertTrue(com.example.data.remote.GithubReleaseClient.isCurrentModernStream("V3.2.0"))
    }

    @Test
    fun `github release pull and installer resolution operates with v3 2 0 conventions`() {
        val client = com.example.data.remote.GithubReleaseClient(ApplicationProvider.getApplicationContext())
        val apkUrl = client.resolveApkUrl(
            com.example.data.remote.GithubReleaseClient.DEFAULT_OWNER,
            com.example.data.remote.GithubReleaseClient.DEFAULT_REPO,
            "V3.2.0"
        )
        println("RESOLVED APK URL: $apkUrl")
        assertNotNull(apkUrl)
        assertTrue(apkUrl!!.contains("V3.2.0", ignoreCase = true))
        assertTrue(apkUrl!!.endsWith(".apk", ignoreCase = true))

        // Verify installer handles missing APK gracefully without crashing
        val dummyFile = java.io.File(ApplicationProvider.getApplicationContext<android.content.Context>().cacheDir, "non_existent.apk")
        val installed = com.example.util.AppUpdateInstaller.installApk(ApplicationProvider.getApplicationContext(), dummyFile)
        org.junit.Assert.assertFalse(installed)
    }

    @Test
    fun `cloud backup account display name derives deterministic account ID and anchors username`() {
        val id1 = com.example.util.CloudBackupManager.deriveAccountId("Bkiller127")
        val id2 = com.example.util.CloudBackupManager.deriveAccountId("bkiller127")
        val id3 = com.example.util.CloudBackupManager.deriveAccountId("  BKiller127  ")
        assertEquals(id1, id2)
        assertEquals(id1, id3)
        assertTrue(id1.startsWith("acc_bkiller127_"))
        assertTrue(com.example.util.CloudBackupManager.isConfigured())
    }

    @Test
    fun `hardware readiness catalog correctly resolves popular games and heuristics`() {
        // Cyberpunk 2077 (1091500)
        val cpProfile = HardwareReadinessCatalog.getProfile(
            steamAppId = 1091500,
            title = "Cyberpunk 2077",
            platforms = "PC, Steam",
            description = "Night City open world RPG. Requires 70 GB SSD space.",
            manualInstallSizeGb = null
        )
        assertEquals(SteamDeckStatus.VERIFIED, cpProfile.deckStatus)
        assertEquals(ControllerSupport.FULL, cpProfile.controllerSupport)
        assertEquals(SessionStyle.DEEP_DIVE, cpProfile.sessionStyle)
        assertEquals(70.0f, cpProfile.estimatedSizeGb)
        assertEquals(StorageTier.HEAVYWEIGHT, cpProfile.storageTier)

        // Half-Life 2 (220)
        val hlProfile = HardwareReadinessCatalog.getProfile(
            steamAppId = 220,
            title = "Half-Life 2",
            platforms = "PC, Steam",
            description = "Gordon Freeman classic. Requires 6.5 GB available space.",
            manualInstallSizeGb = null
        )
        assertEquals(SteamDeckStatus.VERIFIED, hlProfile.deckStatus)
        assertEquals(StorageTier.COMPACT, hlProfile.storageTier)

        // Hades (1145360) - Quick session
        val hadesProfile = HardwareReadinessCatalog.getProfile(
            steamAppId = 1145360,
            title = "Hades",
            platforms = "PC, Steam",
            description = "Rogue-like dungeon crawler. 15 GB install.",
            manualInstallSizeGb = null
        )
        assertEquals(SteamDeckStatus.VERIFIED, hadesProfile.deckStatus)
        assertEquals(SessionStyle.QUICK_SESSION, hadesProfile.sessionStyle)
        assertEquals(StorageTier.MEDIUM, hadesProfile.storageTier)

        // Portal (400) - Compact / Quick session (4.0 GB)
        val portalProfile = HardwareReadinessCatalog.getProfile(
            steamAppId = 400,
            title = "Portal",
            platforms = "PC, Steam",
            description = "Aperture Science puzzle testing.",
            manualInstallSizeGb = null
        )
        assertEquals(SteamDeckStatus.VERIFIED, portalProfile.deckStatus)
        assertEquals(SessionStyle.QUICK_SESSION, portalProfile.sessionStyle)
        assertEquals(StorageTier.COMPACT, portalProfile.storageTier)

        // Stardew Valley (413150) - Featherweight (< 2 GB, 0.6 GB)
        val stardewProfile = HardwareReadinessCatalog.getProfile(
            steamAppId = 413150,
            title = "Stardew Valley",
            platforms = "PC, Steam",
            description = "Farming RPG.",
            manualInstallSizeGb = null
        )
        assertEquals(SteamDeckStatus.VERIFIED, stardewProfile.deckStatus)
        assertEquals(SessionStyle.QUICK_SESSION, stardewProfile.sessionStyle)
        assertEquals(StorageTier.FEATHERWEIGHT, stardewProfile.storageTier)
    }

    @Test
    fun `manual install size override is reflected in hardware profile`() {
        val autoProfile = HardwareReadinessCatalog.getProfile(
            steamAppId = 1091500,
            title = "Cyberpunk 2077",
            platforms = "PC, Steam",
            description = "Requires 70 GB",
            manualInstallSizeGb = null
        )
        assertFalse(autoProfile.isManualOverride)
        assertEquals("70 GB", autoProfile.formattedSize)

        val manualProfile = HardwareReadinessCatalog.getProfile(
            steamAppId = 1091500,
            title = "Cyberpunk 2077",
            platforms = "PC, Steam",
            description = "Requires 70 GB",
            manualInstallSizeGb = 86.4f
        )
        assertTrue(manualProfile.isManualOverride)
        assertEquals(86.4f, manualProfile.effectiveSizeGb)
        assertEquals("86.4 GB", manualProfile.formattedSize)
        assertEquals(StorageTier.HEAVYWEIGHT, manualProfile.storageTier)
    }

    @Test
    fun `room database preserves and updates manual install size for giveaways and steam games`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val giveawayDao = db.giveawayDao()
        val steamDao = db.steamGameDao()

        // Insert giveaway with null manual size
        val entity = GiveawayEntity(
            id = 99,
            title = "Subnautica",
            worth = "$29.99",
            numericWorth = 29.99,
            thumbnail = "",
            image = "",
            description = "Requires 20 GB",
            instructions = "",
            openGiveawayUrl = "https://epicgames.com",
            publishedDate = "",
            type = "Game",
            platforms = "PC",
            endDate = "N/A",
            users = 100,
            status = "Active",
            isClaimed = true,
            manualInstallSizeGb = null
        )
        giveawayDao.insertAll(listOf(entity))
        assertEquals(null, giveawayDao.getGiveawayById(99)?.manualInstallSizeGb)

        // Update manual install size to 18.5 GB
        giveawayDao.updateManualInstallSize(99, 18.5f)
        val updatedGiveaway = giveawayDao.getGiveawayById(99)
        assertEquals(18.5f, updatedGiveaway?.manualInstallSizeGb)

        // Syncing giveaways preserving status also preserves manualInstallSizeGb
        val incomingUpdate = entity.copy(worth = "$19.99", numericWorth = 19.99)
        giveawayDao.syncGiveawaysPreservingStatus(listOf(incomingUpdate))
        val postSyncGiveaway = giveawayDao.getGiveawayById(99)
        assertEquals(18.5f, postSyncGiveaway?.manualInstallSizeGb)

        // Test Steam Game entity manual size
        val steamGame = com.example.data.local.SteamGameEntity(
            appId = 264710,
            name = "Subnautica",
            playtimeMinutes = 120,
            imgIconUrl = "",
            headerImageUrl = "",
            manualInstallSizeGb = null
        )
        steamDao.insertAll(listOf(steamGame))
        assertEquals(null, steamDao.getSteamGameById(264710)?.manualInstallSizeGb)

        steamDao.updateManualInstallSize(264710, 19.2f)
        assertEquals(19.2f, steamDao.getSteamGameById(264710)?.manualInstallSizeGb)

        db.close()
    }

    @Test
    fun `formatStorageGb formats MB, GB and TB correctly`() {
        assertEquals("0 GB", com.example.data.model.formatStorageGb(0f))
        assertEquals("512 MB", com.example.data.model.formatStorageGb(0.5f))
        assertEquals("4 GB", com.example.data.model.formatStorageGb(4.0f))
        assertEquals("18.5 GB", com.example.data.model.formatStorageGb(18.5f))
        assertEquals("1.0 TB", com.example.data.model.formatStorageGb(1024f))

        val stats = com.example.ui.UiStats(
            claimedTotalStorageGb = 64f,
            steamTotalStorageGb = 1500f
        )
        assertEquals("64 GB", stats.claimedStorageFormatted)
        assertEquals("1.5 TB", stats.steamStorageFormatted)
    }

    @Test
    fun `trophy rarity tiers classify global percentages accurately`() {
        fun makeAch(percent: Float) = com.example.data.local.SteamAchievementEntity(
            appId = 1,
            apiName = "ACH",
            displayName = "Test",
            description = "",
            isAchieved = true,
            globalPercentage = percent
        ).rarityTier

        assertEquals(com.example.data.model.TrophyRarityTier.MYTHIC, makeAch(0.4f))
        assertEquals(com.example.data.model.TrophyRarityTier.MYTHIC, makeAch(4.99f))
        assertEquals(com.example.data.model.TrophyRarityTier.GOLD, makeAch(5.0f))
        assertEquals(com.example.data.model.TrophyRarityTier.GOLD, makeAch(14.9f))
        assertEquals(com.example.data.model.TrophyRarityTier.SILVER, makeAch(15.0f))
        assertEquals(com.example.data.model.TrophyRarityTier.SILVER, makeAch(34.9f))
        assertEquals(com.example.data.model.TrophyRarityTier.BRONZE, makeAch(35.0f))
        assertEquals(com.example.data.model.TrophyRarityTier.BRONZE, makeAch(64.9f))
    }

    @Test
    fun `curated achievements catalog provides rich initial showcase data`() {
        val showcaseGames = com.example.util.CuratedAchievementsCatalog.getInitialShowcaseGames()
        assertTrue(showcaseGames.isNotEmpty())
        assertTrue(showcaseGames.any { it.name == "Portal 2" })
        assertTrue(showcaseGames.any { it.name == "Hollow Knight" })
        assertTrue(showcaseGames.any { it.name == "Cyberpunk 2077" })

        val portalAchievements = com.example.util.CuratedAchievementsCatalog.getAchievementsForApp(620, "Portal 2")
        assertTrue(portalAchievements.isNotEmpty())
        assertTrue(portalAchievements.any { it.displayName == "Smash TV" })
        assertTrue(portalAchievements.all { it.globalPercentage > 0f })

        val allSeedAchievements = com.example.util.CuratedAchievementsCatalog.getInitialAchievements()
        assertTrue(allSeedAchievements.size >= 20)
        assertTrue(allSeedAchievements.any { it.isAchieved })
    }

    @Test
    fun `achievement walkthrough helper generates valid community and video guide urls`() {
        val steamUrl = com.example.util.AchievementWalkthroughHelper.buildSteamCommunityGuidesUrl(620)
        assertEquals("https://steamcommunity.com/app/620/guides/", steamUrl)

        val ytUrl = com.example.util.AchievementWalkthroughHelper.buildYouTubeWalkthroughUrl("Portal 2", "Smash TV")
        assertTrue(ytUrl.startsWith("https://www.youtube.com/results?search_query="))
        assertTrue(ytUrl.contains("Portal"))
        assertTrue(ytUrl.contains("Smash"))

        val webUrl = com.example.util.AchievementWalkthroughHelper.buildWebGuideUrl("Hollow Knight", "Voidheart")
        assertTrue(webUrl.startsWith("https://www.google.com/search?q="))
        assertTrue(webUrl.contains("Hollow"))
    }

    @Test
    fun `steam achievement dao persists achievements and queries rarest correctly`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val achievementDao = db.steamAchievementDao()

        val ach1 = com.example.data.local.SteamAchievementEntity(
            apiName = "ACH_01",
            appId = 100,
            gameName = "Game A",
            displayName = "Ultra Mythic",
            description = "Beat on hardest difficulty",
            iconUrl = "",
            isAchieved = true,
            unlockTimestamp = 1600000000L,
            globalPercentage = 1.2f
        )
        val ach2 = com.example.data.local.SteamAchievementEntity(
            apiName = "ACH_02",
            appId = 100,
            gameName = "Game A",
            displayName = "Common Task",
            description = "Start the game",
            iconUrl = "",
            isAchieved = true,
            unlockTimestamp = 1500000000L,
            globalPercentage = 85.0f
        )
        val achLocked = com.example.data.local.SteamAchievementEntity(
            apiName = "ACH_03",
            appId = 100,
            gameName = "Game A",
            displayName = "Locked Secret",
            description = "Find hidden egg",
            iconUrl = "",
            isAchieved = false,
            unlockTimestamp = 0L,
            globalPercentage = 0.5f
        )

        achievementDao.insertAll(listOf(ach1, ach2, achLocked))

        // Check getAchievementsForApp
        val appAchievements = achievementDao.getAchievementsForApp(100)
        assertEquals(3, appAchievements.size)

        // Check getRarestTrophies - only unlocked, sorted ascending by globalPercentage
        val rarest = achievementDao.getRarestTrophies(limit = 10)
        assertEquals(2, rarest.size)
        assertEquals("Ultra Mythic", rarest[0].displayName)
        assertEquals(1.2f, rarest[0].globalPercentage, 0.01f)
        assertEquals("Common Task", rarest[1].displayName)

        db.close()
    }

    @Test
    fun `backlog roulette candidate filtering and UnifiedLibraryItem modeling`() {
        val steamUnplayed = com.example.data.local.SteamGameEntity(
            appId = 10,
            name = "Counter-Strike",
            playtimeMinutes = 0,
            playStatus = PlayStatus.NOT_STARTED
        )
        val steamPlaying = com.example.data.local.SteamGameEntity(
            appId = 730,
            name = "CS:GO",
            playtimeMinutes = 120,
            playStatus = PlayStatus.PLAYING
        )
        val steamBeaten = com.example.data.local.SteamGameEntity(
            appId = 400,
            name = "Portal",
            playtimeMinutes = 300,
            playStatus = PlayStatus.BEATEN
        )

        val vaultUnplayed = GiveawayItem(
            id = 501,
            title = "Death Stranding",
            worth = "$29.99",
            numericWorth = 29.99,
            thumbnail = "https://thumb.url",
            image = "https://image.url",
            description = "Strand game",
            instructions = "Claim on Epic",
            openGiveawayUrl = "https://epicgames.com/claim/501",
            publishedDate = "2024-01-01",
            type = "Game",
            platforms = "Epic Games, PC",
            endDate = "2024-01-10",
            users = 5000,
            status = "Active",
            playStatus = PlayStatus.NOT_STARTED
        )
        val vaultDropped = vaultUnplayed.copy(id = 502, title = "Unwanted Game", playStatus = PlayStatus.DROPPED)

        val unifiedSteamUnplayed = com.example.data.model.UnifiedLibraryItem.Steam(steamUnplayed)
        val unifiedSteamPlaying = com.example.data.model.UnifiedLibraryItem.Steam(steamPlaying)
        val unifiedSteamBeaten = com.example.data.model.UnifiedLibraryItem.Steam(steamBeaten)
        val unifiedVaultUnplayed = com.example.data.model.UnifiedLibraryItem.Vault(vaultUnplayed)
        val unifiedVaultDropped = com.example.data.model.UnifiedLibraryItem.Vault(vaultDropped)

        // Verify Unified properties
        assertEquals("steam_10", unifiedSteamUnplayed.key)
        assertEquals("Counter-Strike", unifiedSteamUnplayed.name)
        assertEquals(PlayStatus.NOT_STARTED, unifiedSteamUnplayed.playStatus)
        assertEquals("Steam", unifiedSteamUnplayed.launcherLabel)
        assertEquals("steam://run/10", unifiedSteamUnplayed.launchUri)

        assertEquals("vault_501", unifiedVaultUnplayed.key)
        assertEquals("Death Stranding", unifiedVaultUnplayed.name)
        assertEquals(PlayStatus.NOT_STARTED, unifiedVaultUnplayed.playStatus)
        assertEquals("Epic Games", unifiedVaultUnplayed.launcherLabel)
        assertEquals("https://epicgames.com/claim/501", unifiedVaultUnplayed.launchUri)

        // All+Paid collection
        val allItems = listOf(
            unifiedSteamUnplayed,
            unifiedSteamPlaying,
            unifiedSteamBeaten,
            unifiedVaultUnplayed,
            unifiedVaultDropped
        )

        // Filter unplayed or partially completed backlog candidates
        val backlogCandidates = allItems.filter {
            it.playStatus == PlayStatus.NOT_STARTED || it.playStatus == PlayStatus.PLAYING
        }

        assertEquals(3, backlogCandidates.size)
        assertTrue(backlogCandidates.any { it.name == "Counter-Strike" })
        assertTrue(backlogCandidates.any { it.name == "CS:GO" })
        assertTrue(backlogCandidates.any { it.name == "Death Stranding" })
        assertFalse(backlogCandidates.any { it.name == "Portal" })
        assertFalse(backlogCandidates.any { it.name == "Unwanted Game" })
    }

    @Test
    fun `backup and restore manager exports and restores library across devices`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val prefs = AppPreferences(context)
        prefs.steamUserId = "76561198000123456"

        val giveawayDao = db.giveawayDao()
        val steamDao = db.steamGameDao()

        // Seed 1 claimed game and 1 non-claimed game
        val claimedGame = GiveawayEntity(
            id = 101,
            title = "Subnautica",
            worth = "$29.99",
            numericWorth = 29.99,
            thumbnail = "https://thumb.url",
            image = "https://image.url",
            description = "Underwater survival",
            instructions = "Claim on Epic",
            openGiveawayUrl = "https://epicgames.com/subnautica",
            publishedDate = "2024-01-01",
            type = "Game",
            platforms = "Epic Games, PC",
            endDate = "2024-01-10",
            users = 10000,
            status = "Active",
            isClaimed = true,
            isBookmarked = true,
            playStatus = PlayStatus.PLAYING,
            manualInstallSizeGb = 8.5f
        )
        val unclaimedGame = claimedGame.copy(id = 102, title = "Unclaimed Demo", isClaimed = false, isBookmarked = false, playStatus = PlayStatus.NOT_STARTED, manualInstallSizeGb = null)

        val steamGame = com.example.data.local.SteamGameEntity(
            appId = 220,
            name = "Half-Life 2",
            playtimeMinutes = 540,
            playStatus = PlayStatus.BEATEN,
            achievementsUnlocked = 33,
            achievementsTotal = 33,
            manualInstallSizeGb = 6.0f
        )

        giveawayDao.insertAll(listOf(claimedGame, unclaimedGame))
        steamDao.insertAll(listOf(steamGame))

        // 1. Export Backup to JSON
        val allGiveaways = giveawayDao.getAllGiveawaysList()
        val allSteam = steamDao.getAllSteamGamesList()
        val jsonOutput = com.example.util.BackupRestoreManager.createBackupJson(
            allGiveaways = allGiveaways,
            steamGames = allSteam,
            steamUserId = prefs.steamUserId
        )

        assertNotNull(jsonOutput)
        assertTrue(jsonOutput.contains("LootDrop"))
        assertTrue(jsonOutput.contains("Subnautica"))
        assertTrue(jsonOutput.contains("Half-Life 2"))
        assertTrue(jsonOutput.contains("76561198000123456"))

        // 2. Simulate restoring on a brand new, empty device
        val newDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val newPrefs = AppPreferences(context)
        newPrefs.steamUserId = "" // empty on fresh device

        val restoreSummary = com.example.util.BackupRestoreManager.parseAndRestoreJson(
            jsonString = jsonOutput,
            database = newDb,
            preferences = newPrefs
        )

        assertEquals(1, restoreSummary.claimedRestoredCount)
        assertEquals(1, restoreSummary.bookmarkedRestoredCount)
        assertEquals(1, restoreSummary.steamRestoredCount)
        assertEquals(2, restoreSummary.totalRestoredCount)

        // Verify database contents on the restored device
        val restoredClaimed = newDb.giveawayDao().getGiveawayById(101)
        assertNotNull(restoredClaimed)
        assertEquals("Subnautica", restoredClaimed?.title)
        assertTrue(restoredClaimed?.isClaimed == true)
        assertTrue(restoredClaimed?.isBookmarked == true)
        assertEquals(PlayStatus.PLAYING, restoredClaimed?.playStatus)
        assertEquals(8.5f, restoredClaimed?.manualInstallSizeGb)

        val restoredSteam = newDb.steamGameDao().getSteamGameById(220)
        assertNotNull(restoredSteam)
        assertEquals("Half-Life 2", restoredSteam?.name)
        assertEquals(PlayStatus.BEATEN, restoredSteam?.playStatus)
        assertEquals(540, restoredSteam?.playtimeMinutes)
        assertEquals(6.0f, restoredSteam?.manualInstallSizeGb)

        assertEquals("76561198000123456", newPrefs.steamUserId)

        db.close()
        newDb.close()
    }

    @Test
    fun `user profile persists in app data and exports and restores correctly through JSON backup`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = AppPreferences(context)
        prefs.userProfileName = "Valkyrie99"

        // Create a dummy avatar file in app data
        val avatarFile = java.io.File(context.filesDir, com.example.util.UserProfileManager.AVATAR_FILE_NAME)
        avatarFile.writeText("fake_avatar_image_bytes")
        prefs.userProfileAvatarPath = avatarFile.absolutePath

        val profile = com.example.util.UserProfileManager.getProfile(prefs)
        assertEquals("Valkyrie99", profile.name)
        assertEquals("Valkyrie99", profile.displayName)
        assertEquals("V", profile.initial)
        assertTrue(profile.hasCustomAvatar)

        // Export to JSON
        val emptyGiveaways = emptyList<com.example.data.local.GiveawayEntity>()
        val emptySteam = emptyList<com.example.data.local.SteamGameEntity>()
        val avatarBase64 = com.example.util.UserProfileManager.getAvatarBase64(context, profile.avatarPath)
        assertTrue(avatarBase64.isNotEmpty())

        val backupJson = com.example.util.BackupRestoreManager.createBackupJson(
            allGiveaways = emptyGiveaways,
            steamGames = emptySteam,
            steamUserId = "12345",
            userProfileName = profile.name,
            userProfileAvatarBase64 = avatarBase64
        )
        assertTrue(backupJson.contains("\"userProfile\""))
        assertTrue(backupJson.contains("Valkyrie99"))
        assertTrue(backupJson.contains("\"avatarBase64\""))

        // Simulate restore on a fresh device
        val newDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val newPrefs = AppPreferences(context)
        newPrefs.userProfileName = ""
        newPrefs.userProfileAvatarPath = ""
        com.example.util.UserProfileManager.deleteAvatarFile(context)

        assertFalse(newPrefs.userProfileName == "Valkyrie99")

        val restoreResult = com.example.util.BackupRestoreManager.parseAndRestoreJson(
            jsonString = backupJson,
            database = newDb,
            preferences = newPrefs
        )
        assertNotNull(restoreResult)
        assertEquals("Valkyrie99", newPrefs.userProfileName)
        assertTrue(newPrefs.userProfileAvatarPath.isNotBlank())
        val restoredFile = java.io.File(newPrefs.userProfileAvatarPath)
        assertTrue(restoredFile.exists())
        assertEquals("fake_avatar_image_bytes", restoredFile.readText())

        newDb.close()
    }

    @Test
    fun `user profile custom trophy showcase selection persists and exports correctly`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = AppPreferences(context)
        prefs.userProfileName = "TrophyHunter"
        prefs.hasCustomTrophySelection = false
        prefs.displayedTrophyKeys = emptySet()

        val initialProfile = com.example.util.UserProfileManager.getProfile(prefs)
        assertFalse(initialProfile.hasCustomTrophySelection)
        assertTrue(initialProfile.displayedTrophyKeys.isEmpty())

        // Choose custom trophies to display
        val selectedKeys = setOf("730_ach_ace", "1091500_ach_cyberpunk")
        val updatedProfile = com.example.util.UserProfileManager.saveDisplayedTrophies(
            preferences = prefs,
            hasCustom = true,
            keys = selectedKeys
        )

        assertTrue(updatedProfile.hasCustomTrophySelection)
        assertEquals(2, updatedProfile.displayedTrophyKeys.size)
        assertTrue(updatedProfile.displayedTrophyKeys.contains("730_ach_ace"))
        assertTrue(updatedProfile.displayedTrophyKeys.contains("1091500_ach_cyberpunk"))

        // Verify JSON export and restore
        val emptyGiveaways = emptyList<com.example.data.local.GiveawayEntity>()
        val emptySteam = emptyList<com.example.data.local.SteamGameEntity>()
        val backupJson = com.example.util.BackupRestoreManager.createBackupJson(
            allGiveaways = emptyGiveaways,
            steamGames = emptySteam,
            steamUserId = "12345",
            userProfileName = updatedProfile.name,
            userProfileHasCustomTrophies = updatedProfile.hasCustomTrophySelection,
            userProfileDisplayedTrophyKeys = updatedProfile.displayedTrophyKeys
        )

        assertTrue(backupJson.contains("\"hasCustomTrophies\""))
        assertTrue(backupJson.contains("730_ach_ace"))

        // Fresh restore
        val newDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val newPrefs = AppPreferences(context)
        newPrefs.hasCustomTrophySelection = false
        newPrefs.displayedTrophyKeys = emptySet()

        com.example.util.BackupRestoreManager.parseAndRestoreJson(
            jsonString = backupJson,
            database = newDb,
            preferences = newPrefs
        )

        assertTrue(newPrefs.hasCustomTrophySelection)
        assertTrue(newPrefs.displayedTrophyKeys.contains("730_ach_ace"))
        assertTrue(newPrefs.displayedTrophyKeys.contains("1091500_ach_cyberpunk"))
        newDb.close()
    }

    @Test
    fun `auto backup preferences persist interval and cloud folder configuration correctly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = AppPreferences(context)

        // Default interval is 2 days ("every couple days")
        assertEquals(2, prefs.autoBackupIntervalDays)
        assertFalse(prefs.isAutoBackupEnabled)

        prefs.isAutoBackupEnabled = true
        prefs.autoBackupFolderUri = "content://com.android.providers.downloads.documents/tree/downloads"
        prefs.autoBackupFolderDisplayName = "Google Drive / LootDrop Backups"
        prefs.autoBackupIntervalDays = 3

        assertTrue(prefs.isAutoBackupEnabled)
        assertEquals("content://com.android.providers.downloads.documents/tree/downloads", prefs.autoBackupFolderUri)
        assertEquals("Google Drive / LootDrop Backups", prefs.autoBackupFolderDisplayName)
        assertEquals(3, prefs.autoBackupIntervalDays)

        // Test display name resolution utility
        val folderUri = android.net.Uri.parse("content://com.android.externalstorage.documents/tree/primary%3ABackups")
        val resolvedName = com.example.util.BackupRestoreManager.getFolderDisplayName(context, folderUri)
        assertNotNull(resolvedName)
        assertTrue(resolvedName.isNotBlank())

        // Test scheduler registration and cancellation without crash
        com.example.notifications.AutoBackupScheduler.schedulePeriodicAutoBackup(context)
        com.example.notifications.AutoBackupScheduler.cancelPeriodicAutoBackup(context)
    }

    @Test
    fun `background steam sync detects newly unlocked mythic achievement and triggers in-app notification popup`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()

        // 1. Initial state: Achievement is locked in the database
        val lockedAch = com.example.data.local.SteamAchievementEntity(
            appId = 620,
            apiName = "ACH_SMASH_TV",
            displayName = "Smash TV",
            description = "Break 11 test chamber monitors throughout Aperture Science.",
            isAchieved = false,
            unlockTimestamp = 0L,
            globalPercentage = 3.6f, // Mythic tier (< 5%)
            iconUrl = "https://cdn.cloudflare.steamstatic.com/steamcommunity/public/images/apps/620/b388277fe76b3a9c7d1e1fa37c570bca554901ba.jpg",
            gameName = "Portal 2"
        )
        db.steamAchievementDao().insertAll(listOf(lockedAch))

        // Reset any pending notification before test
        com.example.util.MythicAchievementNotificationManager.dismissNotification(context)
        assertEquals(null, com.example.util.MythicAchievementNotificationManager.pendingNotification.value)

        // 2. Background sync detects achievement is now UNLOCKED
        val unlockedAch = lockedAch.copy(
            isAchieved = true,
            unlockTimestamp = System.currentTimeMillis() / 1000L
        )

        val newlyUnlocked = com.example.util.SteamSyncHelper.checkAndNotifyMythicAchievements(
            context = context,
            db = db,
            achievements = listOf(unlockedAch),
            sendSystemNotification = true
        )

        // Verify mythic achievement was detected as newly unlocked
        assertEquals(1, newlyUnlocked.size)
        assertEquals("Smash TV", newlyUnlocked.first().displayName)
        assertEquals(com.example.data.model.TrophyRarityTier.MYTHIC, newlyUnlocked.first().rarityTier)

        // Verify in-app notification popup state is populated with achievement and icon
        val inAppNotification = com.example.util.MythicAchievementNotificationManager.pendingNotification.value
        assertNotNull(inAppNotification)
        assertEquals("Smash TV", inAppNotification?.displayName)
        assertEquals("Portal 2", inAppNotification?.gameName)
        assertEquals("3.6%", inAppNotification?.formattedRarity)
        assertEquals(unlockedAch.iconUrl, inAppNotification?.iconUrl)
        assertTrue(inAppNotification?.effectiveIconUrl?.isNotBlank() == true)

        // Verify persisted to preferences for restore upon app re-entry
        val prefs = AppPreferences(context)
        assertNotNull(prefs.pendingMythicAchievementJson)
        assertTrue(prefs.pendingMythicAchievementJson!!.contains("Smash TV"))

        // Test restoring from preferences
        com.example.util.MythicAchievementNotificationManager.dismissNotification()
        assertEquals(null, com.example.util.MythicAchievementNotificationManager.pendingNotification.value)

        com.example.util.MythicAchievementNotificationManager.checkPendingFromPreferences(context)
        val restored = com.example.util.MythicAchievementNotificationManager.pendingNotification.value
        assertNotNull(restored)
        assertEquals("Smash TV", restored?.displayName)

        // Dismiss clears notification and preferences
        com.example.util.MythicAchievementNotificationManager.dismissNotification(context)
        assertEquals(null, com.example.util.MythicAchievementNotificationManager.pendingNotification.value)
        assertEquals(null, prefs.pendingMythicAchievementJson)

        db.close()
    }

    @Test
    fun `mythic detection ignores already unlocked achievements and non-mythic achievements`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()

        // Achievement is ALREADY unlocked
        val alreadyUnlockedMythic = com.example.data.local.SteamAchievementEntity(
            appId = 1091500,
            apiName = "ACH_CP_AUTODIDACT",
            displayName = "Autodidact",
            description = "Develop any skill to the maximum level in Night City.",
            isAchieved = true,
            unlockTimestamp = 1600000000L,
            globalPercentage = 4.2f, // Mythic tier
            iconUrl = "",
            gameName = "Cyberpunk 2077"
        )
        db.steamAchievementDao().insertAll(listOf(alreadyUnlockedMythic))

        // Non-mythic unlocked achievement (> 5%, e.g. Gold tier 14.5%)
        val commonUnlocked = com.example.data.local.SteamAchievementEntity(
            appId = 1091500,
            apiName = "ACH_CP_GUNSLINGER",
            displayName = "Gunslinger",
            description = "Shoot an enemy grenade in mid-air.",
            isAchieved = true,
            unlockTimestamp = 1600000000L,
            globalPercentage = 14.5f, // Gold tier, NOT mythic
            iconUrl = "",
            gameName = "Cyberpunk 2077"
        )

        com.example.util.MythicAchievementNotificationManager.dismissNotification(context)

        // Run sync check on both
        val newlyDetected = com.example.util.SteamSyncHelper.checkAndNotifyMythicAchievements(
            context = context,
            db = db,
            achievements = listOf(alreadyUnlockedMythic, commonUnlocked),
            sendSystemNotification = false
        )

        // Should NOT trigger notification for already unlocked or non-mythic
        assertEquals(0, newlyDetected.size)
        assertEquals(null, com.example.util.MythicAchievementNotificationManager.pendingNotification.value)

        db.close()
    }

    @Test
    fun `system notification helper sends mythic achievement alert with custom icon`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = NotificationHelper(context)
        helper.isNotificationsEnabled = true

        val mythicAch = com.example.data.local.SteamAchievementEntity(
            appId = 367520,
            apiName = "ACH_PURE_VESSEL",
            displayName = "Pure Completion",
            description = "Attain 112% game completion and finish the Pantheon of Hallownest.",
            isAchieved = true,
            unlockTimestamp = System.currentTimeMillis() / 1000L,
            globalPercentage = 2.1f,
            iconUrl = "https://cdn.cloudflare.steamstatic.com/steamcommunity/public/images/apps/367520/226d7fa02640243feab2e342cbcc4ba8746df8f7.jpg",
            gameName = "Hollow Knight"
        )

        // Verify sendMythicAchievementNotification dispatches cleanly
        helper.sendMythicAchievementNotification(mythicAch)
        assertTrue(mythicAch.rarityTier == com.example.data.model.TrophyRarityTier.MYTHIC)
        assertEquals("2.1%", mythicAch.formattedRarity)
    }

    @Test
    fun `official ost catalog resolves curated soundtracks with direct spotify and youtube music links`() {
        // Subnautica curated OST
        val subnautica = GiveawayItem(
            id = 101,
            title = "Subnautica",
            worth = "$29.99",
            numericWorth = 29.99,
            thumbnail = "",
            image = "",
            description = "Underwater survival",
            instructions = "",
            openGiveawayUrl = "https://store.epicgames.com",
            publishedDate = "",
            type = "Game",
            platforms = "PC",
            endDate = "2026-12-31",
            users = 10000,
            status = "Active"
        )
        val subnauticaOst = com.example.util.OfficialOstCatalog.getOstInfo(subnautica)
        assertNotNull(subnauticaOst)
        assertEquals("Subnautica (Original Game Soundtrack)", subnauticaOst!!.albumTitle)
        assertEquals("Simon Chylinski", subnauticaOst.composer)
        assertTrue(subnauticaOst.spotifyUrl.contains("spotify.com"))
        assertTrue(subnauticaOst.youtubeMusicUrl.contains("music.youtube.com"))
        assertEquals("2018", subnauticaOst.releaseYear)

        // Cyberpunk 2077 curated OST
        val cp2077 = GiveawayItem(
            id = 102,
            title = "Cyberpunk 2077",
            worth = "$59.99",
            numericWorth = 59.99,
            thumbnail = "",
            image = "",
            description = "Night City RPG",
            instructions = "",
            openGiveawayUrl = "https://store.steampowered.com",
            publishedDate = "",
            type = "Game",
            platforms = "PC",
            endDate = "2026-12-31",
            users = 25000,
            status = "Active"
        )
        val cpOst = com.example.util.OfficialOstCatalog.getOstInfo(cp2077)
        assertNotNull(cpOst)
        assertEquals("Cyberpunk 2077 (Original Score)", cpOst!!.albumTitle)
        assertTrue(cpOst.spotifyUrl.startsWith("https://open.spotify.com/album/"))
        assertTrue(cpOst.youtubeMusicUrl.startsWith("https://music.youtube.com/playlist"))

        // Portal 2 curated OST
        val portal2Ost = com.example.util.OfficialOstCatalog.getOstInfoForTitle("Portal 2")
        assertNotNull(portal2Ost)
        assertEquals("Portal 2: Songs to Test By", portal2Ost!!.albumTitle)
        assertTrue(portal2Ost.composer.contains("Aperture Science"))
    }

    @Test
    fun `official ost catalog flags DLC items, cosmetics, currency and packs as no OST`() {
        val skinPack = GiveawayItem(
            id = 201,
            title = "Overwatch 2 Legendary Skin Pack",
            worth = "$19.99",
            numericWorth = 19.99,
            thumbnail = "",
            image = "",
            description = "Skins and coins bundle",
            instructions = "",
            openGiveawayUrl = "",
            publishedDate = "",
            type = "Game", // Listed as game but contains skin/pack
            platforms = "PC",
            endDate = "",
            users = 500,
            status = "Active"
        )
        val skinOst = com.example.util.OfficialOstCatalog.getOstInfo(skinPack)
        assertEquals(null, skinOst)

        // Type is DLC
        val dlcItem = GiveawayItem(
            id = 202,
            title = "Destiny 2: The Witch Queen Deluxe Expansion",
            worth = "$39.99",
            numericWorth = 39.99,
            thumbnail = "",
            image = "",
            description = "DLC expansion",
            instructions = "",
            openGiveawayUrl = "",
            publishedDate = "",
            type = "DLC",
            platforms = "PC",
            endDate = "",
            users = 5000,
            status = "Active"
        )
        val dlcOst = com.example.util.OfficialOstCatalog.getOstInfo(dlcItem)
        assertEquals(null, dlcOst)

        // In-game currency bundle
        val goldPack = GiveawayItem(
            id = 203,
            title = "World of Tanks 5000 Gold + Booster Pack",
            worth = "$14.99",
            numericWorth = 14.99,
            thumbnail = "",
            image = "",
            description = "Gold and currency",
            instructions = "",
            openGiveawayUrl = "",
            publishedDate = "",
            type = "Loot",
            platforms = "PC",
            endDate = "",
            users = 1000,
            status = "Active"
        )
        val goldOst = com.example.util.OfficialOstCatalog.getOstInfo(goldPack)
        assertEquals(null, goldOst)
    }

    @Test
    fun `official ost catalog provides search links for verified full games without curated match`() {
        val genericFullGame = GiveawayItem(
            id = 301,
            title = "Axiom Verge 2",
            worth = "$19.99",
            numericWorth = 19.99,
            thumbnail = "",
            image = "",
            description = "Metroidvania exploration",
            instructions = "",
            openGiveawayUrl = "",
            publishedDate = "",
            type = "Game",
            platforms = "PC",
            endDate = "2026-12-31",
            users = 4500,
            status = "Active"
        )
        val ost = com.example.util.OfficialOstCatalog.getOstInfo(genericFullGame)
        assertNotNull(ost)
        assertTrue(ost!!.spotifyUrl.startsWith("https://open.spotify.com/search/"))
        assertTrue(ost.youtubeMusicUrl.startsWith("https://music.youtube.com/search?q="))
        assertTrue(ost.albumTitle.contains("Axiom Verge 2"))
    }

    @Test
    fun `ost quick action intent helpers execute without crashing`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Verify launch methods execute safely on Robolectric
        com.example.util.OfficialOstCatalog.openYouTubeMusic(context, "https://music.youtube.com")
        com.example.util.OfficialOstCatalog.openSpotify(context, "https://open.spotify.com")
        com.example.util.OfficialOstCatalog.openDiscogs(context, "https://www.discogs.com")
    }

    @Test
    fun `discogs api client parses authentic video game soundtrack response and rejects covers`() {
        val validJson = """
            {
              "results": [
                {
                  "id": 12895642,
                  "type": "release",
                  "title": "Simon Chylinski - Subnautica (Original Game Soundtrack)",
                  "year": "2018",
                  "genre": ["Stage & Screen", "Electronic"],
                  "style": ["Video Game Music", "Soundtrack", "Ambient"],
                  "uri": "/release/12895642-Simon-Chylinski-Subnautica"
                }
              ]
            }
        """.trimIndent()

        val result = com.example.data.remote.DiscogsApiClient.parseAndVerifyDiscogsResponse(validJson, "subnautica")
        assertNotNull(result)
        assertEquals("Subnautica (Original Game Soundtrack)", result!!.albumTitle)
        assertEquals("Simon Chylinski", result.composer)
        assertEquals("2018", result.year)
        assertTrue(result.discogsUrl.contains("discogs.com/release/12895642"))
        assertTrue(result.isVerifiedVideoGameScore)

        // Test rejection of tribute / cover version
        val tributeJson = """
            {
              "results": [
                {
                  "id": 9999999,
                  "type": "release",
                  "title": "8-Bit Band - Tribute to Subnautica Metal Cover",
                  "year": "2021",
                  "genre": ["Rock"],
                  "style": ["Cover"],
                  "uri": "/release/9999999"
                }
              ]
            }
        """.trimIndent()
        val tributeResult = com.example.data.remote.DiscogsApiClient.parseAndVerifyDiscogsResponse(tributeJson, "subnautica")
        assertEquals(null, tributeResult)

        // Test rejection of unrelated album that contains a single common word
        val unrelatedJson = """
            {
              "results": [
                {
                  "id": 8888888,
                  "type": "release",
                  "title": "Janet Jackson - Control",
                  "year": "1986",
                  "genre": ["Funk / Soul"],
                  "style": ["RnB/Swing"],
                  "uri": "/release/8888888"
                }
              ]
            }
        """.trimIndent()
        val unrelatedResult = com.example.data.remote.DiscogsApiClient.parseAndVerifyDiscogsResponse(unrelatedJson, "control")
        assertEquals(null, unrelatedResult)
    }

    @Test
    fun `spotify api client parses authentic soundtrack album and rejects fan lofi remixes`() {
        val validJson = """
            {
              "albums": {
                "items": [
                  {
                    "name": "Hades (Original Soundtrack)",
                    "release_date": "2020-09-17",
                    "total_tracks": 30,
                    "external_urls": {
                      "spotify": "https://open.spotify.com/album/2oWf2p3U1N2gW8sM0M0v2Z"
                    },
                    "artists": [
                      { "name": "Darren Korb" },
                      { "name": "Ashley Barrett" }
                    ]
                  }
                ]
              }
            }
        """.trimIndent()

        val result = com.example.data.remote.SpotifyApiClient.parseAndVerifySpotifyResponse(validJson, "hades")
        assertNotNull(result)
        assertEquals("Hades (Original Soundtrack)", result!!.albumTitle)
        assertEquals("Darren Korb, Ashley Barrett", result.composer)
        assertEquals("https://open.spotify.com/album/2oWf2p3U1N2gW8sM0M0v2Z", result.spotifyAlbumUrl)
        assertEquals("2020", result.releaseYear)
        assertTrue(result.isOfficialScore)

        // Fan Lo-fi cover must be rejected
        val lofiJson = """
            {
              "albums": {
                "items": [
                  {
                    "name": "Hades Chill Lofi Beats Remix",
                    "release_date": "2022-01-01",
                    "total_tracks": 10,
                    "external_urls": {
                      "spotify": "https://open.spotify.com/album/12345"
                    },
                    "artists": [
                      { "name": "Lofi Game Guy" }
                    ]
                  }
                ]
              }
            }
        """.trimIndent()
        val lofiResult = com.example.data.remote.SpotifyApiClient.parseAndVerifySpotifyResponse(lofiJson, "hades")
        assertEquals(null, lofiResult)
    }

    @Test
    fun `official ost repository fetches curated soundtrack and persists to room database cache`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = com.example.data.repository.OfficialOstRepository.getInstance(context)

        val hollowKnight = GiveawayItem(
            id = 401,
            title = "Hollow Knight",
            worth = "$14.99",
            numericWorth = 14.99,
            thumbnail = "",
            image = "",
            description = "Hallownest insect adventure",
            instructions = "",
            openGiveawayUrl = "",
            publishedDate = "",
            type = "Game",
            platforms = "PC",
            endDate = "",
            users = 8000,
            status = "Active"
        )

        // Fetch from repo
        val ost = repo.fetchOstInfo(hollowKnight)
        assertNotNull(ost)
        assertEquals("Hollow Knight (Original Soundtrack)", ost!!.albumTitle)
        assertEquals("Christopher Larkin", ost.composer)
        assertTrue(ost.spotifyUrl.contains("spotify.com"))
        assertTrue(ost.isVerified)

        // Verify it was persisted to Room DB ost_cache
        val db = com.example.data.local.AppDatabase.getInstance(context)
        val cached = db.ostCacheDao().getByKey("hollow knight")
        assertNotNull(cached)
        assertTrue(cached!!.hasOst)
        assertEquals("Hollow Knight (Original Soundtrack)", cached.albumTitle)
        assertEquals("Christopher Larkin", cached.composer)

        // Non-game / DLC is ineligible and immediately returns null ("No OST")
        val weaponSkin = GiveawayItem(
            id = 402,
            title = "Apex Legends Bloodhound Cosmetic Weapon Skin",
            worth = "$9.99",
            numericWorth = 9.99,
            thumbnail = "",
            image = "",
            description = "Skin cosmetic",
            instructions = "",
            openGiveawayUrl = "",
            publishedDate = "",
            type = "DLC",
            platforms = "PC",
            endDate = "",
            users = 500,
            status = "Active"
        )
        val skinResult = repo.fetchOstInfo(weaponSkin)
        assertEquals(null, skinResult)
        org.junit.Assert.assertFalse(repo.isEligibleForOst(weaponSkin))
    }

    @Test
    fun `home tab is the default selected tab on ViewModel creation`() {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = com.example.ui.GiveawaysViewModel(context)
        assertEquals(com.example.ui.MainTab.HOME, viewModel.selectedTab.value)
    }

    @Test
    fun `home tab recent steam games sorts by last played and exposes playtime and achievements`() {
        val game1 = com.example.data.local.SteamGameEntity(
            appId = 101,
            name = "Portal 2",
            playtimeMinutes = 300,
            playtime2WeeksMinutes = 60,
            lastPlayedTimestamp = 1700000000L,
            achievementsUnlocked = 20,
            achievementsTotal = 50
        )
        val game2 = com.example.data.local.SteamGameEntity(
            appId = 102,
            name = "Cyberpunk 2077",
            playtimeMinutes = 600,
            playtime2WeeksMinutes = 120,
            lastPlayedTimestamp = 1700005000L,
            achievementsUnlocked = 44,
            achievementsTotal = 44
        )
        val game3 = com.example.data.local.SteamGameEntity(
            appId = 103,
            name = "Stardew Valley",
            playtimeMinutes = 120,
            playtime2WeeksMinutes = 0,
            lastPlayedTimestamp = 1690000000L,
            achievementsUnlocked = 0,
            achievementsTotal = 0
        )
        val game4 = com.example.data.local.SteamGameEntity(
            appId = 104,
            name = "Half-Life 2",
            playtimeMinutes = 50,
            playtime2WeeksMinutes = 0,
            lastPlayedTimestamp = 1680000000L,
            achievementsUnlocked = 10,
            achievementsTotal = 33
        )

        val list = listOf(game1, game2, game3, game4)
        val sortedRecent = list.sortedWith(
            compareByDescending<com.example.data.local.SteamGameEntity> { it.lastPlayedTimestamp }
                .thenByDescending { it.playtime2WeeksMinutes }
                .thenByDescending { it.playtimeMinutes }
        ).take(3)

        assertEquals(3, sortedRecent.size)
        assertEquals(102, sortedRecent[0].appId) // Cyberpunk 2077 (most recent)
        assertEquals(101, sortedRecent[1].appId) // Portal 2
        assertEquals(103, sortedRecent[2].appId) // Stardew Valley

        // Verify playtime and achievements getters
        assertEquals("10h", sortedRecent[0].playtimeHours)
        assertEquals(100, sortedRecent[0].achievementsPercentage)
        assertTrue(sortedRecent[0].achievementsTotal > 0)

        assertEquals("5.0h", sortedRecent[1].playtimeHours)
        assertEquals(40, sortedRecent[1].achievementsPercentage)

        assertEquals(0, sortedRecent[2].achievementsTotal)
        assertEquals(0, sortedRecent[2].achievementsPercentage)
    }

    @Test
    fun `top 3 free games today takes top rated and highest value active giveaways`() {
        val item1 = GiveawayItem(
            id = 1,
            title = "A Great Indie",
            worth = "$19.99",
            numericWorth = 19.99,
            thumbnail = "",
            image = "",
            description = "Indie gem",
            instructions = "",
            openGiveawayUrl = "",
            publishedDate = "",
            type = "Game",
            platforms = "PC",
            endDate = "2026-12-01",
            users = 1000,
            status = "Active",
            metacriticScore = 88
        )
        val item2 = GiveawayItem(
            id = 2,
            title = "AAA Masterpiece",
            worth = "$59.99",
            numericWorth = 59.99,
            thumbnail = "",
            image = "",
            description = "Big game",
            instructions = "",
            openGiveawayUrl = "",
            publishedDate = "",
            type = "Game",
            platforms = "PC",
            endDate = "2026-12-01",
            users = 5000,
            status = "Active",
            metacriticScore = 93
        )
        val item3 = GiveawayItem(
            id = 3,
            title = "Fun RPG",
            worth = "$29.99",
            numericWorth = 29.99,
            thumbnail = "",
            image = "",
            description = "RPG",
            instructions = "",
            openGiveawayUrl = "",
            publishedDate = "",
            type = "Game",
            platforms = "PC",
            endDate = "2026-12-01",
            users = 2500,
            status = "Active",
            metacriticScore = 80
        )
        val item4 = GiveawayItem(
            id = 4,
            title = "Older Game",
            worth = "$9.99",
            numericWorth = 9.99,
            thumbnail = "",
            image = "",
            description = "Old",
            instructions = "",
            openGiveawayUrl = "",
            publishedDate = "",
            type = "Game",
            platforms = "PC",
            endDate = "2026-12-01",
            users = 500,
            status = "Active",
            metacriticScore = 70
        )

        val list = listOf(item1, item2, item3, item4)
        val top3 = list.filter { it.status.equals("Active", ignoreCase = true) }
            .sortedWith(
                compareByDescending<GiveawayItem> { it.metacriticScore }
                    .thenByDescending { it.numericWorth }
                    .thenByDescending { it.users }
            ).take(3)

        assertEquals(3, top3.size)
        assertEquals(2, top3[0].id) // AAA Masterpiece (Metascore 93)
        assertEquals(1, top3[1].id) // A Great Indie (Metascore 88)
        assertEquals(3, top3[2].id) // Fun RPG (Metascore 80)
    }

    @Test
    fun `default tab preference defaults to HOME and persists selection`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = com.example.preferences.AppPreferences(context)
        assertEquals(com.example.ui.MainTab.HOME, prefs.defaultTab)

        prefs.defaultTab = com.example.ui.MainTab.DISCOVER
        assertEquals(com.example.ui.MainTab.DISCOVER, prefs.defaultTab)

        prefs.defaultTab = com.example.ui.MainTab.CLAIMED
        assertEquals(com.example.ui.MainTab.CLAIMED, prefs.defaultTab)

        prefs.defaultTab = com.example.ui.MainTab.CALENDAR
        assertEquals(com.example.ui.MainTab.CALENDAR, prefs.defaultTab)

        // Reset back to HOME
        prefs.defaultTab = com.example.ui.MainTab.HOME
        assertEquals(com.example.ui.MainTab.HOME, prefs.defaultTab)
    }

    @Test
    fun `viewModel setDefaultTab updates preference and state flow`() {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        val prefs = com.example.preferences.AppPreferences(context)
        prefs.defaultTab = com.example.ui.MainTab.CALENDAR

        val viewModel = com.example.ui.GiveawaysViewModel(context)
        assertEquals(com.example.ui.MainTab.CALENDAR, viewModel.defaultTab.value)
        assertEquals(com.example.ui.MainTab.CALENDAR, viewModel.selectedTab.value)

        viewModel.setDefaultTab(com.example.ui.MainTab.HOME)
        assertEquals(com.example.ui.MainTab.HOME, viewModel.defaultTab.value)
        assertEquals(com.example.ui.MainTab.HOME, prefs.defaultTab)
    }
}



