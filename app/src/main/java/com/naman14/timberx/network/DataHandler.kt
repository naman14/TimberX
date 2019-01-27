package com.naman14.timberx.network

import android.content.Context
import com.doubtnutapp.data.remote.util.LiveDataCallAdapterFactory
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.naman14.timberx.network.api.LastFmRestService
import com.naman14.timberx.network.api.LyricsRestService
import com.naman14.timberx.network.repository.LastFMRepository
import com.naman14.timberx.network.repository.LyricsRepository
import okhttp3.Cache
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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