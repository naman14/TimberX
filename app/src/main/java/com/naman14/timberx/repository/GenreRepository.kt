package com.naman14.timberx.repository

import android.content.Context
import android.database.Cursor
import com.naman14.timberx.models.Genre
import android.provider.MediaStore

object GenreRepository {

    private var mCursor: Cursor? = null

    fun getAllGenres(context: Context): ArrayList<Genre> {

        val mGenreList = java.util.ArrayList<Genre>()

        mCursor = makeGenreCusror(context)

        if (mCursor != null && mCursor!!.moveToFirst()) {
            do {

                val id = mCursor!!.getLong(0)

                val name = mCursor!!.getString(1)

                val songCount = getSongCountForGenre(context, id)

                val genre = Genre(id, name, songCount)

                mGenreList.add(genre)
            } while (mCursor!!.moveToNext())
        }
        if (mCursor != null) {
            mCursor!!.close()
            mCursor = null
        }
        return mGenreList
    }

    private fun makeGenreCusror(context: Context): Cursor? {
        val uri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI
        val columns = arrayOf(MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME)
        val orderBy = MediaStore.Audio.Genres.NAME

        return context.contentResolver.query(uri, columns, null, null, orderBy)
    }

    private fun getSongCountForGenre(context: Context, genreID: Long): Int {
        val uri = MediaStore.Audio.Genres.Members.getContentUri("external", genreID)
        val c = context.contentResolver.query(uri, null, null, null, null)

        if (c == null || c.count == 0)
            return -1

        val num = c.count
        c.close()

        return num
    }
}