package com.naman14.timberx.models

import android.os.Parcelable
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import com.naman14.timberx.TimberMusicService
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Artist(var id: Long = 0,
                  var name: String = "",
                  var songCount: Int = 0,
                  var albumCount: Int = 0) : MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
                .setMediaId(MediaID(TimberMusicService.TYPE_ARTIST.toString(), id.toString()).asString())
                .setTitle(name)
                .setSubtitle(albumCount.toString() + " albums")
                .build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE), Parcelable {

}