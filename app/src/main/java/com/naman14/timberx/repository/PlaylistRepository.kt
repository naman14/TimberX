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

import android.content.ContentProviderOperation
import android.content.Context
import android.content.OperationApplicationException
import android.database.Cursor
import android.os.RemoteException
import android.provider.BaseColumns
import android.provider.MediaStore
import com.naman14.timberx.models.MediaID
import com.naman14.timberx.models.Playlist
import com.naman14.timberx.models.Song
import java.util.ArrayList

const val YIELD_FREQUENCY = 100

// TODO make this a normal class that is injected with DI
object PlaylistRepository {

    private var mCursor: Cursor? = null

    fun getPlaylists(context: Context, caller: String?): List<Playlist> {
        MediaID.currentCaller = caller

        val mPlaylistList = ArrayList<Playlist>()

        mCursor = makePlaylistCursor(context)

        if (mCursor != null && mCursor!!.moveToFirst()) {
            do {

                val id = mCursor!!.getLong(0)

                val name = mCursor!!.getString(1)

                val songCount = getSongCountForPlaylist(context, id)

                val playlist = Playlist(id, name, songCount)

                mPlaylistList.add(playlist)
            } while (mCursor!!.moveToNext())
        }
        if (mCursor != null) {
            mCursor!!.close()
            mCursor = null
        }
        return mPlaylistList
    }

    private fun makePlaylistCursor(context: Context): Cursor? {
        return context.contentResolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                arrayOf(BaseColumns._ID, MediaStore.Audio.PlaylistsColumns.NAME), null, null, MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER)
    }

    fun deletePlaylists(context: Context, playlistId: Long) {
        val localUri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI
        val localStringBuilder = StringBuilder()
        localStringBuilder.append("_id IN (")
        localStringBuilder.append(playlistId)
        localStringBuilder.append(")")
        context.contentResolver.delete(localUri, localStringBuilder.toString(), null)
    }

    private fun getSongCountForPlaylist(context: Context, playlistId: Long): Int {
        var c = context.contentResolver.query(
                MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
                arrayOf(BaseColumns._ID), com.naman14.timberx.util.Utils.MUSIC_ONLY_SELECTION, null, null)

        if (c != null) {
            var count = 0
            if (c.moveToFirst()) {
                count = c.count
            }
            c.close()
            c = null
            return count
        }
        return 0
    }

    fun getSongsInPlaylist(context: Context, playlistID: Long, caller: String?): List<Song> {
        MediaID.currentCaller = caller
        val mSongList = ArrayList<Song>()

        val playlistCount = countPlaylist(context, playlistID)

        mCursor = makePlaylistSongCursor(context, playlistID)

        if (mCursor != null) {
            var runCleanup = false
            if (mCursor!!.count != playlistCount) {
                runCleanup = true
            }

            if (!runCleanup && mCursor!!.moveToFirst()) {
                val playOrderCol = mCursor!!.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.PLAY_ORDER)

                var lastPlayOrder = -1
                do {
                    val playOrder = mCursor!!.getInt(playOrderCol)
                    if (playOrder == lastPlayOrder) {
                        runCleanup = true
                        break
                    }
                    lastPlayOrder = playOrder
                } while (mCursor!!.moveToNext())
            }

            if (runCleanup) {

                cleanupPlaylist(context, playlistID, mCursor!!)

                mCursor!!.close()
                mCursor = makePlaylistSongCursor(context, playlistID)
                if (mCursor != null) {
                }
            }
        }

        if (mCursor != null && mCursor!!.moveToFirst()) {
            do {

                val id = mCursor!!.getLong(mCursor!!
                        .getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID))

                val songName = mCursor!!.getString(mCursor!!
                        .getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE))

                val artist = mCursor!!.getString(mCursor!!
                        .getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST))

                val albumId = mCursor!!.getLong(mCursor!!
                        .getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM_ID))

                val artistId = mCursor!!.getLong(mCursor!!
                        .getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST_ID))

                val album = mCursor!!.getString(mCursor!!
                        .getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM))

                val duration = mCursor!!.getLong(mCursor!!
                        .getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION))

                val durationInSecs = duration.toInt() / 1000

                val tracknumber = mCursor!!.getInt(mCursor!!
                        .getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TRACK))

                val song = Song(id, albumId, artistId, songName, artist, album, durationInSecs, tracknumber)

                mSongList.add(song)
            } while (mCursor!!.moveToNext())
        }
        // Close the cursor
        if (mCursor != null) {
            mCursor!!.close()
            mCursor = null
        }
        return mSongList
    }

    private fun cleanupPlaylist(
        context: Context,
        playlistId: Long,
        cursor: Cursor
    ) {
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID)
        val uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId)

        val ops = ArrayList<ContentProviderOperation>()

        ops.add(ContentProviderOperation.newDelete(uri).build())

        if (cursor.moveToFirst() && cursor.count > 0) {
            do {
                val builder = ContentProviderOperation.newInsert(uri)
                        .withValue(MediaStore.Audio.Playlists.Members.PLAY_ORDER, cursor.position)
                        .withValue(MediaStore.Audio.Playlists.Members.AUDIO_ID, cursor.getLong(idCol))

                if ((cursor.position + 1) % YIELD_FREQUENCY == 0) {
                    builder.withYieldAllowed(true)
                }
                ops.add(builder.build())
            } while (cursor.moveToNext())
        }

        try {
            context.contentResolver.applyBatch(MediaStore.AUTHORITY, ops)
        } catch (e: RemoteException) {
        } catch (e: OperationApplicationException) {
        }
    }

    private fun countPlaylist(context: Context, playlistId: Long): Int {
        var c: Cursor? = null
        try {
            c = context.contentResolver.query(
                    MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
                    arrayOf(MediaStore.Audio.Playlists.Members.AUDIO_ID), null, null,
                    MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER)

            if (c != null) {
                return c.count
            }
        } finally {
            if (c != null) {
                c.close()
                c = null
            }
        }

        return 0
    }

    private fun makePlaylistSongCursor(context: Context, playlistID: Long?): Cursor? {
        val mSelection = StringBuilder()
        mSelection.append(MediaStore.Audio.AudioColumns.IS_MUSIC + "=1")
        mSelection.append(" AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''")
        return context.contentResolver.query(
                MediaStore.Audio.Playlists.Members.getContentUri("external", playlistID!!),
                arrayOf(MediaStore.Audio.Playlists.Members._ID, MediaStore.Audio.Playlists.Members.AUDIO_ID, MediaStore.Audio.AudioColumns.TITLE, MediaStore.Audio.AudioColumns.ARTIST, MediaStore.Audio.AudioColumns.ALBUM_ID, MediaStore.Audio.AudioColumns.ARTIST_ID, MediaStore.Audio.AudioColumns.ALBUM, MediaStore.Audio.AudioColumns.DURATION, MediaStore.Audio.AudioColumns.TRACK, MediaStore.Audio.Playlists.Members.PLAY_ORDER), mSelection.toString(), null,
                MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER)
    }
}
