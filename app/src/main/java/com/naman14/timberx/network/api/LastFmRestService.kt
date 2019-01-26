package com.naman14.timberx.network.api

import com.naman14.timberx.network.RetrofitLiveData
import com.naman14.timberx.network.models.AlbumInfo
import com.naman14.timberx.network.models.ArtistInfo
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface LastFmRestService {

    @Headers("Cache-Control: public")
    @GET(BASE_PARAMETERS_ALBUM)
    fun getAlbumInfo(@Query("artist") artist: String, @Query("album") album: String): RetrofitLiveData<AlbumInfo>

    @Headers("Cache-Control: public")
    @GET(BASE_PARAMETERS_ARTIST)
    fun getArtistInfo(@Query("artist") artist: String): RetrofitLiveData<ArtistInfo>

    companion object {
        const val BASE_PARAMETERS_ALBUM = "?method=album.getinfo&api_key=fdb3a51437d4281d4d64964d333531d4&format=json"
        const val BASE_PARAMETERS_ARTIST = "?method=artist.getinfo&api_key=fdb3a51437d4281d4d64964d333531d4&format=json"
    }

}