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
import androidx.recyclerview.widget.GridLayoutManager
import com.afollestad.rxkprefs.Pref
import com.naman14.btsplay.PREF_ALBUM_SORT_ORDER
import com.naman14.btsplay.R
import com.naman14.btsplay.constants.AlbumSortOrder
import com.naman14.btsplay.constants.AlbumSortOrder.ALBUM_A_Z
import com.naman14.btsplay.constants.AlbumSortOrder.ALBUM_Z_A
import com.naman14.btsplay.constants.AlbumSortOrder.ALBUM_YEAR
import com.naman14.btsplay.constants.AlbumSortOrder.ALBUM_NUMBER_OF_SONGS
import com.naman14.btsplay.databinding.LayoutRecyclerviewPaddingBinding
import com.naman14.btsplay.extensions.*
import com.naman14.btsplay.models.Album
import com.naman14.btsplay.ui.adapters.AlbumAdapter
import com.naman14.btsplay.ui.fragments.base.MediaItemFragment
import com.naman14.btsplay.ui.listeners.SortMenuListener
import com.naman14.btsplay.util.AutoClearedValue
import com.naman14.btsplay.util.SpacesItemDecoration
import org.koin.android.ext.android.inject

class AlbumsFragment : MediaItemFragment() {
    private lateinit var albumAdapter: AlbumAdapter
    private val sortOrderPref by inject<Pref<AlbumSortOrder>>(name = PREF_ALBUM_SORT_ORDER)

    var binding by AutoClearedValue<LayoutRecyclerviewPaddingBinding>(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflater.inflateWithBinding(R.layout.layout_recyclerview_padding, container)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        albumAdapter = AlbumAdapter().apply {
            showHeader = true
            sortMenuListener = sortListener
        }

        binding.recyclerView.apply {
            val gridSpan = resources.getInteger(R.integer.grid_span)
            layoutManager = GridLayoutManager(safeActivity, gridSpan).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (position == 0) 2 else 1
                    }
                }
            }
            adapter = albumAdapter
            addOnItemClick { position: Int, _: View ->
                albumAdapter.getAlbumForPosition(position)?.let {
                    mainViewModel.mediaItemClicked(it, null)
                }
            }

            val spacingInPixels = resources.getDimensionPixelSize(R.dimen.album_art_spacing)
            addItemDecoration(SpacesItemDecoration(spacingInPixels))
        }

        mediaItemFragmentViewModel.mediaItems
                .filter { it.isNotEmpty() }
                .observe(this) { list ->
                    @Suppress("UNCHECKED_CAST")
                    albumAdapter.updateData(list as List<Album>)
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
        override fun shuffleAll() {}

        override fun sortAZ() {
            sortOrderPref.set(ALBUM_A_Z)
        }

        override fun sortDuration() {}

        override fun sortYear() {
            sortOrderPref.set(ALBUM_YEAR)
        }

        override fun sortZA() {
            sortOrderPref.set(ALBUM_Z_A)
        }

        override fun numOfSongs() {
            sortOrderPref.set(ALBUM_NUMBER_OF_SONGS)
        }
    }
}
