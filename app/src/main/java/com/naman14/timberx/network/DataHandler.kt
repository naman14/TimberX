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

import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient

object DataHandler {

    private const val CACHE_SIZE = (1024 * 1024).toLong()

    private const val networkEnabled = true

    var client: OkHttpClient = OkHttpClient.Builder().build()

    fun initCache(context: Context) {

        val cacheHeader = String.format("max-age=%d,max-stale=%d",
                60 * 60 * 24 * 7,

                Integer.valueOf(31536000))

        client = OkHttpClient.Builder().cache(Cache(context.applicationContext.cacheDir,
                CACHE_SIZE)).addInterceptor {
            it.proceed(it.request().newBuilder().addHeader("Cache-Control", cacheHeader).build())
        }.build()
    }
}