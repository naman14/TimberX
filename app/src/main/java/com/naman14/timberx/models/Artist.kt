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

import android.database.Cursor
import android.provider.MediaStore.Audio.Artists.ARTIST
import android.provider.MediaStore.Audio.Artists.NUMBER_OF_ALBUMS
import android.provider.MediaStore.Audio.Artists.NUMBER_OF_TRACKS
import android.provider.MediaStore.Audio.Artists._ID
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import com.naman14.timberx.playback.TimberMusicService.Companion.TYPE_ARTIST
import com.naman14.timberx.extensions.value
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Artist(
    var id: Long = 0,
    var name: String = "",
    var songCount: Int = 0,
    var albumCount: Int = 0
) : MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
                .setMediaId(MediaID(TYPE_ARTIST.toString(), id.toString()).asString())
                .setTitle(name)
                .setSubtitle("$albumCount albums")
                .build(), FLAG_BROWSABLE) {
    companion object {
        fun fromCursor(cursor: Cursor): Artist {
            return Artist(
                    id = cursor.value(_ID),
                    name = cursor.value(ARTIST),
                    songCount = cursor.value(NUMBER_OF_TRACKS),
                    albumCount = cursor.value(NUMBER_OF_ALBUMS)
            )
        }
    }
}
