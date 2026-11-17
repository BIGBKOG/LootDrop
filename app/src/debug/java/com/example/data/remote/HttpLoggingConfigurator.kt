package com.example.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

internal object HttpLoggingConfigurator {
    fun configure(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        return builder.addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
        )
    }
}
