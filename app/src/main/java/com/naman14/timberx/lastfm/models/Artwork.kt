package com.naman14.timberx.lastfm.models

import com.google.gson.annotations.SerializedName

data class Artwork(@SerializedName("#text") val url: String,
                   @SerializedName("size") val size: String)