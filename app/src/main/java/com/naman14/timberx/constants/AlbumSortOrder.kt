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

enum class AlbumSortOrder(val rawValue: String) {
    /* Album sort order A-Z */
    ALBUM_A_Z(MediaStore.Audio.Albums.DEFAULT_SORT_ORDER),
    /* Album sort order Z-A */
    ALBUM_Z_A(MediaStore.Audio.Albums.DEFAULT_SORT_ORDER + " DESC"),
    /* Album sort order songs */
    ALBUM_NUMBER_OF_SONGS(MediaStore.Audio.Albums.NUMBER_OF_SONGS + " DESC"),
    /* Album sort order year */
    ALBUM_YEAR(MediaStore.Audio.Albums.FIRST_YEAR + " DESC");

    companion object {
        fun fromString(raw: String): AlbumSortOrder {
            return AlbumSortOrder.values().single { it.rawValue == raw }
        }

        fun toString(value: AlbumSortOrder): String = value.rawValue
    }
}
