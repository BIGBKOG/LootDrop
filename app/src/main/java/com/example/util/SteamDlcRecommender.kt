package com.example.util

import com.example.data.local.SteamGameEntity
import com.example.data.model.GiveawayItem
import com.example.data.model.PlayStatus
import com.example.data.model.SteamDlcRecommendation
import com.example.data.remote.SteamApiClient
import com.example.preferences.AppPreferences
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

object SteamDlcRecommender {

    private const val CACHE_TTL_MS = 12L * 60L * 60L * 1000L // 12 hours

    private val COSMETIC_OR_SOUNDTRACK_REGEX = Regex(
        """(?i)\b(soundtrack|ost|original score|art\s*book|digital art|wallpaper|music pack|digital extras|comic book|ringtones)\b"""
    )

    private val DLC_GIVEAWAY_INDICATOR_REGEX = Regex(
        """(?i)\b(dlc|expansion|season pass|starter pack|booster pack|add-on|addon|bundle|chapter|pass|cosmetic|skin|pack)\b"""
    )

    /**
     * Matches active Free DLC / Loot giveaways in LootDrop against the user's owned Steam games.
     */
    fun findFreeDlcGiveawaysForSteamLibrary(
        giveaways: List<GiveawayItem>,
        steamGames: List<SteamGameEntity>
    ): List<SteamDlcRecommendation> {
        if (giveaways.isEmpty() || steamGames.isEmpty()) return emptyList()

        // Precompute clean owned game titles (minimum 4 chars to avoid spurious short-word matches)
        val indexedOwned = steamGames.mapNotNull { game ->
            val clean = normalizeForBaseGameMatch(game.name)
            if (clean.length >= 4) clean to game else null
        }.sortedByDescending { it.first.length }

        if (indexedOwned.isEmpty()) return emptyList()

        val matches = mutableListOf<SteamDlcRecommendation>()

        for (giveaway in giveaways) {
            if (!giveaway.status.equals("Active", ignoreCase = true)) continue

            val typeLower = giveaway.type.lowercase(Locale.ROOT)
            val isDlcGiveaway = typeLower.contains("dlc") ||
                typeLower.contains("loot") ||
                DLC_GIVEAWAY_INDICATOR_REGEX.containsMatchIn(giveaway.title)

            if (!isDlcGiveaway) continue

            val giveawayNorm = normalizeForBaseGameMatch(giveaway.title)
            if (giveawayNorm.isBlank()) continue

            val matchedParent = indexedOwned.firstOrNull { (ownedNorm, game) ->
                if (giveaway.steamAppId != null && giveaway.steamAppId == game.appId) {
                    // Same appId as base game only if title explicitly marks it as DLC/Pack
                    DLC_GIVEAWAY_INDICATOR_REGEX.containsMatchIn(giveaway.title)
                } else {
                    isClearTitleContainment(haystack = giveawayNorm, needle = ownedNorm)
                }
            }?.second ?: continue

            val dlcAppId = SteamApiClient.extractSteamAppId(giveaway.openGiveawayUrl)
                ?: giveaway.steamAppId
                ?: (9_000_000 + giveaway.id)

            val playtimeLabel = if (matchedParent.playtimeMinutes > 0) {
                "${matchedParent.playtimeHours} played"
            } else {
                "Owned in Steam"
            }

            val bonusScore = 1500 +
                minOf(matchedParent.playtimeMinutes / 10, 300) +
                if (!giveaway.isClaimed) 300 else 0

            matches.add(
                SteamDlcRecommendation(
                    dlcAppId = dlcAppId,
                    name = giveaway.title,
                    parentAppId = matchedParent.appId,
                    parentGameName = matchedParent.name,
                    parentPlaytimeMinutes = matchedParent.playtimeMinutes,
                    parentPlayStatus = matchedParent.playStatus,
                    headerImageUrl = giveaway.image.ifBlank { giveaway.thumbnail },
                    shortDescription = giveaway.description,
                    isFree = true,
                    discountPercent = 100,
                    priceFormatted = "FREE",
                    initialPriceFormatted = giveaway.worth.takeIf { it != "N/A" && it.isNotBlank() } ?: "",
                    recommendationsCount = giveaway.users,
                    releaseDate = giveaway.publishedDate,
                    metacriticScore = giveaway.metacriticScore.takeIf { it > 0 },
                    reasonBadge = if (giveaway.isClaimed) "✓ Claimed Free DLC" else "🎁 Free DLC Drop",
                    recommendationReason = "100% Free giveaway for ${matchedParent.name} ($playtimeLabel)",
                    storeUrl = giveaway.openGiveawayUrl,
                    matchedGiveawayId = giveaway.id,
                    isSoundtrackOrCosmetic = false,
                    relevanceScore = bonusScore
                )
            )
        }

        return matches.sortedByDescending { it.relevanceScore }
    }

