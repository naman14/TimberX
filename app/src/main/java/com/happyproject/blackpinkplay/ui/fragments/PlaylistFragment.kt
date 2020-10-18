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
package com.happyproject.blackpinkplay.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.LinearLayoutManager
import com.happyproject.blackpinkplay.R
import com.happyproject.blackpinkplay.databinding.FragmentPlaylistsBinding
import com.happyproject.blackpinkplay.extensions.*
import com.happyproject.blackpinkplay.models.Playlist
import com.happyproject.blackpinkplay.ui.adapters.PlaylistAdapter
import com.happyproject.blackpinkplay.ui.dialogs.CreatePlaylistDialog
import com.happyproject.blackpinkplay.ui.fragments.base.MediaItemFragment
import com.happyproject.blackpinkplay.util.AutoClearedValue

class PlaylistFragment : MediaItemFragment(), CreatePlaylistDialog.PlaylistCreatedCallback {
    var binding by AutoClearedValue<FragmentPlaylistsBinding>(this)
    private lateinit var playlistAdapter: PlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflater.inflateWithBinding(R.layout.fragment_playlists, container)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        playlistAdapter = PlaylistAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(safeActivity)
            adapter = playlistAdapter
            addItemDecoration(DividerItemDecoration(safeActivity, VERTICAL).apply {
                setDrawable(safeActivity.drawable(R.drawable.divider)!!)
            })
            addOnItemClick { position, _ ->
                mainViewModel.mediaItemClicked(playlistAdapter.playlists[position], null)
            }
        }

        mediaItemFragmentViewModel.mediaItems
                .filter { it.isNotEmpty() }
                .observe(this) { list ->
                    @Suppress("UNCHECKED_CAST")
                    playlistAdapter.updateData(list as List<Playlist>)
                }

        binding.btnNewPlaylist.setOnClickListener {
            CreatePlaylistDialog.show(this@PlaylistFragment)
        }
    }

    override fun onPlaylistCreated() = mediaItemFragmentViewModel.reloadMediaItems()
}
