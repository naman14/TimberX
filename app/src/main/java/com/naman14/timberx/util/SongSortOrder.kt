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

import android.provider.MediaStore

interface SongSortOrder {
    companion object {

        /* Song sort order A-Z */
        val SONG_A_Z = MediaStore.Audio.Media.DEFAULT_SORT_ORDER

        /* Song sort order Z-A */
        val SONG_Z_A = "$SONG_A_Z DESC"

        /* Song sort order artist */
        val SONG_ARTIST = MediaStore.Audio.Media.ARTIST

        /* Song sort order album */
        val SONG_ALBUM = MediaStore.Audio.Media.ALBUM

        /* Song sort order year */
        val SONG_YEAR = MediaStore.Audio.Media.YEAR + " DESC"

        /* Song sort order duration */
        val SONG_DURATION = MediaStore.Audio.Media.DURATION + " DESC"

        /* Song sort order date */
        val SONG_DATE = MediaStore.Audio.Media.DATE_ADDED + " DESC"

        /* Song sort order filename */
        val SONG_FILENAME = MediaStore.Audio.Media.DATA
    }
}
