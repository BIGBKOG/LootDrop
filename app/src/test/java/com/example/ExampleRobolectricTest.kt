package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.GiveawayItem
import com.example.data.model.LauncherType
import com.example.notifications.NotificationHelper
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
}
