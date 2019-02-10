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
package com.naman14.timberx.playback.players

import android.app.Application
import android.app.PendingIntent
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM_ART
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ARTIST
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DURATION
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.ACTION_PAUSE
import android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY
import android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
import android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
import android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY_PAUSE
import android.support.v4.media.session.PlaybackStateCompat.ACTION_SET_REPEAT_MODE
import android.support.v4.media.session.PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE
import android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_NEXT
import android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ALL
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_NONE
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ONE
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_ALL
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_NONE
import android.support.v4.media.session.PlaybackStateCompat.STATE_NONE
import android.support.v4.media.session.PlaybackStateCompat.STATE_PAUSED
import android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING
import android.support.v4.media.session.PlaybackStateCompat.STATE_STOPPED
import androidx.core.net.toUri
import com.naman14.timberx.R
import com.naman14.timberx.constants.Constants.ACTION_REPEAT_QUEUE
import com.naman14.timberx.constants.Constants.ACTION_REPEAT_SONG
import com.naman14.timberx.constants.Constants.REPEAT_MODE
import com.naman14.timberx.constants.Constants.SHUFFLE_MODE
import com.naman14.timberx.db.QueueDao
import com.naman14.timberx.db.QueueEntity
import com.naman14.timberx.extensions.friendlyString
import com.naman14.timberx.extensions.isPlaying
import com.naman14.timberx.extensions.moveElement
import com.naman14.timberx.extensions.position
import com.naman14.timberx.extensions.toQueue
import com.naman14.timberx.extensions.toSongIDs
import com.naman14.timberx.models.Song
import com.naman14.timberx.repository.SongsRepository
import com.naman14.timberx.util.MusicUtils
import timber.log.Timber
import java.util.Random

typealias OnIsPlaying = SongPlayer.(playing: Boolean) -> Unit

/**
 * A wrapper around [MusicPlayer] that specifically manages playing [Song] and
 * manages the Queue for the background service.
 */
interface SongPlayer {

    fun setQueue(
        data: LongArray = LongArray(0),
        title: String = ""
    )

    fun getSession(): MediaSessionCompat

    fun playSong()

    fun playSong(id: Long)

    fun playSong(song: Song)

    fun seekTo(position: Int)

    fun pause()

    fun nextSong()

    fun repeatSong()

    fun repeatQueue()

    fun previousSong()

    fun playNext(id: Long)

    fun swapQueueSongs(from: Int, to: Int)

    fun removeFromQueue(id: Long)

    fun stop()

    fun release()

    fun onPlayingState(playing: OnIsPlaying)

    fun onPrepared(prepared: OnPrepared<SongPlayer>)

    fun onError(error: OnError<SongPlayer>)

    fun onCompletion(completion: OnCompletion<SongPlayer>)

    fun updatePlaybackState(applier: PlaybackStateCompat.Builder.() -> Unit)

    fun setPlaybackState(state: PlaybackStateCompat)

    fun restoreFromQueueData(queueData: QueueEntity)
}

