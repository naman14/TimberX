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
import android.provider.MediaStore.Audio.Albums.ALBUM
import android.provider.MediaStore.Audio.Albums.ARTIST
import android.provider.MediaStore.Audio.Albums.FIRST_YEAR
import android.provider.MediaStore.Audio.Albums.NUMBER_OF_SONGS
import android.provider.MediaStore.Audio.Albums._ID
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import com.naman14.timberx.playback.TimberMusicService.Companion.TYPE_ALBUM
import com.naman14.timberx.extensions.value
import com.naman14.timberx.extensions.valueOrEmpty
import com.naman14.timberx.util.Utils
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Album(
    var id: Long = 0,
    var title: String = "",
    var artist: String = "",
    var artistId: Long = 0,
    var songCount: Int = 0,
    var year: Int = 0
) : MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
                .setMediaId(MediaID(TYPE_ALBUM.toString(), id.toString()).asString())
                .setTitle(title)
                .setIconUri(Utils.getAlbumArtUri(id))
                .setSubtitle(artist)
                .build(), FLAG_BROWSABLE) {

    companion object {
        fun fromCursor(cursor: Cursor, artistId: Long = -1): Album {
            return Album(
                    id = cursor.value(_ID),
                    title = cursor.valueOrEmpty(ALBUM),
                    artist = cursor.valueOrEmpty(ARTIST),
                    artistId = artistId,
                    songCount = cursor.value(NUMBER_OF_SONGS),
                    year = cursor.value(FIRST_YEAR)
            )
        }
    }
}
