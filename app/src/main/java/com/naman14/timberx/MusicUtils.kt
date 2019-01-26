package com.naman14.timberx

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.widget.Toast

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
}