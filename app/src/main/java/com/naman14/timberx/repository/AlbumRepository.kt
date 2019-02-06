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
import android.provider.MediaStore.Audio.Albums.FIRST_YEAR
import android.provider.MediaStore.Audio.Media.DEFAULT_SORT_ORDER
import android.provider.MediaStore.Audio.Media.TRACK
import com.naman14.timberx.extensions.mapList
import com.naman14.timberx.models.Album
import com.naman14.timberx.models.MediaID
import com.naman14.timberx.models.Song
import android.provider.MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI as ALBUMS_URI
import android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI as SONGS_URI

// TODO make this a normal class that is injected with DI
object AlbumRepository {
    private const val SONG_TRACK_SORT_ORDER = ("$TRACK, $DEFAULT_SORT_ORDER")

    fun getAllAlbums(context: Context, caller: String?): List<Album> {
        MediaID.currentCaller = caller
        return makeAlbumCursor(context, null, null)
                .mapList(true) { Album.fromCursor(this) }
    }

    fun getAlbum(context: Context, id: Long): Album {
        return getAlbum(makeAlbumCursor(context, "_id=?", arrayOf(id.toString())))
    }

    fun getAlbums(context: Context, paramString: String, limit: Int): List<Album> {
        val result = makeAlbumCursor(context, "album LIKE ?", arrayOf("$paramString%"))
                .mapList(true) { Album.fromCursor(this) }
        if (result.size < limit) {
            val moreResults = makeAlbumCursor(context, "album LIKE ?", arrayOf("%_$paramString%"))
                    .mapList(true) { Album.fromCursor(this) }
            result += moreResults
        }
        return if (result.size < limit) {
            result
        } else {
            result.subList(0, limit)
        }
    }

    fun getSongsForAlbum(context: Context, albumId: Long, caller: String?): List<Song> {
        MediaID.currentCaller = caller
        return makeAlbumSongCursor(context, albumId)
                .mapList(true, Song.Companion::fromAlbumSongCursor)
    }

    fun getAlbumsForArtist(context: Context, artistId: Long): List<Album> {
        return makeAlbumForArtistCursor(context, artistId)
                .mapList(true) { Album.fromCursor(this, artistId) }
    }

    private fun getAlbum(cursor: Cursor?): Album {
        return cursor?.use {
            if (cursor.moveToFirst()) {
                Album.fromCursor(cursor)
            } else {
                null
            }
        } ?: Album()
    }

    private fun makeAlbumCursor(context: Context, selection: String?, paramArrayOfString: Array<String>?): Cursor? {
        return context.contentResolver.query(
                ALBUMS_URI,
                arrayOf("_id", "album", "artist", "artist_id", "numsongs", "minyear"),
                selection,
                paramArrayOfString,
                null
        )
    }

    private fun makeAlbumForArtistCursor(context: Context, artistID: Long): Cursor? {
        if (artistID == -1L) {
            return null
        }
        return context.contentResolver.query(
                MediaStore.Audio.Artists.Albums.getContentUri("external", artistID),
                arrayOf("_id", "album", "artist", "numsongs", "minyear"),
                null,
                null,
                FIRST_YEAR
        )
    }

    private fun makeAlbumSongCursor(context: Context, albumID: Long): Cursor? {
        val contentResolver = context.contentResolver
        val selection = "is_music=1 AND title != '' AND album_id=$albumID"
        return contentResolver.query(
                SONGS_URI,
                arrayOf("_id", "title", "artist", "album", "duration", "track", "artist_id"),
                selection,
                null,
                SONG_TRACK_SORT_ORDER
        )
    }
}