    /**
     * Picks the best owned Steam games to scan for DLC recommendations:
     * recently played, currently playing, and highest playtime favorites.
     */
    fun selectCandidateGamesForDlcScan(
        steamGames: List<SteamGameEntity>,
        maxGames: Int = 8
    ): List<SteamGameEntity> {
        if (steamGames.isEmpty()) return emptyList()
        return steamGames
            .filter { it.appId > 0 && it.name.isNotBlank() }
            .sortedWith(
                compareByDescending<SteamGameEntity> {
                    if (it.playStatus == PlayStatus.DROPPED) {
                        -20_000L + minOf(it.playtimeMinutes.toLong(), 10_000L)
                    } else {
                        var weight = 0L
                        if (it.playtime2WeeksMinutes > 0) weight += 10_000L
                        when (it.playStatus) {
                            PlayStatus.PLAYING -> weight += 5_000L
                            PlayStatus.BEATEN -> weight += 3_000L
                            PlayStatus.NOT_STARTED -> weight += 500L
                            PlayStatus.DROPPED -> Unit
                        }
                        weight + minOf(it.playtimeMinutes.toLong(), 10_000L)
                    }
                }.thenByDescending { it.lastPlayedTimestamp }
            )
            .take(maxGames)
    }

    /**
     * Converts raw Steam Store DLC metadata + parent owned game context into a ranked recommendation.
     */
    fun buildRecommendation(
        dlcInfo: SteamApiClient.SteamStoreDlcInfo,
        parentGame: SteamGameEntity,
        isAlreadyOwned: Boolean = false
    ): SteamDlcRecommendation? {
        if (dlcInfo.comingSoon || isAlreadyOwned) return null

        val isCosmeticOrOst = COSMETIC_OR_SOUNDTRACK_REGEX.containsMatchIn(dlcInfo.name)
        var score = 0

        if (dlcInfo.isFree) score += 600
        if (dlcInfo.discountPercent > 0) score += dlcInfo.discountPercent * 5

        // Community approval boost
        score += minOf(dlcInfo.recommendationsCount / 15, 500)

        // Parent game ownership context
        score += minOf(parentGame.playtimeMinutes / 15, 300)
        if (parentGame.playtime2WeeksMinutes > 0) score += 220
        when (parentGame.playStatus) {
            PlayStatus.PLAYING -> score += 180
            PlayStatus.BEATEN -> score += 130
            PlayStatus.NOT_STARTED -> score += 20
            PlayStatus.DROPPED -> score -= 120
        }

        // Metacritic bonus
        val meta = dlcInfo.metacriticScore
        if (meta != null && meta > 65) {
            score += (meta - 65) * 4
        }

        // Keep actual expansions & gameplay packs ahead of soundtracks/artbooks
        if (isCosmeticOrOst) {
            score -= 450
        }

        val playtimeText = if (parentGame.playtimeMinutes > 0) {
            "${parentGame.playtimeHours} played"
        } else {
            "In Steam Library"
        }

        val reasonBadge = when {
            dlcInfo.isFree -> "🎁 Free DLC"
            dlcInfo.discountPercent >= 50 -> "🔥 -${dlcInfo.discountPercent}% Deal"
            dlcInfo.discountPercent > 0 -> "🏷️ -${dlcInfo.discountPercent}% Off"
            isCosmeticOrOst -> "🎵 Soundtrack / Extra"
            dlcInfo.recommendationsCount >= 5000 -> "⭐ Fan Favorite"
            parentGame.playStatus == PlayStatus.PLAYING -> "🎮 Expand Active Game"
            parentGame.playtimeMinutes >= 1200 -> "🏆 Top Game Pick"
            else -> "🧩 DLC Expansion"
        }

        val reasonText = when {
            dlcInfo.isFree -> "Free add-on for ${parentGame.name} ($playtimeText)"
            dlcInfo.discountPercent > 0 -> "On sale (-${dlcInfo.discountPercent}%) for ${parentGame.name} • $playtimeText"
            dlcInfo.recommendationsCount >= 1000 -> "Popular expansion for ${parentGame.name} ($playtimeText)"
            parentGame.playStatus == PlayStatus.PLAYING -> "You're currently playing ${parentGame.name} ($playtimeText)"
            parentGame.playtimeMinutes > 0 -> "Recommended based on your $playtimeText in ${parentGame.name}"
            else -> "Official expansion for ${parentGame.name} in your library"
        }

        return SteamDlcRecommendation(
            dlcAppId = dlcInfo.dlcAppId,
            name = dlcInfo.name,
            parentAppId = parentGame.appId,
            parentGameName = parentGame.name,
            parentPlaytimeMinutes = parentGame.playtimeMinutes,
            parentPlayStatus = parentGame.playStatus,
            headerImageUrl = dlcInfo.headerImageUrl,
            shortDescription = dlcInfo.shortDescription,
            isFree = dlcInfo.isFree,
            discountPercent = dlcInfo.discountPercent,
            priceFormatted = dlcInfo.priceFormatted.ifBlank { if (dlcInfo.isFree) "Free" else "View Price" },
            initialPriceFormatted = dlcInfo.initialPriceFormatted,
            recommendationsCount = dlcInfo.recommendationsCount,
            releaseDate = dlcInfo.releaseDate,
            metacriticScore = dlcInfo.metacriticScore,
            reasonBadge = reasonBadge,
            recommendationReason = reasonText,
            storeUrl = "https://store.steampowered.com/app/${dlcInfo.dlcAppId}",
            matchedGiveawayId = null,
            isSoundtrackOrCosmetic = isCosmeticOrOst,
            relevanceScore = score
        )
    }

