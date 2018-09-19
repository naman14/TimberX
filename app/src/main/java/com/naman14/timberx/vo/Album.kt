package com.naman14.timberx.vo

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Album(var id: Long = 0,
                 var title: String = "",
                 var artist: String = "",
                 var artistId: Long = 0,
                var songCount: Int = 0,
                 var year: Int  = 0
): Parcelable