package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.preferences.AppPreferences
import com.example.util.SupabaseKeepAliveManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SupabaseKeepAliveTest {

    private lateinit var context: Context
    private lateinit var preferences: AppPreferences

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("lootdrop_app_settings", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        preferences = AppPreferences(context)
    }

    private fun fakeClient(handler: (okhttp3.Request) -> Response): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain -> handler(chain.request()) })
            .build()
    }

    private fun jsonResponse(request: okhttp3.Request, code: Int, body: String = "[]"): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(if (code in 200..299) "OK" else "Error")
            .body(body.toResponseBody("application/json".toMediaType()))
            .build()
    }

    @Test
    fun buildPostgrestPingRequest_includesAuthAndExactCountHeaders() {
        val req = SupabaseKeepAliveManager.buildPostgrestPingRequest(
            baseUrl = "https://example.supabase.co/",
            path = "/rest/v1/lootdrop_backups?select=account_id&limit=1",
            anonKey = "test-anon-key",
            useExactCountHeader = true
        )
        assertEquals(
            "https://example.supabase.co/rest/v1/lootdrop_backups?select=account_id&limit=1",
            req.url.toString()
        )
        assertEquals("test-anon-key", req.header("apikey"))
        assertEquals("Bearer test-anon-key", req.header("Authorization"))
        assertEquals("count=exact", req.header("Prefer"))
        assertEquals("application/json", req.header("Accept"))
    }

    @Test
    fun shouldPing_respectsTwelveHourThrottleWindow() {
        val now = 1_750_000_000_000L
        assertTrue(SupabaseKeepAliveManager.shouldPing(0L, nowMs = now))
        assertFalse(
            SupabaseKeepAliveManager.shouldPing(
                lastTimestampMs = now - (6L * 3600L * 1000L),
                nowMs = now
            )
        )
        assertTrue(
            SupabaseKeepAliveManager.shouldPing(
                lastTimestampMs = now - SupabaseKeepAliveManager.DEFAULT_INTERVAL_MS,
                nowMs = now
            )
        )
    }

    @Test
    fun pingNow_succeedsImmediatelyWhenPrimaryTableReturns200() = runBlocking {
        val requestedPaths = mutableListOf<String>()
        val client = fakeClient { req ->
            requestedPaths.add(req.url.encodedPath)
            jsonResponse(req, 200, "[]")
        }

        val fixedNow = 1_750_100_000_000L
        val result = SupabaseKeepAliveManager.pingNow(
            preferences = preferences,
            httpClient = client,
            baseUrl = "https://example.supabase.co",
            anonKey = "anon-key",
            clock = { fixedNow }
        )

        assertTrue(result.isSuccess)
        val value = result.getOrThrow()
        assertEquals("lootdrop_backups", value.endpoint)
        assertEquals(200, value.httpCode)
        assertEquals(1, requestedPaths.size)
        assertEquals("/rest/v1/lootdrop_backups", requestedPaths.first())
        assertEquals(fixedNow, preferences.lastSupabaseKeepAliveTimestamp)
        assertTrue(preferences.lastSupabaseKeepAliveStatus.contains("HTTP 200"))
    }

    @Test
    fun pingNow_fallsBackWhenPrimaryTableReturns404Or401() = runBlocking {
        val requestedPaths = mutableListOf<String>()
        val client = fakeClient { req ->
            requestedPaths.add(req.url.encodedPath)
            when (req.url.encodedPath) {
                "/rest/v1/lootdrop_backups" -> jsonResponse(req, 401, """{"message":"RLS"}""")
                "/rest/v1/keep_alive" -> jsonResponse(req, 200, """[{"id":1}]""")
                else -> jsonResponse(req, 200, "{}")
            }
        }

        val result = SupabaseKeepAliveManager.pingNow(
            preferences = preferences,
            httpClient = client,
            baseUrl = "https://example.supabase.co",
            anonKey = "anon-key",
            clock = { 1_750_200_000_000L }
        )

        assertTrue(result.isSuccess)
        val value = result.getOrThrow()
        assertEquals("keep_alive", value.endpoint)
        assertEquals(200, value.httpCode)
        assertEquals(listOf("/rest/v1/lootdrop_backups", "/rest/v1/keep_alive"), requestedPaths)
    }

    @Test
    fun pingNow_returnsFailureOn500WithoutOverwritingLastSuccessTimestamp() = runBlocking {
        val previousTs = 1_700_000_000_000L
        preferences.lastSupabaseKeepAliveTimestamp = previousTs

        val client = fakeClient { req -> jsonResponse(req, 503, """{"error":"paused"}""") }

        val result = SupabaseKeepAliveManager.pingNow(
            preferences = preferences,
            httpClient = client,
            baseUrl = "https://example.supabase.co",
            anonKey = "anon-key",
            clock = { 1_750_300_000_000L }
        )

        assertTrue(result.isFailure)
        assertEquals(previousTs, preferences.lastSupabaseKeepAliveTimestamp)
        assertTrue(preferences.lastSupabaseKeepAliveStatus.startsWith("ERR HTTP 503"))
    }

    @Test
    fun pingNow_returnsFailureOnNetworkException() = runBlocking {
        val client = fakeClient { _ -> throw IOException("timeout") }

        val result = SupabaseKeepAliveManager.pingNow(
            preferences = preferences,
            httpClient = client,
            baseUrl = "https://example.supabase.co",
            anonKey = "anon-key"
        )

        assertTrue(result.isFailure)
        assertEquals(0L, preferences.lastSupabaseKeepAliveTimestamp)
    }

    @Test
    fun pingIfDue_skipsNetworkWhenRecentlyPinged() = runBlocking {
        val now = 1_750_400_000_000L
        preferences.lastSupabaseKeepAliveTimestamp = now - (1L * 3600L * 1000L)
        var calls = 0
        val client = fakeClient { req ->
            calls++
            jsonResponse(req, 200, "[]")
        }

        val res = SupabaseKeepAliveManager.pingIfDue(
            context = context,
            preferences = preferences,
            httpClient = client,
            baseUrl = "https://example.supabase.co",
            anonKey = "anon-key",
            clock = { now }
        )

        assertNull(res)
        assertEquals(0, calls)
    }
}
