/*
 * Copyright (c) 2019 Naman Dwivedi.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package com.naman14.timberx.models

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import com.naman14.timberx.util.toIDList

data class QueueData(
    var queueTitle: String = "All songs",
    var queue: LongArray = LongArray(0),
    var currentId: Long = 0
) {

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
