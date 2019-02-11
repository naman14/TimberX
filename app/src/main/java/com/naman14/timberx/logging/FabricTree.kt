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
package com.naman14.timberx.logging

import com.crashlytics.android.Crashlytics
import timber.log.Timber

/** @author Aidan Follestad (@afollestad) */
class FabricTree : Timber.Tree() {

    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?
    ) {
        try {
            if (t != null) {
                Crashlytics.setString("crash_tag", tag)
                Crashlytics.logException(t)
            } else {
                Crashlytics.log(priority, tag, message)
            }
        } catch (e: IllegalStateException) {
            // TODO this is caught so that Robolelectric tests which test classes that make use of Timber don't crash.
            // TODO they crash because Robolectric initializes the app and this tree in release configurations,
            // TODO and calls to Timber logging ends up here.
            // TODO we should maybe somehow avoid adding this tree to Timber in the context of Robolectric tests,
            // TODO somehow?
            e.printStackTrace()
        }
    }
}
