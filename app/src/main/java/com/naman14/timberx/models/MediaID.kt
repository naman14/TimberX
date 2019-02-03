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

    private val TYPE = "type: "
    private val MEDIA_ID = "media_id: "
    private val CALLER = "caller: "
    private val SEPERATOR = " | "

    companion object {
        const val CALLER_SELF = "self"
        const val CALLER_OTHER = "other"

        var currentCaller: String? = MediaID.CALLER_SELF
    }

    var mediaItem: MediaBrowserCompat.MediaItem? = null

    fun asString(): String {
        return TYPE + type + SEPERATOR + MEDIA_ID + mediaId + SEPERATOR + CALLER + caller
    }

    fun fromString(s: String): MediaID {
        this.type = s.substring(6, s.indexOf(SEPERATOR))
        this.mediaId = s.substring(s.indexOf(SEPERATOR) + 3 + 10, s.lastIndexOf(SEPERATOR))
        this.caller = s.substring(s.lastIndexOf(SEPERATOR) + 3 + 8, s.length)
        return this
    }
}
