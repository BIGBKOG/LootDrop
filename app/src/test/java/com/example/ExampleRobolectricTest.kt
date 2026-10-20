package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.GiveawayItem
import com.example.data.model.LauncherType
import com.example.notifications.NotificationHelper
import com.example.preferences.AppPreferences
import org.junit.Assert.assertEquals
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

        // Cross-platform item supporting PC and consoles
        val crossPlatItem = steamItem.copy(platforms = "PC, Steam, Playstation 5, Xbox Series X|S")
        assertTrue(crossPlatItem.isPlayStation)
        assertTrue(crossPlatItem.isXbox)
        assertTrue(crossPlatItem.matchesLauncher(LauncherType.PLAYSTATION))
        assertTrue(crossPlatItem.matchesLauncher(LauncherType.XBOX))
        assertTrue(crossPlatItem.matchesLauncher(LauncherType.STEAM))
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
    fun `navigation tabs only contain discover claimed and settings without saved tab`() {
        val tabNames = com.example.ui.MainTab.values().map { it.name }
        assertEquals(listOf("DISCOVER", "CLAIMED", "SETTINGS"), tabNames)
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
}
