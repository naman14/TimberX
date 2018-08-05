package com.naman14.timberx

import android.content.ComponentName
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import androidx.appcompat.app.AppCompatActivity
import com.naman14.timberx.db.DbHelper

open class MediaBrowserActivity: AppCompatActivity() {

    private lateinit var mediaBrowser: MediaBrowserCompat

    private val mConnectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
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


    private fun saveCurrentData() {
        val queue = com.naman14.timberx.util.getMediaController(this)?.queue
        DbHelper.updateQueueSongs(this, adapter.songs!!, adapter.songs!![position].id)

    }

    open fun buildUIControls() {}


}