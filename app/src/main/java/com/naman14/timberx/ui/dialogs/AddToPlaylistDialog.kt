package com.naman14.timberx.ui.dialogs

import android.app.Dialog
import android.os.Bundle

import com.afollestad.materialdialogs.MaterialDialog
import com.naman14.timberx.models.Song

import androidx.annotation.NonNull
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.list.listItems
import com.naman14.timberx.util.MusicUtils
import com.naman14.timberx.repository.PlaylistRepository

class AddToPlaylistDialog : DialogFragment() {

    var callback: () -> Unit? = {
        null
    }

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val playlists = PlaylistRepository.getPlaylists(activity!!)

        val itemList = arrayListOf<String>()
        itemList.add("Create new playlist")

        for (i in playlists.indices) {
            itemList.add(playlists[i].name)
        }

       return MaterialDialog(activity!!).show {
            title(text = "Add to playlist")

            listItems(items = itemList) { dialog, index, text ->
                if (index == 0) {
                    CreatePlaylistDialog.newInstance(arguments!!.getLongArray("songs")!!)
                            .show(fragmentManager, "CreatePlaylist")
                } else {
                    MusicUtils.addToPlaylist(activity!!, arguments!!.getLongArray("songs")!!,
                            playlists[index - 1].id)
                    dialog.dismiss()
                }
            }
        }
    }

    companion object {
        @JvmOverloads
        fun newInstance(song: Song? = null): AddToPlaylistDialog {
            val songs: LongArray
            if (song == null) {
                songs = LongArray(0)
            } else {
                songs = LongArray(1)
                songs[0] = song.id
            }
            return newInstance(songs)
        }

        fun newInstance(songList: LongArray): AddToPlaylistDialog {
            return AddToPlaylistDialog().apply {
                arguments = Bundle().apply { putLongArray("songs", songList) }
            }
        }
    }
}
