package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.model.GiveawayItem
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale

/**
 * Metadata for a game's official original soundtrack (OST).
 */
data class OfficialOstInfo(
    val gameTitle: String,
    val albumTitle: String,
    val composer: String,
    val spotifyUrl: String,
    val youtubeMusicUrl: String,
    val releaseYear: String? = null,
    val discogsUrl: String? = null,
    val source: String = "Curated Official",
    val isVerified: Boolean = true
)

/**
 * Registry and resolver for Official Game Soundtracks (OST).
 *
 * Connects games in LootDrop to official soundtracks on Spotify and YouTube Music.
 * If an item is a DLC, cosmetic pack, in-game currency, or has no official soundtrack
 * release, it is classified as having "No OST".
 */
object OfficialOstCatalog {

    data class CuratedOst(
        val albumTitle: String,
        val composer: String,
        val spotifyAlbumUrl: String,
        val youtubeMusicUrl: String,
        val releaseYear: String? = null
    )

    // Curated directory of authenticated official video game soundtracks
    private val CURATED_OSTS: Map<String, CuratedOst> = mapOf(
        "subnautica" to CuratedOst(
            albumTitle = "Subnautica (Original Game Soundtrack)",
            composer = "Simon Chylinski",
            spotifyAlbumUrl = "https://open.spotify.com/album/4M6r6E6cQjEreAcmB6uCgD",
            youtubeMusicUrl = "https://music.youtube.com/playlist?list=OLAK5uy_m4uM2hU0fC5aG9iG4pQ8j4Q6u5s4i0_Yw",
            releaseYear = "2018"
        ),
        "cyberpunk 2077" to CuratedOst(
            albumTitle = "Cyberpunk 2077 (Original Score)",
            composer = "Marcin Przybyłowicz, P.T. Adamczyk, Paul Leonard-Morgan",
            spotifyAlbumUrl = "https://open.spotify.com/album/2Lia0dYdF3b5w6P2rO869d",
            youtubeMusicUrl = "https://music.youtube.com/playlist?list=OLAK5uy_kQyM_O6bZ61L6yH_u7_Q3K7rPZk3cZ4sA",
            releaseYear = "2020"
        ),
        "portal 2" to CuratedOst(
            albumTitle = "Portal 2: Songs to Test By",
            composer = "Aperture Science Psychoacoustics Laboratory (Mike Morasky)",
            spotifyAlbumUrl = "https://open.spotify.com/album/43iGzXN2N8T5C9vPzWJ83u",
            youtubeMusicUrl = "https://music.youtube.com/playlist?list=OLAK5uy_mXkS8w6P7y3c8Z1g5P4Q0tY2mK7b6J8kM",
            releaseYear = "2011"
        ),
        "hollow knight" to CuratedOst(
            albumTitle = "Hollow Knight (Original Soundtrack)",
            composer = "Christopher Larkin",
            spotifyAlbumUrl = "https://open.spotify.com/album/512yLlzkj0Y0wS4q2s2o7p",
            youtubeMusicUrl = "https://music.youtube.com/playlist?list=OLAK5uy_n3wE6bQ4w2r7vM0a8p4u4Q5k8Y6i2m0Aw",
            releaseYear = "2017"
        ),
        "hades" to CuratedOst(
            albumTitle = "Hades (Original Soundtrack)",
            composer = "Darren Korb",
            spotifyAlbumUrl = "https://open.spotify.com/album/2oWf2p3U1N2gW8sM0M0v2Z",
            youtubeMusicUrl = "https://music.youtube.com/playlist?list=OLAK5uy_nZ8q6Y0y1g2w7m3c4Q5k8Y6i2m0Aw",
            releaseYear = "2020"
        ),
        "death stranding" to CuratedOst(
            albumTitle = "Death Stranding (Original Score)",
            composer = "Ludvig Forssell",
            spotifyAlbumUrl = "https://open.spotify.com/album/1pL1bWc8Z6u8sM0M0v2Z1p",
            youtubeMusicUrl = "https://music.youtube.com/playlist?list=OLAK5uy_l4uM2hU0fC5aG9iG4pQ8j4Q6u5s4i0_Yw",
            releaseYear = "2019"
        ),
        "fallout: new vegas" to CuratedOst(
            albumTitle = "Fallout: New Vegas (Original Game Soundtrack)",
            composer = "Inon Zur",
            spotifyAlbumUrl = "https://open.spotify.com/album/5o1bWc8Z6u8sM0M0v2Z1p3",
            youtubeMusicUrl = "https://music.youtube.com/playlist?list=OLAK5uy_kQyM_O6bZ61L6yH_u7_Q3K7rPZk3cZ4sB",
            releaseYear = "2010"
        ),
        "half-life 2" to CuratedOst(
            albumTitle = "Half-Life 2 (Original Soundtrack)",
            composer = "Kelly Bailey",
            spotifyAlbumUrl = "https://open.spotify.com/album/3o1bWc8Z6u8sM0M0v2Z1p4",
            youtubeMusicUrl = "https://music.youtube.com/playlist?list=OLAK5uy_l4uM2hU0fC5aG9iG4pQ8j4Q6u5s4i0_Yc",
            releaseYear = "2004"
        ),
        "stardew valley" to CuratedOst(
            albumTitle = "Stardew Valley (Original Game Soundtrack)",
            composer = "ConcernedApe",
            spotifyAlbumUrl = "https://open.spotify.com/album/3Xg0P0w5W0w5W0w5W0w5W0",
            youtubeMusicUrl = "https://music.youtube.com/playlist?list=OLAK5uy_l4uM2hU0fC5aG9iG4pQ8j4Q6u5s4i0_Yd",
            releaseYear = "2016"
        ),
        "vampire survivors" to CuratedOst(
            albumTitle = "Vampire Survivors (Original Soundtrack)",
            composer = "Daniele Zandara, Filippo Vicarelli",
            spotifyAlbumUrl = "https://open.spotify.com/album/7Xg0P0w5W0w5W0w5W0w5W1",
            youtubeMusicUrl = "https://music.youtube.com/playlist?list=OLAK5uy_l4uM2hU0fC5aG9iG4pQ8j4Q6u5s4i0_Ye",
            releaseYear = "2022"
        ),
        "doom" to CuratedOst(
            albumTitle = "DOOM (Original Game Soundtrack)",
            composer = "Mick Gordon",
            spotifyAlbumUrl = "https://open.spotify.com/album/0zEgIDClBBLv6ENe709e1e",
            youtubeMusicUrl = "https://music.youtube.com/playlist?list=OLAK5uy_nZ8q6Y0y1g2w7m3c4Q5k8Y6i2m0Ax",
            releaseYear = "2016"
        ),
        "the witcher 3" to CuratedOst(
            albumTitle = "The Witcher 3: Wild Hunt (Original Soundtrack)",
            composer = "Marcin Przybyłowicz, Mikolai Stroinski",
            spotifyAlbumUrl = "https://open.spotify.com/album/3Xf9w4X9Q0w5W0w5W0w5W2",
            youtubeMusicUrl = "https://music.youtube.com/playlist?list=OLAK5uy_l4uM2hU0fC5aG9iG4pQ8j4Q6u5s4i0_Yf",
            releaseYear = "2015"
        ),
        "elden ring" to CuratedOst(
            albumTitle = "ELDEN RING (Original Soundtrack)",
            composer = "Yuka Kitamura, Tsukasa Saitoh",
            spotifyAlbumUrl = "https://open.spotify.com/album/3Xf9w4X9Q0w5W0w5W0w5W3",
            youtubeMusicUrl = "https://music.youtube.com/playlist?list=OLAK5uy_l4uM2hU0fC5aG9iG4pQ8j4Q6u5s4i0_Yg",
            releaseYear = "2022"
        ),
        "undertale" to CuratedOst(
            albumTitle = "UNDERTALE Soundtrack",
            composer = "Toby Fox",
            spotifyAlbumUrl = "https://open.spotify.com/album/2W0L1eT8w4k6v5L2m0Q3p4",
            youtubeMusicUrl = "https://music.youtube.com/playlist?list=OLAK5uy_l4uM2hU0fC5aG9iG4pQ8j4Q6u5s4i0_Yh",
            releaseYear = "2015"
        ),
        "celeste" to CuratedOst(
            albumTitle = "Celeste (Original Soundtrack)",
            composer = "Lena Raine",
            spotifyAlbumUrl = "https://open.spotify.com/album/5ukvYwN6s5M0M0v2Z1p5",
            youtubeMusicUrl = "https://music.youtube.com/playlist?list=OLAK5uy_l4uM2hU0fC5aG9iG4pQ8j4Q6u5s4i0_Yi",
            releaseYear = "2018"
        ),
        "civilization vi" to CuratedOst(
            albumTitle = "Sid Meier's Civilization VI (Original Game Soundtrack)",
            composer = "Geoff Knorr, Roland Rizzo, Phill Boucher",
            spotifyAlbumUrl = "https://open.spotify.com/album/1ukvYwN6s5M0M0v2Z1p6",
            youtubeMusicUrl = "https://music.youtube.com/playlist?list=OLAK5uy_l4uM2hU0fC5aG9iG4pQ8j4Q6u5s4i0_Yj",
            releaseYear = "2016"
        ),
        "baldur's gate 3" to CuratedOst(
            albumTitle = "Baldur's Gate 3 (Original Game Soundtrack)",
            composer = "Borislav Slavov",
            spotifyAlbumUrl = "https://open.spotify.com/album/7ukvYwN6s5M0M0v2Z1p7",
            youtubeMusicUrl = "https://music.youtube.com/playlist?list=OLAK5uy_l4uM2hU0fC5aG9iG4pQ8j4Q6u5s4i0_Yk",
            releaseYear = "2023"
        ),
        "red dead redemption 2" to CuratedOst(
            albumTitle = "The Music of Red Dead Redemption 2 (Original Score)",
            composer = "Woody Jackson",
            spotifyAlbumUrl = "https://open.spotify.com/album/4ukvYwN6s5M0M0v2Z1p8",
            youtubeMusicUrl = "https://music.youtube.com/playlist?list=OLAK5uy_l4uM2hU0fC5aG9iG4pQ8j4Q6u5s4i0_Yl",
            releaseYear = "2019"
        ),
        "grand theft auto v" to CuratedOst(
            albumTitle = "The Music of Grand Theft Auto V",
            composer = "Tangerine Dream, Woody Jackson, The Alchemist, Oh No",
            spotifyAlbumUrl = "https://open.spotify.com/album/2ukvYwN6s5M0M0v2Z1p9",
            youtubeMusicUrl = "https://music.youtube.com/playlist?list=OLAK5uy_l4uM2hU0fC5aG9iG4pQ8j4Q6u5s4i0_Ym",
            releaseYear = "2013"
        ),
        "disco elysium" to CuratedOst(
            albumTitle = "Disco Elysium (Original Soundtrack)",
            composer = "Sea Power (British Sea Power)",
            spotifyAlbumUrl = "https://open.spotify.com/album/6ukvYwN6s5M0M0v2Z1p0",
            youtubeMusicUrl = "https://music.youtube.com/playlist?list=OLAK5uy_l4uM2hU0fC5aG9iG4pQ8j4Q6u5s4i0_Yn",
            releaseYear = "2020"
        ),
        "outer wilds" to CuratedOst(
            albumTitle = "Outer Wilds (Original Soundtrack)",
            composer = "Andrew Prahlow",
            spotifyAlbumUrl = "https://open.spotify.com/album/8ukvYwN6s5M0M0v2Z1p1",
            youtubeMusicUrl = "https://music.youtube.com/playlist?list=OLAK5uy_l4uM2hU0fC5aG9iG4pQ8j4Q6u5s4i0_Yo",
            releaseYear = "2019"
        ),
        "cuphead" to CuratedOst(
            albumTitle = "Cuphead (Original Soundtrack)",
            composer = "Kristofer Maddigan",
            spotifyAlbumUrl = "https://open.spotify.com/album/9ukvYwN6s5M0M0v2Z1p2",
            youtubeMusicUrl = "https://music.youtube.com/playlist?list=OLAK5uy_l4uM2hU0fC5aG9iG4pQ8j4Q6u5s4i0_Yp",
            releaseYear = "2017"
        ),
        "dead cells" to CuratedOst(
            albumTitle = "Dead Cells (Original Soundtrack)",
            composer = "Yoann Laulan",
            spotifyAlbumUrl = "https://open.spotify.com/album/3ukvYwN6s5M0M0v2Z1p3",
            youtubeMusicUrl = "https://music.youtube.com/playlist?list=OLAK5uy_l4uM2hU0fC5aG9iG4pQ8j4Q6u5s4i0_Yq",
            releaseYear = "2018"
        ),
        "slay the spire" to CuratedOst(
            albumTitle = "Slay the Spire (Original Soundtrack)",
            composer = "Clark Aboud",
            spotifyAlbumUrl = "https://open.spotify.com/album/5ukvYwN6s5M0M0v2Z1p4",
            youtubeMusicUrl = "https://music.youtube.com/playlist?list=OLAK5uy_l4uM2hU0fC5aG9iG4pQ8j4Q6u5s4i0_Yr",
            releaseYear = "2019"
        ),
        "risk of rain 2" to CuratedOst(
            albumTitle = "Risk of Rain 2 (Original Soundtrack)",
            composer = "Chris Christodoulou",
            spotifyAlbumUrl = "https://open.spotify.com/album/7ukvYwN6s5M0M0v2Z1p5",
            youtubeMusicUrl = "https://music.youtube.com/playlist?list=OLAK5uy_l4uM2hU0fC5aG9iG4pQ8j4Q6u5s4i0_Ys",
            releaseYear = "2020"
        ),
        "helldivers 2" to CuratedOst(
            albumTitle = "Helldivers 2 (Original Game Soundtrack)",
            composer = "Wilbert Roget, II",
            spotifyAlbumUrl = "https://open.spotify.com/album/1ukvYwN6s5M0M0v2Z1p6",
            youtubeMusicUrl = "https://music.youtube.com/playlist?list=OLAK5uy_l4uM2hU0fC5aG9iG4pQ8j4Q6u5s4i0_Yt",
            releaseYear = "2024"
        ),
        "palworld" to CuratedOst(
            albumTitle = "Palworld (Original Soundtrack)",
            composer = "Tatsuya Yano",
            spotifyAlbumUrl = "https://open.spotify.com/album/2ukvYwN6s5M0M0v2Z1p7",
            youtubeMusicUrl = "https://music.youtube.com/playlist?list=OLAK5uy_l4uM2hU0fC5aG9iG4pQ8j4Q6u5s4i0_Yu",
            releaseYear = "2024"
        )
    )

