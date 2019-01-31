package com.naman14.timberx.models

import android.os.Parcel
import android.os.Parcelable
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import com.naman14.timberx.TimberMusicService
import com.naman14.timberx.util.Utils
import kotlinx.android.parcel.Parcelize

data class Album(var id: Long = 0,
                 var title: String = "",
                 var artist: String = "",
                 var artistId: Long = 0,
                 var songCount: Int = 0,
                 var year: Int = 0
) : MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
                .setMediaId(MediaID(TimberMusicService.TYPE_ALBUM.toString(), id.toString()).asString())
                .setTitle(title)
                .setIconUri(Utils.getAlbumArtUri(id))
                .setSubtitle(artist)
                .build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE), Parcelable {
    constructor(source: Parcel) : this(
            source.readLong(),
            source.readString(),
            source.readString(),
            source.readLong(),
            source.readInt(),
            source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeLong(id)
        writeString(title)
        writeString(artist)
        writeLong(artistId)
        writeInt(songCount)
        writeInt(year)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Album> = object : Parcelable.Creator<Album> {
            override fun createFromParcel(source: Parcel): Album = Album(source)
            override fun newArray(size: Int): Array<Album?> = arrayOfNulls(size)
        }
    }
}