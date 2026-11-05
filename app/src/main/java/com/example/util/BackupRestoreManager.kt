package com.example.util

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.example.data.local.AppDatabase
import com.example.data.local.GiveawayEntity
import com.example.data.local.SteamGameEntity
import com.example.data.model.PlayStatus
import com.example.preferences.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupSummary(
    val claimedVaultCount: Int,
    val bookmarkedVaultCount: Int,
    val steamGameCount: Int,
    val totalGamesCount: Int,
    val fileSizeFormatted: String
)

data class RestoreSummary(
    val claimedRestoredCount: Int,
    val bookmarkedRestoredCount: Int,
    val steamRestoredCount: Int,
    val totalRestoredCount: Int,
    val message: String
)

/**
 * Utility to backup and restore games, user curation statuses, and Steam library data
 * to a standardized JSON file that can be transferred across devices.
 */
object BackupRestoreManager {

    private const val BACKUP_SCHEMA_VERSION = 1
    private const val APP_IDENTIFIER = "LootDrop"

    /**
     * Serializes user's claimed games, bookmarks, customized metadata, and Steam library into a JSON string.
     */
    fun createBackupJson(
        allGiveaways: List<GiveawayEntity>,
        steamGames: List<SteamGameEntity>,
        steamUserId: String = "",
        userProfileName: String = "",
        userProfileAvatarBase64: String = "",
        userProfileHasCustomTrophies: Boolean = false,
        userProfileDisplayedTrophyKeys: Set<String> = emptySet()
    ): String {
        val root = JSONObject()
        root.put("schemaVersion", BACKUP_SCHEMA_VERSION)
        root.put("appName", APP_IDENTIFIER)
        root.put("exportTimestamp", System.currentTimeMillis())
        root.put("exportDateIso", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()))
        root.put("steamUserId", steamUserId)

        // User Profile
        if (userProfileName.isNotBlank() || userProfileAvatarBase64.isNotBlank() || userProfileHasCustomTrophies) {
            val profileObj = JSONObject()
            if (userProfileName.isNotBlank()) profileObj.put("name", userProfileName)
            if (userProfileAvatarBase64.isNotBlank()) {
                profileObj.put("avatarBase64", userProfileAvatarBase64)
            }
            if (userProfileHasCustomTrophies) {
                profileObj.put("hasCustomTrophies", true)
                val trophyArr = org.json.JSONArray()
                userProfileDisplayedTrophyKeys.forEach { trophyArr.put(it) }
                profileObj.put("displayedTrophyKeys", trophyArr)
            }
            root.put("userProfile", profileObj)
        }

        // Only export giveaways that the user has interacted with (claimed, bookmarked, play status, custom size)
        val userGiveaways = allGiveaways.filter {
            it.isClaimed || it.isBookmarked || it.playStatus != PlayStatus.NOT_STARTED || it.manualInstallSizeGb != null
        }

        val summaryObj = JSONObject()
        val claimedCount = userGiveaways.count { it.isClaimed }
        val bookmarkedCount = userGiveaways.count { it.isBookmarked }
        summaryObj.put("claimedVaultCount", claimedCount)
        summaryObj.put("bookmarkedVaultCount", bookmarkedCount)
        summaryObj.put("steamGameCount", steamGames.size)
        summaryObj.put("totalGamesCount", claimedCount + steamGames.size)
        root.put("summary", summaryObj)

        // Claimed and Bookmarked Vault Games array
        val giveawaysArray = JSONArray()
        for (g in userGiveaways) {
            val gObj = JSONObject()
            gObj.put("id", g.id)
            gObj.put("title", g.title)
            gObj.put("worth", g.worth)
            gObj.put("numericWorth", g.numericWorth)
            gObj.put("thumbnail", g.thumbnail)
            gObj.put("image", g.image)
            gObj.put("description", g.description)
            gObj.put("instructions", g.instructions)
            gObj.put("openGiveawayUrl", g.openGiveawayUrl)
            gObj.put("publishedDate", g.publishedDate)
            gObj.put("type", g.type)
            gObj.put("platforms", g.platforms)
            gObj.put("endDate", g.endDate)
            gObj.put("users", g.users)
            gObj.put("status", g.status)
            gObj.put("isClaimed", g.isClaimed)
            gObj.put("isBookmarked", g.isBookmarked)
            gObj.put("playStatus", g.playStatus.name)
            if (g.steamAppId != null) gObj.put("steamAppId", g.steamAppId)
            if (g.manualInstallSizeGb != null) gObj.put("manualInstallSizeGb", g.manualInstallSizeGb.toDouble())
            gObj.put("firstSeenTimestamp", g.firstSeenTimestamp)
            giveawaysArray.put(gObj)
        }
        root.put("claimedVaultGames", giveawaysArray)

