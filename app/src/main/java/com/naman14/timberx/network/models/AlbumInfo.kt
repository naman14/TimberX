package com.naman14.timberx.network.models

import com.google.gson.annotations.SerializedName

data class AlbumInfo(@SerializedName("album") val album: LastfmAlbum)