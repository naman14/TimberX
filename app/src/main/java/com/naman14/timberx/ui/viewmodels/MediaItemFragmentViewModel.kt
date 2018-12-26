package com.naman14.timberx.ui.viewmodels

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaBrowserCompat.SubscriptionCallback
import androidx.lifecycle.*
import com.naman14.timberx.MediaSessionConnection
import com.naman14.timberx.util.MediaID

/**
 * [ViewModel] for [MediaItemFragment].
 */
class MediaItemFragmentViewModel(private val mediaId: MediaID,
                                 mediaSessionConnection: MediaSessionConnection) : ViewModel() {

    /**
     * Use a backing property so consumers of mediaItems only get a [LiveData] instance so
     * they don't inadvertently modify it.
     */
    private val _mediaItems = MutableLiveData<List<MediaBrowserCompat.MediaItem>>()
            .apply { postValue(emptyList()) }

    val mediaItems: LiveData<List<MediaItem>> = _mediaItems

    private val subscriptionCallback = object : SubscriptionCallback() {
        override fun onChildrenLoaded(parentId: String, children: List<MediaItem>) {
            _mediaItems.postValue(children)
        }
    }


    /**
     * Because there's a complex dance between this [ViewModel] and the [MediaSessionConnection]
     * (which is wrapping a [MediaBrowserCompat] object), the usual guidance of using
     * [Transformations] doesn't quite work.
     *
     * Specifically there's three things that are watched that will cause the single piece of
     * [LiveData] exposed from this class to be updated.
     *
     * [subscriptionCallback] (defined above) is called if/when the children of this
     * ViewModel's [mediaId] changes.
     *
     * [MediaSessionConnection.playbackState] changes state based on the playback state of
     * the player, which can change the [MediaItemData.playbackRes]s in the list.
     *
     * [MediaSessionConnection.nowPlaying] changes based on the item that's being played,
     * which can also change the [MediaItemData.playbackRes]s in the list.
     */
    private val mediaSessionConnection = mediaSessionConnection.also {
        it.subscribe(mediaId.asString(), subscriptionCallback)
    }

    /**
     * Since we use [LiveData.observeForever] above (in [mediaSessionConnection]), we want
     * to call [LiveData.removeObserver] here to prevent leaking resources when the [ViewModel]
     * is not longer in use.
     *
     * For more details, see the kdoc on [mediaSessionConnection] above.
     */
    override fun onCleared() {
        super.onCleared()
        // And then, finally, unsubscribe the media ID that was being watched.
        mediaSessionConnection.unsubscribe(mediaId.asString(), subscriptionCallback)
    }

    class Factory(private val mediaId: MediaID,
                  private val mediaSessionConnection: MediaSessionConnection) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MediaItemFragmentViewModel(mediaId, mediaSessionConnection) as T
        }
    }
}
