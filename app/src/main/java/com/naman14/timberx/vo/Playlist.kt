package com.naman14.timberx.vo

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Playlist(val id: Long,
                    val name: String,
                    val songCount: Int): MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
                .setMediaId(id.toString())
                .setTitle(name)
                .setSubtitle(songCount.toString())
                .build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE) {

}