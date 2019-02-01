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

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.naman14.timberx.network.DataHandler
import com.naman14.timberx.network.repository.LastFMRepository
import com.naman14.timberx.network.util.LiveDataCallAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object LastFmDataHandler {

    private const val BASE_API_URL = "http://ws.audioscrobbler.com/2.0/"
    val lastfmRepository: LastFMRepository

    init {

        val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()

        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_API_URL)
                .client(DataHandler.client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(LiveDataCallAdapterFactory())
                .build()

        lastfmRepository = LastFMRepository(retrofit.create(LastFmRestService::class.java))
    }
}