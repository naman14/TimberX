package com.naman14.timberx.network.models

import com.google.gson.annotations.SerializedName

data class Artwork(@SerializedName("#text") val url: String,
                   @SerializedName("size") val size: String)