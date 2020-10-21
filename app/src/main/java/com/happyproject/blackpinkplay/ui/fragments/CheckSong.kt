package com.happyproject.blackpinkplay.ui.fragments

import android.support.v4.media.MediaBrowserCompat
import com.happyproject.blackpinkplay.constants.Constants
import com.happyproject.blackpinkplay.constants.Constants.ARTIST_NAME
import com.happyproject.blackpinkplay.models.Album
import com.happyproject.blackpinkplay.models.Song

object CheckSong {
    // Environment.getExternalStorageDirectory().toString() = /storage/emulated/0/
    // /storage/emulated/0/Music/03. #Ud718#Ud30c#Ub78c (Acoustic Ver.).mp3

    fun getValidSong(
        list: List<MediaBrowserCompat.MediaItem>
    ): List<Song> {
        val songList: MutableList<Song> = mutableListOf()

        list.forEach {
            val song = (it as Song)

            if (song.path.contains(Constants.APP_PACKAGE_NAME) && song.artist.contains(ARTIST_NAME)) {
                songList.add(song)
            }
        }

        return songList
    }

    fun getValidAlbum(
        list: List<MediaBrowserCompat.MediaItem>
    ): List<Album> {
        val albumList: MutableList<Album> = mutableListOf()

        list.forEach {
            val album = (it as Album)

            if (album.artist.contains(ARTIST_NAME)) {
                albumList.add(album)
            }
        }

        return albumList
    }
}