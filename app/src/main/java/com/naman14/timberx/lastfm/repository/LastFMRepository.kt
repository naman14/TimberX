package com.naman14.timberx.lastfm.repository

import com.naman14.timberx.lastfm.LastFmRestService
import com.naman14.timberx.lastfm.RetrofitLiveData
import com.naman14.timberx.lastfm.models.AlbumInfo
import com.naman14.timberx.lastfm.models.ArtistInfo

class LastFMRepository(val lastFmService: LastFmRestService) {

    fun getAlbumInfo(artist: String, album: String) : RetrofitLiveData<AlbumInfo> = lastFmService.getAlbumInfo(artist, album)

    fun getArtistInfo(artist: String) : RetrofitLiveData<ArtistInfo> = lastFmService.getArtistInfo(artist)

}