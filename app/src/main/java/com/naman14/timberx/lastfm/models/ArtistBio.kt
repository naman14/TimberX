package com.naman14.timberx.lastfm.models

import com.google.gson.annotations.SerializedName

data class ArtistBio(@SerializedName("summary") val summary: String,
                     @SerializedName("content") val content: String)