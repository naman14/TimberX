package com.naman14.timberx.network.api

import com.naman14.timberx.network.RetrofitLiveData
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface LyricsRestService {

    @Headers("Cache-Control: public")
    @GET("/lyrics")
    abstract fun getLyrics(@Query("artist") artist: String, @Query("title") title: String): RetrofitLiveData<String>
}