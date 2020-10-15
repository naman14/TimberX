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
package com.happyproject.btsplay.models

import android.database.Cursor
import android.provider.MediaStore.Audio.Albums.*
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import com.happyproject.btsplay.playback.TimberMusicService.Companion.TYPE_ALBUM
import com.happyproject.btsplay.extensions.value
import com.happyproject.btsplay.extensions.valueOrEmpty
import com.happyproject.btsplay.util.Utils
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
        fun fromCursor(cursor: Cursor): Album {
            return Album(
                    id = cursor.value(_ID),
                    title = cursor.valueOrEmpty(ALBUM),
                    artist = cursor.valueOrEmpty(ARTIST),
                    artistId = cursor.value("artist_id"),
                    songCount = cursor.value(NUMBER_OF_SONGS),
                    year = cursor.value(FIRST_YEAR)
            )
        }
    }
}
