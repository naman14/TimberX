package com.naman14.timberx.util

import android.support.v4.media.MediaBrowserCompat

class MediaID(var type: String? = null, var mediaId: String? = "NA") {

    private val TYPE = "type: "
    private val MEDIA_ID = "media_id: "
    private val SEPERATOR = " | "

    var mediaItem: MediaBrowserCompat.MediaItem? = null

    fun asString(): String {
        return TYPE + type + SEPERATOR + MEDIA_ID + mediaId
    }

    fun fromString(s: String): MediaID {
        this.type = s.substring(6, s.indexOf(SEPERATOR))
        this.mediaId = s.substring(s.indexOf(SEPERATOR) + 2 + 11, s.length)
        return this
    }

}