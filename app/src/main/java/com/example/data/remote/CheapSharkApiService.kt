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
}
