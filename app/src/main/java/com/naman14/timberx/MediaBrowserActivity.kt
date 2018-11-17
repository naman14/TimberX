package com.naman14.timberx

import android.content.ComponentName
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.appcompat.app.AppCompatActivity
import com.naman14.timberx.db.DbHelper
import com.naman14.timberx.db.QueueEntity
import com.naman14.timberx.util.toIDList

open class MediaBrowserActivity: AppCompatActivity() {

    private lateinit var mediaBrowser: MediaBrowserCompat

    private val mConnectionCallbacks: MediaBrowserCompat.ConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            val token = mediaBrowser.sessionToken
            val mediaController = MediaControllerCompat(this@MediaBrowserActivity, // Context
                    token)
            MediaControllerCompat.setMediaController(this@MediaBrowserActivity, mediaController)
            buildUIControls()
        }

        override fun onConnectionSuspended() {
            // The Service has crashed. Disable transport controls until it automatically reconnects
        }

        override fun onConnectionFailed() {
            // The Service has refused our connection
        }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaBrowser = MediaBrowserCompat(this,
                ComponentName(this, TimberMusicService::class.java),
                mConnectionCallbacks,
                null)

    }

    override fun onStart() {
        super.onStart()
        mediaBrowser.connect()
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC

    }

    override fun onStop() {
        super.onStop()
        saveCurrentData()
        mediaBrowser.disconnect()
    }


    fun getMediaBrowser(): MediaBrowserCompat {
        return mediaBrowser
    }

    private fun saveCurrentData() {
        val mediaController = com.naman14.timberx.util.getMediaController(this)
        val queue = mediaController?.queue
        val currentId = mediaController?.metadata?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)

        DbHelper.updateQueueSongs(this, queue?.toIDList(), currentId?.toLong())

        val queueEntity = QueueEntity()
        queueEntity.currentId = currentId?.toLong()
        queueEntity.currentSeekPos = mediaController?.playbackState?.position
        queueEntity.repeatMode = mediaController?.repeatMode
        queueEntity.shuffleMode = mediaController?.shuffleMode
        queueEntity.playState = mediaController?.playbackState?.state

        DbHelper.updateQueueData(this, queueEntity)
    }

    open fun buildUIControls() {}

}