package com.naman14.timberx.repository

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore

import com.naman14.timberx.vo.Album

object AlbumRepository {

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

    fun getAllAlbums(context: Context): ArrayList<Album> {
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


    fun makeAlbumCursor(context: Context, selection: String?, paramArrayOfString: Array<String>?): Cursor? {
        return context.contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, arrayOf("_id", "album", "artist", "artist_id", "numsongs", "minyear"), selection, paramArrayOfString, null)
    }
}