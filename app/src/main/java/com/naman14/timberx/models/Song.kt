package com.naman14.timberx.models

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import com.naman14.timberx.TimberMusicService
import com.naman14.timberx.util.MediaID
import com.naman14.timberx.util.Utils
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Song(var id: Long = 0,
                var albumId: Long = 0,
                var artistId: Long = 0,
                var title: String = "",
                var artist: String = "",
                var album: String = "",
                var duration: Int = 0,
                var trackNumber: Int = 0): MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
                .setMediaId(MediaID(TimberMusicService.TYPE_SONG.toString(), id.toString()).asString())
                .setTitle(title)
                .setIconUri(Utils.getAlbumArtUri(albumId))
                .setSubtitle(artist)
                .build(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE) {
}