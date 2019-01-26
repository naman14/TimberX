package com.naman14.timberx.network.api

import com.doubtnutapp.data.remote.util.LiveDataCallAdapterFactory
import com.naman14.timberx.network.DataHandler
import com.naman14.timberx.network.repository.LyricsRepository
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