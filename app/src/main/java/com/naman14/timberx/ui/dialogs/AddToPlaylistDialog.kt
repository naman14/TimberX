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

import com.afollestad.materialdialogs.MaterialDialog
import com.naman14.timberx.models.Song

import androidx.annotation.NonNull
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.list.listItems
import com.naman14.timberx.models.MediaID
import com.naman14.timberx.util.MusicUtils
import com.naman14.timberx.repository.PlaylistRepository

class AddToPlaylistDialog : DialogFragment() {

    var callback: () -> Unit? = {
        null
    }

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val playlists = PlaylistRepository.getPlaylists(activity!!, MediaID.CALLER_SELF)

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
