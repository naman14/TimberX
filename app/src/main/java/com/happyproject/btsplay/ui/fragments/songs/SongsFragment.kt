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
package com.happyproject.btsplay.ui.fragments.songs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.rxkprefs.Pref
import com.google.android.material.snackbar.Snackbar
import com.happyproject.btsplay.PREF_SONG_SORT_ORDER
import com.happyproject.btsplay.R
import com.happyproject.btsplay.constants.SongSortOrder
import com.happyproject.btsplay.constants.SongSortOrder.SONG_A_Z
import com.happyproject.btsplay.constants.SongSortOrder.SONG_DURATION
import com.happyproject.btsplay.constants.SongSortOrder.SONG_YEAR
import com.happyproject.btsplay.constants.SongSortOrder.SONG_Z_A
import com.happyproject.btsplay.databinding.LayoutRecyclerviewBinding
import com.happyproject.btsplay.extensions.*
import com.happyproject.btsplay.models.Song
import com.happyproject.btsplay.ui.adapters.SongsAdapter
import com.happyproject.btsplay.ui.fragments.base.MediaItemFragment
import com.happyproject.btsplay.ui.listeners.SortMenuListener
import com.happyproject.btsplay.util.AutoClearedValue
import org.koin.android.ext.android.inject

class SongsFragment : MediaItemFragment() {
    private lateinit var songsAdapter: SongsAdapter
    private val sortOrderPref by inject<Pref<SongSortOrder>>(name = PREF_SONG_SORT_ORDER)

    var binding by AutoClearedValue<LayoutRecyclerviewBinding>(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflater.inflateWithBinding(R.layout.layout_recyclerview, container)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        songsAdapter = SongsAdapter(this).apply {
            showHeader = true
            popupMenuListener = mainViewModel.popupMenuListener
            sortMenuListener = sortListener
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(safeActivity)
            adapter = songsAdapter
            addOnItemClick { position: Int, _: View ->
                songsAdapter.getSongForPosition(position)?.let { song ->
                    val extras = getExtraBundle(songsAdapter.songs.toSongIds(), getString(R.string.all_songs))
                    mainViewModel.mediaItemClicked(song, extras)
                }
            }
        }

        mediaItemFragmentViewModel.mediaItems
                .observe(this) { list ->
                    val artistName = "BLACKPINK"
                    val songList: MutableList<Song> = mutableListOf()

                    list.forEach {
                        val song = (it as Song)

                        if (song.path.contains("/kpop-player/$artistName") && song.artist == artistName) {
                            songList.add(song)
                        }

                        // song.add(it as Song)
                    }

                    // /storage/emulated/0/Music/03. #Ud718#Ud30c#Ub78c (Acoustic Ver.).mp3
                    @Suppress("UNCHECKED_CAST")
                    songsAdapter.updateData(songList)
                }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Auto trigger a reload when the sort order pref changes
        sortOrderPref.observe()
                .ioToMain()
                .subscribe { mediaItemFragmentViewModel.reloadMediaItems() }
                .disposeOnDetach(view)
    }

    private val sortListener = object : SortMenuListener {
        override fun shuffleAll() {
            songsAdapter.songs.shuffled().apply {
                val extras = getExtraBundle(toSongIds(), getString(R.string.all_songs))

                if (this.isEmpty()) {
                    Snackbar.make(binding.recyclerView, R.string.shuffle_no_songs_error, Snackbar.LENGTH_SHORT)
                            .show()
                } else {
                    mainViewModel.mediaItemClicked(this[0], extras)
                }
            }
        }

        override fun sortAZ() = sortOrderPref.set(SONG_A_Z)

        override fun sortDuration() = sortOrderPref.set(SONG_DURATION)

        override fun sortYear() = sortOrderPref.set(SONG_YEAR)

        override fun numOfSongs() {}

        override fun sortZA() = sortOrderPref.set(SONG_Z_A)
    }
}
