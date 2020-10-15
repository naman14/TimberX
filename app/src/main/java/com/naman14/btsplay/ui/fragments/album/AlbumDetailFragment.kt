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
package com.naman14.btsplay.ui.fragments.album

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.naman14.btsplay.R
import com.naman14.btsplay.constants.Constants.ALBUM
import com.naman14.btsplay.databinding.FragmentAlbumDetailBinding
import com.naman14.btsplay.extensions.addOnItemClick
import com.naman14.btsplay.extensions.argument
import com.naman14.btsplay.extensions.filter
import com.naman14.btsplay.extensions.getExtraBundle
import com.naman14.btsplay.extensions.inflateWithBinding
import com.naman14.btsplay.extensions.observe
import com.naman14.btsplay.extensions.safeActivity
import com.naman14.btsplay.extensions.toSongIds
import com.naman14.btsplay.models.Album
import com.naman14.btsplay.models.Song
import com.naman14.btsplay.ui.adapters.SongsAdapter
import com.naman14.btsplay.ui.fragments.base.MediaItemFragment
import com.naman14.btsplay.util.AutoClearedValue

class AlbumDetailFragment : MediaItemFragment() {
    private lateinit var songsAdapter: SongsAdapter
    lateinit var album: Album
    var binding by AutoClearedValue<FragmentAlbumDetailBinding>(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        album = argument(ALBUM)
        binding = inflater.inflateWithBinding(R.layout.fragment_album_detail, container)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.album = album

        songsAdapter = SongsAdapter(this).apply {
            popupMenuListener = mainViewModel.popupMenuListener
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(safeActivity)
            adapter = songsAdapter
            addOnItemClick { position: Int, _: View ->
                val extras = getExtraBundle(songsAdapter.songs.toSongIds(), album.title)
                mainViewModel.mediaItemClicked(songsAdapter.songs[position], extras)
            }
        }

        mediaItemFragmentViewModel.mediaItems
                .filter { it.isNotEmpty() }
                .observe(this) { list ->
                    @Suppress("UNCHECKED_CAST")
                    songsAdapter.updateData(list as List<Song>)
                }
    }
}
