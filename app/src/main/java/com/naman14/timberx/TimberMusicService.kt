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

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY
import android.media.AudioManager.STREAM_MUSIC
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager.PARTIAL_WAKE_LOCK
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
import android.support.v4.media.session.MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import androidx.annotation.Nullable
import androidx.core.net.toUri
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.naman14.timberx.db.DbHelper
import com.naman14.timberx.db.QueueEntity
import com.naman14.timberx.db.TimberDatabase
import com.naman14.timberx.models.MediaID
import com.naman14.timberx.models.MediaID.Companion.CALLER_OTHER
import com.naman14.timberx.models.MediaID.Companion.CALLER_SELF
import com.naman14.timberx.models.Song
import com.naman14.timberx.repository.AlbumRepository.getAllAlbums
import com.naman14.timberx.repository.AlbumRepository.getSongsForAlbum
import com.naman14.timberx.repository.ArtistRepository.getAllArtists
import com.naman14.timberx.repository.ArtistRepository.getSongsForArtist
import com.naman14.timberx.repository.GenreRepository.getAllGenres
import com.naman14.timberx.repository.GenreRepository.getSongsForGenre
import com.naman14.timberx.repository.PlaylistRepository.getPlaylists
import com.naman14.timberx.repository.PlaylistRepository.getSongsInPlaylist
import com.naman14.timberx.repository.SongsRepository
import com.naman14.timberx.repository.SongsRepository.getSongForId
import com.naman14.timberx.repository.SongsRepository.loadSongs
import com.naman14.timberx.util.*
import com.naman14.timberx.util.Constants.ACTION_NEXT
import com.naman14.timberx.util.Constants.ACTION_PLAY_NEXT
import com.naman14.timberx.util.Constants.ACTION_PREVIOUS
import com.naman14.timberx.util.Constants.ACTION_QUEUE_REORDER
import com.naman14.timberx.util.Constants.ACTION_REPEAT_QUEUE
import com.naman14.timberx.util.Constants.ACTION_REPEAT_SONG
import com.naman14.timberx.util.Constants.ACTION_RESTORE_MEDIA_SESSION
import com.naman14.timberx.util.Constants.ACTION_SET_MEDIA_STATE
import com.naman14.timberx.util.Constants.ACTION_SONG_DELETED
import com.naman14.timberx.util.Constants.APP_PACKAGE_NAME
import com.naman14.timberx.util.Constants.QUEUE_FROM
import com.naman14.timberx.util.Constants.QUEUE_TO
import com.naman14.timberx.util.Constants.REPEAT_MODE
import com.naman14.timberx.util.Constants.SHUFFLE_MODE
import com.naman14.timberx.util.Constants.SONG
import com.naman14.timberx.util.MusicUtils.getSongUri
import com.naman14.timberx.util.NotificationUtils.buildNotification
import com.naman14.timberx.util.NotificationUtils.updateNotification
import com.naman14.timberx.util.Utils.getEmptyAlbumArtUri
import com.naman14.timberx.util.media.isPlayEnabled
import com.naman14.timberx.util.media.isPlaying
import com.naman14.timberx.util.media.position
import com.naman14.timberx.util.media.toRawMediaItems
import java.util.*
import kotlin.collections.ArrayList
import timber.log.Timber.d as log

