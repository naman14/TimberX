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

import com.naman14.timberx.models.Album
import com.naman14.timberx.models.MediaID
import com.naman14.timberx.models.Song

object AlbumRepository {

    /* Album song sort order track list */
    private const val SONG_TRACK_LIST = (MediaStore.Audio.Media.TRACK + ", " +
            MediaStore.Audio.Media.DEFAULT_SORT_ORDER)

    fun getAlbum(cursor: Cursor?): Album {
        var album = Album()
        if (cursor != null) {
            if (cursor.moveToFirst())
                album = Album(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getLong(3), cursor.getInt(4), cursor.getInt(5))
        }
        cursor?.close()
        return album
    }

    fun getAlbumsForCursor(cursor: Cursor?): ArrayList<Album> {
        val arrayList = arrayListOf<Album>()
        if (cursor != null && cursor.moveToFirst())
            do {
                arrayList.add(Album(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getLong(3), cursor.getInt(4), cursor.getInt(5)))
            } while (cursor.moveToNext())
        cursor?.close()
        return arrayList
    }

    fun getAllAlbums(context: Context, caller: String?): ArrayList<Album> {
        MediaID.currentCaller = caller
        return getAlbumsForCursor(makeAlbumCursor(context, null, null))
    }

    fun getAlbum(context: Context, id: Long): Album {
        return getAlbum(makeAlbumCursor(context, "_id=?", arrayOf(id.toString())))
    }

    fun getAlbums(context: Context, paramString: String, limit: Int): List<Album> {
        val result = getAlbumsForCursor(makeAlbumCursor(context, "album LIKE ?", arrayOf("$paramString%")))
        if (result.size < limit) {
            result.addAll(getAlbumsForCursor(makeAlbumCursor(context, "album LIKE ?", arrayOf("%_$paramString%"))))
        }
        return if (result.size < limit) result else result.subList(0, limit)
    }

    private fun makeAlbumCursor(context: Context, selection: String?, paramArrayOfString: Array<String>?): Cursor? {
        return context.contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, arrayOf("_id", "album", "artist", "artist_id", "numsongs", "minyear"), selection, paramArrayOfString, null)
    }

    fun getSongsForAlbum(context: Context, albumID: Long, caller: String?): ArrayList<Song> {
        MediaID.currentCaller = caller

        val cursor = makeAlbumSongCursor(context, albumID)
        val arrayList = arrayListOf<Song>()
        if (cursor != null && cursor.moveToFirst())
            do {
                val id = cursor.getLong(0)
                val title = cursor.getString(1)
                val artist = cursor.getString(2)
                val album = cursor.getString(3)
                val duration = cursor.getInt(4)
                var trackNumber = cursor.getInt(5)
                /*This fixes bug where some track numbers displayed as 100 or 200*/
                while (trackNumber >= 1000) {
                    trackNumber -= 1000 //When error occurs the track numbers have an extra 1000 or 2000 added, so decrease till normal.
                }
                val artistId = cursor.getInt(6).toLong()

                arrayList.add(Song(id, albumID, artistId, title, artist, album, duration, trackNumber))
            } while (cursor.moveToNext())
        cursor?.close()
        return arrayList
    }

    fun getAlbumsForArtist(context: Context, artistID: Long): ArrayList<Album> {
        val albumList = arrayListOf<Album>()
        val cursor = makeAlbumForArtistCursor(context, artistID)

        if (cursor != null) {
            if (cursor.moveToFirst())
                do {
                    val album = Album(cursor.getLong(0), cursor.getString(1), cursor.getString(2), artistID, cursor.getInt(3), cursor.getInt(4))
                    albumList.add(album)
                } while (cursor.moveToNext())
        }
        cursor?.close()
        return albumList
    }

    private fun makeAlbumForArtistCursor(context: Context, artistID: Long): Cursor? {

        if (artistID.toInt() == -1)
            return null

        return context.contentResolver
                .query(MediaStore.Audio.Artists.Albums.getContentUri("external", artistID),
                        arrayOf("_id", "album", "artist", "numsongs", "minyear"), null, null,
                        MediaStore.Audio.Albums.FIRST_YEAR)
    }

    private fun makeAlbumSongCursor(context: Context, albumID: Long): Cursor? {
        val contentResolver = context.contentResolver
        val albumSongSortOrder = SONG_TRACK_LIST
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val string = "is_music=1 AND title != '' AND album_id=$albumID"
        return contentResolver.query(uri, arrayOf("_id", "title", "artist", "album", "duration", "track", "artist_id"), string, null, albumSongSortOrder)
    }
}
