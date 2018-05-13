package com.naman14.timberx.repository

import android.content.Context
import android.database.Cursor
import com.naman14.timberx.vo.Song
import android.provider.MediaStore
import android.text.TextUtils


object SongsRepository {

    fun loadSongs(context: Context): ArrayList<Song> {
        return  getSongsForCursor(makeSongCursor(context, null, null))
    }


    fun getSongsForCursor(cursor: Cursor?): ArrayList<Song> {
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

    fun makeSongCursor(context: Context, selection: String?, paramArrayOfString: Array<String>?): Cursor {
        val songSortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        return makeSongCursor(context, selection, paramArrayOfString, songSortOrder)
    }

    private fun makeSongCursor(context: Context, selection: String?, paramArrayOfString: Array<String>?, sortOrder: String?): Cursor {
        var selectionStatement = "is_music=1 AND title != ''"

        if (!TextUtils.isEmpty(selection)) {
            selectionStatement = "$selectionStatement AND $selection"
        }
        return context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, arrayOf("_id", "title", "artist", "album", "duration", "track", "artist_id", "album_id"), selectionStatement, paramArrayOfString, sortOrder)

    }
}