    /**
     * Fetches Storefront DLC recommendations across top owned Steam games.
     */
    suspend fun fetchRecommendationsForLibrary(
        steamGames: List<SteamGameEntity>,
        dismissedIds: Set<Int>,
        onProgress: (String) -> Unit = {}
    ): List<SteamDlcRecommendation> {
        val candidates = selectCandidateGamesForDlcScan(steamGames, maxGames = 7)
        if (candidates.isEmpty()) return emptyList()

        val ownedAppIds = steamGames.map { it.appId }.toSet()
        val collected = mutableListOf<SteamDlcRecommendation>()
        val seenDlcIds = mutableSetOf<Int>()

        for (game in candidates) {
            onProgress("Checking DLCs for ${game.name}...")
            val allDlcIds = SteamApiClient.getAppDlcIds(game.appId)
                .filter { it !in ownedAppIds && it !in dismissedIds && seenDlcIds.add(it) }

            if (allDlcIds.isEmpty()) continue

            // Sample newest expansions (end of list) plus earliest major DLC (start of list)
            val sampledIds = buildList {
                addAll(allDlcIds.takeLast(4).reversed())
                if (allDlcIds.size > 4) {
                    add(allDlcIds.first())
                }
            }.distinct().take(5)

            // Fetch in small parallel chunks to avoid Steam rate limiting
            for (chunk in sampledIds.chunked(3)) {
                val infos = coroutineScope {
                    chunk.map { dlcId ->
                        async { SteamApiClient.getDlcStoreDetails(dlcId) }
                    }.awaitAll()
                }
                for (info in infos) {
                    if (info != null) {
                        val rec = buildRecommendation(info, game, isAlreadyOwned = info.dlcAppId in ownedAppIds)
                        if (rec != null) {
                            collected.add(rec)
                        }
                    }
                }
                delay(150)
            }
        }

        // Prioritize gameplay DLCs first, while keeping at least up to 30 strong results
        return collected
            .distinctBy { it.dlcAppId }
            .sortedWith(
                compareBy<SteamDlcRecommendation> { it.isSoundtrackOrCosmetic }
                    .thenByDescending { it.relevanceScore }
            )
            .take(30)
    }

