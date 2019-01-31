package com.naman14.timberx.models

import android.os.Parcel
import android.os.Parcelable
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import com.naman14.timberx.TimberMusicService
import kotlinx.android.parcel.Parcelize

data class Artist(var id: Long = 0,
                  var name: String = "",
                  var songCount: Int = 0,
                  var albumCount: Int = 0) : MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
                .setMediaId(MediaID(TimberMusicService.TYPE_ARTIST.toString(), id.toString()).asString())
                .setTitle(name)
                .setSubtitle(albumCount.toString() + " albums")
                .build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE), Parcelable {
    constructor(source: Parcel) : this(
            source.readLong(),
            source.readString(),
            source.readInt(),
            source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeLong(id)
        writeString(name)
        writeInt(songCount)
        writeInt(albumCount)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Artist> = object : Parcelable.Creator<Artist> {
            override fun createFromParcel(source: Parcel): Artist = Artist(source)
            override fun newArray(size: Int): Array<Artist?> = arrayOfNulls(size)
        }
    }
}