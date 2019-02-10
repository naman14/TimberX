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
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import com.naman14.timberx.playback.TimberMusicService.Companion.TYPE_PLAYLIST
import com.naman14.timberx.extensions.value
import com.naman14.timberx.extensions.valueOrEmpty
import kotlinx.android.parcel.Parcelize
import android.provider.MediaStore.Audio.Playlists._ID
import android.provider.MediaStore.Audio.Playlists.NAME

@Parcelize
data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int
) : MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
                .setMediaId(MediaID(TYPE_PLAYLIST.toString(), id.toString()).asString())
                .setTitle(name)
                .setSubtitle("$songCount songs")
                .build(), FLAG_BROWSABLE) {
    companion object {
        fun fromCursor(cursor: Cursor, songCount: Int): Playlist {
            return Playlist(
                    id = cursor.value(_ID),
                    name = cursor.valueOrEmpty(NAME),
                    songCount = songCount
            )
        }
    }
}
