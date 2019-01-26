package com.naman14.timberx.network.api

import com.doubtnutapp.data.remote.util.LiveDataCallAdapterFactory
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.naman14.timberx.network.DataHandler
import com.naman14.timberx.network.repository.LyricsRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object LyricsDataHandler {

    private const val BASE_API_URL = "https://makeitpersonal.co"
    val lyricsRepository: LyricsRepository

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

        lyricsRepository = LyricsRepository(retrofit.create(LyricsRestService::class.java))
    }
}