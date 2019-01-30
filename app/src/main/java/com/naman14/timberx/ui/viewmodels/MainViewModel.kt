package com.naman14.timberx.ui.viewmodels

import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.naman14.timberx.MediaSessionConnection
import com.naman14.timberx.MusicUtils
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


}
