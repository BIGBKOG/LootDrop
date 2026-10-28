package com.example.data.remote

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "https://www.gamerpower.com/api/"
    private const val HTTP_CACHE_SIZE = 15L * 1024L * 1024L // 15 MB max HTTP cache

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Volatile
    private var okHttpClientInstance: OkHttpClient? = null

    fun getOkHttpClient(context: Context): OkHttpClient {
        return okHttpClientInstance ?: synchronized(this) {
            okHttpClientInstance ?: buildOkHttpClient(context.applicationContext).also {
                okHttpClientInstance = it
            }
        }
    }

    private fun buildOkHttpClient(context: Context): OkHttpClient {
        val cacheDir = File(context.cacheDir, "http_cache")
        val cache = runCatching { Cache(cacheDir, HTTP_CACHE_SIZE) }.getOrNull()

        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool(8, 5, TimeUnit.MINUTES))
            .apply {
                if (cache != null) {
                    cache(cache)
                }
            }
            .addInterceptor { chain ->
                // Provide standard user agent header for GamerPower API requests
                val request = chain.request().newBuilder()
                    .header("User-Agent", "LootDrop-Android/1.0")
                    .build()
                chain.proceed(request)
            }
            .addNetworkInterceptor(Interceptor { chain ->
                val response: Response = chain.proceed(chain.request())
                // Cache API responses for 60s while fresh, and up to 24h when offline
                response.newBuilder()
                    .header("Cache-Control", "public, max-age=60, max-stale=86400")
                    .removeHeader("Pragma")
                    .build()
            })
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
            .build()
    }

    private const val CHEAPSHARK_BASE_URL = "https://www.cheapshark.com/api/1.0/"
    private const val CHEAPSHARK_USER_AGENT = "LootDropApp/2.1.0 (contact@lootdrop.com)"

    @Volatile
    private var cheapSharkClientInstance: OkHttpClient? = null

    private fun getCheapSharkOkHttpClient(context: Context? = null): OkHttpClient {
        return cheapSharkClientInstance ?: synchronized(this) {
            cheapSharkClientInstance ?: run {
                val builder = OkHttpClient.Builder()
                    .connectTimeout(12, TimeUnit.SECONDS)
                    .readTimeout(12, TimeUnit.SECONDS)
                    .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .header("User-Agent", CHEAPSHARK_USER_AGENT)
                            .header("Accept", "application/json")
                            .build()
                        chain.proceed(request)
                    }
                if (context != null) {
                    val cacheDir = File(context.cacheDir, "cheapshark_cache")
                    runCatching { Cache(cacheDir, 5L * 1024L * 1024L) }.getOrNull()?.let {
                        builder.cache(it)
                    }
                }
                builder.build().also { cheapSharkClientInstance = it }
            }
        }
    }

    @Volatile
    private var cheapSharkApiServiceInstance: CheapSharkApiService? = null

    fun getCheapSharkApiService(context: Context? = null): CheapSharkApiService {
        return cheapSharkApiServiceInstance ?: synchronized(this) {
            cheapSharkApiServiceInstance ?: Retrofit.Builder()
                .baseUrl(CHEAPSHARK_BASE_URL)
                .client(getCheapSharkOkHttpClient(context))
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(CheapSharkApiService::class.java).also {
                    cheapSharkApiServiceInstance = it
                }
        }
    }

    val cheapSharkApiService: CheapSharkApiService
        get() = getCheapSharkApiService(null)

    @Volatile
    private var apiServiceInstance: GamerPowerApiService? = null

    fun getApiService(context: Context): GamerPowerApiService {
        return apiServiceInstance ?: synchronized(this) {
            apiServiceInstance ?: Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(getOkHttpClient(context))
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(GamerPowerApiService::class.java).also {
                    apiServiceInstance = it
                }
        }
    }

    // Backward-compatibility property: fallback without context
    val apiService: GamerPowerApiService
        get() = apiServiceInstance ?: synchronized(this) {
            apiServiceInstance ?: run {
                val client = okHttpClientInstance ?: OkHttpClient.Builder()
                    .connectTimeout(12, TimeUnit.SECONDS)
                    .readTimeout(12, TimeUnit.SECONDS)
                    .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .header("User-Agent", "LootDrop-Android/1.0")
                            .build()
                        chain.proceed(request)
                    }
                    .build()

                Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .build()
                    .create(GamerPowerApiService::class.java).also {
                        apiServiceInstance = it
                    }
            }
        }

    fun getHttpCacheSizeBytes(context: Context): Long {
        return runCatching {
            val cacheDir = File(context.cacheDir, "http_cache")
            if (cacheDir.exists()) {
                cacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
            } else 0L
        }.getOrDefault(0L)
    }

    fun clearHttpCache(context: Context): Long {
        var reclaimed = 0L
        runCatching {
            val cacheDir = File(context.cacheDir, "http_cache")
            if (cacheDir.exists()) {
                reclaimed = cacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
                okHttpClientInstance?.cache?.evictAll()
                cacheDir.deleteRecursively()
            }
        }
        return reclaimed
    }
}
