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

import android.graphics.Bitmap
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM_ART
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ARTIST
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DURATION
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE
import android.support.v4.media.session.PlaybackStateCompat
import com.naman14.timberx.constants.Constants.REPEAT_MODE
import com.naman14.timberx.constants.Constants.SHUFFLE_MODE

data class MediaData(
    var mediaId: String? = "",
    var title: String? = "",
    var artist: String? = "",
    var album: String ? = "",
    var artwork: Bitmap? = null,
    var artworkId: Long? = 0,
    var position: Int? = 0,
    var duration: Int? = 0,
    var shuffleMode: Int? = 0,
    var repeatMode: Int? = 0,
    var state: Int? = 0
) {

    fun pullMediaMetadata(metaData: MediaMetadataCompat): MediaData {
        mediaId = metaData.getString(METADATA_KEY_MEDIA_ID)
        title = metaData.getString(METADATA_KEY_TITLE) ?: ""
        album = metaData.getString(METADATA_KEY_ALBUM) ?: ""
        artist = metaData.getString(METADATA_KEY_ARTIST) ?: ""
        duration = metaData.getLong(METADATA_KEY_DURATION).toInt()
        artwork = metaData.getBitmap(METADATA_KEY_ALBUM_ART)
        //this is the album id
        artworkId = metaData.getString(METADATA_KEY_ALBUM_ART_URI)?.let { it.toLong() } ?: 0
        return this
    }

    fun pullPlaybackState(playbackState: PlaybackStateCompat): MediaData {
        position = playbackState.position.toInt()
        state = playbackState.state
        playbackState.extras?.let {
            repeatMode = it.getInt(REPEAT_MODE)
            shuffleMode = it.getInt(SHUFFLE_MODE)
        }
        return this
    }

    /** only used to check the song id for play pause purposes, do not use this elsewhere since it doesn't have any other data */
    fun toDummySong() = Song(id = mediaId?.toLong() ?: 0)
}
