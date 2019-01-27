package com.naman14.timberx.ui.viewmodels

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.*
import com.naman14.timberx.MediaSessionConnection
import com.naman14.timberx.util.Constants
import com.naman14.timberx.models.MediaData
import com.naman14.timberx.ui.listeners.PopupMenuListener

class NowPlayingViewModel(mediaSessionConnection: MediaSessionConnection
) : ViewModel() {

    private val _currentData = MutableLiveData<MediaData>()
    val currentData: LiveData<MediaData> = _currentData

    private val playbackStateObserver = Observer<PlaybackStateCompat> { playbackState ->
        playbackState?.let {
            _currentData.postValue(_currentData.value?.fromPlaybackState(it)
                    ?: MediaData().fromPlaybackState(it))
        }
    }

    private val mediaMetadataObserver = Observer<MediaMetadataCompat> { mediaMetaData ->
        mediaMetaData?.let {
            _currentData.postValue(_currentData.value?.fromMediaMetadata(it)
                    ?: MediaData().fromMediaMetadata(it))
        }
    }

    private val mediaSessionConnection = mediaSessionConnection.also {
        it.playbackState.observeForever(playbackStateObserver)
        it.nowPlaying.observeForever(mediaMetadataObserver)
    }

    private fun setSavedDBData() {
        //set media data and state saved in db to the media session when connected
        mediaSessionConnection.isConnected.observeForever { connected ->
            if (connected) {
                mediaSessionConnection.transportControls.sendCustomAction(Constants.ACTION_SET_MEDIA_STATE, null)

            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaSessionConnection.playbackState.removeObserver(playbackStateObserver)
        mediaSessionConnection.nowPlaying.removeObserver(mediaMetadataObserver)
    }

    class Factory(private val mediaSessionConnection: MediaSessionConnection
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return NowPlayingViewModel(mediaSessionConnection).apply { setSavedDBData() } as T
        }
    }
}
