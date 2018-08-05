package com.naman14.timberx.vo

import android.app.Activity
import android.graphics.Bitmap
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.naman14.timberx.db.QueueEntity
import com.naman14.timberx.util.Utils
import com.naman14.timberx.util.getMediaController

data class MediaData(var title: String = "",
                     var artist: String = "",
                     var album: String = "",
                     var artwork: Bitmap? = null,
                     var artworkUri: String = "",
                     var position: Int = 0,
                     var duration: Int = 0,
                     var shuffleMode: Int = 0,
                     var repeatMode: Int = 0,
                     var state: Int = 0) {


    fun fromMediaMetadata(metaData: MediaMetadataCompat): MediaData {
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

}