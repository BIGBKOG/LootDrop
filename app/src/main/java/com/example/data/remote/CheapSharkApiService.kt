package com.example.data.remote

import com.example.data.model.CheapSharkDto
import retrofit2.http.GET
import retrofit2.http.Query

interface CheapSharkApiService {

    /**
     * CheapShark deals endpoint filtered for 100% discount free game deals.
     * Endpoint: /deals?onSale=1&upperPrice=0
     */
    @GET("deals")
    suspend fun getFreeDeals(
        @Query("onSale") onSale: Int = 1,
        @Query("upperPrice") upperPrice: Int = 0
    ): List<CheapSharkDto>

    /**
     * Look up game deals and accurate Metacritic ratings by game title.
     * Endpoint: /deals?title={title}&pageSize={pageSize}
     */
    @GET("deals")
    suspend fun getDealsByTitle(
        @Query("title") title: String,
        @Query("pageSize") pageSize: Int = 3
    ): List<CheapSharkDto>

    /**
     * Look up game deal and accurate Metacritic rating by exact Steam AppID.
     * Endpoint: /deals?steamAppID={steamAppID}&pageSize=1
     */
    @GET("deals")
    suspend fun getDealsBySteamAppId(
        @Query("steamAppID") steamAppId: Int,
        @Query("pageSize") pageSize: Int = 1
    ): List<CheapSharkDto>

    /**
     * Bulk fetch top Metacritic rated games to preload verified ratings cache.
     * Endpoint: /deals?metacritic=1&sortBy=Metacritic&pageSize={pageSize}
     */
    @GET("deals")
    suspend fun getTopMetacriticDeals(
        @Query("metacritic") metacritic: Int = 1,
        @Query("sortBy") sortBy: String = "Metacritic",
        @Query("pageSize") pageSize: Int = 60
    ): List<CheapSharkDto>
}
