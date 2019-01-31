package com.naman14.timberx

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.Nullable
import androidx.media.MediaBrowserServiceCompat
import com.naman14.timberx.models.Song
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.session.MediaButtonReceiver
import com.naman14.timberx.util.*
import android.provider.MediaStore
import android.support.v4.media.session.MediaControllerCompat
import com.naman14.timberx.cast.CastHelper
import com.naman14.timberx.db.DbHelper
import com.naman14.timberx.db.QueueEntity
import com.naman14.timberx.db.TimberDatabase
import com.naman14.timberx.models.MediaID
import com.naman14.timberx.repository.*
import com.naman14.timberx.util.media.isPlayEnabled
import com.naman14.timberx.util.media.isPlaying
import com.naman14.timberx.util.media.position
import java.io.FileNotFoundException
import java.util.*
import kotlin.collections.ArrayList

class TimberMusicService : MediaBrowserServiceCompat(), MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

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
    private var nextPlayer: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()

        setUpMediaSession()

        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

        mStateBuilder = PlaybackStateCompat.Builder().setActions(
                PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_PAUSE
                        or PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                        or PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                        or PlaybackStateCompat.ACTION_PLAY_PAUSE
                        or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        or PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE
                        or PlaybackStateCompat.ACTION_SET_REPEAT_MODE)
                .setState(PlaybackStateCompat.STATE_NONE, 0, 1f)
        mMediaSession.setPlaybackState(mStateBuilder.build())

        val sessionIntent = packageManager?.getLaunchIntentForPackage(packageName)
        val sessionActivityPendingIntent = PendingIntent.getActivity(this, 0, sessionIntent, 0)

        mMediaSession.setSessionActivity(sessionActivityPendingIntent)

        sessionToken = mMediaSession.sessionToken

        becomingNoisyReceiver =
                BecomingNoisyReceiver(context = this, sessionToken = mMediaSession.sessionToken)

        mMetadataBuilder = MediaMetadataCompat.Builder()


        mQueue = LongArray(0)
        mQueueTitle = "All songs"
        mMediaSession.setQueue(mQueue.toQueue(this))
        mMediaSession.setQueueTitle(mQueueTitle)

        initPlayer()
    }

    private fun setUpMediaSession() {
        mMediaSession = MediaSessionCompat(this, "TimberX")
        mMediaSession.isActive = true
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

            override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
                super.onPlayFromUri(uri, extras)
            }

            override fun onPrepare() {
                super.onPrepare()
            }

            override fun onPrepareFromSearch(query: String?, extras: Bundle?) {
                super.onPrepareFromSearch(query, extras)
            }

            override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {

                val songID = MediaID().fromString(mediaId!!).mediaId!!.toLong()

                if (!mStarted) {
                    startService()
                }

                setPlaybackState(mStateBuilder.setState(mMediaSession.controller.playbackState.state, 0, 1F).build())
                playSong(songID)

                extras?.let {
                    doAsync {
                        val queue = it.getLongArray(Constants.SONGS_LIST)
                        val seekTo = it.getInt(Constants.SEEK_TO_POS)
                        val queueTitle = it.getString(Constants.QUEUE_TITLE)
                        queue?.let { mQueue = it }
                        queueTitle?.let { mQueueTitle = it }
                        setPlaybackState(mStateBuilder.setState(mMediaSession.controller.playbackState.state, seekTo.toLong(), 1F).build())
                        mMediaSession.setQueue(mQueue.toQueue(this@TimberMusicService))
                        mMediaSession.setQueueTitle(mQueueTitle)
                    }.execute()
                }
            }

            override fun onSeekTo(pos: Long) {
                if (isInitialized) {
                    player?.seekTo(pos.toInt())
                    setPlaybackState(mStateBuilder.setState(mMediaSession.controller.playbackState.state, pos , 1F).build())
                }
            }

            override fun onSkipToNext() {
                val currentIndex = mQueue.indexOf(mCurrentSongId)
                if (currentIndex + 1 < mQueue.size) {
                    val nextSongIndex = if (mMediaSession.controller.shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
                        Random().nextInt(mQueue.size - 1)
                    } else currentIndex + 1
                    onPlayFromMediaId(MediaID(TYPE_SONG.toString(), mQueue[nextSongIndex].toString()).asString(), null)
                } else {
                    // reached end of queue, pause player
                    // note that repeat queue would have already been handled in onCustomAction
                    onPause()
                }
            }

            override fun onSkipToPrevious() {
                val currentIndex = mQueue.indexOf(mCurrentSongId)
                if (currentIndex - 1 >= 0) {
                    onPlayFromMediaId(MediaID(TYPE_SONG.toString(), mQueue[currentIndex - 1].toString()).asString(), null)
                }
            }

            override fun onStop() {
                player?.stop()
                setPlaybackState(mStateBuilder.setState(PlaybackStateCompat.STATE_NONE, 0, 1F).build())
                NotificationUtils.updateNotification(this@TimberMusicService, mMediaSession)
                stopService()
            }

            override fun onAddQueueItem(description: MediaDescriptionCompat?) {
                super.onAddQueueItem(description)
            }

            override fun onRemoveQueueItem(description: MediaDescriptionCompat?) {
                super.onRemoveQueueItem(description)
            }

            override fun onAddQueueItem(description: MediaDescriptionCompat?, index: Int) {
                super.onAddQueueItem(description, index)
            }

            override fun onSetRepeatMode(repeatMode: Int) {
                super.onSetRepeatMode(repeatMode)
                val bundle = mMediaSession.controller.playbackState.extras ?: Bundle()
                setPlaybackState(PlaybackStateCompat.Builder(mMediaSession.controller.playbackState)
                        .setExtras(bundle.apply {
                            putInt(Constants.REPEAT_MODE, repeatMode)
                        }).build())
            }

            override fun onSetShuffleMode(shuffleMode: Int) {
                super.onSetShuffleMode(shuffleMode)
                val bundle = mMediaSession.controller.playbackState.extras ?: Bundle()
                setPlaybackState(PlaybackStateCompat.Builder(mMediaSession.controller.playbackState)
                        .setExtras(bundle.apply {
                            putInt(Constants.SHUFFLE_MODE, shuffleMode)
                        }).build())
            }

            override fun onCustomAction(action: String?, extras: Bundle?) {
                when (action) {
                    Constants.ACTION_SET_MEDIA_STATE -> setSavedMediaSessionState()

                    Constants.ACTION_REPEAT_SONG -> {
                        onPlayFromMediaId(MediaID(TYPE_SONG.toString(), mCurrentSongId.toString()).asString(), null)
                    }

                    Constants.ACTION_REPEAT_QUEUE -> {
                        if (mCurrentSongId == mQueue[mQueue.size - 1])
                            onPlayFromMediaId(MediaID(TYPE_SONG.toString(), mQueue[0].toString()).asString(), null)
                        else onSkipToNext()
                    }

                    Constants.ACTION_PLAY_NEXT -> {
                        val nextSongId = extras!!.getLong(Constants.SONG)
                        val list =  arrayListOf<Long>().apply {
                            addAll(mQueue.asList())
                            remove(nextSongId)
                            add(mQueue.indexOf(mCurrentSongId), nextSongId)
                        }
                        mQueue = list.toLongArray()
                        mMediaSession.setQueue(mQueue.toQueue(this@TimberMusicService))
                    }

                    Constants.ACTION_QUEUE_REORDER -> {
                        val from = extras!!.getInt(Constants.QUEUE_FROM)
                        val to = extras.getInt(Constants.QUEUE_TO)

                        mQueue = mQueue.asList().moveElement(from, to).toLongArray()
                        mMediaSession.setQueue(mQueue.toQueue(this@TimberMusicService))
                    }

                    Constants.ACTION_SONG_DELETED -> {
                        //remove song from current queue if deleted
                       val list =  arrayListOf<Long>().apply {
                           addAll(mQueue.asList())
                           remove(extras!!.getLong(Constants.SONG))
                       }
                        mQueue = list.toLongArray()
                        mMediaSession.setQueue(mQueue.toQueue(this@TimberMusicService))
                    }

                    Constants.ACTION_RESTORE_MEDIA_SESSION -> {
                       restoreMediaSession()
                    }
                }
            }
        })
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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
                Constants.ACTION_NEXT -> { mMediaSession.controller.transportControls.skipToNext() }
                Constants.ACTION_PREVIOUS -> {mMediaSession.controller.transportControls.skipToPrevious() }
                else -> {}
            }
        }
        MediaButtonReceiver.handleIntent(mMediaSession, intent)
        return START_STICKY
    }

    override fun onDestroy() {
        saveCurrentData()
        mMediaSession.isActive = false
        mMediaSession.release()
        player?.release()
        player = null
        super.onDestroy()
    }

    override fun onPrepared(player: MediaPlayer?) {
        isPlaying = true
        player?.start()
        player?.seekTo(mMediaSession.position().toInt())
        setPlaybackState(mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, mMediaSession.position(), 1F).build())
        NotificationUtils.updateNotification(this, mMediaSession)
    }

    override fun onCompletion(player: MediaPlayer?) {
        when (mMediaSession.controller.repeatMode) {
            PlaybackStateCompat.REPEAT_MODE_ONE ->
                mMediaSession.controller.transportControls.sendCustomAction(Constants.ACTION_REPEAT_SONG, null)
            PlaybackStateCompat.REPEAT_MODE_ALL ->
                mMediaSession.controller.transportControls.sendCustomAction(Constants.ACTION_REPEAT_QUEUE, null)
            else -> mMediaSession.controller.transportControls.skipToNext()
        }

    }

    override fun onError(player: MediaPlayer?, p1: Int, p2: Int): Boolean {
        isPlaying = false
        return false
    }

    private fun setPlaybackState(playbackStateCompat: PlaybackStateCompat) {
        mMediaSession.setPlaybackState(playbackStateCompat)
        playbackStateCompat.extras?.let { bundle ->
            mMediaSession.setRepeatMode(bundle.getInt(Constants.REPEAT_MODE))
            mMediaSession.setShuffleMode(bundle.getInt(Constants.SHUFFLE_MODE))
        }
        if (playbackStateCompat.isPlaying) becomingNoisyReceiver.register()
        else becomingNoisyReceiver.unregister()
    }

    private fun setMetaData(song: Song) {
        doAsync {
            var artwork: Bitmap? = null
            try {
                artwork = MediaStore.Images.Media.getBitmap(this.contentResolver, Utils.getAlbumArtUri(song.albumId))
            } catch (e: FileNotFoundException) {
                artwork = BitmapFactory.decodeResource(resources, R.drawable.icon)
            }

            val mediaMetadata = mMetadataBuilder
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.album)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, Utils.getAlbumArtUri(song.albumId).toString())
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, artwork)
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.id.toString())
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration.toLong()).build()
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
        return MediaBrowserServiceCompat.BrowserRoot(MediaID(MEDIA_ID_ROOT.toString(), null).asString(), null)
    }

    private fun addMediaRoots(mMediaRoot: MutableList<MediaBrowserCompat.MediaItem>) {
        mMediaRoot.add(MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder()
                        .setMediaId(MediaID(TYPE_ALL_ARTISTS.toString(), null).asString())
                        .setTitle(getString(R.string.artists))
                        .setIconUri(Uri.parse(Utils.getEmptyAlbumArtUri()))
                        .setSubtitle(getString(R.string.artists))
                        .build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
        ))

        mMediaRoot.add(MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder()
                        .setMediaId(MediaID(TYPE_ALL_ALBUMS.toString(), null).asString())
                        .setTitle(getString(R.string.albums))
                        .setIconUri(Uri.parse(Utils.getEmptyAlbumArtUri()))
                        .setSubtitle(getString(R.string.albums))
                        .build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
        ))

        mMediaRoot.add(MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder()
                        .setMediaId(MediaID(TYPE_ALL_SONGS.toString(), null).asString())
                        .setTitle(getString(R.string.songs))
                        .setIconUri(Uri.parse(Utils.getEmptyAlbumArtUri()))
                        .setSubtitle(getString(R.string.songs))
                        .build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
        ))


        mMediaRoot.add(MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder()
                        .setMediaId(MediaID(TYPE_ALL_PLAYLISTS.toString(), null).asString())
                        .setTitle(getString(R.string.playlists))
                        .setIconUri(Uri.parse(Utils.getEmptyAlbumArtUri()))
                        .setSubtitle(getString(R.string.playlists))
                        .build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
        ))

        mMediaRoot.add(MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder()
                        .setMediaId(MediaID(TYPE_ALL_GENRES.toString(), null).asString())
                        .setTitle(getString(R.string.genres))
                        .setIconUri(Uri.parse(Utils.getEmptyAlbumArtUri()))
                        .setSubtitle(getString(R.string.genres))
                        .build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
        ))


    }

    private fun loadChildren(parentId: String, result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>) {

        val mediaItems = ArrayList<MediaBrowserCompat.MediaItem>()
        val mediaIdParent = MediaID().fromString(parentId)

        val mediaType = mediaIdParent.fromString(parentId).type
        val mediaId = mediaIdParent.fromString(parentId).mediaId

        doAsyncPost(handler = {
            if (mediaType == MEDIA_ID_ROOT.toString()) {
                addMediaRoots(mediaItems)
            } else {
                when (Integer.parseInt(mediaType.toString())) {
                    TYPE_ALL_ARTISTS -> {
                        mediaItems.addAll(ArtistRepository.getAllArtists(this))
                    }
                    TYPE_ALL_ALBUMS -> {
                        mediaItems.addAll(AlbumRepository.getAllAlbums(this))
                    }
                    TYPE_ALL_SONGS -> {
                        mediaItems.addAll(SongsRepository.loadSongs(this))
                    }
                    TYPE_ALL_GENRES -> {
                        mediaItems.addAll(GenreRepository.getAllGenres(this))
                    }
                    TYPE_ALL_PLAYLISTS -> {
                        mediaItems.addAll(PlaylistRepository.getPlaylists(this))
                    }
                    TYPE_ALBUM -> {
                        mediaId?.let {
                            mediaItems.addAll(AlbumRepository.getSongsForAlbum(this, it.toLong()))
                        }
                    }
                    TYPE_ARTIST -> {
                        mediaId?.let {
                            mediaItems.addAll(ArtistRepository.getSongsForArtist(this, it.toLong()))
                        }
                    }
                    TYPE_PLAYLIST -> {
                        mediaId?.let {
                            mediaItems.addAll(PlaylistRepository.getSongsInPlaylist(this, it.toLong()))
                        }
                    }
                    TYPE_GENRE -> {
                        mediaId?.let {
                            mediaItems.addAll(GenreRepository.getSongsForGenre(this, it.toLong()))
                        }
                    }
                }
            }
        }, postHandler = {
            result.sendResult(mediaItems)
        }).execute()


    }

    private fun setLastCurrentID() {
        val queueData = TimberDatabase.getInstance(this)!!.queueDao().getQueueDataSync()
        mCurrentSongId = queueData?.currentId ?: 0
    }

    private fun setSavedMediaSessionState() {
        doAsync {
            //only set saved session from db if we know there is not any active media session
            if (mMediaSession.controller.playbackState == null || mMediaSession.controller.playbackState.state == PlaybackStateCompat.STATE_NONE) {
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
                        setMetaData(SongsRepository.getSongForId(this, queueData.currentId!!))
                        setPlaybackState(mStateBuilder.setState(queueData.playState!!, queueData.currentSeekPos!!, 1F).setExtras(
                                Bundle().apply {
                                    putInt(Constants.REPEAT_MODE, queueData.repeatMode!!)
                                    putInt(Constants.SHUFFLE_MODE, queueData.shuffleMode!!)
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
        setPlaybackState(mMediaSession.controller.playbackState)
        mMediaSession.setMetadata(mMediaSession.controller.metadata)
    }

    private fun saveCurrentData() {
        if (mMediaSession.controller == null ||
                mMediaSession.controller.playbackState == null ||
                mMediaSession.controller.playbackState.state == PlaybackStateCompat.STATE_NONE) return
        doAsync {
            val mediaController = mMediaSession.controller
            val queue = mediaController.queue
            val currentId = mediaController.metadata?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)

            DbHelper.updateQueueSongs(this, queue?.toIDList(), currentId?.toLong())

            val queueEntity = QueueEntity()
            queueEntity.currentId = currentId?.toLong()
            queueEntity.currentSeekPos = mediaController?.playbackState?.position
            queueEntity.repeatMode = mediaController?.repeatMode
            queueEntity.shuffleMode = mediaController?.shuffleMode
            queueEntity.playState = mediaController?.playbackState?.state
            queueEntity.queueTitle = mediaController?.queueTitle?.toString() ?: "All songs"

            DbHelper.updateQueueData(this, queueEntity)
        }.execute()

    }

    private fun startService() {
        if (!mStarted) {
            val intent = Intent(this, TimberMusicService::class.java)
            startService(intent)
            startForeground(NOTIFICATION_ID, NotificationUtils.buildNotification(this, mMediaSession))
            mStarted = true
        }
    }

    private fun stopService() {
        saveCurrentData()
        if (mStarted) {
            stopSelf()
            mStarted = false
        }
    }

    private fun initPlayer() {
        player = MediaPlayer()
        player?.setWakeMode(applicationContext,
                PowerManager.PARTIAL_WAKE_LOCK)
        player?.setAudioStreamType(AudioManager.STREAM_MUSIC)
        player?.setOnPreparedListener(this)
        player?.setOnCompletionListener(this)
        player?.setOnErrorListener(this)

    }

    fun playSong(id: Long) {
        val song = SongsRepository.getSongForId(this, id)
        playSong(song)
    }

    fun playSong(song: Song) {
        if (mCurrentSongId != song.id) {
            mCurrentSongId = song.id
            isInitialized = false
        }
        setMetaData(song)
        playSong()
    }

    fun playSong() {
        if (mCurrentSongId.toInt() == -1) setLastCurrentID()

        if (isInitialized) {
            setPlaybackState(mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, mMediaSession.position(), 1F).build())
            NotificationUtils.updateNotification(this, mMediaSession)
            player?.start()
            return
        }

        player?.reset()
        val path: String = MusicUtils.getSongUri(mCurrentSongId).toString()
        if (path.startsWith("content://")) {
            player?.setDataSource(this, Uri.parse(path))
        } else {
            player?.setDataSource(path)
        }
        isInitialized = true
        player?.prepareAsync()

        player?.setNextMediaPlayer(nextPlayer)

    }

    fun pause() {
        if (isPlaying && isInitialized) {
            player?.pause()
            setPlaybackState(mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, mMediaSession.position(), 1F).build())
            NotificationUtils.updateNotification(this, mMediaSession)
            stopForeground(false)
            saveCurrentData()
        }
    }

    fun position(): Int {
        return player?.currentPosition ?: 0
    }

    /**
     * Helper class for listening for when headphones are unplugged (or the audio
     * will otherwise cause playback to become "noisy").
     */
    private class BecomingNoisyReceiver(private val context: Context,
                                        sessionToken: MediaSessionCompat.Token)
        : BroadcastReceiver() {

        private val noisyIntentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
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
            if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                controller.transportControls.pause()
            }
        }
    }


    companion object {
        const val MEDIA_ID_ARG = "MEDIA_ID"
        const val MEDIA_TYPE_ARG = "MEDIA_TYPE"
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

}