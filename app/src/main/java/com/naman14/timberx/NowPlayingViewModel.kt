package com.naman14.timberx

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.*
import com.naman14.timberx.vo.MediaData

/**
 * [ViewModel] for [MediaItemFragment].
 */
class NowPlayingViewModel(mediaSessionConnection: MediaSessionConnection
) : ViewModel() {

    private val _currentData = MutableLiveData<MediaData>()
    val currentData: LiveData<MediaData> = _currentData

    private val playbackStateObserver = Observer<PlaybackStateCompat> { playbackState ->
        playbackState?.let {
            _currentData.postValue(_currentData.value?.fromPlaybackState(it) ?: MediaData().fromPlaybackState(it))
        }
    }

    private val mediaMetadataObserver = Observer<MediaMetadataCompat> { mediaMetaData ->
        mediaMetaData?.let {
            _currentData.postValue(_currentData.value?.fromMediaMetadata(it) ?: MediaData().fromMediaMetadata(it))
        }
    }

    private val mediaSessionConnection = mediaSessionConnection.also {
        it.playbackState.observeForever(playbackStateObserver)
        it.nowPlaying.observeForever(mediaMetadataObserver)
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
            return NowPlayingViewModel(mediaSessionConnection) as T
        }
    }
}

private const val TAG = "MediaItemFragmentVM"
private const val NO_RES = 0
