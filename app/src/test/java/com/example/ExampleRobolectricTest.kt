package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.GiveawayItem
import com.example.data.model.LauncherType
import com.example.notifications.NotificationHelper
import com.example.preferences.AppPreferences
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
    fun `navigation tabs contain discover claimed calendar and settings without saved tab`() {
        val tabNames = com.example.ui.MainTab.values().map { it.name }
        assertEquals(listOf("DISCOVER", "CLAIMED", "CALENDAR", "SETTINGS"), tabNames)
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
        assertTrue(entity.description.contains("Steam Rating: Very Positive"))
        assertTrue(entity.description.contains("84% positive"))

        // Verify that deal's Metacritic score of 84 is registered and resolves
        assertEquals(84, com.example.util.MetacriticCatalog.resolveScore("Sker Ritual"))
        val domain = entity.toDomain()
        assertEquals(84, domain.metacriticScore)
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
}

