package com.naman14.timberx.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class CategorySongData(val title: String,
                            val songCount: Int,
                            val type: Int,
                            val id: Long) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readInt(),
            source.readInt(),
            source.readLong()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(title)
        writeInt(songCount)
        writeInt(type)
        writeLong(id)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<CategorySongData> = object : Parcelable.Creator<CategorySongData> {
            override fun createFromParcel(source: Parcel): CategorySongData = CategorySongData(source)
            override fun newArray(size: Int): Array<CategorySongData?> = arrayOfNulls(size)
        }
    }
}