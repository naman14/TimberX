package com.naman14.timberx.network.repository

import com.naman14.timberx.network.api.LyricsRestService
import com.naman14.timberx.network.RetrofitLiveData

class LyricsRepository(private val lyricsRestService: LyricsRestService) {

    fun getLyrics(artist: String, title: String) : RetrofitLiveData<String> = lyricsRestService.getLyrics(artist, title)


}