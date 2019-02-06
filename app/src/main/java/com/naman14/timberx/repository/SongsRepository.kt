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
package com.naman14.timberx.repository

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
import com.naman14.timberx.constants.Constants.SONG_SORT_ORDER
import com.naman14.timberx.constants.SongSortOrder.SONG_A_Z
import com.naman14.timberx.extensions.defaultPrefs
import com.naman14.timberx.extensions.mapList
import com.naman14.timberx.models.MediaID
import com.naman14.timberx.models.Song

// TODO make this a normal class that is injected with DI
object SongsRepository {

    fun loadSongs(context: Context, caller: String?): List<Song> {
        MediaID.currentCaller = caller
        return makeSongCursor(context, null, null)
                .mapList(true, Song.Companion::fromCursor)
    }

    fun getSongForId(context: Context, id: Long): Song {
        val songs = makeSongCursor(context, "_id = $id", null)
                .mapList(true, Song.Companion::fromCursor)
        return songs.firstOrNull() ?: Song()
    }

    fun getSongsForIDs(context: Context, idList: LongArray): List<Song> {
        var selection = "_id IN ("
        for (id in idList) {
            selection += "$id,"
        }
        if (idList.isNotEmpty()) {
            selection = selection.substring(0, selection.length - 1)
        }
        selection += ")"

        return makeSongCursor(context, selection, null)
                .mapList(true, Song.Companion::fromCursor)
    }

    private fun makeSongCursor(context: Context, selection: String?, paramArrayOfString: Array<String>?): Cursor {
        val songSortOrder = context.defaultPrefs().getString(SONG_SORT_ORDER, SONG_A_Z)
        return makeSongCursor(context, selection, paramArrayOfString, songSortOrder)
    }

    @SuppressLint("Recycle")
    private fun makeSongCursor(context: Context, selection: String?, paramArrayOfString: Array<String>?, sortOrder: String?): Cursor {
        val selectionStatement = StringBuilder("is_music=1 AND title != ''")
        if (!selection.isNullOrEmpty()) {
            selectionStatement.append("$selectionStatement AND $selection")
        }
        val projection = arrayOf("_id", "title", "artist", "album", "duration", "track", "artist_id", "album_id")

        return context.contentResolver.query(
                EXTERNAL_CONTENT_URI,
                projection,
                selectionStatement.toString(),
                paramArrayOfString,
                sortOrder
        ) ?: throw IllegalStateException("Unable to query $EXTERNAL_CONTENT_URI, system returned null.")
    }

    fun getSongFromPath(songPath: String, context: Context): Song {
        val cr = context.contentResolver

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.DATA
        val selectionArgs = arrayOf(songPath)
        val projection = arrayOf("_id", "title", "artist", "album", "duration", "track", "artist_id", "album_id")
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        return cr.query(uri, projection, "$selection=?", selectionArgs, sortOrder)?.use {
            if (it.moveToFirst() && it.count > 0) {
                Song.fromCursor(it)
            } else {
                Song()
            }
        } ?: throw IllegalStateException("Unable to query $uri, system returned null.")
    }

    fun searchSongs(context: Context, searchString: String, limit: Int): List<Song> {
        val result = makeSongCursor(context, "title LIKE ?", arrayOf("$searchString%"))
                .mapList(true, Song.Companion::fromCursor)
        if (result.size < limit) {
            val moreSongs = makeSongCursor(context, "title LIKE ?", arrayOf("%_$searchString%"))
                    .mapList(true, Song.Companion::fromCursor)
            result += moreSongs
        }
        return if (result.size < limit) {
            result
        } else {
            result.subList(0, limit)
        }
    }
}
