package com.naman14.timberx.ui.listeners

import com.naman14.timberx.models.Song

interface PopupMenuListener {

    fun play(song: Song)

    fun goToAlbum(song: Song)

    fun goToArtist(song: Song)

    fun addToPlaylist(song: Song)

    fun deleteSong(song: Song)

    fun removeFromPlaylist(song: Song, playlistId: Long)

    fun playNext(song: Song)
}