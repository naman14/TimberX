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

package com.naman14.timberx.network.api

import com.naman14.timberx.network.DataHandler
import com.naman14.timberx.network.repository.LyricsRepository
import com.naman14.timberx.network.util.LiveDataCallAdapterFactory
import com.naman14.timberx.network.util.LyricsConverterFactory
import retrofit2.Retrofit

object LyricsDataHandler {

    private const val BASE_API_URL = "https://makeitpersonal.co"
    val lyricsRepository: LyricsRepository

    init {

        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_API_URL)
                .client(DataHandler.client)
                .addConverterFactory(LyricsConverterFactory())
                .addCallAdapterFactory(LiveDataCallAdapterFactory())
                .build()

        lyricsRepository = LyricsRepository(retrofit.create(LyricsRestService::class.java))
    }
}