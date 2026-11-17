package com.example.util

import android.util.Log
import com.example.BuildConfig
import com.example.preferences.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

internal object CloudAuthTransport {
    private const val TAG = "CloudBackupManager"
    internal val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    internal val USER_AGENT = "LootDrop-Android/${BuildConfig.VERSION_NAME}"

    private const val FALLBACK_SUPABASE_URL = "https://imcwutouksxeevmofcmn.supabase.co"
    private const val FALLBACK_SUPABASE_KEY = "sb_publishable_htD9aXXk0QHjhG_ifS3nYg_kjUp7osD"
    private const val FALLBACK_CLOUD_URL = "https://lootdrop-storage-proxy.hardingbentley67.workers.dev"

    internal val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    private fun resolveBuildConfigField(vararg names: String, placeholder: String, fallback: String): String {
        for (name in names) {
            val value = runCatching { BuildConfig::class.java.getField(name).get(null) as? String }.getOrNull()
            if (!value.isNullOrBlank() && value != placeholder) return value.trim()
        }
        return fallback
    }

    fun resolveSupabaseUrl(): String = resolveBuildConfigField(
        "supabase_URL", "SUPABASE_URL",
        placeholder = "YOUR_SUPABASE_URL_HERE",
        fallback = FALLBACK_SUPABASE_URL
    )

    fun resolveSupabaseAnonKey(): String = resolveBuildConfigField(
        "supabase_ANON_KEY", "SUPABASE_ANON_KEY",
        placeholder = "YOUR_SUPABASE_ANON_KEY_HERE",
        fallback = FALLBACK_SUPABASE_KEY
    )

    fun resolveCloudBackupUrl(): String = resolveBuildConfigField(
        "CLOUD_BACKUP_URL",
        placeholder = "YOUR_CLOUD_BACKUP_URL_HERE",
        fallback = FALLBACK_CLOUD_URL
    )

