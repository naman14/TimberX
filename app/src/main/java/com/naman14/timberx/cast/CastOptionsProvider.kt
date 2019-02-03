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
package com.naman14.timberx.cast

import android.content.Context

import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions
import com.google.android.gms.cast.framework.media.MediaIntentReceiver
import com.google.android.gms.cast.framework.media.NotificationOptions
import com.naman14.timberx.R
import com.naman14.timberx.ui.activities.ExpandedControlsActivity

import java.util.ArrayList

class CastOptionsProvider : OptionsProvider {

    override fun getCastOptions(context: Context): CastOptions {
        val buttonActions = ArrayList<String>()
        buttonActions.add(MediaIntentReceiver.ACTION_TOGGLE_PLAYBACK)
        buttonActions.add(MediaIntentReceiver.ACTION_STOP_CASTING)
        val compatButtonActionsIndicies = intArrayOf(0, 1)

        val notificationOptions = NotificationOptions.Builder()
                .setActions(buttonActions, compatButtonActionsIndicies)
                .setTargetActivityClassName(ExpandedControlsActivity::class.java!!.getName())
                .build()

        val mediaOptions = CastMediaOptions.Builder()
                .setNotificationOptions(notificationOptions)
                .setExpandedControllerActivityClassName(ExpandedControlsActivity::class.java!!.getName())
                .build()

        return CastOptions.Builder()
                .setReceiverApplicationId(context.getString(R.string.cast_app_id))
                .setCastMediaOptions(mediaOptions)
                .build()
    }

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? {
        return null
    }
}
