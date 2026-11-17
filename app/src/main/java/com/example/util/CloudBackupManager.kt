package com.example.util

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.i18n.AppLanguage
import com.example.preferences.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Manages Cloud Backups and Restores with Supabase Auth and Cloudflare Worker endpoints.
 */
object CloudBackupManager {

    private const val TAG = "CloudBackupManager"

    fun resolveSupabaseUrl(): String = CloudAuthTransport.resolveSupabaseUrl()
    fun resolveSupabaseAnonKey(): String = CloudAuthTransport.resolveSupabaseAnonKey()
    fun resolveCloudBackupUrl(): String = CloudAuthTransport.resolveCloudBackupUrl()

    fun isConfigured(): Boolean =
        resolveCloudBackupUrl().isNotBlank() ||
            (resolveSupabaseUrl().isNotBlank() && resolveSupabaseAnonKey().isNotBlank())

    fun getProviderHost(): String = if (isConfigured()) "Encrypted Private Cloud" else "Not Configured"
    fun deriveAccountId(username: String): String = CloudAuthTransport.deriveAccountId(username)
    fun hashPassword(password: String, username: String): String = CloudAuthTransport.hashPassword(password, username)
    fun resolveAuthEmail(identifier: String, preferences: AppPreferences): String = CloudAuthTransport.resolveAuthEmail(identifier, preferences)

    suspend fun checkUsernameAvailability(cleanUser: String, candidateEmail: String): Pair<Boolean, String?> =
        CloudAuthTransport.checkUsernameAvailability(cleanUser, candidateEmail)

    suspend fun recordUsernameReservation(cleanUser: String, userEmail: String, pwHash: String): Boolean =
        CloudAuthTransport.recordUsernameReservation(cleanUser, userEmail, pwHash)

    suspend fun updateSupabaseDisplayName(newProfileName: String, preferences: AppPreferences, tokenOverride: String? = null): Boolean =
        CloudAuthTransport.updateSupabaseDisplayName(newProfileName, preferences, tokenOverride)

    suspend fun refreshSession(preferences: AppPreferences): Boolean = CloudAuthTransport.refreshSession(preferences)

