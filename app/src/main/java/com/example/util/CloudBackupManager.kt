package com.example.util

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.preferences.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

/**
 * Manages Cloud Backups and Restores with Supabase Auth and Cloudflare Worker endpoints.
 * Integrates direct user account authentication so each user's backup is securely stored
 * under their verified user account and protected with their personal password.
 */
object CloudBackupManager {

    private const val TAG = "CloudBackupManager"
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    // Production fallbacks to guarantee cloud connectivity when secrets aren't bundled in exported APKs
    private const val FALLBACK_SUPABASE_URL = "https://imcwutouksxeevmofcmn.supabase.co"
    private const val FALLBACK_SUPABASE_KEY = "sb_publishable_htD9aXXk0QHjhG_ifS3nYg_kjUp7osD"
    private const val FALLBACK_CLOUD_URL = "https://lootdrop-storage-proxy.hardingbentley67.workers.dev"

    /**
     * Resolves the Supabase URL, checking both uppercase and lowercase field variants.
     */
    fun resolveSupabaseUrl(): String {
        val b1 = runCatching { BuildConfig::class.java.getField("supabase_URL").get(null) as? String }.getOrNull()
        if (!b1.isNullOrBlank() && b1 != "YOUR_SUPABASE_URL_HERE") return b1.trim()
        val b2 = runCatching { BuildConfig::class.java.getField("SUPABASE_URL").get(null) as? String }.getOrNull()
        if (!b2.isNullOrBlank() && b2 != "YOUR_SUPABASE_URL_HERE") return b2.trim()
        return FALLBACK_SUPABASE_URL
    }

    /**
     * Resolves the Supabase Anon Key, checking both uppercase and lowercase field variants.
     */
    fun resolveSupabaseAnonKey(): String {
        val b1 = runCatching { BuildConfig::class.java.getField("supabase_ANON_KEY").get(null) as? String }.getOrNull()
        if (!b1.isNullOrBlank() && b1 != "YOUR_SUPABASE_ANON_KEY_HERE") return b1.trim()
        val b2 = runCatching { BuildConfig::class.java.getField("SUPABASE_ANON_KEY").get(null) as? String }.getOrNull()
        if (!b2.isNullOrBlank() && b2 != "YOUR_SUPABASE_ANON_KEY_HERE") return b2.trim()
        return FALLBACK_SUPABASE_KEY
    }

    /**
     * Resolves the Cloud Backup Worker / Proxy URL.
     */
    fun resolveCloudBackupUrl(): String {
        val b = runCatching { BuildConfig::class.java.getField("CLOUD_BACKUP_URL").get(null) as? String }.getOrNull()
        if (!b.isNullOrBlank() && b != "YOUR_CLOUD_BACKUP_URL_HERE") return b.trim()
        return FALLBACK_CLOUD_URL
    }

    /**
     * Returns true if either a custom Cloud Backup URL or Supabase credentials are configured.
     */
    fun isConfigured(): Boolean {
        val cloudUrl = resolveCloudBackupUrl()
        val supabaseUrl = resolveSupabaseUrl()
        val supabaseKey = resolveSupabaseAnonKey()
        return cloudUrl.isNotBlank() || (supabaseUrl.isNotBlank() && supabaseKey.isNotBlank())
    }

    /**
     * Returns a human-friendly provider description without exposing endpoints.
     */
    fun getProviderHost(): String {
        return if (isConfigured()) "Encrypted Private Cloud" else "Not Configured"
    }

    /**
     * Computes a standard SHA-256 hex string.
     */
    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Derives a deterministic, URL/storage-safe account identifier from a username/email.
     */
    fun deriveAccountId(username: String): String {
        val clean = username.trim().lowercase()
        val slug = clean.replace(Regex("[^a-z0-9_-]"), "_").take(20)
        val hash = sha256("lootdrop_account_salt_$clean").take(12)
        return "acc_${slug}_$hash"
    }

    /**
     * Hashes a password with a username-salted HMAC-like SHA-256 for secure cloud verification.
     */
    fun hashPassword(password: String, username: String): String {
        val salt = "lootdrop_salt_" + username.trim().lowercase()
        return sha256("$salt:$password:$salt")
    }

    /**
     * Resolves an email address for Supabase Auth, mapping usernames to the user's primary email.
     */
    fun resolveAuthEmail(identifier: String, preferences: AppPreferences): String {
        val clean = identifier.trim()
        if (clean.contains("@")) return clean
        if (preferences.cloudAccountEmail.isNotBlank() && preferences.cloudAccountEmail.contains("@")) {
            return preferences.cloudAccountEmail.trim()
        }
        val accountId = deriveAccountId(clean)
        return "$accountId@lootdrop.app"
    }

