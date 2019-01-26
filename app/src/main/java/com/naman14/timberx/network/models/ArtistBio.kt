package com.naman14.timberx.network.models

import com.google.gson.annotations.SerializedName

data class ArtistBio(@SerializedName("summary") val summary: String,
                     @SerializedName("content") val content: String)