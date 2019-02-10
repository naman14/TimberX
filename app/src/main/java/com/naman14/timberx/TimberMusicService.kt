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
package com.naman14.timberx

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY
import android.media.AudioManager.STREAM_MUSIC
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager.PARTIAL_WAKE_LOCK
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM_ART
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ARTIST
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DURATION
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
import android.support.v4.media.session.MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
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
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ONE
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_ALL
import android.support.v4.media.session.PlaybackStateCompat.STATE_NONE
import android.support.v4.media.session.PlaybackStateCompat.STATE_PAUSED
import android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.naman14.timberx.constants.Constants
import com.naman14.timberx.constants.Constants.ACTION_NEXT
import com.naman14.timberx.constants.Constants.ACTION_PLAY_NEXT
import com.naman14.timberx.constants.Constants.ACTION_PREVIOUS
import com.naman14.timberx.constants.Constants.ACTION_QUEUE_REORDER
import com.naman14.timberx.constants.Constants.ACTION_REPEAT_QUEUE
import com.naman14.timberx.constants.Constants.ACTION_REPEAT_SONG
import com.naman14.timberx.constants.Constants.ACTION_RESTORE_MEDIA_SESSION
import com.naman14.timberx.constants.Constants.ACTION_SET_MEDIA_STATE
import com.naman14.timberx.constants.Constants.ACTION_SONG_DELETED
import com.naman14.timberx.constants.Constants.APP_PACKAGE_NAME
import com.naman14.timberx.constants.Constants.QUEUE_FROM
import com.naman14.timberx.constants.Constants.QUEUE_TO
import com.naman14.timberx.constants.Constants.REPEAT_MODE
import com.naman14.timberx.constants.Constants.SHUFFLE_MODE
import com.naman14.timberx.constants.Constants.SONG
import com.naman14.timberx.db.QueueDao
import com.naman14.timberx.db.QueueEntity
import com.naman14.timberx.db.QueueHelper
import com.naman14.timberx.extensions.isPlayEnabled
import com.naman14.timberx.extensions.isPlaying
import com.naman14.timberx.extensions.moveElement
import com.naman14.timberx.extensions.position
import com.naman14.timberx.extensions.toIDList
import com.naman14.timberx.extensions.toQueue
import com.naman14.timberx.extensions.toRawMediaItems
import com.naman14.timberx.extensions.toSongIDs
import com.naman14.timberx.models.MediaID
import com.naman14.timberx.models.MediaID.Companion.CALLER_OTHER
import com.naman14.timberx.models.MediaID.Companion.CALLER_SELF
import com.naman14.timberx.models.Song
import com.naman14.timberx.notifications.Notifications
import com.naman14.timberx.repository.AlbumRepository
import com.naman14.timberx.repository.ArtistRepository
import com.naman14.timberx.repository.GenreRepository
import com.naman14.timberx.repository.PlaylistRepository
import com.naman14.timberx.repository.SongsRepository
import com.naman14.timberx.util.MusicUtils
import com.naman14.timberx.util.MusicUtils.getSongUri
import com.naman14.timberx.util.Utils.EMPTY_ALBUM_ART_URI
import org.koin.android.ext.android.inject
import org.koin.standalone.KoinComponent
import java.util.Random
import timber.log.Timber.d as log

// TODO pull out media logic to separate class to make this more readable
class TimberMusicService : MediaBrowserServiceCompat(), MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, KoinComponent {

    companion object {
        const val MEDIA_ID_ARG = "MEDIA_ID"
        const val MEDIA_TYPE_ARG = "MEDIA_TYPE"
        const val MEDIA_CALLER = "MEDIA_CALLER"
        const val MEDIA_ID_ROOT = -1
        const val TYPE_ALL_ARTISTS = 0
        const val TYPE_ALL_ALBUMS = 1
        const val TYPE_ALL_SONGS = 2
        const val TYPE_ALL_PLAYLISTS = 3
        const val TYPE_SONG = 9
        const val TYPE_ALBUM = 10
        const val TYPE_ARTIST = 11
        const val TYPE_PLAYLIST = 12
        const val TYPE_ALL_FOLDERS = 13
        const val TYPE_ALL_GENRES = 14
        const val TYPE_GENRE = 15

        const val NOTIFICATION_ID = 888
    }

