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

import android.app.Activity
import android.graphics.Bitmap
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.naman14.timberx.db.QueueEntity
import com.naman14.timberx.util.Constants
import com.naman14.timberx.util.Utils
import com.naman14.timberx.util.media.getMediaController

data class MediaData(var mediaId: String? = "",
                     var title: String? = "",
                     var artist: String? = "",
                     var album: String ?= "",
                     var artwork: Bitmap? = null,
                     var artworkUri: String? = "",
                     var position: Int? = 0,
                     var duration: Int? = 0,
                     var shuffleMode: Int? = 0,
                     var repeatMode: Int? = 0,
                     var state: Int? = 0) {


    fun fromMediaMetadata(metaData: MediaMetadataCompat): MediaData {
        mediaId = metaData.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
        title = metaData.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
        album = metaData.getString(MediaMetadataCompat.METADATA_KEY_ALBUM)
        artist = metaData.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
        duration = metaData.getLong(MediaMetadataCompat.METADATA_KEY_DURATION).toInt()
        artwork = metaData.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART)
        artworkUri = metaData.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI)
        return this
    }

    fun fromPlaybackState(playbackState: PlaybackStateCompat): MediaData {
        position = playbackState.position.toInt()
        state = playbackState.state
        playbackState.extras?.let {
            repeatMode = it.getInt(Constants.REPEAT_MODE)
            shuffleMode = it.getInt(Constants.SHUFFLE_MODE)
        }
        return this
    }

    fun fromMediaController(activity: Activity): MediaData {
        val mediaController = getMediaController(activity)
        mediaController?.let {
            shuffleMode = mediaController.shuffleMode
            repeatMode = mediaController.repeatMode
            fromMediaMetadata(mediaController.metadata)
            fromPlaybackState(mediaController.playbackState)
        }
        return this
    }

    fun fromDBData(song: Song, queueEntity: QueueEntity): MediaData {
        mediaId = song.id.toString()
        title = song.title
        album = song.album
        artist = song.artist
        duration = song.duration
        artworkUri = Utils.getAlbumArtUri(song.albumId).toString()

        shuffleMode = queueEntity.shuffleMode!!
        repeatMode = queueEntity.repeatMode!!
        position = queueEntity.currentSeekPos!!.toInt()
        state = queueEntity.playState!!
        return this
    }

    //only used to check the song id for play pause purposes, do not use this elsewhere since it doesn't have any other data
    fun toDummySong(): Song {
        return Song(id = mediaId?.toLong() ?: 0)
    }

}