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

import com.naman14.timberx.network.api.LyricsRestService
import com.naman14.timberx.network.conversion.LyricsConverterFactory
import okhttp3.OkHttpClient
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

private const val LYRICS_API_HOST = "https://makeitpersonal.co"

val lyricsModule = module {

    single<LyricsRestService> {
        val client = get<OkHttpClient>()
        val retrofit = Retrofit.Builder()
                .baseUrl(LYRICS_API_HOST)
                .client(client)
                .addConverterFactory(LyricsConverterFactory())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        retrofit.create(LyricsRestService::class.java)
    }
}
