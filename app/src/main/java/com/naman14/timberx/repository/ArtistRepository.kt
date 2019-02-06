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
import com.naman14.timberx.extensions.mapList
import com.naman14.timberx.models.Artist
import com.naman14.timberx.models.MediaID
import com.naman14.timberx.models.Song

// TODO make this a normal class that is injected with DI
object ArtistRepository {

    fun getAllArtists(context: Context, caller: String?): List<Artist> {
        MediaID.currentCaller = caller
        return makeArtistCursor(context, null, null)
                .mapList(true, Artist.Companion::fromCursor)
    }

    fun getArtist(context: Context, id: Long): Artist {
        return makeArtistCursor(context, "_id=?", arrayOf(id.toString()))
                .mapList(true, Artist.Companion::fromCursor)
                .firstOrNull() ?: Artist()
    }

    fun getArtists(context: Context, paramString: String, limit: Int): List<Artist> {
        val results = makeArtistCursor(context, "artist LIKE ?", arrayOf("$paramString%"))
                .mapList(true, Artist.Companion::fromCursor)
        if (results.size < limit) {
            val moreArtists = makeArtistCursor(context, "artist LIKE ?", arrayOf("%_$paramString%"))
                    .mapList(true, Artist.Companion::fromCursor)
            results += moreArtists
        }
        return if (results.size < limit) {
            results
        } else {
            results.subList(0, limit)
        }
    }

    private fun makeArtistCursor(context: Context, selection: String?, paramArrayOfString: Array<String>?): Cursor? {
        val artistSortOrder = MediaStore.Audio.Artists.DEFAULT_SORT_ORDER
        return context.contentResolver.query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                arrayOf("_id", "artist", "number_of_albums", "number_of_tracks"),
                selection,
                paramArrayOfString,
                artistSortOrder
        )
    }

    fun getSongsForArtist(context: Context, artistId: Long, caller: String?): List<Song> {
        MediaID.currentCaller = caller
        return makeArtistSongCursor(context, artistId)
                .mapList(true, Song.Companion::fromCursor)
    }

    private fun makeArtistSongCursor(context: Context, artistId: Long): Cursor? {
        val contentResolver = context.contentResolver
        val artistSongSortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = "is_music=1 AND title != '' AND artist_id=$artistId"
        return contentResolver.query(uri, arrayOf("_id", "title", "artist", "album", "duration", "track", "album_id"), selection, null, artistSongSortOrder)
    }
}
