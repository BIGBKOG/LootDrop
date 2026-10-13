package com.example.data.remote

import com.example.data.model.GiveawayDto
import retrofit2.http.GET
import retrofit2.http.Query

interface GamerPowerApiService {

    @GET("giveaways")
    suspend fun getGiveaways(
        @Query("platform") platform: String? = null,
        @Query("type") type: String? = null,
        @Query("sort-by") sortBy: String? = null
    ): List<GiveawayDto>

    @GET("giveaway")
    suspend fun getGiveawayDetails(
        @Query("id") id: Int
    ): GiveawayDto
}
