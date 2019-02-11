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
import android.content.ContentResolver
import android.database.Cursor
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
import com.afollestad.rxkprefs.Pref
import com.naman14.timberx.constants.SongSortOrder
import com.naman14.timberx.extensions.mapList
import com.naman14.timberx.models.MediaID
import com.naman14.timberx.models.Song
import android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI as AUDIO_URI
import android.provider.BaseColumns._ID
import timber.log.Timber
import java.io.File

interface SongsRepository {

    fun loadSongs(caller: String?): List<Song>

    fun getSongForId(id: Long): Song

    fun getSongsForIds(idList: LongArray): List<Song>

    fun getSongFromPath(songPath: String): Song

    fun searchSongs(searchString: String, limit: Int): List<Song>

    fun deleteTracks(ids: LongArray): Int
}

class RealSongsRepository(
    private val contentResolver: ContentResolver,
    private val sortOrderPref: Pref<SongSortOrder>
) : SongsRepository {

    override fun loadSongs(caller: String?): List<Song> {
        MediaID.currentCaller = caller
        return makeSongCursor(null, null)
                .mapList(true) { Song.fromCursor(this) }
    }

    override fun getSongForId(id: Long): Song {
        val songs = makeSongCursor("_id = $id", null)
                .mapList(true) { Song.fromCursor(this) }
        return songs.firstOrNull() ?: Song()
    }

    override fun getSongsForIds(idList: LongArray): List<Song> {
        var selection = "_id IN ("
        for (id in idList) {
            selection += "$id,"
        }
        if (idList.isNotEmpty()) {
            selection = selection.substring(0, selection.length - 1)
        }
        selection += ")"

        return makeSongCursor(selection, null)
                .mapList(true) { Song.fromCursor(this) }
    }

    override fun getSongFromPath(songPath: String): Song {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.DATA
        val selectionArgs = arrayOf(songPath)
        val projection = arrayOf("_id", "title", "artist", "album", "duration", "track", "artist_id", "album_id")
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        return contentResolver.query(uri, projection, "$selection=?", selectionArgs, sortOrder)?.use {
            if (it.moveToFirst() && it.count > 0) {
                Song.fromCursor(it)
            } else {
                Song()
            }
        } ?: throw IllegalStateException("Unable to query $uri, system returned null.")
    }

    override fun searchSongs(searchString: String, limit: Int): List<Song> {
        val result = makeSongCursor("title LIKE ?", arrayOf("$searchString%"))
                .mapList(true) { Song.fromCursor(this) }
        if (result.size < limit) {
            val moreSongs = makeSongCursor("title LIKE ?", arrayOf("%_$searchString%"))
                    .mapList(true) { Song.fromCursor(this) }
            result += moreSongs
        }
        return if (result.size < limit) {
            result
        } else {
            result.subList(0, limit)
        }
    }

    // TODO a lot of operations are done here without verifying results,
    // TODO e.g. if moveToFirst() returns true...
    override fun deleteTracks(ids: LongArray): Int {
        val projection = arrayOf(
                _ID,
                MediaStore.MediaColumns.DATA,
                MediaStore.Audio.AudioColumns.ALBUM_ID
        )
        val selection = StringBuilder().apply {
            append("$_ID IN (")
            for (i in ids.indices) {
                append(ids[i])
                if (i < ids.size - 1) {
                    append(",")
                }
            }
            append(")")
        }

        contentResolver.query(
                AUDIO_URI,
                projection,
                selection.toString(),
                null,
                null
        )?.use {
            it.moveToFirst()
            // Step 2: Remove selected tracks from the database
            contentResolver.delete(AUDIO_URI, selection.toString(), null)

            // Step 3: Remove files from card
            it.moveToFirst()
            while (!it.isAfterLast) {
                val name = it.getString(1)
                val f = File(name)
                try { // File.delete can throw a security exception
                    if (!f.delete()) {
                        // I'm not sure if we'd ever get here (deletion would
                        // have to fail, but no exception thrown)
                        Timber.d("Failed to delete file: $name")
                    }
                } catch (_: SecurityException) {
                }
                it.moveToNext()
            }
        }

        return ids.size
    }

    private fun makeSongCursor(selection: String?, paramArrayOfString: Array<String>?): Cursor {
        return makeSongCursor(selection, paramArrayOfString, sortOrderPref.get().rawValue)
    }

    @SuppressLint("Recycle")
    private fun makeSongCursor(selection: String?, paramArrayOfString: Array<String>?, sortOrder: String?): Cursor {
        val selectionStatement = StringBuilder("is_music=1 AND title != ''")
        if (!selection.isNullOrEmpty()) {
            selectionStatement.append(" AND $selection")
        }
        val projection = arrayOf("_id", "title", "artist", "album", "duration", "track", "artist_id", "album_id")

        return contentResolver.query(
                EXTERNAL_CONTENT_URI,
                projection,
                selectionStatement.toString(),
                paramArrayOfString,
                sortOrder
        ) ?: throw IllegalStateException("Unable to query $EXTERNAL_CONTENT_URI, system returned null.")
    }
}
