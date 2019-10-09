/*
 * Copyright (c) 2019 Naman Dwivedi.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package com.naman14.timberx.ui.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.PopupMenu
import com.naman14.timberx.R
import com.naman14.timberx.models.Song
import com.naman14.timberx.ui.listeners.PopupMenuListener

class SongPopupMenu constructor(context: Context, attrs: AttributeSet) : AppCompatImageView(context, attrs) {

    private var popupMenuListener: PopupMenuListener? = null
    private var adapterSong: () -> Song? = {
        null
    }

    //specific for playlist song, need to show remove from playlist
    var playlistId: Long = -1

    init {
        setImageResource(R.drawable.ic_more_vert)
        setOnClickListener {
            val popupMenu = PopupMenu(context, this)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.popup_song_play -> popupMenuListener?.play(adapterSong()!!)
                    R.id.popup_song_goto_album -> popupMenuListener?.goToAlbum(adapterSong()!!)
                    R.id.popup_song_goto_artist -> popupMenuListener?.goToArtist(adapterSong()!!)
                    R.id.popup_song_play_next -> popupMenuListener?.playNext(adapterSong()!!)
                    R.id.popup_song_addto_playlist -> popupMenuListener?.addToPlaylist(context, adapterSong()!!)
                    R.id.popup_song_delete -> popupMenuListener?.deleteSong(context, adapterSong()!!)
                    R.id.popup_song_remove_playlist -> popupMenuListener?.removeFromPlaylist(adapterSong()!!, playlistId)
                }
                true
            }

            popupMenu.inflate(R.menu.menu_popup_song)

            if (playlistId.toInt() != -1)
                popupMenu.menu.findItem(R.id.popup_song_remove_playlist).isVisible = true

            popupMenu.show()
        }
    }

    fun setupMenu(listener: PopupMenuListener?, adapterSong: () -> Song) {
        this.popupMenuListener = listener
        this.adapterSong = adapterSong
    }
}
