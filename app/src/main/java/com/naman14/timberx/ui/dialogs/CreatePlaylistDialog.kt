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
import android.widget.Toast.LENGTH_SHORT
import androidx.annotation.NonNull
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.input.input
import com.naman14.timberx.models.Song
import com.naman14.timberx.util.Constants.SONGS
import com.naman14.timberx.util.MusicUtils

class CreatePlaylistDialog : DialogFragment() {
    interface PlaylistCreatedCallback {
        fun onPlaylistCreated()
    }

    companion object {
        private const val TAG = "CreatePlaylistDialog"

        fun <T> show(parent: T, song: Song? = null) where T : Fragment, T : PlaylistCreatedCallback {
            val songs: LongArray
            if (song == null) {
                songs = LongArray(0)
            } else {
                songs = LongArray(1)
                songs[0] = song.id
            }
            show(parent, songs)
        }

        fun <T> show(parent: T, songList: LongArray) where T : Fragment, T : PlaylistCreatedCallback {
            val dialog = CreatePlaylistDialog().apply {
                arguments = Bundle().apply { putLongArray(SONGS, songList) }
                setTargetFragment(parent, 69)
            }
            dialog.show(parent.fragmentManager, TAG)
        }
    }

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = activity ?: throw IllegalStateException("Not attached")
        val songs = arguments?.getLongArray(SONGS)

        return MaterialDialog(context).show {
            title(text = "Create new playlist") // TODO this should be in strings.xml
            positiveButton(text = "Create") // TODO this should be in strings.xml
            negativeButton(text = "Cancel") // TODO this should be in strings.xml

            // TODO this should be in strings.xml
            input(hint = "Enter playlist name", callback = { _, text ->
                val playlistId = MusicUtils.createPlaylist(context, text.toString())
                if (playlistId.toInt() != -1) {
                    if (songs != null && songs.isNotEmpty()) {
                        MusicUtils.addToPlaylist(context, songs, playlistId)
                    } else {
                        // TODO this should be in strings.xml
                        Toast.makeText(context, "Created playlist", LENGTH_SHORT).show()
                    }
                    (targetFragment as? PlaylistCreatedCallback)?.onPlaylistCreated()
                } else {
                    // TODO this should be in strings.xml
                    Toast.makeText(context, "Unable to create playlist", LENGTH_SHORT).show()
                }
            })

            onDismiss {
                // Make sure the DialogFragment dismisses as well
                this@CreatePlaylistDialog.dismiss()
            }
        }
    }
}
