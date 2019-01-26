package com.naman14.timberx.network.repository

import com.naman14.timberx.network.api.LastFmRestService
import com.naman14.timberx.network.RetrofitLiveData
import com.naman14.timberx.network.models.AlbumInfo
import com.naman14.timberx.network.models.ArtistInfo

class LastFMRepository(private val lastFmService: LastFmRestService) {

    fun getAlbumInfo(artist: String, album: String) : RetrofitLiveData<AlbumInfo> = lastFmService.getAlbumInfo(artist, album)

    fun getArtistInfo(artist: String) : RetrofitLiveData<ArtistInfo> = lastFmService.getArtistInfo(artist)

}