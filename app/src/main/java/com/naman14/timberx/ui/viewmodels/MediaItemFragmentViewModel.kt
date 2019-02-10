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

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaBrowserCompat.SubscriptionCallback
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.naman14.timberx.playback.MediaSessionConnection
import com.naman14.timberx.models.MediaID

class MediaItemFragmentViewModel(
    private val mediaId: MediaID,
    mediaSessionConnection: MediaSessionConnection
) : ViewModel() {

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
}
