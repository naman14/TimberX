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
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.happyproject.blackpinkplay.R
import com.happyproject.blackpinkplay.extensions.addOnItemClick
import com.happyproject.blackpinkplay.extensions.getExtraBundle
import com.happyproject.blackpinkplay.extensions.inflateWithBinding
import com.happyproject.blackpinkplay.databinding.FragmentSearchBinding
import com.happyproject.blackpinkplay.extensions.observe
import com.happyproject.blackpinkplay.extensions.safeActivity
import com.happyproject.blackpinkplay.extensions.toSongIds
import com.happyproject.blackpinkplay.ui.adapters.AlbumAdapter
import com.happyproject.blackpinkplay.ui.adapters.ArtistAdapter
import com.happyproject.blackpinkplay.ui.adapters.SongsAdapter
import com.happyproject.blackpinkplay.ui.fragments.base.BaseNowPlayingFragment
import com.happyproject.blackpinkplay.ui.viewmodels.SearchViewModel
import com.happyproject.blackpinkplay.util.AutoClearedValue
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SearchFragment : BaseNowPlayingFragment() {

    private val searchViewModel by sharedViewModel<SearchViewModel>()

    private lateinit var songAdapter: SongsAdapter
    private lateinit var albumAdapter: AlbumAdapter
    private lateinit var artistAdapter: ArtistAdapter

    var binding by AutoClearedValue<FragmentSearchBinding>(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflater.inflateWithBinding(R.layout.fragment_search, container)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        songAdapter = SongsAdapter(this).apply {
            popupMenuListener = mainViewModel.popupMenuListener
        }
        binding.rvSongs.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = songAdapter
        }

        albumAdapter = AlbumAdapter()
        binding.rvAlbums.apply {
            layoutManager = GridLayoutManager(safeActivity, 3)
            adapter = albumAdapter
            addOnItemClick { position: Int, _: View ->
                mainViewModel.mediaItemClicked(albumAdapter.albums[position], null)
            }
        }

        artistAdapter = ArtistAdapter()
        binding.rvArtist.apply {
            layoutManager = GridLayoutManager(safeActivity, 3)
            adapter = artistAdapter
            addOnItemClick { position: Int, _: View ->
                mainViewModel.mediaItemClicked(artistAdapter.artists[position], null)
            }
        }

        binding.rvSongs.addOnItemClick { position: Int, _: View ->
            songAdapter.getSongForPosition(position)?.let { song ->
                val extras = getExtraBundle(songAdapter.songs.toSongIds(), "All songs")
                mainViewModel.mediaItemClicked(song, extras)
            }
        }
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                searchViewModel.search(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                songAdapter.updateData(emptyList())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        })
        binding.btnBack.setOnClickListener { safeActivity.onBackPressed() }

        searchViewModel.searchLiveData.observe(this) { searchData ->
            songAdapter.updateData(searchData.songs)
            albumAdapter.updateData(searchData.albums)
            artistAdapter.updateData(searchData.artists)
        }

        binding.let {
            it.viewModel = searchViewModel
            it.setLifecycleOwner(this)
        }
    }
}