    /**
     * Fetches up to 12 DLCs specifically for a single owned Steam game on demand.
     */
    suspend fun fetchDlcsForSingleGame(
        parentGame: SteamGameEntity,
        ownedAppIds: Set<Int>,
        dismissedIds: Set<Int> = emptySet()
    ): List<SteamDlcRecommendation> {
        val allDlcIds = SteamApiClient.getAppDlcIds(parentGame.appId)
            .filter { it !in ownedAppIds && it !in dismissedIds }

        if (allDlcIds.isEmpty()) return emptyList()

        // Blend newest 8 DLCs with earliest 4 classic expansions
        val targetIds = buildList {
            addAll(allDlcIds.takeLast(8).reversed())
            addAll(allDlcIds.take(4))
        }.distinct().take(12)

        val results = mutableListOf<SteamDlcRecommendation>()
        for (chunk in targetIds.chunked(3)) {
            val details = coroutineScope {
                chunk.map { id ->
                    async { SteamApiClient.getDlcStoreDetails(id) }
                }.awaitAll()
            }
            for (info in details) {
                if (info != null) {
                    val rec = buildRecommendation(info, parentGame, isAlreadyOwned = info.dlcAppId in ownedAppIds)
                    if (rec != null) {
                        results.add(rec)
                    }
                }
            }
            delay(120)
        }

        return results
            .distinctBy { it.dlcAppId }
            .sortedWith(
                compareBy<SteamDlcRecommendation> { it.isSoundtrackOrCosmetic }
                    .thenByDescending { it.relevanceScore }
            )
    }

    // --- Local Cache Helpers ---

    fun isCacheFresh(prefs: AppPreferences, nowMs: Long = System.currentTimeMillis()): Boolean {
        val ts = prefs.steamDlcCacheTimestamp
        return ts > 0L && (nowMs - ts) < CACHE_TTL_MS && prefs.steamDlcCacheJson.isNotBlank()
    }

    fun saveCachedDlcs(prefs: AppPreferences, list: List<SteamDlcRecommendation>, nowMs: Long = System.currentTimeMillis()) {
        // Only cache Storefront DLCs (Free giveaway matches are computed dynamically from live DB giveaways)
        val storeOnly = list.filter { it.matchedGiveawayId == null }
        prefs.steamDlcCacheJson = serializeRecommendations(storeOnly)
        prefs.steamDlcCacheTimestamp = nowMs
    }

    fun loadCachedDlcs(prefs: AppPreferences): List<SteamDlcRecommendation> {
        val raw = prefs.steamDlcCacheJson
        if (raw.isBlank()) return emptyList()
        return deserializeRecommendations(raw)
    }

