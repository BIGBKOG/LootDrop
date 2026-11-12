package com.example

import com.example.data.model.PublicReview
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
        val seeds = com.example.util.PublicReviewsManager.getInitialSeedReviews()
        assertTrue(seeds.isNotEmpty())
        assertTrue(seeds.any { it.gameTitle.contains("Death Stranding", ignoreCase = true) })
        assertTrue(seeds.all { it.rating in 1..5 })
    }
}
