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
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.naman14.timberx.R
import com.naman14.timberx.TimberMusicService.Companion.TYPE_PLAYLIST
import com.naman14.timberx.databinding.FragmentCategorySongsBinding
import com.naman14.timberx.models.CategorySongData
import com.naman14.timberx.models.Song
import com.naman14.timberx.ui.adapters.SongsAdapter
import com.naman14.timberx.ui.fragments.base.MediaItemFragment
import com.naman14.timberx.util.AutoClearedValue
import com.naman14.timberx.constants.Constants.CATEGORY_SONG_DATA
import com.naman14.timberx.extensions.addOnItemClick
import com.naman14.timberx.extensions.argument
import com.naman14.timberx.extensions.getExtraBundle
import com.naman14.timberx.extensions.inflateWithBinding
import com.naman14.timberx.extensions.safeActivity
import com.naman14.timberx.extensions.toSongIds
import kotlinx.android.synthetic.main.fragment_album_detail.recyclerView

class CategorySongsFragment : MediaItemFragment() {
    private lateinit var categorySongData: CategorySongData
    var binding by AutoClearedValue<FragmentCategorySongsBinding>(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        categorySongData = argument(CATEGORY_SONG_DATA)
        binding = inflater.inflateWithBinding(R.layout.fragment_category_songs, container)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.categorySongData = categorySongData

        val adapter = SongsAdapter().apply {
            popupMenuListener = mainViewModel.popupMenuListener
            if (categorySongData.type == TYPE_PLAYLIST) {
                playlistId = categorySongData.id
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(safeActivity)
        recyclerView.adapter = adapter

        mediaItemFragmentViewModel.mediaItems.observe(this,
                Observer<List<MediaBrowserCompat.MediaItem>> { list ->
                    if (list.isNotEmpty()) {
                        @Suppress("UNCHECKED_CAST")
                        adapter.updateData(list as List<Song>)
                    }
                })

        recyclerView.addOnItemClick { position: Int, _: View ->
            val extras = getExtraBundle(adapter.songs.toSongIds(), categorySongData.title)
            mainViewModel.mediaItemClicked(adapter.songs[position], extras)
        }
    }
}
