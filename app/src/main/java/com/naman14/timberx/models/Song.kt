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
import android.support.v4.media.MediaDescriptionCompat
import com.naman14.timberx.TimberMusicService
import com.naman14.timberx.util.Utils
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
                .setMediaId(MediaID(TimberMusicService.TYPE_SONG.toString(), id.toString()).asString())
                .setTitle(title)
                .setIconUri(Utils.getAlbumArtUri(albumId))
                .setSubtitle(artist)
                .build(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