        // Steam library games array
        val steamArray = JSONArray()
        for (s in steamGames) {
            val sObj = JSONObject()
            sObj.put("appId", s.appId)
            sObj.put("name", s.name)
            sObj.put("playtimeMinutes", s.playtimeMinutes)
            sObj.put("playtime2WeeksMinutes", s.playtime2WeeksMinutes)
            sObj.put("lastPlayedTimestamp", s.lastPlayedTimestamp)
            sObj.put("playStatus", s.playStatus.name)
            sObj.put("achievementsUnlocked", s.achievementsUnlocked)
            sObj.put("achievementsTotal", s.achievementsTotal)
            sObj.put("imgIconUrl", s.imgIconUrl)
            sObj.put("headerImageUrl", s.headerImageUrl)
            if (s.matchedGiveawayId != null) sObj.put("matchedGiveawayId", s.matchedGiveawayId)
            sObj.put("isLootDropClaimed", s.isLootDropClaimed)
            if (s.manualInstallSizeGb != null) sObj.put("manualInstallSizeGb", s.manualInstallSizeGb.toDouble())
            steamArray.put(sObj)
        }
        root.put("steamGames", steamArray)

        return root.toString(2)
    }

    /**
     * Parses a backup JSON string and restores games into the database and preferences.
     */
    suspend fun parseAndRestoreJson(
        jsonString: String,
        database: AppDatabase,
        preferences: AppPreferences
    ): RestoreSummary = withContext(Dispatchers.IO) {
        val root = JSONObject(jsonString)

        val appName = root.optString("appName", "")
        if (appName.isNotEmpty() && !appName.equals(APP_IDENTIFIER, ignoreCase = true) && !appName.contains("loot", ignoreCase = true)) {
            // Soft check, allow restore but log/proceed
        }

        var claimedRestored = 0
        var bookmarkedRestored = 0
        var steamRestored = 0

        // 1. Restore Vault Games
        val vaultArray = root.optJSONArray("claimedVaultGames") ?: root.optJSONArray("giveaways") ?: root.optJSONArray("claimedGames")
        if (vaultArray != null) {
            val toInsert = mutableListOf<GiveawayEntity>()
            for (i in 0 until vaultArray.length()) {
                val gObj = vaultArray.optJSONObject(i) ?: continue
                val id = gObj.optInt("id", -1)
                if (id <= 0) continue

                val isClaimed = gObj.optBoolean("isClaimed", true)
                val isBookmarked = gObj.optBoolean("isBookmarked", false)
                val playStatusStr = gObj.optString("playStatus", PlayStatus.NOT_STARTED.name)
                val playStatus = runCatching { PlayStatus.valueOf(playStatusStr) }.getOrDefault(PlayStatus.NOT_STARTED)
                val manualSize = if (gObj.has("manualInstallSizeGb") && !gObj.isNull("manualInstallSizeGb")) {
                    gObj.optDouble("manualInstallSizeGb").toFloat()
                } else null
                val steamAppId = if (gObj.has("steamAppId") && !gObj.isNull("steamAppId")) gObj.optInt("steamAppId") else null

                val existing = database.giveawayDao().getGiveawayById(id)
                if (existing != null) {
                    database.giveawayDao().updateClaimed(id, isClaimed)
                    database.giveawayDao().updateBookmarked(id, isBookmarked)
                    database.giveawayDao().updatePlayStatus(id, playStatus)
                    if (manualSize != null) {
                        database.giveawayDao().updateManualInstallSize(id, manualSize)
                    }
                } else {
                    val entity = GiveawayEntity(
                        id = id,
                        title = gObj.optString("title", "Game #$id"),
                        worth = gObj.optString("worth", "N/A"),
                        numericWorth = gObj.optDouble("numericWorth", 0.0),
                        thumbnail = gObj.optString("thumbnail", ""),
                        image = gObj.optString("image", ""),
                        description = gObj.optString("description", ""),
                        instructions = gObj.optString("instructions", ""),
                        openGiveawayUrl = gObj.optString("openGiveawayUrl", ""),
                        publishedDate = gObj.optString("publishedDate", ""),
                        type = gObj.optString("type", "Game"),
                        platforms = gObj.optString("platforms", "PC"),
                        endDate = gObj.optString("endDate", ""),
                        users = gObj.optInt("users", 0),
                        status = gObj.optString("status", "Active"),
                        isClaimed = isClaimed,
                        isBookmarked = isBookmarked,
                        firstSeenTimestamp = gObj.optLong("firstSeenTimestamp", System.currentTimeMillis()),
                        notified = false,
                        playStatus = playStatus,
                        steamAppId = steamAppId,
                        manualInstallSizeGb = manualSize
                    )
                    toInsert.add(entity)
                }

                if (isClaimed) claimedRestored++
                if (isBookmarked) bookmarkedRestored++
            }
            if (toInsert.isNotEmpty()) {
                database.giveawayDao().insertAll(toInsert)
            }
        }

        // 2. Restore Steam Games
        val steamArray = root.optJSONArray("steamGames")
        if (steamArray != null && steamArray.length() > 0) {
            val steamList = mutableListOf<SteamGameEntity>()
            for (i in 0 until steamArray.length()) {
                val sObj = steamArray.optJSONObject(i) ?: continue
                val appId = sObj.optInt("appId", -1)
                if (appId <= 0) continue

                val sPlayStatusStr = sObj.optString("playStatus", PlayStatus.NOT_STARTED.name)
                val sPlayStatus = runCatching { PlayStatus.valueOf(sPlayStatusStr) }.getOrDefault(PlayStatus.NOT_STARTED)
                val sManualSize = if (sObj.has("manualInstallSizeGb") && !sObj.isNull("manualInstallSizeGb")) {
                    sObj.optDouble("manualInstallSizeGb").toFloat()
                } else null
                val matchedId = if (sObj.has("matchedGiveawayId") && !sObj.isNull("matchedGiveawayId")) sObj.optInt("matchedGiveawayId") else null

                val steamEntity = SteamGameEntity(
                    appId = appId,
                    name = sObj.optString("name", "Steam Game $appId"),
                    playtimeMinutes = sObj.optInt("playtimeMinutes", 0),
                    playtime2WeeksMinutes = sObj.optInt("playtime2WeeksMinutes", 0),
                    lastPlayedTimestamp = sObj.optLong("lastPlayedTimestamp", 0L),
                    playStatus = sPlayStatus,
                    achievementsUnlocked = sObj.optInt("achievementsUnlocked", 0),
                    achievementsTotal = sObj.optInt("achievementsTotal", 0),
                    imgIconUrl = sObj.optString("imgIconUrl", ""),
                    headerImageUrl = sObj.optString("headerImageUrl", "https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/$appId/header.jpg"),
                    matchedGiveawayId = matchedId,
                    isLootDropClaimed = sObj.optBoolean("isLootDropClaimed", false),
                    manualInstallSizeGb = sManualSize
                )
                steamList.add(steamEntity)
                steamRestored++
            }
            if (steamList.isNotEmpty()) {
                database.steamGameDao().insertAll(steamList)
            }
        }

        // 3. Restore Steam User ID if present and currently unconfigured
        val steamUserId = root.optString("steamUserId", "")
        if (steamUserId.isNotBlank() && preferences.steamUserId.isBlank()) {
            preferences.steamUserId = steamUserId
        }

        // 4. Restore User Profile if present
        val profileObj = root.optJSONObject("userProfile")
        if (profileObj != null) {
            val name = profileObj.optString("name", "")
            if (name.isNotBlank()) {
                preferences.userProfileName = name
            }
            val avatarBase64 = profileObj.optString("avatarBase64", "")
            if (avatarBase64.isNotBlank()) {
                val savedPath = UserProfileManager.saveAvatarFromBase64(preferences.appContext, avatarBase64)
                if (savedPath != null) {
                    preferences.userProfileAvatarPath = savedPath
                }
            }
            val hasCustom = profileObj.optBoolean("hasCustomTrophies", false)
            if (hasCustom) {
                preferences.hasCustomTrophySelection = true
                val arr = profileObj.optJSONArray("displayedTrophyKeys")
                val set = mutableSetOf<String>()
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        set.add(arr.getString(i))
                    }
                }
                preferences.displayedTrophyKeys = set
            }
        }

        val total = claimedRestored + steamRestored
        val message = "Restored $claimedRestored claimed Vault games and $steamRestored Steam games!"
        RestoreSummary(
            claimedRestoredCount = claimedRestored,
            bookmarkedRestoredCount = bookmarkedRestored,
            steamRestoredCount = steamRestored,
            totalRestoredCount = total,
            message = message
        )
    }

    /**
     * Exports backup data directly to a Storage Access Framework Uri.
     */
    suspend fun exportBackup(
        context: Context,
        targetUri: Uri,
        database: AppDatabase,
        preferences: AppPreferences
    ): Result<BackupSummary> = withContext(Dispatchers.IO) {
        runCatching {
            val allGiveaways = database.giveawayDao().getAllGiveawaysList()
            val steamGames = database.steamGameDao().getAllSteamGamesList()
            val steamUserId = preferences.steamUserId
            val profileName = preferences.userProfileName
            val avatarBase64 = UserProfileManager.getAvatarBase64(context, preferences.userProfileAvatarPath)

            val jsonString = createBackupJson(
                allGiveaways = allGiveaways,
                steamGames = steamGames,
                steamUserId = steamUserId,
                userProfileName = profileName,
                userProfileAvatarBase64 = avatarBase64,
                userProfileHasCustomTrophies = preferences.hasCustomTrophySelection,
                userProfileDisplayedTrophyKeys = preferences.displayedTrophyKeys
            )
            val bytes = jsonString.toByteArray(Charsets.UTF_8)

            context.contentResolver.openOutputStream(targetUri)?.use { outputStream ->
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(jsonString)
                    writer.flush()
                }
            } ?: throw IllegalStateException("Could not open output stream for $targetUri")

            val userGiveawaysCount = allGiveaways.count { it.isClaimed }
            val bookmarkedCount = allGiveaways.count { it.isBookmarked }
            val formattedSize = if (bytes.size < 1024) "${bytes.size} B" else String.format(Locale.US, "%.1f KB", bytes.size / 1024f)

            BackupSummary(
                claimedVaultCount = userGiveawaysCount,
                bookmarkedVaultCount = bookmarkedCount,
                steamGameCount = steamGames.size,
                totalGamesCount = userGiveawaysCount + steamGames.size,
                fileSizeFormatted = formattedSize
            )
        }
    }

    /**
     * Restores backup data directly from a Storage Access Framework Uri.
     */
    suspend fun restoreBackup(
        context: Context,
        sourceUri: Uri,
        database: AppDatabase,
        preferences: AppPreferences
    ): Result<RestoreSummary> = withContext(Dispatchers.IO) {
        runCatching {
            val content = context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            } ?: throw IllegalStateException("Could not open input stream for $sourceUri")

            if (content.isBlank()) {
                throw IllegalArgumentException("The selected backup file is empty.")
            }

            parseAndRestoreJson(content, database, preferences)
        }
    }

    /**
     * Extracts a human-friendly display name for a SAF tree URI (e.g. Google Drive folder or local folder).
     */
    fun getFolderDisplayName(context: Context, folderTreeUri: Uri): String {
        return runCatching {
            val treeDocumentId = DocumentsContract.getTreeDocumentId(folderTreeUri)
            val docUri = DocumentsContract.buildDocumentUriUsingTree(folderTreeUri, treeDocumentId)
            context.contentResolver.query(
                docUri,
                arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val col = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    if (col >= 0) cursor.getString(col) else null
                } else null
            }
        }.getOrNull() ?: runCatching {
            val path = folderTreeUri.path ?: ""
            val last = path.substringAfterLast(":")
            if (last.isNotBlank()) last else folderTreeUri.lastPathSegment ?: "Cloud Folder"
        }.getOrDefault("Cloud Folder")
    }

    /**
     * Exports backup JSON directly into a user-selected folder tree URI (Google Drive, cloud, or local storage),
     * keeping the file current (overwriting or creating the file).
     */
    suspend fun exportToFolderUri(
        context: Context,
        folderTreeUri: Uri,
        database: AppDatabase,
        preferences: AppPreferences,
        fileName: String = "lootdrop_backup_latest.json"
    ): Result<BackupSummary> = withContext(Dispatchers.IO) {
        runCatching {
            val allGiveaways = database.giveawayDao().getAllGiveawaysList()
            val steamGames = database.steamGameDao().getAllSteamGamesList()
            val steamUserId = preferences.steamUserId
            val profileName = preferences.userProfileName
            val avatarBase64 = UserProfileManager.getAvatarBase64(context, preferences.userProfileAvatarPath)

            val jsonString = createBackupJson(
                allGiveaways = allGiveaways,
                steamGames = steamGames,
                steamUserId = steamUserId,
                userProfileName = profileName,
                userProfileAvatarBase64 = avatarBase64,
                userProfileHasCustomTrophies = preferences.hasCustomTrophySelection,
                userProfileDisplayedTrophyKeys = preferences.displayedTrophyKeys
            )
            val bytes = jsonString.toByteArray(Charsets.UTF_8)

            val treeDocumentId = DocumentsContract.getTreeDocumentId(folderTreeUri)
            val parentDocumentUri = DocumentsContract.buildDocumentUriUsingTree(folderTreeUri, treeDocumentId)

            var targetFileUri: Uri? = null
            // Check if file already exists in the folder to overwrite
            try {
                val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(folderTreeUri, treeDocumentId)
                val projection = arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME
                )
                context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                    val idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                    val nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    if (idCol >= 0 && nameCol >= 0) {
                        while (cursor.moveToNext()) {
                            val name = cursor.getString(nameCol)
                            if (name.equals(fileName, ignoreCase = true)) {
                                val docId = cursor.getString(idCol)
                                targetFileUri = DocumentsContract.buildDocumentUriUsingTree(folderTreeUri, docId)
                                break
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                // Querying child documents might fail on certain cloud content providers; fallback to create
            }

            if (targetFileUri == null) {
                targetFileUri = DocumentsContract.createDocument(
                    context.contentResolver,
                    parentDocumentUri,
                    "application/json",
                    fileName
                )
            }

            val fileUri = targetFileUri ?: throw IllegalStateException("Could not create '$fileName' in the selected folder.")

            val outStream = runCatching {
                context.contentResolver.openOutputStream(fileUri, "wt")
            }.getOrNull() ?: context.contentResolver.openOutputStream(fileUri)

            outStream?.use { stream ->
                OutputStreamWriter(stream, Charsets.UTF_8).use { writer ->
                    writer.write(jsonString)
                    writer.flush()
                }
            } ?: throw IllegalStateException("Could not write to destination backup file.")

            val claimedCount = allGiveaways.count { it.isClaimed }
            val bookmarkedCount = allGiveaways.count { it.isBookmarked }
            val formattedSize = if (bytes.size < 1024) "${bytes.size} B" else String.format(Locale.US, "%.1f KB", bytes.size / 1024f)

            val summary = BackupSummary(
                claimedVaultCount = claimedCount,
                bookmarkedVaultCount = bookmarkedCount,
                steamGameCount = steamGames.size,
                totalGamesCount = claimedCount + steamGames.size,
                fileSizeFormatted = formattedSize
            )

            preferences.lastAutoBackupTimestamp = System.currentTimeMillis()
            val timeFormatted = SimpleDateFormat("MMM d, h:mm a", Locale.US).format(Date())
            preferences.lastAutoBackupStatus = "✅ Current as of $timeFormatted (${summary.totalGamesCount} games, $formattedSize)"

            summary
        }
    }

    /**
     * Executes automatic scheduled backup to the user's configured cloud or drive folder.
     */
    suspend fun performScheduledAutoBackup(context: Context): Result<BackupSummary>? {
        val prefs = AppPreferences(context)
        if (!prefs.isAutoBackupEnabled || prefs.autoBackupFolderUri.isBlank()) {
            return null
        }

        val treeUri = runCatching { Uri.parse(prefs.autoBackupFolderUri) }.getOrNull() ?: return null

        // Check if persistable permissions are still active
        val hasPermission = try {
            context.contentResolver.persistedUriPermissions.any {
                it.uri == treeUri && it.isWritePermission
            }
        } catch (_: Exception) {
            true // In test or mocked environment, proceed
        }

        if (!hasPermission) {
            prefs.lastAutoBackupStatus = "⚠️ Folder access expired. Please reselect your backup folder."
            return Result.failure(SecurityException("Folder permission revoked or expired"))
        }

        val db = AppDatabase.getInstance(context)
        val result = exportToFolderUri(context, treeUri, db, prefs, "lootdrop_backup_latest.json")
        if (result.isFailure) {
            val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
            prefs.lastAutoBackupStatus = "❌ Auto-backup failed: $errorMsg"
        }
        return result
    }
}
