package com.naman14.timberx.network.models

import com.google.gson.annotations.SerializedName

data class LastfmArtist(@SerializedName("image") val artwork: List<Artwork>, @SerializedName("bio") val bio: ArtistBio)