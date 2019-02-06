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
package com.naman14.timberx.ui.fragments.songs

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.naman14.timberx.R
import com.naman14.timberx.models.Song
import com.naman14.timberx.ui.adapters.SongsAdapter
import com.naman14.timberx.ui.fragments.base.MediaItemFragment
import com.naman14.timberx.ui.listeners.SortMenuListener
import com.naman14.timberx.constants.Constants.SONG_SORT_ORDER
import com.naman14.timberx.constants.SongSortOrder.SONG_A_Z
import com.naman14.timberx.constants.SongSortOrder.SONG_DURATION
import com.naman14.timberx.constants.SongSortOrder.SONG_YEAR
import com.naman14.timberx.constants.SongSortOrder.SONG_Z_A
import com.naman14.timberx.extensions.addOnItemClick
import com.naman14.timberx.extensions.defaultPrefs
import com.naman14.timberx.extensions.getExtraBundle
import com.naman14.timberx.extensions.inflateTo
import com.naman14.timberx.extensions.safeActivity
import com.naman14.timberx.extensions.toSongIds
import kotlinx.android.synthetic.main.layout_recyclerview.recyclerView

class SongsFragment : MediaItemFragment() {
    private lateinit var songsAdapter: SongsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflateTo(R.layout.layout_recyclerview, container)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        songsAdapter = SongsAdapter().apply {
            showHeader = true
            popupMenuListener = mainViewModel.popupMenuListener
            sortMenuListener = sortListener
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(safeActivity)
            adapter = songsAdapter
            addOnItemClick { position: Int, _: View ->
                songsAdapter.getSongForPosition(position)?.let { song ->
                    val extras = getExtraBundle(songsAdapter.songs.toSongIds(), getString(R.string.all_songs))
                    mainViewModel.mediaItemClicked(song, extras)
                }
            }
        }

        mediaItemFragmentViewModel.mediaItems.observe(this,
                Observer<List<MediaBrowserCompat.MediaItem>> { list ->
                    val isEmptyList = list?.isEmpty() ?: true
                    if (!isEmptyList) {
                        @Suppress("UNCHECKED_CAST")
                        songsAdapter.updateData(list as List<Song>)
                    }
                })
    }

    private val sortListener = object : SortMenuListener {
        override fun shuffleAll() {
            songsAdapter.songs.shuffled().apply {
                val extras = getExtraBundle(toSongIds(), getString(R.string.all_songs))
                mainViewModel.mediaItemClicked(this[0], extras)
            }
        }

        override fun sortAZ() {
            activity?.defaultPrefs()?.edit {
                putString(SONG_SORT_ORDER, SONG_A_Z)
            }
            mediaItemFragmentViewModel.reloadMediaItems()
        }

        override fun sortDuration() {
            activity?.defaultPrefs()?.edit {
                putString(SONG_SORT_ORDER, SONG_DURATION)
            }
            mediaItemFragmentViewModel.reloadMediaItems()
        }

        override fun sortYear() {
            activity?.defaultPrefs()?.edit {
                putString(SONG_SORT_ORDER, SONG_YEAR)
            }
            mediaItemFragmentViewModel.reloadMediaItems()
        }

        override fun sortZA() {
            activity?.defaultPrefs()?.edit {
                putString(SONG_SORT_ORDER, SONG_Z_A)
            }
            mediaItemFragmentViewModel.reloadMediaItems()
        }
    }
}
