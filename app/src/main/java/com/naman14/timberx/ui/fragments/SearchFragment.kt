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

package com.naman14.timberx.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager

import com.naman14.timberx.R
import com.naman14.timberx.databinding.FragmentSearchBinding
import com.naman14.timberx.ui.adapters.AlbumAdapter
import com.naman14.timberx.ui.widgets.RecyclerItemClickListener
import com.naman14.timberx.util.*
import com.naman14.timberx.ui.adapters.ArtistAdapter
import com.naman14.timberx.ui.adapters.SongsAdapter
import com.naman14.timberx.ui.viewmodels.SearchViewModel
import com.naman14.timberx.util.media.getExtraBundle
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment : BaseNowPlayingFragment() {

    private lateinit var searchViewModel: SearchViewModel
    private lateinit var songAdapter: SongsAdapter
    private lateinit var albumAdapter: AlbumAdapter
    private lateinit var artistAdapter: ArtistAdapter

    var binding by AutoClearedValue<FragmentSearchBinding>(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_search, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        searchViewModel = ViewModelProviders
                .of(activity!!, InjectorUtils.provideSearchViewModel(activity!!))
                .get(SearchViewModel::class.java)

        songAdapter = SongsAdapter().apply {
            popupMenuListener = mainViewModel.popupMenuListener
        }

        rvSongs.layoutManager = LinearLayoutManager(activity)
        rvSongs.adapter = songAdapter

        albumAdapter = AlbumAdapter()
        rvAlbums.layoutManager = GridLayoutManager(activity, 3)
        rvAlbums.adapter = albumAdapter

        artistAdapter = ArtistAdapter()
        rvArtist.layoutManager = GridLayoutManager(activity, 3)
        rvArtist.adapter = artistAdapter

        rvSongs.addOnItemClick(object : RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {
                songAdapter.getSongForPosition(position)?.let { song ->
                    mainViewModel.mediaItemClicked(song,
                            getExtraBundle(songAdapter.songs!!.toSongIDs(), "All songs"))
                }
            }
        })

        rvAlbums.addOnItemClick(object : RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {
                mainViewModel.mediaItemClicked(albumAdapter.albums!![position], null)
            }
        })

        rvArtist.addOnItemClick(object : RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {
                mainViewModel.mediaItemClicked(artistAdapter.artists!![position], null)
            }
        })

        etSearch.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                searchViewModel.search(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                songAdapter.updateData(arrayListOf())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
        })

        btnBack.setOnClickListener {
            activity!!.onBackPressed()
        }

        searchViewModel.searchLiveData.observe(this, Observer {
            songAdapter.updateData(it.songs)
            albumAdapter.updateData(it.albums)
            artistAdapter.updateData(it.artists)
        })

        binding.let {
            it.viewModel = searchViewModel
            it.setLifecycleOwner(this)
        }

    }
}
