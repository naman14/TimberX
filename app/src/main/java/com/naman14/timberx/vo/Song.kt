package com.naman14.timberx.vo

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Song(var id: Long = 0,
                var albumId: Long = 0,
                var artistId: Long = 0,
                var title: String = "",
                var artist: String = "",
                var album: String = "",
                var duration: Int = 0,
                var trackNumber: Int = 0): Parcelable {
}