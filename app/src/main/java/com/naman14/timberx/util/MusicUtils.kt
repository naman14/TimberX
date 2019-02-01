package com.naman14.timberx.util

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.BaseColumns
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.naman14.timberx.R
import java.io.File
import java.io.FileNotFoundException

object MusicUtils {

    private var mContentValuesCache = arrayOf<ContentValues?>()

    fun createPlaylist(context: Context, name: String?): Long {
        if (name != null && name.isNotEmpty()) {
            val resolver = context.contentResolver
            val projection = arrayOf(MediaStore.Audio.PlaylistsColumns.NAME)
            val selection = MediaStore.Audio.PlaylistsColumns.NAME + " = '" + name + "'"
            var cursor = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                    projection, selection, null, null)
            if (cursor!!.count <= 0) {
                val values = ContentValues(1)
                values.put(MediaStore.Audio.PlaylistsColumns.NAME, name)
                val uri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                        values)
                return java.lang.Long.parseLong(uri!!.lastPathSegment!!)
            }
            if (cursor != null) {
                cursor.close()
                cursor = null
            }
            return -1
        }
        return -1
    }

    fun addToPlaylist(context: Context, ids: LongArray, playlistid: Long) {
        val size = ids.size
        val resolver = context.contentResolver
        val projection = arrayOf("max(" + "play_order" + ")")
        val uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistid)
        var cursor: Cursor? = null
        var base = 0

        try {
            cursor = resolver.query(uri, projection, null, null, null)

            if (cursor != null && cursor.moveToFirst()) {
                base = cursor.getInt(0) + 1
            }
        } finally {
            if (cursor != null) {
                cursor.close()
                cursor = null
            }
        }

        var numinserted = 0
        var offSet = 0
        while (offSet < size) {
            makeInsertItems(ids, offSet, 1000, base)
            numinserted += resolver.bulkInsert(uri, mContentValuesCache)
            offSet += 1000
        }
        val message = context.resources.getQuantityString(
                R.plurals.NNNtrackstoplaylist, numinserted, numinserted)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun removeFromPlaylist(context: Context, id: Long,
                           playlistId: Long) {
        val uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId)
        val resolver = context.contentResolver
        resolver.delete(uri, MediaStore.Audio.Playlists.Members.AUDIO_ID + " = ? ", arrayOf(java.lang.Long.toString(id)))
    }

    fun makeInsertItems(ids: LongArray, offset: Int, len: Int, base: Int) {
        var len = len
        if (offset + len > ids.size) {
            len = ids.size - offset
        }

        if (mContentValuesCache.size != len) {
            mContentValuesCache = arrayOfNulls(len)
        }
        for (i in 0 until len) {
            if (mContentValuesCache[i] == null) {
                mContentValuesCache[i] = ContentValues()
            }
            mContentValuesCache[i]?.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + offset + i)
            mContentValuesCache[i]?.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, ids[offset + i])
        }
    }

    fun deleteTracks(context: Context, list: LongArray) {
        val projection = arrayOf(BaseColumns._ID, MediaStore.MediaColumns.DATA, MediaStore.Audio.AudioColumns.ALBUM_ID)
        val selection = StringBuilder()
        selection.append(BaseColumns._ID + " IN (")
        for (i in list.indices) {
            selection.append(list[i])
            if (i < list.size - 1) {
                selection.append(",")
            }
        }
        selection.append(")")
        val c = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection.toString(), null, null)
        if (c != null) {
            c.moveToFirst()
            // Step 2: Remove selected tracks from the database
            context.contentResolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    selection.toString(), null)

            // Step 3: Remove files from card
            c.moveToFirst()
            while (!c.isAfterLast) {
                val name = c.getString(1)
                val f = File(name)
                try { // File.delete can throw a security exception
                    if (!f.delete()) {
                        // I'm not sure if we'd ever get here (deletion would
                        // have to fail, but no exception thrown)
                        Log.e("MusicUtils", "Failed to delete file $name")
                    }
                    c.moveToNext()
                } catch (ex: SecurityException) {
                    c.moveToNext()
                }

            }
            c.close()
        }

        val message = Utils.makeLabel(context, R.plurals.NNNtracksdeleted, list.size)

        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        context.contentResolver.notifyChange(Uri.parse("content://media"), null)
    }

    fun getSongUri(id: Long): Uri {
        return ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                id)
    }

    fun getRealPathFromURI(context: Context, contentUri: Uri): String {
        val proj = arrayOf(MediaStore.Audio.Media.DATA)
        val cursor = context.contentResolver.query(contentUri, proj, null, null, null)
        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(column_index)
    }

    fun getAlbumArtBitmap(context: Context, albumId: Long?): Bitmap? {
        albumId?: return null
        var artwork: Bitmap? = null
        try {
            artwork = MediaStore.Images.Media.getBitmap(context.contentResolver, Utils.getAlbumArtUri(albumId))
        } catch (e: FileNotFoundException) {
            artwork = BitmapFactory.decodeResource(context.resources, R.drawable.icon)
        }
        return artwork
    }

}