    suspend fun createAccount(
        username: String,
        password: String,
        preferences: AppPreferences,
        emailInput: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        val isEs = preferences.appLanguage == AppLanguage.SPANISH
        val cleanUser = username.trim().ifBlank { preferences.userProfileName.trim().ifBlank { "User" } }
        if (cleanUser.length < 3) {
            return@withContext Result.failure(
                IllegalArgumentException(
                    if (isEs) "El nombre de usuario / Gamer Tag debe tener al menos 3 caracteres."
                    else "Gamer Tag / Username must be at least 3 characters."
                )
            )
        }
        if (password.length < 6) {
            return@withContext Result.failure(
                IllegalArgumentException(
                    if (isEs) "La contraseña debe tener al menos 6 caracteres por seguridad."
                    else "Password must be at least 6 characters for cloud security."
                )
            )
        }

        val targetEmail = when {
            emailInput.isNotBlank() -> emailInput.trim()
            preferences.cloudAccountEmail.isNotBlank() -> preferences.cloudAccountEmail.trim()
            else -> "${deriveAccountId(cleanUser)}@lootdrop.app"
        }
        if (!targetEmail.contains("@") || !targetEmail.contains(".")) {
            return@withContext Result.failure(
                IllegalArgumentException(
                    if (isEs) "Por favor ingresa un correo electrónico válido."
                    else "Please enter a valid email address."
                )
            )
        }
        if (!isConfigured()) {
            return@withContext Result.failure(
                IllegalStateException(
                    if (isEs) "El servidor en la nube no está configurado."
                    else "Cloud endpoint is not configured. Please set your secrets in AI Studio."
                )
            )
        }

        val accountId = deriveAccountId(cleanUser)
        val pwHash = hashPassword(password, cleanUser)
        val (isAvailable, conflictReason) = checkUsernameAvailability(cleanUser, targetEmail)
        if (!isAvailable) {
            val err = conflictReason ?: if (isEs) {
                "El usuario '$cleanUser' ya está ocupado. ¡Elige otro nombre de usuario!"
            } else {
                "The username '$cleanUser' is already taken by another gamer. Nobody else can use your username! Please choose a different username."
            }
            preferences.lastCloudBackupStatus = "❌ $err"
            return@withContext Result.failure(IllegalStateException(err))
        }

        val supabaseUrl = resolveSupabaseUrl()
        val supabaseKey = resolveSupabaseAnonKey()
        var tokenAcquired = ""
        var refreshTokenAcquired = ""
        var pendingEmailConfirmation = false
        var lastAuthError = ""
        var userAlreadyExists = false

        if (supabaseUrl.isNotBlank() && supabaseKey.isNotBlank()) {
            val base = supabaseUrl.trimEnd('/')
            val authBody = JSONObject().apply {
                put("email", targetEmail)
                put("password", password)
                put("data", CloudAuthTransport.buildUserMetadata(cleanUser, accountId, targetEmail))
            }.toString()

            val authReq = Request.Builder()
                .url("$base/auth/v1/signup")
                .post(authBody.toRequestBody(CloudAuthTransport.JSON_MEDIA_TYPE))
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer $supabaseKey")
                .header("Content-Type", "application/json")
                .header("User-Agent", CloudAuthTransport.USER_AGENT)
                .build()

            runCatching {
                CloudAuthTransport.httpClient.newCall(authReq).execute().use { resp ->
                    val body = resp.body?.string() ?: ""
                    Log.d(TAG, "Supabase signup response code: ${resp.code}, body: ${body.take(150)}")
                    if (resp.isSuccessful) {
                        val json = JSONObject(body)
                        tokenAcquired = json.optString("access_token", "")
                        json.optString("refresh_token", "").takeIf { it.isNotBlank() }?.let { refreshTokenAcquired = it }
                        if (tokenAcquired.isBlank()) {
                            val userObj = if (json.has("user")) json.optJSONObject("user") else json
                            if (userObj?.optString("confirmed_at", "").isNullOrBlank()) pendingEmailConfirmation = true
                        }
                    } else {
                        val errJson = runCatching { JSONObject(body) }.getOrNull()
                        val code = errJson?.optString("error_code", "") ?: ""
                        val errMsg = errJson?.optString("msg", "") ?: ""
                        val desc = errJson?.optString("error_description", "") ?: ""
                        if (code == "user_already_exists" || errMsg.contains("User already registered", ignoreCase = true)) {
                            userAlreadyExists = true
                        } else {
                            lastAuthError = desc.ifBlank { errMsg }
                        }
                    }
                }
            }

            if (userAlreadyExists || (tokenAcquired.isBlank() && !pendingEmailConfirmation && lastAuthError.isBlank())) {
                val tokenBody = JSONObject().apply { put("email", targetEmail); put("password", password) }.toString()
                val tokenReq = Request.Builder()
                    .url("$base/auth/v1/token?grant_type=password")
                    .post(tokenBody.toRequestBody(CloudAuthTransport.JSON_MEDIA_TYPE))
                    .header("apikey", supabaseKey)
                    .header("Content-Type", "application/json")
                    .header("User-Agent", CloudAuthTransport.USER_AGENT)
                    .build()

                runCatching {
                    CloudAuthTransport.httpClient.newCall(tokenReq).execute().use { resp ->
                        val body = resp.body?.string() ?: ""
                        if (resp.isSuccessful) {
                            val json = JSONObject(body)
                            tokenAcquired = json.optString("access_token", "")
                            json.optString("refresh_token", "").takeIf { it.isNotBlank() }?.let { refreshTokenAcquired = it }
                            pendingEmailConfirmation = false
                        } else {
                            val errJson = runCatching { JSONObject(body) }.getOrNull()
                            val clean = (errJson?.optString("error_description", "") ?: "").ifBlank { errJson?.optString("msg", "") ?: "" }
                            lastAuthError = if (clean.contains("invalid login", ignoreCase = true) || clean.contains("invalid_grant", ignoreCase = true)) {
                                if (isEs) {
                                    "Ya existe una cuenta con '$targetEmail' y otra contraseña. Si es tuya, escribe tu contraseña o pulsa 'Iniciar Sesión'."
                                } else {
                                    "An account with email '$targetEmail' is already registered with a different password. If this is your account, please enter your correct password or tap 'Sign In'."
                                }
                            } else clean
                        }
                    }
                }
            }
        }

        if (pendingEmailConfirmation && tokenAcquired.isBlank()) {
            preferences.cloudAccountUsername = cleanUser
            preferences.userProfileName = cleanUser
            preferences.cloudAccountEmail = targetEmail
            preferences.cloudAccountId = accountId
            preferences.cloudAccountPasswordHash = pwHash
            val note = if (targetEmail.contains("@lootdrop.app")) {
                if (isEs) "Cuenta creada con el nombre '$cleanUser'. Inicia sesión con tus credenciales para activar la sincronización."
                else "Account created with display name '$cleanUser'. Please sign in with your credentials to activate cloud sync."
            } else {
                if (isEs) "Correo de verificación enviado a $targetEmail. Confirma tu correo para activar tu cuenta."
                else "Verification email sent to $targetEmail. Please verify your email to finish activating your account."
            }
            preferences.lastCloudBackupStatus = "✉️ $note"
            return@withContext Result.failure(IllegalStateException(note))
        }

        if (supabaseUrl.isNotBlank() && supabaseKey.isNotBlank() && tokenAcquired.isBlank()) {
            val err = lastAuthError.ifBlank {
                if (isEs) "No se pudo conectar la cuenta en la nube. Revisa tus credenciales."
                else "Could not connect cloud account. Please check your credentials."
            }
            preferences.lastCloudBackupStatus = "❌ $err"
            return@withContext Result.failure(IllegalStateException(err))
        }

        preferences.cloudAccountUsername = cleanUser
        preferences.userProfileName = cleanUser
        preferences.cloudAccountEmail = targetEmail
        preferences.lastCloudAccountEmail = targetEmail
        preferences.cloudAccountId = accountId
        preferences.cloudAccountPasswordHash = pwHash
        if (tokenAcquired.isNotBlank()) {
            preferences.cloudAccountToken = tokenAcquired
            updateSupabaseDisplayName(cleanUser, preferences, tokenOverride = tokenAcquired)
        }
        if (refreshTokenAcquired.isNotBlank()) preferences.cloudAccountRefreshToken = refreshTokenAcquired

        recordUsernameReservation(cleanUser, targetEmail, pwHash)
        val successMsg = if (isEs) "¡Cuenta '$cleanUser' conectada con tu perfil!" else "Account '$cleanUser' connected with display name matching your profile!"
        preferences.lastCloudBackupStatus = "✅ $successMsg"
        Result.success(cleanUser)
    }

