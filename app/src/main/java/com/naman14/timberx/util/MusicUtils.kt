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
package com.naman14.timberx.util

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.BaseColumns._ID
import android.provider.MediaStore
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.core.net.toUri
import com.naman14.timberx.R
import java.io.File
import java.io.FileNotFoundException
import android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI as AUDIO_URI
import android.provider.MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI as PLAYLISTS_URI
import android.provider.MediaStore.Audio.Playlists.Members.AUDIO_ID as PLAYLIST_AUDIO_ID
import android.provider.MediaStore.Audio.Playlists.Members.PLAY_ORDER as PLAYLIST_PLAY_ORDER
import android.provider.MediaStore.Audio.PlaylistsColumns.NAME as PLAYLIST_COLUMN_NAME
import timber.log.Timber.d as log

// TODO get rid of this and move things to respective repositories
object MusicUtils {

    private var valuesCache = arrayOf<ContentValues?>()

    fun createPlaylist(context: Context, name: String?): Long {
        log("Creating playlist: $name")
        if (name != null && name.isNotEmpty()) {
            val resolver = context.contentResolver
            val projection = arrayOf(PLAYLIST_COLUMN_NAME)
            val selection = "$PLAYLIST_COLUMN_NAME = '$name'"

            log("Querying $PLAYLISTS_URI")
            resolver.query(PLAYLISTS_URI, projection, selection, null, null)?.use {
                if (it.count <= 0) {
                    val values = ContentValues(1).apply {
                        put(PLAYLIST_COLUMN_NAME, name)
                    }
                    val uri = resolver.insert(PLAYLISTS_URI, values)!!
                    return uri.lastPathSegment!!.toLong()
                }
            } ?: throw IllegalStateException("Unable to query $PLAYLISTS_URI, system returned null.")

            return -1
        }
        return -1
    }

    fun addToPlaylist(context: Context, ids: LongArray, playlistId: Long) {
        log("Adding $ids to playlist $playlistId")
        val size = ids.size
        val resolver = context.contentResolver
        val projection = arrayOf("max($PLAYLIST_PLAY_ORDER)")
        val uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId)
        var base = 0

        log("Querying $uri")
        resolver.query(uri, projection, null, null, null)?.use {
            if (it.moveToFirst()) {
                base = it.getInt(0) + 1
            }
        } ?: throw IllegalStateException("Unable to query $uri, system returned null.")

        var numinserted = 0
        var offSet = 0
        while (offSet < size) {
            makeInsertItems(ids, offSet, 1000, base)
            numinserted += resolver.bulkInsert(uri, valuesCache)
            offSet += 1000
        }
        val message = context.resources.getQuantityString(
                R.plurals.NNNtrackstoplaylist, numinserted, numinserted)
        Toast.makeText(context, message, LENGTH_SHORT).show()
    }

    fun removeFromPlaylist(context: Context, id: Long, playlistId: Long) {
        log("Removing song $id from playlist $playlistId")
        val uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId)
        val resolver = context.contentResolver
        resolver.delete(uri, "$PLAYLIST_AUDIO_ID = ?", arrayOf(id.toString()))
    }

    fun deleteTracks(context: Context, list: LongArray) {
        log("Deleting tracks: $list")
        val projection = arrayOf(
                _ID,
                MediaStore.MediaColumns.DATA,
                MediaStore.Audio.AudioColumns.ALBUM_ID
        )
        val selection = StringBuilder().apply {
            append("$_ID IN (")
            for (i in list.indices) {
                append(list[i])
                if (i < list.size - 1) {
                    append(",")
                }
            }
            append(")")
        }

        log("Querying $AUDIO_URI")
        context.contentResolver.query(
                AUDIO_URI,
                projection,
                selection.toString(),
                null,
                null
        )?.use {
            it.moveToFirst()
            // Step 2: Remove selected tracks from the database
            context.contentResolver.delete(AUDIO_URI, selection.toString(), null)

            // Step 3: Remove files from card
            it.moveToFirst()
            while (!it.isAfterLast) {
                val name = it.getString(1)
                val f = File(name)
                try { // File.delete can throw a security exception
                    if (!f.delete()) {
                        // I'm not sure if we'd ever get here (deletion would
                        // have to fail, but no exception thrown)
                        log("Failed to delete file: $name")
                    }
                    it.moveToNext()
                } catch (ex: SecurityException) {
                    it.moveToNext()
                }
            }
        }

        val message = Utils.makeLabel(context, R.plurals.NNNtracksdeleted, list.size)

        Toast.makeText(context, message, LENGTH_SHORT).show()
        context.contentResolver.notifyChange("content://media".toUri(), null)
    }

    fun getSongUri(id: Long): Uri {
        return ContentUris.withAppendedId(AUDIO_URI, id)
    }

    fun getRealPathFromURI(context: Context, contentUri: Uri): String {
        val projection = arrayOf(MediaStore.Audio.Media.DATA)
        log("Querying $contentUri")
        return context.contentResolver.query(contentUri, projection, null, null, null)?.use {
            val dataIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            if (it.moveToFirst()) {
                it.getString(dataIndex)
            } else {
                ""
            }
        } ?: throw IllegalStateException("Unable to query $contentUri, system returned null.")
    }

    fun getAlbumArtBitmap(context: Context, albumId: Long?): Bitmap? {
        if (albumId == null) return null
        return try {
            MediaStore.Images.Media.getBitmap(context.contentResolver, Utils.getAlbumArtUri(albumId))
        } catch (e: FileNotFoundException) {
            BitmapFactory.decodeResource(context.resources, R.drawable.icon)
        }
    }

    private fun makeInsertItems(ids: LongArray, offset: Int, len: Int, base: Int) {
        var actualLen = len
        if (offset + actualLen > ids.size) {
            actualLen = ids.size - offset
        }

        if (valuesCache.size != actualLen) {
            valuesCache = arrayOfNulls(actualLen)
        }
        for (i in 0 until actualLen) {
            if (valuesCache[i] == null) {
                valuesCache[i] = ContentValues()
            }
            valuesCache[i]?.run {
                put(PLAYLIST_PLAY_ORDER, base + offset + i)
                put(PLAYLIST_AUDIO_ID, ids[offset + i])
            }
        }
    }
}
