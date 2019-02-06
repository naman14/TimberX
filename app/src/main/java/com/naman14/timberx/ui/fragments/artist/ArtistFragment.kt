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
package com.naman14.timberx.ui.fragments.artist

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.naman14.timberx.R
import com.naman14.timberx.models.Artist
import com.naman14.timberx.ui.adapters.ArtistAdapter
import com.naman14.timberx.ui.fragments.base.MediaItemFragment
import com.naman14.timberx.util.SpacesItemDecoration
import com.naman14.timberx.extensions.addOnItemClick
import com.naman14.timberx.extensions.inflateTo
import com.naman14.timberx.extensions.safeActivity
import kotlinx.android.synthetic.main.layout_recyclerview_padding.*

class ArtistFragment : MediaItemFragment() {
    private lateinit var artistAdapter: ArtistAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflateTo(R.layout.layout_recyclerview_padding, container)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        artistAdapter = ArtistAdapter()
        recyclerView.apply {
            layoutManager = GridLayoutManager(safeActivity, 2)
            adapter = artistAdapter
            addOnItemClick { position: Int, _: View ->
                mainViewModel.mediaItemClicked(artistAdapter.artists[position], null)
            }
        }

        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.album_art_spacing)
        recyclerView.addItemDecoration(SpacesItemDecoration(spacingInPixels))

        mediaItemFragmentViewModel.mediaItems.observe(this,
                Observer<List<MediaBrowserCompat.MediaItem>> { list ->
                    val isEmptyList = list?.isEmpty() ?: true
                    if (!isEmptyList) {
                        @Suppress("UNCHECKED_CAST")
                        artistAdapter.updateData(list as List<Artist>)
                    }
                })
    }
}