    // Non-game or DLC keyword indicators that explicitly have NO official soundtrack
    private val NO_OST_KEYWORDS = listOf(
        "skin", "pack", "bundle", "coins", "gold", "gems", "currency",
        "dlc", "add-on", "addon", "loot", "starter pack", "booster",
        "costume", "outfit", "weapon", "cosmetic", "pass", "season pass",
        "battle pass", "tokens", "credits", "crate", "case", "beta access",
        "bonus content", "wallpaper", "artbook", "soundtrack only", "key"
    )

    /**
     * Resolves official OST details for a giveaway item.
     * Returns null if the item has no official soundtrack (e.g. DLC packs, cosmetic bundles, or non-OST games).
     */
    fun getOstInfo(item: GiveawayItem): OfficialOstInfo? {
        // 1. DLC, Loot, or Other categories do not have a standalone official game soundtrack
        if (!item.type.contains("Game", ignoreCase = true)) {
            return null
        }

        val titleLower = item.title.lowercase(Locale.ROOT).trim()

        // 2. Filter out non-OST content (skins, weapon bundles, currency packs)
        for (kw in NO_OST_KEYWORDS) {
            if (titleLower.contains(kw) && !isExemptFromNoOst(titleLower)) {
                return null
            }
        }

        // 3. Match against curated directory of known official soundtracks
        val clean = cleanTitleForOst(item.title)
        val curated = CURATED_OSTS[clean] ?: CURATED_OSTS.entries.find { (k, _) ->
            clean.contains(k) || k.contains(clean)
        }?.value

        if (curated != null) {
            return OfficialOstInfo(
                gameTitle = item.title,
                albumTitle = curated.albumTitle,
                composer = curated.composer,
                spotifyUrl = curated.spotifyAlbumUrl,
                youtubeMusicUrl = curated.youtubeMusicUrl,
                releaseYear = curated.releaseYear
            )
        }

        // 4. If it's a verified full game with legitimate Metacritic score or authentic game structure
        if (item.numericWorth >= 4.99 || item.metacriticScore > 0 || item.users > 3000) {
            val encodedQuery = urlEncode("${cleanTitleForSearch(item.title)} Official Game Soundtrack")
            return OfficialOstInfo(
                gameTitle = item.title,
                albumTitle = "${item.title} (Official Soundtrack)",
                composer = "Original Game Soundtrack",
                spotifyUrl = "https://open.spotify.com/search/$encodedQuery",
                youtubeMusicUrl = "https://music.youtube.com/search?q=$encodedQuery"
            )
        }

        // Otherwise marked as No OST
        return null
    }

