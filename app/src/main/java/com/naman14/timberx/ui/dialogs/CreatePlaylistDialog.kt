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

package com.naman14.timberx.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast

import com.afollestad.materialdialogs.MaterialDialog
import com.naman14.timberx.models.Song

import androidx.annotation.NonNull
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.input.input
import com.naman14.timberx.util.MusicUtils

class CreatePlaylistDialog : DialogFragment() {

    var callback: () -> Unit? = {
        null
    }

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
                    callback()
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
