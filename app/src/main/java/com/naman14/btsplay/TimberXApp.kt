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
@file:Suppress("unused")

package com.naman14.btsplay

import android.app.Application
import com.naman14.btsplay.BuildConfig.DEBUG
import com.naman14.btsplay.db.roomModule
import com.naman14.btsplay.logging.FabricTree
import com.naman14.btsplay.network.lastFmModule
import com.naman14.btsplay.network.lyricsModule
import com.naman14.btsplay.network.networkModule
import com.naman14.btsplay.notifications.notificationModule
import com.naman14.btsplay.permissions.permissionsModule
import com.naman14.btsplay.playback.mediaModule
import com.naman14.btsplay.repository.repositoriesModule
import com.naman14.btsplay.ui.viewmodels.viewModelsModule
import org.koin.android.ext.android.startKoin
import timber.log.Timber

class TimberXApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(FabricTree())
        }

        val modules = listOf(
                mainModule,
                permissionsModule,
                mediaModule,
                prefsModule,
                networkModule,
                roomModule,
                notificationModule,
                repositoriesModule,
                viewModelsModule,
                lyricsModule,
                lastFmModule
        )
        startKoin(
                androidContext = this,
                modules = modules
        )
    }
}
