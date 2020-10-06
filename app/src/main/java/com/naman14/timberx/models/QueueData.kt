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

import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.support.v4.media.session.MediaControllerCompat
import com.naman14.timberx.extensions.toIDList

data class QueueData(
    var queueTitle: String = "All songs",
    var queue: LongArray = LongArray(0),
    var currentId: Long = 0
) {
    fun fromMediaController(mediaControllerCompat: MediaControllerCompat?): QueueData {
        mediaControllerCompat?.let {
            return QueueData(
                    queueTitle = mediaControllerCompat.queueTitle?.toString().orEmpty().let {
                        if (it.isEmpty()) "All songs" else it
                    },
                    queue = mediaControllerCompat.queue?.toIDList() ?: LongArray(0),
                    currentId = mediaControllerCompat.metadata?.getString(METADATA_KEY_MEDIA_ID)?.toLong() ?: 0
            )
        }
        return QueueData()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QueueData

        if (queueTitle != other.queueTitle) return false
        if (!queue.contentEquals(other.queue)) return false
        if (currentId != other.currentId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = queueTitle.hashCode()
        result = 31 * result + queue.contentHashCode()
        result = 31 * result + currentId.hashCode()
        return result
    }
}