class RealSongPlayer(
    private val context: Application,
    private val musicPlayer: MusicPlayer,
    private val songsRepository: SongsRepository,
    private val queueDao: QueueDao
) : SongPlayer {

    private var currentSongId: Long = -1
    private var isInitialized: Boolean = false
    private val shuffleRandom = Random()

    private var isPlayingCallback: OnIsPlaying = {}
    private var preparedCallback: OnPrepared<SongPlayer> = {}
    private var errorCallback: OnError<SongPlayer> = {}
    private var completionCallback: OnCompletion<SongPlayer> = {}

    private var currentQueue = LongArray(0)
    private var queueTitle = ""

    private var metadataBuilder = MediaMetadataCompat.Builder()
    private var stateBuilder = createDefaultPlaybackState()

    private var mediaSession = MediaSessionCompat(context, context.getString(R.string.app_name)).apply {
        setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        setCallback(MediaSessionCallback(this, this@RealSongPlayer, songsRepository, queueDao))
        setPlaybackState(stateBuilder.build())

        val sessionIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val sessionActivityPendingIntent = PendingIntent.getActivity(context, 0, sessionIntent, 0)
        setSessionActivity(sessionActivityPendingIntent)
        isActive = true
    }

    init {
        musicPlayer.onPrepared {
            preparedCallback(this@RealSongPlayer)
            playSong()
            seekTo(getSession().position().toInt())
        }

        musicPlayer.onCompletion {
            completionCallback(this@RealSongPlayer)
            val controller = getSession().controller
            when (controller.repeatMode) {
                REPEAT_MODE_ONE -> {
                    controller.transportControls.sendCustomAction(ACTION_REPEAT_SONG, null)
                }
                REPEAT_MODE_ALL -> {
                    controller.transportControls.sendCustomAction(ACTION_REPEAT_QUEUE, null)
                }
                else -> controller.transportControls.skipToNext()
            }
        }
    }

    override fun setQueue(
        data: LongArray,
        title: String
    ) {
        Timber.d("""setQueue: ${data.friendlyString()} ("$title"))""")
        this.currentQueue = data
        this.queueTitle = if (title.isEmpty()) {
            context.getString(R.string.all_songs)
        } else {
            title
        }

        mediaSession.apply {
            setQueue(currentQueue.toQueue(songsRepository))
            setQueueTitle(queueTitle)
        }
    }

    override fun getSession(): MediaSessionCompat = mediaSession

    override fun playSong() {
        Timber.d("playSong()")
        if (currentSongId.toInt() == -1) {
            setLastCurrentId()
        }

        if (isInitialized) {
            updatePlaybackState {
                setState(STATE_PLAYING, mediaSession.position(), 1F)
            }
            musicPlayer.play()
            return
        }
        musicPlayer.reset()

        val path = MusicUtils.getSongUri(currentSongId).toString()
        val isSourceSet = if (path.startsWith("content://")) {
            musicPlayer.setSource(path.toUri())
        } else {
            musicPlayer.setSource(path)
        }
        if (isSourceSet) {
            isInitialized = true
            musicPlayer.prepare()
        }
    }

    override fun playSong(id: Long) {
        Timber.d("playSong(): $id")
        val song = songsRepository.getSongForId(id)
        playSong(song)
    }

    override fun playSong(song: Song) {
        Timber.d("playSong(): ${song.title}")
        if (currentSongId != song.id) {
            currentSongId = song.id
            isInitialized = false
            updatePlaybackState {
                setState(STATE_STOPPED, 0, 1F)
            }
        }
        setMetaData(song)
        playSong()
    }

    override fun seekTo(position: Int) {
        Timber.d("seekTo(): $position")
        if (isInitialized) {
            musicPlayer.seekTo(position)
            updatePlaybackState {
                setState(
                        mediaSession.controller.playbackState.state,
                        position.toLong(),
                        1F
                )
            }
        }
    }

    override fun pause() {
        Timber.d("pause()")
        if (musicPlayer.isPlaying() && isInitialized) {
            musicPlayer.pause()
            updatePlaybackState {
                setState(STATE_PAUSED, mediaSession.position(), 1F)
            }
        }
    }

    override fun nextSong() {
        Timber.d("nextSong()")
        val currentIndex = currentQueue.indexOf(currentSongId)
        if (currentIndex + 1 < currentQueue.size) {
            val nextSongIndex = if (mediaSession.controller.shuffleMode == SHUFFLE_MODE_ALL) {
                shuffleRandom.nextInt(currentQueue.size - 1)
            } else {
                currentIndex + 1
            }
            playSong(currentQueue[nextSongIndex])
        } else {
            // reached end of queue, pause player
            // note that repeat queue would have already been handled in onCustomAction
            pause()
        }
    }

    override fun repeatSong() {
        Timber.d("repeatSong()")
        updatePlaybackState {
            setState(STATE_STOPPED, 0, 1F)
        }
        playSong(currentSongId)
    }

    override fun repeatQueue() {
        Timber.d("repeatQueue()")
        if (currentSongId == currentQueue.last())
            playSong(currentQueue.first())
        else {
            nextSong()
        }
    }

    override fun previousSong() {
        Timber.d("previousSong()")
        val currentIndex = currentQueue.indexOf(currentSongId) - 1
        if (currentIndex >= 0) {
            playSong(currentQueue[currentIndex])
        }
    }

    override fun playNext(id: Long) {
        Timber.d("playNext(): $id")
        val list = mutableListOf<Long>().apply {
            addAll(currentQueue.asList())
            remove(id)
            add(currentQueue.indexOf(currentSongId) + 1, id)
        }
        currentQueue = list.toLongArray()
        mediaSession.setQueue(currentQueue.toQueue(songsRepository))
    }

    override fun swapQueueSongs(from: Int, to: Int) {
        Timber.d("swapQueueSongs(): $from -> $to")
        currentQueue = currentQueue.asList().moveElement(from, to).toLongArray()
        mediaSession.setQueue(currentQueue.toQueue(songsRepository))
    }

    override fun removeFromQueue(id: Long) {
        Timber.d("removeFromQueue(): $id")
        val list = currentQueue.toMutableList().apply {
            remove(id)
        }
        currentQueue = list.toLongArray()
        mediaSession.setQueue(currentQueue.toQueue(songsRepository))
    }

    override fun stop() {
        Timber.d("stop()")
        musicPlayer.stop()
        updatePlaybackState {
            setState(STATE_NONE, 0, 1F)
        }
    }

    override fun release() {
        Timber.d("release()")
        mediaSession.apply {
            isActive = false
            release()
        }
        musicPlayer.release()
    }

    override fun onPlayingState(playing: OnIsPlaying) {
        this.isPlayingCallback = playing
    }

    override fun onPrepared(prepared: OnPrepared<SongPlayer>) {
        this.preparedCallback = prepared
    }

    override fun onError(error: OnError<SongPlayer>) {
        this.errorCallback = error
        musicPlayer.onError { throwable ->
            errorCallback(this@RealSongPlayer, throwable)
        }
    }

    override fun onCompletion(completion: OnCompletion<SongPlayer>) {
        this.completionCallback = completion
    }

    override fun updatePlaybackState(applier: PlaybackStateCompat.Builder.() -> Unit) {
        applier(stateBuilder)
        setPlaybackState(stateBuilder.build())
    }

    override fun setPlaybackState(state: PlaybackStateCompat) {
        mediaSession.setPlaybackState(state)
        state.extras?.let { bundle ->
            mediaSession.setRepeatMode(bundle.getInt(REPEAT_MODE))
            mediaSession.setShuffleMode(bundle.getInt(SHUFFLE_MODE))
        }
        if (state.isPlaying) {
            isPlayingCallback(this, true)
        } else {
            isPlayingCallback(this, false)
        }
    }

    override fun restoreFromQueueData(queueData: QueueEntity) {
        this.currentSongId = queueData.currentId ?: -1
        val playbackState = queueData.playState ?: STATE_NONE
        val currentPos = queueData.currentSeekPos ?: 0
        val repeatMode = queueData.repeatMode ?: REPEAT_MODE_NONE
        val shuffleMode = queueData.shuffleMode ?: SHUFFLE_MODE_NONE

        val queueIds = queueDao.getQueueSongsSync().toSongIDs()
        setQueue(queueIds, queueData.queueTitle)
        setMetaData(songsRepository.getSongForId(currentSongId))

        val extras = Bundle().apply {
            putInt(REPEAT_MODE, repeatMode)
            putInt(SHUFFLE_MODE, shuffleMode)
        }
        updatePlaybackState {
            setState(playbackState, currentPos, 1F)
            setExtras(extras)
        }
    }

    private fun setMetaData(song: Song) {
        // TODO make music utils injectable
        val artwork = MusicUtils.getAlbumArtBitmap(context, song.albumId)
        val mediaMetadata = metadataBuilder.apply {
            putString(METADATA_KEY_ALBUM, song.album)
            putString(METADATA_KEY_ARTIST, song.artist)
            putString(METADATA_KEY_TITLE, song.title)
            putString(METADATA_KEY_ALBUM_ART_URI, song.albumId.toString())
            putBitmap(METADATA_KEY_ALBUM_ART, artwork)
            putString(METADATA_KEY_MEDIA_ID, song.id.toString())
            putLong(METADATA_KEY_DURATION, song.duration.toLong())
        }.build()
        mediaSession.setMetadata(mediaMetadata)
    }

    private fun setLastCurrentId() {
        val queueData = queueDao.getQueueDataSync()
        currentSongId = queueData?.currentId ?: 0
        Timber.d("setLastCurrentId(): $currentSongId")
    }
}

private fun createDefaultPlaybackState(): PlaybackStateCompat.Builder {
    return PlaybackStateCompat.Builder().setActions(
            ACTION_PLAY
                    or ACTION_PAUSE
                    or ACTION_PLAY_FROM_SEARCH
                    or ACTION_PLAY_FROM_MEDIA_ID
                    or ACTION_PLAY_PAUSE
                    or ACTION_SKIP_TO_NEXT
                    or ACTION_SKIP_TO_PREVIOUS
                    or ACTION_SET_SHUFFLE_MODE
                    or ACTION_SET_REPEAT_MODE)
            .setState(STATE_NONE, 0, 1f)
}
