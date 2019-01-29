package com.naman14.timberx.models

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import com.naman14.timberx.util.toIDList

data class QueueData(var queueTitle: String = "All songs",
                     var queue: LongArray = LongArray(0),
                     var currentId: Long = 0) {

    fun fromMediaController(mediaControllerCompat: MediaControllerCompat?): QueueData {
        mediaControllerCompat?.let {
            val queueData = QueueData(mediaControllerCompat.queueTitle?.toString() ?: "All songs",
                    mediaControllerCompat.queue?.toIDList() ?: LongArray(0),
                    mediaControllerCompat.metadata?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)?.toLong() ?: 0)
            return queueData
        }
        return QueueData()
    }
}