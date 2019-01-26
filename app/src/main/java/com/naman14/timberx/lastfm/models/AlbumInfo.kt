package com.naman14.timberx.lastfm.models

import com.google.gson.annotations.SerializedName

data class AlbumInfo(@SerializedName("album") val album: LastfmAlbum)