package com.naman14.timberx.ui.viewmodels

import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import androidx.mediarouter.app.MediaRouteButton
import androidx.mediarouter.media.MediaRouteSelector
import com.google.android.gms.cast.framework.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.naman14.timberx.MediaSessionConnection
import com.naman14.timberx.util.MusicUtils
import com.naman14.timberx.cast.CastHelper
import com.naman14.timberx.cast.CastServer
import com.naman14.timberx.models.MediaID
import com.naman14.timberx.models.Song
import com.naman14.timberx.repository.AlbumRepository
import com.naman14.timberx.repository.ArtistRepository
import com.naman14.timberx.ui.dialogs.AddToPlaylistDialog
import com.naman14.timberx.ui.dialogs.DeleteSongDialog
import com.naman14.timberx.ui.listeners.PopupMenuListener
import com.naman14.timberx.util.*
import com.naman14.timberx.util.media.id
import com.naman14.timberx.util.media.isPlayEnabled
import com.naman14.timberx.util.media.isPlaying
import com.naman14.timberx.util.media.isPrepared
import java.io.IOException
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouter
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.naman14.timberx.models.CastStatus
import com.naman14.timberx.repository.SongsRepository

class MainViewModel(private val context: Context, private val mediaSessionConnection: MediaSessionConnection) : ViewModel() {

