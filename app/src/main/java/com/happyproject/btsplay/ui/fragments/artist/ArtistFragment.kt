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
package com.happyproject.btsplay.ui.fragments.artist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.happyproject.btsplay.R
import com.happyproject.btsplay.databinding.LayoutRecyclerviewPaddingBinding
import com.happyproject.btsplay.extensions.*
import com.happyproject.btsplay.models.Artist
import com.happyproject.btsplay.ui.adapters.ArtistAdapter
import com.happyproject.btsplay.ui.fragments.base.MediaItemFragment
import com.happyproject.btsplay.util.AutoClearedValue
import com.happyproject.btsplay.util.SpacesItemDecoration

class ArtistFragment : MediaItemFragment() {
    private lateinit var artistAdapter: ArtistAdapter
    var binding by AutoClearedValue<LayoutRecyclerviewPaddingBinding>(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  inflater.inflateWithBinding(R.layout.layout_recyclerview_padding, container)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        artistAdapter = ArtistAdapter()
        binding.recyclerView.apply {
            val gridSpan = resources.getInteger(R.integer.grid_span)
            layoutManager = GridLayoutManager(safeActivity, gridSpan)
            adapter = artistAdapter
            addOnItemClick { position: Int, _: View ->
                mainViewModel.mediaItemClicked(artistAdapter.artists[position], null)
            }

            val spacingInPixels = resources.getDimensionPixelSize(R.dimen.album_art_spacing)
            addItemDecoration(SpacesItemDecoration(spacingInPixels))
        }

        mediaItemFragmentViewModel.mediaItems
                .filter { it.isNotEmpty() }
                .observe(this) { list ->
                    @Suppress("UNCHECKED_CAST")
                    artistAdapter.updateData(list as List<Artist>)
                }
    }
}
