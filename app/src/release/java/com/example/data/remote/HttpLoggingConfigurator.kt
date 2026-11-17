package com.example.data.remote

import okhttp3.OkHttpClient

internal object HttpLoggingConfigurator {
    fun configure(builder: OkHttpClient.Builder): OkHttpClient.Builder = builder
}