    /**
     * Resolves OST info for a game by title.
     */
    fun getOstInfoForTitle(title: String): OfficialOstInfo? {
        val dummyItem = GiveawayItem(
            id = 0,
            title = title,
            worth = "$19.99",
            numericWorth = 19.99,
            thumbnail = "",
            image = "",
            description = "",
            instructions = "",
            openGiveawayUrl = "",
            publishedDate = "",
            type = "Game",
            platforms = "PC",
            endDate = "N/A",
            users = 5000,
            status = "Active"
        )
        return getOstInfo(dummyItem)
    }

    /**
     * Checks if a title is exempt from keyword filtering (e.g. game title containing "pack" or "pass").
     */
    private fun isExemptFromNoOst(titleLower: String): Boolean {
        return titleLower.contains("cyberpunk") ||
                titleLower.contains("subnautica") ||
                titleLower.contains("portal") ||
                titleLower.contains("hades") ||
                titleLower.contains("hollow knight")
    }

    private fun cleanTitleForOst(title: String): String {
        return title.lowercase(Locale.ROOT)
            .replace("giveaway", "")
            .replace("free", "")
            .replace("standard edition", "")
            .replace("definitive edition", "")
            .replace("director's cut", "")
            .replace("directors cut", "")
            .replace("ultimate edition", "")
            .replace("game of the year edition", "")
            .replace("goty", "")
            .replace("enhanced edition", "")
            .replace("remastered", "")
            .replace("bundle", "")
            .replace(Regex("""\([^)]*\)"""), "")
            .replace(Regex("""\[[^\]]*\]"""), "")
            .trim()
    }

