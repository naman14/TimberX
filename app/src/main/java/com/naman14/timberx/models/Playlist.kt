package com.naman14.timberx.models

import android.os.Parcel
import android.os.Parcelable
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import com.naman14.timberx.TimberMusicService
import kotlinx.android.parcel.Parcelize

data class Playlist(val id: Long,
                    val name: String,
                    val songCount: Int) : MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
                .setMediaId(MediaID(TimberMusicService.TYPE_PLAYLIST.toString(), id.toString()).asString())
                .setTitle(name)
                .setSubtitle(songCount.toString())
                .build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE), Parcelable {
    constructor(source: Parcel) : this(
            source.readLong(),
            source.readString(),
            source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeLong(id)
        writeString(name)
        writeInt(songCount)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Playlist> = object : Parcelable.Creator<Playlist> {
            override fun createFromParcel(source: Parcel): Playlist = Playlist(source)
            override fun newArray(size: Int): Array<Playlist?> = arrayOfNulls(size)
        }
    }
}