    class Factory(private val context: Context, private val mediaSessionConnection: MediaSessionConnection) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MainViewModel(context, mediaSessionConnection) as T
        }
    }

    val rootMediaId: LiveData<MediaID> =
            Transformations.map(mediaSessionConnection.isConnected) { isConnected ->
                if (isConnected) {
                    MediaID().fromString(mediaSessionConnection.rootMediaId)
                } else {
                    null
                }
            }

    val mediaController: LiveData<MediaControllerCompat> =
            Transformations.map(mediaSessionConnection.isConnected) { isConnected ->
                if (isConnected) {
                    mediaSessionConnection.mediaController
                } else {
                    null
                }
            }


    val navigateToMediaItem: LiveData<Event<MediaID>> get() = _navigateToMediaItem
    private val _navigateToMediaItem = MutableLiveData<Event<MediaID>>()

    val customAction: LiveData<Event<String>> get() = _customAction
    private val _customAction = MutableLiveData<Event<String>>()

    fun mediaItemClicked(clickedItem: MediaBrowserCompat.MediaItem, extras: Bundle?) {
        if (clickedItem.isBrowsable) {
            browseToItem(clickedItem)
        } else {
            playMedia(clickedItem, extras)
        }
    }


    private fun browseToItem(mediaItem: MediaBrowserCompat.MediaItem) {
        _navigateToMediaItem.value = Event(MediaID().fromString(mediaItem.mediaId!!).apply {
            this.mediaItem = mediaItem
        })
    }

    fun transportControls() = mediaSessionConnection.transportControls

    private fun playMedia(mediaItem: MediaBrowserCompat.MediaItem, extras: Bundle?) {

        //check if casting
        castSession?.let { castSession ->
            val songID = MediaID().fromString(mediaItem.mediaId!!).mediaId!!.toLong()
            castLiveData.value?.let {
                if (it.state != CastStatus.STATUS_NONE &&
                        it.castSongId != -1 && it.castSongId == songID.toInt()) {
                    castSession.remoteMediaClient.togglePlayback()
                    return
                }
            }
            val song = SongsRepository.getSongForId(context, songID)
            if (extras != null && extras.getLongArray(Constants.SONGS_LIST) != null) {
                val mQueue = extras.getLongArray(Constants.SONGS_LIST)!!
                val currentIndex = mQueue.indexOf(songID)
                val extraSize = mQueue.size - currentIndex - 1
                //only send 5 items in queue
                val filteredQueue = mQueue.copyOfRange(currentIndex, currentIndex + if (extraSize >= 5) 5 else extraSize)
                CastHelper.castSongQueue(castSession, SongsRepository.getSongsForIDs(context, filteredQueue), 0)
                return
            }
            CastHelper.castSong(castSession, song)
            return
        }

        val nowPlaying = mediaSessionConnection.nowPlaying.value
        val transportControls = mediaSessionConnection.transportControls

        val isPrepared = mediaSessionConnection.playbackState.value?.isPrepared ?: false
        if (isPrepared && MediaID().fromString(mediaItem.mediaId!!).mediaId == nowPlaying?.id) {
            mediaSessionConnection.playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> transportControls.pause()
                    playbackState.isPlayEnabled -> transportControls.play()
                    else -> {
                        Log.w("MainViewModel", "Playable item clicked but neither play nor pause are enabled!" +
                                " (mediaId=${mediaItem.mediaId})")
                    }
                }
            }
        } else {
            transportControls.playFromMediaId(mediaItem.mediaId, extras)
        }
    }

    val popupMenuListener = object : PopupMenuListener {

        override fun play(song: Song) {
            playMedia(song, null)
        }

        override fun goToAlbum(song: Song) {
            browseToItem(AlbumRepository.getAlbum(context, song.albumId))
        }

        override fun goToArtist(song: Song) {
            browseToItem(ArtistRepository.getArtist(context, song.artistId))
        }

        override fun addToPlaylist(song: Song) {
            AddToPlaylistDialog.newInstance(song).show((context as AppCompatActivity).supportFragmentManager, "AddPlaylist")
        }

        override fun removeFromPlaylist(song: Song, playlistId: Long) {
            MusicUtils.removeFromPlaylist(context, song.id, playlistId)
            _customAction.postValue(Event(Constants.ACTION_REMOVED_FROM_PLAYLIST))
        }

        override fun deleteSong(song: Song) {
            DeleteSongDialog.newInstance(song).apply {
                callback = {
                    _customAction.postValue(Event(Constants.ACTION_SONG_DELETED))
                    // also need to remove the deleted song from the current playing queue
                    mediaSessionConnection.transportControls.sendCustomAction(Constants.ACTION_SONG_DELETED, Bundle().apply {
                        // sending parceleable data through media session custom action bundle is not working currently
                        putLong(Constants.SONG, song.id)
                    })
                }
            }.show((context as AppCompatActivity).supportFragmentManager, "DeleteSong")
        }

        override fun playNext(song: Song) {
            mediaSessionConnection.transportControls.sendCustomAction(Constants.ACTION_PLAY_NEXT, Bundle().apply {
                putLong(Constants.SONG, song.id)
            })
        }
    }


    //cast helpers
    private var castSession: CastSession? = null
    private var sessionManager: SessionManager? = null
    private var isPlayServiceAvailable = false
    private var castServer: CastServer? = null
    private var mediaRouteButton: MediaRouteButton? = null

    val castLiveData: LiveData<CastStatus> get() = _castLiveData
    private val _castLiveData = MutableLiveData<CastStatus>()

    val castProgressLiveData: LiveData<Pair<Long, Long>> get() = _castProgressLiveData
    private val _castProgressLiveData = MutableLiveData<Pair<Long, Long>>()

    private val castCallback = object: RemoteMediaClient.Callback() {

        override fun onStatusUpdated() {
            super.onStatusUpdated()
            castSession?.let {
                _castLiveData.postValue(CastStatus().fromRemoteMediaClient(it.castDevice.friendlyName, it.remoteMediaClient))
            }
        }
    }

    private val castProgressListener = object: RemoteMediaClient.ProgressListener {
        override fun onProgressUpdated(progress: Long, duration: Long) {
            _castProgressLiveData.postValue(Pair(progress, duration))
        }
    }

    fun setupCastButton(mediaRouteButton: MediaRouteButton) {
        if (isPlayServiceAvailable) {
            this.mediaRouteButton = mediaRouteButton
            val selector = MediaRouteSelector.fromBundle(MediaRouteSelector.Builder()
                    .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
                    .build().asBundle())

            val router = MediaRouter.getInstance(context)
            router.addCallback(selector, object : MediaRouter.Callback() {
                override fun onRouteChanged(router: MediaRouter?, route: MediaRouter.RouteInfo?) {
                    super.onRouteChanged(router, route)
                    mediaRouteButton.visibility = View.VISIBLE
                    mediaRouteButton.routeSelector = selector
                }
            }, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY)

            CastButtonFactory.setUpMediaRouteButton(context.applicationContext, mediaRouteButton)
        }
    }

    fun setupCastSession() {
        try {
            isPlayServiceAvailable = GoogleApiAvailability
                    .getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
        } catch (ignored: Exception) {
        }

        if (isPlayServiceAvailable) {
            val castContext = CastContext.getSharedInstance(context.applicationContext)
            sessionManager = castContext.sessionManager
            if (castSession == null) {
                castSession = sessionManager?.currentCastSession
                sessionManager?.addSessionManagerListener(sessionManagerListener)
                castSession?.remoteMediaClient?.registerCallback(castCallback)
                castSession?.remoteMediaClient?.addProgressListener(castProgressListener, 100)
            } else {
                sessionManager?.currentCastSession?.let { castSession = it }
            }

        }
    }

    fun pauseCastSession() {
        sessionManager?.removeSessionManagerListener(sessionManagerListener)
        castSession?.remoteMediaClient?.unregisterCallback(castCallback)
        castSession?.remoteMediaClient?.removeProgressListener(castProgressListener)
        castSession = null
    }

    private val sessionManagerListener = object : com.google.android.gms.cast.framework.SessionManagerListener<Session> {
        override fun onSessionEnded(p0: Session?, p1: Int) {
            _customAction.postValue(Event(Constants.ACTION_CAST_DISCONNECTED))
            pauseCastSession()
            stopCastServer()
        }

        override fun onSessionEnding(p0: Session?) {}

        override fun onSessionResumeFailed(p0: Session?, p1: Int) {}

        override fun onSessionResumed(p0: Session?, p1: Boolean) {
            _customAction.postValue(Event(Constants.ACTION_CAST_CONNECTED))
            setupCastSession()
            mediaRouteButton?.visibility = View.VISIBLE
        }

        override fun onSessionResuming(p0: Session?, p1: String?) {
            startCastServer()
        }

        override fun onSessionStartFailed(p0: Session?, p1: Int) {}

        override fun onSessionStarted(p0: Session?, p1: String?) {
            _customAction.postValue(Event(Constants.ACTION_CAST_CONNECTED))
            setupCastSession()
            mediaRouteButton?.visibility = View.VISIBLE
        }

        override fun onSessionStarting(p0: Session?) {
            startCastServer()
        }

        override fun onSessionSuspended(p0: Session?, p1: Int) {
            stopCastServer()
        }
    }

    private fun startCastServer() {
        castServer = CastServer(context)
        try {
            castServer?.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun stopCastServer() {
        castServer?.stop()
    }

}