    suspend fun signInAccount(
        usernameOrEmail: String,
        password: String,
        preferences: AppPreferences,
        emailInput: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        val isEs = preferences.appLanguage == AppLanguage.SPANISH
        val cleanIdentifier = usernameOrEmail.trim()
        if (cleanIdentifier.isBlank() || password.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException(if (isEs) "Se requieren usuario y contraseña." else "Username and password are required.")
            )
        }
        if (!isConfigured()) {
            return@withContext Result.failure(
                IllegalStateException(if (isEs) "El servidor en la nube no está configurado." else "Cloud endpoint is not configured. Please set your secrets in AI Studio.")
            )
        }

        val expectedAccountId = deriveAccountId(cleanIdentifier)
        val expectedPwHash = hashPassword(password, cleanIdentifier)
        val supabaseUrl = resolveSupabaseUrl()
        val supabaseKey = resolveSupabaseAnonKey()
        var tokenAcquired = ""
        var refreshTokenAcquired = ""
        var matchedEmail = ""
        var failureReason = ""

        if (supabaseUrl.isNotBlank() && supabaseKey.isNotBlank()) {
            val base = supabaseUrl.trimEnd('/')
            val tokenUrl = "$base/auth/v1/token?grant_type=password"
            val candidates = buildList {
                if (emailInput.isNotBlank()) add(emailInput.trim())
                if (cleanIdentifier.contains("@")) add(cleanIdentifier)
                if (preferences.cloudAccountEmail.isNotBlank()) add(preferences.cloudAccountEmail.trim())
                if (preferences.lastCloudAccountEmail.isNotBlank()) add(preferences.lastCloudAccountEmail.trim())
                add("$expectedAccountId@lootdrop.app")
            }.distinct()

            for (candidateEmail in candidates) {
                val authBody = JSONObject().apply { put("email", candidateEmail); put("password", password) }.toString()
                val authReq = Request.Builder()
                    .url(tokenUrl)
                    .post(authBody.toRequestBody(CloudAuthTransport.JSON_MEDIA_TYPE))
                    .header("apikey", supabaseKey)
                    .header("Content-Type", "application/json")
                    .build()

                runCatching {
                    CloudAuthTransport.httpClient.newCall(authReq).execute().use { resp ->
                        val body = resp.body?.string() ?: ""
                        if (resp.isSuccessful) {
                            val json = JSONObject(body)
                            tokenAcquired = json.optString("access_token", "")
                            refreshTokenAcquired = json.optString("refresh_token", "")
                            matchedEmail = candidateEmail
                        } else {
                            val errJson = runCatching { JSONObject(body) }.getOrNull()
                            val errCode = errJson?.optString("error_code", "") ?: ""
                            val cleanMsg = (errJson?.optString("error_description", "") ?: "").ifBlank { errJson?.optString("msg", "") ?: "" }
                            when {
                                errCode == "email_not_confirmed" || cleanMsg.contains("Email not confirmed", ignoreCase = true) ->
                                    failureReason = if (isEs) "Correo sin confirmar. Revisa tu bandeja de entrada." else "Email not confirmed. Please check your email inbox to confirm your account, or disable 'Confirm email' in Supabase Auth settings."
                                cleanMsg.contains("invalid login", ignoreCase = true) || cleanMsg.contains("invalid_grant", ignoreCase = true) ->
                                    if (failureReason.isBlank()) failureReason = if (isEs) "Credenciales inválidas. Verifica usuario, correo y contraseña." else "Invalid credentials. Please verify your username, email, and password."
                                failureReason.isBlank() -> failureReason = cleanMsg
                            }
                        }
                    }
                }
                if (tokenAcquired.isNotBlank()) break
            }

            if (tokenAcquired.isBlank() && !failureReason.contains("Email not confirmed", ignoreCase = true)) {
                val fallbackEmail = candidates.firstOrNull { it.isNotBlank() } ?: "$expectedAccountId@lootdrop.app"
                val signupBody = JSONObject().apply {
                    put("email", fallbackEmail)
                    put("password", password)
                    put("data", JSONObject().apply { put("gamer_tag", cleanIdentifier); put("account_id", expectedAccountId) })
                }.toString()
                val signupReq = Request.Builder()
                    .url("$base/auth/v1/signup")
                    .post(signupBody.toRequestBody(CloudAuthTransport.JSON_MEDIA_TYPE))
                    .header("apikey", supabaseKey)
                    .header("Authorization", "Bearer $supabaseKey")
                    .header("Content-Type", "application/json")
                    .build()

                runCatching {
                    CloudAuthTransport.httpClient.newCall(signupReq).execute().use { resp ->
                        val body = resp.body?.string() ?: ""
                        if (resp.isSuccessful) {
                            val json = JSONObject(body)
                            tokenAcquired = json.optString("access_token", "")
                            refreshTokenAcquired = json.optString("refresh_token", "")
                            matchedEmail = fallbackEmail
                            if (tokenAcquired.isBlank()) {
                                val retryBody = JSONObject().apply { put("email", fallbackEmail); put("password", password) }.toString()
                                val retryReq = Request.Builder()
                                    .url(tokenUrl)
                                    .post(retryBody.toRequestBody(CloudAuthTransport.JSON_MEDIA_TYPE))
                                    .header("apikey", supabaseKey)
                                    .header("Content-Type", "application/json")
                                    .build()
                                CloudAuthTransport.httpClient.newCall(retryReq).execute().use { retryResp ->
                                    if (retryResp.isSuccessful) {
                                        val retryJson = JSONObject(retryResp.body?.string() ?: "")
                                        tokenAcquired = retryJson.optString("access_token", "")
                                        refreshTokenAcquired = retryJson.optString("refresh_token", "")
                                    }
                                }
                            }
                        } else {
                            val errJson = runCatching { JSONObject(body) }.getOrNull()
                            val cleanMsg = (errJson?.optString("error_description", "") ?: "").ifBlank { errJson?.optString("msg", "") ?: "" }
                            if (cleanMsg.contains("already registered", ignoreCase = true)) {
                                failureReason = if (isEs) "Contraseña incorrecta para '$cleanIdentifier'. Inténtalo de nuevo." else "Incorrect password for '$cleanIdentifier'. Please check your password and try again."
                            } else if (cleanMsg.isNotBlank()) failureReason = cleanMsg
                        }
                    }
                }
            }
        }

