package com.naman14.timberx.models

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import com.naman14.timberx.TimberMusicService
import com.naman14.timberx.util.Utils
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Album(var id: Long = 0,
                 var title: String = "",
                 var artist: String = "",
                 var artistId: Long = 0,
                 var songCount: Int = 0,
                 var year: Int = 0
) : MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
                .setMediaId(MediaID(TimberMusicService.TYPE_ALBUM.toString(), id.toString(), MediaID.currentCaller).asString())
                .setTitle(title)
                .setIconUri(Utils.getAlbumArtUri(id))
                .setSubtitle(artist)
                .build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE) {
}