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

import android.support.v4.media.MediaBrowserCompat

class MediaID(var type: String? = null, var mediaId: String? = "NA", var caller: String? = currentCaller) {
    companion object {
        const val CALLER_SELF = "self"
        const val CALLER_OTHER = "other"

        private const val TYPE = "type: "
        private const val MEDIA_ID = "media_id: "
        private const val CALLER = "caller: "
        private const val SEPARATOR = " | "

        var currentCaller: String? = MediaID.CALLER_SELF
    }

    var mediaItem: MediaBrowserCompat.MediaItem? = null

    fun asString(): String {
        return TYPE + type + SEPARATOR + MEDIA_ID + mediaId + SEPARATOR + CALLER + caller
    }

    fun fromString(s: String): MediaID {
        this.type = s.substring(6, s.indexOf(SEPARATOR))
        this.mediaId = s.substring(s.indexOf(SEPARATOR) + 3 + 10, s.lastIndexOf(SEPARATOR))
        this.caller = s.substring(s.lastIndexOf(SEPARATOR) + 3 + 8, s.length)
        return this
    }
}