        if (supabaseUrl.isNotBlank() && supabaseKey.isNotBlank() && tokenAcquired.isBlank()) {
            val err = failureReason.ifBlank {
                if (isEs) "No se pudo iniciar sesión en la nube. Revisa tus credenciales."
                else "Could not sign in to cloud account. Please check your username, email, and password."
            }
            preferences.lastCloudBackupStatus = "❌ $err"
            return@withContext Result.failure(IllegalStateException(err))
        }

        val finalEmail = matchedEmail.ifBlank {
            if (emailInput.isNotBlank()) emailInput.trim() else resolveAuthEmail(cleanIdentifier, preferences)
        }
        preferences.cloudAccountUsername = cleanIdentifier
        preferences.cloudAccountEmail = finalEmail
        preferences.lastCloudAccountEmail = finalEmail
        preferences.cloudAccountId = expectedAccountId
        preferences.cloudAccountPasswordHash = expectedPwHash
        if (tokenAcquired.isNotBlank()) {
            preferences.cloudAccountToken = tokenAcquired
            updateSupabaseDisplayName(cleanIdentifier, preferences, tokenOverride = tokenAcquired)
        }
        if (refreshTokenAcquired.isNotBlank()) preferences.cloudAccountRefreshToken = refreshTokenAcquired

