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

import android.content.ContentResolver
import android.database.Cursor
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Genres.NAME
import android.provider.MediaStore.Audio.Genres._ID
import android.provider.MediaStore.Audio.Media.DEFAULT_SORT_ORDER
import com.naman14.timberx.extensions.mapList
import com.naman14.timberx.extensions.value
import com.naman14.timberx.models.Genre
import com.naman14.timberx.models.MediaID
import com.naman14.timberx.models.Song

interface GenreRepository {

    fun getAllGenres(caller: String?): List<Genre>

    fun getSongsForGenre(genreId: Long, caller: String?): List<Song>
}

class RealGenreRepository(
    private val contentResolver: ContentResolver
) : GenreRepository {

    override fun getAllGenres(caller: String?): List<Genre> {
        MediaID.currentCaller = caller
        return makeGenreCursor().mapList(true) {
            val id: Long = value(_ID)
            val songCount = getSongCountForGenre(id)
            Genre.fromCursor(this, songCount)
        }.filter { it.songCount > 0 }
    }

    override fun getSongsForGenre(genreId: Long, caller: String?): List<Song> {
        MediaID.currentCaller = caller
        return makeGenreSongCursor(genreId)
                .mapList(true) { Song.fromCursor(this) }
    }

    private fun makeGenreCursor(): Cursor? {
        val uri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI
        val projection = arrayOf(_ID, NAME)
        return contentResolver.query(uri, projection, null, null, NAME)
    }

    private fun getSongCountForGenre(genreID: Long): Int {
        val uri = MediaStore.Audio.Genres.Members.getContentUri("external", genreID)
        return contentResolver.query(uri, null, null, null, null)?.use {
            it.moveToFirst()
            if (it.count == 0) {
                -1
            } else {
                it.count
            }
        } ?: -1
    }

    private fun makeGenreSongCursor(genreID: Long): Cursor? {
        val uri = MediaStore.Audio.Genres.Members.getContentUri("external", genreID)
        val projection = arrayOf("_id", "title", "artist", "album", "duration", "track", "album_id", "artist_id")
        return contentResolver.query(uri, projection, null, null, DEFAULT_SORT_ORDER)
    }
}
