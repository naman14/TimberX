package com.naman14.timberx.ui.viewmodels

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaBrowserCompat.SubscriptionCallback
import androidx.lifecycle.*
import com.naman14.timberx.MediaSessionConnection
import com.naman14.timberx.models.MediaID

class MediaItemFragmentViewModel(private val mediaId: MediaID,
                                 mediaSessionConnection: MediaSessionConnection) : ViewModel() {

    private val _mediaItems = MutableLiveData<List<MediaBrowserCompat.MediaItem>>()
            .apply { postValue(emptyList()) }

    val mediaItems: LiveData<List<MediaItem>> = _mediaItems

    private val subscriptionCallback = object : SubscriptionCallback() {
        override fun onChildrenLoaded(parentId: String, children: List<MediaItem>) {
            _mediaItems.postValue(children)
        }
    }

    private val mediaSessionConnection = mediaSessionConnection.also {
        it.subscribe(mediaId.asString(), subscriptionCallback)
    }

    //hacky way to force reload items (e.g. song sort order changed)
    fun reloadMediaItems() {
        mediaSessionConnection.unsubscribe(mediaId.asString(), subscriptionCallback)
        mediaSessionConnection.subscribe(mediaId.asString(), subscriptionCallback)
    }

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
