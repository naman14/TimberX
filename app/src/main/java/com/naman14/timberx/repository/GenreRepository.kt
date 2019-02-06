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
import com.naman14.timberx.models.Genre
import android.provider.MediaStore
import com.naman14.timberx.models.MediaID
import com.naman14.timberx.models.Song

// TODO make this a normal class that is injected with DI
object GenreRepository {

    private var mCursor: Cursor? = null

    fun getAllGenres(context: Context, caller: String?): ArrayList<Genre> {
        MediaID.currentCaller = caller

        val mGenreList = java.util.ArrayList<Genre>()

        mCursor = makeGenreCusror(context)

        if (mCursor != null && mCursor!!.moveToFirst()) {
            do {

                val id = mCursor!!.getLong(0)

                val name = mCursor!!.getString(1)

                val songCount = getSongCountForGenre(context, id)

                val genre = Genre(id, name, songCount)

                mGenreList.add(genre)
            } while (mCursor!!.moveToNext())
        }
        if (mCursor != null) {
            mCursor!!.close()
            mCursor = null
        }
        return mGenreList
    }

    private fun makeGenreCusror(context: Context): Cursor? {
        val uri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI
        val columns = arrayOf(MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME)
        val orderBy = MediaStore.Audio.Genres.NAME

        return context.contentResolver.query(uri, columns, null, null, orderBy)
    }

    private fun getSongCountForGenre(context: Context, genreID: Long): Int {
        val uri = MediaStore.Audio.Genres.Members.getContentUri("external", genreID)
        val c = context.contentResolver.query(uri, null, null, null, null)

        if (c == null || c.count == 0)
            return -1

        val num = c.count
        c.close()

        return num
    }

    fun getSongsForGenre(context: Context, genreID: Long, caller: String?): ArrayList<Song> {
        MediaID.currentCaller = caller
        val cursor = makeGenreSongCursor(context, genreID)
        val songsList = arrayListOf<Song>()
        if (cursor != null && cursor.moveToFirst())
            do {
                val id = cursor.getLong(0)
                val title = cursor.getString(1)
                val artist = cursor.getString(2)
                val album = cursor.getString(3)
                val duration = cursor.getInt(4)
                val trackNumber = cursor.getInt(5)
                val albumId = cursor.getInt(6).toLong()
                val artistId = cursor.getInt(7).toLong()

                songsList.add(Song(id, albumId, artistId, title, artist, album, duration, trackNumber))
            } while (cursor.moveToNext())
        cursor?.close()
        return songsList
    }

    private fun makeGenreSongCursor(context: Context, genreID: Long): Cursor? {
        val contentResolver = context.contentResolver
        val artistSongSortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        val uri = MediaStore.Audio.Genres.Members.getContentUri("external", genreID)
        return contentResolver.query(uri, arrayOf("_id", "title", "artist", "album", "duration", "track", "album_id", "artist_id"), null, null, artistSongSortOrder)
    }
}
