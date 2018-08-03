package com.naman14.timberx

import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.content.ContextWrapper
import android.app.Activity
import android.content.Context
import android.content.ServiceConnection
import java.util.*
import android.content.ComponentName
import android.media.browse.MediaBrowser
import android.net.Uri
import android.os.*
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.Nullable
import androidx.media.MediaBrowserServiceCompat
import com.naman14.timberx.db.DbHelper
import com.naman14.timberx.repository.SongsRepository
import com.naman14.timberx.util.Utils
import com.naman14.timberx.util.doAsyncPost
import com.naman14.timberx.util.getSongUri
import com.naman14.timberx.vo.Song
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.content.ContextCompat
import com.naman14.timberx.util.NotificationUtils

class TimberMusicService: MediaBrowserServiceCompat(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    val MEDIA_ID_ROOT = "__ROOT__"
    val TYPE_ARTIST = 0
    val TYPE_ALBUM = 1
    val TYPE_SONG = 2
    val TYPE_PLAYLIST = 3
    val TYPE_ARTIST_SONG_ALBUMS = 4
    val TYPE_ALBUM_SONGS = 5
    val TYPE_ARTIST_ALL_SONGS = 6
    val TYPE_PLAYLIST_ALL_SONGS = 7

    val NOTIFICATION_ID = 888


    var mCurrentSongId: Long = -1
    var isPlaying = false
    var isInitialized = false
    var mStarted = false

    private lateinit var mMediaSession: MediaSessionCompat
    private lateinit var mStateBuilder: PlaybackStateCompat.Builder


    companion object {
        val mConnectionMap = WeakHashMap<Context, ServiceBinder>()
        var mService: TimberMusicService? = null
        var mServiceStartId: Int = -1

        fun bindToService(context: Context,
                          callback: ServiceConnection): ServiceToken? {

            var realActivity: Activity? = (context as Activity).parent
            if (realActivity == null) {
                realActivity = context
            }
            val contextWrapper = ContextWrapper(realActivity)
            contextWrapper.startService(Intent(contextWrapper, TimberMusicService::class.java))
            val binder = ServiceBinder(callback,
                    contextWrapper.applicationContext)
            if (contextWrapper.bindService(
                            Intent().setClass(contextWrapper, TimberMusicService::class.java!!), binder, 0)) {
                mConnectionMap.put(contextWrapper, binder)
                return ServiceToken(contextWrapper)
            }
            return null
        }

        fun unbindFromService(token: ServiceToken?) {
            if (token == null) {
                return
            }
            val mContextWrapper = token!!.mWrappedContext
            val mBinder = mConnectionMap.remove(mContextWrapper) ?: return
            mContextWrapper.unbindService(mBinder)
            if (mConnectionMap.isEmpty()) {
                mService = null
            }
        }

        fun isPlaybackServiceConnected(): Boolean {
            return mService != null
        }
    }

    private val musicBind = MusicBinder()
    private var player: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()

        setUpMediaSession()

        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

        mStateBuilder = PlaybackStateCompat.Builder().setActions(
                        PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE)
        mMediaSession.setPlaybackState(mStateBuilder.build())

        sessionToken = mMediaSession.sessionToken


        player = MediaPlayer()
        player?.setWakeMode(applicationContext,
                    PowerManager.PARTIAL_WAKE_LOCK)
        player?.setAudioStreamType(AudioManager.STREAM_MUSIC)
        player?.setOnPreparedListener(this)
        player?.setOnCompletionListener(this)
        player?.setOnErrorListener(this)

    }

    private fun setUpMediaSession() {
        mMediaSession = MediaSessionCompat(this, "TimberX")
        mMediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPause() {

            }

            override fun onPlay() {

            }

            override fun onSeekTo(pos: Long) {

            }

            override fun onSkipToNext() {

            }

            override fun onSkipToPrevious() {

            }

            override fun onStop() {

            }
        })
    }


    override fun onBind(p0: Intent?): IBinder {
        return musicBind
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (isInitialized)
            DbHelper.setCurrentSeekPos(this, position())

        stopSelf(mServiceStartId)
        return false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mServiceStartId = startId

        return START_STICKY

    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }


    override fun onPrepared(player: MediaPlayer?) {
        isPlaying = true
        player?.start()
    }

    override fun onCompletion(player: MediaPlayer?) {

    }

    override fun onError(player: MediaPlayer?, p1: Int, p2: Int): Boolean {
        isPlaying = false
        return false
    }


    fun playSong(song: Song) {

        mCurrentSongId = song.id
        player?.reset()

        val path: String = getSongUri(song.id).toString()

        if (path.startsWith("content://")) {
            player?.setDataSource(this, Uri.parse(path))
        } else {
            player?.setDataSource(path)
        }
        isInitialized = true

        player?.prepareAsync()
    }

    fun position(): Int {
        return player?.currentPosition ?: 0
    }


    private fun startService() {
        if (isPlaying && !mStarted) {
            val intent = Intent(this, TimberMusicService::class.java)
            ContextCompat.startForegroundService(this, intent)
            startForeground(NOTIFICATION_ID, NotificationUtils.buildNotification(this, mMediaSession.sessionToken))
            mStarted = true
        }
    }

    private fun stopService() {
        stopSelf()
    }

    inner class MusicBinder : Binder() {
        internal val service: TimberMusicService
            get() = this@TimberMusicService
    }

    class ServiceToken(var mWrappedContext: ContextWrapper)

    class ServiceBinder(private val mCallback: ServiceConnection?, private val mContext: Context) : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as TimberMusicService.MusicBinder
            mService = binder.service
            mCallback?.onServiceConnected(className, service)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mCallback?.onServiceDisconnected(className)
            mService = null
        }
    }



    //media browser

    override fun onLoadChildren(parentId: String, result: Result<List<MediaBrowserCompat.MediaItem>>) {
        result.detach()
        loadChildren(parentId, result)
    }

    @Nullable
    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): MediaBrowserServiceCompat.BrowserRoot? {
        return MediaBrowserServiceCompat.BrowserRoot(MEDIA_ID_ROOT, null)
    }

    private fun addMediaRoots(mMediaRoot: MutableList<MediaBrowserCompat.MediaItem>) {
        mMediaRoot.add(MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder()
                        .setMediaId(Integer.toString(TYPE_ARTIST))
                        .setTitle(getString(R.string.artists))
                        .setIconUri(Uri.parse(Utils.getEmptyAlbumArtUri()))
                        .setSubtitle(getString(R.string.artists))
                        .build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
        ))

        mMediaRoot.add(MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder()
                        .setMediaId(Integer.toString(TYPE_ALBUM))
                        .setTitle(getString(R.string.albums))
                        .setIconUri(Uri.parse(Utils.getEmptyAlbumArtUri()))
                        .setSubtitle(getString(R.string.albums))
                        .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE
        ))

        mMediaRoot.add(MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder()
                        .setMediaId(Integer.toString(TYPE_SONG))
                        .setTitle(getString(R.string.songs))
                        .setIconUri(Uri.parse(Utils.getEmptyAlbumArtUri()))
                        .setSubtitle(getString(R.string.songs))
                        .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE
        ))


        mMediaRoot.add(MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder()
                        .setMediaId(Integer.toString(TYPE_PLAYLIST))
                        .setTitle(getString(R.string.playlists))
                        .setIconUri(Uri.parse(Utils.getEmptyAlbumArtUri()))
                        .setSubtitle(getString(R.string.playlists))
                        .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE
        ))

    }

    private fun loadChildren(parentId: String, result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>) {

        val mediaItems = ArrayList<MediaBrowserCompat.MediaItem>()

        doAsyncPost(handler = {
            if (parentId == MEDIA_ID_ROOT) {
                addMediaRoots(mediaItems)
            } else {
                when (Integer.parseInt(Character.toString(parentId[0]))) {
                    TYPE_ARTIST -> {
//                        val artistList = ArtistLoader.getAllArtists(mContext)
//                        for (artist in artistList) {
//                            val albumNmber = TimberUtils.makeLabel(mContext, R.plurals.Nalbums, artist.albumCount)
//                            val songCount = TimberUtils.makeLabel(mContext, R.plurals.Nsongs, artist.songCount)
//                            fillMediaItems(mediaItems, Integer.toString(TYPE_ARTIST_SONG_ALBUMS) + java.lang.Long.toString(artist.id), artist.name, Uri.parse("android.resource://" + "naman14.timber/drawable/ic_empty_music2"), TimberUtils.makeCombinedString(mContext, albumNmber, songCount), MediaBrowser.MediaItem.FLAG_BROWSABLE)
//                        }
                    }
                    TYPE_ALBUM -> {
//                        val albumList = AlbumLoader.getAllAlbums(mContext)
//                        for (album in albumList) {
//                            fillMediaItems(mediaItems, Integer.toString(TYPE_ALBUM_SONGS) + java.lang.Long.toString(album.id), album.title, TimberUtils.getAlbumArtUri(album.id), album.artistName, MediaBrowser.MediaItem.FLAG_BROWSABLE)
//                        }
                    }
                    TYPE_SONG -> {
                        val songList = SongsRepository.loadSongs(this)
                        for (song in songList) {
                            fillMediaItems(mediaItems, song.id.toString(), song.title, Utils.getAlbumArtUri(song.albumId), song.artist, MediaBrowser.MediaItem.FLAG_PLAYABLE)
                        }
                    }
                    TYPE_ALBUM_SONGS -> {
//                        val albumSongList = AlbumSongLoader.getSongsForAlbum(mContext, java.lang.Long.parseLong(parentId.substring(1)))
//                        for (song in albumSongList) {
//                            fillMediaItems(mediaItems, String.valueOf(song.id), song.title, TimberUtils.getAlbumArtUri(song.albumId), song.artistName, MediaBrowser.MediaItem.FLAG_PLAYABLE)
//                        }
                    }
                    TYPE_ARTIST_SONG_ALBUMS -> {
//                        fillMediaItems(mediaItems, Integer.toString(TYPE_ARTIST_ALL_SONGS) + java.lang.Long.parseLong(parentId.substring(1)), "All songs", Uri.parse("android.resource://" + "naman14.timber/drawable/ic_empty_music2"), "All songs by artist", MediaBrowser.MediaItem.FLAG_BROWSABLE)
//                        val artistAlbums = ArtistAlbumLoader.getAlbumsForArtist(mContext, java.lang.Long.parseLong(parentId.substring(1)))
//                        for (album in artistAlbums) {
//                            val songCount = TimberUtils.makeLabel(mContext, R.plurals.Nsongs, album.songCount)
//                            fillMediaItems(mediaItems, Integer.toString(TYPE_ALBUM_SONGS) + java.lang.Long.toString(album.id), album.title, TimberUtils.getAlbumArtUri(album.id), songCount, MediaBrowser.MediaItem.FLAG_BROWSABLE)
//
//                        }
                    }
                    TYPE_ARTIST_ALL_SONGS -> {
//                        val artistSongs = ArtistSongLoader.getSongsForArtist(mContext, java.lang.Long.parseLong(parentId.substring(1)))
//                        for (song in artistSongs) {
//                            fillMediaItems(mediaItems, String.valueOf(song.id), song.title, TimberUtils.getAlbumArtUri(song.albumId), song.albumName, MediaBrowser.MediaItem.FLAG_PLAYABLE)
//                        }
                    }
                    TYPE_PLAYLIST -> {
//                        val playlistList = PlaylistLoader.getPlaylists(mContext, false)
//                        for (playlist in playlistList) {
//                            val songCount = TimberUtils.makeLabel(mContext, R.plurals.Nsongs, playlist.songCount)
//                            fillMediaItems(mediaItems, Integer.toString(TYPE_PLAYLIST_ALL_SONGS) + java.lang.Long.toString(playlist.id), playlist.name,
//                                    Uri.parse("android.resource://" + "naman14.timber/drawable/ic_empty_music2"), songCount, MediaBrowser.MediaItem.FLAG_BROWSABLE)
//                        }
                    }
                    TYPE_PLAYLIST_ALL_SONGS -> {
//                        val playlistSongs = PlaylistSongLoader.getSongsInPlaylist(mContext, java.lang.Long.parseLong(parentId.substring(1)))
//                        for (song in playlistSongs) {
//                            fillMediaItems(mediaItems, String.valueOf(song.id), song.title, TimberUtils.getAlbumArtUri(song.albumId), song.albumName, MediaBrowser.MediaItem.FLAG_PLAYABLE)
//                        }
                    }
                }
            }
        }, postHandler = {
            result.sendResult(mediaItems)
        }).execute()


    }

    private fun fillMediaItems(mediaItems: MutableList<MediaBrowserCompat.MediaItem>, mediaId: String, title: String, icon: Uri, subTitle: String, playableOrBrowsable: Int) {
        mediaItems.add(MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder()
                        .setMediaId(mediaId)
                        .setTitle(title)
                        .setIconUri(icon)
                        .setSubtitle(subTitle)
                        .build(), playableOrBrowsable
        ))
    }
}