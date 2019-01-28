package com.naman14.timberx.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.appcompat.widget.PopupMenu
import com.naman14.timberx.R
import com.naman14.timberx.models.Song
import com.naman14.timberx.ui.listeners.PopupMenuListener

class SongPopupMenu constructor(context: Context, attrs: AttributeSet): ImageView(context, attrs) {

    private var popupMenuListener: PopupMenuListener? = null
    private var adapterSong: () -> Song? = {
        null
    }

    init {
        setImageResource(R.drawable.ic_more_vert)
        setOnClickListener {
            val popupMenu = PopupMenu(context, this)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.popup_song_goto_album -> popupMenuListener?.goToAlbum(adapterSong()!!)
                    R.id.popup_song_goto_artist -> popupMenuListener?.goToArtist(adapterSong()!!)
                    R.id.popup_song_addto_playlist -> popupMenuListener?.addToPlaylist(adapterSong()!!)
                }
                true
            }

            popupMenu.inflate(R.menu.menu_popup_song)
            popupMenu.show()
        }
    }

    fun setupMenu(listener: PopupMenuListener?, adapterSong: () -> Song) {
        this.popupMenuListener = listener
        this.adapterSong = adapterSong
    }
}