class TimberMusicService : MediaBrowserServiceCompat(), MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

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

    private var mCurrentSongId: Long = -1
    private var isPlaying = false
    private var isInitialized = false
    private var mStarted = false

    private lateinit var mQueue: LongArray
    private lateinit var mMediaSession: MediaSessionCompat
    private lateinit var mStateBuilder: PlaybackStateCompat.Builder
    private lateinit var mMetadataBuilder: MediaMetadataCompat.Builder
    private lateinit var mQueueTitle: String
    private lateinit var becomingNoisyReceiver: BecomingNoisyReceiver

    private var player: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        log("onCreate()")

        setUpMediaSession()
        mMediaSession.setFlags(FLAG_HANDLES_MEDIA_BUTTONS or FLAG_HANDLES_TRANSPORT_CONTROLS)

        mStateBuilder = PlaybackStateCompat.Builder().setActions(
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
        mMediaSession.setPlaybackState(mStateBuilder.build())

        val sessionIntent = packageManager?.getLaunchIntentForPackage(packageName)
        val sessionActivityPendingIntent = PendingIntent.getActivity(this, 0, sessionIntent, 0)

        mMediaSession.setSessionActivity(sessionActivityPendingIntent)
        mMediaSession.isActive = true

        sessionToken = mMediaSession.sessionToken

        becomingNoisyReceiver =
                BecomingNoisyReceiver(context = this, sessionToken = mMediaSession.sessionToken)

        mMetadataBuilder = MediaMetadataCompat.Builder()


        mQueue = LongArray(0)
        mQueueTitle = "All songs"
        mMediaSession.run {
            setQueue(mQueue.toQueue(this@TimberMusicService))
            setQueueTitle(mQueueTitle)
        }

        initPlayer()
    }

    private fun setUpMediaSession() {
        mMediaSession = MediaSessionCompat(this, "TimberX")
        mMediaSession.setCallback(object : MediaSessionCompat.Callback() {

            override fun onPause() {
                pause()
            }

            override fun onPlay() {
                if (!mStarted) {
                    startService()
                }
                playSong()
            }

            override fun onPlayFromSearch(query: String?, extras: Bundle?) {
                query?.let {
                    val song = SongsRepository.searchSongs(this@TimberMusicService, query, 1)
                    if (song.isNotEmpty()) {
                        if (!mStarted) {
                            startService()
                        }
                        playSong(song[0])
                    }
                } ?: onPlay()
            }

            override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {

                val songID = MediaID().fromString(mediaId!!).mediaId!!.toLong()

                if (!mStarted) {
                    startService()
                }

                setPlaybackState(mStateBuilder.setState(mMediaSession.controller.playbackState.state,
                        0, 1F).build())
                playSong(songID)

                extras?.let {
                    doAsync {
                        val queue = it.getLongArray(Constants.SONGS_LIST)
                        val seekTo = it.getInt(Constants.SEEK_TO_POS)
                        val queueTitle = it.getString(Constants.QUEUE_TITLE)
                        queue?.let { mQueue = it }
                        queueTitle?.let { mQueueTitle = it }
                        setPlaybackState(mStateBuilder.setState(mMediaSession.controller.playbackState.state,
                                seekTo.toLong(), 1F).build())
                        mMediaSession.setQueue(mQueue.toQueue(this@TimberMusicService))
                        mMediaSession.setQueueTitle(mQueueTitle)
                    }.execute()
                }
            }

            override fun onSeekTo(pos: Long) {
                if (isInitialized) {
                    player?.seekTo(pos.toInt())
                    setPlaybackState(mStateBuilder.setState(mMediaSession.controller.playbackState.state,
                            pos, 1F).build())
                }
            }

            override fun onSkipToNext() {
                val currentIndex = mQueue.indexOf(mCurrentSongId)
                if (currentIndex + 1 < mQueue.size) {
                    val nextSongIndex = if (mMediaSession.controller.shuffleMode == SHUFFLE_MODE_ALL) {
                        Random().nextInt(mQueue.size - 1)
                    } else {
                        currentIndex + 1
                    }
                    onPlayFromMediaId(MediaID(TYPE_SONG.toString(),
                            mQueue[nextSongIndex].toString()).asString(), null)
                } else {
                    // reached end of queue, pause player
                    // note that repeat queue would have already been handled in onCustomAction
                    onPause()
                }
            }

            override fun onSkipToPrevious() {
                val currentIndex = mQueue.indexOf(mCurrentSongId)
                if (currentIndex - 1 >= 0) {
                    onPlayFromMediaId(MediaID(TYPE_SONG.toString(),
                            mQueue[currentIndex - 1].toString()).asString(), null)
                }
            }

            override fun onStop() {
                player?.stop()
                setPlaybackState(mStateBuilder.setState(STATE_NONE, 0, 1F).build())
                NotificationUtils.updateNotification(this@TimberMusicService, mMediaSession)
                stopService()
            }

            override fun onSetRepeatMode(repeatMode: Int) {
                super.onSetRepeatMode(repeatMode)
                val bundle = mMediaSession.controller.playbackState.extras ?: Bundle()
                setPlaybackState(PlaybackStateCompat.Builder(mMediaSession.controller.playbackState)
                        .setExtras(bundle.apply {
                            putInt(REPEAT_MODE, repeatMode)
                        }
                        ).build())
            }

            override fun onSetShuffleMode(shuffleMode: Int) {
                super.onSetShuffleMode(shuffleMode)
                val bundle = mMediaSession.controller.playbackState.extras ?: Bundle()
                setPlaybackState(PlaybackStateCompat.Builder(mMediaSession.controller.playbackState)
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
                        if (mCurrentSongId == mQueue[mQueue.size - 1])
                            onPlayFromMediaId(MediaID(TYPE_SONG.toString(),
                                    mQueue[0].toString()).asString(), null)
                        else onSkipToNext()
                    }

                    ACTION_PLAY_NEXT -> {
                        val nextSongId = extras!!.getLong(SONG)
                        val list = arrayListOf<Long>().apply {
                            addAll(mQueue.asList())
                            remove(nextSongId)
                            add(mQueue.indexOf(mCurrentSongId), nextSongId)
                        }
                        mQueue = list.toLongArray()
                        mMediaSession.setQueue(mQueue.toQueue(this@TimberMusicService))
                    }

                    ACTION_QUEUE_REORDER -> {
                        val from = extras!!.getInt(QUEUE_FROM)
                        val to = extras.getInt(QUEUE_TO)

                        mQueue = mQueue.asList().moveElement(from, to).toLongArray()
                        mMediaSession.setQueue(mQueue.toQueue(this@TimberMusicService))
                    }

                    ACTION_SONG_DELETED -> {
                        //remove song from current queue if deleted
                        val list = arrayListOf<Long>().apply {
                            addAll(mQueue.asList())
                            remove(extras!!.getLong(SONG))
                        }
                        mQueue = list.toLongArray()
                        mMediaSession.setQueue(mQueue.toQueue(this@TimberMusicService))
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
                    mMediaSession.controller.playbackState?.let { playbackState ->
                        when {
                            playbackState.isPlaying -> mMediaSession.controller.transportControls.pause()
                            playbackState.isPlayEnabled -> mMediaSession.controller.transportControls.play()
                        }
                    }
                }
                ACTION_NEXT -> {
                    mMediaSession.controller.transportControls.skipToNext()
                }
                ACTION_PREVIOUS -> {
                    mMediaSession.controller.transportControls.skipToPrevious()
                }
                else -> Unit
            }
        }
        MediaButtonReceiver.handleIntent(mMediaSession, intent)
        return START_STICKY
    }

    override fun onDestroy() {
        log("onDestroy()")
        saveCurrentData()
        mMediaSession.run {
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
            seekTo(mMediaSession.position().toInt())
        }
        setPlaybackState(mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                mMediaSession.position(), 1F).build())
        NotificationUtils.updateNotification(this, mMediaSession)
    }

    override fun onCompletion(player: MediaPlayer?) {
        log("onCompletion()")
        when (mMediaSession.controller.repeatMode) {
            REPEAT_MODE_ONE -> {
                mMediaSession.controller.transportControls.sendCustomAction(ACTION_REPEAT_SONG, null)
            }
            REPEAT_MODE_ALL -> {
                mMediaSession.controller.transportControls.sendCustomAction(ACTION_REPEAT_QUEUE, null)
            }
            else -> {
                mMediaSession.controller.transportControls.skipToNext()
            }
        }

    }

    override fun onError(player: MediaPlayer?, p1: Int, p2: Int): Boolean {
        log("onError(): $p1, $p2")
        isPlaying = false
        return false
    }

    private fun setPlaybackState(playbackStateCompat: PlaybackStateCompat) {
        mMediaSession.setPlaybackState(playbackStateCompat)
        playbackStateCompat.extras?.let { bundle ->
            mMediaSession.setRepeatMode(bundle.getInt(REPEAT_MODE))
            mMediaSession.setShuffleMode(bundle.getInt(SHUFFLE_MODE))
        }
        if (playbackStateCompat.isPlaying) becomingNoisyReceiver.register()
        else becomingNoisyReceiver.unregister()
    }

    private fun setMetaData(song: Song) {
        doAsync {
            val artwork = MusicUtils.getAlbumArtBitmap(this, song.albumId)

            val mediaMetadata = mMetadataBuilder.apply {
                putString(METADATA_KEY_ALBUM, song.album)
                putString(METADATA_KEY_ARTIST, song.artist)
                putString(METADATA_KEY_TITLE, song.title)
                putString(METADATA_KEY_ALBUM_ART_URI, Utils.getAlbumArtUri(song.albumId).toString())
                putBitmap(METADATA_KEY_ALBUM_ART, artwork)
                putString(METADATA_KEY_MEDIA_ID, song.id.toString())
                putLong(METADATA_KEY_DURATION, song.duration.toLong())
            }.build()
            mMediaSession.setMetadata(mediaMetadata)
        }.execute()
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
                    setIconUri(getEmptyAlbumArtUri().toUri())
                    setSubtitle(getString(R.string.artists))
                }.build(), FLAG_BROWSABLE
        ))

        mMediaRoot.add(MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder().apply {
                    setMediaId(MediaID(TYPE_ALL_ALBUMS.toString(), null, caller).asString())
                    setTitle(getString(R.string.albums))
                    setIconUri(getEmptyAlbumArtUri().toUri())
                    setSubtitle(getString(R.string.albums))
                }.build(), FLAG_BROWSABLE
        ))

        mMediaRoot.add(MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder().apply {
                    setMediaId(MediaID(TYPE_ALL_SONGS.toString(), null, caller).asString())
                    setTitle(getString(R.string.songs))
                    setIconUri(getEmptyAlbumArtUri().toUri())
                    setSubtitle(getString(R.string.songs))
                }.build(), FLAG_BROWSABLE
        ))


        mMediaRoot.add(MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder().apply {
                    setMediaId(MediaID(TYPE_ALL_PLAYLISTS.toString(), null, caller).asString())
                    setTitle(getString(R.string.playlists))
                    setIconUri(getEmptyAlbumArtUri().toUri())
                    setSubtitle(getString(R.string.playlists))
                }.build(), FLAG_BROWSABLE
        ))

        mMediaRoot.add(MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder().apply {
                    setMediaId(MediaID(TYPE_ALL_GENRES.toString(), null, caller).asString())
                    setTitle(getString(R.string.genres))
                    setIconUri(getEmptyAlbumArtUri().toUri())
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

        doAsyncPost(handler = {
            if (mediaType == MEDIA_ID_ROOT.toString()) {
                addMediaRoots(mediaItems, caller!!)
            } else {
                when (mediaType?.toInt() ?: 0) {
                    TYPE_ALL_ARTISTS -> {
                        mediaItems.addAll(getAllArtists(this, caller))
                    }
                    TYPE_ALL_ALBUMS -> {
                        mediaItems.addAll(getAllAlbums(this, caller))
                    }
                    TYPE_ALL_SONGS -> {
                        mediaItems.addAll(loadSongs(this, caller))
                    }
                    TYPE_ALL_GENRES -> {
                        mediaItems.addAll(getAllGenres(this, caller))
                    }
                    TYPE_ALL_PLAYLISTS -> {
                        mediaItems.addAll(getPlaylists(this, caller))
                    }
                    TYPE_ALBUM -> {
                        mediaId?.let {
                            mediaItems.addAll(getSongsForAlbum(this, it.toLong(), caller))
                        }
                    }
                    TYPE_ARTIST -> {
                        mediaId?.let {
                            mediaItems.addAll(getSongsForArtist(this, it.toLong(), caller))
                        }
                    }
                    TYPE_PLAYLIST -> {
                        mediaId?.let {
                            mediaItems.addAll(getSongsInPlaylist(this, it.toLong(), caller))
                        }
                    }
                    TYPE_GENRE -> {
                        mediaId?.let {
                            mediaItems.addAll(getSongsForGenre(this, it.toLong(), caller))
                        }
                    }
                }
            }
        }, postHandler = {
            if (caller == CALLER_SELF) {
                result.sendResult(mediaItems)
            } else {
                result.sendResult(mediaItems.toRawMediaItems())
            }
        }).execute()


    }

    private fun setLastCurrentID() {
        val queueData = TimberDatabase.getInstance(this)!!.queueDao().getQueueDataSync()
        mCurrentSongId = queueData?.currentId ?: 0
        log("setLastCurrentID(): $mCurrentSongId")
    }

    private fun setSavedMediaSessionState() {
        doAsync {
            //only set saved session from db if we know there is not any active media session
            if (mMediaSession.controller.playbackState == null
                    || mMediaSession.controller.playbackState.state == STATE_NONE) {
                val queueData = TimberDatabase.getInstance(this)!!.queueDao().getQueueDataSync()
                queueData?.let {
                    mQueueTitle = it.queueTitle
                    val queue = TimberDatabase.getInstance(this)!!.queueDao().getQueueSongsSync()
                    queue.toSongIDs(this).also { queueIDs ->
                        mMediaSession.setQueue(queueIDs.toQueue(this))
                        mQueue = queueIDs
                    }
                    mCurrentSongId = queueData.currentId!!
                    queueData.currentId?.let {
                        setMetaData(getSongForId(this, queueData.currentId!!))
                        setPlaybackState(mStateBuilder.setState(queueData.playState!!,
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
        }.execute()
    }

    private fun restoreMediaSession() {
        log("restoreMediaSession()")
        setPlaybackState(mMediaSession.controller.playbackState)
        mMediaSession.setMetadata(mMediaSession.controller.metadata)
    }

    private fun saveCurrentData() {
        if (mMediaSession.controller == null ||
                mMediaSession.controller.playbackState == null ||
                mMediaSession.controller.playbackState.state == STATE_NONE) {
            return
        }
        log("saveCurrentData()")
        doAsync {
            val mediaController = mMediaSession.controller
            val queue = mediaController.queue
            val currentId = mediaController.metadata?.getString(METADATA_KEY_MEDIA_ID)

            DbHelper.updateQueueSongs(this, queue?.toIDList(), currentId?.toLong())

            val queueEntity = QueueEntity().apply {
                this.currentId = currentId?.toLong()
                currentSeekPos = mediaController?.playbackState?.position
                repeatMode = mediaController?.repeatMode
                shuffleMode = mediaController?.shuffleMode
                playState = mediaController?.playbackState?.state
                queueTitle = mediaController?.queueTitle?.toString() ?: "All songs"
            }

            DbHelper.updateQueueData(this, queueEntity)
        }.execute()

    }

    private fun startService() {
        log("startService()")
        if (!mStarted) {
            val intent = Intent(this, TimberMusicService::class.java)
            startService(intent)
            startForeground(NOTIFICATION_ID, buildNotification(this, mMediaSession))
            mStarted = true
        }
    }

    private fun stopService() {
        log("stopService()")
        saveCurrentData()
        if (mStarted) {
            stopSelf()
            mStarted = false
        }
    }

    private fun initPlayer() {
        log("initPlayer()")
        player = MediaPlayer().apply {
            setWakeMode(applicationContext, PARTIAL_WAKE_LOCK)
            setAudioStreamType(STREAM_MUSIC)
            setOnPreparedListener(this@TimberMusicService)
            setOnCompletionListener(this@TimberMusicService)
            setOnErrorListener(this@TimberMusicService)
        }

    }

    fun playSong(id: Long) {
        val song = getSongForId(this, id)
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
            setPlaybackState(mStateBuilder.setState(STATE_PLAYING, mMediaSession.position(), 1F).build())
            updateNotification(this, mMediaSession)
            player?.start()
            return
        }

        player?.reset()
        val path = getSongUri(mCurrentSongId).toString()
        if (path.startsWith("content://")) {
            player?.setDataSource(this, Uri.parse(path))
        } else {
            player?.setDataSource(path)
        }
        isInitialized = true
        player?.prepareAsync()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        log("onUnbind")
        return super.onUnbind(intent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        log("onBind")
        return super.onBind(intent)
    }

    fun pause() {
        log("pause()")
        if (isPlaying && isInitialized) {
            player?.pause()
            setPlaybackState(mStateBuilder.setState(STATE_PAUSED,
                    mMediaSession.position(), 1F).build())
            NotificationUtils.updateNotification(this, mMediaSession)
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