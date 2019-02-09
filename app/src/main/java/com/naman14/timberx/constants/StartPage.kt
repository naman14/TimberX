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

enum class StartPage(val index: Int) {
    SONGS(0),
    ALBUMS(1),
    PLAYLISTS(2),
    ARTISTS(3),
    FOLDERS(4),
    GENRES(5);

    companion object {
        fun fromString(raw: String): StartPage {
            return StartPage.values().single { it.name.toLowerCase() == raw }
        }

        fun fromIndex(index: Int): StartPage {
            return StartPage.values().single { it.index == index }
        }

        fun toString(value: StartPage): String = value.name.toLowerCase()
    }
}
