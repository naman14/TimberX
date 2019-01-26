package com.naman14.timberx.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast

import com.afollestad.materialdialogs.MaterialDialog
import com.naman14.timberx.models.Song

import androidx.annotation.NonNull
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.input.input
import com.naman14.timberx.MusicUtils

class CreatePlaylistDialog : DialogFragment() {

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
       return MaterialDialog(activity!!).show {
            title(text = "Create new playlist")
            positiveButton(text = "Create")
            negativeButton(text = "Cancel")
            input(hint = "Enter playlist name", callback = { dialog, text ->
                val songs = arguments?.getLongArray("songs")

                val playistId = MusicUtils.createPlaylist(activity!!, text.toString())

                if (playistId.toInt() != -1) {
                    if (songs != null && songs.isNotEmpty())
                        MusicUtils.addToPlaylist(activity!!, songs, playistId)
                    else
                        Toast.makeText(activity, "Created playlist", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(activity, "Unable to create playlist", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    companion object {
        @JvmOverloads
        fun newInstance(song: Song? = null): CreatePlaylistDialog {
            val songs: LongArray
            if (song == null) {
                songs = LongArray(0)
            } else {
                songs = LongArray(1)
                songs[0] = song.id
            }
            return newInstance(songs)
        }

        fun newInstance(songList: LongArray): CreatePlaylistDialog {
            return CreatePlaylistDialog().apply {
                arguments = Bundle().apply { putLongArray("songs", songList) }
            }
        }
    }
}