    fun serializeRecommendations(list: List<SteamDlcRecommendation>): String {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("dlcAppId", item.dlcAppId)
                put("name", item.name)
                put("parentAppId", item.parentAppId)
                put("parentGameName", item.parentGameName)
                put("parentPlaytimeMinutes", item.parentPlaytimeMinutes)
                put("parentPlayStatus", item.parentPlayStatus.name)
                put("headerImageUrl", item.headerImageUrl)
                put("shortDescription", item.shortDescription)
                put("isFree", item.isFree)
                put("discountPercent", item.discountPercent)
                put("priceFormatted", item.priceFormatted)
                put("initialPriceFormatted", item.initialPriceFormatted)
                put("recommendationsCount", item.recommendationsCount)
                put("releaseDate", item.releaseDate)
                if (item.metacriticScore != null) put("metacriticScore", item.metacriticScore)
                put("reasonBadge", item.reasonBadge)
                put("recommendationReason", item.recommendationReason)
                put("storeUrl", item.storeUrl)
                if (item.matchedGiveawayId != null) put("matchedGiveawayId", item.matchedGiveawayId)
                put("isSoundtrackOrCosmetic", item.isSoundtrackOrCosmetic)
                put("relevanceScore", item.relevanceScore)
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun deserializeRecommendations(json: String): List<SteamDlcRecommendation> {
        return runCatching {
            val array = JSONArray(json)
            val list = mutableListOf<SteamDlcRecommendation>()
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val dlcAppId = obj.optInt("dlcAppId", 0)
                val name = obj.optString("name", "")
                if (dlcAppId <= 0 || name.isBlank()) continue
                list.add(
                    SteamDlcRecommendation(
                        dlcAppId = dlcAppId,
                        name = name,
                        parentAppId = obj.optInt("parentAppId", 0),
                        parentGameName = obj.optString("parentGameName", ""),
                        parentPlaytimeMinutes = obj.optInt("parentPlaytimeMinutes", 0),
                        parentPlayStatus = PlayStatus.fromString(obj.optString("parentPlayStatus")),
                        headerImageUrl = obj.optString("headerImageUrl", ""),
                        shortDescription = obj.optString("shortDescription", ""),
                        isFree = obj.optBoolean("isFree", false),
                        discountPercent = obj.optInt("discountPercent", 0),
                        priceFormatted = obj.optString("priceFormatted", ""),
                        initialPriceFormatted = obj.optString("initialPriceFormatted", ""),
                        recommendationsCount = obj.optInt("recommendationsCount", 0),
                        releaseDate = obj.optString("releaseDate", ""),
                        metacriticScore = if (obj.has("metacriticScore")) obj.optInt("metacriticScore") else null,
                        reasonBadge = obj.optString("reasonBadge", "Expansion Pick"),
                        recommendationReason = obj.optString("recommendationReason", ""),
                        storeUrl = obj.optString("storeUrl", "https://store.steampowered.com/app/$dlcAppId"),
                        matchedGiveawayId = if (obj.has("matchedGiveawayId")) obj.optInt("matchedGiveawayId") else null,
                        isSoundtrackOrCosmetic = obj.optBoolean("isSoundtrackOrCosmetic", false),
                        relevanceScore = obj.optInt("relevanceScore", 0)
                    )
                )
            }
            list
        }.getOrDefault(emptyList())
    }

    private fun normalizeForBaseGameMatch(title: String): String {
        return title
            .replace(Regex("[™®©:]"), " ")
            .replace(Regex("\\([^)]*\\)"), " ")
            .replace(Regex("\\[[^\\]]*\\]"), " ")
            .replace(Regex("[^a-zA-Z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .lowercase(Locale.ROOT)
    }

    private fun isClearTitleContainment(haystack: String, needle: String): Boolean {
        if (needle.length < 4) return false
        if (haystack == needle) return false // Exact same name without DLC qualifier isn't treated as DLC match unless appId+DLC tag matched
        val escaped = Regex.escape(needle)
        return Regex("""(?:^|\s)$escaped(?:\s|$)""").containsMatchIn(haystack)
    }
}
