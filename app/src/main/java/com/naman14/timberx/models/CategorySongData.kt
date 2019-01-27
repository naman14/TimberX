package com.naman14.timberx.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CategorySongData(val title: String,
                            val songCount: Int): Parcelable