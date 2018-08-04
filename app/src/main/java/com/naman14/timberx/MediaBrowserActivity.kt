package com.naman14.timberx

import android.content.ComponentName
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import androidx.appcompat.app.AppCompatActivity
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.MediaMetadataCompat

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

    private var controllerCallback: MediaControllerCompat.Callback = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {}

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {}
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
        if (MediaControllerCompat.getMediaController(this) != null) {
            MediaControllerCompat.getMediaController(this).unregisterCallback(controllerCallback)
        }
        mediaBrowser.disconnect()

    }


    fun buildUIControls() {

    }


}