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
package com.naman14.timberx.constants

import android.provider.MediaStore

enum class SongSortOrder(val rawValue: String) {
    /* Song sort order A-Z */
    SONG_A_Z(MediaStore.Audio.Media.DEFAULT_SORT_ORDER),
    /* Song sort order Z-A */
    SONG_Z_A(MediaStore.Audio.Media.DEFAULT_SORT_ORDER + " DESC"),
    /* Song sort order year */
    SONG_YEAR(MediaStore.Audio.Media.YEAR + " DESC"),
    /* Song sort order duration */
    SONG_DURATION(MediaStore.Audio.Media.DURATION + " DESC");

    companion object {
        fun fromString(raw: String): SongSortOrder {
            return SongSortOrder.values().single { it.rawValue == raw }
        }

        fun toString(value: SongSortOrder): String = value.rawValue
    }
}
