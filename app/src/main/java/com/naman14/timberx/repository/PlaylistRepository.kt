package com.naman14.timberx.repository

import android.content.Context
import android.database.Cursor
import android.provider.BaseColumns
import android.provider.MediaStore
import com.naman14.timberx.models.Playlist
import java.util.ArrayList

object PlaylistRepository {

    private var mCursor: Cursor? = null

    fun getPlaylists(context: Context): List<Playlist> {

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


    fun makePlaylistCursor(context: Context): Cursor? {
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

    fun getSongCountForPlaylist(context: Context, playlistId: Long): Int {
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
}