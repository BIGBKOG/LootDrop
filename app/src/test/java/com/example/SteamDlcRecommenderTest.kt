package com.example

import com.example.data.local.SteamGameEntity
import com.example.data.model.GiveawayItem
import com.example.data.model.PlayStatus
import com.example.data.remote.SteamApiClient
import com.example.util.SteamDlcRecommender
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SteamDlcRecommenderTest {

    private fun sampleSteamGame(
        appId: Int,
        name: String,
        playtimeMinutes: Int = 600,
        playtime2Weeks: Int = 0,
        status: PlayStatus = PlayStatus.PLAYING,
        lastPlayed: Long = 1_700_000_000L
    ) = SteamGameEntity(
        appId = appId,
        name = name,
        playtimeMinutes = playtimeMinutes,
        playtime2WeeksMinutes = playtime2Weeks,
        lastPlayedTimestamp = lastPlayed,
        playStatus = status
    )

    private fun sampleGiveaway(
        id: Int,
        title: String,
        type: String = "DLC",
        status: String = "Active",
        url: String = "https://store.steampowered.com/app/999001"
    ) = GiveawayItem(
        id = id,
        title = title,
        worth = "$14.99",
        numericWorth = 14.99,
        thumbnail = "",
        image = "",
        description = "Test expansion",
        instructions = "Claim on Steam",
        openGiveawayUrl = url,
        publishedDate = "2026-01-01",
        type = type,
        platforms = "PC, Steam",
        endDate = "N/A",
        users = 500,
        status = status
    )

    @Test
    fun findFreeDlcGiveaways_matchesOwnedSteamGameAndSkipsUnowned() {
        val owned = listOf(
            sampleSteamGame(appId = 1245620, name = "ELDEN RING", playtimeMinutes = 3200),
            sampleSteamGame(appId = 413150, name = "Stardew Valley", playtimeMinutes = 900)
        )
        val giveaways = listOf(
            sampleGiveaway(
                id = 1,
                title = "ELDEN RING: Shadow Pack DLC",
                type = "DLC",
                status = "Active",
                url = "https://store.steampowered.com/app/2778580"
            ),
            sampleGiveaway(
                id = 2,
                title = "Cyberpunk 2077: Phantom Liberty DLC",
                type = "DLC",
                status = "Active",
                url = "https://store.steampowered.com/app/2138330"
            ),
            sampleGiveaway(
                id = 3,
                title = "Stardew Valley Soundtrack Pack",
                type = "DLC",
                status = "Expired",
                url = "https://store.steampowered.com/app/440820"
            )
        )

        val results = SteamDlcRecommender.findFreeDlcGiveawaysForSteamLibrary(giveaways, owned)

        assertEquals(1, results.size)
        assertEquals(2778580, results.first().dlcAppId)
        assertEquals(1245620, results.first().parentAppId)
        assertTrue(results.first().isFree)
        assertTrue(results.first().isFromFreeGiveaway)
    }

    @Test
    fun selectCandidateGames_prioritizesRecentAndPlayingOverDropped() {
        val dropped = sampleSteamGame(10, "Dropped Game", playtimeMinutes = 5000, status = PlayStatus.DROPPED)
        val notStarted = sampleSteamGame(20, "Backlog Game", playtimeMinutes = 0, status = PlayStatus.NOT_STARTED)
        val recent = sampleSteamGame(30, "Active Favorite", playtimeMinutes = 1200, playtime2Weeks = 180, status = PlayStatus.PLAYING)

        val ranked = SteamDlcRecommender.selectCandidateGamesForDlcScan(
            listOf(dropped, notStarted, recent),
            maxGames = 3
        )

        assertEquals(30, ranked[0].appId)
        assertEquals(20, ranked[1].appId)
        assertEquals(10, ranked[2].appId)
    }

    @Test
    fun buildRecommendation_filtersComingSoonAndAlreadyOwnedAndRanksExpansionAboveOst() {
        val parent = sampleSteamGame(appId = 1091500, name = "Cyberpunk 2077", playtimeMinutes = 2400, status = PlayStatus.PLAYING)

        val comingSoon = SteamApiClient.SteamStoreDlcInfo(
            dlcAppId = 1,
            name = "Upcoming Expansion",
            comingSoon = true
        )
        assertNull(SteamDlcRecommender.buildRecommendation(comingSoon, parent))

        val ownedDlc = SteamApiClient.SteamStoreDlcInfo(
            dlcAppId = 2,
            name = "Owned Expansion"
        )
        assertNull(SteamDlcRecommender.buildRecommendation(ownedDlc, parent, isAlreadyOwned = true))

        val expansion = SteamApiClient.SteamStoreDlcInfo(
            dlcAppId = 2138330,
            name = "Phantom Liberty",
            discountPercent = 30,
            priceFormatted = "$20.99",
            initialPriceFormatted = "$29.99",
            recommendationsCount = 18000,
            metacriticScore = 89
        )
        val soundtrack = SteamApiClient.SteamStoreDlcInfo(
            dlcAppId = 2138331,
            name = "Cyberpunk 2077 Original Soundtrack",
            isFree = true,
            priceFormatted = "Free"
        )

        val recExpansion = SteamDlcRecommender.buildRecommendation(expansion, parent)!!
        val recSoundtrack = SteamDlcRecommender.buildRecommendation(soundtrack, parent)!!

        assertFalse(recExpansion.isSoundtrackOrCosmetic)
        assertTrue(recExpansion.isOnSale)
        assertTrue(recSoundtrack.isSoundtrackOrCosmetic)
        assertTrue(recExpansion.relevanceScore > recSoundtrack.relevanceScore)
    }

    @Test
    fun cacheSerializationRoundtrip_preservesAllFields() {
        val parent = sampleSteamGame(appId = 292030, name = "The Witcher 3: Wild Hunt", playtimeMinutes = 4800)
        val info = SteamApiClient.SteamStoreDlcInfo(
            dlcAppId = 378648,
            name = "Blood and Wine",
            shortDescription = "Major story expansion set in Toussaint.",
            discountPercent = 70,
            priceFormatted = "$5.99",
            initialPriceFormatted = "$19.99",
            recommendationsCount = 32000,
            releaseDate = "May 30, 2016",
            metacriticScore = 92
        )
        val original = SteamDlcRecommender.buildRecommendation(info, parent)!!

        val json = SteamDlcRecommender.serializeRecommendations(listOf(original))
        val restored = SteamDlcRecommender.deserializeRecommendations(json)

        assertEquals(1, restored.size)
        val item = restored.first()
        assertEquals(original.dlcAppId, item.dlcAppId)
        assertEquals(original.name, item.name)
        assertEquals(original.parentAppId, item.parentAppId)
        assertEquals(original.discountPercent, item.discountPercent)
        assertEquals(original.priceFormatted, item.priceFormatted)
        assertEquals(original.initialPriceFormatted, item.initialPriceFormatted)
        assertEquals(original.metacriticScore, item.metacriticScore)
        assertEquals(original.relevanceScore, item.relevanceScore)
    }
}
