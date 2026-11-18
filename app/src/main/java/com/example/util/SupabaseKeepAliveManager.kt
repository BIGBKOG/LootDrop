package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.preferences.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

data class KeepAliveResult(
    val endpoint: String,
    val httpCode: Int,
    val latencyMs: Long,
    val timestampMs: Long,
    val databaseReached: Boolean
)

/**
 * Executes lightweight PostgreSQL-backed PostgREST queries against Supabase so the
 * Free Tier project remains active even when LootDrop's primary storage/reviews
 * route through Cloudflare R2 or Supabase Auth.
 */
object SupabaseKeepAliveManager {
    private const val TAG = "SupabaseKeepAlive"
    const val DEFAULT_INTERVAL_MS: Long = 12L * 60L * 60L * 1000L // 12 hours

    private val CANDIDATE_ENDPOINTS = listOf(
        "lootdrop_backups" to "/rest/v1/lootdrop_backups?select=account_id&limit=1",
        "keep_alive" to "/rest/v1/keep_alive?select=id&limit=1",
        "postgrest_schema" to "/rest/v1/"
    )

    internal fun shouldPing(
        lastTimestampMs: Long,
        minIntervalMs: Long = DEFAULT_INTERVAL_MS,
        nowMs: Long = System.currentTimeMillis()
    ): Boolean {
        if (lastTimestampMs <= 0L) return true
        return (nowMs - lastTimestampMs) >= minIntervalMs
    }

    internal fun buildPostgrestPingRequest(
        baseUrl: String,
        path: String,
        anonKey: String,
        useExactCountHeader: Boolean = true
    ): Request {
        val cleanBase = baseUrl.trim().trimEnd('/')
        val cleanPath = if (path.startsWith("/")) path else "/$path"
        return Request.Builder()
            .url("$cleanBase$cleanPath")
            .get()
            .header("User-Agent", CloudAuthTransport.USER_AGENT)
            .header("apikey", anonKey)
            .header("Authorization", "Bearer $anonKey")
            .header("Accept", "application/json")
            .apply {
                if (useExactCountHeader) {
                    header("Prefer", "count=exact")
                }
            }
            .build()
    }

    internal fun isPostgrestSuccessCode(code: Int): Boolean = code in 200..299

    internal fun isPostgrestTableHitCode(code: Int): Boolean =
        code in 200..299 || code == 401 || code == 403

    suspend fun pingIfDue(
        context: Context,
        preferences: AppPreferences,
        minIntervalMs: Long = DEFAULT_INTERVAL_MS,
        httpClient: OkHttpClient = CloudAuthTransport.httpClient,
        baseUrl: String = CloudAuthTransport.resolveSupabaseUrl(),
        anonKey: String = CloudAuthTransport.resolveSupabaseAnonKey(),
        clock: () -> Long = System::currentTimeMillis
    ): Result<KeepAliveResult>? {
        val now = clock()
        if (!shouldPing(preferences.lastSupabaseKeepAliveTimestamp, minIntervalMs, now)) {
            return null
        }
        if (!hasActiveInternet(context)) {
            return null
        }
        return pingNow(
            preferences = preferences,
            httpClient = httpClient,
            baseUrl = baseUrl,
            anonKey = anonKey,
            clock = clock
        )
    }

    suspend fun pingNow(
        preferences: AppPreferences,
        httpClient: OkHttpClient = CloudAuthTransport.httpClient,
        baseUrl: String = CloudAuthTransport.resolveSupabaseUrl(),
        anonKey: String = CloudAuthTransport.resolveSupabaseAnonKey(),
        clock: () -> Long = System::currentTimeMillis
    ): Result<KeepAliveResult> = withContext(Dispatchers.IO) {
        val cleanBase = baseUrl.trim().trimEnd('/')
        val cleanKey = anonKey.trim()
        if (cleanBase.isBlank() || cleanKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Supabase URL or anon key is blank")
            )
        }

        val startMs = clock()
        var bestResult: KeepAliveResult? = null
        var lastException: Throwable? = null

        for ((label, path) in CANDIDATE_ENDPOINTS) {
            val useCountHeader = path != "/rest/v1/"
            val req = buildPostgrestPingRequest(cleanBase, path, cleanKey, useCountHeader)
            val attempt = runCatching {
                httpClient.newCall(req).execute().use { resp ->
                    resp.body?.close()
                    val code = resp.code
                    val elapsed = (clock() - startMs).coerceAtLeast(0L)
                    val isDirectSuccess = isPostgrestSuccessCode(code)
                    val result = KeepAliveResult(
                        endpoint = label,
                        httpCode = code,
                        latencyMs = elapsed,
                        timestampMs = clock(),
                        databaseReached = isDirectSuccess || (label != "postgrest_schema" && isPostgrestTableHitCode(code))
                    )
                    result
                }
            }

            attempt.onSuccess { res ->
                if (bestResult == null || isPostgrestSuccessCode(res.httpCode)) {
                    bestResult = res
                }
            }.onFailure { err ->
                lastException = err
            }

            val currentBest = bestResult
            if (currentBest != null && isPostgrestSuccessCode(currentBest.httpCode)) {
                break
            }
        }

        val finalResult = bestResult
        if (finalResult != null && finalResult.databaseReached && finalResult.httpCode < 500) {
            preferences.lastSupabaseKeepAliveTimestamp = finalResult.timestampMs
            preferences.lastSupabaseKeepAliveStatus =
                "HTTP ${finalResult.httpCode} • ${finalResult.endpoint} (${finalResult.latencyMs}ms)"
            Log.d(TAG, "Supabase keep-alive OK: ${preferences.lastSupabaseKeepAliveStatus}")
            Result.success(finalResult)
        } else if (finalResult != null) {
            val msg = "HTTP ${finalResult.httpCode} via ${finalResult.endpoint}"
            preferences.lastSupabaseKeepAliveStatus = "ERR $msg"
            Log.w(TAG, "Supabase keep-alive returned non-OK: $msg")
            Result.failure(IllegalStateException(msg))
        } else {
            val err = lastException ?: IllegalStateException("Supabase keep-alive request failed")
            Log.w(TAG, "Supabase keep-alive failed: ${err.message}")
            Result.failure(err)
        }
    }

    private fun hasActiveInternet(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return true
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