    private val notifications by inject<Notifications>()
    private val albumRepository by inject<AlbumRepository>()
    private val artistRepository by inject<ArtistRepository>()
    private val songsRepository by inject<SongsRepository>()
    private val genreRepository by inject<GenreRepository>()
    private val playlistRepository by inject<PlaylistRepository>()

    private val queueHelper by inject<QueueHelper>()
    private val queueDao by inject<QueueDao>()

    private var mCurrentSongId: Long = -1
    private var isPlaying = false
    private var isInitialized = false
    private var isStarted = false

    private lateinit var currentQueue: LongArray
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private lateinit var metadataBuilder: MediaMetadataCompat.Builder
    private lateinit var queueTitle: String
    private lateinit var becomingNoisyReceiver: BecomingNoisyReceiver

    private var player: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        log("onCreate()")

        setUpMediaSession()
        mediaSession.setFlags(FLAG_HANDLES_MEDIA_BUTTONS or FLAG_HANDLES_TRANSPORT_CONTROLS)

        stateBuilder = PlaybackStateCompat.Builder().setActions(
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
        mediaSession.setPlaybackState(stateBuilder.build())

        val sessionIntent = packageManager?.getLaunchIntentForPackage(packageName)
        val sessionActivityPendingIntent = PendingIntent.getActivity(this, 0, sessionIntent, 0)

        mediaSession.setSessionActivity(sessionActivityPendingIntent)
        mediaSession.isActive = true

        sessionToken = mediaSession.sessionToken

        becomingNoisyReceiver =
                BecomingNoisyReceiver(context = this, sessionToken = mediaSession.sessionToken)

        metadataBuilder = MediaMetadataCompat.Builder()

        currentQueue = LongArray(0)
        queueTitle = "All songs"
        mediaSession.run {
            if (ContextCompat.checkSelfPermission(this@TimberMusicService,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                setQueue(currentQueue.toQueue(songsRepository))
                setQueueTitle(queueTitle)
            }
        }

        initPlayer()
    }

    private fun setUpMediaSession() {
        mediaSession = MediaSessionCompat(this, "TimberX")
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPause() {
                pause()
            }

            override fun onPlay() {
                if (!isStarted) {
                    startService()
                }
                playSong()
            }

            override fun onPlayFromSearch(query: String?, extras: Bundle?) {
                query?.let {
                    val song = songsRepository.searchSongs(query, 1)
                    if (song.isNotEmpty()) {
                        if (!isStarted) {
                            startService()
                        }
                        playSong(song[0])
                    }
                } ?: onPlay()
            }

            override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
                val songID = MediaID().fromString(mediaId!!).mediaId!!.toLong()
                if (!isStarted) {
                    startService()
                }

                setPlaybackState(stateBuilder.setState(mediaSession.controller.playbackState.state,
                        0, 1F).build())
                playSong(songID)

                if (extras == null) {
                    return
                }
                val queue = extras.getLongArray(Constants.SONGS_LIST)
                val seekTo = extras.getInt(Constants.SEEK_TO_POS)
                val queueTitle = extras.getString(Constants.QUEUE_TITLE)
                queue?.let { currentQueue = it }
                queueTitle?.let { this@TimberMusicService.queueTitle = it }
                setPlaybackState(stateBuilder.setState(mediaSession.controller.playbackState.state,
                        seekTo.toLong(), 1F).build())
                mediaSession.setQueue(currentQueue.toQueue(songsRepository))
                mediaSession.setQueueTitle(this@TimberMusicService.queueTitle)
            }

            override fun onSeekTo(pos: Long) {
                if (isInitialized) {
                    player?.seekTo(pos.toInt())
                    setPlaybackState(stateBuilder.setState(mediaSession.controller.playbackState.state,
                            pos, 1F).build())
                }
            }

            override fun onSkipToNext() {
                val currentIndex = currentQueue.indexOf(mCurrentSongId)
                if (currentIndex + 1 < currentQueue.size) {
                    val nextSongIndex = if (mediaSession.controller.shuffleMode == SHUFFLE_MODE_ALL) {
                        Random().nextInt(currentQueue.size - 1)
                    } else {
                        currentIndex + 1
                    }
                    onPlayFromMediaId(MediaID(TYPE_SONG.toString(),
                            currentQueue[nextSongIndex].toString()).asString(), null)
                } else {
                    // reached end of queue, pause player
                    // note that repeat queue would have already been handled in onCustomAction
                    onPause()
                }
            }

            override fun onSkipToPrevious() {
                val currentIndex = currentQueue.indexOf(mCurrentSongId)
                if (currentIndex - 1 >= 0) {
                    onPlayFromMediaId(MediaID(TYPE_SONG.toString(),
                            currentQueue[currentIndex - 1].toString()).asString(), null)
                }
            }

            override fun onStop() {
                player?.stop()
                setPlaybackState(stateBuilder.setState(STATE_NONE, 0, 1F).build())
                notifications.updateNotification(mediaSession)
                stopService()
            }

            override fun onSetRepeatMode(repeatMode: Int) {
                super.onSetRepeatMode(repeatMode)
                val bundle = mediaSession.controller.playbackState.extras ?: Bundle()
                setPlaybackState(PlaybackStateCompat.Builder(mediaSession.controller.playbackState)
                        .setExtras(bundle.apply {
                            putInt(REPEAT_MODE, repeatMode)
                        }
                        ).build())
            }

            override fun onSetShuffleMode(shuffleMode: Int) {
                super.onSetShuffleMode(shuffleMode)
                val bundle = mediaSession.controller.playbackState.extras ?: Bundle()
                setPlaybackState(PlaybackStateCompat.Builder(mediaSession.controller.playbackState)
                        .setExtras(bundle.apply {
                            putInt(SHUFFLE_MODE, shuffleMode)
                        }).build())
            }

            override fun onCustomAction(action: String?, extras: Bundle?) {
                when (action) {
                    ACTION_SET_MEDIA_STATE -> setSavedMediaSessionState()

                    ACTION_REPEAT_SONG -> {
                        onPlayFromMediaId(MediaID(TYPE_SONG.toString(),
                                mCurrentSongId.toString()).asString(), null)
                    }

                    ACTION_REPEAT_QUEUE -> {
                        if (mCurrentSongId == currentQueue[currentQueue.size - 1])
                            onPlayFromMediaId(MediaID(TYPE_SONG.toString(),
                                    currentQueue[0].toString()).asString(), null)
                        else onSkipToNext()
                    }

                    ACTION_PLAY_NEXT -> {
                        val nextSongId = extras!!.getLong(SONG)
                        val list = arrayListOf<Long>().apply {
                            addAll(currentQueue.asList())
                            remove(nextSongId)
                            add(currentQueue.indexOf(mCurrentSongId), nextSongId)
                        }
                        currentQueue = list.toLongArray()
                        mediaSession.setQueue(currentQueue.toQueue(songsRepository))
                    }

                    ACTION_QUEUE_REORDER -> {
                        val from = extras!!.getInt(QUEUE_FROM)
                        val to = extras.getInt(QUEUE_TO)

                        currentQueue = currentQueue.asList().moveElement(from, to).toLongArray()
                        mediaSession.setQueue(currentQueue.toQueue(songsRepository))
                    }

                    ACTION_SONG_DELETED -> {
                        //remove song from current queue if deleted
                        val list = arrayListOf<Long>().apply {
                            addAll(currentQueue.asList())
                            remove(extras!!.getLong(SONG))
                        }
                        currentQueue = list.toLongArray()
                        mediaSession.setQueue(currentQueue.toQueue(songsRepository))
                    }

                    ACTION_RESTORE_MEDIA_SESSION -> {
                        restoreMediaSession()
                    }
                }
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log("onStartCommand(): ${intent?.action}")
        intent?.let {
            when (intent.action) {
                Constants.ACTION_PLAY_PAUSE -> {
                    mediaSession.controller.playbackState?.let { playbackState ->
                        when {
                            playbackState.isPlaying -> mediaSession.controller.transportControls.pause()
                            playbackState.isPlayEnabled -> mediaSession.controller.transportControls.play()
                        }
                    }
                }
                ACTION_NEXT -> {
                    mediaSession.controller.transportControls.skipToNext()
                }
                ACTION_PREVIOUS -> {
                    mediaSession.controller.transportControls.skipToPrevious()
                }
                else -> Unit
            }
        }
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return START_STICKY
    }

    override fun onDestroy() {
        log("onDestroy()")
        saveCurrentData()
        mediaSession.run {
            isActive = false
            release()
        }
        player?.release()
        player = null
        super.onDestroy()
    }

    override fun onPrepared(player: MediaPlayer?) {
        log("onPrepared()")
        isPlaying = true
        player?.run {
            start()
            seekTo(mediaSession.position().toInt())
        }
        setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                mediaSession.position(), 1F).build())
        notifications.updateNotification(mediaSession)
    }

    override fun onCompletion(player: MediaPlayer?) {
        log("onCompletion()")
        when (mediaSession.controller.repeatMode) {
            REPEAT_MODE_ONE -> {
                mediaSession.controller.transportControls.sendCustomAction(ACTION_REPEAT_SONG, null)
            }
            REPEAT_MODE_ALL -> {
                mediaSession.controller.transportControls.sendCustomAction(ACTION_REPEAT_QUEUE, null)
            }
            else -> {
                mediaSession.controller.transportControls.skipToNext()
            }
        }
    }

    override fun onError(player: MediaPlayer?, p1: Int, p2: Int): Boolean {
        log("onError(): $p1, $p2")
        isPlaying = false
        return false
    }

    private fun setPlaybackState(playbackStateCompat: PlaybackStateCompat) {
        mediaSession.setPlaybackState(playbackStateCompat)
        playbackStateCompat.extras?.let { bundle ->
            mediaSession.setRepeatMode(bundle.getInt(REPEAT_MODE))
            mediaSession.setShuffleMode(bundle.getInt(SHUFFLE_MODE))
        }
        if (playbackStateCompat.isPlaying) becomingNoisyReceiver.register()
        else becomingNoisyReceiver.unregister()
    }

    private fun setMetaData(song: Song) {
        // TODO should these be in a coroutine?
        val artwork = MusicUtils.getAlbumArtBitmap(this, song.albumId)
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

    //media browser
    override fun onLoadChildren(parentId: String, result: Result<List<MediaBrowserCompat.MediaItem>>) {
        result.detach()
        loadChildren(parentId, result)
    }

    @Nullable
    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): MediaBrowserServiceCompat.BrowserRoot? {
        val caller = if (clientPackageName == APP_PACKAGE_NAME) {
            CALLER_SELF
        } else {
            CALLER_OTHER
        }
        return MediaBrowserServiceCompat.BrowserRoot(MediaID(MEDIA_ID_ROOT.toString(), null, caller).asString(), null)
    }

    private fun addMediaRoots(mMediaRoot: MutableList<MediaBrowserCompat.MediaItem>, caller: String) {
        mMediaRoot.add(MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder().apply {
                    setMediaId(MediaID(TYPE_ALL_ARTISTS.toString(), null, caller).asString())
                    setTitle(getString(R.string.artists))
                    setIconUri(EMPTY_ALBUM_ART_URI.toUri())
                    setSubtitle(getString(R.string.artists))
                }.build(), FLAG_BROWSABLE
        ))

        mMediaRoot.add(MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder().apply {
                    setMediaId(MediaID(TYPE_ALL_ALBUMS.toString(), null, caller).asString())
                    setTitle(getString(R.string.albums))
                    setIconUri(EMPTY_ALBUM_ART_URI.toUri())
                    setSubtitle(getString(R.string.albums))
                }.build(), FLAG_BROWSABLE
        ))

        mMediaRoot.add(MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder().apply {
                    setMediaId(MediaID(TYPE_ALL_SONGS.toString(), null, caller).asString())
                    setTitle(getString(R.string.songs))
                    setIconUri(EMPTY_ALBUM_ART_URI.toUri())
                    setSubtitle(getString(R.string.songs))
                }.build(), FLAG_BROWSABLE
        ))

        mMediaRoot.add(MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder().apply {
                    setMediaId(MediaID(TYPE_ALL_PLAYLISTS.toString(), null, caller).asString())
                    setTitle(getString(R.string.playlists))
                    setIconUri(EMPTY_ALBUM_ART_URI.toUri())
                    setSubtitle(getString(R.string.playlists))
                }.build(), FLAG_BROWSABLE
        ))

        mMediaRoot.add(MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder().apply {
                    setMediaId(MediaID(TYPE_ALL_GENRES.toString(), null, caller).asString())
                    setTitle(getString(R.string.genres))
                    setIconUri(EMPTY_ALBUM_ART_URI.toUri())
                    setSubtitle(getString(R.string.genres))
                }.build(), FLAG_BROWSABLE
        ))
    }

    private fun loadChildren(parentId: String, result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>) {

        val mediaItems = ArrayList<MediaBrowserCompat.MediaItem>()
        val mediaIdParent = MediaID().fromString(parentId)

        val mediaType = mediaIdParent.type
        val mediaId = mediaIdParent.mediaId
        val caller = mediaIdParent.caller

        if (mediaType == MEDIA_ID_ROOT.toString()) {
            addMediaRoots(mediaItems, caller!!)
        } else {
            when (mediaType?.toInt() ?: 0) {
                TYPE_ALL_ARTISTS -> {
                    mediaItems.addAll(artistRepository.getAllArtists(caller))
                }
                TYPE_ALL_ALBUMS -> {
                    mediaItems.addAll(albumRepository.getAllAlbums(caller))
                }
                TYPE_ALL_SONGS -> {
                    mediaItems.addAll(songsRepository.loadSongs(caller))
                }
                TYPE_ALL_GENRES -> {
                    mediaItems.addAll(genreRepository.getAllGenres(caller))
                }
                TYPE_ALL_PLAYLISTS -> {
                    mediaItems.addAll(playlistRepository.getPlaylists(caller))
                }
                TYPE_ALBUM -> {
                    mediaId?.let {
                        mediaItems.addAll(albumRepository.getSongsForAlbum(it.toLong(), caller))
                    }
                }
                TYPE_ARTIST -> {
                    mediaId?.let {
                        mediaItems.addAll(artistRepository.getSongsForArtist(it.toLong(), caller))
                    }
                }
                TYPE_PLAYLIST -> {
                    mediaId?.let {
                        mediaItems.addAll(playlistRepository.getSongsInPlaylist(it.toLong(), caller))
                    }
                }
                TYPE_GENRE -> {
                    mediaId?.let {
                        mediaItems.addAll(genreRepository.getSongsForGenre(it.toLong(), caller))
                    }
                }
            }
        }
        if (caller == CALLER_SELF) {
            result.sendResult(mediaItems)
        } else {
            result.sendResult(mediaItems.toRawMediaItems())
        }
    }

    private fun setLastCurrentID() {
        val queueData = queueDao.getQueueDataSync()
        mCurrentSongId = queueData?.currentId ?: 0
        log("setLastCurrentID(): $mCurrentSongId")
    }

    private fun setSavedMediaSessionState() {
        //only set saved session from db if we know there is not any active media session
        if (mediaSession.controller.playbackState == null ||
                mediaSession.controller.playbackState.state == STATE_NONE) {
            val queueData = queueDao.getQueueDataSync()
            queueData?.let {
                queueTitle = it.queueTitle
                val queue = queueDao.getQueueSongsSync()
                queue.toSongIDs().also { queueIDs ->
                    mediaSession.setQueue(queueIDs.toQueue(songsRepository))
                    currentQueue = queueIDs
                }
                mCurrentSongId = queueData.currentId!!
                queueData.currentId?.let {
                    setMetaData(songsRepository.getSongForId(queueData.currentId!!))
                    setPlaybackState(stateBuilder.setState(queueData.playState!!,
                            queueData.currentSeekPos!!, 1F).setExtras(
                            Bundle().apply {
                                putInt(REPEAT_MODE, queueData.repeatMode!!)
                                putInt(SHUFFLE_MODE, queueData.shuffleMode!!)
                            }
                    ).build())
                }
            }
        } else {
            //force update the playback state and metadata from the mediasession so that the attached observer in NowPlayingViewModel gets the current state
            restoreMediaSession()
        }
    }

    private fun restoreMediaSession() {
        log("restoreMediaSession()")
        setPlaybackState(mediaSession.controller.playbackState)
        mediaSession.setMetadata(mediaSession.controller.metadata)
    }

    private fun saveCurrentData() {
        if (mediaSession.controller == null ||
                mediaSession.controller.playbackState == null ||
                mediaSession.controller.playbackState.state == STATE_NONE) {
            return
        }
        log("saveCurrentData()")

        val mediaController = mediaSession.controller
        val queue = mediaController.queue
        val currentId = mediaController.metadata?.getString(METADATA_KEY_MEDIA_ID)

        queueHelper.updateQueueSongs(queue?.toIDList(), currentId?.toLong())

        val queueEntity = QueueEntity().apply {
            this.currentId = currentId?.toLong()
            currentSeekPos = mediaController?.playbackState?.position
            repeatMode = mediaController?.repeatMode
            shuffleMode = mediaController?.shuffleMode
            playState = mediaController?.playbackState?.state
            queueTitle = mediaController?.queueTitle?.toString() ?: "All songs"
        }

        queueHelper.updateQueueData(queueEntity)
    }

    private fun startService() {
        log("startService()")
        if (!isStarted) {
            val intent = Intent(this, TimberMusicService::class.java)
            startService(intent)
            startForeground(NOTIFICATION_ID, notifications.buildNotification(mediaSession))
            isStarted = true
        }
    }

    private fun stopService() {
        log("stopService()")
        saveCurrentData()
        if (isStarted) {
            stopSelf()
            isStarted = false
        }
    }

    private fun initPlayer() {
        log("initPlayer()")
        player = MediaPlayer().apply {
            setWakeMode(applicationContext, PARTIAL_WAKE_LOCK)
            // TODO replace with non-deprecated method usage
            setAudioStreamType(STREAM_MUSIC)
            setOnPreparedListener(this@TimberMusicService)
            setOnCompletionListener(this@TimberMusicService)
            setOnErrorListener(this@TimberMusicService)
        }
    }

    fun playSong(id: Long) {
        val song = songsRepository.getSongForId(id)
        playSong(song)
    }

    fun playSong(song: Song) {
        log("playSong(): $song")
        if (mCurrentSongId != song.id) {
            mCurrentSongId = song.id
            isInitialized = false
        }
        setMetaData(song)
        playSong()
    }

    fun playSong() {
        log("playSong()")
        if (mCurrentSongId.toInt() == -1) {
            setLastCurrentID()
        }

        if (isInitialized) {
            setPlaybackState(stateBuilder.setState(STATE_PLAYING, mediaSession.position(), 1F).build())
            notifications.updateNotification(mediaSession)
            player?.start()
            return
        }

        player?.reset()
        val path = getSongUri(mCurrentSongId).toString()
        try {
            if (path.startsWith("content://")) {
                player?.setDataSource(this, Uri.parse(path))
            } else {
                player?.setDataSource(path)
            }
            isInitialized = true
            player?.prepareAsync()
        } catch (e: Exception) {
            log("Unable to set data source")
            Toast.makeText(this, R.string.play_song_error, Toast.LENGTH_SHORT).show()
        }
    }

    fun pause() {
        log("pause()")
        if (isPlaying && isInitialized) {
            player?.pause()
            setPlaybackState(stateBuilder.setState(STATE_PAUSED,
                    mediaSession.position(), 1F).build())
            notifications.updateNotification(mediaSession)
            stopForeground(false)
            saveCurrentData()
        }
    }

    fun position(): Int = player?.currentPosition ?: 0

    /**
     * Helper class for listening for when headphones are unplugged (or the audio
     * will otherwise cause playback to become "noisy").
     */
    private class BecomingNoisyReceiver(private val context: Context, sessionToken: MediaSessionCompat.Token)
        : BroadcastReceiver() {

        private val noisyIntentFilter = IntentFilter(ACTION_AUDIO_BECOMING_NOISY)
        private val controller = MediaControllerCompat(context, sessionToken)

        private var registered = false

        fun register() {
            if (!registered) {
                context.registerReceiver(this, noisyIntentFilter)
                registered = true
            }
        }

        fun unregister() {
            if (registered) {
                context.unregisterReceiver(this)
                registered = false
            }
        }

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_AUDIO_BECOMING_NOISY) {
                controller.transportControls.pause()
            }
        }
    }
}
