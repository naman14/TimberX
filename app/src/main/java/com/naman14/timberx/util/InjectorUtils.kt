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
package com.naman14.timberx.util

import android.content.ComponentName
import android.content.Context
import com.naman14.timberx.MediaSessionConnection
import com.naman14.timberx.TimberMusicService
import com.naman14.timberx.models.MediaID
import com.naman14.timberx.ui.viewmodels.MainViewModel
import com.naman14.timberx.ui.viewmodels.MediaItemFragmentViewModel
import com.naman14.timberx.ui.viewmodels.NowPlayingViewModel
import com.naman14.timberx.ui.viewmodels.SearchViewModel

/**
 * Static methods used to inject classes needed for various Activities and Fragments.
 */
object InjectorUtils {
    private fun provideMediaSessionConnection(context: Context): MediaSessionConnection {
        return MediaSessionConnection.getInstance(context,
                ComponentName(context, TimberMusicService::class.java))
    }

    fun provideMainActivityViewModel(context: Context): MainViewModel.Factory {
        val applicationContext = context.applicationContext
        val mediaSessionConnection = provideMediaSessionConnection(applicationContext)
        return MainViewModel.Factory(context, mediaSessionConnection)
    }

    fun provideMediaItemFragmentViewModel(context: Context, mediaId: MediaID): MediaItemFragmentViewModel.Factory {
        val applicationContext = context.applicationContext
        val mediaSessionConnection = provideMediaSessionConnection(applicationContext)
        return MediaItemFragmentViewModel.Factory(mediaId, mediaSessionConnection)
    }

    fun provideNowPlayingViewModel(context: Context): NowPlayingViewModel.Factory {
        val applicationContext = context.applicationContext
        val mediaSessionConnection = provideMediaSessionConnection(applicationContext)
        return NowPlayingViewModel.Factory(mediaSessionConnection)
    }

    fun provideSearchViewModel(context: Context): SearchViewModel.Factory {
        return SearchViewModel.Factory(context)
    }
}
