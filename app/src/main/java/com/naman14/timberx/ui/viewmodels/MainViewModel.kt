package com.naman14.timberx.ui.viewmodels

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import androidx.lifecycle.*
import com.naman14.timberx.MediaSessionConnection
import com.naman14.timberx.util.*

class MainViewModel(private val mediaSessionConnection: MediaSessionConnection) : ViewModel() {

    class Factory(private val mediaSessionConnection: MediaSessionConnection) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MainViewModel(mediaSessionConnection) as T
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

    /**
     * [navigateToMediaItem] acts as an "event", rather than state. [Observer]s
     * are notified of the change as usual with [LiveData], but only one [Observer]
     * will actually read the data. For more information, check the [Event] class.
     */
    val navigateToMediaItem: LiveData<Event<MediaID>> get() = _navigateToMediaItem
    private val _navigateToMediaItem = MutableLiveData<Event<MediaID>>()

    /**
     * This method takes a [MediaItemData] and routes it depending on whether it's
     * browsable (i.e.: it's the parent media item of a set of other media items,
     * such as an album), or not.
     *
     * If the item is browsable, handle it by sending an event to the Activity to
     * browse to it, otherwise play it.
     */
    fun mediaItemClicked(clickedItem: MediaBrowserCompat.MediaItem, extras: Bundle?) {
        if (clickedItem.isBrowsable) {
            browseToItem(clickedItem)
        } else {
            playMedia(clickedItem, extras)
        }
    }

    /**
     * This posts a browse [Event] that will be handled by the
     * observer in [MainActivity].
     */
    private fun browseToItem(mediaItem: MediaBrowserCompat.MediaItem) {
        _navigateToMediaItem.value = Event(MediaID().fromString(mediaItem.mediaId!!))
    }

    /**
     * This method takes a [MediaItemData] and does one of the following:
     * - If the item is *not* the active item, then play it directly.
     * - If the item *is* the active item, check whether "pause" is a permitted command. If it is,
     *   then pause playback, otherwise send "play" to resume playback.
     */
    fun playMedia(mediaItem: MediaBrowserCompat.MediaItem, extras: Bundle?) {
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

}
