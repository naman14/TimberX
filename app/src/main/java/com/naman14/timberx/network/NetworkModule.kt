/*
 * Copyright (c) 2019 Naman Dwivedi.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package com.naman14.timberx.network

import android.app.Application
import com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES
import com.google.gson.GsonBuilder
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.koin.dsl.module.module

private const val CACHE_MAX_AGE = 60 * 60 * 24 * 7
private const val CACHE_MAX_STALE = 31536000
private const val CACHE_SIZE = 1024L * 1024
private const val CACHE_CONTROL = "Cache-Control"

val networkModule = module {

    // OkHttp
    single {
        val cacheHeader = "max-age=$CACHE_MAX_AGE,max-stale=$CACHE_MAX_STALE"
        val cache = Cache(get<Application>().cacheDir, CACHE_SIZE)
        OkHttpClient.Builder()
                .cache(cache)
                .addInterceptor {
                    val newRequest = it.request()
                            .newBuilder()
                            .addHeader(CACHE_CONTROL, cacheHeader)
                            .build()
                    it.proceed(newRequest)
                }
                .build()
    }

    // Gson
    single {
        GsonBuilder()
                .setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES)
                .create()
    }
}
