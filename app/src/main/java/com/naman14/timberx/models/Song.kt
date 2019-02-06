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
import android.provider.MediaStore.Audio.Media.ALBUM
import android.provider.MediaStore.Audio.Media.ARTIST
import android.provider.MediaStore.Audio.Media.ARTIST_ID
import android.provider.MediaStore.Audio.Media.DURATION
import android.provider.MediaStore.Audio.Media.TITLE
import android.provider.MediaStore.Audio.Media.TRACK
import android.provider.MediaStore.Audio.Media._ID
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import com.naman14.timberx.TimberMusicService.Companion.TYPE_SONG
import com.naman14.timberx.extensions.value
import com.naman14.timberx.extensions.valueOrEmpty
import com.naman14.timberx.util.Utils.getAlbumArtUri
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Song(
    var id: Long = 0,
    var albumId: Long = 0,
    var artistId: Long = 0,
    var title: String = "",
    var artist: String = "",
    var album: String = "",
    var duration: Int = 0,
    var trackNumber: Int = 0
) : MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
                .setMediaId(MediaID("$TYPE_SONG", "$id").asString())
                .setTitle(title)
                .setIconUri(getAlbumArtUri(albumId))
                .setSubtitle(artist)
                .build(), FLAG_PLAYABLE) {
    companion object {
        fun fromCursor(cursor: Cursor): Song {
            return Song(
                    id = cursor.value(_ID),
                    artistId = cursor.value(ARTIST_ID),
                    title = cursor.valueOrEmpty(TITLE),
                    artist = cursor.valueOrEmpty(ARTIST),
                    album = cursor.valueOrEmpty(ALBUM),
                    duration = cursor.value(DURATION),
                    trackNumber = cursor.value<Int>(TRACK).normalizeTrackNumber()
            )
        }
    }
}

private fun Int.normalizeTrackNumber(): Int {
    var returnValue = this
    // This fixes bug where some track numbers displayed as 100 or 200.
    while (returnValue >= 1000) {
        // When error occurs the track numbers have an extra 1000 or 2000 added, so decrease till normal.
        returnValue -= 1000
    }
    return returnValue
}
