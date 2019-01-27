package com.naman14.timberx.ui.listeners

import com.naman14.timberx.models.Song

interface PopupMenuListener {

    fun goToAlbum(song: Song)

    fun goToArtist(song: Song)
}