    fun sha256(input: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    fun deriveAccountId(username: String): String {
        val clean = username.trim().lowercase()
        val slug = clean.replace(Regex("[^a-z0-9_-]"), "_").take(20)
        val hash = sha256("lootdrop_account_salt_$clean").take(12)
        return "acc_${slug}_$hash"
    }

    fun hashPassword(password: String, username: String): String {
        val salt = "lootdrop_salt_" + username.trim().lowercase()
        return sha256("$salt:$password:$salt")
    }

    fun resolveAuthEmail(identifier: String, preferences: AppPreferences): String {
        val clean = identifier.trim()
        if (clean.contains("@")) return clean
        if (preferences.cloudAccountEmail.isNotBlank() && preferences.cloudAccountEmail.contains("@")) {
            return preferences.cloudAccountEmail.trim()
        }
        return "${deriveAccountId(clean)}@lootdrop.app"
    }

    fun buildUserMetadata(displayName: String, accountId: String? = null, contactEmail: String? = null): JSONObject =
        JSONObject().apply {
            put("display_name", displayName)
            put("full_name", displayName)
            put("name", displayName)
            put("username", displayName)
            put("user_name", displayName)
            put("gamer_tag", displayName)
            put("profile_name", displayName)
            if (accountId != null) put("account_id", accountId)
            if (contactEmail != null) put("contact_email", contactEmail)
        }

    fun cloudWorkerRequestBuilder(
        url: String,
        accountId: String,
        username: String,
        pwHash: String,
        token: String,
        supabaseKey: String
    ): Request.Builder = Request.Builder()
        .url(url)
        .header("User-Agent", USER_AGENT)
        .header("X-Account-Id", accountId)
        .header("X-Account-Username", username)
        .header("X-Account-Key", pwHash)
        .apply {
            if (token.isNotBlank()) {
                header("Authorization", "Bearer $token")
            } else if (supabaseKey.isNotBlank()) {
                header("apikey", supabaseKey)
                header("Authorization", "Bearer $supabaseKey")
            }
        }

    suspend fun executeWithTokenRefresh(
        request: Request,
        preferences: AppPreferences,
        onResponse: (Response, String) -> Unit
    ) {
        httpClient.newCall(request).execute().use { resp ->
            if (resp.code == 401) {
                val respBody = resp.body?.string() ?: ""
                Log.w(TAG, "Endpoint returned HTTP 401 ($respBody), attempting session refresh")
                if (refreshSession(preferences)) {
                    val newToken = preferences.cloudAccountToken
                    val retryReq = request.newBuilder().header("Authorization", "Bearer $newToken").build()
                    httpClient.newCall(retryReq).execute().use { retryResp ->
                        val retryBody = retryResp.body?.string() ?: ""
                        if (retryResp.code == 401) preferences.cloudAccountToken = ""
                        onResponse(retryResp, retryBody)
                    }
                } else {
                    preferences.cloudAccountToken = ""
                    onResponse(resp, respBody)
                }
            } else {
                onResponse(resp, resp.body?.string() ?: "")
            }
        }
    }

    suspend fun checkUsernameAvailability(cleanUser: String, candidateEmail: String): Pair<Boolean, String?> =
        withContext(Dispatchers.IO) {
            val cloudUrl = resolveCloudBackupUrl()
            val supabaseKey = resolveSupabaseAnonKey()
            val accountId = deriveAccountId(cleanUser)
            if (cloudUrl.isBlank()) return@withContext Pair(true, null)

            val req = Request.Builder()
                .url("$cloudUrl?accountId=$accountId&filename=lootdrop_username_registry.json")
                .get()
                .header("User-Agent", USER_AGENT)
                .apply { if (supabaseKey.isNotBlank()) header("Authorization", "Bearer $supabaseKey") }
                .build()

            runCatching {
                httpClient.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val json = runCatching { JSONObject(resp.body?.string() ?: "") }.getOrNull()
                        val registeredEmail = json?.optString("email", "")?.trim() ?: ""
                        val registeredUsername = json?.optString("username", "")?.trim() ?: cleanUser
                        if (registeredEmail.isNotBlank() && !registeredEmail.equals(candidateEmail.trim(), ignoreCase = true)) {
                            return@withContext Pair(
                                false,
                                "The username '$registeredUsername' is already taken by another gamer. Nobody else can use your username! Please choose a different username."
                            )
                        }
                    }
                }
            }
            Pair(true, null)
        }

    suspend fun recordUsernameReservation(cleanUser: String, userEmail: String, pwHash: String): Boolean =
        withContext(Dispatchers.IO) {
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

            val req = cloudWorkerRequestBuilder(
                url = "$cloudUrl?accountId=$accountId&filename=lootdrop_username_registry.json",
                accountId = accountId,
                username = cleanUser,
                pwHash = pwHash,
                token = "",
                supabaseKey = supabaseKey
            ).post(payload.toRequestBody(JSON_MEDIA_TYPE))
                .header("Content-Type", "application/json")
                .build()

            runCatching { httpClient.newCall(req).execute().use { it.isSuccessful } }.getOrDefault(false)
        }

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

        val payload = JSONObject().apply { put("data", buildUserMetadata(cleanName)) }.toString()
        val req = Request.Builder()
            .url("${supabaseUrl.trimEnd('/')}/auth/v1/user")
            .put(payload.toRequestBody(JSON_MEDIA_TYPE))
            .header("apikey", supabaseKey)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .header("User-Agent", USER_AGENT)
            .build()

        runCatching {
            httpClient.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    preferences.cloudAccountUsername = cleanName
                    Log.d(TAG, "Successfully updated Supabase display_name to '$cleanName'")
                } else {
                    Log.w(TAG, "Supabase PUT /auth/v1/user returned code: ${resp.code}")
                }
                resp.isSuccessful
            }
        }.getOrDefault(false)
    }

    suspend fun refreshSession(preferences: AppPreferences): Boolean = withContext(Dispatchers.IO) {
        val refreshToken = preferences.cloudAccountRefreshToken
        val supabaseUrl = resolveSupabaseUrl()
        val supabaseKey = resolveSupabaseAnonKey()
        if (refreshToken.isBlank() || supabaseUrl.isBlank() || supabaseKey.isBlank()) return@withContext false

        val bodyObj = JSONObject().apply { put("refresh_token", refreshToken) }.toString()
        val req = Request.Builder()
            .url("${supabaseUrl.trimEnd('/')}/auth/v1/token?grant_type=refresh_token")
            .post(bodyObj.toRequestBody(JSON_MEDIA_TYPE))
            .header("apikey", supabaseKey)
            .header("Content-Type", "application/json")
            .build()

        var refreshed = false
        runCatching {
            httpClient.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val json = JSONObject(resp.body?.string() ?: "")
                    val newAccessToken = json.optString("access_token", "")
                    val newRefreshToken = json.optString("refresh_token", "")
                    if (newAccessToken.isNotBlank()) {
                        preferences.cloudAccountToken = newAccessToken
                        if (newRefreshToken.isNotBlank()) preferences.cloudAccountRefreshToken = newRefreshToken
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
}
