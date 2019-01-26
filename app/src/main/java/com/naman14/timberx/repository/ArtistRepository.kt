package com.naman14.timberx.repository

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.naman14.timberx.models.Artist
import com.naman14.timberx.models.Song
import java.util.ArrayList

object ArtistRepository {

    fun getArtist(cursor: Cursor?): Artist {
        var artist = Artist()
        if (cursor != null) {
            if (cursor.moveToFirst())
                artist = Artist(cursor.getLong(0), cursor.getString(1), cursor.getInt(2), cursor.getInt(3))
        }
        cursor?.close()
        return artist
    }

    fun getArtistsForCursor(cursor: Cursor?): MutableList<Artist> {
        val arrayList = arrayListOf<Artist>()
        if (cursor != null && cursor.moveToFirst())
            do {
                arrayList.add(Artist(cursor.getLong(0), cursor.getString(1), cursor.getInt(2), cursor.getInt(3)))
            } while (cursor.moveToNext())
        cursor?.close()
        return arrayList
    }

    fun getAllArtists(context: Context): List<Artist> {
        return getArtistsForCursor(makeArtistCursor(context, null, null))
    }

    fun getArtist(context: Context, id: Long): Artist {
        return getArtist(makeArtistCursor(context, "_id=?", arrayOf(id.toString())))
    }

    fun getArtists(context: Context, paramString: String, limit: Int): List<Artist> {
        val result = getArtistsForCursor(makeArtistCursor(context, "artist LIKE ?", arrayOf("$paramString%")))
        if (result.size < limit) {
            result.addAll(getArtistsForCursor(makeArtistCursor(context, "artist LIKE ?", arrayOf("%_$paramString%"))))
        }
        return if (result.size < limit) result else result.subList(0, limit)
    }


    private fun makeArtistCursor(context: Context, selection: String?, paramArrayOfString: Array<String>?): Cursor? {
        val artistSortOrder =  MediaStore.Audio.Artists.DEFAULT_SORT_ORDER;
        return context.contentResolver.query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, arrayOf("_id", "artist", "number_of_albums", "number_of_tracks"), selection, paramArrayOfString, artistSortOrder)
    }

    fun getSongsForArtist(context: Context, artistID: Long): ArrayList<Song> {
        val cursor = makeArtistSongCursor(context, artistID)
        val songsList = arrayListOf<Song>()
        if (cursor != null && cursor.moveToFirst())
            do {
                val id = cursor.getLong(0)
                val title = cursor.getString(1)
                val artist = cursor.getString(2)
                val album = cursor.getString(3)
                val duration = cursor.getInt(4)
                val trackNumber = cursor.getInt(5)
                val albumId = cursor.getInt(6).toLong()
                val artistId = artistID

                songsList.add(Song(id, albumId, artistID, title, artist, album, duration, trackNumber))
            } while (cursor.moveToNext())
        cursor?.close()
        return songsList
    }


    private fun makeArtistSongCursor(context: Context, artistID: Long): Cursor? {
        val contentResolver = context.contentResolver
        val artistSongSortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val string = "is_music=1 AND title != '' AND artist_id=$artistID"
        return contentResolver.query(uri, arrayOf("_id", "title", "artist", "album", "duration", "track", "album_id"), string, null, artistSongSortOrder)
    }
}