        recordUsernameReservation(cleanIdentifier, finalEmail, expectedPwHash)
        val msg = if (isEs) "Sesión iniciada como '$cleanIdentifier'. Nube conectada." else "Signed in as '$cleanIdentifier'. Cloud Backup connected."
        preferences.lastCloudBackupStatus = "✅ $msg"
        Result.success(cleanIdentifier)
    }

    fun signOut(preferences: AppPreferences) {
        val isEs = preferences.appLanguage == AppLanguage.SPANISH
        preferences.clearCloudAccount()
        preferences.lastCloudBackupStatus = if (isEs) "Sesión cerrada en la cuenta en la nube." else "Signed out of Cloud Account."
    }

    suspend fun uploadBackupToCloud(
        context: Context,
        database: AppDatabase,
        preferences: AppPreferences
    ): Result<String> = withContext(Dispatchers.IO) {
        val isEs = preferences.appLanguage == AppLanguage.SPANISH
        if (!isConfigured()) {
            val err = if (isEs) "Respaldo en la nube sin configurar." else "Cloud Backup not configured. Set supabase_URL, supabase_ANON_KEY, or CLOUD_BACKUP_URL in AI Studio Secrets."
            preferences.lastCloudBackupStatus = "❌ $err"
            return@withContext Result.failure(IllegalStateException(err))
        }
        if (!preferences.isCloudAccountLoggedIn) {
            val err = if (isEs) "Cuenta requerida. Inicia sesión para conectar tu respaldo." else "Account required. Please sign in to connect your backup."
            preferences.lastCloudBackupStatus = "🔒 $err"
            return@withContext Result.failure(IllegalStateException(err))
        }

        val username = preferences.cloudAccountUsername.ifBlank { "User" }
        val accountId = preferences.cloudAccountId.ifBlank { deriveAccountId(username) }
        val pwHash = preferences.cloudAccountPasswordHash
        val email = preferences.cloudAccountEmail.ifBlank { resolveAuthEmail(username, preferences) }

        val rawJsonString = BackupRestoreManager.createBackupJson(
            allGiveaways = database.giveawayDao().getAllGiveawaysList(),
            steamGames = database.steamGameDao().getAllSteamGamesList(),
            steamUserId = preferences.steamUserId,
            userProfileName = preferences.userProfileName,
            userProfileAvatarBase64 = UserProfileManager.getAvatarBase64(context, preferences.userProfileAvatarPath),
            userProfileHasCustomTrophies = preferences.hasCustomTrophySelection,
            userProfileDisplayedTrophyKeys = preferences.displayedTrophyKeys
        )

        val backupRoot = JSONObject(rawJsonString).apply {
            put("cloudAccount", JSONObject().apply {
                put("accountId", accountId)
                put("username", username)
                put("email", email)
                put("authHash", pwHash.take(16))
                put("savedAt", System.currentTimeMillis())
            })
        }
        val jsonString = backupRoot.toString(2)
        val sizeKb = (jsonString.toByteArray(Charsets.UTF_8).size / 1024).coerceAtLeast(1)

        if (preferences.cloudAccountToken.isBlank() && preferences.cloudAccountRefreshToken.isNotBlank()) refreshSession(preferences)

        val cloudUrl = resolveCloudBackupUrl()
        val supabaseUrl = resolveSupabaseUrl()
        val supabaseKey = resolveSupabaseAnonKey()
        var token = preferences.cloudAccountToken
        var success = false
        var responseMessage = ""
        var detailedError = ""

        if (cloudUrl.isNotBlank()) {
            val req = CloudAuthTransport.cloudWorkerRequestBuilder(
                url = "$cloudUrl?accountId=$accountId&filename=lootdrop_backup_${accountId}.json",
                accountId = accountId,
                username = username,
                pwHash = pwHash,
                token = token,
                supabaseKey = supabaseKey
            ).post(jsonString.toRequestBody(CloudAuthTransport.JSON_MEDIA_TYPE))
                .header("Content-Type", "application/json")
                .header("X-Backup-File", "lootdrop_backup_${accountId}.json")
                .build()

            runCatching {
                CloudAuthTransport.executeWithTokenRefresh(req, preferences) { resp, respBody ->
                    if (resp.isSuccessful) {
                        success = true
                        responseMessage = "Saved to Cloudflare R2 for '$username' ($sizeKb KB)"
                    } else if (resp.code == 401) {
                        detailedError = "Worker 401: ${respBody.ifBlank { "Session expired. Please tap 'Sign In' to reconnect." }}"
                    } else {
                        detailedError = "Worker HTTP ${resp.code}: ${respBody.take(100)}"
                    }
                }
            }.onFailure { err ->
                Log.e(TAG, "Cloudflare Worker network error", err)
                detailedError = "Worker connection error: ${err.message}"
            }
            token = preferences.cloudAccountToken
        }

        if (!success && token.isNotBlank() && supabaseUrl.isNotBlank() && supabaseKey.isNotBlank()) {
            val updatePayload = JSONObject().apply {
                put("data", JSONObject().apply {
                    put("lootdrop_backup", backupRoot)
                    put("lootdrop_account_name", username)
                    put("lootdrop_saved_at", System.currentTimeMillis())
                })
            }.toString()
            val updateReq = Request.Builder()
                .url("${supabaseUrl.trimEnd('/')}/auth/v1/user")
                .put(updatePayload.toRequestBody(CloudAuthTransport.JSON_MEDIA_TYPE))
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
                .build()

            runCatching {
                CloudAuthTransport.executeWithTokenRefresh(updateReq, preferences) { resp, _ ->
                    if (resp.isSuccessful) {
                        success = true
                        responseMessage = "Saved to Cloud Account '$username' ($sizeKb KB)"
                    } else if (resp.code == 401) {
                        detailedError = "Cloud session expired. Please tap 'Sign In' with your password to reconnect."
                    }
                }
            }
            token = preferences.cloudAccountToken
        }

        if (!success && supabaseUrl.isNotBlank() && supabaseKey.isNotBlank()) {
            val authHeader = if (token.isNotBlank()) "Bearer $token" else "Bearer $supabaseKey"
            val storageReq = Request.Builder()
                .url("${supabaseUrl.trimEnd('/')}/storage/v1/object/lootdrop_backups/$accountId/backup.json")
                .post(jsonString.toRequestBody(CloudAuthTransport.JSON_MEDIA_TYPE))
                .header("apikey", supabaseKey)
                .header("Authorization", authHeader)
                .header("x-upsert", "true")
                .header("Content-Type", "application/json")
                .build()
            runCatching {
                CloudAuthTransport.httpClient.newCall(storageReq).execute().use { resp ->
                    if (resp.isSuccessful) {
                        success = true
                        responseMessage = "Saved to Cloud Storage for '$username' ($sizeKb KB)"
                    } else {
                        val errMsg = runCatching { JSONObject(resp.body?.string() ?: "").optString("message", "") }.getOrDefault("")
                        if (errMsg.isNotBlank()) detailedError = errMsg
                    }
                }
            }
        }

        if (!success && supabaseUrl.isNotBlank() && supabaseKey.isNotBlank()) {
            val recordPayload = JSONArray().apply {
                put(JSONObject().apply {
                    put("account_id", accountId)
                    put("username", username)
                    put("auth_hash", pwHash.take(16))
                    put("backup_json", jsonString)
                    put("updated_at", System.currentTimeMillis())
                })
            }.toString()
            val tableReq = Request.Builder()
                .url("${supabaseUrl.trimEnd('/')}/rest/v1/lootdrop_backups")
                .post(recordPayload.toRequestBody(CloudAuthTransport.JSON_MEDIA_TYPE))
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer $supabaseKey")
                .header("Prefer", "resolution=merge-duplicates")
                .header("Content-Type", "application/json")
                .build()
            runCatching {
                CloudAuthTransport.httpClient.newCall(tableReq).execute().use { resp ->
                    if (resp.isSuccessful) {
                        success = true
                        responseMessage = "Saved to Cloud Database for '$username' ($sizeKb KB)"
                    }
                }
            }
        }

        if (success) {
            val now = System.currentTimeMillis()
            preferences.lastCloudBackupTimestamp = now
            val timeFormatted = SimpleDateFormat("hh:mm a", preferences.appLanguage.locale).format(Date(now))
            val statusText = if (isEs) "✅ Guardado en la nube para '$username' ($sizeKb KB) a las $timeFormatted" else "✅ $responseMessage at $timeFormatted"
            preferences.lastCloudBackupStatus = statusText
            Result.success(statusText)
        } else {
            val cleanErr = when {
                detailedError.isNotBlank() -> detailedError
                token.isBlank() -> if (isEs) "Se requiere sesión activa. Inicia sesión de nuevo." else "Active cloud session required. Please tap 'Sign In' with your password to connect."
                else -> if (isEs) "El servidor rechazó el respaldo en la nube." else "Cloud backup rejected. Please verify your Supabase credentials or endpoint permissions."
            }
            preferences.lastCloudBackupStatus = if (isEs) "❌ Error al subir: $cleanErr" else "❌ Upload failed: $cleanErr"
            Result.failure(IllegalStateException(cleanErr))
        }
    }

    suspend fun restoreBackupFromCloud(
        context: Context,
        database: AppDatabase,
        preferences: AppPreferences
    ): Result<RestoreSummary> = withContext(Dispatchers.IO) {
        val isEs = preferences.appLanguage == AppLanguage.SPANISH
        if (!isConfigured()) {
            val err = if (isEs) "Respaldo en la nube sin configurar." else "Cloud Backup not configured. Set supabase_URL, supabase_ANON_KEY, or CLOUD_BACKUP_URL in AI Studio Secrets."
            preferences.lastCloudBackupStatus = "❌ $err"
            return@withContext Result.failure(IllegalStateException(err))
        }
        if (!preferences.isCloudAccountLoggedIn) {
            val err = if (isEs) "Inicia sesión en tu cuenta para encontrar tu respaldo." else "Please sign in to your cloud account to find your personal backup file."
            preferences.lastCloudBackupStatus = "🔒 $err"
            return@withContext Result.failure(IllegalStateException(err))
        }

        val username = preferences.cloudAccountUsername.ifBlank { "User" }
        val accountId = preferences.cloudAccountId.ifBlank { deriveAccountId(username) }
        val pwHash = preferences.cloudAccountPasswordHash
        if (preferences.cloudAccountToken.isBlank() && preferences.cloudAccountRefreshToken.isNotBlank()) refreshSession(preferences)

        val cloudUrl = resolveCloudBackupUrl()
        val supabaseUrl = resolveSupabaseUrl()
        val supabaseKey = resolveSupabaseAnonKey()
        var token = preferences.cloudAccountToken
        var backupJson: String? = null

        if (cloudUrl.isNotBlank()) {
            val req = CloudAuthTransport.cloudWorkerRequestBuilder(
                url = "$cloudUrl?accountId=$accountId&filename=lootdrop_backup_${accountId}.json",
                accountId = accountId,
                username = username,
                pwHash = pwHash,
                token = token,
                supabaseKey = supabaseKey
            ).get().build()
            runCatching {
                CloudAuthTransport.executeWithTokenRefresh(req, preferences) { resp, respBody ->
                    if (resp.isSuccessful) backupJson = respBody
                }
            }
            token = preferences.cloudAccountToken
        }

        if (backupJson.isNullOrBlank() && token.isNotBlank() && supabaseUrl.isNotBlank() && supabaseKey.isNotBlank()) {
            val req = Request.Builder()
                .url("${supabaseUrl.trimEnd('/')}/auth/v1/user")
                .get()
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer $token")
                .build()
            runCatching {
                CloudAuthTransport.executeWithTokenRefresh(req, preferences) { resp, respBody ->
                    if (resp.isSuccessful) {
                        val meta = runCatching { JSONObject(respBody).optJSONObject("user_metadata") }.getOrNull()
                        if (meta != null && meta.has("lootdrop_backup")) {
                            backupJson = meta.get("lootdrop_backup").toString()
                        }
                    }
                }
            }
            token = preferences.cloudAccountToken
        }

        if (backupJson.isNullOrBlank() && supabaseUrl.isNotBlank() && supabaseKey.isNotBlank()) {
            val authHeader = if (token.isNotBlank()) "Bearer $token" else "Bearer $supabaseKey"
            val storageReq = Request.Builder()
                .url("${supabaseUrl.trimEnd('/')}/storage/v1/object/lootdrop_backups/$accountId/backup.json")
                .get()
                .header("apikey", supabaseKey)
                .header("Authorization", authHeader)
                .build()
            runCatching {
                CloudAuthTransport.httpClient.newCall(storageReq).execute().use { resp ->
                    if (resp.isSuccessful) backupJson = resp.body?.string()
                }
            }
        }

        if (backupJson.isNullOrBlank() && supabaseUrl.isNotBlank() && supabaseKey.isNotBlank()) {
            val tableReq = Request.Builder()
                .url("${supabaseUrl.trimEnd('/')}/rest/v1/lootdrop_backups?account_id=eq.$accountId&select=backup_json")
                .get()
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer $supabaseKey")
                .build()
            runCatching {
                CloudAuthTransport.httpClient.newCall(tableReq).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val array = JSONArray(resp.body?.string() ?: "[]")
                        if (array.length() > 0) backupJson = array.getJSONObject(0).optString("backup_json", "")
                    }
                }
            }
        }

        val validJson = backupJson
        if (validJson.isNullOrBlank()) {
            val err = if (isEs) "No se encontró respaldo en la nube para '$username'. ¡Sube uno primero!" else "No cloud backup found for account '$username'. You may need to backup first!"
            preferences.lastCloudBackupStatus = "⚠️ $err"
            return@withContext Result.failure(IllegalStateException(err))
        }

        val summary = BackupRestoreManager.parseAndRestoreJson(
            jsonString = validJson,
            database = database,
            preferences = preferences
        )
        preferences.lastCloudBackupStatus = if (isEs) {
            "✅ ¡Se restauraron ${summary.claimedRestoredCount} juegos y perfil para '$username'!"
        } else {
            "✅ Restored ${summary.claimedRestoredCount} games & profile for account '$username'!"
        }
        Result.success(summary)
    }
}
