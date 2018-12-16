package com.naman14.timberx.repository

import android.content.Context
import android.database.Cursor
import com.naman14.timberx.vo.Song
import android.provider.MediaStore
import android.support.v4.media.MediaDescriptionCompat
import android.text.TextUtils


object SongsRepository {

    fun loadSongs(context: Context): ArrayList<Song> {
        return  getSongsForCursor(makeSongCursor(context, null, null))
    }


    fun getSongForId(context: Context, id: Long): Song {
        return getSongsForCursor(makeSongCursor(context, "_id = " + id.toString(), null))[0]
    }

    fun getSongsForIDs(context: Context, idList: LongArray): ArrayList<Song> {
        var selection = "_id IN ("
        for (id in idList) {
            selection += id.toString() + ","
        }
        if (idList.isNotEmpty()) {
            selection = selection.substring(0, selection.length - 1)
        }
        selection += ")"

        return getSongsForCursor(makeSongCursor(context, selection, null))
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
        val songSortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        return makeSongCursor(context, selection, paramArrayOfString, songSortOrder)
    }

    private fun makeSongCursor(context: Context, selection: String?, paramArrayOfString: Array<String>?, sortOrder: String?): Cursor {
        var selectionStatement = "is_music=1 AND title != ''"

        if (!TextUtils.isEmpty(selection)) {
            selectionStatement = "$selectionStatement AND $selection"
        }
        return context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf("_id", "title", "artist", "album", "duration", "track", "artist_id", "album_id"),
                selectionStatement, paramArrayOfString, sortOrder)

    }

    fun getSongsForAlbum(context: Context, albumID: Long): java.util.ArrayList<Song> {
        val cursor = makeAlbumSongCursor(context, albumID)
        val arrayList = ArrayList<Song>()
        if (cursor != null && cursor!!.moveToFirst())
            do {
                val id = cursor!!.getLong(0)
                val title = cursor!!.getString(1)
                val artist = cursor!!.getString(2)
                val album = cursor!!.getString(3)
                val duration = cursor!!.getInt(4)
                var trackNumber = cursor!!.getInt(5)
                /*This fixes bug where some track numbers displayed as 100 or 200*/
                while (trackNumber >= 1000) {
                    trackNumber -= 1000 //When error occurs the track numbers have an extra 1000 or 2000 added, so decrease till normal.
                }
                val artistId = cursor!!.getInt(6).toLong()

                arrayList.add(Song(id, albumID, artistId, title, artist, album, duration, trackNumber))
            } while (cursor!!.moveToNext())
        if (cursor != null)
            cursor!!.close()
        return arrayList
    }

    private fun makeAlbumSongCursor(context: Context, albumID: Long): Cursor? {
        val contentResolver = context.contentResolver
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val songSortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        val string = "is_music=1 AND title != '' AND album_id=$albumID"
        return contentResolver.query(uri, arrayOf("_id", "title", "artist", "album", "duration", "track", "artist_id"), string, null, songSortOrder)
    }
}