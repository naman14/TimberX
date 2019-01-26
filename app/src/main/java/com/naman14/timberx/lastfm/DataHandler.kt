package com.naman14.timberx.lastfm

import android.content.Context
import com.doubtnutapp.data.remote.util.LiveDataCallAdapterFactory
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.naman14.timberx.lastfm.repository.LastFMRepository
import okhttp3.Cache
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient

object DataHandler {

    private const val BASE_API_URL = "http://ws.audioscrobbler.com/2.0/"
    private const val CACHE_SIZE = (1024 * 1024).toLong()

    private const val loadLastFMImages = true

    private var client: OkHttpClient = OkHttpClient.Builder().build()

    val lastfmRepository: LastFMRepository

    fun initCache(context: Context) {

        val cacheHeader = String.format("max-age=%d,%smax-stale=%d",
                60 * 60 * 24 * 7,
                if (loadLastFMImages) "" else "only-if-cached,",
                Integer.valueOf(31536000))

        client = OkHttpClient.Builder().cache(Cache(context.applicationContext.cacheDir,
                CACHE_SIZE)).addInterceptor {
            it.proceed(it.request().newBuilder().addHeader("Cache-Control", cacheHeader).build())
        }.build()
    }

    init {

        val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()

        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_API_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(LiveDataCallAdapterFactory())
                .build()

        lastfmRepository = LastFMRepository(retrofit.create(LastFmRestService::class.java))

    }
}