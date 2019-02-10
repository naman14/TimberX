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
package com.naman14.timberx.ui.viewmodels

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.naman14.timberx.playback.MediaSessionConnection
import com.naman14.timberx.constants.Constants.ACTION_SET_MEDIA_STATE
import com.naman14.timberx.models.MediaData
import com.naman14.timberx.models.QueueData
import io.reactivex.disposables.Disposable

class NowPlayingViewModel(
    mediaSessionConnection: MediaSessionConnection
) : ViewModel() {

    private var albumArtDisposable: Disposable? = null

    private val _currentData = MutableLiveData<MediaData>()
    val currentData: LiveData<MediaData> = _currentData

    private val _queueData = MutableLiveData<QueueData>()
    val queueData: LiveData<QueueData> = _queueData

    private val playbackStateObserver = Observer<PlaybackStateCompat> { playbackState ->
        playbackState?.let {
            _currentData.postValue(_currentData.value?.pullPlaybackState(it)
                    ?: MediaData().pullPlaybackState(it))
        }
    }

    private val mediaMetadataObserver = Observer<MediaMetadataCompat> { mediaMetaData ->
        mediaMetaData?.let {
            val newValue = _currentData.value?.pullMediaMetadata(it)
                    ?: MediaData().pullMediaMetadata(it)
            _currentData.postValue(newValue)
        }
    }

    private val queueDataObserver = Observer<QueueData> { queueData ->
        queueData?.let {
            _queueData.postValue(queueData)
        }
    }

    private val mediaSessionConnection = mediaSessionConnection.also {
        it.playbackState.observeForever(playbackStateObserver)
        it.nowPlaying.observeForever(mediaMetadataObserver)
        it.queueData.observeForever(queueDataObserver)

        //set media data and state saved in db to the media session when connected
        it.isConnected.observeForever { connected ->
            if (connected) {
                it.transportControls.sendCustomAction(ACTION_SET_MEDIA_STATE, null)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaSessionConnection.playbackState.removeObserver(playbackStateObserver)
        mediaSessionConnection.nowPlaying.removeObserver(mediaMetadataObserver)
        albumArtDisposable?.dispose()
    }
}
