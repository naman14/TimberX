package com.naman14.timberx.cast

import android.net.Uri

import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.common.images.WebImage
import com.naman14.timberx.models.Song
import com.naman14.timberx.util.Constants
import com.naman14.timberx.util.Utils

import java.net.MalformedURLException
import java.net.URL

/**
 * Created by naman on 2/12/17.
 */

object CastHelper {

    fun startCasting(castSession: CastSession, song: Song) {

        val ipAddress = Utils.getIPAddress(true)
        val baseUrl: URL
        try {
            baseUrl = URL("http", ipAddress, Constants.CAST_SERVER_PORT, "")
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            return
        }

        val songUrl = baseUrl.toString() + "/song?id=" + song.id
        val albumArtUrl = baseUrl.toString() + "/albumart?id=" + song.albumId

        val musicMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK)

        musicMetadata.putString(MediaMetadata.KEY_TITLE, song.title)
        musicMetadata.putString(MediaMetadata.KEY_ARTIST, song.artist)
        musicMetadata.putString(MediaMetadata.KEY_ALBUM_TITLE, song.album)
        musicMetadata.putInt(MediaMetadata.KEY_TRACK_NUMBER, song.trackNumber)
        musicMetadata.addImage(WebImage(Uri.parse(albumArtUrl)))

        try {
            val mediaInfo = MediaInfo.Builder(songUrl)
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setContentType("audio/mpeg")
                    .setMetadata(musicMetadata)
                    .setStreamDuration(song.duration.toLong())
                    .build()
            val remoteMediaClient = castSession.remoteMediaClient
            remoteMediaClient.load(mediaInfo, true, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun playPause(castSession: CastSession, song: Song) {
        val remoteMediaClient = castSession.remoteMediaClient
        if (remoteMediaClient.currentItem != null) {
            remoteMediaClient.togglePlayback()
        } else {
            startCasting(castSession, song)
        }
    }
}
