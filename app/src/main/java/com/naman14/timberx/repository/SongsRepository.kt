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

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
import com.naman14.timberx.models.MediaID
import com.naman14.timberx.models.Song
import com.naman14.timberx.util.Constants
import com.naman14.timberx.util.SongSortOrder
import com.naman14.timberx.util.extensions.defaultPrefs

object SongsRepository {

    fun loadSongs(context: Context, caller: String?): ArrayList<Song> {
        MediaID.currentCaller = caller
        return getSongsForCursor(makeSongCursor(context, null, null))
    }

    fun getSongForId(context: Context, id: Long): Song {
        val songs = getSongsForCursor(makeSongCursor(context, "_id = $id", null))
        return if (songs.isNotEmpty()) {
            songs[0]
        } else {
            Song()
        }
    }

    fun getSongsForIDs(context: Context, idList: LongArray): ArrayList<Song> {
        var selection = "_id IN ("
        for (id in idList) {
            selection += "$id,"
        }
        if (idList.isNotEmpty()) {
            selection = selection.substring(0, selection.length - 1)
        }
        selection += ")"

        val cursor = makeSongCursor(context, selection, null)
        return getSongsForCursor(cursor)
    }

    private fun getSongsForCursor(cursor: Cursor?): ArrayList<Song> {
        val arrayList = ArrayList<Song>()
        if (cursor != null && cursor.moveToFirst())
            do {
                val id = cursor.getLong(0)
                val title = cursor.getString(1)
                val artist = cursor.getString(2)
                val album = cursor.getString(3)
                val duration = cursor.getInt(4)
                val trackNumber = cursor.getInt(5)
                val artistId = cursor.getLong(6)
                val albumId = cursor.getLong(7)
                arrayList.add(Song(id, albumId, artistId, title, artist, album, duration, trackNumber))
            } while (cursor.moveToNext())
        cursor?.close()
        return arrayList
    }

    private fun makeSongCursor(context: Context, selection: String?, paramArrayOfString: Array<String>?): Cursor {
        val songSortOrder = context.defaultPrefs().getString(Constants.SONG_SORT_ORDER, SongSortOrder.SONG_A_Z)
        return makeSongCursor(context, selection, paramArrayOfString, songSortOrder)
    }

    private fun makeSongCursor(context: Context, selection: String?, paramArrayOfString: Array<String>?, sortOrder: String?): Cursor {
        var selectionStatement = "is_music=1 AND title != ''"

        if (!selection.isNullOrEmpty()) {
            selectionStatement = "$selectionStatement AND $selection"
        }

        val projection = arrayOf("_id", "title", "artist", "album", "duration", "track", "artist_id", "album_id")
        return context.contentResolver.query(
                EXTERNAL_CONTENT_URI,
                projection,
                selectionStatement,
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
            if (it.count > 0) {
                val song = getSongsForCursor(it)
                song[0]
            } else
                Song()
        } ?: throw IllegalStateException("Unable to query $uri, system returned null.")
    }

    fun searchSongs(context: Context, searchString: String, limit: Int): List<Song> {
        val result = getSongsForCursor(makeSongCursor(context, "title LIKE ?", arrayOf("$searchString%")))
        if (result.size < limit) {
            result.addAll(getSongsForCursor(makeSongCursor(context, "title LIKE ?", arrayOf("%_$searchString%"))))
        }
        return if (result.size < limit) result else result.subList(0, limit)
    }
}
