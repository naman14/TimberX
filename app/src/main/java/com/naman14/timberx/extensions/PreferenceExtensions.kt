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
package com.naman14.timberx.extensions

import android.app.Activity
import androidx.core.content.edit
import com.naman14.timberx.R
import com.naman14.timberx.constants.Constants.START_PAGE_PREFERENCE
import com.naman14.timberx.constants.Constants.THEME_PREFERENCE

fun Activity.getCurrentTheme(): Int {
    return when (defaultPrefs().getString(THEME_PREFERENCE, "light")) {
        "light" -> return R.style.AppTheme_Light
        "dark" -> return R.style.AppTheme_Dark
        "black" -> R.style.AppTheme_Black
        else -> return R.style.AppTheme_Light
    }
}

fun Activity.getStartPageIndex(): Int {
    return when (defaultPrefs().getString(START_PAGE_PREFERENCE, "last_opened")) {
        "last_opened" -> 0
        "songs" -> 1
        "albums" -> 2
        "playlists" -> 3
        "artists" -> 4
        "folders" -> 5
        "genres" -> 6
        else -> 0
    }
}

fun Activity.saveCurrentPage(index: Int) {
    defaultPrefs().edit {
        putString(START_PAGE_PREFERENCE,
                when (index) {
                    0 -> "last_opened"
                    1 -> "songs"
                    2 -> "albums"
                    3 -> "playlists"
                    4 -> "artists"
                    5 -> "folders"
                    6 -> "genres"
                    else -> "last_opened"
                }
        )
    }
}
