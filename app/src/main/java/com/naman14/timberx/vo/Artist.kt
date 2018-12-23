package com.naman14.timberx.vo

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Artist(var id: Long = 0,
                  var name: String = "",
                  var songCount: Int = 0,
                  var albumCount: Int = 0):  MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
                .setMediaId(id.toString())
                .setTitle(name)
                .setSubtitle(albumCount.toString() + " songs")
                .build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE)