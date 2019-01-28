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

class DeleteSongDialog : DialogFragment() {

    var callback: () -> Unit? = {
        null
    }

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
       return MaterialDialog(activity!!).show {
            title(text = "Delete song?")
            positiveButton(text = "Delete") {
                MusicUtils.deleteTracks(activity!!, arguments!!.getLongArray("songs")!!)
                callback()
            }
            negativeButton(text = "Cancel")

        }
    }

    companion object {
        @JvmOverloads
        fun newInstance(song: Song? = null): DeleteSongDialog {
            val songs: LongArray
            if (song == null) {
                songs = LongArray(0)
            } else {
                songs = LongArray(1)
                songs[0] = song.id
            }
            return newInstance(songs)
        }

        fun newInstance(songList: LongArray): DeleteSongDialog {
            return DeleteSongDialog().apply {
                arguments = Bundle().apply { putLongArray("songs", songList) }
            }
        }
    }
}