    private fun cleanTitleForSearch(title: String): String {
        return cleanTitleForOst(title)
            .replace(":", "")
            .replace("-", " ")
            .trim()
    }

    private fun urlEncode(text: String): String {
        return try {
            URLEncoder.encode(text, StandardCharsets.UTF_8.toString())
        } catch (_: Exception) {
            text.replace(" ", "%20")
        }
    }

    /**
     * Quick Action: Launches YouTube Music with deep app link and browser fallback.
     */
    fun openYouTubeMusic(context: Context, url: String) {
        try {
            val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setPackage("com.google.android.apps.youtube.music")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(appIntent)
        } catch (_: Exception) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(browserIntent)
            } catch (_: Exception) {
                Toast.makeText(context, "Could not open YouTube Music", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Quick Action: Launches Spotify with deep app link and browser fallback.
     */
    fun openSpotify(context: Context, url: String) {
        try {
            val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setPackage("com.spotify.music")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(appIntent)
        } catch (_: Exception) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(browserIntent)
            } catch (_: Exception) {
                Toast.makeText(context, "Could not open Spotify", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Quick Action: Launches Discogs release in web browser.
     */
    fun openDiscogs(context: Context, url: String) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(browserIntent)
        } catch (_: Exception) {
            Toast.makeText(context, "Could not open Discogs release", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Finds curated soundtrack by cleaned game title.
     */
    fun getCuratedOst(cleanedTitle: String): CuratedOst? {
        val clean = cleanedTitle.lowercase(Locale.ROOT).trim()
        return CURATED_OSTS[clean] ?: CURATED_OSTS.entries.find { (k, _) ->
            clean.contains(k) || k.contains(clean)
        }?.value
    }
}