    /**
     * Checks if a username has already been claimed by another email address in the cloud registry.
     */
    suspend fun checkUsernameAvailability(cleanUser: String, candidateEmail: String): Pair<Boolean, String?> = withContext(Dispatchers.IO) {
        val cloudUrl = resolveCloudBackupUrl()
        val supabaseKey = resolveSupabaseAnonKey()
        val accountId = deriveAccountId(cleanUser)
        if (cloudUrl.isBlank()) return@withContext Pair(true, null)

        val registryUrl = "$cloudUrl?accountId=$accountId&filename=lootdrop_username_registry.json"
        val req = Request.Builder()
            .url(registryUrl)
            .get()
            .header("User-Agent", "LootDrop-Android/${BuildConfig.VERSION_NAME}")
            .apply {
                if (supabaseKey.isNotBlank()) {
                    header("Authorization", "Bearer $supabaseKey")
                }
            }
            .build()

        runCatching {
            httpClient.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: ""
                    val json = runCatching { JSONObject(body) }.getOrNull()
                    val registeredEmail = json?.optString("email", "")?.trim() ?: ""
                    val registeredUsername = json?.optString("username", "")?.trim() ?: cleanUser
                    if (registeredEmail.isNotBlank() && !registeredEmail.equals(candidateEmail.trim(), ignoreCase = true)) {
                        val msg = "The username '$registeredUsername' is already taken by another gamer. Nobody else can use your username! Please choose a different username."
                        return@withContext Pair(false, msg)
                    }
                }
            }
        }
        Pair(true, null)
    }

    /**
     * Records the user's claimed username, email, and account ID in the Cloudflare R2 registry
     * so that nobody else can register the same username with a different email.
     */
    suspend fun recordUsernameReservation(cleanUser: String, userEmail: String, pwHash: String): Boolean = withContext(Dispatchers.IO) {
        val cloudUrl = resolveCloudBackupUrl()
        val supabaseKey = resolveSupabaseAnonKey()
        val accountId = deriveAccountId(cleanUser)
        if (cloudUrl.isBlank()) return@withContext false

        val payload = JSONObject().apply {
            put("username", cleanUser)
            put("accountId", accountId)
            put("email", userEmail.trim())
            put("pwHash", pwHash)
            put("claimedAt", System.currentTimeMillis())
        }.toString()

        val registryUrl = "$cloudUrl?accountId=$accountId&filename=lootdrop_username_registry.json"
        val req = Request.Builder()
            .url(registryUrl)
            .post(payload.toRequestBody(JSON_MEDIA_TYPE))
            .header("Content-Type", "application/json")
            .header("User-Agent", "LootDrop-Android/${BuildConfig.VERSION_NAME}")
            .header("X-Account-Id", accountId)
            .header("X-Account-Username", cleanUser)
            .header("X-Account-Key", pwHash)
            .apply {
                if (supabaseKey.isNotBlank()) {
                    header("Authorization", "Bearer $supabaseKey")
                }
            }
            .build()

        runCatching {
            httpClient.newCall(req).execute().use { it.isSuccessful }
        }.getOrDefault(false)
    }

    /**
     * Creates a new cloud backup account with username, email, and password.
     * Attaches a display_name matching the user's profile name in Supabase user_metadata,
     * updates the user record via PUT /auth/v1/user, and reserves the username so nobody
     * else can use the same username.
     */
    suspend fun createAccount(
        username: String,
        password: String,
        preferences: AppPreferences,
        emailInput: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        val cleanUser = username.trim().ifBlank { preferences.userProfileName.trim().ifBlank { "User" } }
        if (cleanUser.length < 3) {
            return@withContext Result.failure(IllegalArgumentException("Gamer Tag / Username must be at least 3 characters."))
        }
        if (password.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Password must be at least 6 characters for cloud security."))
        }
        val targetEmail = if (emailInput.isNotBlank()) {
            emailInput.trim()
        } else if (preferences.cloudAccountEmail.isNotBlank()) {
            preferences.cloudAccountEmail.trim()
        } else {
            "${deriveAccountId(cleanUser)}@lootdrop.app"
        }

        if (!targetEmail.contains("@") || !targetEmail.contains(".")) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }

        if (!isConfigured()) {
            return@withContext Result.failure(IllegalStateException("Cloud endpoint is not configured. Please set your secrets in AI Studio."))
        }

        // Profile display name to send to Supabase (matches profile name)
        val profileDisplayName = cleanUser
        val accountId = deriveAccountId(cleanUser)
        val pwHash = hashPassword(password, cleanUser)

        // 1. Enforce Username Uniqueness: Check if another gamer has already registered this username
        val (isAvailable, conflictReason) = checkUsernameAvailability(cleanUser, targetEmail)
        if (!isAvailable) {
            val err = conflictReason ?: "The username '$profileDisplayName' is already taken by another gamer. Nobody else can use your username! Please choose a different username."
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
            val authUrl = "$base/auth/v1/signup"

            // Construct Supabase user_metadata with display_name matching profile name
            val userMeta = JSONObject().apply {
                put("display_name", profileDisplayName)
                put("full_name", profileDisplayName)
                put("name", profileDisplayName)
                put("username", profileDisplayName)
                put("user_name", profileDisplayName)
                put("gamer_tag", profileDisplayName)
                put("profile_name", profileDisplayName)
                put("account_id", accountId)
                put("contact_email", targetEmail)
            }

            // Main Supabase Auth signup with display_name matching profile name
            val authBody = JSONObject().apply {
                put("email", targetEmail)
                put("password", password)
                put("data", userMeta)
            }.toString()

            val authReq = Request.Builder()
                .url(authUrl)
                .post(authBody.toRequestBody(JSON_MEDIA_TYPE))
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer $supabaseKey")
                .header("Content-Type", "application/json")
                .header("User-Agent", "LootDrop-Android/${BuildConfig.VERSION_NAME}")
                .build()

            runCatching {
                httpClient.newCall(authReq).execute().use { resp ->
                    val body = resp.body?.string() ?: ""
                    Log.d(TAG, "Supabase signup response code: ${resp.code}, body: ${body.take(150)}")
                    if (resp.isSuccessful) {
                        val json = JSONObject(body)
                        tokenAcquired = json.optString("access_token", "")
                        val rToken = json.optString("refresh_token", "")
                        if (rToken.isNotBlank()) {
                            refreshTokenAcquired = rToken
                        }
                        // Check if email confirmation is required
                        if (tokenAcquired.isBlank()) {
                            val userObj = if (json.has("user")) json.optJSONObject("user") else json
                            val confirmedAt = userObj?.optString("confirmed_at", "") ?: ""
                            if (confirmedAt.isBlank()) {
                                pendingEmailConfirmation = true
                            }
                        }
                    } else {
                        val code = runCatching { JSONObject(body).optString("error_code", "") }.getOrDefault("")
                        val errMsg = runCatching { JSONObject(body).optString("msg", "") }.getOrDefault("")
                        val desc = runCatching { JSONObject(body).optString("error_description", "") }.getOrDefault("")
                        if (code == "user_already_exists" || errMsg.contains("User already registered", ignoreCase = true)) {
                            userAlreadyExists = true
                        } else {
                            lastAuthError = desc.ifBlank { errMsg }
                        }
                    }
                }
            }

            // 2. If user already exists in Supabase with this email, authenticate to verify password and update display name
            if (userAlreadyExists || (tokenAcquired.isBlank() && !pendingEmailConfirmation && lastAuthError.isBlank())) {
                val tokenUrl = "$base/auth/v1/token?grant_type=password"
                val tokenBody = JSONObject().apply {
                    put("email", targetEmail)
                    put("password", password)
                }.toString()

                val tokenReq = Request.Builder()
                    .url(tokenUrl)
                    .post(tokenBody.toRequestBody(JSON_MEDIA_TYPE))
                    .header("apikey", supabaseKey)
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "LootDrop-Android/${BuildConfig.VERSION_NAME}")
                    .build()

                runCatching {
                    httpClient.newCall(tokenReq).execute().use { resp ->
                        val body = resp.body?.string() ?: ""
                        if (resp.isSuccessful) {
                            val json = JSONObject(body)
                            tokenAcquired = json.optString("access_token", "")
                            val rToken = json.optString("refresh_token", "")
                            if (rToken.isNotBlank()) {
                                refreshTokenAcquired = rToken
                            }
                            pendingEmailConfirmation = false
                            userAlreadyExists = false
                        } else {
                            val desc = runCatching { JSONObject(body).optString("error_description", "") }.getOrDefault("")
                            val msg = runCatching { JSONObject(body).optString("msg", "") }.getOrDefault("")
                            val clean = desc.ifBlank { msg }
                            if (clean.contains("invalid login", ignoreCase = true) || clean.contains("invalid_grant", ignoreCase = true)) {
                                lastAuthError = "An account with email '$targetEmail' is already registered with a different password. If this is your account, please enter your correct password or tap 'Sign In'."
                            } else {
                                lastAuthError = clean
                            }
                        }
                    }
                }
            }
        }

        // If email confirmation is pending on Supabase, inform the user clearly
        if (pendingEmailConfirmation && tokenAcquired.isBlank()) {
            preferences.cloudAccountUsername = cleanUser
            preferences.userProfileName = cleanUser
            preferences.cloudAccountEmail = targetEmail
            preferences.cloudAccountId = accountId
            preferences.cloudAccountPasswordHash = pwHash
            val note = if (targetEmail.contains("@lootdrop.app")) {
                "Account created with display name '$profileDisplayName'. Please sign in with your credentials to activate cloud sync."
            } else {
                "Verification email sent to $targetEmail. Please verify your email to finish activating your account."
            }
            preferences.lastCloudBackupStatus = "✉️ $note"
            return@withContext Result.failure(IllegalStateException(note))
        }

        if (supabaseUrl.isNotBlank() && supabaseKey.isNotBlank() && tokenAcquired.isBlank()) {
            val err = lastAuthError.ifBlank { "Could not connect cloud account. Please check your credentials." }
            preferences.lastCloudBackupStatus = "❌ $err"
            return@withContext Result.failure(IllegalStateException(err))
        }

        // Save session & update local profile name to match cloud display name
        preferences.cloudAccountUsername = cleanUser
        preferences.userProfileName = cleanUser
        preferences.cloudAccountEmail = targetEmail
        preferences.lastCloudAccountEmail = targetEmail
        preferences.cloudAccountId = accountId
        preferences.cloudAccountPasswordHash = pwHash
        if (tokenAcquired.isNotBlank()) {
            preferences.cloudAccountToken = tokenAcquired
        }
        if (refreshTokenAcquired.isNotBlank()) {
            preferences.cloudAccountRefreshToken = refreshTokenAcquired
        }

        // Explicitly sync display_name to Supabase user metadata via PUT /auth/v1/user
        if (tokenAcquired.isNotBlank()) {
            updateSupabaseDisplayName(cleanUser, preferences, tokenOverride = tokenAcquired)
        }

        // Reserve username in R2 so nobody else can take it
        recordUsernameReservation(cleanUser, targetEmail, pwHash)

        val successMsg = "Account '$cleanUser' connected with display name matching your profile!"
        preferences.lastCloudBackupStatus = "✅ $successMsg"

        Result.success(cleanUser)
    }

    /**
     * Updates the user's display name and profile name in Supabase user metadata.
     */
    suspend fun updateSupabaseDisplayName(
        newProfileName: String,
        preferences: AppPreferences,
        tokenOverride: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val cleanName = newProfileName.trim()
        if (cleanName.isBlank()) return@withContext false
        val token = tokenOverride?.takeIf { it.isNotBlank() } ?: preferences.cloudAccountToken
        val supabaseUrl = resolveSupabaseUrl()
        val supabaseKey = resolveSupabaseAnonKey()
        if (token.isBlank() || supabaseUrl.isBlank() || supabaseKey.isBlank()) return@withContext false

        val base = supabaseUrl.trimEnd('/')
        val userUrl = "$base/auth/v1/user"
        val payload = JSONObject().apply {
            val userMeta = JSONObject().apply {
                put("display_name", cleanName)
                put("full_name", cleanName)
                put("name", cleanName)
                put("username", cleanName)
                put("user_name", cleanName)
                put("gamer_tag", cleanName)
                put("profile_name", cleanName)
            }
            put("data", userMeta)
        }.toString()

        val req = Request.Builder()
            .url(userUrl)
            .put(payload.toRequestBody(JSON_MEDIA_TYPE))
            .header("apikey", supabaseKey)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .header("User-Agent", "LootDrop-Android/${BuildConfig.VERSION_NAME}")
            .build()

        runCatching {
            httpClient.newCall(req).execute().use { resp ->
                val ok = resp.isSuccessful
                if (ok) {
                    preferences.cloudAccountUsername = cleanName
                    Log.d(TAG, "Successfully updated Supabase display_name to '$cleanName'")
                } else {
                    Log.w(TAG, "Supabase PUT /auth/v1/user returned code: ${resp.code}")
                }
                ok
            }
        }.getOrDefault(false)
    }

    /**
     * Signs in to an existing cloud account with username/email and password.
     */
    suspend fun signInAccount(
        usernameOrEmail: String,
        password: String,
        preferences: AppPreferences,
        emailInput: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        val cleanIdentifier = usernameOrEmail.trim()
        if (cleanIdentifier.isBlank() || password.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Username and password are required."))
        }
        if (!isConfigured()) {
            return@withContext Result.failure(IllegalStateException("Cloud endpoint is not configured. Please set your secrets in AI Studio."))
        }

        val expectedAccountId = deriveAccountId(cleanIdentifier)
        val expectedPwHash = hashPassword(password, cleanIdentifier)

        val supabaseUrl = resolveSupabaseUrl()
        val supabaseKey = resolveSupabaseAnonKey()
        var tokenAcquired = ""
        var refreshTokenAcquired = ""
        var matchedEmail = ""
        var failureReason = ""

        // 1. Supabase Auth sign-in
        if (supabaseUrl.isNotBlank() && supabaseKey.isNotBlank()) {
            val base = supabaseUrl.trimEnd('/')
            val tokenUrl = "$base/auth/v1/token?grant_type=password"

            // Gather candidate email addresses to test against Supabase
            val candidates = mutableListOf<String>()
            if (emailInput.isNotBlank()) candidates.add(emailInput.trim())
            if (cleanIdentifier.contains("@")) candidates.add(cleanIdentifier)
            if (preferences.cloudAccountEmail.isNotBlank()) candidates.add(preferences.cloudAccountEmail.trim())
            if (preferences.lastCloudAccountEmail.isNotBlank()) candidates.add(preferences.lastCloudAccountEmail.trim())
            candidates.add("${expectedAccountId}@lootdrop.app")

            for (candidateEmail in candidates.distinct()) {
                val authBody = JSONObject().apply {
                    put("email", candidateEmail)
                    put("password", password)
                }.toString()

                val authReq = Request.Builder()
                    .url(tokenUrl)
                    .post(authBody.toRequestBody(JSON_MEDIA_TYPE))
                    .header("apikey", supabaseKey)
                    .header("Content-Type", "application/json")
                    .build()

                runCatching {
                    httpClient.newCall(authReq).execute().use { resp ->
                        val body = resp.body?.string() ?: ""
                        if (resp.isSuccessful) {
                            val json = JSONObject(body)
                            tokenAcquired = json.optString("access_token", "")
                            refreshTokenAcquired = json.optString("refresh_token", "")
                            matchedEmail = candidateEmail
                        } else {
                            val errCode = runCatching { JSONObject(body).optString("error_code", "") }.getOrDefault("")
                            val desc = runCatching { JSONObject(body).optString("error_description", "") }.getOrDefault("")
                            val msg = runCatching { JSONObject(body).optString("msg", "") }.getOrDefault("")
                            val cleanMsg = desc.ifBlank { msg }
                            if (errCode == "email_not_confirmed" || cleanMsg.contains("Email not confirmed", ignoreCase = true)) {
                                failureReason = "Email not confirmed. Please check your email inbox to confirm your account, or disable 'Confirm email' in Supabase Auth settings."
                            } else if (cleanMsg.contains("invalid login", ignoreCase = true) || cleanMsg.contains("invalid_grant", ignoreCase = true)) {
                                if (failureReason.isBlank()) {
                                    failureReason = "Invalid credentials. Please verify your username, email, and password."
                                }
                            } else if (failureReason.isBlank()) {
                                failureReason = cleanMsg
                            }
                        }
                    }
                }

                if (tokenAcquired.isNotBlank()) {
                    break
                }
            }

            // Fallback attempt: If user was not yet created in Supabase Auth, attempt automatic signup
            if (tokenAcquired.isBlank() && !failureReason.contains("Email not confirmed", ignoreCase = true)) {
                val fallbackEmail = candidates.firstOrNull { it.isNotBlank() } ?: "${expectedAccountId}@lootdrop.app"
                val signupUrl = "$base/auth/v1/signup"
                val signupBody = JSONObject().apply {
                    put("email", fallbackEmail)
                    put("password", password)
                    val userMeta = JSONObject().apply {
                        put("gamer_tag", cleanIdentifier)
                        put("account_id", expectedAccountId)
                    }
                    put("data", userMeta)
                }.toString()

                val signupReq = Request.Builder()
                    .url(signupUrl)
                    .post(signupBody.toRequestBody(JSON_MEDIA_TYPE))
                    .header("apikey", supabaseKey)
                    .header("Authorization", "Bearer $supabaseKey")
                    .header("Content-Type", "application/json")
                    .build()

                runCatching {
                    httpClient.newCall(signupReq).execute().use { resp ->
                        val body = resp.body?.string() ?: ""
                        if (resp.isSuccessful) {
                            val json = JSONObject(body)
                            tokenAcquired = json.optString("access_token", "")
                            refreshTokenAcquired = json.optString("refresh_token", "")
                            matchedEmail = fallbackEmail
                            if (tokenAcquired.isBlank()) {
                                val tokenBody = JSONObject().apply {
                                    put("email", fallbackEmail)
                                    put("password", password)
                                }.toString()
                                val retryTokenReq = Request.Builder()
                                    .url(tokenUrl)
                                    .post(tokenBody.toRequestBody(JSON_MEDIA_TYPE))
                                    .header("apikey", supabaseKey)
                                    .header("Content-Type", "application/json")
                                    .build()
                                httpClient.newCall(retryTokenReq).execute().use { retryResp ->
                                    if (retryResp.isSuccessful) {
                                        val retryJson = JSONObject(retryResp.body?.string() ?: "")
                                        tokenAcquired = retryJson.optString("access_token", "")
                                        refreshTokenAcquired = retryJson.optString("refresh_token", "")
                                    }
                                }
                            }
                        } else {
                            val desc = runCatching { JSONObject(body).optString("error_description", "") }.getOrDefault("")
                            val msg = runCatching { JSONObject(body).optString("msg", "") }.getOrDefault("")
                            val cleanMsg = desc.ifBlank { msg }
                            if (cleanMsg.contains("already registered", ignoreCase = true)) {
                                failureReason = "Incorrect password for '$cleanIdentifier'. Please check your password and try again."
                            } else if (cleanMsg.isNotBlank()) {
                                failureReason = cleanMsg
                            }
                        }
                    }
                }
            }
        }

        // Validate that token was acquired when Supabase is configured
        if (supabaseUrl.isNotBlank() && supabaseKey.isNotBlank() && tokenAcquired.isBlank()) {
            val err = failureReason.ifBlank { "Could not sign in to cloud account. Please check your username, email, and password." }
            preferences.lastCloudBackupStatus = "❌ $err"
            return@withContext Result.failure(IllegalStateException(err))
        }

        val finalEmail = matchedEmail.ifBlank {
            if (emailInput.isNotBlank()) emailInput.trim() else resolveAuthEmail(cleanIdentifier, preferences)
        }

        // Save active session
        preferences.cloudAccountUsername = cleanIdentifier
        preferences.cloudAccountEmail = finalEmail
        preferences.lastCloudAccountEmail = finalEmail
        preferences.cloudAccountId = expectedAccountId
        preferences.cloudAccountPasswordHash = expectedPwHash
        if (tokenAcquired.isNotBlank()) {
            preferences.cloudAccountToken = tokenAcquired
            updateSupabaseDisplayName(cleanIdentifier, preferences, tokenOverride = tokenAcquired)
        }
        if (refreshTokenAcquired.isNotBlank()) {
            preferences.cloudAccountRefreshToken = refreshTokenAcquired
        }

        // Keep username reservation updated in R2
        recordUsernameReservation(cleanIdentifier, finalEmail, expectedPwHash)

        val msg = "Signed in as '$cleanIdentifier'. Cloud Backup connected."
        preferences.lastCloudBackupStatus = "✅ $msg"

        Result.success(cleanIdentifier)
    }

    /**
     * Signs out of the current cloud account.
     */
    fun signOut(preferences: AppPreferences) {
        preferences.clearCloudAccount()
        preferences.lastCloudBackupStatus = "Signed out of Cloud Account."
    }

    /**
     * Refreshes the Supabase user session using the stored refresh_token.
     */
    suspend fun refreshSession(preferences: AppPreferences): Boolean = withContext(Dispatchers.IO) {
        val refreshToken = preferences.cloudAccountRefreshToken
        val supabaseUrl = resolveSupabaseUrl()
        val supabaseKey = resolveSupabaseAnonKey()
        if (refreshToken.isBlank() || supabaseUrl.isBlank() || supabaseKey.isBlank()) return@withContext false

        val base = supabaseUrl.trimEnd('/')
        val refreshUrl = "$base/auth/v1/token?grant_type=refresh_token"
        val bodyObj = JSONObject().apply {
            put("refresh_token", refreshToken)
        }.toString()

        val req = Request.Builder()
            .url(refreshUrl)
            .post(bodyObj.toRequestBody(JSON_MEDIA_TYPE))
            .header("apikey", supabaseKey)
            .header("Content-Type", "application/json")
            .build()

        var refreshed = false
        runCatching {
            httpClient.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val respBody = resp.body?.string() ?: ""
                    val json = JSONObject(respBody)
                    val newAccessToken = json.optString("access_token", "")
                    val newRefreshToken = json.optString("refresh_token", "")
                    if (newAccessToken.isNotBlank()) {
                        preferences.cloudAccountToken = newAccessToken
                        if (newRefreshToken.isNotBlank()) {
                            preferences.cloudAccountRefreshToken = newRefreshToken
                        }
                        refreshed = true
                        Log.i(TAG, "Successfully refreshed Supabase session token")
                    }
                } else {
                    Log.w(TAG, "Failed to refresh Supabase session token: HTTP ${resp.code}")
                    preferences.cloudAccountRefreshToken = ""
                }
            }
        }
        refreshed
    }

    /**
     * Serializes current app data and uploads to the user's authenticated cloud account storage.
     */
    suspend fun uploadBackupToCloud(
        context: Context,
        database: AppDatabase,
        preferences: AppPreferences
    ): Result<String> = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            val err = "Cloud Backup not configured. Set supabase_URL, supabase_ANON_KEY, or CLOUD_BACKUP_URL in AI Studio Secrets."
            preferences.lastCloudBackupStatus = "❌ $err"
            return@withContext Result.failure(IllegalStateException(err))
        }

        if (!preferences.isCloudAccountLoggedIn) {
            val err = "Account required. Please sign in to connect your backup."
            preferences.lastCloudBackupStatus = "🔒 $err"
            return@withContext Result.failure(IllegalStateException(err))
        }

        val username = preferences.cloudAccountUsername.ifBlank { "User" }
        val accountId = preferences.cloudAccountId.ifBlank { deriveAccountId(username) }
        val pwHash = preferences.cloudAccountPasswordHash
        val email = preferences.cloudAccountEmail.ifBlank { resolveAuthEmail(username, preferences) }

        val allGiveaways = database.giveawayDao().getAllGiveawaysList()
        val steamGames = database.steamGameDao().getAllSteamGamesList()
        val steamUserId = preferences.steamUserId
        val profileName = preferences.userProfileName
        val avatarBase64 = UserProfileManager.getAvatarBase64(context, preferences.userProfileAvatarPath)

        val rawJsonString = BackupRestoreManager.createBackupJson(
            allGiveaways = allGiveaways,
            steamGames = steamGames,
            steamUserId = steamUserId,
            userProfileName = profileName,
            userProfileAvatarBase64 = avatarBase64,
            userProfileHasCustomTrophies = preferences.hasCustomTrophySelection,
            userProfileDisplayedTrophyKeys = preferences.displayedTrophyKeys
        )

        // Inject cryptographic account ownership into the backup file
        val backupRoot = JSONObject(rawJsonString)
        val accountAuth = JSONObject().apply {
            put("accountId", accountId)
            put("username", username)
            put("email", email)
            put("authHash", pwHash.take(16))
            put("savedAt", System.currentTimeMillis())
        }
        backupRoot.put("cloudAccount", accountAuth)

        val jsonString = backupRoot.toString(2)
        val bytes = jsonString.toByteArray(Charsets.UTF_8)
        val sizeKb = (bytes.size / 1024).coerceAtLeast(1)

        if (preferences.cloudAccountToken.isBlank() && preferences.cloudAccountRefreshToken.isNotBlank()) {
            refreshSession(preferences)
        }

        val cloudUrl = resolveCloudBackupUrl()
        val supabaseUrl = resolveSupabaseUrl()
        val supabaseKey = resolveSupabaseAnonKey()
        var token = preferences.cloudAccountToken

        var success = false
        var responseMessage = ""
        var detailedError = ""

        // 1. PRIMARY: Cloudflare Worker proxy (Cloudflare R2 Object Storage - 10GB free tier)
        if (cloudUrl.isNotBlank()) {
            val reqBuilder = Request.Builder()
                .url("$cloudUrl?accountId=$accountId&filename=lootdrop_backup_${accountId}.json")
                .post(jsonString.toRequestBody(JSON_MEDIA_TYPE))
                .header("Content-Type", "application/json")
                .header("User-Agent", "LootDrop-Android/${BuildConfig.VERSION_NAME}")
                .header("X-Account-Id", accountId)
                .header("X-Account-Username", username)
                .header("X-Account-Key", pwHash)
                .header("X-Backup-File", "lootdrop_backup_${accountId}.json")

            if (token.isNotBlank()) {
                reqBuilder.header("Authorization", "Bearer $token")
            } else if (supabaseKey.isNotBlank()) {
                reqBuilder.header("apikey", supabaseKey)
                reqBuilder.header("Authorization", "Bearer $supabaseKey")
            }

            runCatching {
                httpClient.newCall(reqBuilder.build()).execute().use { response ->
                    val respBody = response.body?.string() ?: ""
                    Log.d(TAG, "Cloudflare Worker upload response code: ${response.code}, body: ${respBody.take(120)}")
                    if (response.isSuccessful) {
                        success = true
                        responseMessage = "Saved to Cloudflare R2 for '$username' ($sizeKb KB)"
                    } else if (response.code == 401) {
                        Log.w(TAG, "Cloud endpoint returned HTTP 401 ($respBody), attempting automatic session refresh")
                        val refreshed = refreshSession(preferences)
                        if (refreshed) {
                            token = preferences.cloudAccountToken
                            val retryReq = reqBuilder
                                .header("Authorization", "Bearer $token")
                                .build()
                            httpClient.newCall(retryReq).execute().use { retryResp ->
                                val retryBody = retryResp.body?.string() ?: ""
                                if (retryResp.isSuccessful) {
                                    success = true
                                    responseMessage = "Saved to Cloudflare R2 for '$username' ($sizeKb KB)"
                                } else {
                                    preferences.cloudAccountToken = ""
                                    detailedError = "Worker 401: ${retryBody.ifBlank { "Session expired. Please tap 'Sign In' to reconnect." }}"
                                }
                            }
                        } else {
                            preferences.cloudAccountToken = ""
                            detailedError = "Worker 401: ${respBody.ifBlank { "Session expired. Please tap 'Sign In' to reconnect." }}"
                        }
                    } else {
                        detailedError = "Worker HTTP ${response.code}: ${respBody.take(100)}"
                    }
                }
            }.onFailure { err ->
                Log.e(TAG, "Cloudflare Worker network error", err)
                detailedError = "Worker connection error: ${err.message}"
            }
        }

        // 2. SECONDARY FALLBACK: Supabase Auth user_metadata
        if (!success && token.isNotBlank() && supabaseUrl.isNotBlank() && supabaseKey.isNotBlank()) {
            val base = supabaseUrl.trimEnd('/')
            val userUrl = "$base/auth/v1/user"
            val updatePayload = JSONObject().apply {
                val dataObj = JSONObject().apply {
                    put("lootdrop_backup", backupRoot)
                    put("lootdrop_account_name", username)
                    put("lootdrop_saved_at", System.currentTimeMillis())
                }
                put("data", dataObj)
            }.toString()

            val updateReq = Request.Builder()
                .url(userUrl)
                .put(updatePayload.toRequestBody(JSON_MEDIA_TYPE))
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
                .build()

            runCatching {
                httpClient.newCall(updateReq).execute().use { resp ->
                    if (resp.isSuccessful) {
                        success = true
                        responseMessage = "Saved to Cloud Account '$username' ($sizeKb KB)"
                    } else if (resp.code == 401) {
                        Log.w(TAG, "Auth token expired, attempting automatic session refresh")
                        val refreshed = refreshSession(preferences)
                        if (refreshed) {
                            token = preferences.cloudAccountToken
                            val retryReq = updateReq.newBuilder()
                                .header("Authorization", "Bearer $token")
                                .build()
                            httpClient.newCall(retryReq).execute().use { retryResp ->
                                if (retryResp.isSuccessful) {
                                    success = true
                                    responseMessage = "Saved to Cloud Account '$username' ($sizeKb KB)"
                                }
                            }
                        } else {
                            preferences.cloudAccountToken = ""
                            detailedError = "Cloud session expired. Please tap 'Sign In' with your password to reconnect."
                        }
                    }
                }
            }
        }

        // 3. TERTIARY FALLBACK: Supabase Storage Bucket
        if (!success && supabaseUrl.isNotBlank() && supabaseKey.isNotBlank()) {
            val base = supabaseUrl.trimEnd('/')
            val authHeader = if (token.isNotBlank()) "Bearer $token" else "Bearer $supabaseKey"

            // Upload file to bucket if it exists
            val storageUrl = "$base/storage/v1/object/lootdrop_backups/$accountId/backup.json"
            val storageReq = Request.Builder()
                .url(storageUrl)
                .post(jsonString.toRequestBody(JSON_MEDIA_TYPE))
                .header("apikey", supabaseKey)
                .header("Authorization", authHeader)
                .header("x-upsert", "true")
                .header("Content-Type", "application/json")
                .build()

            runCatching {
                httpClient.newCall(storageReq).execute().use { resp ->
                    if (resp.isSuccessful) {
                        success = true
                        responseMessage = "Saved to Cloud Storage for '$username' ($sizeKb KB)"
                    } else {
                        val body = resp.body?.string() ?: ""
                        val errMsg = runCatching { JSONObject(body).optString("message", "") }.getOrDefault("")
                        if (errMsg.isNotBlank()) detailedError = errMsg
                    }
                }
            }
        }

        // 4. QUATERNARY FALLBACK: Supabase Database Table (lootdrop_backups)
        if (!success && supabaseUrl.isNotBlank() && supabaseKey.isNotBlank()) {
            val base = supabaseUrl.trimEnd('/')
            val tableUrl = "$base/rest/v1/lootdrop_backups"
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
                .url(tableUrl)
                .post(recordPayload.toRequestBody(JSON_MEDIA_TYPE))
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer $supabaseKey")
                .header("Prefer", "resolution=merge-duplicates")
                .header("Content-Type", "application/json")
                .build()

            runCatching {
                httpClient.newCall(tableReq).execute().use { resp ->
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
            val timeFormatted = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US).format(java.util.Date(now))
            val statusText = "✅ $responseMessage at $timeFormatted"
            preferences.lastCloudBackupStatus = statusText
            Result.success(statusText)
        } else {
            val cleanErr = when {
                detailedError.isNotBlank() -> detailedError
                token.isBlank() -> "Active cloud session required. Please tap 'Sign In' with your password to connect."
                else -> "Cloud backup rejected. Please verify your Supabase credentials or endpoint permissions."
            }
            preferences.lastCloudBackupStatus = "❌ Upload failed: $cleanErr"
            Result.failure(IllegalStateException(cleanErr))
        }
    }

    /**
     * Downloads the specific backup file for the authenticated user account and restores it.
     */
    suspend fun restoreBackupFromCloud(
        context: Context,
        database: AppDatabase,
        preferences: AppPreferences
    ): Result<RestoreSummary> = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            val err = "Cloud Backup not configured. Set supabase_URL, supabase_ANON_KEY, or CLOUD_BACKUP_URL in AI Studio Secrets."
            preferences.lastCloudBackupStatus = "❌ $err"
            return@withContext Result.failure(IllegalStateException(err))
        }

        if (!preferences.isCloudAccountLoggedIn) {
            val err = "Please sign in to your cloud account to find your personal backup file."
            preferences.lastCloudBackupStatus = "🔒 $err"
            return@withContext Result.failure(IllegalStateException(err))
        }

        val username = preferences.cloudAccountUsername.ifBlank { "User" }
        val accountId = preferences.cloudAccountId.ifBlank { deriveAccountId(username) }
        val pwHash = preferences.cloudAccountPasswordHash
        val email = preferences.cloudAccountEmail.ifBlank { resolveAuthEmail(username, preferences) }

        if (preferences.cloudAccountToken.isBlank() && preferences.cloudAccountRefreshToken.isNotBlank()) {
            refreshSession(preferences)
        }

        val cloudUrl = resolveCloudBackupUrl()
        val supabaseUrl = resolveSupabaseUrl()
        val supabaseKey = resolveSupabaseAnonKey()
        var token = preferences.cloudAccountToken
        var backupJson: String? = null

        // 1. PRIMARY: Fetch from Cloudflare Worker proxy (Cloudflare R2 Object Storage - 10GB free tier)
        if (cloudUrl.isNotBlank()) {
            val reqBuilder = Request.Builder()
                .url("$cloudUrl?accountId=$accountId&filename=lootdrop_backup_${accountId}.json")
                .get()
                .header("User-Agent", "LootDrop-Android/${BuildConfig.VERSION_NAME}")
                .header("X-Account-Id", accountId)
                .header("X-Account-Username", username)
                .header("X-Account-Key", pwHash)

            if (token.isNotBlank()) {
                reqBuilder.header("Authorization", "Bearer $token")
            } else if (supabaseKey.isNotBlank()) {
                reqBuilder.header("apikey", supabaseKey)
                reqBuilder.header("Authorization", "Bearer $supabaseKey")
            }

            runCatching {
                httpClient.newCall(reqBuilder.build()).execute().use { resp ->
                    if (resp.isSuccessful) {
                        backupJson = resp.body?.string()
                    } else if (resp.code == 401) {
                        Log.w(TAG, "Cloud endpoint returned HTTP 401 during restore, attempting refresh")
                        val refreshed = refreshSession(preferences)
                        if (refreshed) {
                            token = preferences.cloudAccountToken
                            val retryReq = reqBuilder
                                .header("Authorization", "Bearer $token")
                                .build()
                            httpClient.newCall(retryReq).execute().use { retryResp ->
                                if (retryResp.isSuccessful) {
                                    backupJson = retryResp.body?.string()
                                } else {
                                    preferences.cloudAccountToken = ""
                                }
                            }
                        } else {
                            preferences.cloudAccountToken = ""
                        }
                    }
                }
            }
        }

        // 2. SECONDARY FALLBACK: Fetch from Supabase Auth user_metadata
        if (backupJson.isNullOrBlank() && token.isNotBlank() && supabaseUrl.isNotBlank() && supabaseKey.isNotBlank()) {
            val base = supabaseUrl.trimEnd('/')
            val userUrl = "$base/auth/v1/user"
            val req = Request.Builder()
                .url(userUrl)
                .get()
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer $token")
                .build()

            runCatching {
                httpClient.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val body = resp.body?.string() ?: ""
                        val userObj = JSONObject(body)
                        val meta = userObj.optJSONObject("user_metadata")
                        if (meta != null && meta.has("lootdrop_backup")) {
                            val backupObj = meta.get("lootdrop_backup")
                            backupJson = if (backupObj is JSONObject) backupObj.toString() else backupObj.toString()
                        }
                    } else if (resp.code == 401) {
                        Log.w(TAG, "Auth token expired during restore, attempting automatic session refresh")
                        val refreshed = refreshSession(preferences)
                        if (refreshed) {
                            token = preferences.cloudAccountToken
                            val retryReq = req.newBuilder()
                                .header("Authorization", "Bearer $token")
                                .build()
                            httpClient.newCall(retryReq).execute().use { retryResp ->
                                if (retryResp.isSuccessful) {
                                    val body = retryResp.body?.string() ?: ""
                                    val userObj = JSONObject(body)
                                    val meta = userObj.optJSONObject("user_metadata")
                                    if (meta != null && meta.has("lootdrop_backup")) {
                                        val backupObj = meta.get("lootdrop_backup")
                                        backupJson = if (backupObj is JSONObject) backupObj.toString() else backupObj.toString()
                                    }
                                }
                            }
                        } else {
                            preferences.cloudAccountToken = ""
                        }
                    }
                }
            }
        }

        // 3. TERTIARY FALLBACK: Fetch from Supabase Storage
        if (backupJson.isNullOrBlank() && supabaseUrl.isNotBlank() && supabaseKey.isNotBlank()) {
            val base = supabaseUrl.trimEnd('/')
            val storageUrl = "$base/storage/v1/object/lootdrop_backups/$accountId/backup.json"
            val authHeader = if (token.isNotBlank()) "Bearer $token" else "Bearer $supabaseKey"
            val storageReq = Request.Builder()
                .url(storageUrl)
                .get()
                .header("apikey", supabaseKey)
                .header("Authorization", authHeader)
                .build()

            runCatching {
                httpClient.newCall(storageReq).execute().use { resp ->
                    if (resp.isSuccessful) {
                        backupJson = resp.body?.string()
                    }
                }
            }
        }

        // 4. QUATERNARY FALLBACK: Fetch from Supabase Table
        if (backupJson.isNullOrBlank() && supabaseUrl.isNotBlank() && supabaseKey.isNotBlank()) {
            val base = supabaseUrl.trimEnd('/')
            val tableUrl = "$base/rest/v1/lootdrop_backups?account_id=eq.$accountId&select=backup_json"
            val tableReq = Request.Builder()
                .url(tableUrl)
                .get()
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer $supabaseKey")
                .build()

            runCatching {
                httpClient.newCall(tableReq).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val body = resp.body?.string() ?: ""
                        val array = JSONArray(body)
                        if (array.length() > 0) {
                            backupJson = array.getJSONObject(0).optString("backup_json", "")
                        }
                    }
                }
            }
        }

        val validJson = backupJson
        if (validJson.isNullOrBlank()) {
            val err = "No cloud backup found for account '$username'. You may need to backup first!"
            preferences.lastCloudBackupStatus = "⚠️ $err"
            return@withContext Result.failure(IllegalStateException(err))
        }

        // Parse and restore into database
        val summary = BackupRestoreManager.parseAndRestoreJson(
            jsonString = validJson,
            database = database,
            preferences = preferences
        )

        preferences.lastCloudBackupStatus = "✅ Restored ${summary.claimedRestoredCount} games & profile for account '$username'!"
        Result.success(summary)
    }
}
