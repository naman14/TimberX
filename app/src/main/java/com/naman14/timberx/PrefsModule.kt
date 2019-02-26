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
package com.naman14.timberx

import android.app.Application
import android.os.Environment.DIRECTORY_MUSIC
import android.os.Environment.getExternalStoragePublicDirectory
import com.afollestad.rxkprefs.RxkPrefs
import com.afollestad.rxkprefs.rxkPrefs
import com.naman14.timberx.constants.AlbumSortOrder
import com.naman14.timberx.constants.AlbumSortOrder.ALBUM_A_Z
import com.naman14.timberx.constants.AppThemes
import com.naman14.timberx.constants.AppThemes.LIGHT
import com.naman14.timberx.constants.SongSortOrder
import com.naman14.timberx.constants.SongSortOrder.SONG_A_Z
import com.naman14.timberx.constants.StartPage
import com.naman14.timberx.constants.StartPage.SONGS
import org.koin.dsl.module.module

const val PREF_APP_THEME = "theme_preference"
const val PREF_SONG_SORT_ORDER = "song_sort_order"
const val PREF_ALBUM_SORT_ORDER = "album_sort_order"
const val PREF_START_PAGE = "start_page_preference"
const val PREF_LAST_FOLDER = "last_folder"

val prefsModule = module {
    single { rxkPrefs(get<Application>()) }

    factory(name = PREF_SONG_SORT_ORDER) {
        get<RxkPrefs>().enum(PREF_SONG_SORT_ORDER, SONG_A_Z,
                SongSortOrder.Companion::fromString, SongSortOrder.Companion::toString)
    }

    factory(name = PREF_ALBUM_SORT_ORDER) {
        get<RxkPrefs>().enum(PREF_ALBUM_SORT_ORDER, ALBUM_A_Z,
                AlbumSortOrder.Companion::fromString, AlbumSortOrder.Companion::toString)
    }

    factory(name = PREF_APP_THEME) {
        get<RxkPrefs>().enum(PREF_APP_THEME, LIGHT,
                AppThemes.Companion::fromString, AppThemes.Companion::toString)
    }

    factory(name = PREF_START_PAGE) {
        get<RxkPrefs>().enum(PREF_START_PAGE, SONGS,
                StartPage.Companion::fromString, StartPage.Companion::toString)
    }

    factory(name = PREF_LAST_FOLDER) {
        val defaultFolder = getExternalStoragePublicDirectory(DIRECTORY_MUSIC).path
        get<RxkPrefs>().string(PREF_LAST_FOLDER, defaultFolder)
    }
}
