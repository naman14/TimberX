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
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.rxkprefs.Pref
import com.happyproject.blackpinkplay.PREF_LAST_FOLDER
import com.happyproject.blackpinkplay.R
import com.happyproject.blackpinkplay.databinding.LayoutRecyclerviewPaddingBinding
import com.happyproject.blackpinkplay.extensions.getExtraBundle
import com.happyproject.blackpinkplay.extensions.inflateWithBinding
import com.happyproject.blackpinkplay.extensions.safeActivity
import com.happyproject.blackpinkplay.repository.FoldersRepository
import com.happyproject.blackpinkplay.repository.SongsRepository
import com.happyproject.blackpinkplay.ui.adapters.FolderAdapter
import com.happyproject.blackpinkplay.ui.fragments.base.MediaItemFragment
import com.happyproject.blackpinkplay.util.AutoClearedValue
import org.koin.android.ext.android.inject

class FolderFragment : MediaItemFragment() {
    private lateinit var folderAdapter: FolderAdapter

    private val songsRepository by inject<SongsRepository>()
    private val foldersRepository by inject<FoldersRepository>()
    private val lastFolderPref by inject<Pref<String>>(name = PREF_LAST_FOLDER)

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

        folderAdapter = FolderAdapter(safeActivity, songsRepository, foldersRepository, lastFolderPref)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = folderAdapter
        }
        folderAdapter.init(callback = { song, queueIds, title ->
            val extras = getExtraBundle(queueIds, title)
            mainViewModel.mediaItemClicked(song, extras)
        })
    }
}
