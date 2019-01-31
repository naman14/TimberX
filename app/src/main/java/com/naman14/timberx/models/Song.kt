package com.naman14.timberx.models

import android.os.Parcel
import android.os.Parcelable
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import com.naman14.timberx.TimberMusicService
import com.naman14.timberx.util.Utils

data class Song(var id: Long = 0,
                var albumId: Long = 0,
                var artistId: Long = 0,
                var title: String = "",
                var artist: String = "",
                var album: String = "",
                var duration: Int = 0,
                var trackNumber: Int = 0) : MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
                .setMediaId(MediaID(TimberMusicService.TYPE_SONG.toString(), id.toString()).asString())
                .setTitle(title)
                .setIconUri(Utils.getAlbumArtUri(albumId))
                .setSubtitle(artist)
                .build(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE), Parcelable {

    constructor(source: Parcel) : this(
            source.readLong(),
            source.readLong(),
            source.readLong(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readInt(),
            source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeLong(id)
        writeLong(albumId)
        writeLong(artistId)
        writeString(title)
        writeString(artist)
        writeString(album)
        writeInt(duration)
        writeInt(trackNumber)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Song> = object : Parcelable.Creator<Song> {
            override fun createFromParcel(source: Parcel): Song = Song(source)
            override fun newArray(size: Int): Array<Song?> = arrayOfNulls(size)
        }
    }
}