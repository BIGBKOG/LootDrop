package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.PublicReview
import com.example.util.PublicReviewsManager
import com.example.util.ReviewModerationFilter
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PublicReviewsTest {

    @Test
    fun testPublicReviewJsonSerialization() {
        val review = PublicReview(
            id = "rev_test_123",
            giveawayId = 42L,
            gameTitle = "Death Stranding",
            authorName = "HideoFan",
            authorTag = "Verified Claimer",
            rating = 5,
            title = "Incredible Experience",
            comment = "Outstanding visuals and storytelling.",
            platform = "Epic Games",
            timestamp = 1731420000000L,
            helpfulCount = 14,
            isVerifiedClaimer = true
        )

        val json = review.toJson()
        assertEquals("rev_test_123", json.getString("id"))
        assertEquals(42L, json.getLong("giveawayId"))
        assertEquals("Death Stranding", json.getString("gameTitle"))
        assertEquals(5, json.getInt("rating"))
        assertEquals(true, json.getBoolean("isVerifiedClaimer"))
        assertEquals(14, json.getInt("helpfulCount"))

        val parsed = PublicReview.fromJson(json)
        assertEquals(review.id, parsed.id)
        assertEquals(review.giveawayId, parsed.giveawayId)
        assertEquals(review.gameTitle, parsed.gameTitle)
        assertEquals(review.authorName, parsed.authorName)
        assertEquals(review.rating, parsed.rating)
        assertEquals(review.title, parsed.title)
        assertEquals(review.comment, parsed.comment)
        assertEquals(review.isVerifiedClaimer, parsed.isVerifiedClaimer)
        assertEquals(review.helpfulCount, parsed.helpfulCount)
    }

    @Test
    fun testInitialSeedReviewsExist() {
        val seeds = PublicReviewsManager.getInitialSeedReviews()
        assertTrue(seeds.isNotEmpty())
        assertTrue(seeds.any { it.gameTitle.contains("Death Stranding", ignoreCase = true) })
        assertTrue(seeds.all { it.rating in 1..5 })
        seeds.forEach { seed ->
            assertTrue(
                ReviewModerationFilter.validateReviewInput(
                    title = seed.title,
                    comment = seed.comment,
                    authorName = seed.authorName
                ).isAllowed
            )
        }
    }

    @Test
    fun testReviewBlacklistBlocksSwearsAndSlursIncludingObfuscation() {
        // Explicit swears
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("This port runs like shit"))
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("What a fucking mess"))
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("Total asshole move by devs"))

        // Explicit slurs
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("Stop being a retard"))
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("Blocked faggot behavior"))
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("Shut up nigger"))

        // Leetspeak, symbol masking, repeated letters, and spaced-letter obfuscation
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("Total $#!7 game"))
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("What a b!tch"))
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("This is f u c k i n g broken"))
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("f.u.c.k this launcher"))
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("sh1t optimizer and n1gg3r"))
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("fuuuuuck this bug"))

        // Requested restricted phrases, sexual slang, and troll/brainrot words
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("cash is clay bro"))
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("c@sh-is-clay"))
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("cashisclay"))
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("nuh dude"))
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("busted a nut"))
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("deez nuts"))
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("cum here"))
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("c.u.m"))
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("what up cuh"))
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("gyatt jizz skibidi"))
    }

    @Test
    fun testReviewBlacklistAllowsCleanGamingTermsWithoutFalsePositives() {
        val cleanReviews = listOf(
            "Assassin's Creed is a classic Stealth RPG",
            "Helldivers 2 and Hellblade II run great on Steam Deck",
            "RollerCoaster Tycoon Classic + Peacock support is awesome",
            "Great cocktail of combat, scrap crafting, and compassionate NPCs",
            "Runs smooth at 120 FPS for CyberHunter88!",
            "S.T.A.L.K.E.R. and F.E.A.R. are must-claim freebies",
            "Peanut butter vacuum document circumstances and walnut chestnut minute"
        )
        cleanReviews.forEach { text ->
            assertFalse("Unexpectedly flagged clean text: $text", ReviewModerationFilter.containsBlacklistedWords(text))
            assertEquals(text, ReviewModerationFilter.sanitizeForDisplay(text))
        }
    }

    @Test
    fun testValidateReviewInputChecksAllFieldsAndPostReviewRejects() = runBlocking {
        val badHeadline = ReviewModerationFilter.validateReviewInput(
            title = "Complete bullshit",
            comment = "Crashes on launch",
            authorName = "PixelHunter"
        )
        assertFalse(badHeadline.isAllowed)
        assertTrue(badHeadline.flaggedFields.contains("headline"))

        val badAuthor = ReviewModerationFilter.validateReviewInput(
            title = "Fun game",
            comment = "Really enjoyed it",
            authorName = "b1tchSlayer"
        )
        assertFalse(badAuthor.isAllowed)
        assertTrue(badAuthor.flaggedFields.contains("gamer tag"))

        val context = ApplicationProvider.getApplicationContext<Context>()

        // Game review blocked
        val rejectedGameResult = PublicReviewsManager.postReview(
            context = context,
            giveawayId = 999L,
            gameTitle = "Test Game",
            authorName = "Tester",
            authorTag = "Gamer",
            rating = 1,
            title = "cash is clay",
            comment = "this nuh nut game",
            platform = "Android",
            isVerifiedClaimer = false,
            isAppReview = false
        )
        assertTrue(rejectedGameResult.isFailure)
        assertTrue(rejectedGameResult.exceptionOrNull()?.message?.contains("Blacklisted", ignoreCase = true) == true)

        // App review blocked
        val rejectedAppResult = PublicReviewsManager.postReview(
            context = context,
            giveawayId = -1L,
            gameTitle = "LootDrop App",
            authorName = "Tester",
            authorTag = "LootDrop Player",
            rating = 1,
            title = "App check",
            comment = "Stop saying cum cuh",
            platform = "Android",
            isVerifiedClaimer = true,
            isAppReview = true
        )
        assertTrue(rejectedAppResult.isFailure)
        assertTrue(rejectedAppResult.exceptionOrNull()?.message?.contains("Blacklisted", ignoreCase = true) == true)

        // Cached / legacy remote reviews are sanitized on load across all fields
        val prefs = context.getSharedPreferences("lootdrop_public_reviews_cache", Context.MODE_PRIVATE)
        prefs.edit().putString(
            "cached_public_reviews_json",
            """[{"id":"old_1","giveawayId":42,"gameTitle":"Game","authorName":"cuh","title":"cash is clay","comment":"nuh nut cum","rating":5}]"""
        ).commit()
        val cachedParsed = PublicReviewsManager.getCachedReviews(context)
        assertEquals(1, cachedParsed.size)
        assertFalse(ReviewModerationFilter.containsBlacklistedWords(cachedParsed[0].authorName))
        assertFalse(ReviewModerationFilter.containsBlacklistedWords(cachedParsed[0].title))
        assertFalse(ReviewModerationFilter.containsBlacklistedWords(cachedParsed[0].comment))

        // Display masking censors profanity cleanly
        val masked = ReviewModerationFilter.sanitizeForDisplay("Game had some shit glitches")
        assertFalse(ReviewModerationFilter.containsBlacklistedWords(masked))
        assertTrue(masked.contains("s***"))
    }

    @Test
    fun testReviewBlacklistBlocksAddressesAndPhoneNumbersWithoutFalsePositives() = runBlocking {
        // Street addresses, PO Boxes, City/State/ZIP, emails
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("Meet me at 123 Main St"))
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("Lives at 742 Evergreen Terrace"))
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("Address: 1600 Pennsylvania Ave NW, Washington, DC 20500"))
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("Send mail to P.O. Box 4512"))
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("Found in Springfield, IL 62704"))
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("Email me at player@example.com"))

        // Phone numbers (US, local, and international)
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("Call (555) 123-4567 for support"))
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("My number is 555-867-5309"))
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("Text +1 800 555 0199"))
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("Reach out 5551234567"))
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("UK line +44 20 7946 0958"))
        assertTrue(ReviewModerationFilter.containsBlacklistedWords("Dial 555-0199"))

        // Clean gaming numbers, storage specs, titles, and dates must remain allowed
        val cleanGamingTexts = listOf(
            "Runs at 120 FPS in 1920x1080 or 2560x1440 with 16GB RAM and 100 GB hard drive",
            "1 TB SSD drive recommended for Test Drive Unlimited and Street Fighter 6",
            "Claimed on 2026-11-16 for $19.99 saved (App ID 1091500)",
            "Played 200 hours on the road and won 1st place in 3rd Street Saints",
            "220-1440p dynamic resolution looks crisp"
        )
        cleanGamingTexts.forEach { text ->
            assertFalse("False positive on clean text: $text", ReviewModerationFilter.containsBlacklistedWords(text))
            assertEquals(text, ReviewModerationFilter.sanitizeForDisplay(text))
        }

        // Redaction masks addresses and phone numbers in stored/displayed text
        val redacted = ReviewModerationFilter.sanitizeForDisplay("Call 555-123-4567 or visit 123 Main St")
        assertFalse(ReviewModerationFilter.containsBlacklistedWords(redacted))
        assertTrue(redacted.contains("[redacted]"))

        // postReview rejects submissions containing an address or phone number
        val context = ApplicationProvider.getApplicationContext<Context>()
        val rejectedPhone = PublicReviewsManager.postReview(
            context = context,
            giveawayId = 55L,
            gameTitle = "Co-op Game",
            authorName = "FriendlyPlayer",
            authorTag = "Gamer",
            rating = 5,
            title = "Great game",
            comment = "Add me or call 555-234-5678",
            platform = "Android",
            isVerifiedClaimer = true
        )
        assertTrue(rejectedPhone.isFailure)
        assertTrue(rejectedPhone.exceptionOrNull()?.message?.contains("phone", ignoreCase = true) == true)
    }
}
