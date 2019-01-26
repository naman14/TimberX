package com.naman14.timberx.lastfm.models

import com.google.gson.annotations.SerializedName

data class ArtistInfo(@SerializedName("artist") val artist: LastfmArtist)