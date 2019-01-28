package com.naman14.timberx.models

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import com.naman14.timberx.util.toIDList

data class QueueData(var queueTitle: String? = "All songs",
                     var queue: LongArray? = LongArray(0),
                     var currentId: Long? = 0) {

    fun fromMediaController(mediaControllerCompat: MediaControllerCompat?): QueueData {
        mediaControllerCompat?.let {
            return QueueData(mediaControllerCompat.queueTitle?.toString(), mediaControllerCompat.queue?.toIDList(),
                    mediaControllerCompat.metadata?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)?.toLong())
        }
        return QueueData()
    }
}