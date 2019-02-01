package com.naman14.timberx.models

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import com.naman14.timberx.TimberMusicService
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Genre(val id: Long,
                 val name: String,
                 val songCount: Int) : MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
                .setMediaId(MediaID(TimberMusicService.TYPE_GENRE.toString(), id.toString(), MediaID.currentCaller).asString())
                .setTitle(name)
                .setSubtitle(songCount.toString())
                .build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE) {
}