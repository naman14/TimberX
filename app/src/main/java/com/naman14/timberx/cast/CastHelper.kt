package com.naman14.timberx.cast

import android.net.Uri

import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.MediaStatus
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

    fun castSong(castSession: CastSession, song: Song) {
        try {
            val remoteMediaClient = castSession.remoteMediaClient
            remoteMediaClient.load(song.toMediaInfo(), true, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun castSongQueue(castSession: CastSession, songs: ArrayList<Song>, currentPos: Int) {
        try {
            val remoteMediaClient = castSession.remoteMediaClient
            remoteMediaClient.queueLoad(songs.toQueueInfoList(), currentPos, MediaStatus.REPEAT_MODE_REPEAT_OFF, 0, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun ArrayList<Song>.toQueueInfoList(): Array<MediaQueueItem> {
        val items = ArrayList<MediaQueueItem>()
        forEachIndexed { index, song ->
            items.add(MediaQueueItem.Builder(song.toMediaInfo()).build())
        }
        return items.toTypedArray()
    }

    fun Song.toMediaInfo(): MediaInfo? {
        val song = this
        val ipAddress = Utils.getIPAddress(true)
        val baseUrl: URL

        try {
            baseUrl = URL("http", ipAddress, Constants.CAST_SERVER_PORT, "")
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            return null
        }

        val songUrl = baseUrl.toString() + "/song?id=" + song.id
        val albumArtUrl = baseUrl.toString() + "/albumart?id=" + song.albumId

        val musicMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK)

        musicMetadata.putInt(Constants.CAST_MUSIC_METADATA_ID, song.id.toInt())
        musicMetadata.putInt(Constants.CAST_MUSIC_METADATA_ALBUM_ID, song.albumId.toInt())
        musicMetadata.putString(MediaMetadata.KEY_TITLE, song.title)
        musicMetadata.putString(MediaMetadata.KEY_ARTIST, song.artist)
        musicMetadata.putString(MediaMetadata.KEY_ALBUM_TITLE, song.album)
        musicMetadata.putInt(MediaMetadata.KEY_TRACK_NUMBER, song.trackNumber)
        musicMetadata.addImage(WebImage(Uri.parse(albumArtUrl)))

        return MediaInfo.Builder(songUrl)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("audio/mpeg")
                .setMetadata(musicMetadata)
                .setStreamDuration(song.duration.toLong())
                .build()
    }
}
